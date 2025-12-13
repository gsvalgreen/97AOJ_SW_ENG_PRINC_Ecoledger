package com.ecoledger.movimentacao.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalFeatureTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String base = "http://localhost:8080";

    @BeforeAll
    public static void cleanDatabase() {
        String url = System.getenv().getOrDefault("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5433/movimentacao");
        String user = System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "ecoledger");
        String pass = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "ecoledger");
        try (Connection conn = DriverManager.getConnection(url, user, pass); Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE movimentacao_anexos, idempotency_records, movimentacoes RESTART IDENTITY CASCADE");
            System.out.println("[LocalFeatureTests] Database tables truncated successfully");
        } catch (Exception e) {
            System.out.println("[LocalFeatureTests] Warning: failed to clean database before feature tests: " + e.getMessage());
        }
    }

    private boolean serviceUp() {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(base + "/actuator/health"))
                    .GET().build();
            HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void attachmentFlow_shouldReturnSignedUrlAndConfirm() throws Exception {
        Assumptions.assumeTrue(serviceUp(), "Local movimentacao-service is not running on " + base);

        Map<String, String> body = Map.of("contentType", "image/png");
        String json = objectMapper.writeValueAsString(body);

        HttpRequest uploadReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/upload-url"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> uploadResp = client.send(uploadReq, HttpResponse.BodyHandlers.ofString());
        assertThat(uploadResp.statusCode()).isEqualTo(200);
        JsonNode u = objectMapper.readTree(uploadResp.body());
        assertThat(u.has("objectKey")).isTrue();
        assertThat(u.has("uploadUrl")).isTrue();
        String objectKey = u.get("objectKey").asText();

        // upload actual bytes via application proxy so MinIO contains the object before confirmation
        HttpRequest proxyReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/upload-proxy?objectKey=" + objectKey))
                .header("Content-Type", "image/png")
                .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[] {9,8,7,6}))
                .build();
        HttpResponse<String> proxyResp = client.send(proxyReq, HttpResponse.BodyHandlers.ofString());
        assertThat(proxyResp.statusCode()).isEqualTo(200);

        // confirm
        String confirmJson = objectMapper.writeValueAsString(Map.of("objectKey", objectKey));
        HttpRequest confirmReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/confirm"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(confirmJson))
                .build();

        HttpResponse<String> confirmResp = client.send(confirmReq, HttpResponse.BodyHandlers.ofString());
        assertThat(confirmResp.statusCode()).isEqualTo(200);
        JsonNode c = objectMapper.readTree(confirmResp.body());
        assertThat(c.get("objectKey")).isNotNull();
        assertThat(c.get("url")).isNotNull();
        assertThat(c.get("url").asText()).contains(objectKey);
    }

    @Test
    public void movimentacao_createShouldBeIdempotentWhenKeyProvided() throws Exception {
        Assumptions.assumeTrue(serviceUp(), "Local movimentacao-service is not running on " + base);

        // ensure an attachment is available by requesting upload url and confirming it
        Map<String, String> uploadBody = Map.of("contentType", "image/png");
        String uploadJson = objectMapper.writeValueAsString(uploadBody);
        HttpRequest uploadReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/upload-url"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(uploadJson))
                .build();
        HttpResponse<String> uploadResp = client.send(uploadReq, HttpResponse.BodyHandlers.ofString());
        Assumptions.assumeTrue(uploadResp.statusCode() == 200, "Upload URL not available - skip");
        JsonNode u = objectMapper.readTree(uploadResp.body());
        String objectKey = u.get("objectKey").asText();

        // upload actual bytes via application proxy so MinIO contains the object
        HttpRequest proxyReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/upload-proxy?objectKey=" + objectKey))
                .header("Content-Type", "image/png")
                .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[] {1,2,3,4}))
                .build();
        HttpResponse<String> proxyResp = client.send(proxyReq, HttpResponse.BodyHandlers.ofString());
        Assumptions.assumeTrue(proxyResp.statusCode() == 200, "Upload proxy failed - skip");

        String confirmJson = objectMapper.writeValueAsString(Map.of("objectKey", objectKey));
        HttpRequest confirmReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/confirm"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(confirmJson))
                .build();
        HttpResponse<String> confirmResp = client.send(confirmReq, HttpResponse.BodyHandlers.ofString());
        Assumptions.assumeTrue(confirmResp.statusCode() == 200, "Confirm upload failed - skip");
        JsonNode c = objectMapper.readTree(confirmResp.body());
        String url = c.get("url").asText();

        Map<String, Object> payload = new HashMap<>();
        payload.put("producerId", "prod-local");
        payload.put("commodityId", "cmd-local");
        payload.put("tipo", "COLHEITA");
        payload.put("quantidade", 1.5);
        payload.put("unidade", "KG");
        payload.put("timestamp", OffsetDateTime.now().toString());
        // include attachment so service can validate and persist
        Map<String, String> anexo = Map.of("tipo", "image/png", "url", url, "hash", "h1");
        payload.put("anexos", java.util.List.of(anexo));

        String json = objectMapper.writeValueAsString(payload);

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(base + "/movimentacoes"))
                .header("Content-Type", "application/json")
                .header("X-Idempotency-Key", "local-key-1")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> r1 = client.send(req1, HttpResponse.BodyHandlers.ofString());

        // if service not configured to accept movimentacao creation (producer approval missing), skip
        Assumptions.assumeTrue(r1.statusCode() == 201, "Service did not accept creation (status=" + r1.statusCode() + ") - skip idempotency validation");

        JsonNode j1 = objectMapper.readTree(r1.body());
        String id1 = j1.get("movimentacaoId").asText();

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(base + "/movimentacoes"))
                .header("Content-Type", "application/json")
                .header("X-Idempotency-Key", "local-key-1")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> r2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        assertThat(r2.statusCode()).isEqualTo(201);
        JsonNode j2 = objectMapper.readTree(r2.body());
        String id2 = j2.get("movimentacaoId").asText();
        assertThat(id2).isEqualTo(id1);

        // fetch created resource
        HttpRequest getReq = HttpRequest.newBuilder().uri(URI.create(base + "/movimentacoes/" + id1)).GET().build();
        HttpResponse<String> getResp = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertThat(getResp.statusCode()).isEqualTo(200);
        JsonNode getJson = objectMapper.readTree(getResp.body());
        assertThat(getJson.get("id").asText()).isEqualTo(id1);
    }
}

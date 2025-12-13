package com.ecoledger.movimentacao.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

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

        Map<String, Object> payload = new HashMap<>();
        payload.put("producerId", "prod-local");
        payload.put("commodityId", "cmd-local");
        payload.put("tipo", "COLHEITA");
        payload.put("quantidade", 1.5);
        payload.put("unidade", "KG");
        payload.put("timestamp", OffsetDateTime.now().toString());

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

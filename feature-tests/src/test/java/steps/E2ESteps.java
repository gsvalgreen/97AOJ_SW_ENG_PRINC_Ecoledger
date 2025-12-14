package steps;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class E2ESteps {

    private String lastCreatedId;
    private String lastCreatedIdSecond;
    private String lastGetResponseBody;
    private int lastGetStatus;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    public static String movimentacaoResponseBody;
    public static int movimentacaoResponseStatus;
    private String lastProducerId;
    private String attachmentUrl;

    @Dado("os serviços de movimentação e auditoria estão disponíveis em localhost")
    public void services_available() {
        String[] services = new String[]{"http://localhost:8082", "http://localhost:8083"};
        String[] probes = new String[]{"/actuator/health", "/"};
        long deadline = System.currentTimeMillis() + 30_000L;
        while (System.currentTimeMillis() < deadline) {
            boolean allUp = true;
            for (String base : services) {
                boolean up = false;
                for (String p : probes) {
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(base + p))
                                .timeout(Duration.ofSeconds(3))
                                .GET()
                                .build();
                        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
                        if (r.statusCode() >= 200 && r.statusCode() < 400) { up = true; break; }
                    } catch (Exception ignored) {}
                }
                if (!up) { allUp = false; break; }
            }
            if (allUp) return;
            try { Thread.sleep(1000L); } catch (InterruptedException ignored) {}
        }
        throw new IllegalStateException("required services not available: movimentacao/auditoria");
    }

    @Quando("eu anexo um arquivo valido para o produtor {string}")
    public void upload_valid_attachment(String producerId) throws Exception {
        lastProducerId = producerId;
        // request signed upload metadata
        JsonObject reqBody = new JsonObject();
        reqBody.addProperty("contentType", "image/png");

        HttpRequest uploadUrlReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/anexos/upload-url"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(reqBody)))
                .build();

        HttpResponse<String> uploadUrlResp = client.send(uploadUrlReq, HttpResponse.BodyHandlers.ofString());
        if (uploadUrlResp.statusCode() != 200) {
            attachmentUrl = null;
            return;
        }
        JsonObject u = gson.fromJson(uploadUrlResp.body(), JsonObject.class);
        if (!u.has("objectKey")) {
            attachmentUrl = null;
            return;
        }
        String objectKey = u.get("objectKey").getAsString();

        // upload bytes via proxy (application will push to MinIO)
        byte[] bytes = ("Teste de anexo para " + producerId).getBytes();
        HttpRequest proxyReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/anexos/upload-proxy?objectKey=" + objectKey))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "image/png")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        HttpResponse<String> proxyResp = client.send(proxyReq, HttpResponse.BodyHandlers.ofString());
        if (proxyResp.statusCode() != 200) {
            attachmentUrl = null;
            return;
        }

        // confirm upload
        JsonObject confirm = new JsonObject();
        confirm.addProperty("objectKey", objectKey);
        HttpRequest confirmReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/anexos/confirm"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(confirm)))
                .build();

        HttpResponse<String> confirmResp = client.send(confirmReq, HttpResponse.BodyHandlers.ofString());
        if (confirmResp.statusCode() == 200 && confirmResp.body() != null && !confirmResp.body().isBlank()) {
            JsonObject json = gson.fromJson(confirmResp.body(), JsonObject.class);
            if (json.has("url")) {
                attachmentUrl = json.get("url").getAsString();
                return;
            }
        }
        attachmentUrl = null;
    }

    @Quando("eu registro uma movimentacao valida para o produtor {string} via API de movimentacao")
    public void register_valid_movement(String producerId) throws Exception {
        // reset last ids
        lastCreatedId = null;
        lastCreatedIdSecond = null;
        lastProducerId = producerId;

        // existing implementation continues below
        JsonObject local = new JsonObject();
        local.addProperty("lat", -23.55052);
        local.addProperty("lon", -46.633308);

        JsonObject body = new JsonObject();
        body.addProperty("producerId", producerId);
        body.addProperty("commodityId", "commodity-1");
        body.addProperty("tipo", "PRODUCAO");
        body.addProperty("quantidade", 10.0);
        body.addProperty("unidade", "KG");
        body.addProperty("timestamp", java.time.OffsetDateTime.now().toString());
        body.add("localizacao", local);

        if (attachmentUrl != null) {
            JsonArray anexos = new JsonArray();
            JsonObject a = new JsonObject();
            a.addProperty("tipo", "image/png");
            a.addProperty("url", attachmentUrl);
            a.addProperty("hash", "h1");
            anexos.add(a);
            body.add("anexos", anexos);
            // clear attachment for next scenarios
            attachmentUrl = null;
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/movimentacoes"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        movimentacaoResponseBody = resp.body();
        movimentacaoResponseStatus = resp.statusCode();
    }

    @Quando("eu registro uma movimentacao invalida para o produtor {string} via API de movimentacao")
    public void register_invalid_movement(String producerId) throws Exception {
        lastProducerId = producerId;

        JsonObject local = new JsonObject();
        local.addProperty("lat", -23.55052);
        local.addProperty("lon", -46.633308);

        JsonObject body = new JsonObject();
        body.addProperty("producerId", producerId);
        body.addProperty("commodityId", "commodity-1");
        body.addProperty("tipo", "PRODUCAO");
        // set a quantity above default maxThreshold (10000) to trigger REPROVADO in RulesEngine
        body.addProperty("quantidade", 100000.0);
        body.addProperty("unidade", "KG");
        body.addProperty("timestamp", java.time.OffsetDateTime.now().toString());
        body.add("localizacao", local);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/movimentacoes"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        movimentacaoResponseBody = resp.body();
        movimentacaoResponseStatus = resp.statusCode();
    }

    @Entao("a API de movimentacao retorna 201")
    public void assert_mov_returned_201() {
        System.out.println("Movimentacao response status: " + movimentacaoResponseStatus);
        System.out.println("Movimentacao response body: " + movimentacaoResponseBody);
        assertEquals(201, movimentacaoResponseStatus, "expected 201 Created from movimentacao API");
    }

    @Entao("o serviço de auditoria registra uma auditoria para o produtor {string} dentro de {int} segundos")
    public void assert_auditoria_created_for_producer(String producerId, Integer segundos) throws Exception {
        boolean found = pollForAuditoria(producerId, segundos);
        assertTrue(found, "Expected an auditoria record for producer " + producerId);
    }

    @Entao("a auditoria para o produtor {string} é publicada com resultado {string} dentro de {int} segundos")
    public void assert_auditoria_result_for_producer(String producerId, String expectedResult, Integer segundos) throws Exception {
        long deadline = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < deadline) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8083/produtores/" + producerId + "/historico-auditorias"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                    JsonElement el = gson.fromJson(resp.body(), JsonElement.class);
                    JsonArray arr = null;
                    if (el.isJsonArray()) {
                        arr = el.getAsJsonArray();
                    } else if (el.isJsonObject() && el.getAsJsonObject().has("items") && el.getAsJsonObject().get("items").isJsonArray()) {
                        arr = el.getAsJsonObject().getAsJsonArray("items");
                    }
                    if (arr != null) {
                        for (JsonElement item : arr) {
                            JsonObject obj = item.getAsJsonObject();
                            String resultado = obj.has("resultado") ? obj.get("resultado").getAsString() : null;
                            if (resultado != null && resultado.equalsIgnoreCase(expectedResult)) {
                                return; // success
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignore and retry
            }
            Thread.sleep(1000);
        }
        fail("Expected auditoria with result '" + expectedResult + "' for producer " + producerId);
    }

    private boolean pollForAuditoria(String producerId, int segundos) throws Exception {
        long deadline = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < deadline) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8083/produtores/" + producerId + "/historico-auditorias"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                    JsonElement el = gson.fromJson(resp.body(), JsonElement.class);
                    if (el.isJsonArray() && el.getAsJsonArray().size() > 0) {
                        return true;
                    }
                    if (el.isJsonObject() && el.getAsJsonObject().has("items") && el.getAsJsonObject().get("items").isJsonArray() && el.getAsJsonObject().getAsJsonArray("items").size() > 0) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // ignore and retry
            }
            Thread.sleep(1000);
        }
        return false;
    }

    @Quando("eu registro uma movimentacao valida para o produtor {string}")
    public void register_valid_movement_short(String producerId) throws Exception {
        register_valid_movement(producerId);
    }

    @Dado("os serviços de movimentação, auditoria e certificacao estão disponíveis em localhost")
    public void services_all_available() {
        String[] services = new String[]{"http://localhost:8082", "http://localhost:8083", "http://localhost:8085"};
        String[] probes = new String[]{"/actuator/health", "/"};
        long deadline = System.currentTimeMillis() + 30_000L;
        while (System.currentTimeMillis() < deadline) {
            boolean allUp = true;
            for (String base : services) {
                boolean up = false;
                for (String p : probes) {
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(base + p))
                                .timeout(Duration.ofSeconds(3))
                                .GET()
                                .build();
                        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
                        if (r.statusCode() >= 200 && r.statusCode() < 400) { up = true; break; }
                    } catch (Exception ignored) {}
                }
                if (!up) { allUp = false; break; }
            }
            if (allUp) return;
            try { Thread.sleep(1000L); } catch (InterruptedException ignored) {}
        }
        throw new IllegalStateException("required services not available: movimentacao/auditoria/certificacao");
    }

    @Entao("o selo para o produtor {string} tem status {string} dentro de {int} segundos")
    public void assert_selo_status_for_producer(String producerId, String expectedStatus, Integer segundos) throws Exception {
        long deadline = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8085/selos/" + producerId))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                    JsonObject jo = gson.fromJson(resp.body(), JsonObject.class);
                    if (jo.has("status")) {
                        String status = jo.get("status").getAsString();
                        if (status.equalsIgnoreCase(expectedStatus)) return; // success
                    }
                }
            } catch (Exception e) { /* ignore */ }
            Thread.sleep(1000);
        }
        fail("Expected selo status '" + expectedStatus + "' for producer " + producerId);
    }

    @Quando("eu solicito recálculo do selo para o produtor {string}")
    public void request_recalculate_selo(String producerId) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("motivo", "feature-tests-recalc");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8085/selos/" + producerId + "/recalcular"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();
        try { client.send(req, HttpResponse.BodyHandlers.ofString()); } catch (Exception ignored) {}
    }

    @Entao("o historico de selo para o produtor {string} contém pelo menos {int} entradas dentro de {int} segundos")
    public void assert_selo_history_contains(String producerId, Integer minEntries, Integer segundos) throws Exception {
        long deadline = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8085/selos/" + producerId + "/historico"))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                    JsonObject jo = gson.fromJson(resp.body(), JsonObject.class);
                    if (jo.has("alteracoes")) {
                        JsonArray arr = jo.getAsJsonArray("alteracoes");
                        if (arr.size() >= minEntries) return;
                    }
                }
            } catch (Exception ignored) {}
            Thread.sleep(1000);
        }
        fail("Expected at least " + minEntries + " history entries for producer " + producerId);
    }
}

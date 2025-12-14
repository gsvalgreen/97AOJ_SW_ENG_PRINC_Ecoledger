package steps;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class E2ESteps {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private String movimentacaoResponseBody;
    private int movimentacaoResponseStatus;
    private String lastProducerId;
    private String attachmentUrl;

    @Given("os serviços de movimentação e auditoria estão disponíveis em localhost")
    public void services_available() {
        // noop - assume docker-compose will expose services on localhost:8082 and 8083
    }

    @When("eu anexo um arquivo valido para o produtor {string}")
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

    @When("eu registro uma movimentacao valida para o produtor {string} via API de movimentacao")
    public void register_valid_movement(String producerId) throws Exception {
        lastProducerId = producerId;
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

    @When("eu registro uma movimentacao invalida para o produtor {string} via API de movimentacao")
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

    @Then("a API de movimentacao retorna 201")
    public void assert_mov_returned_201() {
        System.out.println("Movimentacao response status: " + movimentacaoResponseStatus);
        System.out.println("Movimentacao response body: " + movimentacaoResponseBody);
        assertEquals(201, movimentacaoResponseStatus, "expected 201 Created from movimentacao API");
    }

    @Then("o serviço de auditoria registra uma auditoria para o produtor {string} dentro de {int} seconds")
    public void assert_auditoria_created_for_producer(String producerId, Integer seconds) throws Exception {
        boolean found = pollForAuditoria(producerId, seconds);
        assertTrue(found, "Expected an auditoria record for producer " + producerId);
    }

    @Then("a auditoria para o produtor {string} é publicada com resultado {string} dentro de {int} seconds")
    public void assert_auditoria_result_for_producer(String producerId, String expectedResult, Integer seconds) throws Exception {
        long deadline = System.currentTimeMillis() + seconds * 1000L;
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

    // Portuguese aliases to support feature files that use "segundos"
    @Then("o serviço de auditoria registra uma auditoria para o produtor {string} dentro de {int} segundos")
    public void assert_auditoria_created_for_producer_pt(String producerId, Integer seconds) throws Exception {
        assert_auditoria_created_for_producer(producerId, seconds);
    }

    @Then("a auditoria para o produtor {string} é publicada com resultado {string} dentro de {int} segundos")
    public void assert_auditoria_result_for_producer_pt(String producerId, String expectedResult, Integer seconds) throws Exception {
        assert_auditoria_result_for_producer(producerId, expectedResult, seconds);
    }

    private boolean pollForAuditoria(String producerId, int seconds) throws Exception {
        long deadline = System.currentTimeMillis() + seconds * 1000L;
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
}

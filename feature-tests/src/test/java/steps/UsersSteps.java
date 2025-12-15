package steps;

import com.google.gson.Gson;
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

public class UsersSteps {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private String lastCadastroId;
    private String previousCadastroId;
    private int lastStatus;
    private String lastBody;
    private String lastIdempotencyKey;
    private String lastPayload;

    @Dado("o serviço de usuarios está disponível em localhost:8084")
    public void users_service_available() {
        var base = "http://localhost:8084";
        String[] probes = new String[]{"/actuator/health", "/"};
        long deadline = System.currentTimeMillis() + 30_000L;
        while (System.currentTimeMillis() < deadline) {
            for (String p : probes) {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(base + p))
                            .timeout(Duration.ofSeconds(3))
                            .GET()
                            .build();
                    HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
                    if (r.statusCode() >= 200 && r.statusCode() < 400) return;
                } catch (Exception ignored) {}
            }
            try { Thread.sleep(1000L); } catch (InterruptedException ignored) {}
        }
        throw new IllegalStateException("users-service not available at " + base);
    }

    @Quando("eu submeter um cadastro valido")
    public void submit_valid_cadastro() throws Exception {
        JsonObject payload = new JsonObject();
        String unique = String.valueOf(System.currentTimeMillis());
        payload.addProperty("nome", "Teste " + unique);
        payload.addProperty("email", "tester+" + unique + "@example.com");
        payload.addProperty("documento", "DOC" + unique);
        payload.addProperty("senha", "Senha@123");
        payload.addProperty("role", "produtor");
        var dados = new com.google.gson.JsonObject();
        dados.addProperty("fazenda", "x");
        payload.add("dadosFazenda", dados);
        payload.add("anexos", new com.google.gson.JsonArray());
        lastPayload = gson.toJson(payload);
        lastIdempotencyKey = "ft-key-" + unique;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/cadastros"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", lastIdempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(lastPayload))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        lastStatus = resp.statusCode();
        lastBody = resp.body();
        if (lastStatus == 201 && lastBody != null && !lastBody.isBlank()) {
            var jo = gson.fromJson(lastBody, JsonObject.class);
            if (jo.has("cadastroId")) lastCadastroId = jo.get("cadastroId").getAsString();
        }
    }

    @Entao("o serviço retorna 201 e eu consigo recuperar o cadastro criado")
    public void assert_created_and_get() throws Exception {
        assertEquals(201, lastStatus, "expected 201 Created from users-service");
        assertNotNull(lastCadastroId, "expected cadastroId in response");

        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/cadastros/" + lastCadastroId))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> gresp = client.send(get, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, gresp.statusCode());
        assertTrue(gresp.body().contains("email") || gresp.body().contains("nome"));
    }

    @Quando("eu reenviar o mesmo cadastro com a mesma idempotency key")
    public void resend_same_cadastro_with_same_key() throws Exception {
        assertNotNull(lastPayload, "Nenhum cadastro válido foi submetido antes do reenvio");
        assertNotNull(lastIdempotencyKey, "Chave de idempotência não registrada");
        assertNotNull(lastCadastroId, "Cadastro inicial não disponível para comparação");
        previousCadastroId = lastCadastroId;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/cadastros"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", lastIdempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(lastPayload))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        lastStatus = resp.statusCode();
        lastBody = resp.body();
        lastCadastroId = null;
        if ((lastStatus == 200 || lastStatus == 201) && lastBody != null && !lastBody.isBlank()) {
            var jo = gson.fromJson(lastBody, JsonObject.class);
            if (jo.has("cadastroId")) {
                lastCadastroId = jo.get("cadastroId").getAsString();
            }
        }
    }

    @Entao("o serviço retorna o mesmo cadastroId")
    public void assert_same_cadastro_id() {
        assertTrue(lastStatus == 200 || lastStatus == 201, "Esperava 200 ou 201 no reenvio, mas foi " + lastStatus);
        assertNotNull(previousCadastroId, "Cadastro original não registrado para comparação");
        assertNotNull(lastCadastroId, "Resposta idempotente deve conter cadastroId");
        assertEquals(previousCadastroId, lastCadastroId, "Idempotency-Key deve retornar o mesmo cadastroId");
    }

    @Quando("eu submeter um cadastro inválido")
    public void submit_invalid_cadastro() throws Exception {
        // missing required fields
        JsonObject payload = new JsonObject();
        payload.addProperty("nome", "");
        payload.addProperty("email", "");
        payload.addProperty("documento", "");
        payload.addProperty("senha", "");
        payload.addProperty("role", "");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/cadastros"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        lastStatus = resp.statusCode();
        lastBody = resp.body();
    }

    @Entao("a API retorna 400")
    public void assert_returns_400() {
        assertEquals(400, lastStatus, "expected 400 Bad Request for invalid cadastro");
    }

    @Quando("eu consulto usuarios {word} sem autenticação")
    public void get_user_without_auth(String id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/" + id))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        lastStatus = resp.statusCode();
        lastBody = resp.body();
    }

    @Quando("eu atualizo usuarios {word} status sem autenticação")
    public void patch_status_without_auth(String id) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("status", "APROVADO");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/" + id + "/status"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        lastStatus = resp.statusCode();
        lastBody = resp.body();
    }

    @Entao("a resposta deve ser 401")
    public void assert_401() {
        assertTrue(lastStatus == 401 || lastStatus == 403, "expected 401/403 but was: " + lastStatus);
    }
}

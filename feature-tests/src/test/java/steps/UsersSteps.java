package steps;

import com.google.gson.Gson;
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

public class UsersSteps {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private String lastCadastroId;
    private int lastStatus;
    private String lastBody;

    @Given("o serviço de usuarios está disponível em localhost:8084")
    public void users_service_available() {
        // noop - assume docker-compose exposes users-service on localhost:8084
    }

    @When("eu submeter um cadastro valido")
    public void submit_valid_cadastro() throws Exception {
        JsonObject payload = new JsonObject();
        String unique = String.valueOf(System.currentTimeMillis());
        payload.addProperty("nome", "Teste " + unique);
        payload.addProperty("email", "tester+" + unique + "@example.com");
        payload.addProperty("documento", "DOC" + unique);
        payload.addProperty("role", "produtor");
        var dados = new com.google.gson.JsonObject();
        dados.addProperty("fazenda", "x");
        payload.add("dadosFazenda", dados);
        payload.add("anexos", new com.google.gson.JsonArray());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/usuarios/cadastros"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "ft-key-" + unique)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        lastStatus = resp.statusCode();
        lastBody = resp.body();
        if (lastStatus == 201 && lastBody != null && !lastBody.isBlank()) {
            var jo = gson.fromJson(lastBody, JsonObject.class);
            if (jo.has("cadastroId")) lastCadastroId = jo.get("cadastroId").getAsString();
        }
    }

    @Then("o serviço retorna 201 e eu consigo recuperar o cadastro criado")
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

    @When("eu submeter um cadastro inválido")
    public void submit_invalid_cadastro() throws Exception {
        // missing required fields
        JsonObject payload = new JsonObject();
        payload.addProperty("nome", "");

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

    @Then("a API retorna 400")
    public void assert_returns_400() {
        assertEquals(400, lastStatus, "expected 400 Bad Request for invalid cadastro");
    }

    @When("eu consulto usuarios {word} sem autenticação")
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

    @When("eu atualizo usuarios {word} status sem autenticação")
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

    @Then("a resposta deve ser 401")
    public void assert_401() {
        assertTrue(lastStatus == 401 || lastStatus == 403, "expected 401/403 but was: " + lastStatus);
    }
}

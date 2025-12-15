package support;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserProvisioner {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;
    private final String jwtSecret;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Map<String, ProvisionedUser> provisionedByAlias = new HashMap<>();
    private final List<ProvisionedUser> createdUsers = new ArrayList<>();

    public UserProvisioner() {
        this.baseUrl = normalize(resolveConfig("USERS_SERVICE_BASE_URL", "http://localhost:8084"));
        this.jwtSecret = resolveConfig("USERS_JWT_SECRET", "changeitchangeitchangeitchangeit");
        this.dbUrl = resolveConfig("USERS_DB_URL", "jdbc:postgresql://localhost:5432/users");
        this.dbUser = resolveConfig("USERS_DB_USERNAME", "ecoledger_admin");
        this.dbPassword = resolveConfig("USERS_DB_PASSWORD", "ecoledger_admin");
    }

    public ProvisionedUser ensureProducer(String alias) {
        return provisionedByAlias.computeIfAbsent(alias, this::provisionProducer);
    }

    public void cleanup() {
        if (createdUsers.isEmpty()) {
            provisionedByAlias.clear();
            return;
        }
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM cadastros WHERE id = ?")) {
            for (ProvisionedUser user : new ArrayList<>(createdUsers)) {
                stmt.setObject(1, UUID.fromString(user.cadastroId()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (Exception e) {
            System.err.println("Warning cleaning provisioned users: " + e.getMessage());
        } finally {
            createdUsers.clear();
            provisionedByAlias.clear();
        }
    }

    private ProvisionedUser provisionProducer(String alias) {
        try {
            String uniqueSuffix = alias.replaceAll("[^a-zA-Z0-9]", "") + "-" + UUID.randomUUID();

            JsonObject payload = new JsonObject();
            payload.addProperty("nome", "FT Produtor " + alias);
            payload.addProperty("email", "ft+" + uniqueSuffix + "@example.com");
            payload.addProperty("documento", generateDocumento());
            payload.addProperty("role", "produtor");

            JsonObject fazenda = new JsonObject();
            fazenda.addProperty("fazenda", "FT Fazenda " + alias);
            payload.add("dadosFazenda", fazenda);

            payload.add("anexos", new JsonArray());

            String cadastroId = submitCadastro(payload, uniqueSuffix);
            String usuarioId = fetchUsuarioId(cadastroId);
            approveUsuario(usuarioId);

            ProvisionedUser user = new ProvisionedUser(alias, usuarioId, cadastroId, payload.get("email").getAsString());
            createdUsers.add(user);
            return user;
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao provisionar produtor '" + alias + "': " + ex.getMessage(), ex);
        }
    }

    private String submitCadastro(JsonObject payload, String uniqueSuffix) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/usuarios/cadastros"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "ft-users-" + uniqueSuffix)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new IllegalStateException("Falha ao criar cadastro: status=" + response.statusCode() + " body=" + response.body());
        }
        JsonObject body = gson.fromJson(response.body(), JsonObject.class);
        String cadastroId = optString(body, "cadastroId");
        if (cadastroId == null) {
            throw new IllegalStateException("cadastroId ausente na resposta: " + response.body());
        }
        return cadastroId;
    }

    private String fetchUsuarioId(String cadastroId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/usuarios/cadastros/" + cadastroId))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Falha ao buscar cadastro " + cadastroId + ": status=" + response.statusCode());
        }
        JsonObject body = gson.fromJson(response.body(), JsonObject.class);
        JsonObject candidato = body.getAsJsonObject("candidatoUsuario");
        if (candidato == null || !candidato.has("id")) {
            throw new IllegalStateException("Resposta de cadastro sem candidatoUsuario: " + response.body());
        }
        return candidato.get("id").getAsString();
    }

    private void approveUsuario(String usuarioId) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("status", "APROVADO");
        payload.addProperty("reason", "feature-tests auto approval");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/usuarios/" + usuarioId + "/status"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + generateToken("feature-tests", "admin:usuarios usuarios:read usuarios:write"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Falha ao aprovar usuario " + usuarioId + ": status=" + response.statusCode() + " body=" + response.body());
        }
    }

    private String generateToken(String subject, String scopes) throws Exception {
        JsonObject header = new JsonObject();
        header.addProperty("alg", "HS256");
        header.addProperty("typ", "JWT");

        JsonObject payload = new JsonObject();
        long now = Instant.now().getEpochSecond();
        payload.addProperty("sub", subject);
        payload.addProperty("scopes", scopes);
        payload.addProperty("iat", now);
        payload.addProperty("exp", now + 3600);

        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.toString().getBytes(StandardCharsets.UTF_8));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));

        String signingInput = encodedHeader + "." + encodedPayload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        return signingInput + "." + encodedSignature;
    }

    private String generateDocumento() {
        long raw = Math.abs(UUID.randomUUID().getMostSignificantBits());
        long normalized = raw % 1_000_000_00000L;
        return String.format("%011d", normalized);
    }

    private static String resolveConfig(String key, String defaultValue) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return defaultValue;
    }

    private static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String optString(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }
}

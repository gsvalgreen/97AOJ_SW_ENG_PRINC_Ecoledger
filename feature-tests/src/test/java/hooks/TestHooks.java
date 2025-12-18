package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import support.ScenarioContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;

public class TestHooks {

    @Before
    public void prepare() {
        waitForService("http://localhost:8082/actuator/health", 60);
        waitForService("http://localhost:8083/actuator/health", 60);
        waitForService("http://localhost:8085/actuator/health", 60);

        cleanupUsersDb();
        // attempt to truncate tables in movimentacao and auditoria databases to keep tests idempotent
        truncateDb("jdbc:postgresql://localhost:5432/movimentacao", "ecoledger_admin", "ecoledger_admin", "movimentacao_anexos");
        truncateDb("jdbc:postgresql://localhost:5432/movimentacao", "ecoledger_admin", "ecoledger_admin", "movimentacoes");
        truncateDb("jdbc:postgresql://localhost:5432/movimentacao", "ecoledger_admin", "ecoledger_admin", "idempotency_records");
        // auditoria tables
        truncateDb("jdbc:postgresql://localhost:5432/auditoria", "ecoledger_admin", "ecoledger_admin", "auditoria_evidencias");
        truncateDb("jdbc:postgresql://localhost:5432/auditoria", "ecoledger_admin", "ecoledger_admin", "registro_auditorias");
        // certificacao tables
        truncateDb("jdbc:postgresql://localhost:5432/certificacao", "ecoledger_admin", "ecoledger_admin", "selo_motivos");
        truncateDb("jdbc:postgresql://localhost:5432/certificacao", "ecoledger_admin", "ecoledger_admin", "alteracoes_selo");
        truncateDb("jdbc:postgresql://localhost:5432/certificacao", "ecoledger_admin", "ecoledger_admin", "selos");

        // ensure an initial attachment exists for tests (upload-url -> upload-proxy -> confirm)
        try {
            createInitialAttachment();
        } catch (Exception e) {
            System.err.println("Warning: failed to create initial attachment: " + e.getMessage());
        }

        ScenarioContext.init();
    }

    @After
    public void cleanScenario() {
        ScenarioContext.cleanup();
        cleanupUsersDb();
    }

    private void createInitialAttachment() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String base = "http://localhost:8082";
        // 1) request upload-url
        HttpRequest uploadUrlReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/upload-url"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"contentType\":\"image/png\"}"))
                .build();
        HttpResponse<String> uploadUrlResp = client.send(uploadUrlReq, HttpResponse.BodyHandlers.ofString());
        if (uploadUrlResp.statusCode() != 200) {
            throw new IllegalStateException("upload-url returned " + uploadUrlResp.statusCode());
        }
        String body = uploadUrlResp.body();
        int keyIndex = body.indexOf("\"objectKey\"");
        if (keyIndex == -1) {
            throw new IllegalStateException("objectKey not found in upload-url response: " + body);
        }
        int colon = body.indexOf(':', keyIndex);
        int firstQuote = body.indexOf('"', colon);
        int secondQuote = body.indexOf('"', firstQuote + 1);
        if (firstQuote == -1 || secondQuote == -1) {
            throw new IllegalStateException("objectKey value not found in upload-url response: " + body);
        }
        String objectKey = body.substring(firstQuote + 1, secondQuote);

        // 2) upload proxy bytes
        byte[] bytes = ("initial-attachment-" + System.currentTimeMillis()).getBytes();
        HttpRequest proxyReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/upload-proxy?objectKey=" + objectKey))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "image/png")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        HttpResponse<String> proxyResp = client.send(proxyReq, HttpResponse.BodyHandlers.ofString());
        if (proxyResp.statusCode() != 200) {
            throw new IllegalStateException("upload-proxy returned " + proxyResp.statusCode());
        }

        // 3) confirm
        String confirmJson = "{\"objectKey\":\"" + objectKey + "\"}";
        HttpRequest confirmReq = HttpRequest.newBuilder()
                .uri(URI.create(base + "/anexos/confirm"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(confirmJson))
                .build();
        HttpResponse<String> confirmResp = client.send(confirmReq, HttpResponse.BodyHandlers.ofString());
        if (confirmResp.statusCode() != 200) {
            throw new IllegalStateException("confirm upload returned " + confirmResp.statusCode());
        }
        System.out.println("Created initial attachment objectKey=" + objectKey);
    }

    private void waitForService(String url, int seconds) {
        HttpClient client = HttpClient.newHttpClient();
        long deadline = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    System.out.println("Service ready: " + url);
                    return;
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        System.err.println("Warning: service did not become ready: " + url);
    }

    private void truncateDb(String jdbcUrl, String user, String password, String table) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement st = conn.createStatement()) {
            // Use to_regclass to check existence safely, then truncate
            String checkSql = "SELECT to_regclass('public.' || '" + table + "')";
            try (var rs = st.executeQuery(checkSql)) {
                if (rs.next() && rs.getString(1) != null) {
                    String sql = "TRUNCATE TABLE public.\"" + table + "\" CASCADE";
                    st.execute(sql);
                } else {
                    System.out.println("Table not present, skipping truncate: " + table + " on " + jdbcUrl);
                }
            }
        } catch (Exception e) {
            // ignore: DB may not be ready or tables may not exist yet
            System.err.println("Warning: could not truncate " + table + " on " + jdbcUrl + " - " + e.getMessage());
        }
    }

    private void cleanupUsersDb() {
        String url = resolveConfig("USERS_DB_URL", "jdbc:postgresql://localhost:5432/users");
        String user = resolveConfig("USERS_DB_USERNAME", "ecoledger_users");
        String password = resolveConfig("USERS_DB_PASSWORD", "ecoledger_users");
        truncateDb(url, user, password, "idempotency_keys");
        truncateDb(url, user, password, "cadastros");
        truncateDb(url, user, password, "usuarios");
    }

    private String resolveConfig(String key, String defaultValue) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;
        return defaultValue;
    }
}

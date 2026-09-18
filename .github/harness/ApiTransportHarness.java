import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.JSONUtil;
import com.formulamanager.sokker.auxiliares.SokkerApiHttp;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public final class ApiTransportHarness {
    private ApiTransportHarness() {
    }

    public static void main(String[] args) throws Exception {
        loginAndJsonRequestsBypassHtmlUnitAndKeepSession();
        System.out.println("API transport harness OK");
    }

    @SuppressWarnings("unchecked")
    private static void loginAndJsonRequestsBypassHtmlUnitAndKeepSession() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/auth/login", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    exchange.close();
                    return;
                }
                String requestBody = read(exchange.getRequestBody());
                String expected = "{\"login\":\"user\",\"password\":\"p\\\"ass\\\\word\",\"remember\":false}";
                if (!expected.equals(requestBody)) {
                    byte[] body = ("Unexpected login body: " + requestBody).getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(400, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                    return;
                }
                exchange.getResponseHeaders().add("Set-Cookie", "PHPSESSID=test-session; Path=/; HttpOnly");
                byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            }
        });
        server.createContext("/api/current", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String cookie = exchange.getRequestHeaders().getFirst("Cookie");
                if (cookie == null || !cookie.contains("PHPSESSID=test-session")) {
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }
                byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            }
        });
        server.start();

        WebClient navegador = new WebClient() {
            private static final long serialVersionUID = 1L;

            @Override
            public <P extends Page> P getPage(String url)
                    throws IOException, FailingHttpStatusCodeException, MalformedURLException {
                throw new AssertionError("JSON API requests must not go through HtmlUnit");
            }
        };

        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            SokkerApiHttp.login(navegador, base + "/api/auth/login", "user", "p\"ass\\word");
            Object response = JSONUtil.getJson(navegador, base + "/api/current");
            LinkedHashMap<String, Object> json = (LinkedHashMap<String, Object>) response;
            if (!Boolean.TRUE.equals(json.get("ok"))) {
                throw new AssertionError("Direct API transport did not return the expected JSON payload");
            }
        } finally {
            navegador.close();
            server.stop(0);
        }
    }

    private static String read(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.JSONUtil;
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
        jsonRequestsBypassHtmlUnit();
        System.out.println("API transport harness OK");
    }

    @SuppressWarnings("unchecked")
    private static void jsonRequestsBypassHtmlUnit() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/current", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
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
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/api/current";
            Object response = JSONUtil.getJson(navegador, url);
            LinkedHashMap<String, Object> json = (LinkedHashMap<String, Object>) response;
            if (!Boolean.TRUE.equals(json.get("ok"))) {
                throw new AssertionError("Direct API transport did not return the expected JSON payload");
            }
        } finally {
            navegador.close();
            server.stop(0);
        }
    }
}

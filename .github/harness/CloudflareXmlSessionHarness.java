package com.formulamanager.sokker.auxiliares;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public final class CloudflareXmlSessionHarness {
    private static final String EXPECTED_USER_AGENT = "Sokker Asistente (+https://raqueto.com/sokker/asistente)";
    private static final String EXPECTED_CLIENT_KEY = "skc_bae02f2686bf9038d248";

    private CloudflareXmlSessionHarness() {
    }

    public static void main(String[] args) throws Exception {
        userAgentAndXmlSessionAreCompatible();
        failedXmlSessionIsRejected();
        System.out.println("Cloudflare/XML session harness OK");
    }

    private static void userAgentAndXmlSessionAreCompatible() throws Exception {
        final String login = "test-user";
        final String password = "p@ ss+word";
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/start.php", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    throw new AssertionError("XML session must use POST");
                }
                if (!"session=xml".equals(exchange.getRequestURI().getQuery())) {
                    throw new AssertionError("XML session query must be session=xml");
                }
                String userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
                if (!EXPECTED_USER_AGENT.equals(userAgent)) {
                    throw new AssertionError("Unexpected User-Agent: " + userAgent);
                }
                String clientKey = exchange.getRequestHeaders().getFirst("X-Sokker-Client");
                if (!EXPECTED_CLIENT_KEY.equals(clientKey)) {
                    throw new AssertionError("Unexpected X-Sokker-Client: " + clientKey);
                }
                Map<String, String> form = parseForm(readAll(exchange.getRequestBody()));
                if (!login.equals(form.get("ilogin"))) {
                    throw new AssertionError("Missing/invalid ilogin");
                }
                if (!password.equals(form.get("ipassword"))) {
                    throw new AssertionError("Missing/invalid ipassword");
                }
                byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Set-Cookie", "XMLSESSID=test-session; Path=/");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            }
        });
        server.start();

        WebClient navegador = Navegador.createSokkerWebClient();
        try {
            if (!EXPECTED_USER_AGENT.equals(navegador.getBrowserVersion().getUserAgent())) {
                throw new AssertionError("BrowserVersion must expose the Sokker Asistente User-Agent");
            }
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            Navegador.startXmlSession(navegador, baseUrl, login, password);
            if (navegador.getCookieManager().getCookie("XMLSESSID") == null) {
                throw new AssertionError("XMLSESSID cookie was not preserved in HtmlUnit");
            }
        } finally {
            navegador.close();
            server.stop(0);
        }
    }

    private static void failedXmlSessionIsRejected() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/start.php", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] body = "FAILED errorno=1".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            }
        });
        server.start();

        WebClient navegador = Navegador.createSokkerWebClient();
        try {
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            try {
                Navegador.startXmlSession(navegador, baseUrl, "bad", "bad");
                throw new AssertionError("FAILED XML session response must throw LoginException");
            } catch (LoginException expected) {
                // expected
            }
        } finally {
            navegador.close();
            server.stop(0);
        }
    }

    private static Map<String, String> parseForm(String body) throws IOException {
        Map<String, String> values = new HashMap<String, String>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], "UTF-8") : "";
            values.put(key, value);
        }
        return values;
    }

    private static String readAll(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}

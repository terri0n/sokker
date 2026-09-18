package com.formulamanager.sokker.auxiliares;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * HTTP transport for Sokker JSON endpoints.
 *
 * HtmlUnit is intentionally not used here. Sokker is behind Cloudflare and old
 * HtmlUnit browser fingerprints can receive a managed JavaScript challenge
 * instead of the JSON API response. The WebClient remains the shared session
 * holder so legacy HTML/XML fallbacks can keep using the same cookies.
 */
public final class SokkerApiHttp {
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final String USER_AGENT = "SokkerAsistente/1.0";

    private SokkerApiHttp() {
    }

    public static void login(WebClient navegador, String url, String login, String password) throws IOException {
        String body = "{\"login\":\"" + escapeJson(login) + "\",\"password\":\""
                + escapeJson(password) + "\",\"remember\":false}";
        request(navegador, url, "POST", body);
    }

    public static String get(WebClient navegador, String url) throws IOException {
        return request(navegador, url, "GET", null);
    }

    private static String request(WebClient navegador, String urlText, String method, String requestBody)
            throws IOException {
        URL url = new URL(urlText);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json, text/plain, */*");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        addCookies(navegador, connection);

        if (requestBody != null) {
            byte[] bytes = requestBody.getBytes(StandardCharsets.UTF_8);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
        }

        int status = connection.getResponseCode();
        storeCookies(navegador, url, connection.getHeaderFields());
        String contentType = connection.getContentType();
        String responseBody = readResponse(connection, status);
        connection.disconnect();

        if (isCloudflareChallenge(status, contentType, responseBody)) {
            throw new HttpStatusException(status,
                    "Sokker API returned a Cloudflare browser challenge instead of JSON", contentType);
        }
        if (status < 200 || status >= 300) {
            throw new HttpStatusException(status, "Sokker API returned HTTP " + status, contentType);
        }
        return responseBody;
    }

    private static void addCookies(WebClient navegador, HttpURLConnection connection) {
        if (navegador == null || navegador.getCookieManager() == null) {
            return;
        }
        Set<Cookie> cookies = navegador.getCookieManager().getCookies();
        if (cookies == null || cookies.isEmpty()) {
            return;
        }

        StringBuilder header = new StringBuilder();
        for (Cookie cookie : cookies) {
            if (header.length() > 0) {
                header.append("; ");
            }
            header.append(cookie.getName()).append('=').append(cookie.getValue());
        }
        connection.setRequestProperty("Cookie", header.toString());
    }

    private static void storeCookies(WebClient navegador, URL url, Map<String, List<String>> headers) {
        if (navegador == null || navegador.getCookieManager() == null || headers == null) {
            return;
        }

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() == null || !"Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            for (String header : entry.getValue()) {
                if (header == null) {
                    continue;
                }
                String first = header.split(";", 2)[0];
                int equals = first.indexOf('=');
                if (equals <= 0) {
                    continue;
                }
                String name = first.substring(0, equals).trim();
                String value = first.substring(equals + 1).trim();
                navegador.getCookieManager().addCookie(new Cookie(url.getHost(), name, value));
            }
        }
    }

    private static String readResponse(HttpURLConnection connection, int status) throws IOException {
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (input == null) {
            return "";
        }
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static boolean isCloudflareChallenge(int status, String contentType, String body) {
        if (status != 403 || body == null) {
            return false;
        }
        String lowerType = contentType == null ? "" : contentType.toLowerCase();
        return lowerType.contains("text/html")
                && (body.contains("__cf_chl_") || body.contains("challenge-platform") || body.contains("Just a moment"));
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case '\\':
                escaped.append("\\\\");
                break;
            case '"':
                escaped.append("\\\"");
                break;
            case '\b':
                escaped.append("\\b");
                break;
            case '\f':
                escaped.append("\\f");
                break;
            case '\n':
                escaped.append("\\n");
                break;
            case '\r':
                escaped.append("\\r");
                break;
            case '\t':
                escaped.append("\\t");
                break;
            default:
                if (c < 0x20) {
                    String hex = Integer.toHexString(c);
                    escaped.append("\\u");
                    for (int padding = hex.length(); padding < 4; padding++) {
                        escaped.append('0');
                    }
                    escaped.append(hex);
                } else {
                    escaped.append(c);
                }
            }
        }
        return escaped.toString();
    }

    public static final class HttpStatusException extends IOException {
        private static final long serialVersionUID = 1L;
        private final int statusCode;
        private final String contentType;

        private HttpStatusException(int statusCode, String message, String contentType) {
            super(message);
            this.statusCode = statusCode;
            this.contentType = contentType;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getContentType() {
            return contentType;
        }
    }
}

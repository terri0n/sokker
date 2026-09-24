package com.formulamanager.sokker.asistente;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

final class SokkerProxy {
    static final String SOKKER_CLIENT_HEADER = "X-Sokker-Client";
    static final String SOKKER_CLIENT_KEY = "skc_bae02f2686bf9038d248";

    private SokkerProxy() {
    }

    static boolean isSokkerUrl(String url) {
        URI uri = parseUri(url);
        return uri != null
                && "https".equalsIgnoreCase(uri.getScheme())
                && uri.getHost() != null
                && "sokker.org".equalsIgnoreCase(uri.getHost());
    }

    static boolean isLegacyXmlSessionLogin(String method, String url) {
        if (!"POST".equalsIgnoreCase(method) || !isSokkerUrl(url)) {
            return false;
        }

        URI uri = parseUri(url);
        if (uri == null || !"/start.php".equals(uri.getPath())) {
            return false;
        }

        Map<String, String> query = rawQuery(uri);
        return "xml".equals(query.get("session"))
                && query.containsKey("ilogin")
                && query.containsKey("ipassword");
    }

    static String buildLegacyLoginBody(String url) {
        if (!isSokkerUrl(url)) {
            return null;
        }

        URI uri = parseUri(url);
        if (uri == null || !"/start.php".equals(uri.getPath())) {
            return null;
        }

        Map<String, String> query = rawQuery(uri);
        if (!"xml".equals(query.get("session"))
                || !query.containsKey("ilogin")
                || !query.containsKey("ipassword")) {
            return null;
        }

        return "ilogin=" + query.get("ilogin") + "&ipassword=" + query.get("ipassword");
    }

    static Map<String, String> buildForwardHeaders(Map<String, String> requestHeaders) {
        Map<String, String> result = new LinkedHashMap<>();
        boolean hasClientHeader = false;

        if (requestHeaders != null) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null || isConnectionManagedHeader(name)) {
                    continue;
                }

                result.put(name, entry.getValue());
                if (SOKKER_CLIENT_HEADER.equalsIgnoreCase(name)) {
                    hasClientHeader = true;
                }
            }
        }

        if (!hasClientHeader) {
            result.put(SOKKER_CLIENT_HEADER, SOKKER_CLIENT_KEY);
        }

        return result;
    }

    static InputStream responseStream(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status >= 400) {
            InputStream error = connection.getErrorStream();
            return error != null ? error : new ByteArrayInputStream(new byte[0]);
        }
        return connection.getInputStream();
    }

    private static boolean isConnectionManagedHeader(String name) {
        return "Host".equalsIgnoreCase(name)
                || "Content-Length".equalsIgnoreCase(name)
                || "Connection".equalsIgnoreCase(name)
                || "Proxy-Connection".equalsIgnoreCase(name)
                || "Keep-Alive".equalsIgnoreCase(name)
                || "Transfer-Encoding".equalsIgnoreCase(name)
                || "TE".equalsIgnoreCase(name)
                || "Trailer".equalsIgnoreCase(name)
                || "Upgrade".equalsIgnoreCase(name);
    }

    private static URI parseUri(String url) {
        if (url == null) {
            return null;
        }
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static Map<String, String> rawQuery(URI uri) {
        Map<String, String> result = new LinkedHashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return result;
        }

        for (String item : rawQuery.split("&", -1)) {
            int equals = item.indexOf('=');
            String name = equals < 0 ? item : item.substring(0, equals);
            String value = equals < 0 ? "" : item.substring(equals + 1);
            if (!result.containsKey(name)) {
                result.put(name, value);
            }
        }
        return result;
    }
}

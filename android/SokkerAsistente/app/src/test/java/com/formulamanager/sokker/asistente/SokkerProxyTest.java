package com.formulamanager.sokker.asistente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class SokkerProxyTest {
    @Test
    public void exactSokkerHttpsHostIsProxied() {
        assertTrue(SokkerProxy.isSokkerUrl("https://sokker.org/api/current"));
        assertTrue(SokkerProxy.isSokkerUrl("https://SOKKER.ORG/start.php?session=xml"));
    }

    @Test
    public void lookalikeOrNonHttpsHostIsNotProxied() {
        assertFalse(SokkerProxy.isSokkerUrl("https://sokker.org.evil.example/api/current"));
        assertFalse(SokkerProxy.isSokkerUrl("http://sokker.org/api/current"));
        assertFalse(SokkerProxy.isSokkerUrl("https://raqueto.com/sokker/asistente"));
        assertFalse(SokkerProxy.isSokkerUrl("not a url"));
    }

    @Test
    public void legacyLoginRequiresExactPostEndpointAndBothCredentials() {
        String login = "https://sokker.org/start.php?session=xml&ilogin=terrion&ipassword=a%2Bb%26c";
        assertTrue(SokkerProxy.isLegacyXmlSessionLogin("POST", login));

        assertFalse(SokkerProxy.isLegacyXmlSessionLogin("GET", login));
        assertFalse(SokkerProxy.isLegacyXmlSessionLogin("POST",
                "https://sokker.org/start.php?session=xml&ilogin=terrion"));
        assertFalse(SokkerProxy.isLegacyXmlSessionLogin("POST",
                "https://sokker.org/other.php?session=xml&ilogin=terrion&ipassword=x"));
        assertFalse(SokkerProxy.isLegacyXmlSessionLogin("POST",
                "https://sokker.org.evil.example/start.php?session=xml&ilogin=terrion&ipassword=x"));
    }

    @Test
    public void legacyBodyPreservesRawEncodedCredentialValues() {
        String url = "https://sokker.org/start.php?session=xml"
                + "&ilogin=t%C3%A9st%2Buser"
                + "&ipassword=p%25ss%2Bword%26x%20y";

        assertEquals("ilogin=t%C3%A9st%2Buser&ipassword=p%25ss%2Bword%26x%20y",
                SokkerProxy.buildLegacyLoginBody(url));
    }

    @Test
    public void legacyBodyIsNotSynthesizedWhenCredentialsAreIncomplete() {
        assertNull(SokkerProxy.buildLegacyLoginBody(
                "https://sokker.org/start.php?session=xml&ilogin=terrion"));
        assertNull(SokkerProxy.buildLegacyLoginBody(
                "https://sokker.org/api/current?ilogin=terrion&ipassword=x"));
    }

    @Test
    public void existingClientHeaderIsPreservedCaseInsensitivelyWithoutDuplication() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        headers.put("x-sokker-client", "existing-client");

        Map<String, String> result = SokkerProxy.buildForwardHeaders(headers);

        assertEquals("application/json", result.get("Accept"));
        assertEquals(1, countHeaderIgnoreCase(result, SokkerProxy.SOKKER_CLIENT_HEADER));
        assertEquals("existing-client", valueIgnoreCase(result, SokkerProxy.SOKKER_CLIENT_HEADER));
    }

    @Test
    public void missingClientHeaderIsInjected() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");

        Map<String, String> result = SokkerProxy.buildForwardHeaders(headers);

        assertEquals(1, countHeaderIgnoreCase(result, SokkerProxy.SOKKER_CLIENT_HEADER));
        assertEquals(SokkerProxy.SOKKER_CLIENT_KEY,
                valueIgnoreCase(result, SokkerProxy.SOKKER_CLIENT_HEADER));
    }

    @Test
    public void connectionManagedAndContentEncodingHeadersAreNotForwarded() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Host", "sokker.org");
        headers.put("Content-Length", "123");
        headers.put("Connection", "keep-alive");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept", "application/json");

        Map<String, String> result = SokkerProxy.buildForwardHeaders(headers);

        assertFalse(containsHeaderIgnoreCase(result, "Host"));
        assertFalse(containsHeaderIgnoreCase(result, "Content-Length"));
        assertFalse(containsHeaderIgnoreCase(result, "Connection"));
        assertFalse(containsHeaderIgnoreCase(result, "Accept-Encoding"));
        assertEquals("application/json", result.get("Accept"));
    }

    @Test
    public void httpErrorsUseErrorStreamInsteadOfThrowingInputStream() throws Exception {
        FakeHttpURLConnection connection = new FakeHttpURLConnection(401, "bad credentials");

        InputStream stream = SokkerProxy.responseStream(connection);

        assertEquals("bad credentials", readUtf8(stream));
        assertFalse(connection.inputStreamRequested);
    }

    @Test
    public void successfulResponsesUseInputStream() throws Exception {
        FakeHttpURLConnection connection = new FakeHttpURLConnection(200, "ok");

        InputStream stream = SokkerProxy.responseStream(connection);

        assertEquals("ok", readUtf8(stream));
        assertTrue(connection.inputStreamRequested);
    }

    private static int countHeaderIgnoreCase(Map<String, String> headers, String name) {
        int count = 0;
        for (String key : headers.keySet()) {
            if (key != null && key.equalsIgnoreCase(name)) {
                count++;
            }
        }
        return count;
    }

    private static boolean containsHeaderIgnoreCase(Map<String, String> headers, String name) {
        return countHeaderIgnoreCase(headers, name) > 0;
    }

    private static String valueIgnoreCase(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String readUtf8(InputStream stream) throws IOException {
        byte[] buffer = new byte[256];
        int read = stream.read(buffer);
        return read < 0 ? "" : new String(buffer, 0, read, StandardCharsets.UTF_8);
    }

    private static class FakeHttpURLConnection extends HttpURLConnection {
        private final byte[] payload;
        private boolean inputStreamRequested;

        FakeHttpURLConnection(int status, String body) throws Exception {
            super(new URL("https://sokker.org/test"));
            this.responseCode = status;
            this.responseMessage = status >= 400 ? "error" : "ok";
            this.payload = body.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            inputStreamRequested = true;
            if (responseCode >= 400) {
                throw new IOException("HTTP " + responseCode);
            }
            return new ByteArrayInputStream(payload);
        }

        @Override
        public InputStream getErrorStream() {
            return responseCode >= 400 ? new ByteArrayInputStream(payload) : null;
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
        }
    }
}

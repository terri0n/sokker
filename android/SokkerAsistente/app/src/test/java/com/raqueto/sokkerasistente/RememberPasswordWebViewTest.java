package com.raqueto.sokkerasistente;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class RememberPasswordWebViewTest {
    @Test
    public void webViewEnablesDomStorageForRememberPassword() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(
                "src/main/java/com/raqueto/sokkerasistente/MainActivity.java")), StandardCharsets.UTF_8);

        assertTrue("WebView must enable DOM storage so util.js localStorage survives app restarts",
                source.contains("myWebView.getSettings().setDomStorageEnabled(true);"));
    }
}

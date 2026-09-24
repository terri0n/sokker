import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class I18nParityHarness {
    private static final File BUNDLE_DIR = new File("sokker/src/com/formulamanager/sokker/idiomas");
    private static final String[] LANGUAGES = {"en", "es", "fr", "it", "sk"};

    public static void main(String[] args) throws Exception {
        Set<String> referenceKeys = loadKeys("en");

        for (String language : LANGUAGES) {
            Set<String> keys = loadKeys(language);
            if (!keys.equals(referenceKeys)) {
                Set<String> missing = new LinkedHashSet<String>(referenceKeys);
                missing.removeAll(keys);
                Set<String> extra = new LinkedHashSet<String>(keys);
                extra.removeAll(referenceKeys);
                throw new AssertionError("Bundle " + language + " differs from EN. Missing=" + missing + ", extra=" + extra);
            }
        }

        File ntdbMenu = new File("sokker/WebContent/jsp/asistente/ntdb_menu.jsp");
        String jsp = new String(Files.readAllBytes(ntdbMenu.toPath()), StandardCharsets.UTF_8);
        assertContains(jsp, "items=\"<%= Idioma.IDIOMAS %>\"");
        for (String hardcoded : Arrays.asList(
                "Available National Teams",
                ">Links<",
                "U21s schedule",
                "NTs schedule")) {
            if (jsp.contains(hardcoded)) {
                throw new AssertionError("ntdb_menu.jsp still contains hardcoded UI text: " + hardcoded);
            }
        }
        if (jsp.contains("<fmt:message key=\"common.append\" /> <fmt:message key=\"common.players\" />")) {
            throw new AssertionError("ntdb_menu.jsp still exposes the Append Players button");
        }

        System.out.println("I18n parity OK for " + Arrays.toString(LANGUAGES));
    }

    private static Set<String> loadKeys(String language) throws IOException {
        File file = new File(BUNDLE_DIR, "ApplicationResources_" + language + ".properties");
        if (!file.isFile()) {
            throw new AssertionError("Missing language bundle: " + file.getPath());
        }
        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(file);
        try {
            properties.load(input);
        } finally {
            input.close();
        }
        return new LinkedHashSet<String>(properties.stringPropertyNames());
    }

    private static void assertContains(String value, String expected) {
        if (!value.contains(expected)) {
            throw new AssertionError("Expected ntdb_menu.jsp to contain: " + expected);
        }
    }
}

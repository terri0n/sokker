import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

public final class PrivacyPagesHarness {
    private static final List<String> BUNDLES = Arrays.asList("en", "es", "fr", "it", "sk");
    private static final List<String> REQUIRED_KEYS = Arrays.asList(
            "account.delete.request",
            "account.delete.pending",
            "account.delete.admin.title",
            "account.delete.admin.confirm",
            "account.delete.completed",
            "privacy.title",
            "privacy.account_data",
            "privacy.sokker_data",
            "privacy.logs",
            "privacy.google_services",
            "privacy.retention",
            "privacy.deletion",
            "privacy.contact");

    private PrivacyPagesHarness() {}

    public static void main(String[] args) throws Exception {
        publicRoutesExist();
        publicPagesDoNotLoadAdsOrAnalytics();
        assistantLinksBothPublicPages();
        allLanguageBundlesContainRequiredKeys();
    }

    private static void publicRoutesExist() throws Exception {
        String privacy = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Privacidad.java");
        String deletion = read("sokker/src/com/formulamanager/sokker/acciones/asistente/EliminarCuenta.java");
        require(privacy.contains("@WebServlet(\"/asistente/privacidad\")"), "Missing public privacy route");
        require(deletion.contains("@WebServlet(\"/asistente/eliminar_cuenta\")"), "Missing public deletion route");
    }

    private static void publicPagesDoNotLoadAdsOrAnalytics() throws Exception {
        for (String path : Arrays.asList(
                "sokker/WebContent/jsp/asistente/privacidad.jsp",
                "sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp")) {
            String page = read(path);
            forbid(page, "googletagmanager", path + " loads Google Tag Manager");
            forbid(page, "adsbygoogle", path + " loads AdSense");
            forbid(page, "google_ad_client", path + " loads Google advertising");
        }
    }

    private static void assistantLinksBothPublicPages() throws Exception {
        String assistant = read("sokker/WebContent/jsp/asistente/asistente.jsp");
        require(assistant.contains("/asistente/privacidad"), "Assistant has no privacy link");
        require(assistant.contains("/asistente/eliminar_cuenta"), "Assistant has no public account-deletion link");
    }

    private static void allLanguageBundlesContainRequiredKeys() throws Exception {
        for (String language : BUNDLES) {
            String path = "sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_" + language + ".properties";
            Properties properties = new Properties();
            try (FileInputStream input = new FileInputStream(path)) {
                properties.load(input);
            }
            for (String key : REQUIRED_KEYS) {
                String value = properties.getProperty(key);
                require(value != null && !value.trim().isEmpty(), "Missing key " + key + " in " + language);
            }
        }
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void forbid(String text, String needle, String message) {
        require(!text.contains(needle), message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

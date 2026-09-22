import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SokkerClientIdentificationHarness {
    private static final String CLIENT_KEY = "skc_bae02f2686bf9038d248";

    private SokkerClientIdentificationHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path helper = Paths.get("sokker/WebContent/js/sokker-client.js");
        if (!Files.isRegularFile(helper)) {
            throw new AssertionError("Missing shared browser Sokker client helper: " + helper);
        }

        String helperText = read(helper);
        require(helperText, "X-Sokker-Client", "browser helper must set X-Sokker-Client");
        require(helperText, CLIENT_KEY, "browser helper must use the assigned Sokker client key");

        String login = read(Paths.get("sokker/WebContent/jsp/asistente/login.jsp"));
        require(login, "/js/sokker-client.js", "assistant login page must load the shared Sokker client helper");
        require(login, "sokkerPost(", "assistant registration/login precheck must use identified Sokker requests");

        String assistant = read(Paths.get("sokker/WebContent/jsp/asistente/asistente.jsp"));
        require(assistant, "/js/sokker-client.js", "assistant main page must load the shared Sokker client helper");
        require(assistant, "sokkerPost(", "assistant update/password prechecks must use identified Sokker requests");

        System.out.println("Sokker client identification harness OK");
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static void require(String text, String expected, String message) {
        if (!text.contains(expected)) {
            throw new AssertionError(message + ": missing " + expected);
        }
    }
}

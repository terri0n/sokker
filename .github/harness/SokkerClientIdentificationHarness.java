import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SokkerClientIdentificationHarness {
    private static final String CLIENT_KEY = "skc_bae02f2686bf9038d248";

    private SokkerClientIdentificationHarness() {
    }

    public static void main(String[] args) throws Exception {
        String sharedJs = read(Paths.get("sokker/WebContent/js/desplegable.js"));
        require(sharedJs, "X-Sokker-Client", "shared assistant JavaScript must set X-Sokker-Client");
        require(sharedJs, CLIENT_KEY, "shared assistant JavaScript must use the assigned Sokker client key");
        require(sharedJs, "$.post = function", "shared assistant JavaScript must identify direct Sokker POSTs");
        require(sharedJs, "originalPost", "shared assistant JavaScript must preserve the original POST implementation");
        require(sharedJs, "xhr.status === 0", "browser identification must preserve the legacy fallback while Sokker preflight is unavailable");

        String login = read(Paths.get("sokker/WebContent/jsp/asistente/login.jsp"));
        require(login, "/js/desplegable.js", "assistant login page must load the shared identified-request JavaScript");
        require(login, "https://sokker.org/start.php?session=xml", "assistant login precheck must still run directly from the browser");

        String assistant = read(Paths.get("sokker/WebContent/jsp/asistente/asistente.jsp"));
        require(assistant, "/js/desplegable.js", "assistant main page must load the shared identified-request JavaScript");
        require(assistant, "https://sokker.org/start.php?session=xml", "assistant update/password prechecks must still run directly from the browser");

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

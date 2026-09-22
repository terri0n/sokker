import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class SokkerClientIdentificationHarness {
    private static final String CLIENT_KEY = "skc_bae02f2686bf9038d248";

    private SokkerClientIdentificationHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path util = Paths.get("sokker/WebContent/js/util.js");
        if (!Files.isRegularFile(util)) {
            throw new AssertionError("Missing shared browser utility JavaScript: " + util);
        }

        String sharedJs = read(util);
        require(sharedJs, "function sokkerPost", "shared utility JavaScript must expose sokkerPost");
        require(sharedJs, "X-Sokker-Client", "sokkerPost must set X-Sokker-Client");
        require(sharedJs, CLIENT_KEY, "sokkerPost must use the assigned Sokker client key");
        require(sharedJs, "xhr.status === 0", "browser identification must preserve the legacy fallback while Sokker preflight is unavailable");
        forbid(sharedJs, "$.post =", "shared utility JavaScript must not monkey-patch $.post");

        requireUtilAndSokkerPost("sokker/WebContent/jsp/asistente/login.jsp", "assistant login");
        requireUtilAndSokkerPost("sokker/WebContent/jsp/asistente/asistente.jsp", "assistant main page");
        requireUtilOnly("sokker/WebContent/jsp/asistente/seleccion.jsp", "assistant selection page");
        requireUtilOnly("sokker/WebContent/jsp/asistente/ntdb_menu.jsp", "assistant NTDB menu");
        requireUtilAndSokkerPost("sokker/WebContent/jsp/sete/sete.jsp", "SETE");
        requireUtilOnly("sokker/WebContent/jsp/sete/oldsete.jsp", "legacy SETE");

        String seteJs = read(Paths.get("sokker/WebContent/js/sete.js.jsp"));
        require(seteJs, "sokkerPost(", "legacy SETE browser login must use sokkerPost");
        forbid(seteJs, "$.post('https://sokker.org", "legacy SETE must not call $.post directly for Sokker");

        if (Files.exists(Paths.get("sokker/WebContent/test.jsp"))) {
            throw new AssertionError("obsolete test.jsp must be removed");
        }
        if (Files.exists(Paths.get("sokker/WebContent/js/desplegable.js"))) {
            throw new AssertionError("desplegable.js must be renamed to util.js");
        }

        verifyNoLegacyBrowserCalls(Paths.get("sokker/WebContent"));

        System.out.println("Sokker client identification harness OK");
    }

    private static void requireUtilAndSokkerPost(String path, String label) throws Exception {
        String text = read(Paths.get(path));
        require(text, "/js/util.js", label + " must load util.js");
        require(text, "sokkerPost(", label + " must use sokkerPost for direct Sokker POSTs");
        forbid(text, "$.post('https://sokker.org", label + " must not call $.post directly for Sokker");
    }

    private static void requireUtilOnly(String path, String label) throws Exception {
        String text = read(Paths.get(path));
        require(text, "/js/util.js", label + " must load util.js");
        forbid(text, "/js/desplegable.js", label + " must not load desplegable.js");
    }

    private static void verifyNoLegacyBrowserCalls(Path root) throws Exception {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(SokkerClientIdentificationHarness::isBrowserSource)
                    .forEach(path -> {
                        try {
                            String text = read(path);
                            forbid(text, "$.post('https://sokker.org", path + " must use sokkerPost for Sokker POSTs");
                            forbid(text, "$.post(\"https://sokker.org", path + " must use sokkerPost for Sokker POSTs");
                            forbid(text, "/js/desplegable.js", path + " must not reference the renamed JavaScript");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception)e.getCause();
            }
            throw e;
        }
    }

    private static boolean isBrowserSource(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jsp") || name.endsWith(".js") || name.endsWith(".tag");
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static void require(String text, String expected, String message) {
        if (!text.contains(expected)) {
            throw new AssertionError(message + ": missing " + expected);
        }
    }

    private static void forbid(String text, String forbidden, String message) {
        if (text.contains(forbidden)) {
            throw new AssertionError(message + ": found " + forbidden);
        }
    }
}

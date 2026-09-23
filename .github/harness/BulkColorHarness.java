import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BulkColorHarness {
    private BulkColorHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path jsp = Paths.get("sokker/WebContent/jsp/asistente/asistente.jsp");
        String ui = read(jsp);
        require(ui, "function cambiar_color", "assistant UI must expose bulk color action");
        require(ui, "/asistente/cambiar_color?color=", "bulk color UI must call the color endpoint");
        require(ui, "&pids=", "bulk color UI must send selected player IDs");

        Path servlet = Paths.get("sokker/src/com/formulamanager/sokker/acciones/asistente/Cambiar_color.java");
        if (!Files.isRegularFile(servlet)) {
            throw new AssertionError("Missing bulk color servlet: " + servlet);
        }

        String source = read(servlet);
        require(source, "@WebServlet(\"/asistente/cambiar_color\")", "bulk color servlet must keep the UI route");
        require(source, "request.getParameter(\"pids\")", "bulk color servlet must read selected player IDs");
        require(source, "request.getParameter(\"color\")", "bulk color servlet must read the chosen color");
        require(source, "AsistenteBO.leer_jugadores", "bulk color must use the established player loading path");
        require(source, "j.setColor(color)", "bulk color must change only the selected player's color field");
        require(source, "AsistenteBO.grabar_jugadores", "bulk color must use the established lossless persistence path");
        require(source, "boolean historico", "bulk color must preserve the current/history database selection");

        System.out.println("Bulk color harness OK");
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

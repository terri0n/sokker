import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ColorAssignmentHarness {
    public static void main(String[] args) throws Exception {
        Path servletPath = Paths.get("sokker/src/com/formulamanager/sokker/acciones/asistente/Cambiar_color.java");
        require(Files.exists(servletPath),
                "Color assignment endpoint source is missing: /asistente/cambiar_color would return 404 on a clean build");

        String servlet = new String(Files.readAllBytes(servletPath), StandardCharsets.UTF_8);
        String jsp = read("sokker/WebContent/jsp/asistente/asistente.jsp");

        require(servlet.contains("@WebServlet(\"/asistente/cambiar_color\")"),
                "Color assignment servlet must expose /asistente/cambiar_color");
        require(servlet.contains("j.setColor(color)"),
                "Color assignment servlet must update the selected players' color");
        require(servlet.contains("AsistenteBO.leer_jugadores"),
                "Color assignment servlet must load players through AsistenteBO");
        require(servlet.contains("AsistenteBO.grabar_jugadores"),
                "Color assignment servlet must persist the updated players");
        require(servlet.contains("new Navegador(true, request)"),
                "Color assignment servlet must use the current Sokker navigation compatibility path");
        require(jsp.contains("/asistente/cambiar_color?color="),
                "Assistant UI must keep pointing to the color assignment endpoint");

        System.out.println("Color assignment harness OK");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

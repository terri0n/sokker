import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class NtBotIsolationHarness {
    private NtBotIsolationHarness() {}

    public static void main(String[] args) throws Exception {
        backgroundRefreshIsBoundToCapturedSelectionId();
        updaterReReadsTheCapturedSelectionBeforeSaving();
    }

    private static void backgroundRefreshIsBoundToCapturedSelectionId() throws Exception {
        String source = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Actualizar.java");

        require(source.contains("final Integer tid_actualizacion = usuario.getDef_tid();"),
                "The selection id must be captured before the background work is scheduled");
        require(source.contains("NtBotUpdater.programar(tid_actualizacion, jornada_actual);"),
                "The bot refresh is not scheduled with the captured selection id");

        Class<?> updater = Class.forName("com.formulamanager.sokker.bo.NtBotUpdater");
        Method schedule = updater.getDeclaredMethod("programar", Integer.class, int.class);
        require(schedule != null, "Missing NT bot updater entry point");
    }

    private static void updaterReReadsTheCapturedSelectionBeforeSaving() throws Exception {
        Path updaterPath = Paths.get("sokker/src/com/formulamanager/sokker/bo/NtBotUpdater.java");
        require(Files.exists(updaterPath), "Missing isolated NT bot updater");
        String source = read(updaterPath.toString());

        require(!source.contains("getDef_tid()"),
                "Background bot refresh must not read mutable Usuario.def_tid");
        require(!source.contains(" Usuario ") && !source.contains("Usuario usuario"),
                "Background bot refresh must not depend on the mutable session Usuario");
        require(source.contains("/api/player/"),
                "Bot refresh must recover the player's real owner team from the current JSON API");
        require(source.contains("/api/team/") && source.contains("/owner"),
                "Bot refresh must recover bot ownership from the current JSON API");
        require(source.contains("AsistenteBO.leer_jugadores(tid, null, false, null)"),
                "Bot refresh must re-read the captured target selection before applying asynchronous results");
        require(source.contains("AsistenteBO.grabar_jugadores(jugadores_actuales, tid, jornada_actual, false)"),
                "Bot refresh must save only to the captured selection id");
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

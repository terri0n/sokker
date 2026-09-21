import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.formulamanager.sokker.dao.AsistenteDAO;
import com.gargoylesoftware.htmlunit.WebClient;

public final class NtBotIsolationHarness {
    private NtBotIsolationHarness() {}

    public static void main(String[] args) throws Exception {
        botHelpersAreAvailable();
        backgroundRefreshIsBoundToCapturedSelectionId();
    }

    private static void botHelpersAreAvailable() throws Exception {
        Method ownerTeam = AsistenteDAO.class.getDeclaredMethod("obtener_tid", WebClient.class, Integer.class);
        Method botState = AsistenteDAO.class.getDeclaredMethod("es_bot", WebClient.class, Integer.class);
        require(ownerTeam != null, "Missing player owner-team lookup used by NT bot refresh");
        require(botState != null, "Missing bot-team detection used by NT bot refresh");
    }

    private static void backgroundRefreshIsBoundToCapturedSelectionId() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(
                "sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java")), StandardCharsets.UTF_8);

        require(source.contains("programar_comprobacion_bots(tid, jugadores_actualizados, jornada_actual);"),
                "NT update does not schedule bot refresh with the already captured selection tid");

        int start = source.indexOf("private static void programar_comprobacion_bots(");
        require(start >= 0, "Missing isolated NT bot background task");
        int end = source.indexOf("\n\tpublic static List<Juvenil>", start);
        require(end > start, "Could not delimit NT bot background task");
        String block = source.substring(start, end);

        require(!block.contains("getDef_tid()"),
                "Background bot refresh reads mutable Usuario.def_tid and can write players into another selection");
        require(block.contains("comprobar_bots(tid,"),
                "Background task does not pass the captured selection tid to bot refresh");
        require(block.contains("leer_jugadores(tid, null, false, null)"),
                "Bot refresh must re-read the current target file before applying asynchronous results");
        require(block.contains("grabar_jugadores(jugadores_actuales, tid, jornada_actual, false)"),
                "Bot refresh does not save back to the captured selection tid");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

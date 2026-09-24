import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.formulamanager.sokker.bo.NtdbBO;

public class TeamIdThresholdHarness {
    public static void main(String[] args) throws Exception {
        if (NtdbBO.MAX_ID_SELECCION != 800) {
            throw new AssertionError("Expected MAX_ID_SELECCION=800 but was " + NtdbBO.MAX_ID_SELECCION);
        }

        int maxKnownNationalTeamId = NtdbBO.DIF_NT_U21 + NtdbBO.paises.length;
        if (!(maxKnownNationalTeamId < NtdbBO.MAX_ID_SELECCION)) {
            throw new AssertionError("National-team IDs overlap club threshold: " + maxKnownNationalTeamId);
        }

        assertNational(503);
        assertNational(506);
        assertClub(995);
        assertClub(998);
        assertDemoAccountDoesNotSwitchTeams();

        System.out.println("Team ID threshold compatibility OK");
    }

    private static void assertDemoAccountDoesNotSwitchTeams() throws Exception {
        String config = read("sokker/WebContent/jsp/asistente/config.jsp");
        String servlet = read("sokker/src/com/formulamanager/sokker/acciones/asistente/CambiarEquipo.java");

        if (!config.contains("sessionScope.usuario.login eq 'demo'")) {
            throw new AssertionError("Demo account must hide team-switch controls");
        }
        if (!config.contains("a[href$=\"/asistente/cambiar_equipo\"]")) {
            throw new AssertionError("Demo account must hide the direct team-switch link");
        }
        if (!servlet.contains("\"demo\".equalsIgnoreCase(usuario.getLogin())")) {
            throw new AssertionError("Demo account must be protected server-side from team switching");
        }
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void assertNational(int tid) {
        if (!(tid < NtdbBO.MAX_ID_SELECCION)) {
            throw new AssertionError("Expected national-team classification for tid=" + tid);
        }
    }

    private static void assertClub(int tid) {
        if (!(tid > NtdbBO.MAX_ID_SELECCION)) {
            throw new AssertionError("Expected club classification for tid=" + tid);
        }
    }
}

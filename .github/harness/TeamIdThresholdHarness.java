import com.formulamanager.sokker.bo.NtdbBO;

public class TeamIdThresholdHarness {
    public static void main(String[] args) {
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

        System.out.println("Team ID threshold compatibility OK");
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

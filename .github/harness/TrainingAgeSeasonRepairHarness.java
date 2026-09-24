import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TrainingAgeSeasonRepairHarness {
    private static final Path JSP = Paths.get(
            "sokker/WebContent/jsp/asistente/repararEdadesUltimosEntrenamientos.jsp");
    private static final Path HELPER = Paths.get(
            "sokker/src/com/formulamanager/sokker/auxiliares/TrainingAgeSeasonRepair.java");

    private TrainingAgeSeasonRepairHarness() {}

    public static void main(String[] args) throws Exception {
        String jsp = read(JSP);

        require(!Files.exists(HELPER),
                "the one-off correction must live in the JSP, not in permanent Java application code");
        require(jsp.contains("private RepairSummary repairDirectory(File directory)"),
                "the JSP must contain the repair implementation it executes manually");
        require(jsp.contains("private Integer expectedHistoricalAge(int referenceAge, int snapshotWeek)"),
                "the JSP must use week 1209 age as the fixed reference for the previous season");
        require(jsp.contains("private void selfTestRepair()"),
                "the JSP must self-test the destructive transformation before execution");

        require(jsp.contains("session.getAttribute(\"admin\") == null"),
                "the repair JSP must reject non-admin sessions");
        require(jsp.contains("HttpServletResponse.SC_FORBIDDEN"),
                "non-admin access must be forbidden rather than merely hidden");
        require(jsp.contains("\"POST\".equalsIgnoreCase(request.getMethod())"),
                "the repair must run only after an explicit manual POST");
        require(!jsp.contains("repairTrainingAges20260924Executed"),
                "the repair must not rely on a one-run application marker");
        require(!jsp.contains("alreadyExecuted"),
                "a second manual execution must be allowed and naturally be a no-op");

        require(jsp.contains("snapshot.week == 1209"),
                "week 1209 must be located explicitly and used as the age reference");
        require(jsp.contains("snapshotWeek < 1197 || snapshotWeek > 1208"),
                "only weeks 1 to 12 of the previous season may be rewritten");
        require(jsp.contains("return Integer.valueOf(referenceAge)"),
                "weeks 1197..1208 must be restored to exactly the age stored at week 1209");
        require(!jsp.contains("latestAge - 1"),
                "the repair must not derive previous-season age from week 1210");
        require(!jsp.contains("snapshotWeek > 1209"),
                "week 1209 itself must not be part of the rewrite range");
        require(jsp.contains("future_key=keep\\\\:exactly"),
                "the JSP self-test must protect unknown properties during round-trip");
        require(jsp.contains("requireRepair(second.modifiedSnapshots == 0"),
                "the destructive transformation must prove that a second pass makes no changes");
        require(jsp.contains("snapshot(1210, 25) + \",\" + snapshot(1209, 24)"),
                "week 1210 and week 1209 must both retain their already-correct ages");
        require(jsp.contains("snapshot(1209, 24) + \",\" + snapshot(1208, 24) + \",\" + snapshot(1197, 24)"),
                "weeks 1 to 12 must be normalized to the exact age stored at week 13");
        require(jsp.contains("missingReference"),
                "the self-test must verify that players without week 1209 are skipped");
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

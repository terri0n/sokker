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
        require(jsp.contains("private Integer expectedAge(int latestWeek, int latestAge, int snapshotWeek)"),
                "the JSP must derive historical age from the latest valid snapshot");
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

        require(jsp.contains("latest.week != 1210 && latest.week != 1209 && latest.week != 1208"),
                "only weeks 1210, 1209 or 1208 may be used as the valid age base");
        require(jsp.contains("snapshotWeek < 1195 || snapshotWeek > latestWeek"),
                "the repair must be restricted to week 1195 through the latest valid snapshot");
        require(jsp.contains("if (latestWeek >= 1209)"),
                "the age calculation must distinguish the current season from a 1208 reference");
        require(jsp.contains("if (snapshotWeek >= 1209)"),
                "weeks 1209 and 1210 must keep the current-season age");
        require(jsp.contains("if (snapshotWeek >= 1196)"),
                "weeks 1196 through 1208 must share the previous-season age");
        require(jsp.contains("return Integer.valueOf(latestAge - 2)"),
                "week 1195 must be two years below a 1209/1210 current-season reference");
        require(jsp.contains("return Integer.valueOf(latestAge - 1)"),
                "crossing one season boundary must subtract exactly one year");
        require(jsp.contains("future_key=keep\\\\:exactly"),
                "the JSP self-test must protect unknown properties during round-trip");
        require(jsp.contains("requireRepair(second.modifiedSnapshots == 0"),
                "the destructive transformation must prove that a second pass makes no changes");

        require(jsp.contains("snapshot(1210, 25) + \",\" + snapshot(1209, 25) + \",\" + snapshot(1208, 24)"),
                "a 1210 reference must keep 1209 at the same age and put 1208 in the previous season");
        require(jsp.contains("snapshot(1196, 24) + \",\" + snapshot(1195, 23)"),
                "a 1210/1209 reference must repair the complete previous season and the preceding week 13 boundary");
        require(jsp.contains("snapshot(1208, 24) + \",\" + snapshot(1196, 24) + \",\" + snapshot(1195, 23)"),
                "a 1208 reference must keep that season age and subtract one year at 1195");
        require(jsp.contains("unsupportedLatest"),
                "the self-test must verify that players whose latest week is outside 1208..1210 are skipped");
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

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
        require(jsp.contains("private Integer expectedHistoricalAge(int latestWeek, int latestAge, int snapshotWeek)"),
                "the JSP must contain the exact previous-season age rule");
        require(jsp.contains("private void selfTestRepair()"),
                "the JSP must self-test the destructive transformation before execution");

        require(jsp.contains("session.getAttribute(\"admin\") == null"),
                "the repair JSP must reject non-admin sessions");
        require(jsp.contains("HttpServletResponse.SC_FORBIDDEN"),
                "non-admin access must be forbidden rather than merely hidden");
        require(jsp.contains("\"POST\".equalsIgnoreCase(request.getMethod())"),
                "the repair must run only after an explicit manual POST");
        require(jsp.contains("repairTrainingAges20260924Executed"),
                "the JSP must carry a one-run marker");
        require(jsp.contains("application.setAttribute(\"repairTrainingAges20260924Executed\", Boolean.TRUE)"),
                "a successful run must mark the JSP as already executed");
        require(jsp.contains("if (alreadyExecuted)"),
                "a second manual execution must be blocked");
        require(!jsp.contains("puede volver a ejecutarse"),
                "the UI must not invite running this one-off correction repeatedly");

        require(jsp.contains("latestWeek != 1209 && latestWeek != 1210"),
                "only week 1210 or, for not-yet-updated teams, week 1209 may provide current age");
        require(jsp.contains("snapshotWeek < 1197 || snapshotWeek > 1209"),
                "the correction must be limited to the immediately previous 13-week season");
        require(jsp.contains("return Integer.valueOf(latestAge - 1)"),
                "previous-season snapshots must be normalized to current age minus one");
        require(jsp.contains("future_key=keep\\\\:exactly"),
                "the JSP self-test must protect unknown properties during round-trip");
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

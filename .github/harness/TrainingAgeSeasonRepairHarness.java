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
        require(jsp.contains("private int seasonIndex(int week)"),
                "the JSP must calculate historical seasons instead of hardcoding each boundary");
        require(jsp.contains("Math.floorDiv(week - 1209, 13)"),
                "season boundaries must follow 13-week cycles anchored at week 1209");
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
        require(jsp.contains("snapshotWeek < 1170 || snapshotWeek > latestWeek"),
                "the repair must be restricted to week 1170 through the latest valid snapshot");
        require(jsp.contains("latestAge + seasonIndex(snapshotWeek) - seasonIndex(latestWeek)"),
                "historical ages must be derived from the number of crossed season boundaries");
        require(jsp.contains("future_key=keep\\\\:exactly"),
                "the JSP self-test must protect unknown properties during round-trip");
        require(jsp.contains("requireRepair(second.modifiedSnapshots == 0"),
                "the destructive transformation must prove that a second pass makes no changes");

        require(jsp.contains("snapshot(1210, 25) + \",\" + snapshot(1209, 25) + \",\" + snapshot(1208, 24)"),
                "a 1210 reference must keep 1209 at the same age and put 1208 in the previous season");
        require(jsp.contains("snapshot(1195, 23) + \",\" + snapshot(1183, 23)")
                        && jsp.contains("snapshot(1182, 22) + \",\" + snapshot(1170, 22)"),
                "the full season 1183..1195 must have one age and cross correctly into 1182");
        require(jsp.contains("snapshot(1170, 22) + \",\" + snapshot(1169, 19)"),
                "week 1170 must be repaired while 1169 and older stay untouched");
        require(jsp.contains("snapshot(1208, 24) + \",\" + snapshot(1196, 24) + \",\" + snapshot(1195, 23)"),
                "when 1208 is the reference, its whole 1196..1208 season must keep the base age");
        require(jsp.contains("snapshot(1183, 23) + \",\" + snapshot(1182, 22) + \",\" + snapshot(1170, 22)"),
                "a 1208 reference must step down only when crossing into older seasons");
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

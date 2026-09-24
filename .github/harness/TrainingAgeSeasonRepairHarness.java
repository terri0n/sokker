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
                "training snapshot season boundaries must follow the confirmed 13-week cycle anchored at week 1209");
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
        require(jsp.contains("snapshotWeek < 976 || snapshotWeek > latestWeek"),
                "the repair must cover every stored training week from 976 onward and never touch older data");
        require(!jsp.contains("snapshotWeek < 1170"),
                "the old artificial lower limit at week 1170 must be removed");
        require(jsp.contains("latestAge + seasonIndex(snapshotWeek) - seasonIndex(latestWeek)"),
                "historical ages must be derived from the number of crossed season boundaries");
        require(jsp.contains("week >= NEW_TRAINING_FORMAT_WEEK_REPAIR ? base + \",true\" : base"),
                "the JSP self-test must generate both pre-993 and post-993 snapshot formats correctly");
        require(jsp.contains("future_key=keep\\\\:exactly"),
                "the JSP self-test must protect unknown properties during round-trip");
        require(jsp.contains("requireRepair(second.modifiedSnapshots == 0"),
                "the destructive transformation must prove that a second pass makes no changes");

        require(jsp.contains("snapshot(1210, 25) + \",\" + snapshot(1209, 25) + \",\" + snapshot(1208, 24)"),
                "a 1210 reference must keep 1209 at the same age and put 1208 in the previous season");
        require(jsp.contains("snapshot(993, 8) + \",\" + snapshot(992, 8)"),
                "the self-test must cross the historical training-format boundary at week 993");
        require(jsp.contains("snapshot(988, 8) + \",\" + snapshot(987, 7) + \",\" + snapshot(976, 7)"),
                "the repair must continue through all 13-week training seasons down to week 976");
        require(jsp.contains("snapshot(976, 7) + \",\" + snapshot(975, 19)"),
                "week 976 must be repaired while week 975 and older stay untouched");
        require(jsp.contains("snapshot(1208, 24) + \",\" + snapshot(1196, 24) + \",\" + snapshot(1195, 23)"),
                "when 1208 is the reference, its whole 1196..1208 season must keep the base age");
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

import com.formulamanager.sokker.auxiliares.TrainingAgeSeasonRepair;

public final class TrainingAgeSeasonRepairHarness {
    private TrainingAgeSeasonRepairHarness() {}

    public static void main(String[] args) throws Exception {
        updatedTeamUsesCurrentWeekAge();
        notYetUpdatedTeamUsesPreviousWeekAgeWithoutRewritingReferenceRow();
        touchesOnlyPreviousSeason();
        refusesOtherReferenceWeeks();
        repairsExactlyPreviousSeasonAges();
        keepsReferenceRowForUnupdatedTeam();
        isIdempotent();
    }

    private static void updatedTeamUsesCurrentWeekAge() {
        require(age(1210, 25, 1209) == 24,
                "week 13 of the previous season must be current age - 1");
        require(age(1210, 25, 1197) == 24,
                "week 1 of the previous season must be current age - 1");
    }

    private static void notYetUpdatedTeamUsesPreviousWeekAgeWithoutRewritingReferenceRow() {
        require(age(1209, 25, 1208) == 24,
                "an unupdated team must use week 1209 as the current-age reference");
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1209, 25, 1209) == null,
                "the reference snapshot itself must never be rewritten");
        require(age(1209, 25, 1197) == 24,
                "older snapshots of the same previous season must still be repaired");
    }

    private static void touchesOnlyPreviousSeason() {
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1210, 25, 1210) == null,
                "current-season snapshots must not be touched");
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1210, 25, 1196) == null,
                "seasons older than the immediately previous one must not be touched");
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1210, 25, 1197) == 24,
                "the first week of the immediately previous season is included");
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1210, 25, 1209) == 24,
                "the thirteenth week of the immediately previous season is included");
    }

    private static void refusesOtherReferenceWeeks() {
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1208, 25, 1207) == null,
                "the one-off repair must not run before the season boundary");
        require(TrainingAgeSeasonRepair.expectedHistoricalAge(1211, 25, 1209) == null,
                "the one-off repair must not silently broaden to later weeks");
    }

    private static void repairsExactlyPreviousSeasonAges() throws Exception {
        String player = player(77,
                snapshot(1210, 25) + ","
                + snapshot(1209, 23) + ","
                + snapshot(1208, 25) + ","
                + snapshot(1197, 24) + ","
                + snapshot(1196, 23));
        String input = "future_key=keep\\:exactly\n" + player + "\n";

        String fixed = TrainingAgeSeasonRepair.repairContentForTest(input);
        require(fixed.contains(snapshot(1210, 25)), "current week age must stay unchanged");
        require(fixed.contains(snapshot(1209, 24)), "double-decremented week 13 must be restored to current age - 1");
        require(fixed.contains(snapshot(1208, 24)), "wrong week 12 must be normalized to current age - 1");
        require(fixed.contains(snapshot(1197, 24)), "already correct week 1 must stay unchanged");
        require(fixed.contains(snapshot(1196, 23)), "older seasons must stay unchanged");
        require(fixed.contains("future_key=keep\\:exactly"), "unknown properties must be preserved");
    }

    private static void keepsReferenceRowForUnupdatedTeam() throws Exception {
        String input = player(88,
                snapshot(1209, 25) + ","
                + snapshot(1208, 23) + ","
                + snapshot(1197, 25)) + "\n";

        String fixed = TrainingAgeSeasonRepair.repairContentForTest(input);
        require(fixed.contains(snapshot(1209, 25)), "week 1209 reference row must stay at current age until the team updates");
        require(fixed.contains(snapshot(1208, 24)), "week 1208 must use current age - 1");
        require(fixed.contains(snapshot(1197, 24)), "week 1197 must use current age - 1");
    }

    private static void isIdempotent() throws Exception {
        String input = player(99,
                snapshot(1210, 32) + ","
                + snapshot(1209, 30) + ","
                + snapshot(1197, 32)) + "\n";
        String once = TrainingAgeSeasonRepair.repairContentForTest(input);
        String twice = TrainingAgeSeasonRepair.repairContentForTest(once);
        require(once.equals(twice), "running the repair twice must not change data again");
    }

    private static int age(int latestWeek, int latestAge, int snapshotWeek) {
        Integer result = TrainingAgeSeasonRepair.expectedHistoricalAge(latestWeek, latestAge, snapshotWeek);
        if (result == null) {
            throw new AssertionError("Expected a repair age for " + latestWeek + "/" + snapshotWeek);
        }
        return result.intValue();
    }

    private static String snapshot(int week, int age) {
        return week + "," + age + ",100000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true";
    }

    private static String player(int pid, String snapshots) {
        return pid + "=Jugador " + pid
                + ",1000,DEF,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,"
                + snapshots + ",*";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

import com.formulamanager.sokker.auxiliares.TrainingAgeSeasonRepair;

public final class TrainingAgeSeasonRepairHarness {
    private TrainingAgeSeasonRepairHarness() {}

    public static void main(String[] args) {
        updatedTeamUsesCurrentWeekAge();
        notYetUpdatedTeamUsesPreviousWeekAgeWithoutRewritingReferenceRow();
        touchesOnlyPreviousSeason();
        refusesOtherReferenceWeeks();
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

    private static int age(int latestWeek, int latestAge, int snapshotWeek) {
        Integer result = TrainingAgeSeasonRepair.expectedHistoricalAge(latestWeek, latestAge, snapshotWeek);
        if (result == null) {
            throw new AssertionError("Expected a repair age for " + latestWeek + "/" + snapshotWeek);
        }
        return result.intValue();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

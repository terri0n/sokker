import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class PlayerShortSkillHeadingsHarness {
    private static final String[] SHORT_KEYS = {
        "skills.stamina_short", "skills.pace_short", "skills.technique_short",
        "skills.passing_short", "skills.keeper_short", "skills.defender_short",
        "skills.playmaker_short", "skills.striker_short",
        "skills.tactical_discipline_short", "skills.team_work_short"
    };

    private PlayerShortSkillHeadingsHarness() {}

    public static void main(String[] args) throws Exception {
        String text = new String(Files.readAllBytes(Paths.get(
                "sokker/WebContent/WEB-INF/tags/jugadores.tag")), StandardCharsets.UTF_8);

        for (String key : SHORT_KEYS) {
            require(text.contains("key=\"" + key + "\""), "Missing short heading: " + key);
        }

        require(!text.contains("key=\"skills.stamina\" /></tags:string>"),
                "Stamina heading still truncates the long translation");
        require(!text.contains("key=\"skills.striker\" /></tags:string>"),
                "Striker heading still truncates the long translation");
        require(!text.contains("key=\"skills.tactical_discipline\" /></tags:string>"),
                "Tactical discipline heading still truncates the long translation");
        require(!text.contains("key=\"skills.team_work\" /></tags:string>"),
                "Team-work heading still truncates the long translation");

        System.out.println("Player short skill headings harness OK");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

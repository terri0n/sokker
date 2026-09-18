import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.formulamanager.sokker.auxiliares.SokkerTrainerMapping;

public final class TrainerMappingHarness {
    private static final String[] SKILLS = {
        "stamina", "pace", "technique", "passing",
        "keeper", "defending", "playmaking", "striker"
    };

    private TrainerMappingHarness() {}

    public static void main(String[] args) {
        String first = SokkerTrainerMapping.buildXml(response("first", 50));
        require(first != null && first.contains("<job>1</job>") && first.contains("<skillCoach>8</skillCoach>"),
                "Known first-coach mapping failed");

        String assistant = SokkerTrainerMapping.buildXml(response("assistant", 50));
        require(assistant != null && assistant.contains("<job>2</job>"),
                "Known assistant mapping failed");

        require(SokkerTrainerMapping.buildXml(response("junior", 50)) == null,
                "Undemonstrated junior assignment must force XML fallback");
        require(SokkerTrainerMapping.buildXml(response("unassigned", 50)) == null,
                "Undemonstrated unassigned role must force XML fallback");
        require(SokkerTrainerMapping.buildXml(response("first", 60)) == null,
                "Incoherent averagePercent must force XML fallback");
    }

    private static Map<String, Object> response(String assignment, int averagePercent) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        List<Object> trainers = new ArrayList<Object>();
        trainers.add(trainer(assignment, averagePercent));
        response.put("trainers", trainers);
        return response;
    }

    private static Map<String, Object> trainer(String assignment, int averagePercent) {
        Map<String, Object> trainer = new LinkedHashMap<String, Object>();
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        Map<String, Object> assignmentMap = new LinkedHashMap<String, Object>();
        Map<String, Object> skills = new LinkedHashMap<String, Object>();

        assignmentMap.put("name", assignment);
        info.put("assignment", assignmentMap);

        for (String skill : SKILLS) {
            Map<String, Object> value = new LinkedHashMap<String, Object>();
            value.put("value", Integer.valueOf(8));
            value.put("percent", Integer.valueOf(50));
            skills.put(skill, value);
        }
        skills.put("averagePercent", Integer.valueOf(averagePercent));
        info.put("skills", skills);
        trainer.put("info", info);
        return trainer;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

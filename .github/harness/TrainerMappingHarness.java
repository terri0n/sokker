import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;

import com.formulamanager.sokker.auxiliares.SokkerTrainerMapping;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

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

        verifyCoachWarningThreshold();
    }

    private static void verifyCoachWarningThreshold() {
        String previousResources = Util.ApplicationResources;
        try {
            Util.ApplicationResources = "TrainerMappingHarness$Messages";
            require(hasCoachWarning(15), "A magical coach skill must show the below-16 warning");
            require(!hasCoachWarning(16), "An unearthly coach skill must not show the below-16 warning");
            require(!hasCoachWarning(17), "A divine coach skill must not show the below-16 warning");
        } finally {
            Util.ApplicationResources = previousResources;
        }
    }

    private static boolean hasCoachWarning(int paceLevel) {
        final int jornada = 1210;
        Usuario usuario = new Usuario(Integer.valueOf(1001), "Test team");
        usuario.setLocale("EN");
        usuario.setJornada(Integer.valueOf(jornada));
        usuario.getTipo_entrenamiento(0).put(Integer.valueOf(jornada), TIPO_ENTRENAMIENTO.Rapidez);
        usuario.getEntrenador_principal().put(Integer.valueOf(jornada), new Jugador(new Integer[] {
                Integer.valueOf(16), Integer.valueOf(paceLevel), Integer.valueOf(16), Integer.valueOf(16),
                Integer.valueOf(16), Integer.valueOf(16), Integer.valueOf(16), Integer.valueOf(16), Integer.valueOf(16)
        }));

        String label = usuario.getStr_tipo_entrenamiento(0);
        return label != null && label.contains("class='warning'");
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

    public static final class Messages extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                { "messages.coach_below_16", "The head coach skill is below 16" },
                { "skills.pace", "Pace" }
            };
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

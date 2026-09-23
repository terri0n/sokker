import java.util.LinkedHashMap;

import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

public final class SevereInjuryMappingHarness {
    private SevereInjuryMappingHarness() {}

    public static void main(String[] args) throws Exception {
        Usuario usuario = new Usuario(Integer.valueOf(1200), "Test team");

        Jugador severeSeven = AsistenteDAO.leer_jugador_entrenamiento(
                true, 1300, false, usuario, trainingPlayerJson(301, 7, true));
        require(Integer.valueOf(8).equals(severeSeven.getLesion()),
                "A severe 7-day injury must be represented as 8 days");

        Jugador nonSevereSeven = AsistenteDAO.leer_jugador_entrenamiento(
                true, 1300, false, usuario, trainingPlayerJson(302, 7, false));
        require(Integer.valueOf(7).equals(nonSevereSeven.getLesion()),
                "A non-severe 7-day injury must remain 7 days");

        Jugador severeSix = AsistenteDAO.leer_jugador_entrenamiento(
                true, 1300, false, usuario, trainingPlayerJson(303, 6, true));
        require(Integer.valueOf(6).equals(severeSix.getLesion()),
                "Only the severe 7-day boundary must be adjusted");

        System.out.println("Severe injury mapping harness OK");
    }

    private static LinkedHashMap<String, Object> trainingPlayerJson(int pid, int daysRemaining, boolean severe) {
        LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("id", Integer.valueOf(pid));
        root.put("effectiveness", Integer.valueOf(50));

        LinkedHashMap<String, Object> formation = new LinkedHashMap<String, Object>();
        formation.put("code", Integer.valueOf(1));
        root.put("formation", formation);

        LinkedHashMap<String, Object> info = new LinkedHashMap<String, Object>();
        root.put("info", info);

        LinkedHashMap<String, Object> name = new LinkedHashMap<String, Object>();
        name.put("full", "Injured player " + pid);
        info.put("name", name);

        LinkedHashMap<String, Object> characteristics = new LinkedHashMap<String, Object>();
        characteristics.put("age", Integer.valueOf(20));
        characteristics.put("height", Integer.valueOf(180));
        characteristics.put("weight", Double.valueOf(75.0));
        characteristics.put("bmi", Double.valueOf(23.1));
        info.put("characteristics", characteristics);

        LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
        value.put("value", Integer.valueOf(100000));
        info.put("value", value);

        LinkedHashMap<String, Object> team = new LinkedHashMap<String, Object>();
        team.put("id", Integer.valueOf(1200));
        team.put("nationalType", Integer.valueOf(0));
        info.put("team", team);
        info.put("nationalCallUp", Boolean.FALSE);

        LinkedHashMap<String, Object> country = new LinkedHashMap<String, Object>();
        country.put("code", Integer.valueOf(1));
        info.put("country", country);

        LinkedHashMap<String, Object> stats = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> cards = new LinkedHashMap<String, Object>();
        cards.put("cards", Integer.valueOf(0));
        stats.put("cards", cards);
        info.put("stats", stats);

        LinkedHashMap<String, Object> injury = new LinkedHashMap<String, Object>();
        injury.put("daysRemaining", Integer.valueOf(daysRemaining));
        injury.put("severe", Boolean.valueOf(severe));
        info.put("injury", injury);

        LinkedHashMap<String, Object> wage = new LinkedHashMap<String, Object>();
        wage.put("value", Integer.valueOf(1000));
        info.put("wage", wage);

        LinkedHashMap<String, Object> skills = new LinkedHashMap<String, Object>();
        skills.put("form", Integer.valueOf(10));
        skills.put("tacticalDiscipline", Integer.valueOf(10));
        skills.put("experience", Integer.valueOf(10));
        skills.put("teamwork", Integer.valueOf(10));
        skills.put("stamina", Integer.valueOf(10));
        skills.put("pace", Integer.valueOf(10));
        skills.put("technique", Integer.valueOf(10));
        skills.put("passing", Integer.valueOf(10));
        skills.put("keeper", Integer.valueOf(10));
        skills.put("defending", Integer.valueOf(10));
        skills.put("playmaking", Integer.valueOf(10));
        skills.put("striker", Integer.valueOf(10));
        info.put("skills", skills);

        return root;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

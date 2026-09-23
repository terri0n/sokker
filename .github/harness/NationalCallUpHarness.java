import java.util.LinkedHashMap;

import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

public final class NationalCallUpHarness {
    private NationalCallUpHarness() {}

    public static void main(String[] args) throws Exception {
        playerSnapshotMustUseNationalCallUpInsteadOfNationalType();
        trainingSnapshotMustUseNationalCallUpInsteadOfNationalType();
        System.out.println("National call-up harness OK");
    }

    private static void playerSnapshotMustUseNationalCallUpInsteadOfNationalType() throws Exception {
        Usuario usuario = new Usuario(Integer.valueOf(1200), "Test team");

        Jugador convocado = AsistenteDAO.leer_jugador(
                Integer.valueOf(101), false, 1300, false, usuario,
                playerJson(101, true, 0, false));
        require(Integer.valueOf(1).equals(convocado.getNt()),
                "nationalCallUp=true must mark the player as selected even when nationalType=0");

        Jugador noConvocado = AsistenteDAO.leer_jugador(
                Integer.valueOf(102), false, 1300, false, usuario,
                playerJson(102, false, 1, false));
        require(Integer.valueOf(0).equals(noConvocado.getNt()),
                "nationalCallUp=false must not mark the player even when nationalType=1");
    }

    private static void trainingSnapshotMustUseNationalCallUpInsteadOfNationalType() throws Exception {
        Usuario usuario = new Usuario(Integer.valueOf(1200), "Test team");

        Jugador convocado = AsistenteDAO.leer_jugador_entrenamiento(
                true, 1300, false, usuario,
                playerJson(201, true, 0, true));
        require(Integer.valueOf(1).equals(convocado.getNt()),
                "Training snapshots must preserve nationalCallUp without an extra player request");

        Jugador noConvocado = AsistenteDAO.leer_jugador_entrenamiento(
                false, 1300, false, usuario,
                playerJson(202, false, 1, true));
        require(Integer.valueOf(0).equals(noConvocado.getNt()),
                "Training snapshots must ignore team.nationalType for the call-up star");
    }

    private static LinkedHashMap<String, Object> playerJson(int pid, boolean nationalCallUp,
            int nationalType, boolean training) {
        LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("id", Integer.valueOf(pid));

        LinkedHashMap<String, Object> info = new LinkedHashMap<String, Object>();
        root.put("info", info);

        LinkedHashMap<String, Object> name = new LinkedHashMap<String, Object>();
        name.put("full", "Test player " + pid);
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
        team.put("nationalType", Integer.valueOf(nationalType));
        info.put("team", team);
        info.put("nationalCallUp", Boolean.valueOf(nationalCallUp));

        LinkedHashMap<String, Object> country = new LinkedHashMap<String, Object>();
        country.put("code", Integer.valueOf(1));
        info.put("country", country);

        LinkedHashMap<String, Object> stats = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> cards = new LinkedHashMap<String, Object>();
        cards.put("cards", Integer.valueOf(0));
        stats.put("cards", cards);
        info.put("stats", stats);

        LinkedHashMap<String, Object> injury = new LinkedHashMap<String, Object>();
        injury.put("daysRemaining", Integer.valueOf(0));
        injury.put("severe", Boolean.FALSE);
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

        if (training) {
            LinkedHashMap<String, Object> formation = new LinkedHashMap<String, Object>();
            formation.put("code", Integer.valueOf(1));
            root.put("formation", formation);
            root.put("effectiveness", Integer.valueOf(50));
        }

        return root;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

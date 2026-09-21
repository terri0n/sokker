import java.io.IOException;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

public final class NtSkillMergeHarness {
    private NtSkillMergeHarness() {}

    public static void main(String[] args) throws Exception {
        partialNtApiSkillsMustNotOverwriteKnownPrivateSkills();
        completeManualSkillsMustStillReplaceKnownSkills();
    }

    private static void partialNtApiSkillsMustNotOverwriteKnownPrivateSkills() throws IOException {
        Jugador conocido = jugadorBase(40159671, 1200, 19, 278000);
        setSkills(conocido, 2, 10, 0, 7, 15, 1, 2, 2);
        conocido.setActualizado(true);

        // La API JSON puede devolver un bloque de skills parcial. JSONUtil devuelve null para
        // los campos ausentes, así que no podemos considerar fiable el bloque solo porque stamina exista.
        LinkedHashMap<String, Object> json = playerJson(40159671, 19, 299250, 46720);
        LinkedHashMap<String, Object> skills = mapAt(json, "info", "skills");
        skills.put("stamina", Integer.valueOf(2));
        skills.put("keeper", Integer.valueOf(1));
        // pace/technique/passing/defending/playmaking/striker se omiten deliberadamente.

        Usuario usuario = new Usuario(Integer.valueOf(406), "Australia U21");
        Jugador parcial = AsistenteDAO.leer_jugador(Integer.valueOf(40159671), true, 1200, false, usuario, json);
        require(!parcial.isActualizado(),
                "A partial JSON skill block was incorrectly marked as reliable");

        Jugador combinado = AsistenteBO.combinar_jugadores(parcial, conocido);
        require(Integer.valueOf(15).equals(combinado.getPorteria()),
                "A partial NT API snapshot overwrote the known keeper skill");
        require(Integer.valueOf(10).equals(combinado.getRapidez()),
                "A partial NT API snapshot lost a known outfield skill");
        require(Integer.valueOf(7).equals(combinado.getPases()),
                "A partial NT API snapshot lost a known passing skill");
        require(Integer.valueOf(299250).equals(combinado.getValor()),
                "Public values from the NT API snapshot were not refreshed");
    }

    private static void completeManualSkillsMustStillReplaceKnownSkills() {
        Jugador conocido = jugadorBase(99, 1200, 20, 100000);
        setSkills(conocido, 2, 3, 4, 5, 6, 7, 8, 9);
        conocido.setActualizado(true);

        // Las actualizaciones manuales pueden marcarse como no fiables y aun así deben guardar
        // las ocho habilidades introducidas por el usuario.
        Jugador completo = jugadorBase(99, 1200, 20, 110000);
        setSkills(completo, 3, 4, 5, 6, 7, 8, 9, 10);
        completo.setActualizado(false);

        Jugador combinado = AsistenteBO.combinar_jugadores(completo, conocido);
        require(Integer.valueOf(7).equals(combinado.getPorteria()),
                "A complete manual skill snapshot was incorrectly ignored");
        require(Integer.valueOf(10).equals(combinado.getAnotacion()),
                "A complete manual skill snapshot did not replace the old skills");
    }

    private static LinkedHashMap<String, Object> playerJson(int pid, int age, int value, int tid) {
        LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("id", Integer.valueOf(pid));

        LinkedHashMap<String, Object> info = new LinkedHashMap<String, Object>();
        root.put("info", info);

        LinkedHashMap<String, Object> name = new LinkedHashMap<String, Object>();
        name.put("full", "George Caravella");
        info.put("name", name);

        LinkedHashMap<String, Object> characteristics = new LinkedHashMap<String, Object>();
        characteristics.put("age", Integer.valueOf(age));
        characteristics.put("height", Integer.valueOf(174));
        characteristics.put("weight", Double.valueOf(64.4));
        characteristics.put("bmi", Double.valueOf(21.29));
        info.put("characteristics", characteristics);

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<String, Object>();
        valueMap.put("value", Integer.valueOf(value));
        info.put("value", valueMap);

        LinkedHashMap<String, Object> team = new LinkedHashMap<String, Object>();
        team.put("id", Integer.valueOf(tid));
        team.put("nationalType", Integer.valueOf(1));
        info.put("team", team);

        LinkedHashMap<String, Object> country = new LinkedHashMap<String, Object>();
        country.put("code", Integer.valueOf(6));
        info.put("country", country);

        LinkedHashMap<String, Object> injury = new LinkedHashMap<String, Object>();
        injury.put("daysRemaining", Integer.valueOf(0));
        info.put("injury", injury);

        LinkedHashMap<String, Object> wage = new LinkedHashMap<String, Object>();
        wage.put("value", Integer.valueOf(3800));
        info.put("wage", wage);

        LinkedHashMap<String, Object> skills = new LinkedHashMap<String, Object>();
        skills.put("form", Integer.valueOf(9));
        skills.put("tacticalDiscipline", Integer.valueOf(0));
        skills.put("experience", Integer.valueOf(0));
        skills.put("teamwork", Integer.valueOf(0));
        info.put("skills", skills);

        return root;
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> mapAt(LinkedHashMap<String, Object> root, String first, String second) {
        return (LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) root.get(first)).get(second);
    }

    private static Jugador jugadorBase(int pid, int jornada, int edad, int valor) {
        Jugador j = new Jugador(pid, "Test player", edad, valor, 46720, null);
        j.setJornada(jornada);
        j.setForma(9);
        j.setPais(6);
        j.setTarjetas(0);
        j.setNt(1);
        j.setLesion(0);
        j.setMinutos(0f);
        return j;
    }

    private static void setSkills(Jugador j, int stamina, int pace, int technique, int passing,
            int keeper, int defending, int playmaking, int striker) {
        j.setCondicion(stamina);
        j.setRapidez(pace);
        j.setTecnica(technique);
        j.setPases(passing);
        j.setPorteria(keeper);
        j.setDefensa(defending);
        j.setCreacion(playmaking);
        j.setAnotacion(striker);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

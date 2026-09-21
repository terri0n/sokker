import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;

public final class NtSkillMergeHarness {
    private NtSkillMergeHarness() {}

    public static void main(String[] args) {
        partialNtSkillsMustNotOverwriteKnownPrivateSkills();
        completeManualSkillsMustStillReplaceKnownSkills();
    }

    private static void partialNtSkillsMustNotOverwriteKnownPrivateSkills() {
        Jugador conocido = jugadorBase(40159671, 1200, 19, 278000);
        setSkills(conocido, 2, 10, 0, 7, 15, 1, 2, 2);
        conocido.setActualizado(true);

        // Simula una respuesta de listado NT: algunos valores vienen presentes, otros no.
        // El código anterior solo miraba stamina y podía sustituir el snapshot privado completo.
        Jugador parcial = jugadorBase(40159671, 1200, 19, 299250);
        parcial.setCondicion(2);
        parcial.setPorteria(1);
        parcial.setActualizado(false);

        Jugador combinado = AsistenteBO.combinar_jugadores(parcial, conocido);

        require(Integer.valueOf(15).equals(combinado.getPorteria()),
                "A partial NT snapshot overwrote the known keeper skill");
        require(Integer.valueOf(10).equals(combinado.getRapidez()),
                "A partial NT snapshot lost a known outfield skill");
        require(Integer.valueOf(7).equals(combinado.getPases()),
                "A partial NT snapshot lost a known passing skill");
        require(Integer.valueOf(299250).equals(combinado.getValor()),
                "Public values from the NT snapshot were not refreshed");
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

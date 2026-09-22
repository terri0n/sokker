import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;

public final class TrainingAgeBoundaryHarness {
    private TrainingAgeBoundaryHarness() {}

    public static void main(String[] args) throws Exception {
        seasonBoundaryAdjustment();
        updateFlowKeepsCurrentPlayerAge();
        manualPlayerSaveKeepsCurrentPlayerAge();
        currentTrainingRowUsesCurrentSnapshot();
        firstTrainingHistoryKeepsPreBirthdayAge();
    }

    private static void seasonBoundaryAdjustment() throws Exception {
        Method adjust = Navegador.class.getMethod(
                "calcular_ajuste_edad", LinkedHashMap.class);

        require(invoke(adjust, current(1002, 0)) == -1,
                "Saturday after the birthday must keep the previous Thursday as the training boundary");
        require(invoke(adjust, current(1002, 1)) == -1,
                "Sunday after the birthday must keep the previous Thursday as the training boundary");
        require(invoke(adjust, current(1002, 4)) == -1,
                "Wednesday before the first new-season training must still keep the boundary signal");

        require(invoke(adjust, current(1001, 5)) == 1,
                "Keep the historical Thursday season-boundary signal unchanged");
        require(invoke(adjust, current(1001, 6)) == 0,
                "Friday before the birthday must not change the boundary");
        require(invoke(adjust, current(1002, 5)) == 0,
                "First Thursday of the new season must use the normal boundary");
        require(invoke(adjust, current(1001, 1)) == 0,
                "Ordinary weeks must not change the boundary");
    }

    private static void updateFlowKeepsCurrentPlayerAge() throws Exception {
        String bo = read("sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java");
        require(bo.contains("int ajuste_edad, boolean registro"),
                "Team update must still receive the season-boundary signal for compatibility");
        require(bo.contains("boolean incrementar_edad = ajuste_edad > 0;"),
                "The historical Thursday +1 path must remain available");
        require(!bo.contains("jugador.setEdad(jugador.getEdad() + ajuste_edad)"),
                "The negative boundary signal must not rewrite the live player age");
        require(bo.contains("AsistenteDAO.obtener_entrenamiento(_usuario, jornada_actual, incrementar_edad, navegador)"),
                "Club training snapshots must preserve the historical Thursday +1 path");
        require(bo.contains("AsistenteDAO.obtener_jugadores(tid, jornada_actual, incrementar_edad, _usuario, navegador)"),
                "National-team snapshots must preserve the historical Thursday +1 path");
        require(bo.contains("AsistenteDAO.obtener_jugador(j.getPid(), tid < NtdbBO.MAX_ID_SELECCION, jornada_actual, incrementar_edad, _usuario, navegador)"),
                "Individually refreshed players must preserve the historical Thursday +1 path");
        require(bo.contains("juvenil.setEdad(juvenil.getEdad() + ajuste_edad)"),
                "Junior handling must remain unchanged by this player-only fix");
        require(bo.contains("ajuste_edad[0] = getAjuste_edad()"),
                "Automatic updates must preserve the boundary signal calculated from /api/current");

        assertCallerUsesAdjustment("sokker/src/com/formulamanager/sokker/acciones/asistente/Actualizar.java");
        assertCallerUsesAdjustment("sokker/src/com/formulamanager/sokker/acciones/asistente/Registro.java");
        assertCallerUsesAdjustment("sokker/src/com/formulamanager/sokker/acciones/asistente/CambiarPassword.java");
    }

    private static void manualPlayerSaveKeepsCurrentPlayerAge() throws Exception {
        String source = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Grabar.java");
        require(source.contains("final int ajuste_edad = getAjuste_edad();"),
                "Manual player save must keep the season-boundary signal so Thursday +1 is not lost");
        require(source.contains("j_nuevo = AsistenteDAO.obtener_jugador(pid, tid < NtdbBO.MAX_ID_SELECCION, jornada_actual, ajuste_edad > 0, _usuario, navegador)"),
                "Manual player save must preserve the historical Thursday +1 path");
        require(!source.contains("j_nuevo.setEdad(j_nuevo.getEdad() + ajuste_edad)"),
                "Manual player save must not apply the negative signal to the live age");
    }

    private static void currentTrainingRowUsesCurrentSnapshot() throws Exception {
        String jugador = read("sokker/src/com/formulamanager/sokker/entity/Jugador.java");
        require(jugador.contains("original.getEntrenamientosTemporada2(habilidad, entrenamiento_act)"),
                "Completed training history must continue to be calculated from original snapshots");
        require(jugador.contains("actual.jornadas.add(this);"),
                "The partial current training row must continue to use the current player snapshot");
    }

    private static void firstTrainingHistoryKeepsPreBirthdayAge() throws Exception {
        Method adjustHistory = AsistenteBO.class.getMethod(
                "ajustar_edades_historicas", Jugador.class, int.class);

        // Caso de RaWm1: no había histórico anterior en esta instalación. Al llegar la
        // última jornada, el snapshot vivo usa 19 por el ajuste del cumpleaños, pero los
        // únicos entrenamientos ya completados siguen perteneciendo a los 18 años.
        Jugador oldest = player(1207, 18);
        Jugador previous = player(1208, 18);
        previous.setOriginal(oldest);
        Jugador current = player(1209, 19);
        Jugador combined = AsistenteBO.combinar_jugadores(current, previous);
        adjustHistory.invoke(null, combined, Integer.valueOf(1));
        require(Integer.valueOf(19).equals(combined.getEdad()),
                "Historical correction changed the live Thursday age");
        require(Integer.valueOf(18).equals(combined.getOriginal().getEdad()),
                "The first historical training inherited age 19");
        require(Integer.valueOf(18).equals(combined.getOriginal().getOriginal().getEdad()),
                "The second historical training inherited age 19");

        // Tras el cumpleaños la edad viva ya es realmente 19, pero la frontera del último
        // entrenamiento sigue siendo la misma hasta el jueves siguiente.
        Jugador postBirthday = player(1209, 19);
        postBirthday.setOriginal(player(1208, 19));
        adjustHistory.invoke(null, postBirthday, Integer.valueOf(-1));
        require(Integer.valueOf(19).equals(postBirthday.getEdad()),
                "Post-birthday correction changed the live age");
        require(Integer.valueOf(18).equals(postBirthday.getOriginal().getEdad()),
                "Post-birthday history kept the live age");

        // El viernes antes del cumpleaños no se debe anticipar el cambio de edad.
        Jugador friday = player(1209, 18);
        friday.setOriginal(player(1208, 18));
        adjustHistory.invoke(null, friday, Integer.valueOf(0));
        require(Integer.valueOf(18).equals(friday.getOriginal().getEdad()),
                "Friday before the birthday incorrectly decremented historical age");

        // Una vez empieza la primera jornada de la temporada nueva, cruzar a la jornada
        // anterior sí debe restar un año aunque ajuste_edad ya sea 0.
        Jugador newSeason = player(1210, 19);
        newSeason.setOriginal(player(1209, 19));
        adjustHistory.invoke(null, newSeason, Integer.valueOf(0));
        require(Integer.valueOf(18).equals(newSeason.getOriginal().getEdad()),
                "Previous-season history did not cross the birthday boundary");

        // La reparación debe ejecutarse sobre la cadena ya completa y antes de persistirla.
        String bo = read("sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java");
        int correction = bo.indexOf("ajustar_edades_historicas(j, ajuste_edad);");
        int save = bo.indexOf("grabar_jugadores(jugadores_actualizados");
        require(correction >= 0 && save > correction,
                "Historical ages must be corrected after building history and before saving players");
    }

    private static Jugador player(int week, int age) {
        Jugador j = new Jugador(Integer.valueOf(40219073), "Elio Caldera", Integer.valueOf(age),
                Integer.valueOf(100000), Integer.valueOf(138447), null);
        j.setJornada(Integer.valueOf(week));
        j.setCondicion(Integer.valueOf(4));
        j.setRapidez(Integer.valueOf(10));
        return j;
    }

    private static void assertCallerUsesAdjustment(String path) throws Exception {
        String source = read(path);
        require(source.contains("getAjuste_edad(),"),
                path + " must pass the season-boundary signal to actualizar_equipo");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static int invoke(Method method, LinkedHashMap<String, Object> current) throws Exception {
        return ((Integer) method.invoke(null, current)).intValue();
    }

    private static LinkedHashMap<String, Object> current(int week, int day) {
        LinkedHashMap<String, Object> today = new LinkedHashMap<String, Object>();
        today.put("week", Integer.valueOf(week));
        today.put("day", Integer.valueOf(day));
        LinkedHashMap<String, Object> current = new LinkedHashMap<String, Object>();
        current.put("today", today);
        return current;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

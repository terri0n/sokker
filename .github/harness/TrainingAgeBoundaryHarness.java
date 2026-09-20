import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.Navegador;

public final class TrainingAgeBoundaryHarness {
    private TrainingAgeBoundaryHarness() {}

    public static void main(String[] args) throws Exception {
        seasonBoundaryAdjustment();
        updateFlowKeepsCurrentPlayerAge();
        manualPlayerSaveKeepsCurrentPlayerAge();
        currentTrainingRowUsesCurrentSnapshot();
    }

    private static void seasonBoundaryAdjustment() throws Exception {
        Method adjust = Navegador.class.getMethod(
                "calcular_ajuste_edad", LinkedHashMap.class);

        require(invoke(adjust, current(1002, 0)) == -1,
                "Saturday after the birthday must keep the previous Thursday as the training boundary");
        require(invoke(adjust, current(1002, 1)) == -1,
                "Sunday after the birthday must keep the previous Thursday as the training boundary");
        require(invoke(adjust, current(1002, 4)) == -1,
                "Wednesday before the first new-season training must still use the previous training boundary");

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
        require(!bo.contains("jugador.setEdad(jugador.getEdad() + ajuste_edad)"),
                "The current player snapshot must keep the real age returned by Sokker");
        require(bo.contains("AsistenteDAO.obtener_entrenamiento(_usuario, jornada_actual, false, navegador)"),
                "Club training snapshots must use the real player age from the API");
        require(bo.contains("AsistenteDAO.obtener_jugadores(tid, jornada_actual, false, _usuario, navegador)"),
                "National-team player snapshots must use the real player age from the API");
        require(bo.contains("AsistenteDAO.obtener_jugador(j.getPid(), tid < NtdbBO.MAX_ID_SELECCION, jornada_actual, false, _usuario, navegador)"),
                "Individually refreshed players must use the real player age from the API");
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
        require(!source.contains("final int ajuste_edad = getAjuste_edad();"),
                "Manual player save must not apply the training-boundary age correction to current state");
        require(source.contains("j_nuevo = AsistenteDAO.obtener_jugador(pid, tid < NtdbBO.MAX_ID_SELECCION, jornada_actual, false, _usuario, navegador)"),
                "Manual player save must read the real current age from Sokker");
        require(!source.contains("j_nuevo.setEdad(j_nuevo.getEdad() + ajuste_edad)"),
                "Manual player save must not rewrite the current age for the previous training boundary");
    }

    private static void currentTrainingRowUsesCurrentSnapshot() throws Exception {
        String jugador = read("sokker/src/com/formulamanager/sokker/entity/Jugador.java");
        require(jugador.contains("original.getEntrenamientosTemporada2(habilidad, entrenamiento_act)"),
                "Completed training history must continue to be calculated from original snapshots");
        require(jugador.contains("actual.jornadas.add(this);"),
                "The partial current training row must continue to use the current player snapshot");
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

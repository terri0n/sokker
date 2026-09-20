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
        updateFlowUsesAdjustment();
        manualPlayerSaveKeepsSignedAdjustment();
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

    private static void updateFlowUsesAdjustment() throws Exception {
        String bo = read("sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java");
        require(bo.contains("int ajuste_edad, boolean registro"),
                "Team update must receive the season-boundary signal");
        require(!bo.contains("jugador.setEdad(jugador.getEdad() + ajuste_edad)"),
                "The current player snapshot must keep the real age returned by Sokker");
        require(bo.contains("jugador.setJornada(jornada_actual + 1)"),
                "After the birthday, the partial current training must live in the new-age row");
        require(bo.contains("juvenil.setEdad(juvenil.getEdad() + ajuste_edad)"),
                "Junior handling must remain unchanged by this player-only fix");
        require(bo.contains("ajuste_edad[0] = getAjuste_edad()"),
                "Automatic updates must preserve the boundary signal calculated from /api/current");

        assertCallerUsesAdjustment("sokker/src/com/formulamanager/sokker/acciones/asistente/Actualizar.java");
        assertCallerUsesAdjustment("sokker/src/com/formulamanager/sokker/acciones/asistente/Registro.java");
        assertCallerUsesAdjustment("sokker/src/com/formulamanager/sokker/acciones/asistente/CambiarPassword.java");
    }

    private static void manualPlayerSaveKeepsSignedAdjustment() throws Exception {
        String source = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Grabar.java");
        require(source.contains("final int ajuste_edad = getAjuste_edad();"),
                "Manual player save must carry the season-boundary signal from the outer /api/current lookup");
        require(!source.contains("j_nuevo.setEdad(j_nuevo.getEdad() + ajuste_edad)"),
                "Manual player save must keep the real current age returned by Sokker");
        require(source.contains("j_nuevo.setJornada(jornada_actual + 1)"),
                "Manual player save must put post-birthday partial training in the new-age row");
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

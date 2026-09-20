import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.Navegador;

public final class TrainingAgeBoundaryHarness {
    private TrainingAgeBoundaryHarness() {}

    public static void main(String[] args) throws Exception {
        Method adjust = Navegador.class.getMethod(
                "calcular_ajuste_edad", LinkedHashMap.class);

        require(invoke(adjust, current(1002, 0)) == -1,
                "Saturday after the birthday must store the previous Thursday training at age - 1");
        require(invoke(adjust, current(1002, 1)) == -1,
                "Sunday after the birthday must store the previous Thursday training at age - 1");
        require(invoke(adjust, current(1002, 4)) == -1,
                "Wednesday before the first new-season training must still use age - 1");

        require(invoke(adjust, current(1001, 5)) == 1,
                "Keep the historical Thursday season-boundary correction unchanged");
        require(invoke(adjust, current(1001, 6)) == 0,
                "Friday before the birthday must not change age");
        require(invoke(adjust, current(1002, 5)) == 0,
                "First Thursday of the new season must use the API age");
        require(invoke(adjust, current(1001, 1)) == 0,
                "Ordinary weeks must not change age");
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

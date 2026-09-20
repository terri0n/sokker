import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.Navegador;

public class TrainingAgeBoundaryHarness {
    public static void main(String[] args) throws Exception {
        Method method = Navegador.class.getMethod(
                "debe_restar_edad_entrenamiento", LinkedHashMap.class);

        require(invoke(method, current(1002, 0)),
                "Saturday after the birthday must keep the previous Thursday training at the old age");
        require(invoke(method, current(1002, 1)),
                "Sunday after the birthday must keep the previous Thursday training at the old age");
        require(invoke(method, current(1002, 4)),
                "Wednesday after the birthday must keep the previous Thursday training at the old age");

        require(!invoke(method, current(1001, 5)),
                "Thursday of the last season week already reports the training-age value");
        require(!invoke(method, current(1001, 6)),
                "Friday of the last season week is still before the birthday");
        require(!invoke(method, current(1002, 5)),
                "First Thursday of the new season must use the new age");
        require(!invoke(method, current(1001, 1)),
                "Ordinary weeks must not modify the player age");
    }

    private static boolean invoke(Method method, LinkedHashMap<String, Object> current) throws Exception {
        return ((Boolean) method.invoke(null, current)).booleanValue();
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

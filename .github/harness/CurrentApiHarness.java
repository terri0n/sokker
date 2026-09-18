import java.io.IOException;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.Navegador;

public class CurrentApiHarness {
    public static void main(String[] args) throws Exception {
        calculatesCurrentWeekBoundary();
        rejectsMissingWeek();
        rejectsMissingDay();
    }

    private static void calculatesCurrentWeekBoundary() throws Exception {
        LinkedHashMap<String, Object> current = current(Integer.valueOf(1208), Integer.valueOf(5));
        require(Integer.valueOf(1208).equals(Navegador.calcular_jornada_actual(current)),
                "Day 5 must keep the current Sokker week");

        current = current(Integer.valueOf(1208), Integer.valueOf(4));
        require(Integer.valueOf(1207).equals(Navegador.calcular_jornada_actual(current)),
                "Days before 5 must map to the previous training week");
    }

    private static void rejectsMissingWeek() throws Exception {
        expectIncompleteCurrent(current(null, Integer.valueOf(5)), "today.week");
    }

    private static void rejectsMissingDay() throws Exception {
        expectIncompleteCurrent(current(Integer.valueOf(1208), null), "today.day");
    }

    private static LinkedHashMap<String, Object> current(Integer week, Integer day) {
        LinkedHashMap<String, Object> today = new LinkedHashMap<String, Object>();
        if (week != null) {
            today.put("week", week);
        }
        if (day != null) {
            today.put("day", day);
        }
        LinkedHashMap<String, Object> current = new LinkedHashMap<String, Object>();
        current.put("today", today);
        return current;
    }

    private static void expectIncompleteCurrent(LinkedHashMap<String, Object> current, String field) throws Exception {
        try {
            Navegador.calcular_jornada_actual(current);
            throw new AssertionError("Missing " + field + " must reject an incomplete /api/current payload");
        } catch (IOException expected) {
            // Expected: the update must abort explicitly instead of failing through auto-unboxing.
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

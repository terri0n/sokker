import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.JSONUtil;

public class CurrentApiHarness {
    public static void main(String[] args) throws Exception {
        calculatesCurrentWeekBoundary();
        rejectsMissingWeek();
        rejectsMissingDay();
        handlesNullableAndNumericJsonFields();
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

    private static void handlesNullableAndNumericJsonFields() {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("integer", Long.valueOf(42L));
        payload.put("decimal", new BigDecimal("84.7707"));
        LinkedHashMap<String, Object> skills = new LinkedHashMap<String, Object>();
        skills.put("stamina", Integer.valueOf(5));
        LinkedHashMap<String, Object> changes = new LinkedHashMap<String, Object>();
        changes.put("stamina", Integer.valueOf(2));
        payload.put("skills", skills);
        payload.put("skillsChange", changes);

        require(Integer.valueOf(42).equals(JSONUtil.getInteger(payload, "integer")),
                "Numeric JSON fields must accept any Number implementation");
        require(Double.valueOf(84.7707D).equals(JSONUtil.getDouble(payload, "decimal")),
                "Decimal JSON fields must retain their numeric value");
        require(Integer.valueOf(3).equals(JSONUtil.getSkills(payload, "stamina")),
                "Skill delta was not calculated correctly");

        payload.remove("skillsChange");
        require(JSONUtil.getSkills(payload, "stamina") == null,
                "A missing optional skillsChange value must remain null instead of throwing");
        require(JSONUtil.getInteger(payload, "missing") == null,
                "A missing optional integer field must remain null");
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

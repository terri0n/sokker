package com.formulamanager.sokker.auxiliares;

import java.util.List;
import java.util.Map;

/**
 * Traduce el contrato JSON moderno de entrenadores al subconjunto de datos
 * que consumia el XML historico. Si la escala o el rol no pueden demostrarse,
 * devuelve null para forzar el fallback legado.
 */
public final class SokkerTrainerMapping {
    private static final String[] SKILLS = {
        "stamina", "pace", "technique", "passing",
        "keeper", "defending", "playmaking", "striker"
    };
    private static final String[] XML_SKILLS = {
        "skillStamina", "skillPace", "skillTechnique", "skillPassing",
        "skillKeeper", "skillDefending", "skillPlaymaking", "skillScoring"
    };
    private static final double PERCENT_PER_LEVEL = 6.25d;

    private SokkerTrainerMapping() {}

    public static String buildXml(Object response) {
        Object trainersValue = value(response, "trainers");
        if (!(trainersValue instanceof List<?>)) {
            return null;
        }

        List<?> trainers = (List<?>) trainersValue;
        if (trainers.isEmpty()) {
            return null;
        }

        StringBuilder xml = new StringBuilder("<trainers>");
        for (Object trainer : trainers) {
            Integer job = job(string(trainer, "info.assignment.name"));
            if (job == null) {
                // Solo activamos JSON para roles cuyo nombre actual esta demostrado.
                // Un entrenador de juveniles o sin asignar desconocido fuerza XML.
                return null;
            }

            int[] values = new int[SKILLS.length];
            int percentSum = 0;
            for (int i = 0; i < SKILLS.length; i++) {
                Integer skillValue = integer(trainer, "info.skills." + SKILLS[i] + ".value");
                Integer percent = integer(trainer, "info.skills." + SKILLS[i] + ".percent");
                if (!validSkill(skillValue, percent)) {
                    return null;
                }
                values[i] = skillValue.intValue();
                percentSum += percent.intValue();
            }

            Integer averagePercent = integer(trainer, "info.skills.averagePercent");
            if (averagePercent == null || averagePercent < 0 || averagePercent > 100) {
                return null;
            }

            double calculatedAverage = percentSum / 8.0d;
            if (Math.abs(averagePercent.doubleValue() - calculatedAverage) > 1.0d) {
                return null;
            }

            int generalFromApi = visibleLevel(averagePercent.doubleValue());
            int generalFromSkills = visibleLevel(calculatedAverage);
            if (generalFromApi != generalFromSkills) {
                // Evitamos decidir un nivel en una frontera afectada por redondeos.
                return null;
            }

            xml.append("<trainer><job>").append(job).append("</job>");
            for (int i = 0; i < SKILLS.length; i++) {
                xml.append('<').append(XML_SKILLS[i]).append('>')
                   .append(values[i])
                   .append("</").append(XML_SKILLS[i]).append('>');
            }
            xml.append("<skillCoach>").append(generalFromApi)
               .append("</skillCoach></trainer>");
        }
        return xml.append("</trainers>").toString();
    }

    private static int visibleLevel(double percent) {
        return Math.min(16, (int) Math.floor((percent + 1e-9d) / PERCENT_PER_LEVEL));
    }

    private static boolean validSkill(Integer value, Integer percent) {
        if (value == null || percent == null || value < 0 || value > 16
                || percent < 0 || percent > 100) {
            return false;
        }

        double min = value.doubleValue() * PERCENT_PER_LEVEL;
        double max = value.intValue() == 16
                ? 100.0d
                : (value.doubleValue() + 1.0d) * PERCENT_PER_LEVEL;
        return percent.doubleValue() + 1e-9d >= min
                && percent.doubleValue() < max + 1e-9d;
    }

    private static Integer job(String assignment) {
        if ("first".equals(assignment)) {
            return 1;
        }
        if ("assistant".equals(assignment)) {
            return 2;
        }
        return null;
    }

    private static Object value(Object root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?>)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static Integer integer(Object root, String path) {
        Object value = value(root, path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static String string(Object root, String path) {
        Object value = value(root, path);
        return value == null ? null : String.valueOf(value);
    }
}

package com.formulamanager.sokker.auxiliares;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Normaliza el contenedor formations de /api/training/formations. */
public final class SokkerTrainingFormations {
    private SokkerTrainingFormations() {}

    public static List<LinkedHashMap<String, Object>> normalize(Object document) {
        if (!(document instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Respuesta de entrenamientos inválida");
        }

        Object formations = ((Map<?, ?>) document).get("formations");
        Collection<?> values;
        if (formations instanceof List<?>) {
            values = (List<?>) formations;
        } else if (formations instanceof Map<?, ?>) {
            values = ((Map<?, ?>) formations).values();
        } else {
            throw new IllegalArgumentException("El campo formations no es una lista ni un objeto");
        }

        List<LinkedHashMap<String, Object>> result = new ArrayList<LinkedHashMap<String, Object>>();
        for (Object value : values) {
            if (!(value instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("Entrada de formations inválida");
            }

            LinkedHashMap<String, Object> copy = new LinkedHashMap<String, Object>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    throw new IllegalArgumentException("Clave no textual en formations");
                }
                copy.put((String) entry.getKey(), entry.getValue());
            }
            result.add(copy);
        }
        return result;
    }
}

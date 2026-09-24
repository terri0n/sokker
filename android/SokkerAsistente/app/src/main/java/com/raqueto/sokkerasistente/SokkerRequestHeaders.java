package com.raqueto.sokkerasistente;

import java.util.LinkedHashMap;
import java.util.Map;

final class SokkerRequestHeaders {
    static final String USER_AGENT = "Sokker Asistente (+https://raqueto.com/sokker/asistente)";

    private SokkerRequestHeaders() {
    }

    static Map<String, String> forSokker(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String name = entry.getKey();
                if (name != null && !"User-Agent".equalsIgnoreCase(name)) {
                    result.put(name, entry.getValue());
                }
            }
        }
        result.put("User-Agent", USER_AGENT);
        return result;
    }
}

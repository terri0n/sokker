package com.formulamanager.sokker.auxiliares;

import java.util.Properties;

/**
 * Compatibilidad de las bases .properties de jugadores y juveniles.
 * Las claves numericas son PIDs gestionados por Sokker Asistente; cualquier
 * otra clave se considera metadato desconocido y debe sobrevivir al guardado.
 */
public final class PlayerPropertiesCompat {
    private PlayerPropertiesCompat() {}

    public static boolean isPlayerDatabase(String fileName) {
        return fileName != null
                && fileName.matches("[0-9]+(?:(?:_historico)?(?:_juveniles)?|_juveniles_historico)?\\.properties");
    }

    public static void preserveUnmanagedKeys(Properties previous, Properties next) {
        for (String key : previous.stringPropertyNames()) {
            if (!isManagedPlayerKey(key) && !next.containsKey(key)) {
                next.setProperty(key, previous.getProperty(key));
            }
        }
    }

    private static boolean isManagedPlayerKey(String key) {
        return key != null && key.matches("[0-9]+");
    }
}

package com.formulamanager.sokker.auxiliares;

/**
 * Regla puntual para reparar las edades históricas dañadas en el cambio de
 * temporada de septiembre de 2026.
 *
 * La edad del snapshot más reciente es la única referencia. Ese snapshot
 * puede ser la jornada 1 de la temporada nueva (1210) o la jornada 13 de la
 * anterior (1209) si el equipo todavía no se ha actualizado esta semana.
 * Solo los snapshots anteriores de la temporada 1197..1209 deben tener un
 * año menos que esa edad de referencia.
 */
public final class TrainingAgeSeasonRepair {
    public static final int PREVIOUS_SEASON_START = 1197;
    public static final int PREVIOUS_SEASON_END = 1209;
    public static final int PREVIOUS_WEEK_REFERENCE = 1209;
    public static final int CURRENT_WEEK_REFERENCE = 1210;

    private TrainingAgeSeasonRepair() {
    }

    public static Integer expectedHistoricalAge(int latestWeek, int latestAge, int snapshotWeek) {
        if (latestWeek != PREVIOUS_WEEK_REFERENCE && latestWeek != CURRENT_WEEK_REFERENCE) {
            return null;
        }

        // El snapshot usado como referencia representa el estado actual y no se toca.
        if (snapshotWeek >= latestWeek) {
            return null;
        }

        if (snapshotWeek < PREVIOUS_SEASON_START || snapshotWeek > PREVIOUS_SEASON_END) {
            return null;
        }

        return Integer.valueOf(latestAge - 1);
    }
}

import java.util.List;

import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.Entrenamiento;

public final class TrainingHistoryNullSnapshotHarness {
    private TrainingHistoryNullSnapshotHarness() {}

    public static void main(String[] args) {
        nullSkillSnapshotMustNotDiscardPreviousTrainingBlock();
    }

    private static void nullSkillSnapshotMustNotDiscardPreviousTrainingBlock() {
        Jugador actual = snapshot(40029074, 1200, 19, Integer.valueOf(10));
        Jugador anterior = snapshot(40029074, 1199, 18, Integer.valueOf(10));
        Jugador marcadorSinHabilidades = snapshot(40029074, 1198, 18, null);

        actual.setOriginal(anterior);
        anterior.setOriginal(marcadorSinHabilidades);

        Entrenamiento bloque = actual.new Entrenamiento(actual.getRapidez().intValue());
        List<Entrenamiento> entrenamientos = actual.getEntrenamientosTemporada2(TIPO_ENTRENAMIENTO.Rapidez, bloque);

        require(entrenamientos.size() == 1,
                "A null-skill historical snapshot discarded the accumulated training block");
        require(entrenamientos.get(0).jornadas.size() == 2,
                "The preserved training block must contain both valid snapshots before the null marker");
        require(entrenamientos.get(0).jornadas.get(0).getJornada().intValue() == 1200,
                "The newest valid snapshot is missing from the preserved training block");
        require(entrenamientos.get(0).jornadas.get(1).getJornada().intValue() == 1199,
                "The oldest valid snapshot before the null marker is missing from the training block");
    }

    private static Jugador snapshot(int pid, int jornada, int edad, Integer rapidez) {
        Jugador j = new Jugador(Integer.valueOf(pid), "Test player", Integer.valueOf(edad), Integer.valueOf(100000), Integer.valueOf(30019), null);
        j.setJornada(Integer.valueOf(jornada));
        j.setRapidez(rapidez);
        // Fuerza 0 puntos sin necesitar Usuario/entrenadores; aquí solo probamos la conservación de la cadena.
        j.setLesion(Integer.valueOf(8));
        return j;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

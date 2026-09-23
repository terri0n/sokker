package com.formulamanager.sokker.acciones.asistente;

import java.util.Arrays;
import java.util.List;

import com.formulamanager.sokker.entity.Jugador;

public final class CambiarEdadHarness {
    private CambiarEdadHarness() {}

    public static void main(String[] args) {
        Jugador actual = jugador(200, 25);
        Jugador anterior = jugador(199, 24);
        Jugador objetivo = jugador(198, 23);
        Jugador antiguo = jugador(197, 22);
        actual.setOriginal(anterior);
        anterior.setOriginal(objetivo);
        objetivo.setOriginal(antiguo);

        Jugador segundo = jugador(200, 30);
        Jugador segundoAnterior = jugador(199, 29);
        segundo.setOriginal(segundoAnterior);

        List<Jugador> jugadores = Arrays.asList(actual, segundo);
        CambiarEdad.ajustarEdad(jugadores, 198, 2);

        require(Integer.valueOf(25).equals(actual.getEdad()), "Current snapshot must not change");
        require(Integer.valueOf(24).equals(anterior.getEdad()), "Unrelated historical snapshot must not change");
        require(Integer.valueOf(25).equals(objetivo.getEdad()), "Requested historical week must change");
        require(Integer.valueOf(22).equals(antiguo.getEdad()), "Older snapshots must not change");
        require(Integer.valueOf(30).equals(segundo.getEdad()), "Players without the requested week must remain unchanged");
        require(Integer.valueOf(29).equals(segundoAnterior.getEdad()), "Their history must also remain unchanged");

        CambiarEdad.ajustarEdad(jugadores, 200, -1);
        require(Integer.valueOf(24).equals(actual.getEdad()), "Current week must be adjustable too");
        require(Integer.valueOf(29).equals(segundo.getEdad()), "Adjustment applies to every player with that week");
        require(Integer.valueOf(25).equals(objetivo.getEdad()), "Historical target from previous call must stay intact");

        System.out.println("CambiarEdad harness OK");
    }

    private static Jugador jugador(int jornada, int edad) {
        Jugador j = new Jugador();
        j.setJornada(Integer.valueOf(jornada));
        j.setEdad(Integer.valueOf(edad));
        return j;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

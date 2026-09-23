package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Ajusta manualmente la edad guardada para una jornada concreta.
 */
@WebServlet("/asistente/cambiar_edad")
public class CambiarEdad extends SERVLET_ASISTENTE {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CambiarEdad() {
        super();
    }

    static void ajustarEdad(List<Jugador> jugadores, int jornada, int incr) {
        for (Jugador j : jugadores) {
            Jugador it = j;
            while (it != null) {
                if (it.getJornada() == jornada) {
                    it.setEdad(it.getEdad() + incr);
                    break;
                }
                it = it.getOriginal();
            }
        }
    }

    /**
     * @throws ParseException
     * @throws FailingHttpStatusCodeException
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
        String mensaje = "";

        if (admin(request)) {
            final Usuario usuario = getUsuario(request);
            int jornada = Util.getInteger(request, "jornada");
            int incr = Util.getInteger(request, "incr");

            _log(request, jornada + " " + incr);

            final List<Jugador> jugadores = AsistenteBO.leer_jugadores(
                    usuario.getDef_tid(), usuario.getDef_equipo(), false, usuario);
            ajustarEdad(jugadores, jornada, incr);

            new Navegador(request) {
                @Override
                protected void execute(WebClient navegador)
                        throws FailingHttpStatusCodeException, MalformedURLException, IOException {
                    int jornada_actual = obtener_jornada(navegador);
                    AsistenteBO.grabar_jugadores(
                            jugadores, usuario.getDef_tid(), jornada_actual, false);
                }
            };

            mensaje = "updated";
        }

        response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}

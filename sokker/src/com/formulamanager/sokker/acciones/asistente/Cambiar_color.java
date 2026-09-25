package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
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
 * Cambia el color de varios jugadores de golpe.
 */
@WebServlet("/asistente/cambiar_color")
public class Cambiar_color extends SERVLET_ASISTENTE {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Cambiar_color() {
        super();
    }

    @Override
    protected void execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
        String mensaje = "";

        if (login(request)) {
            Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
            Integer tid = usuario.getDef_tid();
            // La coma del final nos llega en el QueryString.
            String pids = "," + request.getParameter("pids");
            String color = request.getParameter("color");
            boolean historico = Util.getInt(request.getSession(), "historico") > 0;

            _log(request, pids + " color=" + color);

            List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, usuario.getDef_equipo(), historico, usuario);

            Iterator<Jugador> it = jugadores.iterator();
            while (it.hasNext()) {
                Jugador j = it.next();
                if (pids.indexOf("," + j.getPid() + ",") > -1) {
                    j.setColor(color);
                }
            }

            Collections.sort(jugadores, Jugador.getComparator());
            new Navegador(true, request) {
                @Override
                protected void execute(WebClient navegador)
                        throws FailingHttpStatusCodeException, MalformedURLException, IOException {
                    int jornada_actual = obtener_jornada(navegador);
                    AsistenteBO.grabar_jugadores(jugadores, tid, jornada_actual, historico);
                }
            };

            mensaje = "changed";
        }

        response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}

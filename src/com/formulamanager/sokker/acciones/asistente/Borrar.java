package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/asistente/borrar")
public class Borrar extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Borrar() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		if (login(request)) {
			Usuario usuario = (Usuario)request.getSession().getAttribute("usuario");
			Integer tid = usuario.getDef_tid();
			Integer pid = Util.getInteger(request, "pid");
			
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, false);
			List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, true);

			for (Jugador j : jugadores) {
				if (j.getPid().equals(pid)) {
					jugadores_historico.add(j);
					jugadores.remove(j);
					break;
				}
			}
				
			Collections.sort(jugadores, Jugador.getComparator());
			AsistenteBO.grabar_jugadores(jugadores, tid, false);
			AsistenteBO.grabar_jugadores(jugadores_historico, tid, true);
			request.getSession().setAttribute("jugadores", jugadores);

//			AsistenteBO.calcular(jugadores, usuario);				

			mensaje = "removed";
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

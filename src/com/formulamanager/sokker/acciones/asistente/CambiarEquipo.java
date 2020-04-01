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
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/asistente/cambiar_equipo")
public class CambiarEquipo extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CambiarEquipo() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (login(request)) {
			final Usuario usuario = getUsuario(request);
			usuario.setDef_tid(usuario.getDef_tid().equals(usuario.getTid()) ? usuario.getTid_nt() : usuario.getTid());

			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), false);
			Collections.sort(jugadores, Jugador.getComparator());
			request.getSession().setAttribute("jugadores", jugadores);
			UsuarioBO.grabar_usuario(usuario);
			
			AsistenteBO.calcular(jugadores, usuario);	
		}

		response.sendRedirect(request.getContextPath() + "/asistente");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}

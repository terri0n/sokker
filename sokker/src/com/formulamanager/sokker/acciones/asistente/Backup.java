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
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class Backup
 * 
 * Carga un backup de un usuario
 */
@WebServlet("/asistente/backup")
public class Backup extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Backup() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (admin(request)) {
			int jornada = Util.getInteger(request, "jornada");
			boolean recuperar = Util.getBoolean(request, "recuperar");

_log(request, jornada + " " + recuperar);

			Usuario usuario = getUsuario(request);
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), usuario.getDef_equipo(), false, usuario, jornada);
			Collections.sort(jugadores, Jugador.getComparator());
			
			if (recuperar) {
				usuario.setDef_jornada(jornada);
				UsuarioBO.grabar_usuario(usuario);
				AsistenteBO.grabar_jugadores(jugadores, usuario.getDef_tid(), jornada, false);
			}
		}
		response.sendRedirect(request.getContextPath() + "/asistente");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
}

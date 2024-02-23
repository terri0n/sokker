package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;

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
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Servlet implementation class CambiarEquipo
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
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (login(request)) {
			// Borramos los parámetros de la sesión
			request.getSession().setAttribute("juveniles", null);
			request.getSession().setAttribute("historico", null);
			
			final Usuario usuario = getUsuario(request);
			String coach = Util.getString(request, "coach");
			if (coach == null) {
				// Cambiar entre equipo y NT propia
				boolean otra_nt = usuario.getDef_tid() < NtdbBO.MAX_ID_SELECCION && usuario.getTid_nt() != null && !usuario.getDef_tid().equals(usuario.getTid_nt());
				
				usuario.setDef_tid(usuario.getDef_tid().equals(usuario.getTid()) ? usuario.getTid_nt() : usuario.getTid());

				// Si se vuelve al equipo propio, se actualiza el nombre de la NT
				if (otra_nt) {
					int pais = usuario.getTid_nt() % 400;
					usuario.setEquipo_nt(NtdbBO.paises[pais - 1] + (usuario.getTid_nt() < NtdbBO.DIF_NT_U21 ? " NT" : " U21"));
				}
			} else {
				// Cambiar entre equipo y NT scout
				Usuario nt_coach = usuario.getScout_de().get(coach);
				if (nt_coach != null) {
					usuario.setDef_tid(nt_coach.getTid_nt());
					usuario.setEquipo_nt(nt_coach.getEquipo_nt());
				}
			}

			if (usuario.getDef_tid() == null) {
				// NOTA: a Nerozurro le apareció un null en el def_tid. Como no sé si es por esta acción, pongo la comprobación
				throw new RuntimeException("Equipo incorrecto");
			}
			
			UsuarioBO.grabar_usuario(usuario);
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

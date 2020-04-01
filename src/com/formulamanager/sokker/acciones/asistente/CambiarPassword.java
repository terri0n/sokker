package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Cambia la contraseña
 */
@WebServlet("/asistente/cambiar_password")
public class CambiarPassword extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CambiarPassword() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		String login = request.getParameter("login").replace("\\", "").replace("/", "").trim().toLowerCase();
		String password = Util.getMD5(request.getParameter("password"));
		String ilogin = request.getParameter("ilogin");
		String ipassword = request.getParameter("ipassword");
		
		Usuario usuario_ = UsuarioBO.leer_usuario(login);
		
		if (usuario_ == null) {
			throw new RuntimeException(Util.getTexto(request.getLocale().getLanguage(), "messages.user_not_exists"));
		}
		
		new Navegador(false, ilogin, ipassword) {
			@Override
			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
				if (!usuario.getTid().equals(usuario_.getTid())) {
					throw new RuntimeException(Util.getTexto(request.getLocale().getLanguage(), "messages.wrong_team"));
				} else {
					usuario_.setTid_nt(usuario.getTid_nt());
					usuario_.setEquipo_nt(usuario.getEquipo_nt());
					usuario_.setDef_tid(usuario_.getTid());
					
					usuario_.setPassword(password);
					usuario_.setLogin_sokker(ilogin);
					usuario_.setJornada(JugadorBO.obtener_jornada());
					usuario_.setIntentos_fallidos(0);
					usuario_.setLocale(usuario.getLocale());
					
					List<Jugador> jugadores_actualizados = AsistenteBO.actualizar_equipo(ilogin, ipassword, usuario_);
					request.getSession().setAttribute("usuario", usuario_);
					request.getSession().setAttribute("jugadores", jugadores_actualizados);
				}
			}
		};
		
		mensaje = "updated";

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

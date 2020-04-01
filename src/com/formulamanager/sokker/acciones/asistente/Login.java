package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.formulamanager.sokker.tomcat.ServletContextListener;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/asistente/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String alogin = request.getParameter("alogin");
		String apassword = Util.getMD5(request.getParameter("apassword"));
		
		//-----------------
		// COMPROBAR LOGIN
		//-----------------
		String error = "";
		Usuario usuario = UsuarioBO.leer_usuario(alogin);
		
		if (usuario != null && usuario.getIntentos_fallidos() > 2) {
			error = "?mensaje=user_disabled";
		} else if (usuario != null && (usuario.getPassword().equals(apassword))) {
			request.getSession().setAttribute("usuario", usuario);	

			if (usuario.getLocale() != null) {
				Config.set(request.getSession(), Config.FMT_LOCALE, new Locale(usuario.getLocale()));
			}
			
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), false);
			Collections.sort(jugadores, Jugador.getComparator());
			request.getSession().setAttribute("jugadores", jugadores);

			AsistenteBO.calcular(jugadores, usuario);
			
			if (usuario.getIntentos_fallidos() > 0) {
				usuario.setIntentos_fallidos(0);
				UsuarioBO.grabar_usuario(usuario);
			}
						
//			new Navegador() {
//				@Override
//				protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
//					AsistenteBO.enviar_skmail(navegador, "Terrion", "prueba", "lsdkjasdln");					
//				}
//			};
		} else {
			if (usuario != null) {
				usuario.setIntentos_fallidos(usuario.getIntentos_fallidos() + 1);
				UsuarioBO.grabar_usuario(usuario);
			}
			error = "?mensaje=login_error";
		}

		response.sendRedirect(request.getContextPath() + "/asistente" + error);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doPost(request, response);
	}

}

package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.JSONUtil;
import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;

/**
 * Cambia la contrase�a
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
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje = "";
		
		if (!Util.getBoolean(request, "confirmed")) {
			throw new RuntimeException("Login not confirmed");
		} else {
			String login = request.getParameter("login").replace("\\", "").replace("/", "").trim().toLowerCase();
			String ilogin = request.getParameter("ilogin");
			String ipassword = request.getParameter("ipassword");
			
			Usuario usuario_ = UsuarioBO.leer_usuario(login, true);
			
			if (usuario_ == null) {
				throw new LoginException(Util.getTexto(request.getLocale().getLanguage(), "messages.user_not_exists"));
			}
	
			new Navegador(false, ilogin, ipassword, request) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
					Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
					LinkedHashMap<String, Object> datos = JsonPath.read(document, "$");
					Integer tid = JSONUtil.getInteger(datos, "team.id");
					if (tid == null || !tid.equals(usuario_.getTid())) {
						throw new LoginException(Util.getTexto(request.getLocale().getLanguage(), "messages.wrong_team"));
					}

_log(request, "");
					
					int jornada_actual = obtener_jornada_json(navegador);
	
					usuario_.setPassword(Util.getMD5(ipassword));
					usuario_.setLogin_sokker(ilogin);
					usuario_.setIntentos_fallidos(0);

					List<Jugador> jugadores_actualizados = AsistenteBO.actualizar_equipo(usuario_, jornada_actual, getAjuste_edad(), false, navegador, navegador);
					request.getSession().setAttribute("usuario", usuario_);
				}
			};
			
			mensaje = "updated";
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

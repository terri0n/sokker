package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/asistente/registro")
public class Registro extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Registro() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje[] = {""};

		if (!Util.getBoolean(request, "confirmed")) {
			throw new RuntimeException("Login not confirmed");
		} else {
			String login = request.getParameter("login").replace("\\", "").replace("/", "").replace(",", "").trim().toLowerCase();
			String ilogin = request.getParameter("ilogin");
			String ipassword = request.getParameter("ipassword");
			
			if (login.length() == 0 || UsuarioBO.leer_usuario(login, false) != null) {
				throw new LoginException(Util.getTexto(request.getLocale().getLanguage(), "messages.user_already_exists"));
			}
	
			new Navegador(false, ilogin, ipassword, request) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
					Usuario usuario = null;
					try {
						int jornada_actual = obtener_jornada_json(navegador);

						Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
						LinkedHashMap<String, Object> datos = JsonPath.read(document, "$");
						Integer tid = JSONUtil.getInteger(datos, "team.id");
						if (tid == null) {
							throw new LoginException("Error when logging in to Sokker: user has no team");
						}

						usuario = new Usuario(tid, null);
						usuario.inicializar();
						usuario.setLogin(login);
						usuario.setPassword(Util.getMD5(ipassword));
						usuario.setLogin_sokker(ilogin);
						usuario.setJornada(jornada_actual - 100);	// Para recuperar del histórico de entrenamientos
						usuario.setJornada_nt(jornada_actual - 1);	// Esto es porque la NT no se actualiza al registrarte?
						usuario.setIntentos_fallidos(0);
						usuario.setLocale(request.getLocale().getLanguage());
				
_log(request, "");

						List<Jugador> jugadores_actualizados = AsistenteBO.actualizar_equipo(usuario, jornada_actual, isIncrementar_edad(), true, navegador, navegador);
						AsistenteDAO.obtener_datos_NT(navegador, usuario);
					
						request.getSession().setAttribute("usuario", usuario);
						mensaje[0] = "registered";
					} catch (Exception e) {
						mensaje[0] = "Error connecting to Sokker: " + e.toString();
						e.printStackTrace();

						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						SERVLET_ASISTENTE._log_linea("_EXCEPTIONS", "__TID: " + (usuario == null ? "?" : usuario.getDef_tid()) + " -> " + sw.toString() + "\n");
					}
				}
			};
		}
		
		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje[0]);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

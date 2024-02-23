package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

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
	
			new Navegador(true, ilogin, ipassword, request) {
				@Override
				protected void execute(WebClient navegadorXML) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
					try {
						int jornada_actual = obtener_jornada(navegadorXML);
	
						Usuario usuario = getUsuario();	// Aquí está el TID
						
						usuario.inicializar();
						usuario.setLogin(login);
						usuario.setPassword(Util.getMD5(ipassword));
						usuario.setLogin_sokker(ilogin);
						usuario.setJornada(jornada_actual - 100);	// Para recuperar del histórico de entrenamientos
						usuario.setJornada_nt(jornada_actual - 1);	// Esto es porque la NT no se actualiza al registrarte?
						usuario.setIntentos_fallidos(0);
						usuario.setLocale(request.getLocale().getLanguage());
				
_log(request, "");

						// NOTA: quería comentar esta línea para no exigir tener el CORS desabilitado, para los que solo vayan a usar la aplicación para manejar la NT, por ejemplo. Pero si no hago login con el usuario y la contraseña del usuario, no hay forma de saber cuál es su equipo
						// Así que, hasta que cambien la interfaz XML (o JSON), lo dejo como está
						// Se puede crear el archivo en el servidor para registrar al usuario manualmente
						new Navegador(false, ilogin, ipassword, request) {
							@Override
							protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
								List<Jugador> jugadores_actualizados = AsistenteBO.actualizar_equipo(usuario, jornada_actual, isIncrementar_edad(), true, navegadorXML, navegador);
								AsistenteDAO.obtener_datos_NT(navegador, usuario);
							}
						};
					
						request.getSession().setAttribute("usuario", getUsuario());
						mensaje[0] = "registered";
					} catch (Exception e) {
						mensaje[0] = "Error connecting to Sokker: " + e.toString();
						e.printStackTrace();

						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						SERVLET_ASISTENTE._log_linea("_EXCEPTIONS", "__TID: " + getUsuario().getDef_tid() + " -> " + sw.toString() + "\n");
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

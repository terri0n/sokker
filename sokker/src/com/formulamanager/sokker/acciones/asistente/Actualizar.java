package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.ParseException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Actualiza los jugadores del equipo
 */
@WebServlet("/asistente/actualizar")
public class Actualizar extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Actualizar() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje[] = {""};
		
		if (login(request)) {
			Usuario usuario = getUsuario(request);

			if (!Util.getBoolean(request, "confirmed") && usuario.getDef_tid() > NtdbBO.MAX_ID_SELECCION) {
				throw new RuntimeException("Login not confirmed");
			} else {
				String ilogin = usuario.getDef_tid() < NtdbBO.MAX_ID_SELECCION && !Util.getBoolean(request, "confirmed") ? SystemUtil.getVar(SystemUtil.LOGIN) : request.getParameter("ilogin");
				String ipassword = usuario.getDef_tid() < NtdbBO.MAX_ID_SELECCION && !Util.getBoolean(request, "confirmed") ? SystemUtil.getVar(SystemUtil.PASSWORD) : request.getParameter("ipassword");
	
				new Navegador(false, ilogin, ipassword, request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
						try {						
							int jornada_actual = obtener_jornada_json(navegador);
								
		_log(request, "");
								
							// Actualizo el login/password por si han cambiado
							if (usuario.getDef_tid() > NtdbBO.MAX_ID_SELECCION) {
								usuario.setLogin_sokker(ilogin);
								usuario.setPassword(Util.getMD5(ipassword));
								if (request.getParameter("actualizacion_automatica") != null) {
									usuario.setActualizacion_automatica(ipassword);
								} else {
									usuario.setActualizacion_automatica(null);
								}
							}

							AsistenteBO.actualizar_equipo(usuario, jornada_actual, isIncrementar_edad(), false, navegador, navegador);

							mensaje[0] = "?mensaje=updated";
						} catch (Exception e) {
							String error = "Error connecting to Sokker: " + e.toString();
							mensaje[0] = "?error=2&mensaje=" + URLEncoder.encode(error, "UTF-8");
							e.printStackTrace();

							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							SERVLET_ASISTENTE._log_linea("_EXCEPTIONS", "__TID: " + usuario.getDef_tid() + " -> " + sw.toString() + "\n");
						}
					}
				};
			}
		}

		response.sendRedirect(request.getContextPath() + "/asistente" + mensaje[0]);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

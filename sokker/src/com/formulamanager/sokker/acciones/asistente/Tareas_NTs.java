package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

@WebServlet("/asistente/tareas_NTs")
public class Tareas_NTs extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
   	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Tareas_NTs() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (admin(request)) {
			try {
				new Navegador(true, null) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						int jornada_actual = obtener_jornada(navegador);
		    			AsistenteBO.tareas_NTs(navegador, jornada_actual);
					}
				};
			} catch (FailingHttpStatusCodeException | IOException | LoginException | ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=Ok");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

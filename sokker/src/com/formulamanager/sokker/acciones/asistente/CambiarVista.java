package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.NtdbBO;

/**
 * Servlet implementation class CambiarVista
 */
@WebServlet("/asistente/cambiar_vista")
public class CambiarVista extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CambiarVista() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (login(request)) {
			// Guardamos los par�metros en la sesi�n. Asistente.java se encarga de leer los jugadores/juveniles
			if (getUsuario(request).getDef_tid() > NtdbBO.MAX_ID_SELECCION) {
				// Evitamos que la selecci�n vea mis juveniles :P
				request.getSession().setAttribute("juveniles", new Integer(1).equals(Util.getInteger(request, "juveniles")) ? 1 : null);
			}
			request.getSession().setAttribute("historico", new Integer(1).equals(Util.getInteger(request, "historico")) ? 1 : null);
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

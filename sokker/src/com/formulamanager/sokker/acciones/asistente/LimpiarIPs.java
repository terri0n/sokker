package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;

@WebServlet("/asistente/limpiar_ips")
public class LimpiarIPs extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
   	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LimpiarIPs() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (admin(request)) {
			synchronized (SERVLET_ASISTENTE.class) {
				Util.guardar_hashmap(new HashMap<String, String>(), "IPs");
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

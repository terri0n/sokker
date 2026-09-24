package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Endpoint antiguo de recepción NTDB. Se conserva deshabilitado para sobrescribir
 * cualquier clase compilada de versiones anteriores en despliegues incrementales.
 */
@WebServlet("/asistente/ntdb/send")
public class NTDB_send extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_GONE);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_GONE);
	}
}

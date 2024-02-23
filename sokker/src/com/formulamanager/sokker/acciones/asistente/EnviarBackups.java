package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.bo.AsistenteBO;

/**
 * Servlet implementation class EnviarBackups
 * 
 * Crea los backups y los envía por correo
 */
@WebServlet("/asistente/enviar_backups")
public class EnviarBackups extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EnviarBackups() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (admin(request)) {
			AsistenteBO.crear_backups();
			AsistenteBO.enviar_backups();
		}
		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=updated");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
}

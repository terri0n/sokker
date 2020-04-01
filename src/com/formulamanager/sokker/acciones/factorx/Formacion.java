package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.FactorxBO.FORMACIONES;

/**
 * Actualiza la formación premiada
 */
@WebServlet("/factorx/formacion")
public class Formacion extends SERVLET {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Formacion() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean senior = "1".equals(request.getParameter("senior"));
		
		if (login(request)) {
			FORMACIONES formacion = FORMACIONES.valueOf(request.getParameter("formacion"));
			FactorxBO.grabar_formacion(formacion, senior);
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Formacion%20actualizada&senior=" + (senior ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doPost(request, response);
	}
}

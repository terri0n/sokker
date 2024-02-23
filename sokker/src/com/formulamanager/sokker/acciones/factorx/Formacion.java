package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.FactorxBO.FORMACIONES;

/**
 * Actualiza la formaci�n premiada
 */
@WebServlet("/factorx/formacion")
public class Formacion extends SERVLET_FACTORX {
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
		SERVLET_FACTORX.TIPO_FACTORX tipo = getTipo(request);
		
		if (admin(request)) {
			FORMACIONES formacion = FORMACIONES.valueOf(request.getParameter("formacion"));
			FactorxBO.grabar_formacion(formacion, tipo);
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Formacion%20actualizada&tipo=" + tipo.name());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doPost(request, response);
	}
}

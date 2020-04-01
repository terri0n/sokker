package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx/actualizar_todos")
public class ActualizarTodos extends SERVLET {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarTodos() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean senior = "1".equals(request.getParameter("senior"));

		if (login(request)) {
			FactorxBO.actualizar_todos(senior);
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Actualizados&senior=" + (senior ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
	}
}

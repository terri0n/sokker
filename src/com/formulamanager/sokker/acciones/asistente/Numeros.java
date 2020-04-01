package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class Numeros
 */
@WebServlet("/asistente/numeros")
public class Numeros extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Numeros() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean juveniles = Util.getBoolean(request, "juveniles");
		boolean historico = Util.getBoolean(request, "historico");
		boolean numeros = Util.getBoolean(request, "numeros");
		
		if (login(request)) {
			Usuario usuario = getUsuario(request);
			usuario.setNumeros(numeros);
			UsuarioBO.grabar_usuario(usuario);
		}

		response.sendRedirect(request.getContextPath() + "/asistente?juveniles=" + (juveniles ? "1" : "") + "&historico=" + (historico ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}

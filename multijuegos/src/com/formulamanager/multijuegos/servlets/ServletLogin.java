package com.formulamanager.multijuegos.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.multijuegos.dao.JugadoresDao;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.util.Util;

@WebServlet("/login")
public class ServletLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public ServletLogin() {
        super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String nombre = request.getParameter("nombre");
		String contrasenya = request.getParameter("contrasenya");

		try {
	        //request.login(nombre, contrasenya);
			
			Jugador j = JugadoresDao.obtener(nombre, Util.getMD5(contrasenya), null);

			if (j == null) {
				System.out.println("Error en usuario o contraseña: " + nombre + ":" + contrasenya);
			} else {
				request.getSession().setAttribute("jugador", j);
				System.out.println("Loguea " + j);
				response.getWriter().write("true");
			}
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static boolean login(HttpServletRequest request, String nombre, String contrasenya) throws SQLException, ParseException {
		Jugador j = JugadoresDao.obtener(nombre, Util.getMD5(contrasenya), null);

		if (j == null) {
			System.out.println("Error en usuario o contraseña: " + nombre + ":" + contrasenya);
			return false;
		} else {
			request.getSession().setAttribute("jugador", j);
			System.out.println("Loguea " + j);
			return true;
		}
	}
}

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
import com.formulamanager.multijuegos.websockets.EndpointBase;

@WebServlet("/cambio_contrasenya/confirmar")
public class ServletCambioContrasenyaConfirmar extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ServletCambioContrasenyaConfirmar() {
        super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		synchronized (EndpointBase.jugadores_en_espera) {
			try {
				System.out.print(request.getQueryString());

				Jugador j = EndpointBase.jugadores_en_espera.get(request.getQueryString());
				if (j != null) {
					System.out.println(" es " + j.nombre);

					if (JugadoresDao.obtener(j.nombre, null, null) != null) {
						JugadoresDao.actualizar(j);
						EndpointBase.jugadores_en_espera.remove(request.getQueryString());
						response.sendRedirect(request.getContextPath() + "/chessgoal?nombre=" + j.nombre);
					} else {
						System.out.println(" repetido");
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Error: usuario no encontrado");
					}
				} else {
					System.out.println(" no encontrado");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Petici�n de cambio de contrase�a caducada");
				}
			} catch (SQLException | ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}

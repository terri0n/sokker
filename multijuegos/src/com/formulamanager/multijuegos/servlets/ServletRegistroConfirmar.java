package com.formulamanager.multijuegos.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.multijuegos.dao.JugadoresDao;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.websockets.EndpointBase;

@WebServlet("/registro/confirmar")
public class ServletRegistroConfirmar extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ServletRegistroConfirmar() {
        super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		synchronized (EndpointBase.jugadores_en_espera) {
			try {
				System.out.print(request.getQueryString());

				// Buscamos al jugador en la lista de espera
				Jugador j = EndpointBase.jugadores_en_espera.get(request.getQueryString());
				if (j != null) {
					System.out.println(" es " + j.nombre);

					// Comprobamos que no exista ya en la BD
					if (JugadoresDao.obtener(j.nombre, null, null) == null && JugadoresDao.obtener(j.nombre, null, j.email) == null) {
						// Borramos todas las peticiones que haya hecho con ese email
						for (String key : EndpointBase.jugadores_en_espera.keySet()) {
							if (EndpointBase.jugadores_en_espera.get(key).email.equals(j.email)) {
								EndpointBase.jugadores_en_espera.remove(key);
							}
						}
						
						// Insertamos al jugador en la BD
						j.fecha_alta = new Date();
						JugadoresDao.insertar(j);
						
						request.getSession().setAttribute("jugador", j);
						System.out.println("Se registra " + j);
						
						response.sendRedirect(request.getContextPath() + "/");
					} else {
						System.out.println(" repetido");
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Petición de registro ya aceptada");
					}
				} else {
					System.out.println(" no encontrado");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Petición de registro caducada. Vuelva a registrarse");
				}
			} catch (SQLException | ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}

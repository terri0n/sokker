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
import com.formulamanager.multijuegos.util.EmailSenderService;
import com.formulamanager.multijuegos.util.Util;
import com.formulamanager.multijuegos.websockets.EndpointBase;

import jakarta.mail.MessagingException;

@WebServlet("/registro")
public class ServletRegistro extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public final static String SUFIJO = "CG79";

	public ServletRegistro() {
        super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			System.out.print(request.getQueryString());

			String nombre = request.getParameter("nombre");
			String contrasenya = request.getParameter("contrasenya");
			String email = request.getParameter("email");
			String pais = request.getLocale().getCountry();

			if (!nombre.matches("[0-9a-zA-Z_]+") || nombre.startsWith("_Guest")) {
				System.out.println(nombre + ": nombnre inválido");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, EndpointBase.getTexto("error.wrongName", request.getLocale()));
			} else {
				Jugador j = JugadoresDao.obtener(nombre, null, null);
				if (j != null) {
					System.out.println(nombre + " ya existe en la BD");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, EndpointBase.getTexto("error.nameExists", request.getLocale()));
				} else {
					Jugador j2 = JugadoresDao.obtener(null, null, email);
					if (j2 != null) {
						System.out.println(email + " ya existe en la BD");
						response.sendError(HttpServletResponse.SC_FORBIDDEN, EndpointBase.getTexto("error.emailExists", request.getLocale()));
					} else {
						synchronized (EndpointBase.jugadores_en_espera) {
							if (EndpointBase.jugadores_en_espera.get(Util.getMD5(nombre + SUFIJO)) != null) {
								System.out.println(nombre + " ya existe en la lista de espera");
								response.sendError(HttpServletResponse.SC_FORBIDDEN, EndpointBase.getTexto("error.nameExists", request.getLocale()));
							} else {
								j = new Jugador(nombre, Util.getMD5(contrasenya), 1600, pais, 0, email, null, false);
								EndpointBase.jugadores_en_espera.put(Util.getMD5(nombre + SUFIJO), j);
								String url = "https://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/registro/confirmar?" + Util.getMD5(nombre + SUFIJO);
								EmailSenderService.sendEmail(email, "Solicitud de registro en ChessGoal", "¡Hola!<br/><br/>Has solicitado registrarte en ChessGoal con los siguientes datos:<br/><br/>Nombre: " + nombre + "<br/>Contraseña: " + contrasenya + "<br/><br/>Para confirmar tu registro pulsa en el siguiente enlace:<br/><br/><a href='" + url + "'>" + url + "</a>");
								response.getWriter().write(EndpointBase.getTexto("error.emailSent", request.getLocale()));
							}
						}
					}
				}
			}
		} catch (SQLException | IOException | MessagingException | ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

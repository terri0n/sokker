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
import com.formulamanager.multijuegos.util.EmailSenderService;
import com.formulamanager.multijuegos.util.Util;
import com.formulamanager.multijuegos.websockets.EndpointBase;
import com.formulamanager.multijuegos.websockets.Jugador;

import jakarta.mail.MessagingException;

@WebServlet("/cambio_contrasenya")
public class ServletCambioContrasenya extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ServletCambioContrasenya() {
        super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			System.out.print(request.getQueryString());

			String nombre_email = request.getParameter("nombre_email");
			String contrasenya = request.getParameter("contrasenya");
			
			Jugador j = JugadoresDao.obtener(nombre_email, null, null);
			if (j == null) {
				j = JugadoresDao.obtener(null, null, nombre_email);
			}
			
			if (j == null) {
				System.out.println(" no encontrado");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Nombre o e-mail no encontrados");
			} else {
				j.contrasenya = Util.getMD5(contrasenya);
				EndpointBase.jugadores_en_espera.put(Util.getMD5(j.nombre + ServletRegistro.SUFIJO), j);
				String url = "https://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/cambio_contrasenya/confirmar?" + Util.getMD5(j.nombre + ServletRegistro.SUFIJO);
				EmailSenderService.sendEmail(j.email, "Solicitud de cambio de contraseña en ChessGoal", "¡Hola!<br/><br/>Has solicitado cambiar la contraseña de tu cuenta en ChessGoal:<br/><br/>Nombre: " + j.nombre + "<br/>Nueva contraseña: " + contrasenya + "<br/><br/>Para confirmar el cambio pulsa en el siguiente enlace:<br/><br/><a href='" + url + "'>" + url + "</a>");
				response.getWriter().write(EndpointBase.getTexto("error.emailSent", request.getLocale()));
			}
		} catch (SQLException | ParseException | MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

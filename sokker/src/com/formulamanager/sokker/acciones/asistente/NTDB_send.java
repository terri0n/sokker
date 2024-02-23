package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Recibe las habilidades de un jugador
 */
@WebServlet("/asistente/ntdb/send")
public class NTDB_send extends HttpServlet {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NTDB_send() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer pid = Util.getInteger(request, "pid");
		Integer condicion = Util.getInteger(request, "sta");
		Integer rapidez = Util.getInteger(request, "pac");
		Integer tecnica = Util.getInteger(request, "tec");
		Integer pases = Util.getInteger(request, "pas");
		Integer porteria = Util.getInteger(request, "kee");
		Integer defensa = Util.getInteger(request, "def");
		Integer creacion = Util.getInteger(request, "pla");
		Integer anotacion = Util.getInteger(request, "str");
		String notas = Util.getString(request, "obs");

SERVLET_ASISTENTE._log_linea("_NTDB", pid + " " + condicion + " " + rapidez + " " + tecnica + " " + pases + " " + porteria + " " + defensa + " " + creacion + " " + anotacion + " " + notas);
		
		if (pid == null || condicion == null || rapidez == null || tecnica == null || pases == null || porteria == null || defensa == null || creacion == null || creacion == null || anotacion == null) {
			// Si se accede directamente a la página, redirijo a un jsp del formulario
			response.sendRedirect(request.getContextPath() + "/asistente/ntdb?mensaje=Error");
		} else {
			String[] mensaje = new String[1];
			
			try {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegadorXML) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
						int jornada_actual = obtener_jornada(navegadorXML);
						new Navegador(false, request) {
							@Override
							protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
								Jugador j = AsistenteDAO.obtener_jugador(pid, true, jornada_actual, isIncrementar_edad(), null/*usuario*/, navegador);
								if (j == null) {
									mensaje[0] = "Player with ID " + pid + " not found";
								} else {
									Integer id_pais = j.getPais() + (j.getEdad() > 21 ? 0 : NtdbBO.DIF_NT_U21);
									
									// Comprobamos si el usuario permite recibir jugadores desde fuera
									Usuario usuario = AsistenteBO.obtener_usuario_seleccion(id_pais);
									if (usuario != null && usuario.isRecibir_ntdb()) {
										if (j.getEn_venta() == 0) {
											j.setCondicion(condicion);
											j.setRapidez(rapidez);
											j.setTecnica(tecnica);
											j.setPases(pases);
											j.setPorteria(porteria);
											j.setDefensa(defensa);
											j.setCreacion(creacion);
											j.setAnotacion(anotacion);
											j.setActualizado(false);	// No fiable
											j.setFecha(new Date());
										}
		
										j.setNotas(notas);
		
										NtdbBO.actualizar_jugador_local(j, jornada_actual);
											
										mensaje[0] = "Updated";
									} else {
										mensaje[0] = "The NT of the player didn't enable the player reception through this interface";
										SERVLET_ASISTENTE._log_linea("_NTDB", mensaje[0] + ": " + pid + " " + id_pais + " " + (usuario == null ? null : usuario.getLogin()));
									}
								}
							}
						};
					}
				};
			} catch (Exception e) {
				e.printStackTrace();
				mensaje[0] = "There was an error connecting to Sokker: " + e;
				SERVLET_ASISTENTE._log_linea("_NTDB", mensaje[0]);
			}
			request.getRequestDispatcher("/jsp/asistente/seleccion.jsp?mensaje=" + mensaje[0]).forward(request, response);
			//response.sendRedirect(request.getContextPath() + "/asistente/ntdb?mensaje=" + mensaje[0]);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect(request.getContextPath() + "/asistente/ntdb");
	}
}

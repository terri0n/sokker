package com.formulamanager.sokker.acciones.asistente;

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
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Actualiza los niveles de un juvenil
 */
@WebServlet("/asistente/actualizar_juvenil")
public class ActualizarJuvenil extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarJuvenil() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		if (login(request)) {
			Usuario usuario = getUsuario(request);
			
			// Juvenil
			int pid = Util.getInteger(request, "pid_juvenil");
			String niveles = Util.getString(request, "niveles_juvenil");

_log(request, pid + " " + niveles);

			List<Juvenil> juveniles = AsistenteBO.leer_juveniles(usuario.getTid(), false, usuario);
			Juvenil j = new Juvenil(pid);
			
			try {
				if (juveniles.contains(j)) {
					Juvenil juvenil = juveniles.get(juveniles.indexOf(j));
					List<Integer> lista = Util.splitIntNull(niveles, ",");
					Collections.reverse(lista);
					Juvenil it = juvenil;
					Juvenil anterior = null;

					for (int i = 0; i < lista.size(); i++) {
						if (anterior != null && (it == null || anterior.getJornada() - it.getJornada() > 1)) {
							// Creamos un juvenil nuevo
							Juvenil auxiliar = new Juvenil(pid, juvenil.getNombre(), anterior.getJornada() - 1, anterior.getEdad(), lista.get(i) == null ? -100 : -Math.abs(lista.get(i)), anterior.getSemanas(), anterior.isJugador_campo(), usuario);
							auxiliar.setOriginal(it);
							anterior.setOriginal(auxiliar);
							it = auxiliar;
						} else if (it.getNivel() <= 0) {
							// Solo actualizo los valores negativos
							it.setNivel(lista.get(i) == null ? -100 : -Math.abs(lista.get(i)));
						} else {
							// No hago nada
						}

						anterior = it;
						it = (Juvenil)it.getOriginal();
					}

					// Borramos los que se hayan quedado sin valor
					it = juvenil;
					anterior = null;
					while (it != null) {
						if (it.getNivel() == -100 && anterior != null) {
							anterior.setOriginal(it.getOriginal());
						} else {
							anterior = it;
						}
						it = (Juvenil)it.getOriginal();
					}
					
					Collections.sort(juveniles);
					
					new Navegador(true, request) {
						@Override
						protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
							int jornada_actual = obtener_jornada(navegador);
							AsistenteBO.grabar_juveniles(juveniles, usuario.getTid(), jornada_actual, false);
						}
					};
					
					AsistenteBO.calcular_juveniles(juveniles);
					request.setAttribute("jugadores", juveniles);
				}
	
				mensaje = "updated";
			} catch (Exception e) {
				e.printStackTrace();
				mensaje = "error";
			}
		}

		response.sendRedirect(request.getContextPath() + "/asistente?juveniles=1&mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

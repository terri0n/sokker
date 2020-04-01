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
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Actualiza el entrenamiento de un jugador y una jornada
 */
@WebServlet("/asistente/actualizar_entrenamiento")
public class ActualizarEntrenamiento extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarEntrenamiento() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		if (login(request)) {
			Usuario usuario = getUsuario(request);

			// Jugador
			int pid = Util.getInteger(request, "pid_entrenamiento");
			int nivel = Util.getInteger(request, "nivel_entrenamiento");
			TIPO_ENTRENAMIENTO habilidad = TIPO_ENTRENAMIENTO.values()[Util.getInteger(request, "habilidad_entrenamiento")];
			int jornada = Util.getInteger(request, "jornada_entrenamiento");
			int puntos_entrenamiento = Util.getInteger(request, "puntos_entrenamiento");
			int lesion = Util.getInteger(request, "lesion");
			DEMARCACION demarcacion_entrenamiento = DEMARCACION.valueOf(Util.getString(request, "demarcacion_entrenamiento"));

			// Equipo
			TIPO_ENTRENAMIENTO tipo_entrenamiento = TIPO_ENTRENAMIENTO.valueOf(Util.getString(request, "tipo_entrenamiento"));
			DEMARCACION demarcacion = DEMARCACION.valueOf(Util.getString(request, "demarcacion_equipo"));
			
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), false);
			Jugador j = new Jugador();
			j.setPid(pid);
			
			if (jugadores.contains(j)) {
				Jugador jugador = jugadores.get(jugadores.indexOf(j)).buscar_jornada(jornada);
				if (jugador != null) {
					jugador.setMinutos(puntos_entrenamiento);
					jugador.setLesion(lesion);
					jugador.setDemarcacion_entrenamiento(demarcacion_entrenamiento);
					jugador.setValor_habilidad(nivel, habilidad);
					
					Collections.sort(jugadores, Jugador.getComparator());
					AsistenteBO.grabar_jugadores(jugadores, usuario.getDef_tid(), false);
					request.getSession().setAttribute("jugadores", jugadores);

					usuario.getTipo_entrenamiento().put(jornada, tipo_entrenamiento);
					usuario.getDemarcacion().put(jornada, demarcacion);
					UsuarioBO.grabar_usuario(usuario);
				}
			}

			AsistenteBO.calcular(jugadores, usuario);				

			mensaje = "updated";
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}

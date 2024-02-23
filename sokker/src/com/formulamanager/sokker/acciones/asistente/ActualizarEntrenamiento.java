package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

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
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

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
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje = "";
		
		if (login(request)) {
			Usuario usuario = getUsuario(request);
			final int def_tid = usuario.getDef_tid();
			Integer semanas = Util.getInteger(request, "semanas");
			
			// Jugador
			int pid = Util.getInteger(request, "pid_entrenamiento");
			int nivel = Util.getInteger(request, "nivel_entrenamiento");
			TIPO_ENTRENAMIENTO habilidad = TIPO_ENTRENAMIENTO.values()[Util.getInteger(request, "habilidad_entrenamiento")];
			int jornada = Util.getInteger(request, "jornada_entrenamiento");
			float puntos_entrenamiento = Util.getFloat(request, "puntos_entrenamiento");
			boolean avanzado = Util.getBoolean(request, "avanzado");
			Integer lesion = Util.getInteger(request, "lesion");
			DEMARCACION demarcacion_entrenamiento = Util.getString(request, "demarcacion_entrenamiento") == null ? null : DEMARCACION.valueOf(Util.getString(request, "demarcacion_entrenamiento"));

			// Equipo
			TIPO_ENTRENAMIENTO tipo_entrenamiento = Util.getString(request, "tipo_entrenamiento") == null ? null : TIPO_ENTRENAMIENTO.valueOf(Util.getString(request, "tipo_entrenamiento"));
			DEMARCACION demarcacion = Util.getString(request, "demarcacion_equipo") == null ? null : DEMARCACION.valueOf(Util.getString(request, "demarcacion_equipo"));
			List<Integer> tr_entrenador = Util.splitInt(request.getParameter("tr_entrenador"), ",");
			BigDecimal tr_asistentes = Util.getBigDecimal(request, "tr_asistentes");
			BigDecimal tr_juveniles = Util.getBigDecimal(request, "tr_juveniles");
			TIPO_ENTRENAMIENTO tipo_entrenamiento_gk = Util.getString(request, "tipo_entrenamiento_gk") == null ? null : TIPO_ENTRENAMIENTO.valueOf(Util.getString(request, "tipo_entrenamiento_gk"));
			TIPO_ENTRENAMIENTO tipo_entrenamiento_def = Util.getString(request, "tipo_entrenamiento_def") == null ? null : TIPO_ENTRENAMIENTO.valueOf(Util.getString(request, "tipo_entrenamiento_def"));
			TIPO_ENTRENAMIENTO tipo_entrenamiento_mid = Util.getString(request, "tipo_entrenamiento_mid") == null ? null : TIPO_ENTRENAMIENTO.valueOf(Util.getString(request, "tipo_entrenamiento_mid"));
			TIPO_ENTRENAMIENTO tipo_entrenamiento_att = Util.getString(request, "tipo_entrenamiento_att") == null ? null : TIPO_ENTRENAMIENTO.valueOf(Util.getString(request, "tipo_entrenamiento_att"));
			
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), usuario.getDef_equipo(), false, usuario);
			Jugador j = new Jugador();
			j.setPid(pid);

_log(request, pid + " " + nivel + " " + habilidad + " " + jornada + " " + puntos_entrenamiento + " " + avanzado + " " + lesion + " " + demarcacion_entrenamiento + " " + tipo_entrenamiento + " " + demarcacion + " " + semanas + " " + tr_entrenador + " " + tr_asistentes + " " + tr_juveniles + " " + tipo_entrenamiento_gk + " " + tipo_entrenamiento_def + " " + tipo_entrenamiento_mid + " " + tipo_entrenamiento_att);
			
			if (jugadores.contains(j)) {
				j = new Jugador();
				j.setPid(pid);
				Jugador original = jugadores.get(jugadores.indexOf(j));
				Jugador jugador = original.buscar_jornada(jornada);

				if (jugador != null) {
					// Actualizar
					jugador.setMinutos(puntos_entrenamiento);
					jugador.setEntrenamiento_avanzado(avanzado);
					jugador.setLesion(lesion);
					jugador.setDemarcacion_entrenamiento(demarcacion_entrenamiento);
					jugador.setValor_habilidad(nivel, habilidad);
				} else {
					// Añadir
					for (int i = 0; i < semanas; i++) {
						j = new Jugador();
						j.setPid(pid);
						
						original.insertar_inicio(j);
						j.setMinutos(puntos_entrenamiento);
						j.setEntrenamiento_avanzado(avanzado);
						j.setLesion(lesion);
						j.setDemarcacion_entrenamiento(demarcacion_entrenamiento);
						j.setValor_habilidad(nivel, habilidad);
						j.setUsuario(usuario);
						j.setUsuario2(original.getUsuario2());	// Para el jugador de prueba o usuario del traspaso anterior
					}
					
					if (pid <= 0) {
						usuario = original.getUsuario2();
					}

					// Copiamos los entrenadores de la jornada siguiente
					usuario.getEntrenador_principal().put(jornada, usuario.getEntrenador_principal().get(jornada + 1));
					usuario.getNivel_asistentes().put(jornada, usuario.getNivel_asistentes().get(jornada + 1));
					usuario.getNivel_juveniles().put(jornada, usuario.getNivel_juveniles().get(jornada + 1));
				}

				// Es necesario volverlo a comprobar aquí porque si actualizas el jugador tb hay que cambiar el usuario por el usuario2
				if (pid <= 0) {
					usuario = original.getUsuario2();
				}

				Collections.sort(jugadores, Jugador.getComparator());
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						int jornada_actual = obtener_jornada(navegador);
						AsistenteBO.grabar_jugadores(jugadores, def_tid, jornada_actual, false);
					}
				};

				for (int i = 0; i < (semanas == null ? 1 : semanas); i++) {
					// Si estamos editando un entrenamiento o si estamos añadiéndolos y la jornada es nueva, establecemos el tipo de entrenamiento
					if (jugador != null || usuario.getTipo_entrenamiento(0).get(jornada - i) == null) {
						if (jornada < AsistenteBO.JORNADA_NUEVO_ENTRENO) {
							usuario.getTipo_entrenamiento(0).put(jornada - i, tipo_entrenamiento);
						} else {
							usuario.getTipo_entrenamiento(0).put(jornada - i, tipo_entrenamiento_gk);
							usuario.getTipo_entrenamiento(1).put(jornada - i, tipo_entrenamiento_def);
							usuario.getTipo_entrenamiento(2).put(jornada - i, tipo_entrenamiento_mid);
							usuario.getTipo_entrenamiento(3).put(jornada - i, tipo_entrenamiento_att);
						}

						usuario.getDemarcacion().put(jornada - i, demarcacion);
						usuario.getEntrenador_principal().put(jornada - i, new Jugador(tr_entrenador.toArray(new Integer[tr_entrenador.size()])));
						usuario.getNivel_asistentes().put(jornada - i, tr_asistentes);
						usuario.getNivel_juveniles().put(jornada - i, tr_juveniles);
						UsuarioBO.grabar_usuario(usuario);
					}
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

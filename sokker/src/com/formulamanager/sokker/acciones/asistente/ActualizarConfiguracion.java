package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class ActualizarConfiguracion
 */
@WebServlet("/asistente/actualizar_configuracion")
public class ActualizarConfiguracion extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarConfiguracion() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean juveniles = Util.getBoolean(request, "juveniles");
		boolean historico = Util.getBoolean(request, "historico");
		
		boolean numeros = Util.getBoolean(request, "numeros");
		Float factor_edad = Util.getFloat(request, "factor_edad");
		Float factor_edad_rapidez = Util.getFloat(request, "factor_edad_rapidez");
		Float factor_habilidad = Util.getFloat(request, "factor_habilidad");
		Float factor_talento = Util.getFloat(request, "factor_talento");
		Float factor_residual = Util.getFloat(request, "factor_residual");
		Float factor_formacion = Util.getFloat(request, "factor_formacion");
		Float factor_rapidez = Util.getFloat(request, "factor_rapidez");
		Float factor_tecnica = Util.getFloat(request, "factor_tecnica");
		Float factor_pases = Util.getFloat(request, "factor_pases");
		Float factor_porteria = Util.getFloat(request, "factor_porteria");
		Float factor_defensa = Util.getFloat(request, "factor_defensa");
		Float factor_creacion = Util.getFloat(request, "factor_creacion");
		Float factor_anotacion = Util.getFloat(request, "factor_anotacion");
		
		boolean mostrar_salario = Util.getBoolean(request, "mostrar_salario");
		boolean mostrar_experiencia = Util.getBoolean(request, "mostrar_experiencia");
		boolean mostrar_disciplina_tactica = Util.getBoolean(request, "mostrar_disciplina_tactica");
		boolean mostrar_trabajo_en_equipo = Util.getBoolean(request, "mostrar_trabajo_en_equipo");
		boolean mostrar_altura = Util.getBoolean(request, "mostrar_altura");
		boolean mostrar_peso = Util.getBoolean(request, "mostrar_peso");
		boolean mostrar_IMC = Util.getBoolean(request, "mostrar_IMC");
		boolean mostrar_banquillo = Util.getBoolean(request, "mostrar_banquillo");
		boolean mostrar_suma_habilidades = Util.getBoolean(request, "mostrar_suma_habilidades");
		boolean ntdb = Util.getBoolean(request, "ntdb");
		boolean recibir_ntdb = Util.getBoolean(request, "recibir_ntdb");
		
		Integer sumskills_gk_rapidez = Util.getInteger(request, "sumskills_gk_rapidez");
		Integer sumskills_def_rapidez = Util.getInteger(request, "sumskills_def_rapidez");
		Integer sumskills_mid_rapidez = Util.getInteger(request, "sumskills_mid_rapidez");
		Integer sumskills_att_rapidez = Util.getInteger(request, "sumskills_att_rapidez");
		Integer sumskills_gk_tecnica = Util.getInteger(request, "sumskills_gk_tecnica");
		Integer sumskills_def_tecnica = Util.getInteger(request, "sumskills_def_tecnica");
		Integer sumskills_mid_tecnica = Util.getInteger(request, "sumskills_mid_tecnica");
		Integer sumskills_att_tecnica = Util.getInteger(request, "sumskills_att_tecnica");
		Integer sumskills_gk_pases = Util.getInteger(request, "sumskills_gk_pases");
		Integer sumskills_def_pases = Util.getInteger(request, "sumskills_def_pases");
		Integer sumskills_mid_pases = Util.getInteger(request, "sumskills_mid_pases");
		Integer sumskills_att_pases = Util.getInteger(request, "sumskills_att_pases");
		Integer sumskills_gk_porteria = Util.getInteger(request, "sumskills_gk_porteria");
		Integer sumskills_def_porteria = Util.getInteger(request, "sumskills_def_porteria");
		Integer sumskills_mid_porteria = Util.getInteger(request, "sumskills_mid_porteria");
		Integer sumskills_att_porteria = Util.getInteger(request, "sumskills_att_porteria");
		Integer sumskills_gk_defensa = Util.getInteger(request, "sumskills_gk_defensa");
		Integer sumskills_def_defensa = Util.getInteger(request, "sumskills_def_defensa");
		Integer sumskills_mid_defensa = Util.getInteger(request, "sumskills_mid_defensa");
		Integer sumskills_att_defensa = Util.getInteger(request, "sumskills_att_defensa");
		Integer sumskills_gk_creacion = Util.getInteger(request, "sumskills_gk_creacion");
		Integer sumskills_def_creacion = Util.getInteger(request, "sumskills_def_creacion");
		Integer sumskills_mid_creacion = Util.getInteger(request, "sumskills_mid_creacion");
		Integer sumskills_att_creacion = Util.getInteger(request, "sumskills_att_creacion");
		Integer sumskills_gk_anotacion = Util.getInteger(request, "sumskills_gk_anotacion");
		Integer sumskills_def_anotacion = Util.getInteger(request, "sumskills_def_anotacion");
		Integer sumskills_mid_anotacion = Util.getInteger(request, "sumskills_mid_anotacion");
		Integer sumskills_att_anotacion = Util.getInteger(request, "sumskills_att_anotacion");
		
		String scouts = Util.getString(request, "scouts");
		if (scouts != null) {
			scouts = scouts.toLowerCase();
		}
		
		if (login(request)) {

_log(request, numeros+","+factor_edad+","+factor_edad_rapidez+","+factor_habilidad+","+factor_talento+","+factor_residual+","+factor_formacion+","+factor_rapidez+","+factor_tecnica+","+factor_pases+","+factor_porteria+","+factor_defensa+","+factor_creacion+","+factor_anotacion+","+mostrar_salario+","+mostrar_experiencia+","+mostrar_disciplina_tactica+","+mostrar_trabajo_en_equipo+","+mostrar_altura+","+mostrar_peso+","+mostrar_IMC+","+mostrar_banquillo+","+mostrar_suma_habilidades+","+ntdb+","+recibir_ntdb+",["+scouts+"]");
			
			Usuario usuario = getUsuario(request);
			usuario.inicializar();

			usuario.setNumeros(numeros);
			if (factor_edad != null) usuario.setFactor_edad(factor_edad);
			if (factor_edad_rapidez != null) usuario.setFactor_edad_rapidez(factor_edad_rapidez);
			if (factor_habilidad != null) usuario.setFactor_habilidad(factor_habilidad);
			if (factor_talento != null) usuario.setFactor_talento(factor_talento);
			if (factor_residual != null) usuario.setFactor_residual(factor_residual);
			if (factor_formacion != null) usuario.setFactor_formacion(factor_formacion);
			if (factor_rapidez != null) usuario.setFactor_rapidez(factor_rapidez);
			if (factor_tecnica != null) usuario.setFactor_tecnica(factor_tecnica);
			if (factor_pases != null) usuario.setFactor_pases(factor_pases);
			if (factor_porteria != null) usuario.setFactor_porteria(factor_porteria);
			if (factor_defensa != null) usuario.setFactor_defensa(factor_defensa);
			if (factor_creacion != null) usuario.setFactor_creacion(factor_creacion);
			if (factor_anotacion != null) usuario.setFactor_anotacion(factor_anotacion);
						
			usuario.setMostrar_salario(mostrar_salario);
			usuario.setMostrar_experiencia(mostrar_experiencia);
			usuario.setMostrar_disciplina_tactica(mostrar_disciplina_tactica);
			usuario.setMostrar_trabajo_en_equipo(mostrar_trabajo_en_equipo);
			usuario.setMostrar_altura(mostrar_altura);
			usuario.setMostrar_peso(mostrar_peso);
			usuario.setMostrar_IMC(mostrar_IMC);
			usuario.setMostrar_banquillo(mostrar_banquillo);
			usuario.setMostrar_suma_habilidades(mostrar_suma_habilidades);

			if (sumskills_gk_rapidez != null) usuario.setSumskills_gk_rapidez(sumskills_gk_rapidez);
			if (sumskills_def_rapidez != null) usuario.setSumskills_def_rapidez(sumskills_def_rapidez);
			if (sumskills_mid_rapidez != null) usuario.setSumskills_mid_rapidez(sumskills_mid_rapidez);
			if (sumskills_att_rapidez != null) usuario.setSumskills_att_rapidez(sumskills_att_rapidez);
			if (sumskills_gk_tecnica != null) usuario.setSumskills_gk_tecnica(sumskills_gk_tecnica);
			if (sumskills_def_tecnica != null) usuario.setSumskills_def_tecnica(sumskills_def_tecnica);
			if (sumskills_mid_tecnica != null) usuario.setSumskills_mid_tecnica(sumskills_mid_tecnica);
			if (sumskills_att_tecnica != null) usuario.setSumskills_att_tecnica(sumskills_att_tecnica);
			if (sumskills_gk_pases != null) usuario.setSumskills_gk_pases(sumskills_gk_pases);
			if (sumskills_def_pases != null) usuario.setSumskills_def_pases(sumskills_def_pases);
			if (sumskills_mid_pases != null) usuario.setSumskills_mid_pases(sumskills_mid_pases);
			if (sumskills_att_pases != null) usuario.setSumskills_att_pases(sumskills_att_pases);
			if (sumskills_gk_porteria != null) usuario.setSumskills_gk_porteria(sumskills_gk_porteria);
			if (sumskills_def_porteria != null) usuario.setSumskills_def_porteria(sumskills_def_porteria);
			if (sumskills_mid_porteria != null) usuario.setSumskills_mid_porteria(sumskills_mid_porteria);
			if (sumskills_att_porteria != null) usuario.setSumskills_att_porteria(sumskills_att_porteria);
			if (sumskills_gk_defensa != null) usuario.setSumskills_gk_defensa(sumskills_gk_defensa);
			if (sumskills_def_defensa != null) usuario.setSumskills_def_defensa(sumskills_def_defensa);
			if (sumskills_mid_defensa != null) usuario.setSumskills_mid_defensa(sumskills_mid_defensa);
			if (sumskills_att_defensa != null) usuario.setSumskills_att_defensa(sumskills_att_defensa);
			if (sumskills_gk_creacion != null) usuario.setSumskills_gk_creacion(sumskills_gk_creacion);
			if (sumskills_def_creacion != null) usuario.setSumskills_def_creacion(sumskills_def_creacion);
			if (sumskills_mid_creacion != null) usuario.setSumskills_mid_creacion(sumskills_mid_creacion);
			if (sumskills_att_creacion != null) usuario.setSumskills_att_creacion(sumskills_att_creacion);
			if (sumskills_gk_anotacion != null) usuario.setSumskills_gk_anotacion(sumskills_gk_anotacion);
			if (sumskills_def_anotacion != null) usuario.setSumskills_def_anotacion(sumskills_def_anotacion);
			if (sumskills_mid_anotacion != null) usuario.setSumskills_mid_anotacion(sumskills_mid_anotacion);
			if (sumskills_att_anotacion != null) usuario.setSumskills_att_anotacion(sumskills_att_anotacion);
			
			if (usuario.getDef_tid() > NtdbBO.MAX_ID_SELECCION) {
				usuario.setNtdb(ntdb);
			} else {
				usuario.setRecibir_ntdb(recibir_ntdb);
				usuario.setScouts(scouts);
			}
			
			UsuarioBO.grabar_usuario(usuario);

			// Guardamos los scouts en el archivo de scouts
			synchronized (NtdbBO.class) {
				HashMap<String, String> map = Util.leer_hashmap("NTDB");
				map.put(usuario.getLogin(), scouts);
				Util.guardar_hashmap(map, "NTDB");
			}
		}
		
		String mensaje = "configuration_updated";
		response.sendRedirect(request.getContextPath() + "/asistente?juveniles=" + (juveniles ? "1" : "") + "&historico=" + (historico ? "1" : "") + "&mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}

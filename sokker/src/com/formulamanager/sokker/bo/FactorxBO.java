package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX;
import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

public class FactorxBO {
	public static String FACTORX_PROPERTIES = JugadorBO.PATH_BASE + "factorx/factorx.properties";

	public static enum FORMACIONES { _541("5-4-1"), _532("5-3-2"), _523("5-2-3"), _442("4-4-2"), _451("4-5-1"), _433("4-3-3"), _352("3-5-2"), _343("3-4-3"), _253("2-5-3");
		private String texto;
		private FORMACIONES(String texto) {
			this.texto = texto;
		}
		@Override
		public String toString() {
			return texto;
		}
	}

	public static void grabar_formacion(FORMACIONES formacion, TIPO_FACTORX tipo) throws IOException {
		Properties prop = new Properties();
		
		prop.setProperty("formacion", formacion.name());
		
		String ruta = FACTORX_PROPERTIES + tipo.getSufijo();
		Util.guardar_properties(prop, ruta);
	}

	public static FORMACIONES leer_formacion(SERVLET_FACTORX.TIPO_FACTORX tipo) {
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			File file = new File(FACTORX_PROPERTIES + tipo.getSufijo());
			if (!file.exists()) {
				return FORMACIONES._442;
			}
			
			input = new FileInputStream(file);
	
			// load a properties file
			prop.load(input);
			
			return FORMACIONES.valueOf(prop.getProperty("formacion"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void actualizar_todos(TIPO_FACTORX tipo, WebClient navegador, int jornada_actual) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		List<Jugador> jugadores = JugadorBO.leer_jugadores(tipo, getEdicion(jornada_actual), true);
		FactorxBO.actualizar_jugadores(navegador, jugadores, jornada_actual);
		Collections.sort(jugadores);
		FactorxBO.grabar_jugadores(jugadores, tipo, jornada_actual, false);

		for (Jugador j : jugadores) {
			JugadorBO.escribir_anuncio(navegador, j, false);
		}
	}

	public static int getEdicion(int jornada) {
		return jornada + 1 < 976 ? (jornada + 1) / 16 - 25 : (jornada + 1 - 976) / 13 + 36;
	}

	public static Jugador buscar(WebClient navegador, Integer pid, boolean forzar, int jornada_actual) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Jugador nuevo = new Jugador();
		nuevo.setPid(pid);
		
		if (JugadorBO.actualizar_jugador(navegador, nuevo, forzar)) {
			nuevo.setJornada(jornada_actual);
			nuevo.setOriginal(new Jugador(nuevo));
			nuevo.setActualizado(true);
			return nuevo;
		} else {
			return null;
		}
	}

	public static void grabar_jugadores(List<Jugador> jugadores, TIPO_FACTORX tipo, int jornada_actual, boolean historico) throws IOException {
		Properties prop = new Properties();
		
		int i = 0;
		for (Jugador j : jugadores) {
			prop.setProperty(i + "_nombre", j.getNombre());
			prop.setProperty(i + "_pid", j.getPid()+"");
			prop.setProperty(i + "_equipo", j.getEquipo());
			prop.setProperty(i + "_tid", j.getTid()+"");
			prop.setProperty(i + "_edad", j.getEdad()+"");
			prop.setProperty(i + "_valor", j.getValor()+"");
			prop.setProperty(i + "_jornada", j.getJornada()+"");
			
			prop.setProperty(i + "_demarcacion", j.getDemarcacion().name());
			
			prop.setProperty(i + "_condicion", j.getCondicion()+"");
			prop.setProperty(i + "_rapidez", j.getRapidez()+"");
			prop.setProperty(i + "_tecnica", j.getTecnica()+"");
			prop.setProperty(i + "_pases", j.getPases()+"");
			prop.setProperty(i + "_porteria", j.getPorteria()+"");
			prop.setProperty(i + "_defensa", j.getDefensa()+"");
			prop.setProperty(i + "_creacion", j.getCreacion()+"");
			prop.setProperty(i + "_anotacion", j.getAnotacion()+"");
	
			prop.setProperty(i + "_condicion_orig", j.getOriginal().getCondicion()+"");
			prop.setProperty(i + "_rapidez_orig", j.getOriginal().getRapidez()+"");
			prop.setProperty(i + "_tecnica_orig", j.getOriginal().getTecnica()+"");
			prop.setProperty(i + "_pases_orig", j.getOriginal().getPases()+"");
			prop.setProperty(i + "_porteria_orig", j.getOriginal().getPorteria()+"");
			prop.setProperty(i + "_defensa_orig", j.getOriginal().getDefensa()+"");
			prop.setProperty(i + "_creacion_orig", j.getOriginal().getCreacion()+"");
			prop.setProperty(i + "_anotacion_orig", j.getOriginal().getAnotacion()+"");
	
			prop.setProperty(i + "_valor_orig", j.getOriginal().getValor() == null ? null : (j.getOriginal().getValor() + ""));
			prop.setProperty(i + "_jornada_orig", j.getOriginal().getJornada()+"");
			
			i++;
		}
	
		if (historico) {
			// Restamos uno porque getEdicion() suma 1 a la jornada. As� que estaremos en la edici�n siguiente aunque estemos en la jornada 15
			Integer edicion = getEdicion(jornada_actual) -1;
			String ruta = JugadorBO.HISTORICO_PROPERTIES + edicion + tipo.getSufijo();
			Util.guardar_properties(prop, ruta);
		} else {
			String ruta = JugadorBO.CONFIG_PROPERTIES + tipo.getSufijo();
			Util.guardar_properties(prop, ruta);
		}
		
		// BACKUP
		if (AsistenteBO.getJornadaMod(jornada_actual) == 0 || historico) {
			String ruta = JugadorBO.CONFIG_PROPERTIES + jornada_actual + tipo.getSufijo();
			Util.guardar_properties(prop, ruta);
		}
	}

	public static void actualizar_jugadores(WebClient navegador, List<Jugador> jugadores, int jornada_actual) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Iterator<Jugador> it = jugadores.iterator();
		while (it.hasNext()) {
			Jugador j = it.next();
			if (JugadorBO.actualizar_jugador(navegador, j, false)) {
				j.setActualizado(j.getJornada() == null || !j.getJornada().equals(jornada_actual));
				j.setJornada(jornada_actual);
			}
			if (j.getEquipo() == null) {
				// Jugador despedido
				it.remove();
			}
		}
	}
}

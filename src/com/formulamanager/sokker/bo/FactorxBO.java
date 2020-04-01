package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.Navegador;
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

	public static void grabar_formacion(FORMACIONES formacion, boolean senior) throws IOException {
		Properties prop = new Properties();
		
		prop.setProperty("formacion", formacion.name());
		
		String ruta = FACTORX_PROPERTIES + (senior ? JugadorBO.SUFIJO_SENIOR : "");
		Util.guardar_properties(prop, ruta);
	}

	public static FORMACIONES leer_formacion(boolean senior) {
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			File file = new File(FACTORX_PROPERTIES + (senior ? JugadorBO.SUFIJO_SENIOR : ""));
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
	
	public static void actualizar_todos(boolean senior) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		new Navegador() {
			@Override
			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
				List<Jugador> jugadores = JugadorBO.leer_jugadores(senior, null);
				JugadorBO.actualizar_jugadores(navegador, jugadores);
				Collections.sort(jugadores);
				JugadorBO.grabar_jugadores(jugadores, senior, false);

				for (Jugador j : jugadores) {
					JugadorBO.escribir_anuncio(navegador, j, false);
				}
			}
		};
	}
}

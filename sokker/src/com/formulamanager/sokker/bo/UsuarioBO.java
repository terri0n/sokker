package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;

public class UsuarioBO {
	public static Usuario leer_usuario (String login, boolean leer_scouts) {
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD = SystemUtil.getVar("path") + (login.startsWith("prueba/") ? "" : "_") + login.toLowerCase() + ".properties";
			
			File file = new File(BD);
			if (!file.exists()) {
				return null;
			}
			
			input = new FileInputStream(file);
	
			// load a properties file
			prop.load(input);
			String linea = prop.getProperty("usuario");
			Usuario usuario = new Usuario(Arrays.asList(linea.split(",")));
			usuario.setNotas(prop.getProperty("notas", ""));
			
			// ENTRENAMIENTO

			for (Entry<Object, Object> e : prop.entrySet()) {
				String key = e.getKey().toString();
				if (key.startsWith("entrenamiento")) {
					int jornada = Integer.valueOf(key.split("entrenamiento")[1]);
					String[] split = e.getValue().toString().split(",");
					
					if (!split[0].contains("-")) {
						// Sistema de entrenamiento antiguo
						if (Util.nnvl(split[0]) != null) {
							usuario.getTipo_entrenamiento(0).put(jornada, TIPO_ENTRENAMIENTO.valueOf(split[0]));
						}
						if (Util.nnvl(split[1]) != null) {
							usuario.getDemarcacion().put(jornada, DEMARCACION.valueOf(split[1]));
						}
					} else {
						// Sistema de entrenamiento nuevo
						// NOTA: si los últimos entrenamientos no están definidos, el array creado por split tiene menos elementos. Añado un elemento extra para evitarlo
						String[] tipo_entrenamiento = (split[0] + "-*").split("-");
						if (Util.nnvl(tipo_entrenamiento[0]) != null) {
							usuario.getTipo_entrenamiento(0).put(jornada, TIPO_ENTRENAMIENTO.valueOf(tipo_entrenamiento[0]));
						}
						if (Util.nnvl(tipo_entrenamiento[1]) != null) {
							usuario.getTipo_entrenamiento(1).put(jornada, TIPO_ENTRENAMIENTO.valueOf(tipo_entrenamiento[1]));
						}
						if (Util.nnvl(tipo_entrenamiento[2]) != null) {
							usuario.getTipo_entrenamiento(2).put(jornada, TIPO_ENTRENAMIENTO.valueOf(tipo_entrenamiento[2]));
						}
						if (Util.nnvl(tipo_entrenamiento[3]) != null) {
							usuario.getTipo_entrenamiento(3).put(jornada, TIPO_ENTRENAMIENTO.valueOf(tipo_entrenamiento[3]));
						}
					}

					if (split.length > 2 && split[2].length() > 0) {
						usuario.getEntrenador_principal().put(jornada, new Jugador(split[2]));
						usuario.getNivel_asistentes().put(jornada, Util.stringToBigDecimal(split[3]));
						if (split.length > 4) {
							usuario.getNivel_juveniles().put(jornada, Util.stringToBigDecimal(split[4]));
						}
					}
				}
			}

			// Leemos los scouts del archivo de scouts
			if (leer_scouts) {
				HashMap<String, String> map = Util.leer_hashmap("NTDB");
				usuario.setScouts(map.get(usuario.getLogin()));
				
				for (Entry<String, String> entry : map.entrySet()) {
					String scouts = "," + entry.getValue() + ",";
					if (scouts.contains("," + usuario.getLogin() + ",")) {
						Usuario u = UsuarioBO.leer_usuario(entry.getKey(), false);
						if (u != null && u.getTid_nt() != null) {
							usuario.getScout_de().put(entry.getKey(), u);
						}
					}
				}
			}
			
			return usuario;
		} catch (IOException ex) {
			ex.printStackTrace();
			try {
				SERVLET_ASISTENTE._log_linea(login, "Error al leer usuario: " + ex);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	public static void grabar_usuario(Usuario usuario) throws IOException {
		Properties prop = new Properties();
		
		prop.setProperty("usuario", usuario.serializar());
		prop.setProperty("notas", Util.nvl(usuario.getNotas()));

		// Entrenamiento
		for (Integer jornada : usuario.getTipo_entrenamiento(0).keySet()) {
			String tipo_entrenamiento;
			if (jornada < AsistenteBO.JORNADA_NUEVO_ENTRENO) {
				tipo_entrenamiento = Util.nvl(usuario.getTipo_entrenamiento(0).get(jornada));
				prop.setProperty("entrenamiento" + jornada, tipo_entrenamiento + "," + Util.nvl(usuario.getDemarcacion().get(jornada)) + "," + (usuario.getEntrenador_principal().get(jornada) == null ? "" : usuario.getEntrenador_principal().get(jornada).serializar_entrenador()) + "," + Util.nvl(usuario.getNivel_asistentes().get(jornada)) + "," + Util.nvl(usuario.getNivel_juveniles().get(jornada)) + ",*");
			} else {
				tipo_entrenamiento = Util.nvl(usuario.getTipo_entrenamiento(0).get(jornada)) + "-" +
									Util.nvl(usuario.getTipo_entrenamiento(1).get(jornada)) + "-" +
									Util.nvl(usuario.getTipo_entrenamiento(2).get(jornada)) + "-" +
									Util.nvl(usuario.getTipo_entrenamiento(3).get(jornada));
				// Aquí dejo en blanco la demarcación
				prop.setProperty("entrenamiento" + jornada, tipo_entrenamiento + ",," + (usuario.getEntrenador_principal().get(jornada) == null ? "" : usuario.getEntrenador_principal().get(jornada).serializar_entrenador()) + "," + Util.nvl(usuario.getNivel_asistentes().get(jornada)) + "," + Util.nvl(usuario.getNivel_juveniles().get(jornada)) + ",*");
			}
		}

		// El usuario de prueba tiene "prueba/" como prefijo
		String ruta = SystemUtil.getVar("path") + (usuario.getLogin().startsWith("prueba/") ? "" : "_") + usuario.getLogin() + ".properties";
		Util.guardar_properties(prop, ruta);

//		ruta = AsistenteBO.PATH_BACKUP + "_" + usuario.getLogin() + ".properties";
//		Util.guardar_properties(prop, ruta);
	}

	public static void borrar_usuario(String login) throws IOException {
		String ruta = SystemUtil.getVar("path") + "_" + login + ".properties";
		Util.borrar_properties(ruta);
	}

	public static String[] obtener_usuarios() {
		try {
			File f = new File(SystemUtil.getVar("path"));
	
			String[] archivos = f.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("_");
				}
			});
			
			return archivos == null ? new String[0] : archivos;
		} catch (Exception e) {
			return new String[] {"Error reading users"};
		}
	}
	
	public static String[] obtener_ultimos_usuarios(int dias) {
		File f = new File(SystemUtil.getVar("path"));
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -dias);

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(SystemUtil.getVar("path") + "/" + name);

				return f.isFile()
					&& c.getTime().before(new Date(f.lastModified()))
					&& name.startsWith("_");
			}
		});
		
		return archivos;
	}
	
	public static List<Usuario> leer_usuarios() {
		String[] archivos = obtener_usuarios();
		List<Usuario> usuarios = new ArrayList<Usuario>();
		for (String login : archivos) {
			Usuario usuario = leer_usuario(login.split(".properties")[0].substring(1), false);
			usuarios.add(usuario);
		}
		return usuarios;
	}

	public static List<Usuario> leer_ultimos_usuarios(int dias) {
		String[] archivos = obtener_ultimos_usuarios(dias);
		List<Usuario> usuarios = new ArrayList<Usuario>();
		for (String login : archivos) {
			Usuario usuario = leer_usuario(login.split(".properties")[0].substring(1), false);
			usuarios.add(usuario);
		}
		return usuarios;
	}
	
	public static String[] obtener_backups(int tid) {
		File f = new File(SystemUtil.getVar("path") + "/backup/");

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(tid + "_[0-9]+\\.properties");
			}
		});
		
		return archivos;
	}
	
	public static void main(String[] args) {
//		System.out.println("_texex_123.properties".matches("_texex_[0-9]+\\.properties"));
		System.out.println(Arrays.asList("Porteria-Defensa---*".split("-")));
	}
}

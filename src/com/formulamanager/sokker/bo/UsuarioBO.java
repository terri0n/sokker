package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;

public class UsuarioBO {
	public static Usuario leer_usuario (String login) {
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD = AsistenteBO.PATH_BASE + "_" + login.toLowerCase() + ".properties";
			
			File file = new File(BD);
			if (!file.exists()) {
				return null;
			}
			
			input = new FileInputStream(file);
	
			// load a properties file
			prop.load(input);
			String linea = prop.getProperty("usuario");
			
			Usuario usuario = new Usuario(Arrays.asList(linea.split(",")));
			
			// ENTRENAMIENTO

			for (Entry<Object, Object> e : prop.entrySet()) {
				String key = e.getKey().toString();
				if (key.startsWith("entrenamiento")) {
					int jornada = Integer.valueOf(key.split("entrenamiento")[1]);
					String[] split = e.getValue().toString().split(",");
					usuario.getTipo_entrenamiento().put(jornada, TIPO_ENTRENAMIENTO.valueOf(split[0]));
					usuario.getDemarcacion().put(jornada, DEMARCACION.valueOf(split[1]));
					if (split.length > 2 && split[2].length() > 0) {
						usuario.getEntrenador_principal().put(jornada, new Jugador(split[2]));
						usuario.getNivel_asistentes().put(jornada, Util.stringToBigDecimal(split[3]));
						if (split.length > 4) {
							usuario.getNivel_juveniles().put(jornada, Util.stringToBigDecimal(split[4]));
						}
					}
				}
			}
			
			return usuario;
		} catch (IOException ex) {
			ex.printStackTrace();
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

		// Entrenamiento
		for (Integer jornada : usuario.getTipo_entrenamiento().keySet()) {
			prop.setProperty("entrenamiento" + jornada, usuario.getTipo_entrenamiento().get(jornada) + "," + usuario.getDemarcacion().get(jornada) + "," + (usuario.getEntrenador_principal().get(jornada) == null ? "" : usuario.getEntrenador_principal().get(jornada).serializar_entrenador()) + "," + Util.nvl(usuario.getNivel_asistentes().get(jornada)) + "," + Util.nvl(usuario.getNivel_juveniles().get(jornada)) + ",*");
		}
	
		String ruta = AsistenteBO.PATH_BASE + "_" + usuario.getLogin() + ".properties";
		Util.guardar_properties(prop, ruta);

		ruta = AsistenteBO.PATH_BACKUP + "_" + usuario.getLogin() + ".properties";
		Util.guardar_properties(prop, ruta);
	}

	public static String[] obtener_usuarios() {
		File f = new File(AsistenteBO.PATH_BASE);

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("_");
			}
		});
		
		return archivos;
	}
}

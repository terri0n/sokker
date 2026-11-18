package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;
import com.formulamanager.sokker.persistence.SqlitePersistence;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.UserDAO;
import com.formulamanager.sokker.persistence.TeamPlayerDAO;

public class UsuarioBO {
	public static final String ACCOUNT_DELETION_REQUESTED_AT = "account_deletion_requested_at";

	private static UserDAO user_dao() throws IOException {
		File database = SqlitePersistence.getAsistenteDatabaseFile();
		if (database == null) throw new IOException("SQLite Asistente database is not initialized");
		return new UserDAO(new SqliteDatabase(database, true));
	}

	private static UserDAO.UserRecord load_user_record(String login) throws IOException {
		try {
			return user_dao().load(login);
		} catch (java.sql.SQLException e) {
			throw new IOException("Error leyendo usuario " + login + " desde SQLite", e);
		}
	}

	private static void save_user_record(String login, String serialized, String notes, Long deletion,
			java.util.Map<Integer, String> training, Properties extensions) throws IOException {
		try {
			user_dao().save(login, serialized, notes, deletion, training, extensions);
		} catch (java.sql.SQLException e) {
			throw new IOException("Error guardando usuario " + login + " en SQLite", e);
		}
	}

	public static synchronized void solicitar_borrado(String login, long timestamp) throws IOException {
		UserDAO.UserRecord record = load_user_record(login);
		if (record == null) throw new IOException("Usuario no encontrado: " + login);
		save_user_record(login, record.getSerialized(), record.getNotes(), Long.valueOf(timestamp),
			record.getTraining(), record.getExtensions());
	}

	public static Long obtener_fecha_solicitud_borrado(String login) {
		try {
			UserDAO.UserRecord record = load_user_record(login);
			return record == null ? null : record.getDeletionRequestedAt();
		} catch (IOException e) {
			throw new IllegalStateException("Error leyendo solicitud de borrado para " + login, e);
		}
	}

	public static long obtener_ultima_actualizacion(String login) {
		try {
			UserDAO.UserRecord record = load_user_record(login);
			return record == null ? 0L : record.getUpdatedAt();
		} catch (IOException e) {
			throw new IllegalStateException("Error leyendo la última actividad de " + login, e);
		}
	}

	public static void restaurar_ultima_actualizacion(String login, long timestamp) throws IOException {
		try {
			user_dao().setUpdatedAt(login, timestamp);
		} catch (java.sql.SQLException e) {
			throw new IOException("Error restaurando la última actividad de " + login, e);
		}
	}

	public static List<String> obtener_usuarios_con_borrado_solicitado() {
		String[] archivos = obtener_usuarios();
		final HashMap<String, Long> fechas = new HashMap<String, Long>();
		List<String> usuarios = new ArrayList<String>();

		for (String archivo : archivos) {
			if (archivo == null || !archivo.startsWith("_") || !archivo.endsWith(".properties")) {
				continue;
			}
			String login = archivo.substring(1, archivo.length() - ".properties".length());
			Long fecha = obtener_fecha_solicitud_borrado(login);
			if (fecha != null) {
				fechas.put(login, fecha);
				usuarios.add(login);
			}
		}

		Collections.sort(usuarios, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				int fecha = fechas.get(a).compareTo(fechas.get(b));
				return fecha != 0 ? fecha : a.compareToIgnoreCase(b);
			}
		});
		return usuarios;
	}

	public static Usuario leer_usuario (String login, boolean leer_scouts) {
		try {
			UserDAO.UserRecord record = load_user_record(login);
			if (record == null) return null;
			Usuario usuario = new Usuario(Arrays.asList(record.getSerialized().split(",", -1)));
			usuario.setNotas(record.getNotes());

			for (Entry<Integer, String> e : record.getTraining().entrySet()) {
				int jornada = e.getKey().intValue();
				String[] split = e.getValue().split(",");
				if (split.length < 2) throw new IOException("Entrenamiento inválido para " + login + " jornada " + jornada);
				if (!split[0].contains("-")) {
					if (Util.nnvl(split[0]) != null) usuario.getTipo_entrenamiento(0).put(jornada, TIPO_ENTRENAMIENTO.valueOf(split[0]));
					if (Util.nnvl(split[1]) != null) usuario.getDemarcacion().put(jornada, DEMARCACION.valueOf(split[1]));
				} else {
					String[] tipos = (split[0] + "-*").split("-");
					for (int i = 0; i < 4; i++) if (Util.nnvl(tipos[i]) != null)
						usuario.getTipo_entrenamiento(i).put(jornada, TIPO_ENTRENAMIENTO.valueOf(tipos[i]));
				}
				if (split.length > 2 && split[2].length() > 0) {
					usuario.getEntrenador_principal().put(jornada, new Jugador(split[2]));
					if (split.length > 3) usuario.getNivel_asistentes().put(jornada, Util.stringToBigDecimal(split[3]));
					if (split.length > 4) usuario.getNivel_juveniles().put(jornada, Util.stringToBigDecimal(split[4]));
				}
			}
			if (leer_scouts) cargar_scouts(usuario);
			return usuario;
		} catch (IOException ex) {
			try { SERVLET_ASISTENTE._log_linea(login, "Error al leer usuario: " + ex); }
			catch (IOException e) { e.printStackTrace(); }
			throw new IllegalStateException("Error al leer usuario " + login, ex);
		}
	}

	/**
	 * Carga la relación de scouts de un usuario. Es independiente de la lectura
	 * de sus datos básicos para poder diferir este trabajo hasta que sea necesario.
	 */
	public static void cargar_scouts(Usuario usuario) throws IOException {
		if (usuario == null || usuario.isScouts_cargados()) {
			return;
		}

		// Evita lecturas duplicadas de NTDB y publicar relaciones parciales
		// cuando una sesión recibe peticiones concurrentes.
		synchronized (usuario) {
			if (usuario.isScouts_cargados()) {
				return;
			}

			HashMap<String, String> map = Util.leer_hashmap("NTDB");
			usuario.setScouts(map.get(usuario.getLogin()));
			usuario.getScout_de().clear();

			for (Entry<String, String> entry : map.entrySet()) {
				String scouts = "," + entry.getValue() + ",";
				if (scouts.contains("," + usuario.getLogin() + ",")) {
					Usuario u = UsuarioBO.leer_usuario(entry.getKey(), false);
					if (u != null && u.getTid_nt() != null) {
						usuario.getScout_de().put(entry.getKey(), u);
					}
				}
			}
			// Un mapa vacío también es un resultado válido: no se relee en cada cambio.
			usuario.setScouts_cargados(true);
		}
	}

	private static String conservar_valores_usuario_desconocidos(String anterior, String serializado) {
		if (anterior == null) {
			return serializado;
		}

		List<String> valores_anteriores = Arrays.asList(anterior.split(",", -1));
		List<String> valores_serializados = Arrays.asList(serializado.split(",", -1));
		int fin_anterior = valores_anteriores.indexOf("*");
		int fin_serializado = valores_serializados.indexOf("*");

		// Las versiones nuevas añaden los campos al final, antes del asterisco.
		// Si el fichero anterior no tiene más campos que el formato que conocemos, no hay nada que conservar.
		if (fin_anterior < 0 || fin_serializado < 0 || fin_anterior <= fin_serializado) {
			return serializado;
		}

		List<String> resultado = new ArrayList<String>(valores_serializados.subList(0, fin_serializado));
		resultado.addAll(valores_anteriores.subList(fin_serializado, fin_anterior));
		return String.join(",", resultado) + ",*";
	}

	private static String purgar_secreto_actualizacion_automatica(String serializado) {
		List<String> valores = Arrays.asList(serializado.split(",", -1));
		int fin = valores.indexOf("*");
		if (fin > 0) {
			valores.set(fin - 1, "");
		}
		return String.join(",", valores);
	}

	public static UserDAO.SaveData preparar_guardado(Usuario usuario) throws IOException {
		String login = usuario.getLogin();
		UserDAO.UserRecord previous = load_user_record(login);
		String usuarioAnterior = previous == null ? null : previous.getSerialized();
		String serialized = purgar_secreto_actualizacion_automatica(usuario.serializar());
		serialized = conservar_valores_usuario_desconocidos(usuarioAnterior, serialized);

		java.util.Map<Integer, String> training = new java.util.TreeMap<Integer, String>();
		Properties extensions = previous == null ? new Properties() : previous.getExtensions();
		for (Integer jornada : usuario.getTipo_entrenamiento(0).keySet()) {
			String tipo;
			String value;
			if (jornada.intValue() < AsistenteBO.JORNADA_NUEVO_ENTRENO) {
				tipo = Util.nvl(usuario.getTipo_entrenamiento(0).get(jornada));
				value = tipo + "," + Util.nvl(usuario.getDemarcacion().get(jornada)) + "," +
					(usuario.getEntrenador_principal().get(jornada) == null ? "" : usuario.getEntrenador_principal().get(jornada).serializar_entrenador()) + "," +
					Util.nvl(usuario.getNivel_asistentes().get(jornada)) + "," + Util.nvl(usuario.getNivel_juveniles().get(jornada)) + ",*";
			} else {
				tipo = Util.nvl(usuario.getTipo_entrenamiento(0).get(jornada)) + "-" +
					Util.nvl(usuario.getTipo_entrenamiento(1).get(jornada)) + "-" +
					Util.nvl(usuario.getTipo_entrenamiento(2).get(jornada)) + "-" +
					Util.nvl(usuario.getTipo_entrenamiento(3).get(jornada));
				value = tipo + ",," + (usuario.getEntrenador_principal().get(jornada) == null ? "" : usuario.getEntrenador_principal().get(jornada).serializar_entrenador()) + "," +
					Util.nvl(usuario.getNivel_asistentes().get(jornada)) + "," + Util.nvl(usuario.getNivel_juveniles().get(jornada)) + ",*";
			}
			String old = previous == null ? null : previous.getTraining().get(jornada);
			training.put(jornada, conservar_valores_usuario_desconocidos(old, value));
		}
		return new UserDAO.SaveData(login, serialized, Util.nvl(usuario.getNotas()),
			previous == null ? null : previous.getDeletionRequestedAt(), training, extensions);
	}

	public static synchronized void grabar_usuario(Usuario usuario) throws IOException {
		UserDAO.SaveData data = preparar_guardado(usuario);
		try {
			user_dao().save(data.getLogin(), data.getSerialized(), data.getNotes(),
				data.getDeletionRequestedAt(), data.getTraining(), data.getExtensions());
		} catch (java.sql.SQLException e) {
			throw new IOException("Error guardando usuario " + data.getLogin() + " en SQLite", e);
		}
	}

	public static synchronized void borrar_usuario(String login) throws IOException {
		try {
			if (!user_dao().delete(login)) throw new IOException("Usuario no encontrado: " + login);
		} catch (java.sql.SQLException e) {
			throw new IOException("Error borrando usuario " + login, e);
		}
	}

	public static String[] obtener_usuarios() {
		try {
			List<String> logins = user_dao().listLogins();
			List<String> files = new ArrayList<String>();
			for (String login : logins) files.add("_" + login + ".properties");
			return files.toArray(new String[files.size()]);
		} catch (Exception e) {
			throw new IllegalStateException("Error listando usuarios desde SQLite", e);
		}
	}

	public static int contar_usuarios() {
		try { return user_dao().count(); }
		catch (Exception e) { throw new IllegalStateException("Error contando usuarios desde SQLite", e); }
	}

	public static String[] obtener_ultimos_usuarios(int dias) {
		Calendar limite = Calendar.getInstance();
		limite.add(Calendar.DAY_OF_MONTH, -dias);
		try {
			List<String> logins = user_dao().listRecentLogins(limite.getTimeInMillis());
			List<String> files = new ArrayList<String>();
			for (String login : logins) files.add("_" + login + ".properties");
			return files.toArray(new String[files.size()]);
		} catch (Exception e) {
			throw new IllegalStateException("Error buscando usuarios recientes en SQLite", e);
		}
	}

	public static List<Usuario> leer_usuarios() {
		String[] archivos = obtener_usuarios();
		List<Usuario> usuarios = new ArrayList<Usuario>();
		for (String archivo : archivos) {
			if (archivo.endsWith(".properties")) {
				Usuario usuario = leer_usuario(archivo.substring(1, archivo.length() - ".properties".length()), false);
				if (usuario != null) usuarios.add(usuario);
			}
		}
		return usuarios;
	}

	public static List<Usuario> leer_ultimos_usuarios(int dias) {
		String[] archivos = obtener_ultimos_usuarios(dias);
		List<Usuario> usuarios = new ArrayList<Usuario>();
		for (String archivo : archivos) {
			if (archivo.endsWith(".properties")) {
				Usuario usuario = leer_usuario(archivo.substring(1, archivo.length() - ".properties".length()), false);
				if (usuario != null) usuarios.add(usuario);
			}
		}
		return usuarios;
	}

	public static String[] obtener_backups(int tid) {
		File database = SqlitePersistence.getAsistenteDatabaseFile();
		if (database == null) throw new IllegalStateException("SQLite Asistente database is not initialized");
		try {
			List<String> paths = new TeamPlayerDAO(new SqliteDatabase(database, true)).listBackupFiles(tid);
			return paths.toArray(new String[paths.size()]);
		} catch (java.sql.SQLException e) {
			throw new IllegalStateException("Error listando backups del equipo " + tid, e);
		}
	}

	public static void main(String[] args) {
//		System.out.println("_texex_123.properties".matches("_texex_[0-9]+\\.properties"));
		System.out.println(Arrays.asList("Porteria-Defensa---*".split("-")));
	}
}
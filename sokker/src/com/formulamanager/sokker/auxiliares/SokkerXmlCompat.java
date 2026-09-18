package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Adaptador de datos JSON modernos al XML mínimo que todavía consumen
 * algunos algoritmos históricos de Sokker Asistente.
 */
public final class SokkerXmlCompat {
	private static final Map<Integer, Object> partidosJson = new ConcurrentHashMap<Integer, Object>();
	private static final Map<Integer, Object> ligasJson = new ConcurrentHashMap<Integer, Object>();

	private SokkerXmlCompat() {}

	/**
	 * Sustituye únicamente los XML de partidos y ligas que todavía usa el cálculo
	 * de entrenamiento. Para cualquier otra URL devuelve null y el llamador puede
	 * continuar con el comportamiento XML legado.
	 */
	public static XmlPage getXmlPage(WebClient navegador, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		String matchesPrefix = AsistenteBO.SOKKER_URL + "/xml/matches-team-";
		String matchPrefix = AsistenteBO.SOKKER_URL + "/xml/match-";
		String leaguePrefix = AsistenteBO.SOKKER_URL + "/xml/league-";
		String xml;

		if (url.startsWith(matchesPrefix) && url.endsWith(".xml")) {
			Integer tid = extraerId(url, matchesPrefix);
			if (tid == null) {
				return null;
			}
			xml = obtenerPartidosXml(navegador, tid);
		} else if (url.startsWith(matchPrefix) && url.endsWith(".xml")) {
			Integer mid = extraerId(url, matchPrefix);
			if (mid == null) {
				return null;
			}
			xml = obtenerPartidoXml(navegador, mid);
		} else if (url.startsWith(leaguePrefix) && url.endsWith(".xml")) {
			Integer leagueID = extraerId(url, leaguePrefix);
			if (leagueID == null) {
				return null;
			}
			xml = obtenerLigaXml(navegador, leagueID);
		} else {
			return null;
		}

		if (xml == null) {
			return null;
		}

		StringWebResponse response = new StringWebResponse(xml, new URL(url));
		return new XmlPage(response, navegador.getCurrentWindow());
	}

	private static String obtenerPartidosXml(WebClient navegador, int tid) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object actual = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
		Integer temporada = integer(actual, "today.season");
		List<Object> partidos = new ArrayList<Object>();
		Set<Integer> ids = new HashSet<Integer>();

		if (temporada != null) {
			// En la primera jornada de temporada puede hacer falta el partido de la
			// semana anterior, que pertenece todavía a la temporada previa.
			for (int season = Math.max(0, temporada - 1); season <= temporada; season++) {
				Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL
						+ "/api/team/" + tid + "/match?filter[season]=" + season + "&filter[limit]=200");
				agregarPartidos(pagina, partidos, ids);
			}
		} else {
			// Compatibilidad defensiva por si /api/current no incluye season.
			Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL
					+ "/api/team/" + tid + "/match?filter[limit]=200");
			agregarPartidos(pagina, partidos, ids);
		}

		return buildMatchesXml(actual, partidos);
	}

	private static void agregarPartidos(Object pagina, List<Object> partidos, Set<Integer> ids) {
		for (Object partido : list(pagina, "matches")) {
			Integer mid = integer(partido, "id", "matchID");
			if (mid == null) {
				partidos.add(partido);
				continue;
			}
			if (ids.add(mid)) {
				partidos.add(partido);
				partidosJson.put(mid, partido);
				guardarLiga(partido);
			}
		}
	}

	private static String obtenerPartidoXml(WebClient navegador, int mid) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object detalle = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/match/" + mid);
		Object resumen = partidosJson.get(mid);
		detalle = completarLiga(detalle, resumen);
		guardarLiga(detalle);

		Object alineacion = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/match/" + mid + "/lineup");
		Object estadisticas = null;
		if (needsStats(alineacion)) {
			try {
				estadisticas = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/match/" + mid + "/stats");
			} catch (FailingHttpStatusCodeException e) {
				// stats es complementario. La alineación sigue siendo suficiente si
				// Sokker no publica este endpoint para el partido o el usuario.
				if (e.getStatusCode() != 403 && e.getStatusCode() != 404) {
					throw e;
				}
			}
		}

		return buildMatchXml(detalle, alineacion, estadisticas);
	}

	private static Object completarLiga(Object detalle, Object resumen) {
		if (integer(detalle, "league.id", "leagueID", "info.league.id", "info.leagueID", "match.league.id") != null
				|| !(detalle instanceof Map<?, ?>) || !(resumen instanceof Map<?, ?>)) {
			return detalle;
		}

		Object league = value(resumen, "league");
		if (league == null) {
			league = value(resumen, "match.league");
		}
		if (league == null) {
			return detalle;
		}

		Map<String, Object> combinado = new LinkedHashMap<String, Object>();
		for (Map.Entry<?, ?> entry : ((Map<?, ?>) detalle).entrySet()) {
			if (entry.getKey() instanceof String) {
				combinado.put((String) entry.getKey(), entry.getValue());
			}
		}
		combinado.put("league", league);
		return combinado;
	}

	private static String obtenerLigaXml(WebClient navegador, int leagueID) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object league = ligasJson.get(leagueID);
		if (league == null) {
			try {
				Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/league/" + leagueID);
				Object nested = value(pagina, "league");
				league = nested == null ? pagina : nested;
				if (league != null) {
					ligasJson.put(leagueID, league);
				}
			} catch (FailingHttpStatusCodeException e) {
				if (e.getStatusCode() != 403 && e.getStatusCode() != 404) {
					throw e;
				}
			}
		}
		return buildLeagueXml(league);
	}

	private static Integer extraerId(String url, String prefix) {
		try {
			return Integer.valueOf(url.substring(prefix.length(), url.length() - 4));
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static void guardarLiga(Object pagina) {
		Integer leagueID = integer(pagina,
				"league.id", "leagueID", "info.league.id", "info.leagueID", "match.league.id");
		Object league = value(pagina, "league");
		if (league == null) {
			league = value(pagina, "match.league");
		}
		if (leagueID != null && league != null) {
			ligasJson.put(leagueID, league);
		}
	}

	public static String buildMatchesXml(Object current, List<?> matches) {
		Integer currentWeek = integer(current, "today.week");
		Integer currentDay = integer(current, "today.day");
		LocalDate currentDate = date(current, "today.date.value", "today.date");

		StringBuilder xml = new StringBuilder("<matches>");
		if (matches != null) {
			for (Object match : matches) {
				Integer id = integer(match, "id", "matchID");
				if (id == null) {
					return null;
				}

				Integer week = integer(match, "week", "time.week");
				Integer day = integer(match, "day", "time.day");
				if ((week == null || day == null) && currentWeek != null && currentDay != null && currentDate != null) {
					LocalDate matchDate = date(match,
							"time.time.dateTime", "time.dateTime", "time.time.date.value", "date.value", "date");
					if (matchDate != null) {
						long delta = ChronoUnit.DAYS.between(currentDate, matchDate);
						long serial = (long) currentWeek * 7L + currentDay + delta;
						week = (int) Math.floorDiv(serial, 7L);
						day = (int) Math.floorMod(serial, 7L);
					}
				}
				if (week == null || day == null) {
					continue;
				}

				Boolean finished = bool(match, "time.wasPlayed", "wasPlayed", "isFinished");
				if (finished == null) {
					return null;
				}
				xml.append("<match><matchID>").append(id).append("</matchID>")
					.append("<week>").append(week).append("</week>")
					.append("<day>").append(day).append("</day>")
					.append("<isFinished>").append(Boolean.TRUE.equals(finished) ? 1 : 0).append("</isFinished></match>");
			}
		}
		return xml.append("</matches>").toString();
	}

	public static String buildMatchXml(Object detail, Object lineup, Object stats) {
		Integer leagueId = integer(detail,
				"league.id", "leagueID", "info.league.id", "info.leagueID", "match.league.id");
		if (leagueId == null) {
			return null;
		}
		StringBuilder xml = new StringBuilder("<match><info><leagueID>")
				.append(leagueId)
				.append("</leagueID></info><playerStatsList>");

		if (!appendPlayers(xml, list(lineup, "homePlayers", "home.players", "lineup.homePlayers"), stats)
				|| !appendPlayers(xml, list(lineup, "awayPlayers", "away.players", "lineup.awayPlayers"), stats)) {
			return null;
		}
		return xml.append("</playerStatsList></match>").toString();
	}

	private static boolean appendPlayers(StringBuilder xml, List<?> players, Object stats) {
		int starterNumber = 1;
		int benchNumber = 12;
		for (int i = 0; i < players.size(); i++) {
			Object player = players.get(i);
			Integer id = integer(player, "id", "player.id");
			if (id == null) {
				return false;
			}

			Object statsPlayer = findPlayer(stats, id);
			Boolean benchValue = bool(player, "bench", "isBench");
			boolean bench = benchValue != null ? benchValue.booleanValue() : i >= 11;
			int number = bench ? benchNumber++ : starterNumber++;

			Integer formation = integer(player,
					"formation.code", "formation", "player.formation.code");
			if (formation == null && statsPlayer != null) {
				formation = integer(statsPlayer, "formation.code", "formation", "player.formation.code");
			}
			if (formation == null) {
				// Sin formación no se puede reconstruir el entrenamiento con seguridad.
				return false;
			}

			Integer timeIn = integer(player, "timeIn", "time.in", "minuteIn", "substitution.in");
			Integer timeOut = integer(player, "timeOut", "time.out", "minuteOut", "substitution.out");
			Integer minutes = integer(player, "minutes", "timePlaying", "timePlayed", "stats.minutes");
			if (statsPlayer != null) {
				if (timeIn == null) {
					timeIn = integer(statsPlayer, "timeIn", "time.in", "minuteIn", "substitution.in");
				}
				if (timeOut == null) {
					timeOut = integer(statsPlayer, "timeOut", "time.out", "minuteOut", "substitution.out");
				}
				if (minutes == null) {
					minutes = integer(statsPlayer, "minutes", "timePlaying", "timePlayed", "stats.minutes");
				}
			}

			if (timeIn == null || timeOut == null) {
				if (minutes == null) {
					// Si no podemos reconstruir el tiempo jugado de un miembro de la alineación,
					// rechazamos todo el adaptador para que el llamador use el XML real.
					return false;
				}
				int played = Math.max(0, Math.min(90, minutes));
				if (bench) {
					timeIn = played == 0 ? 0 : 91 - played;
					timeOut = played == 0 ? 0 : 90;
				} else if (played == 0) {
					// El XML legado usa timeOut=0 para indicar que jugó hasta el final.
					timeIn = 91;
					timeOut = 90;
				} else {
					timeIn = 0;
					timeOut = played;
				}
			}

			xml.append("<playerStats><playerID>").append(id).append("</playerID>")
				.append("<number>").append(number).append("</number>")
				.append("<timeIn>").append(timeIn).append("</timeIn>")
				.append("<timeOut>").append(timeOut).append("</timeOut>")
				.append("<formation>").append(formation).append("</formation></playerStats>");
		}
		return true;
	}

	public static String buildLeagueXml(Object league) {
		Integer type = integer(league, "type.code", "type", "info.type.code", "info.type");
		Boolean official = bool(league, "isOfficial", "official", "info.isOfficial", "info.official");
		StringBuilder xml = new StringBuilder("<league><info>");
		// El código legado trata una liga sin type como oficial. Si la API no da
		// ambos campos, preservamos exactamente ese fallback en vez de inventar datos.
		if (type != null && official != null) {
			xml.append("<type>").append(type).append("</type><isOfficial>")
				.append(official.booleanValue() ? 1 : 0).append("</isOfficial>");
		}
		return xml.append("</info></league>").toString();
	}

	public static boolean needsStats(Object lineup) {
		for (Object player : allPlayers(lineup)) {
			if (integer(player, "timeIn", "time.in", "minuteIn", "substitution.in") == null
					&& integer(player, "minutes", "timePlaying", "timePlayed", "stats.minutes") == null) {
				return true;
			}
		}
		return false;
	}

	public static List<?> list(Object root, String... paths) {
		for (String path : paths) {
			Object value = value(root, path);
			if (value instanceof List<?>) {
				return (List<?>) value;
			}
		}
		return Collections.emptyList();
	}

	public static Integer integer(Object root, String... paths) {
		for (String path : paths) {
			Object value = value(root, path);
			if (value instanceof Number) {
				return ((Number) value).intValue();
			}
			if (value instanceof String) {
				try {
					return Integer.valueOf((String) value);
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return null;
	}

	public static Object value(Object root, String path) {
		Object current = root;
		for (String part : path.split("\\.")) {
			if (!(current instanceof Map<?, ?>)) {
				return null;
			}
			current = ((Map<?, ?>) current).get(part);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	private static Boolean bool(Object root, String... paths) {
		for (String path : paths) {
			Object value = value(root, path);
			if (value instanceof Boolean) {
				return (Boolean) value;
			}
			if (value instanceof Number) {
				return ((Number) value).intValue() != 0;
			}
			if (value instanceof String) {
				return Boolean.valueOf((String) value);
			}
		}
		return null;
	}

	private static LocalDate date(Object root, String... paths) {
		for (String path : paths) {
			Object value = value(root, path);
			if (value instanceof String) {
				String text = (String) value;
				if (text.length() >= 10) {
					try {
						return LocalDate.parse(text.substring(0, 10));
					} catch (RuntimeException ignored) {
					}
				}
		}
		}
		return null;
	}

	private static List<Object> allPlayers(Object lineup) {
		List<Object> result = new ArrayList<Object>();
		result.addAll(cast(list(lineup, "homePlayers", "home.players", "lineup.homePlayers")));
		result.addAll(cast(list(lineup, "awayPlayers", "away.players", "lineup.awayPlayers")));
		return result;
	}

	private static List<Object> cast(List<?> values) {
		List<Object> result = new ArrayList<Object>();
		result.addAll(values);
		return result;
	}

	private static Object findPlayer(Object root, Integer id) {
		if (root instanceof Map<?, ?>) {
			Integer candidate = integer(root, "id", "player.id", "playerID");
			if (id.equals(candidate)) {
				return root;
			}
			for (Object child : ((Map<?, ?>) root).values()) {
				Object found = findPlayer(child, id);
				if (found != null) {
					return found;
				}
			}
		} else if (root instanceof List<?>) {
			for (Object child : (List<?>) root) {
				Object found = findPlayer(child, id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}

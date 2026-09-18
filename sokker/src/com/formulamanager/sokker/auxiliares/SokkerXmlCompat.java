package com.formulamanager.sokker.auxiliares;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de datos JSON modernos al XML mínimo que todavía consumen
 * algunos algoritmos históricos de Sokker Asistente.
 */
public final class SokkerXmlCompat {
	private SokkerXmlCompat() {}

	public static String buildMatchesXml(Object current, List<?> matches) {
		Integer currentWeek = integer(current, "today.week");
		Integer currentDay = integer(current, "today.day");
		LocalDate currentDate = date(current, "today.date.value", "today.date");

		StringBuilder xml = new StringBuilder("<matches>");
		if (matches != null) {
			for (Object match : matches) {
				Integer id = integer(match, "id", "matchID");
				if (id == null) {
					continue;
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
				xml.append("<match><matchID>").append(id).append("</matchID>")
					.append("<week>").append(week).append("</week>")
					.append("<day>").append(day).append("</day>")
					.append("<isFinished>").append(Boolean.TRUE.equals(finished) ? 1 : 0).append("</isFinished></match>");
			}
		}
		return xml.append("</matches>").toString();
	}

	public static String buildMatchXml(Object detail, Object lineup, Object stats) {
		Integer leagueId = integer(detail, "league.id", "leagueID", "info.league.id", "info.leagueID");
		StringBuilder xml = new StringBuilder("<match><info><leagueID>")
				.append(leagueId == null ? 0 : leagueId)
				.append("</leagueID></info><playerStatsList>");

		appendPlayers(xml, list(lineup, "homePlayers", "home.players"), stats);
		appendPlayers(xml, list(lineup, "awayPlayers", "away.players"), stats);
		return xml.append("</playerStatsList></match>").toString();
	}

	private static void appendPlayers(StringBuilder xml, List<?> players, Object stats) {
		int starterNumber = 1;
		int benchNumber = 12;
		for (int i = 0; i < players.size(); i++) {
			Object player = players.get(i);
			Integer id = integer(player, "id", "player.id");
			if (id == null) {
				continue;
			}

			Object statsPlayer = findPlayer(stats, id);
			Boolean benchValue = bool(player, "bench", "isBench");
			boolean bench = benchValue != null ? benchValue.booleanValue() : i >= 11;
			int number = bench ? benchNumber++ : starterNumber++;

			Integer formation = integer(player,
					"formation.code", "formation", "position.code", "player.formation.code");
			if (formation == null && statsPlayer != null) {
				formation = integer(statsPlayer, "formation.code", "formation", "position.code");
			}
			if (formation == null) {
				// El algoritmo legado no puede procesar un playerStats sin formación.
				continue;
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
				int played = minutes == null ? (bench ? 0 : 90) : Math.max(0, Math.min(90, minutes));
				if (bench) {
					timeIn = played == 0 ? 0 : 91 - played;
					timeOut = played == 0 ? 0 : 90;
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
		result.addAll(cast(list(lineup, "homePlayers", "home.players")));
		result.addAll(cast(list(lineup, "awayPlayers", "away.players")));
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

package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.Configuration;

public class JSONUtil {
	public static Object getJson(WebClient navegador, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		String json = navegador.getPage(url).getWebResponse().getContentAsString();
		return Configuration.defaultConfiguration().jsonProvider().parse(json);
	}

	public static Object getJSON(LinkedHashMap<String, Object> lista, String path) {
		String[] array = path.split("\\.");

		for (int i = 0; i < array.length - 1; i++) {
			Object value = lista.get(array[i]);
			if (!(value instanceof LinkedHashMap)) {
				return null;
			}
			lista = (LinkedHashMap<String, Object>) value;
		}

		return lista.get(array[array.length - 1]);
	}

	public static String getString(LinkedHashMap<String, Object> lista, String path) {
		return (String) getJSON(lista, path);
	}

	public static Integer getInteger(LinkedHashMap<String, Object> lista, String path) {
		Object value = getJSON(lista, path);
		if (value == null) {
			return null;
		}
		if (!(value instanceof Number)) {
			throw new IllegalArgumentException("JSON field is not numeric: " + path);
		}
		try {
			return Integer.valueOf(new BigDecimal(value.toString()).intValueExact());
		} catch (ArithmeticException | NumberFormatException e) {
			throw new IllegalArgumentException("JSON field is not an integer in range: " + path, e);
		}
	}

	public static Integer getSkills(LinkedHashMap<String, Object> lista, String path) {
		Integer skill = getInteger(lista, "skills." + path);
		Integer change = getInteger(lista, "skillsChange." + path);
		return skill == null || change == null ? null : Integer.valueOf(skill.intValue() - change.intValue());
	}

	public static Boolean getBoolean(LinkedHashMap<String, Object> lista, String path) {
		return (Boolean) getJSON(lista, path);
	}

	public static Double getDouble(LinkedHashMap<String, Object> lista, String path) {
		Object value = getJSON(lista, path);
		return value instanceof Number ? Double.valueOf(((Number) value).doubleValue()) : null;
	}
}

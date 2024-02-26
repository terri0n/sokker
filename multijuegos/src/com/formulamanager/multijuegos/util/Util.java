package com.formulamanager.multijuegos.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.formulamanager.multijuegos.idiomas.Idiomas;

public class Util {
	public static String nvl(Object s) {
		return s == null ? "" : s.toString();
	}

	public static String nnvl(String s) {
		return "".equals(s) ? null : s;
	}

	public static int invl(Integer i) {
		return i == null ? 0 : i;
	}

	public static float fnvl(Float f) {
		return f == null ? 0f : f;
	}

	public static BigDecimal bdnvl(BigDecimal bd) {
		return bd == null ? BigDecimal.ZERO : bd;
	}

	public static String ifnull(String s, String ifnull) {
		return s == null ? ifnull : s;
	}

	public static String getString(HttpServletRequest request, String param) {
		return nnvl(request.getParameter(param));
	}

	public static String integerToString(Integer i) {
		return i == null ? null : String.valueOf(i);
	}

	public static Float integerToFloat(Integer i) {
		return i == null ? null : new Float(i);
	}

	public static Integer getInteger(HttpServletRequest request, String param) {
		return stringToInteger(getString(request, param));
	}
	
	public static Integer stringToInteger(String s) {
		return nnvl(s) == null ? null : Integer.valueOf(s);
	}

	public static boolean stringToBoolean(String s) {
		return nnvl(s) != null && !"0".equals(s) && !("false").equals(s);
	}

	public static String booleanToString(boolean b) {
		return b ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}
	
	public static String dateToYMD(Date d) {
		return d == null ? "" : new SimpleDateFormat("yyyyMMdd").format(d);
	}

	public static String dateToString(Date d) {
		return d == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(d);
	}

	public static String dateTimeToString(Date d) {
		return d == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(d);
	}

	public static Date stringToDateTime(String s) throws ParseException {
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(s);
	}

	public static BigDecimal stringToBigDecimal(String s) {
		return nnvl(s) == null ? null : new BigDecimal(s);
	}
	
	public static boolean getBoolean(HttpServletRequest request, String param) {
		return stringToBoolean(getString(request, param));
	}
	
	public static List<Integer> splitInt(String cad, String delim) {
		List<Integer> lista = new ArrayList<Integer>();
		
		for (String s : cad.split(delim)) {
			Integer i = stringToInteger(s.trim());
			if (i != null) {
				lista.add(i);
			}
		}

		return lista;
	}

	public static List<Integer> splitIntNull(String cad, String delim) {
		List<Integer> lista = new ArrayList<Integer>();
		
		for (String s : cad.split(delim)) {
			Integer i = stringToInteger(s.trim());
			lista.add(i);
		}

		return lista;
	}
	
	public static void guardar_properties(Properties prop, String ruta) throws IOException {
		File file = new File(ruta);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(ruta);
		prop.store(fos, null);
		fos.close();
	}

	public static double log(double a, double b) {
		return Math.log(b) / Math.log(a);
	}
	
	public static float sumar(List<Float> lista) {
		float total = 0f;
		for (Float f : lista) {
			total += f;
		}
		return total;
	}

	public static BigDecimal sumar_bd(List<BigDecimal> lista) {
		BigDecimal total = BigDecimal.ZERO;
		for (BigDecimal bd : lista) {
			total = total.add(bd);
		}
		return total;
	}

	public static String getMD5(String input) {
		try {
			 MessageDigest md = MessageDigest.getInstance("MD5");
			 byte[] messageDigest = md.digest(input.getBytes());
			 BigInteger number = new BigInteger(1, messageDigest);
			 String hashtext = number.toString(16);
	
			 while (hashtext.length() < 32) {
				 hashtext = "0" + hashtext;
			 }
			 return hashtext;
		} catch (NoSuchAlgorithmException e) {
			 throw new RuntimeException(e);
		}
	}

	public static String initCap(String s) {
		return s == null ? null : s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String getTexto(String lang, String key) {
////		Locale locale = (Locale)Config.get(request.getSession(), Config.FMT_LOCALE);
//		Locale locale = (Locale)request.getSession().getAttribute("javax.servlet.jsp.jstl.fmt.locale.session");
//		if (locale == null) {
//			locale = request.getLocale();
//		}
//		ResourceBundle rb = ResourceBundle.getBundle(Util.ApplicationResources, locale);
//		return rb.getString(key);

		Locale locale = new Locale(lang.toLowerCase());
		ResourceBundle rb = ResourceBundle.getBundle(Idiomas.APPLICATION_RESOURCES, locale);
		return rb.getString(key);
	}

	public static String getCookie(HttpServletRequest request, String name) {
		try {
			if (request.getCookies() != null) {
				for (Cookie c : request.getCookies()) {
					if (c.getName().equals(name)) {
						return URLDecoder.decode(c.getValue(), "UTF-8");
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

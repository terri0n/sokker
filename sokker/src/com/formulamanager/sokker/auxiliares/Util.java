package com.formulamanager.sokker.auxiliares;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Util {
	public static String ApplicationResources = "com.formulamanager.sokker.idiomas.ApplicationResources";

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

	public static double dnvl(Double d) {
		return d == null ? 0d : d;
	}

	public static BigDecimal bdnvl(BigDecimal bd) {
		return bd == null ? BigDecimal.ZERO : bd;
	}

	public static Object ifnull(Object s, Object ifnull) {
		return s == null ? ifnull : s;
	}

	public static String getString(HttpServletRequest request, String param) {
		return nnvl(request.getParameter(param));
	}

	public static String integerToString(Integer i) {
		return i == null ? null : String.valueOf(i);
	}

	public static Float integerToFloat(Integer i) {
		return i == null ? null : i.floatValue();
	}

	public static Integer getInteger(HttpServletRequest request, String param) {
		return stringToInteger(getString(request, param));
	}

	public static Float getFloat(HttpServletRequest request, String param) {
		return stringToFloat(getString(request, param));
	}

	public static Double getDouble(HttpServletRequest request, String param) {
		return stringToDouble(getString(request, param));
	}

	public static Integer stringToInteger(String s) {
		return nnvl(s) == null ? null : Integer.valueOf(s);
	}

	public static Float stringToFloat(String s) {
		return nnvl(s) == null ? null : Float.valueOf(s);
	}

	public static Double stringToDouble(String s) {
		return nnvl(s) == null ? null : Double.valueOf(s);
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

	public static Date stringToDate(String s) throws ParseException {
		return new SimpleDateFormat("dd/MM/yyyy").parse(s);
	}

	public static BigDecimal getBigDecimal(HttpServletRequest request, String param) {
		return stringToBigDecimal(getString(request, param));
	}
	
	public static BigDecimal stringToBigDecimal(String s) {
		return nnvl(s) == null ? null : new BigDecimal(s);
	}
	
	public static boolean getBoolean(HttpServletRequest request, String param) {
		return stringToBoolean(getString(request, param));
	}

	public static boolean getBool(HttpSession session, String param) {
		return (boolean)ifnull(session.getAttribute(param), false);
	}
	public static int getInt(HttpSession session, String param) {
		return (int)ifnull(session.getAttribute(param), 0);
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
		if (ruta.indexOf("/") > -1) {
			File carpeta = new File(ruta.substring(0, ruta.indexOf("/")));
			carpeta.mkdirs();
		}

		synchronized (ruta.intern()) {
			File file = new File(ruta);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(ruta);
			prop.store(fos, null);
			fos.close();
		}
	}
	
	public static void borrar_properties(String ruta) {
		synchronized (ruta.intern()) {
			File file = new File(ruta);
			file.delete();
		}
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

	public static String getTexto(String lang, String key, String param) {
		Locale locale = new Locale(lang.toLowerCase());
		ResourceBundle rb = ResourceBundle.getBundle(Util.ApplicationResources, locale);
		String pattern = rb.getString(key);
		return MessageFormat.format(pattern, param);
	}

	public static String getTexto(String lang, String key) {
		Locale locale = new Locale(lang.toLowerCase());
		ResourceBundle rb = ResourceBundle.getBundle(Util.ApplicationResources, locale);
		return rb.getString(key);
	}

	public static float round(float f, int i) {
		double exp = Math.pow(10d, i);
		return (float) ((float) Math.round(f * exp) / exp);
	}

	public static double round(double d, int i) {
		double exp = Math.pow(10d, i);
		return Math.round(d * exp) / exp;
	}

	public static String mostrar_espacio_libre() {
		String salida = "";
		NumberFormat nf = new DecimalFormat("#0.0");
		for (Path root : FileSystems.getDefault().getRootDirectories()) {
			try {
				FileStore store = Files.getFileStore(root);
				if (store.getUsableSpace() < Math.pow(1024, 2) * 600) {	// 600 Mb
					salida += "<span style='color: red'>";
				}
				salida += root +  ": available=" + nf.format(store.getUsableSpace() / Math.pow(1024, 3)) + " GB, total=" + nf.format(store.getTotalSpace() / Math.pow(1024, 3)) + " GB<br/>\n";
				if (store.getUsableSpace() < Math.pow(1024, 3)) {
					salida += "</span/>";
				}
			} catch (IOException e) {
				//salida += e.toString();
			}
		}
		return salida;
	}

	public static HashMap<String, String> leer_hashmap(String nombre) throws IOException {
		HashMap<String, String> hashmap = new HashMap<String, String>();
		
		Properties prop = new Properties();
		InputStream input = null;

		synchronized (nombre.intern()) {
			try {
				String ruta = SystemUtil.getVar("path") + nombre + ".properties";
				
				File file = new File(ruta);
				if (!file.exists()) {
					return hashmap;
				}
				
				input = new FileInputStream(file);
		
				// load a properties file
				prop.load(input);
				for (Entry<Object, Object> entry : prop.entrySet()) {
					hashmap.put((String)entry.getKey(), (String)entry.getValue());
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
		}
		
		return hashmap;
	}
	
	public static void guardar_hashmap(HashMap<String, String> hashmap, String nombre) throws IOException {
		Properties prop = new Properties();
		
		for (Entry<String, String> entry : hashmap.entrySet()) {
			prop.setProperty(entry.getKey(), Util.nvl(entry.getValue()));
		}
		
		String ruta = SystemUtil.getVar("path") + nombre + ".properties";
		guardar_properties(prop, ruta);
	}
	
	public static Date limpiar_fecha(Date fecha) {
		Calendar c = Calendar.getInstance();
		c.setTime(fecha);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date limpiar_dia(Date fecha) {
		fecha.setDate(1);
		fecha.setMonth(0);
		fecha.setYear(0);
		return fecha;
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

	public static String escapeJS(String nombre) {
		return nombre.replace("'", "\\\'");
	}
}

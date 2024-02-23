package com.formulamanager.sokker.auxiliares;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;




public class FileUtil {
	
	static final int BUFFER = 2048;

	// Devuelve la lista de clases de un package
	public static List<Class<?>> getClasses(String pckgname, boolean subdirs) {
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			String path = pckgname.replace('.', File.separatorChar);
			URL resource = cld.getResource(path);
			File directory = new File(URLDecoder.decode(resource.getFile()));

			return obtenerClasesDirectorio(directory, pckgname, subdirs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> getClassesJAR(String path) throws IOException {
		List<String> classNames = new ArrayList<String>();
		
		ZipInputStream zip = new ZipInputStream(new FileInputStream(path));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
		    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
		        // This ZipEntry represents a class. Now, what class does it represent?
		        String className = entry.getName().replace('/', '.'); // including ".class"
		        classNames.add(className.substring(0, className.length() - ".class".length()));
		    }
		}
		zip.close();
		
		return classNames;
	}
	
	// Devuelve la lista de clases de un directorio
	private static List<Class<?>> obtenerClasesDirectorio(File directory, String pckgname, boolean subdirs) throws ClassNotFoundException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		if (directory.exists()) {
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				if (files[i].endsWith(".class")) {
					classes.add(Class.forName(pckgname + '.' + files[i].substring(0, files[i].length() - 6)));
				} else if (subdirs) {
					File subdir = new File(directory.getPath() + File.separator + files[i]);
					if (subdir.isDirectory()) {
						classes.addAll(obtenerClasesDirectorio(subdir, pckgname + "." + files[i], subdirs));
					}
				}
			}
		}
		return classes;
	}

/*	public static Class<?>[] getClasses(String pckgname)
			throws ClassNotFoundException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		// Get a File object for the package
		File directory = null;
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			String path = pckgname.replace('.', '/');
			URL resource = cld.getResource(path);
			if (resource == null) {
				throw new ClassNotFoundException("No resource for " + path);
			}
			directory = new File(resource.getFile());
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname + " (" + directory
					+ ") does not appear to be a valid package");
		}
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					classes.add(Class.forName(pckgname + '.'
							+ files[i].substring(0, files[i].length() - 6)));
				}
			}
		} else {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be a valid package");
		}
		Class<?>[] classesA = new Class[classes.size()];
		classes.toArray(classesA);
		return classesA;
	}
*/
	/**
	 * Devuelve una lista de cadenas con los nombres de las clases que forman un paquete
	 * NOTA: Estos nombres incluyen el ".class" al final
	 * @param jarName
	 * @param packageName
	 * @return
	 */
	public static List<String> getClasseNamesInPackage(String jarName, String packageName) {
		List<String> classes = new ArrayList<String>();

		packageName = packageName.replaceAll("\\.", "/");
		
		try {
			JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
			JarEntry jarEntry;

			while (true) {
				jarEntry = jarFile.getNextJarEntry();
				if (jarEntry == null) {
					break;
				}
				if ((jarEntry.getName().startsWith(packageName)) && (jarEntry.getName().endsWith(".class"))) {
					classes.add(jarEntry.getName().replaceAll("/", "\\."));
				}
			}
			jarFile.close();
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return classes;
	}
	
	/**
	 * @param tabla Nombre de la tabla. Se le añade el sufijo "_DAO"
	 * @param dao Nombre completo del dao. Obligatorio si no se especifica el nombre de la tabla
	 * @param pkg
	 * @param jar
	 * @return
	 */
	public static String getDAO(String tabla, String dao, String pkg, String jar){
		
		List<String> clases = FileUtil.getClasseNamesInPackage(jar,pkg);
		String classDao = null;
		for(String clase:clases){
			String st1 = clase.substring(0,clase.lastIndexOf('.'));
			String stDao = st1.substring(st1.lastIndexOf('.')+1);
			//System.out.println(tabla.getNombre() + "==>" + stDao);
			if(tabla.equals(stDao.replaceAll("_DAO","")) || tabla.equals(dao)){
				classDao = clase.substring(0,clase.lastIndexOf('.'));
				break;					
			}
		}
		return classDao;
	}
	
	public static byte[] leerFichero(String fichero) throws FileNotFoundException, IOException, Exception {
		return leerFichero (fichero, null);
	}

	/**
	 * Lee un fichero y lo almacena en un array de bytes
	 * @param fichero Nombre del fichero
	 * @param longitudMaxima M�xima longitud del fichero permitida
	 * @return Array de bytes con el contenido del fichero leído
	 * @throws FileNotFoundException Si no se encuentra el fichero
	 * @throws IOException Si se produce un error durante la lectura
	 * @throws AuxException Si la longitud del fichero supera el m�ximo permitido
	 */
	public static byte[] leerFichero(String fichero, Integer longitudMaxima) throws FileNotFoundException, IOException, Exception {
		File f = new File (fichero);
		if (longitudMaxima != null && f.length() > longitudMaxima) {
			throw new Exception("La longitud del fichero supera el m�ximo permitido");
		}
		byte[] bytes = new byte[(int)f.length()];
		FileInputStream fis = new FileInputStream(f);
		fis.read(bytes);
		fis.close();
		return bytes;
	}
	
	public static String leerURL(String url) {
		try {
			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setReadTimeout(5000);
			conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
			conn.addRequestProperty("User-Agent", "Mozilla");
			conn.addRequestProperty("Referer", "google.com");
	
			System.out.println("Request URL ... " + url);
	
			boolean redirect = false;
	
			// normally, 3xx is redirect
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			}
	
			System.out.println("Response Code ... " + status);
	
			if (redirect) {
	
				// get redirect url from "location" header field
				String newUrl = conn.getHeaderField("Location");
	
				// get the cookie if need, for login
				String cookies = conn.getHeaderField("Set-Cookie");
	
				// open the new connnection again
				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				conn.setRequestProperty("Cookie", cookies);
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				conn.addRequestProperty("User-Agent", "Mozilla");
				conn.addRequestProperty("Referer", "google.com");
										
				System.out.println("Redirect to URL : " + newUrl);
	
			}
	
			BufferedReader in = new BufferedReader(
		                              new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer html = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				html.append(inputLine);
			}
			in.close();
	
			System.out.println("URL Content... \n" + html.toString());
			System.out.println("Done");

			return html.toString();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
	    }
	}

	/**
	 * Crea un fichero temporal a partir de un array de bytes y lo ejecuta
	 * @param fichero Nombre del fichero temporal
	 * @param bytes Array de bytes del fichero a ejecutar
	 * @throws Exception Si se produce algún error
	 */
//	public static void ejecutarFichero(String fichero, byte[] bytes) throws Exception {
//		File f = crearFicheroTemporal(fichero.replaceAll(" ", "_"), bytes);
//		ejecutarFichero(f.getAbsolutePath());
////		Runtime.getRuntime().exec("cmd /C \"" + f.getAbsolutePath() + "\"");
//	}
	
	/**
	 * Crea un fichero temporal a partir de un array de bytes y lo ejecuta
	 * @param fichero Nombre del fichero temporal
	 * @throws Exception Si se produce algún error
	 */
	public static void ejecutarFichero(String fichero) throws Exception {
		   Runtime.getRuntime().exec("cmd /C start \"CetaFarma\" \"" + fichero + "\"");
	}

	/**
	 * Crea un fichero temporal a partir de un array de bytes
	 * @param fichero Nombre del fichero temporal
	 * @param bytes Array de bytes del fichero a crear
	 * @return El fichero temporal
	 * @throws IOException Si se produce algún error
	 */
//	public static File crearFicheroTemporal(String fichero, byte[] bytes) throws IOException {
//		String nombre = General.devNombreArchivo(fichero);
//		String extension = General.devExtensionArchivo(fichero);
//		File f = File.createTempFile(nombre + " ", "." + extension);
//
//		FileOutputStream fos = new FileOutputStream(f);
//		fos.write(bytes);
//		fos.flush();
//		fos.close();
//		return f;
//	}

	/**
	 * Crea una fichero en la carpeta de trabajo
	 * @param fichero
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static File crearFichero(String fichero, byte[] bytes) throws IOException {
		File f = new File(fichero);

		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bytes);
		fos.flush();
		fos.close();
		return f;
	}

	/**
	 * Escribe una cadena de texto en un fichero
	 * @param cadena Cadena de texto a escribir
	 */
//	public static void escribirLog(String texto) {
//		File f = new File("c:\\error.log");
//		BufferedWriter bw = null;
//		try {
//			bw = new BufferedWriter(new FileWriter(f, true));
//			bw.write(General.getFormattedDate(new Date(), "dd/MM/yyyy HH:mm:ss") + "\t" + texto + "\r\n\r\n");
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (bw != null) bw.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	/**
	 * Escribe una excepción en un fichero
	 * @param e Excepción
	 */
//	public static void escribirLog(Exception e) {
//		StringWriter sw = new StringWriter();
//	    PrintWriter pw = new PrintWriter(sw);
//	    e.printStackTrace(pw);
//	    escribirLog(e.getMessage() + "\r\n" + sw.toString());
//	}
	
	/**
	 * Escribe una cadena en un fichero
	 * @param fichero
	 * @param cadena 
	 */
	public static void escribirEnFichero(String fichero, String cadena) {
		escribirEnFichero(fichero, cadena, null);
	}	
	
	/**
	 * 
	 * @param fichero
	 * @param cadena
	 * @param charset Por defecto es ANSI
	 */
	public static void escribirEnFichero(String fichero, String cadena, Charset charset) {
		File f = new File(fichero);
		try {
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter fstream = charset == null ? new OutputStreamWriter(fos) : new OutputStreamWriter(fos, charset);
			fstream.write(cadena + "\r\n");
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	

	
	/**
	 * Escribe una cadena en un fichero
	 * @param fichero
	 * @param cadena 
	 */
	public static void eliminarFichero(String fichero) {
		File f = new File(fichero);
		try {
			f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	
	/**
	 * comprueba si existe un a ruta, si no la crea
	 * @param ruta
	 */	
	public static void comprobarCarpeta(String ruta) {
		//TODO Hacer que tambi�n cree las subcarpetas que no existan. 
		File f = new File(ruta);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Si hay más de 20 archivos en la carpeta los borramos
		String[] archivos = f.list();
		if (archivos.length > 10) {
			for (String a : archivos) {
				new File(ruta + File.separatorChar + a).delete();
			}
		}
	}
	
	public static void asegurarExistenciaRuta(String ruta){
		String[] carpetas;
		String rutaAComprobar = null;
		boolean esWindows;

		if (ruta.substring(0,3).equals("c:\\")){
			esWindows = true;
			carpetas = ruta.split("\\\\");
		}else{
			esWindows = false;
			carpetas = ruta.split("/");
		}
		
		if (esWindows){
			rutaAComprobar = carpetas[0];
		}else{
			rutaAComprobar = File.separatorChar + "";
		}
		
		for (int i = 0;i<carpetas.length;i++){
			if (esWindows){
				rutaAComprobar += File.separatorChar + carpetas[i + 1];
			}else{
				rutaAComprobar += File.separatorChar + carpetas[i];
			}
			File f = new File(rutaAComprobar + File.separatorChar);
			if (!f.exists()) {
				try {
					f.mkdir();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

//	public static void crearWord(File fichero)throws Exception{
//		byte[] doc = leerFichero(fichero.getAbsolutePath());
//		
//		File nuevo = crearFicheroTemporal(fichero.getName()+".zip", doc);
//		
//		Zip.descomnprimir(nuevo.getAbsolutePath());
//	}

	public static String devRutaWorkSpace(Object o) {
		String s = o.getClass().getClassLoader().getResource(
			String.valueOf(File.separatorChar)
		).getPath().split(".metadata")[0];
		
		try {
			s = URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static String devExtension(String nombre){
		String salida = nombre.substring(nombre.lastIndexOf("."));
		return salida;
	}

	public static void escribirSocket(String url, short puerto, String texto) throws UnknownHostException, IOException {
		Socket s = new Socket(url, puerto);
		PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
		pw.print(texto);
		pw.close();
		s.close();
	}
	
	public static String devPackageDeNombreClase(String nombreClase){
		// Buscamos el paquete que contiene nuestra clase
		Package[] packages = Package.getPackages();
	    String className = nombreClase;
	    String pack = null;
	    
	    for (final Package p : packages) {
	        pack = p.getName();
	        String tentative = pack + "." + className;
	        try {
	            Class.forName(tentative);
	        } catch (final ClassNotFoundException e) {
	            continue;
	        }
	        break;
	    }
	    return pack;
	}

	/**
	 * Crea un archivo temporal y devuelve la ruta del archivo
	 * Se puede obtener la ruta de la carpeta temporal de la siguiente forma:
	 * System.out.println(System.getProperty("java.io.tmpdir"));
	 * C:\Users\Levi\AppData\Local\Temp\
	 * 
	 * @param fichero
	 * @param id_empresa
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String guardar_archivo_temporal(String fichero, Integer id_empresa, InputStream is) throws IOException {
		// Creamos un archivo temporal con el prefijo de la empresa
		final File f = File.createTempFile(id_empresa + "_" + fichero.split("\\.")[0], "." + fichero.split("\\.")[1]);
		
		// Guardamos el archivo
		FileOutputStream fos = new FileOutputStream(f);
		InputStream pdf = is;
		byte[] bytes = new byte[1000];
		int leidos = 0;
		while ((leidos = pdf.read(bytes)) > 0) {
			fos.write(bytes, 0, leidos);
		}
		f.deleteOnExit();
		fos.close();
		
		return f.getName();
	}

	public static String leer_input_stream(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] bytes = new byte[1000];
		int leidos = 0;
		while ((leidos = in.read(bytes)) > 0) {
			baos.write(bytes, 0, leidos);
		}
		
		return baos.toString();
	}
}

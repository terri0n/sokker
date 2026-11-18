package com.formulamanager.sokker.auxiliares;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SystemUtil {
	private static final String DOMINIO = "https://raqueto.com";
	public static String REAL_PATH;	// Se inicializa en ServletContext.java
	public static String PATH = "path";
	public static String LOGIN = "login";
	public static String PASSWORD = "password";

	public static Date getCertificateExpirationDate(String url) {
        try {
            URL serverURL = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) serverURL.openConnection();
            connection.connect();

            Certificate[] certs = connection.getServerCertificates();

            if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) certs[0];
                return x509Certificate.getNotAfter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

	public static String mostrar_expiracion_certificado() {
		Date fecha = getCertificateExpirationDate(DOMINIO);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, 7);
		
		String salida = "<b>Certificado:</b> ";
		if (c.getTime().after(fecha)) {
//			salida += renewCertificate(DOMINIO);
			salida += "<span style='color: red'>" + Util.dateToString(fecha) + "</span><br />";
		} else {
			salida += Util.dateToString(fecha);
		}
		
		return salida;
	}

	/**
	 * Requiere permisos de root. Creo que mejor lo ejecuto a mano cada vez
	 * @param domain
	 * @return
	 */
	public static String renewCertificate(String domain) {
        try {
        		StringBuilder result = new StringBuilder();
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "certbot", "renew", "--nginx", "-d", domain);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                result.append("Certificado renovado exitosamente. ");
            } else {
                result.append("Error al renovar el certificado. Código de salida: ").append(exitCode).append("<br />");
            }
            return result.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage() + "</br>";
        }
    }

	public static void saveRealPath(ServletContext servletContext) {
		REAL_PATH = servletContext.getRealPath("/");
	}
	
	/**
	 * Logs live beside the Asistente data directory, never inside either
	 * SQLite data directory.
	 */
	public static String getLogsPath() {
		File dataDirectory = new File(getVar(PATH)).getAbsoluteFile();
		File parent = dataDirectory.getParentFile();
		File logs = new File(parent == null ? new File(".") : parent, "asistente-logs");
		if (!logs.isDirectory() && !logs.mkdirs() && !logs.isDirectory()) {
			throw new IllegalStateException("Cannot create logs directory: " + logs);
		}
		return logs.getAbsolutePath() + File.separator;
	}

	public static void migrateLegacyLogs() throws IOException {
		File dataDirectory = new File(getVar(PATH)).getAbsoluteFile();
		File legacyDirectory = new File(dataDirectory, "logs");
		if (!legacyDirectory.isDirectory()) return;
		File targetDirectory = new File(getLogsPath());
		moveLogContents(legacyDirectory, targetDirectory);
		if (legacyDirectory.exists() && !legacyDirectory.delete()) {
			throw new IOException("Cannot remove empty legacy log directory: " + legacyDirectory);
		}
	}

	private static void moveLogContents(File sourceDirectory, File targetDirectory) throws IOException {
		File[] children = sourceDirectory.listFiles();
		if (children == null) throw new IOException("Cannot list legacy log directory: " + sourceDirectory);
		if (!targetDirectory.isDirectory() && !targetDirectory.mkdirs() && !targetDirectory.isDirectory()) {
			throw new IOException("Cannot create log directory: " + targetDirectory);
		}
		for (File source : children) {
			File target = new File(targetDirectory, source.getName());
			if (source.isDirectory()) {
				moveLogContents(source, target);
				if (!source.delete()) throw new IOException("Cannot remove legacy log directory: " + source);
				continue;
			}
			if (target.exists()) {
				if (java.util.Arrays.equals(java.nio.file.Files.readAllBytes(source.toPath()),
						java.nio.file.Files.readAllBytes(target.toPath()))) {
					if (!source.delete()) throw new IOException("Cannot remove duplicate legacy log: " + source);
					continue;
				}
				String base = target.getName() + ".legacy-" + System.currentTimeMillis();
				target = new File(targetDirectory, base);
				int suffix = 1;
				while (target.exists()) target = new File(targetDirectory, base + "-" + suffix++);
			}
			java.nio.file.Files.move(source.toPath(), target.toPath());
		}
	}

	public static String getVar(String variable) {
        String tomcatPath = new File(REAL_PATH).getParentFile().getParent();

        try {
            File serverXmlFile = new File(tomcatPath, "conf/server.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(serverXmlFile);

            doc.getDocumentElement().normalize();

            NodeList variables = doc.getElementsByTagName("Environment");

            for (int i = 0; i < variables.getLength(); i++) {
                Node variableNode = variables.item(i);
                if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
                    String variableName = variableNode.getAttributes().getNamedItem("name").getNodeValue();
                    String variableValue = variableNode.getAttributes().getNamedItem("value").getNodeValue();
                    if (variableName.equals(variable)) {
                    	return variableValue;
                    }
                }
            }
            throw new RuntimeException(variable + " not found in " + tomcatPath + "conf/server.xml");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error accessing " + variable + " in " + tomcatPath + "conf/server.xml: " + e.getMessage());
        }
    }
}

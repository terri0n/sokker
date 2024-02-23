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
		if (c.after(fecha)) {
			salida += renewCertificate(DOMINIO);
			salida += "<span style='color: red'>" + Util.dateToString(fecha) + "</span><br />";
		} else {
			salida += Util.dateToString(fecha);
		}
		
		return salida;
	}
	
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
                result.append("Certificado renovado exitosamente.");
            } else {
                result.append("Error al renovar el certificado. Código de salida: ").append(exitCode);
            }
            return result.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

	public static void saveRealPath(ServletContext servletContext) {
		REAL_PATH = servletContext.getRealPath("/");
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

package com.formulamanager.sokker.auxiliares;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
 
public class EmailSenderService {
	private static String SMTP = "smtp.mailgun.org";
	
	private static final Properties properties = new Properties();
	private static Session session;
 
	private static void init() {
 		properties.put("mail.smtp.host", SMTP);
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.port", 587);
		properties.put("mail.smtp.user", SystemUtil.getVar("mailgun_login"));
		properties.put("mail.smtp.auth", "true");
		
		properties.put("mail.debug", "true");
		properties.put("mail.smtps.localhost", "localhost");
 
		session = Session.getDefaultInstance(properties);
	}
 
	public static void sendEmail(String email, String asunto, String texto, HashMap<String, byte[]> listaArchivos) throws AddressException, MessagingException, UnsupportedEncodingException {
 		init();

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress((String)properties.get("mail.smtp.user"), "Sokker Asistente"));
		message.setSender(new InternetAddress((String)properties.get("mail.smtp.user"), "Sokker Asistente"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		message.setSubject(asunto);
		message.setText(texto);
		
		if (listaArchivos != null) {
			// Para a�adir el texto
			Multipart multipart = new MimeMultipart();
			BodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(texto,"text/html");
			htmlPart.setDisposition(BodyPart.INLINE);
			multipart.addBodyPart(htmlPart);
	
			// A�adimos los archivos
			Iterator<Entry<String, byte[]>> it = listaArchivos.entrySet().iterator();
			while(it.hasNext()){
				@SuppressWarnings("rawtypes")
				Map.Entry e = (Map.Entry)it.next();
				String mimeType = MimeType.getMimeType(devExtension(e.getKey().toString()));					
				
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setDataHandler(
					new DataHandler(new ByteArrayDataSource((byte[])e.getValue(), mimeType))
				);
				messageBodyPart.setFileName(e.getKey().toString());
				multipart.addBodyPart(messageBodyPart);
			}
			
			message.setContent(multipart);
		}		

		Transport t = session.getTransport("smtp");
		t.connect(SMTP, (String)properties.get("mail.smtp.user"), SystemUtil.getVar("mailgun_password"));
		t.sendMessage(message, message.getAllRecipients());
		t.close();
	}

	public static String devExtension(String nombre){
		return nombre.substring(nombre.lastIndexOf(".") + 1);
	}
}
package com.formulamanager.multijuegos.util;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
 
public class EmailSenderService {
	private static String USUARIO = "postmaster@sandboxaaec920564254deeb875b296fa63355e.mailgun.org"; //"tejedor@gmail.com";
	private static String PASSWORD = "fd46026c61f17a35b0cb16ec7080ad53-8ed21946-88487e1f"; //"gwyvern79";
	private static String SMTP = "smtp.mailgun.org"; //"smtp.gmail.com"*
	
	private static final Properties properties = new Properties();
	private static Session session;
 
	private static void init() {
 		properties.put("mail.smtp.host", SMTP);
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.port", 587);
		properties.put("mail.smtp.user", USUARIO);
		properties.put("mail.smtp.auth", "true");
		
		properties.put("mail.debug", "true");
		properties.put("mail.smtps.localhost", "localhost");
 
		session = Session.getDefaultInstance(properties);
	}
 
	public static void sendEmail(String email, String asunto, String texto) throws AddressException, MessagingException, UnsupportedEncodingException {
 		init();

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress((String)properties.get("mail.smtp.user"), "ChessGoal"));
		message.setSender(new InternetAddress((String)properties.get("mail.smtp.user"), "ChessGoal"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		message.setSubject(asunto);
		message.setContent(texto, "text/html");

		Transport t = session.getTransport("smtp");
		t.connect(SMTP, (String)properties.get("mail.smtp.user"), PASSWORD);
		t.sendMessage(message, message.getAllRecipients());
		t.close();
	}
 
}
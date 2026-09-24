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
    private static final String SMTP = "smtp.mailgun.org";
    private static final Properties properties = new Properties();
    private static Session session;

    private static void init() {
        properties.put("mail.smtp.host", SMTP);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.user", SystemUtil.getVar("mailgun_login"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.debug", "false");
        properties.put("mail.smtps.localhost", "localhost");
        session = Session.getDefaultInstance(properties);
    }

    public static void sendEmail(String email, String asunto, String texto,
            HashMap<String, byte[]> listaArchivos)
            throws AddressException, MessagingException, UnsupportedEncodingException {
        send(email, asunto, texto, listaArchivos, listaArchivos != null);
    }

    public static void sendHtmlEmail(String email, String asunto, String html)
            throws AddressException, MessagingException, UnsupportedEncodingException {
        send(email, asunto, html, null, true);
    }

    private static void send(String email, String asunto, String texto,
            HashMap<String, byte[]> listaArchivos, boolean html)
            throws AddressException, MessagingException, UnsupportedEncodingException {
        init();

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress((String) properties.get("mail.smtp.user"), "Sokker Asistente"));
        message.setSender(new InternetAddress((String) properties.get("mail.smtp.user"), "Sokker Asistente"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(asunto, "UTF-8");

        if (listaArchivos == null) {
            if (html) {
                message.setContent(texto, "text/html; charset=UTF-8");
            } else {
                message.setText(texto, "UTF-8");
            }
        } else {
            Multipart multipart = new MimeMultipart();
            BodyPart textPart = new MimeBodyPart();
            if (html) {
                textPart.setContent(texto, "text/html; charset=UTF-8");
            } else {
                textPart.setText(texto);
            }
            textPart.setDisposition(BodyPart.INLINE);
            multipart.addBodyPart(textPart);

            Iterator<Entry<String, byte[]>> it = listaArchivos.entrySet().iterator();
            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry) it.next();
                String mimeType = MimeType.getMimeType(devExtension(entry.getKey().toString()));
                BodyPart attachment = new MimeBodyPart();
                attachment.setDataHandler(new DataHandler(
                        new ByteArrayDataSource((byte[]) entry.getValue(), mimeType)));
                attachment.setFileName(entry.getKey().toString());
                multipart.addBodyPart(attachment);
            }
            message.setContent(multipart);
        }

        Transport transport = session.getTransport("smtp");
        transport.connect(SMTP, (String) properties.get("mail.smtp.user"), SystemUtil.getVar("mailgun_password"));
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    public static String devExtension(String nombre) {
        return nombre.substring(nombre.lastIndexOf(".") + 1);
    }
}

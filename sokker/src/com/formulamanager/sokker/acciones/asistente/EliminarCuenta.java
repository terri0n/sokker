package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.EmailSenderService;
import com.formulamanager.sokker.auxiliares.SystemUtil;

@WebServlet("/asistente/eliminar_cuenta")
public class EliminarCuenta extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static boolean isSafeLogin(String value) {
        if (value == null) return false;
        String login = value.trim();
        if (login.length() < 1 || login.length() > 100) return false;
        for (int i = 0; i < login.length(); i++) {
            char c = login.charAt(i);
            if (c == '/' || c == '\\' || c == ',' || Character.isISOControl(c)) return false;
        }
        return true;
    }

    static String buildAdminReviewUrl(String contextPath, String login) throws IOException {
        String ctx = contextPath == null ? "" : contextPath;
        return "https://raqueto.com" + ctx + "/asistente/eliminar_cuenta_admin?usuario="
                + URLEncoder.encode(login.trim().toLowerCase(), "UTF-8");
    }

    static String buildDeletionEmailHtml(String login, String contact, String message, String contextPath) throws IOException {
        String safeLogin = escapeHtml(login == null ? "" : login.trim());
        String safeContact = escapeHtml(contact == null ? "" : contact.trim());
        String safeMessage = escapeHtml(message == null ? "" : message.trim()).replace("\n", "<br/>");
        String review = buildAdminReviewUrl(contextPath, login == null ? "" : login);
        return "<h2>Sokker Asistente - account deletion request</h2>"
                + "<p><strong>Login:</strong> " + safeLogin + "</p>"
                + "<p><strong>Contact:</strong> " + safeContact + "</p>"
                + "<p><strong>Message:</strong><br/>" + safeMessage + "</p>"
                + "<p><strong>Received:</strong> " + escapeHtml(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date())) + "</p>"
                + "<p><a href=\"" + escapeHtml(review) + "\">Review and delete this account</a></p>";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/asistente/eliminar_cuenta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String login = request.getParameter("login");
        String contact = trimToMax(request.getParameter("contact"), 254);
        String message = trimToMax(request.getParameter("message"), 2000);

        if (!isSafeLogin(login)) {
            request.setAttribute("requestReceived", Boolean.TRUE);
            request.getRequestDispatcher("/jsp/asistente/eliminar_cuenta.jsp").forward(request, response);
            return;
        }

        String recipient = SystemUtil.getVar("account_deletion_email");
        if (recipient == null || recipient.trim().isEmpty()) {
            recipient = "tejedor@gmail.com";
        }

        try {
            EmailSenderService.sendHtmlEmail(recipient,
                    "Sokker Asistente account deletion request: " + login.trim(),
                    buildDeletionEmailHtml(login, contact, message, request.getContextPath()));
        } catch (MessagingException e) {
            throw new ServletException("Could not send account deletion request", e);
        }

        request.setAttribute("requestReceived", Boolean.TRUE);
        request.getRequestDispatcher("/jsp/asistente/eliminar_cuenta.jsp").forward(request, response);
    }

    private static String trimToMax(String value, int max) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

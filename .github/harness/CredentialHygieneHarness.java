package com.formulamanager.sokker.acciones.asistente;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.Cookie;

public final class CredentialHygieneHarness {
    private CredentialHygieneHarness() {}

    public static void main(String[] args) throws Exception {
        legacyPasswordCookieIsExpiredSafely();
        sourceContainsNoCleartextPasswordRetention();
    }

    private static void legacyPasswordCookieIsExpiredSafely() {
        Cookie cookie = Login.expiredLegacyPasswordCookie("/sokker");
        require("apassword".equals(cookie.getName()), "Wrong legacy cookie name");
        require("".equals(cookie.getValue()), "Legacy password cookie was not blanked");
        require(cookie.getMaxAge() == 0, "Legacy password cookie was not expired");
        require("/sokker/asistente".equals(cookie.getPath()), "Legacy cookie path would not match old cookie scope");
        require(cookie.isHttpOnly(), "Expired legacy password cookie is not HttpOnly");
        require(cookie.getSecure(), "Expired legacy password cookie is not Secure");
    }

    private static void sourceContainsNoCleartextPasswordRetention() throws Exception {
        String loginJsp = read("sokker/WebContent/jsp/asistente/login.jsp");
        String assistantJsp = read("sokker/WebContent/jsp/asistente/asistente.jsp");
        String login = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java");
        String exception = read("sokker/src/com/formulamanager/sokker/auxiliares/LoginExceptionExt.java");
        String servlet = read("sokker/src/com/formulamanager/sokker/auxiliares/SERVLET_ASISTENTE.java");
        String browser = read("sokker/src/com/formulamanager/sokker/auxiliares/Navegador.java");

        forbid(loginJsp, "cookie.apassword", "Login JSP still reads the cleartext password cookie");
        forbid(loginJsp, "getCookie(request, \"apassword\")", "Login JSP still pre-fills from the password cookie");
        forbid(assistantJsp, "cookie.apassword", "Assistant JSP still pre-fills Sokker passwords from the password cookie");
        forbid(exception, "private String contrasenya", "LoginExceptionExt still stores the submitted password");
        forbid(exception, "getContrasenya()", "LoginExceptionExt still exposes the submitted password");
        forbid(servlet, "getContrasenya()", "SERVLET_ASISTENTE still logs the submitted password");
        forbid(login, "new Cookie(\"apassword\", apassword)", "Login still writes the submitted password cookie directly");
        forbid(login, "new Cookie(\"apassword\", URLEncoder.encode(apassword", "Login still writes the submitted password cookie encoded");
        forbid(login, "new LoginExceptionExt(Util.getTexto(request.getLocale().getLanguage(), \"messages.login_error\"), alogin, apassword)",
                "Login still passes the submitted password into LoginExceptionExt");
        forbid(browser, "new LoginExceptionExt(\"Error when logging in to Sokker: bad password\", login, password)",
                "Navegador still passes the bad Sokker password into LoginExceptionExt");
        forbid(browser, "new LoginExceptionExt(\"Error when logging in to Sokker: user has no team\", login, password)",
                "Navegador still passes the Sokker password into LoginExceptionExt for no-team errors");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void forbid(String haystack, String needle, String message) {
        require(!haystack.contains(needle), message + ": " + needle);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

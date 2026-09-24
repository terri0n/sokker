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
        rememberPasswordIsExplicitAndBrowserLocal();
        rememberedAssistantPasswordPrefillsSokkerUpdateForSameUser();
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

    private static void rememberPasswordIsExplicitAndBrowserLocal() throws Exception {
        String loginJsp = read("sokker/WebContent/jsp/asistente/login.jsp");
        String bootstrap = read("sokker/WebContent/js/ip.js.jsp");
        String util = read("sokker/WebContent/js/util.js");

        int bootstrapScript = loginJsp.indexOf("/js/ip.js.jsp");
        int utilScript = loginJsp.indexOf("/js/util.js");
        require(bootstrapScript >= 0 && utilScript > bootstrapScript,
                "Localized login bootstrap must load before util.js");
        require(bootstrap.contains("SOKKER_ASSISTANT_REMEMBER_PASSWORD_LABEL"),
                "Localized remember-password label is not exposed to browser code");
        require(bootstrap.contains("login.remember_password"),
                "Remember-password label is not sourced from the i18n bundle");

        require(util.contains("initAssistantRememberPassword"),
                "Browser-local remember-password initialization is missing");
        require(util.contains("SOKKER_ASSISTANT_REMEMBER_PASSWORD_LABEL"),
                "Remember-password UI does not use the localized label");
        require(util.contains("name: \"recordar\""), "Remember-password opt-in checkbox is missing");
        require(util.contains("autocomplete\", \"username\""),
                "Login field does not expose username autocomplete semantics");
        require(util.contains("autocomplete\", \"current-password\""),
                "Password field does not expose current-password autocomplete semantics");
        require(util.contains("localStorage.setItem"),
                "Remembered password is not stored in browser-local storage");
        require(util.contains("localStorage.removeItem"),
                "Unchecking remember-password does not clear browser-local storage");
        require(util.contains("document.cookie"), "Legacy password-cookie migration is missing");
        require(util.contains("getAssistantLegacyCookie(\"apassword\")"),
                "Legacy apassword cookie is not handled during migration");
    }

    private static void rememberedAssistantPasswordPrefillsSokkerUpdateForSameUser() throws Exception {
        String util = read("sokker/WebContent/js/util.js");

        require(util.contains("initAssistantSokkerPassword"),
                "Sokker update password initialization is missing");
        require(util.contains("form[action$='/asistente/actualizar']"),
                "Sokker update form is not located safely");
        require(util.contains("getAssistantLegacyCookie(\"alogin\")"),
                "Current Assistant login is not checked before reusing the remembered password");
        require(util.contains("rememberedLogin !== assistantLogin"),
                "Remembered password is not restricted to the same Assistant user");
        require(util.contains("$form.find(\"#ipassword\").val(password)"),
                "Remembered Assistant password is not copied into the Sokker password field");
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

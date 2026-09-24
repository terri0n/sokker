package com.formulamanager.sokker.acciones.asistente;

public final class PublicDeletionRequestHarness {
    private PublicDeletionRequestHarness() {}

    public static void main(String[] args) throws Exception {
        require(EliminarCuenta.isSafeLogin("alice"), "Normal login rejected");
        require(EliminarCuenta.isSafeLogin(" Alice "), "Trimmed normal login rejected");
        require(!EliminarCuenta.isSafeLogin("../../x"), "Traversal login accepted");
        require(!EliminarCuenta.isSafeLogin("a/b"), "Slash login accepted");
        require(!EliminarCuenta.isSafeLogin("a\\b"), "Backslash login accepted");
        require(!EliminarCuenta.isSafeLogin("a,b"), "Comma login accepted");
        require(!EliminarCuenta.isSafeLogin("a\r\nb"), "Control characters accepted");

        String url = EliminarCuenta.buildAdminReviewUrl("/sokker", "a b");
        require("https://raqueto.com/sokker/asistente/eliminar_cuenta_admin?usuario=a+b".equals(url), "Admin link is wrong: " + url);

        String html = EliminarCuenta.buildDeletionEmailHtml("<alice>", "x@example.com", "<b>hello</b>", "/sokker");
        require(!html.contains("<alice>"), "Login was not HTML-escaped");
        require(!html.contains("<b>hello</b>"), "Message was not HTML-escaped");
        require(html.contains("eliminar_cuenta_admin"), "Email has no direct admin review link");
        require(!html.toLowerCase().contains("password"), "Deletion email mentions credentials");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

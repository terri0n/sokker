<%@ page import="java.io.File" %>
<%@ page import="java.util.UUID" %>
<%@ page import="com.formulamanager.sokker.auxiliares.SystemUtil" %>
<%@ page import="com.formulamanager.sokker.auxiliares.TrainingAgeSeasonRepair" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%!
    private String escapeHtmlRepair(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/asistente");
        return;
    }

    File dataDirectory = new File(SystemUtil.getVar(SystemUtil.PATH));
    String result = null;
    String error = null;
    String csrf = (String) session.getAttribute("repairTrainingAgesCsrf");

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String supplied = request.getParameter("csrf");
        if (csrf == null || supplied == null || !csrf.equals(supplied)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        session.removeAttribute("repairTrainingAgesCsrf");

        try {
            TrainingAgeSeasonRepair.RepairSummary summary = TrainingAgeSeasonRepair.repairDirectory(dataDirectory);
            result = "Reparación terminada. Ficheros revisados: " + summary.scannedFiles
                    + ", ficheros modificados: " + summary.modifiedFiles
                    + ", jugadores corregidos: " + summary.modifiedPlayers
                    + ", edades corregidas: " + summary.modifiedSnapshots + ".";
            if (summary.concurrentSkips > 0) {
                result += " " + summary.concurrentSkips
                        + " fichero(s) cambiaron mientras se reparaban y se dejaron intactos; vuelve a ejecutar la reparación.";
            }
        } catch (Throwable e) {
            error = e.getClass().getName() + ": " + e.getMessage();
        }
    }

    if (!"POST".equalsIgnoreCase(request.getMethod()) || result != null || error != null) {
        csrf = UUID.randomUUID().toString();
        session.setAttribute("repairTrainingAgesCsrf", csrf);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reparar edades de entrenamientos</title>
</head>
<body>
<% if (error != null) { %>
    <h2>Error</h2>
    <pre><%= escapeHtmlRepair(error) %></pre>
<% } else if (result != null) { %>
    <h2><%= escapeHtmlRepair(result) %></h2>
<% } %>

<h2>Reparar edades de la temporada anterior</h2>
<p>La edad del snapshot más reciente se toma como edad actual.</p>
<p>Si el equipo ya se actualizó esta semana, la referencia es la jornada 1210. Si todavía no se actualizó, la referencia es la jornada 1209 y esa fila se conserva como estado actual.</p>
<p>Solo se revisan snapshots anteriores de la temporada 1197-1209. Su edad debe ser exactamente edad actual - 1. No se toca ninguna otra temporada ni ningún otro campo.</p>
<p>La reparación es idempotente y puede volver a ejecutarse. Esto permite repetirla después de que se actualicen equipos que todavía tenían la jornada 1209 como snapshot actual.</p>
<p>No utiliza backups como referencia y no crea ficheros de backup.</p>
<form method="post">
    <input type="hidden" name="csrf" value="<%= escapeHtmlRepair(csrf) %>">
    <button type="submit">Ejecutar reparación</button>
</form>
</body>
</html>

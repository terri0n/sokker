<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><fmt:message key="privacy.title" /></title>
</head>
<body>
    <h1><fmt:message key="privacy.title" /></h1>

    <h2><fmt:message key="privacy.account_data" /></h2>
    <p>Sokker Asistente stores the account login, the hashed password representation used by the service, preferences, notes and training configuration required to provide the application. Clear-text passwords are used only for the requested authentication or Sokker operation and are not intentionally retained by the application.</p>

    <h2><fmt:message key="privacy.sokker_data" /></h2>
    <p>The service downloads and stores team, player, junior, training and related Sokker data needed to provide historical analysis and the features requested by the user.</p>

    <h2><fmt:message key="privacy.logs" /></h2>
    <p>The service keeps per-user and security/diagnostic logs needed to operate and troubleshoot the application. Shared security and diagnostic logs are retained for up to 30 days under the current maintenance process.</p>

    <h2><fmt:message key="privacy.google_services" /></h2>
    <p>The main Sokker Asistente pages loaded in the controlled Android WebView include Google advertising and analytics services. Google may process browser, device, network and advertising identifiers according to Google's own policies. This privacy page and the public deletion-request page do not load those advertising or analytics scripts.</p>

    <h2><fmt:message key="privacy.retention" /></h2>
    <p>Account and team data is kept while it is required to provide the service. Security or diagnostic information may remain temporarily for the retention period described above.</p>

    <h2><fmt:message key="privacy.deletion" /></h2>
    <p>A logged-in user can request deletion from Sokker Asistente. A request can also be sent without the app through the public deletion page. When deletion is confirmed by the administrator, account-specific data is removed. Club data is removed when no other active account references the same club. Shared national-team datasets are not treated as the personal data of one selector.</p>
    <p><a href="${pageContext.request.contextPath}/asistente/eliminar_cuenta"><fmt:message key="account.delete.request" /></a></p>

    <h2><fmt:message key="privacy.contact" /></h2>
    <p>For privacy or account-deletion requests, use the public account-deletion form linked above so the request reaches the Sokker Asistente administrator.</p>

    <p><a href="${pageContext.request.contextPath}/asistente"><fmt:message key="common.back" /></a></p>
</body>
</html>

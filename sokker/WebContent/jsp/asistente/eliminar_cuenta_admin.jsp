<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><fmt:message key="account.delete.admin.title" /></title>
</head>
<body>
    <h1><fmt:message key="account.delete.admin.title" /></h1>
    <p><strong>Login:</strong> <c:out value="${deletionManifest.login}" /></p>
    <p><strong>TID:</strong> <c:out value="${deletionManifest.tid}" /></p>
    <p>
        <c:choose>
            <c:when test="${deletionManifest.deleteTeamData}">Club data will be removed because no other active account references this TID.</c:when>
            <c:otherwise>Club data will be preserved because another active account references this TID.</c:otherwise>
        </c:choose>
    </p>
    <h2>Files</h2>
    <ul>
        <c:forEach items="${deletionFiles}" var="file">
            <li><c:out value="${file}" /></li>
        </c:forEach>
    </ul>
    <p>NTDB/scout references will be removed from: <c:out value="${deletionManifest.scoutOwnersReferencingLogin}" /></p>

    <form method="post" action="${pageContext.request.contextPath}/asistente/eliminar_cuenta_admin">
        <label><fmt:message key="account.delete.admin.confirm" />:
            <input type="text" name="confirm_login" autocomplete="off" required="required" />
        </label>
        <button type="submit"><fmt:message key="account.delete.admin.button" /></button>
    </form>
    <p><a href="${pageContext.request.contextPath}/asistente">Cancel</a></p>
</body>
</html>

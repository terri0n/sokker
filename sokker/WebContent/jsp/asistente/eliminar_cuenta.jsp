<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><fmt:message key="account.delete.public.title" /></title>
</head>
<body>
    <h1><fmt:message key="account.delete.public.title" /></h1>
    <c:choose>
        <c:when test="${requestReceived}">
            <p><fmt:message key="account.delete.public.received" /></p>
        </c:when>
        <c:otherwise>
            <p><fmt:message key="account.delete.public.explanation" /></p>
            <form method="post" action="${pageContext.request.contextPath}/asistente/eliminar_cuenta">
                <p><label><fmt:message key="account.delete.public.login" />
                    <input type="text" name="login" maxlength="100" required="required" autocomplete="username" />
                </label></p>
                <p><label><fmt:message key="account.delete.public.contact" />
                    <input type="email" name="contact" maxlength="254" autocomplete="email" />
                </label></p>
                <p><label><fmt:message key="account.delete.public.message" /><br/>
                    <textarea name="message" maxlength="2000" rows="6" cols="50"></textarea>
                </label></p>
                <button type="submit"><fmt:message key="account.delete.public.submit" /></button>
            </form>
        </c:otherwise>
    </c:choose>
    <p><a href="${pageContext.request.contextPath}/asistente/privacidad"><fmt:message key="privacy.title" /></a></p>
    <p><a href="${pageContext.request.contextPath}/asistente"><fmt:message key="common.back" /></a></p>
</body>
</html>

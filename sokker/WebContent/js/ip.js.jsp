<%@page import="java.util.Date"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
response.setHeader("Pragma", "no-cache");
%>

<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />
window.SOKKER_ASSISTANT_REMEMBER_PASSWORD_LABEL = '<fmt:message key="login.remember_password" />';

<c:if test="${empty sessionScope.usuario}">
	<% request.getSession().setAttribute("timestamp", new Date().getTime()); %>
	$.get('https://www.cloudflare.com/cdn-cgi/trace', function(data) {
		$.ajax({
			url: "${pageContext.request.contextPath}/servlet/ip?" + data.split('\n')[2]
		});
	});
</c:if>
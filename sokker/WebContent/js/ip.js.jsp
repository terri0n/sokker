<%@page import="java.util.Date"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
response.setHeader("Pragma", "no-cache");
%>

<c:if test="${empty sessionScope.usuario}">
	<% request.getSession().setAttribute("timestamp", new Date().getTime()); %>
	$.get('https://www.cloudflare.com/cdn-cgi/trace', function(data) {
		$.ajax({
			url: "${pageContext.request.contextPath}/servlet/ip?" + data.split('\n')[2]
		});
	});
</c:if>
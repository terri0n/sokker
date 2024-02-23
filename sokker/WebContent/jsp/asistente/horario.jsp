<%@page import="java.net.URL"%>
<%@page import="java.net.URI"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlDivision"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlButton"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlAnchor"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlElement"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.SortedMap"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlBold"%>
<%@page import="java.util.List"%>
<%@page import="com.gargoylesoftware.htmlunit.html.DomNodeList"%>
<%@page import="com.gargoylesoftware.htmlunit.html.DomNode"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.gargoylesoftware.htmlunit.html.DomElement"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlSubmitInput"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlForm"%>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlPage"%>
<%@page import="com.gargoylesoftware.htmlunit.WebClient"%>
<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>

<head>
	<link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/asistente.css?4">
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>Sokker - NTs schedule</title>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>

	<style>
		th, td {
			padding: 3px;
		}
	</style>
</head>

<body>
	<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto;">
		<%@include file="ntdb_menu.jsp" %>

		<h2 class="texto_3d">
			<i class="fas fa-running" style="opacity: 0.1; font-size: 0.4em"></i>
			<i class="fas fa-running" style="opacity: 0.25; font-size: 0.55em"></i>
			<i class="fas fa-running" style="opacity: 0.4; font-size: 0.7em"></i>
			<i class="fas fa-futbol" style="opacity: 0.55; font-size: 0.15em"></i>
			<c:out value="${param.tid == 0 ? 'NTs' : 'U21s'} schedule" />
			<i class="fas fa-futbol" style="opacity: 0.55; font-size: 0.15em"></i>
			<i class="fas fa-running fa-flip-horizontal" style="opacity: 0.4; font-size: 0.7em"></i>
			<i class="fas fa-running fa-flip-horizontal" style="opacity: 0.25; font-size: 0.55em"></i>
			<i class="fas fa-running fa-flip-horizontal" style="opacity: 0.1; font-size: 0.4em"></i>
		</h2>
		
		<br />
	
		<%-------------%>
		<%-- EQUIPOS --%>
		<%-------------%>

		<div style="text-align: center;">
 			<table class="fondo_tabla tabla">
				<thead>
					<tr>
						<th>&nbsp;</th>
						<th>Home</th>
						<th>Away</th>
						<th>Time</th>
						<th>Score</th>
					</tr>
				</thead>
				
 				<c:forEach var="p" items="${partidos}" varStatus="var">
 					<tr>
 						<td align="right"><c:out value="${var.count}" /></td>
 						<td align="left">
 							<img src="https://files.sokker.org/pic/flags/${p.local.tid % 400}.png" />
 							<a target="blank_" href="https://sokker.org/glowna/teamID/${p.local.tid}"><c:out value="${p.local.nombre}" /></a>
 						</td>
 						<td align="left">
 							<img src="https://files.sokker.org/pic/flags/${p.visitante.tid % 400}.png" />
 							<a target="blank_" href="https://sokker.org/glowna/teamID/${p.visitante.tid}"><c:out value="${p.visitante.nombre}" /></a>
 						</td>
 						<td><fmt:formatDate value="${p.fecha}" pattern="HH:mm" /></td>
 						<td>
								<a target="blank_" href="https://sokker.org/${empty p.goles_l ? 'studio' : 'comment'}/matchID/${p.mid}"><b><c:out value="${p.goles_l} : ${p.goles_v}" /></b></a>
 						</td>
 					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.formulamanager.sokker.acciones.scanner.Scanner"%>
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>

<head>
	<link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>Sokker - Escaneador de jugadores</title>
	
	<style>
		body {
			margin: 0px;
			padding: 0px;
			background-color: #e2f2f8; 
		}
		label, .flecha {
			cursor: pointer;
		}
		a, a:visited {
			color: black;
			decoration: none;
		}
		a:hover {
			color: blue;
		}
		.campo input {
			opacity: 0.5;
		}
		hr {
			background-color: #4f8ea2; height: 1px; border: 0;
		}
		.error {
			color: red;
		}
		thead {
			background-color: #4f8ea2;
			color: white;
		}
		.GK, .GK a {
			color: blue;
			font-weight: bold;
		}
		.DEF, .DEF a {
			color: green;
			font-weight: bold;
		}
		.MID, .MID a {
			color: #A52A2A; /* Brown */
			font-weight: bold;
		}
		.ATT, .ATT a {
			color: red;
			font-weight: bold;
		}
		th {
			font-weight: bold;
		}
		.gris {
			color: gray;
		}
		.estrella {
			color: #FFD700; /* Gold */
		}
		.estrella_sup {
			color: #006400; /* DarkGreen */
		}
		.historial {
			color: lightblue;
		}
		.transp {
			color: transparent;
		}
	</style>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>
</head>

<body>
	<div style="background-color: #4f8ea2; color: white; padding: 1px; text-align: center;">
		<b>Sokker - Escaneador de jugadores</b>
	</div>

	<div style="vertical-align: top; padding: 1px;" align="center">
		<br/>
		<form method="post">
			<table>
				<tr>
					<td>Origen jugador:</td>
					<td>
						<select name="id_country_origen">
							<c:forEach items="${paises}" var="pais">
								<option value="<c:out value="${pais.id}" />" <c:out value="${pais.id == param.id_country_origen ? 'selected' : ''}" />>
									<c:out value="${pais.nombre}" />
								</option>
							</c:forEach>
						</select>
					</td>
				</tr>
	
				<tr>
					<td>Filtrar país:</td>
					<td>
						<select name="id_country_filtro">
							<option value="">TODOS</option>
			
							<c:forEach items="${paises}" var="pais">
								<option value="<c:out value="${pais.id}" />" <c:out value="${pais.id == param.id_country_filtro ? 'selected' : ''}" />>
									<c:out value="${pais.nombre}" />
								</option>
							</c:forEach>
						</select>
					</td>
				</tr>
	
				<tr>
					<td>Edad máxima:</td>
					<td>
						<input type="text" value="${param.edad}" name="edad" size="1" />
					</td>
				</tr>
	
				<tr>
					<td>Valor mínimo:</td>
					<td>
						<input type="text" value="${param.valor}" name="valor" size="1" />
						<span class="gris">En blanco = 60.000 * (min(30, edad) - 15)</span>
					</td>
				</tr>

				<tr>
					<td colspan="2">
						<input type="checkbox" <c:out value="${param.solo_copa == 'on' ? 'checked' : ''}" /> name="solo_copa" id="solo_copa" />
						<label for="solo_copa">Solo equipos que juegan la copa</label>
					</td>
				</tr>
			</table>
				
			<input type="submit" value="Buscar">
		</form>
	
		<br/>

		<%---------------%>
		<%-- JUGADORES --%>
		<%---------------%>

		<div style="text-align: center;">
			<div style="background-color: lightblue; display: inline-block;">
	 			<table style="margin: auto;">
					<thead>
						<tr>
							<th>&nbsp;</th>
							<th>País</th>
							<th>Nombre</th>
							<th>Edad</th>
							<th>Valor</th>
							<th>Demarcación</th>
							<th>Rating</th>
						</tr>
					</thead>

					<c:if test="${not empty param.id_country_origen}">
						<% Scanner.scan(request, out); %>
					</c:if>
					
<%-- 	 				<c:forEach var="j" items="${jugadores}" varStatus="var">
	 					<tr class="${j.demarcacion}">
	 						<td align="right"><c:out value="${var.count}" /></td>
	 						<td><a href="https://sokker.org/player/PID/<c:out value="${j.pid}" />"><c:out value="${j.nombre}" /></a></td>
	 						<td><c:out value="${j.edad}" /></td>
	 						<td align="right"><fmt:formatNumber value="${j.valor}" /></td>
	 						<td><c:out value="${j.despDemarcacion(j.demarcacion)}" escapeXml="false" /></td>
	 						<td><c:out value="${j.puntos}" /></td>
	 					</tr>
					</c:forEach>
 --%>				</table>
			</div>
			
			<div>
				IDs:<br/>			
				<textarea cols="40" rows="10">
					<c:forEach var="j" items="${jugadores}" varStatus="var"><c:out value="${j.pid}" /> </c:forEach>
				</textarea>
			</div>
		</div>
	</div>

</body>
</html>

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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>

<head>
	<link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>Sokker - Equipos por fecha de creación</title>
	
	<style>
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
		.incr {
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
	<div style="background-color: #4f8ea2; color: white; margin: 1px; text-align: center;">
		<b>Sokker - Equipos por fecha de creación</b>
	</div>

	<div style="vertical-align: top; background-color: #e2f2f8; padding: 1px;" align="center">
		<br/>
		<form>
			<select name="id_country">
				<c:forEach items="${paises}" var="pais">
					<c:choose>
						<c:when test="${pais.id == param.id_country || (pais.id==17 && empty(param.id_country))}">
							<c:set var="selected" value="selected" />
						</c:when>
						<c:otherwise>
							<c:set var="selected" value="" />
						</c:otherwise>
					</c:choose>
					
					<option value="<c:out value="${pais.id}" />" <c:out value="${selected}" />>
						<c:out value="${pais.nombre}" />
					</option>
				</c:forEach>
			</select>
	
			<input type="submit" value="Buscar">
		</form>
	
		<br/>

		<%-------------%>
		<%-- EQUIPOS --%>
		<%-------------%>

		<div style="text-align: center;">
			<div style="background-color: lightblue; display: inline-block;">
	 			<table style="margin: auto;">
					<thead>
						<tr>
							<th>&nbsp;</th>
							<th>Nombre</th>
							<th>Fecha</th>
						</tr>
					</thead>
					
	 				<c:forEach var="e" items="${equipos}" varStatus="var">
	 					<tr>
	 						<td align="right"><c:out value="${var.count}" /></td>
	 						<td><span class="transp">[b]</span><c:out value="${e.nombre}" /><span class="transp">[/b]</span></td>
	 						<td><fmt:formatDate value="${e.fecha}" pattern="dd-MM-yyyy" /></td>
	 					</tr>
					</c:forEach>
				</table>
			</div>
		</div>
	</div>

</body>
</html>

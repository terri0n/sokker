<%@page import="com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE"%>
<%@page import="java.util.Date"%>
<%@page import="com.formulamanager.sokker.tomcat.HttpSessionListener"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.formulamanager.sokker.bo.JugadorBO"%>
<%@page import="com.formulamanager.sokker.bo.UsuarioBO"%>
<%@page import="com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE"%>
<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="org.apache.catalina.Globals"%>
<%@page import="com.formulamanager.sokker.auxiliares.Util"%>
<%@page import="java.util.Locale"%>
<%@page import="com.formulamanager.sokker.tomcat.ServletContextListener"%>
<%@page import="com.formulamanager.sokker.entity.Usuario"%>
<%@page import="com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO"%>
<%@page import="com.formulamanager.sokker.bo.AsistenteBO"%>
<%@page import="com.formulamanager.sokker.entity.Jugador.DEMARCACION"%>
<%@page import="com.formulamanager.sokker.entity.Jugador"%>
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
<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="xtag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/mistags.tld" prefix="tags" %>

<!DOCTYPE html>
<html prefix="og: http://ogp.me/ns#">

<head>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<script src="https://kit.fontawesome.com/b668e4cfee.js" crossorigin="anonymous"></script>
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<link rel="icon" href="favicon.ico" type="image/x-icon" />

	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>

	<title>Sokker - Emparejamientos</title>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>

	<style>
		body {
			text-align: center;
			background-color: #e2f2f8;
			margin: 0px;
			padding: 0px;
		}
		th {
			background-color: lightblue;
		}
		table, th, td {
			border: 1px solid #e2f2f8;
			border-collapse: collapse;
		}
		a, a:visited {
			color: darkblue;
			decoration: none;
		}
		a:hover {
			color: blue;
		}
		.contenedor {
			display: inline-block;
			text-align: center;
		}
		.division {
			color: white;
			background-color: #539bcd;
			font-weight: bolder;
		}
		.promocion {
			background-color: rgba(107,206,99,.4);
		}
		.directo {
			background-color: rgba(1,152,92,.4);
		}
		@media only screen and (max-width: 800px) {
			.solo_pc {
				display: none;
			}
		}
	</style>
</head>

<body>
	<h2>ASCENSOS Y DESCENSOS EN SOKKER ESPAÑA</h2>

	<div style="position: absolute; width:250px;height:800px;" class="solo_pc">
		<!-- Display-columna -->
		<ins class="adsbygoogle"
		     style="display:block"
		     data-ad-client="ca-pub-7610610063984650"
		     data-ad-slot="1565843826"
		     data-ad-format="auto"></ins>
		<script>
		     (adsbygoogle = window.adsbygoogle || []).push({});
		</script>

		<!-- Display-columna -->
		<ins class="adsbygoogle"
		     style="display:block"
		     data-ad-client="ca-pub-7610610063984650"
		     data-ad-slot="1565843826"
		     data-ad-format="auto"></ins>
		<script>
		     (adsbygoogle = window.adsbygoogle || []).push({});
		</script>
	</div>
	<div style="position: absolute; right: 0px; width:250px;height:800px;text-align: right;" class="solo_pc">
		<!-- Display-columna -->
		<ins class="adsbygoogle"
		     style="display:block"
		     data-ad-client="ca-pub-7610610063984650"
		     data-ad-slot="1565843826"
		     data-ad-format="auto"></ins>
		<script>
		     (adsbygoogle = window.adsbygoogle || []).push({});
		</script>

		<!-- Display-columna -->
		<ins class="adsbygoogle"
		     style="display:block"
		     data-ad-client="ca-pub-7610610063984650"
		     data-ad-slot="1565843826"
		     data-ad-format="auto"></ins>
		<script>
		     (adsbygoogle = window.adsbygoogle || []).push({});
		</script>
	</div>

	<div class="contenedor">
		<table>
			<c:forEach begin="1" end="3" var="d">
				<thead>
					<tr class="division">
						<td colspan="8">
							<c:choose>
								<c:when test="${d == 1}">I vs II</c:when>
								<c:when test="${d == 2}">II vs III</c:when>
								<c:when test="${d == 3}">III vs IV</c:when>
							</c:choose>
						</td>
					</tr>
					<tr>
						<th>Local</th>
						<th>Pos.</th>
						<th>Pts.</th>
						<th>Goles</th>
						<th>Visitante</th>
						<th>Pos.</th>
						<th>Pts.</th>
						<th>Goles</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${partidos}" var="p">
						<c:if test="${p.local.liga.division == d}">
							<tr class="${p.local.posicion < 10 ? 'promocion' : 'directo'}">
								<td><a href="https://sokker.org/team/teamID/${p.local.tid}"><b><c:out value="${p.local.nombre}" /></b></a></td>
								<td><c:out value="${p.local.posicion}" /></td>
								<td><c:out value="${p.local.puntos}" /></td>
								<td><c:out value="${p.local.gf}-${p.local.gc}"/></td>
								<td><a href="https://sokker.org/team/teamID/${p.visitante.tid}"><b><c:out value="${p.visitante.nombre}" /></b></a></td>
								<td><c:out value="${p.visitante.posicion}" /></td>
								<td><c:out value="${p.visitante.puntos}" /></td>
								<td><c:out value="${p.visitante.gf}-${p.visitante.gc}"/></td>
							</tr>
						</c:if>
					</c:forEach>
					<tr><td>&nbsp;</td></tr>
				</tbody>
			</c:forEach>
		</table>
	
		<button onclick="location.href='${pageContext.request.contextPath}/';"><< VOLVER</button>
	</div>
	
	<br />
	<br />

	<!-- InArticle -->
	<ins class="adsbygoogle"
	     style="display:block; text-align:center;"
	     data-ad-layout="in-article"
	     data-ad-format="fluid"
	     data-ad-client="ca-pub-7610610063984650"
	     data-ad-slot="7009191822"></ins>
	<script>
	     (adsbygoogle = window.adsbygoogle || []).push({});
	</script>
</body>
</html>
<%@page import="com.formulamanager.sokker.bo.NtdbBO"%>
<%@page import="javax.servlet.jsp.jstl.core.LoopTagStatus"%>
<%@page import="javax.servlet.jsp.jstl.core.LoopTagSupport"%>
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

<c:if test="${empty sessionScope['javax.servlet.jsp.jstl.fmt.locale.session']}">
	<fmt:setLocale value="${pageContext.request.locale.language}" scope="session" />
</c:if>

<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />

<!DOCTYPE html>
<html prefix="og: http://ogp.me/ns#">

<head>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/desplegable.js"></script>
	<link rel="icon" href="favicon.ico" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/desplegable.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/asistente.css?4">
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>

	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<meta property="og:title" content="Sokker Asistente">
	<meta property="og:description" content="Nuevo asistente online para gestionar tu equipo de Sokker">
	<meta property="og:type" content="website">
	<meta property="og:image" content="https://raqueto.com${pageContext.request.contextPath}/img/sa_preview.png">
	<meta property="og:url" content="https://raqueto.com${pageContext.request.contextPath}/asistente">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta http-equiv=”Content-Language” content=”${sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language}”/>
	<title><fmt:message key="common.sokker_asistente" /> - NTDB interface</title>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>
	
	<script type="text/javascript">
		function idioma_change() {
			location.href = '${pageContext.request.contextPath}/asistente/idioma?juveniles=${param.juveniles}&historico=${param.historico}&lang=' + $('.dropdown').attr('data-toggle');
		}
	</script>
	
	<style>
		@media only screen and (max-width: 800px) {
			.solo_pc {
				display: none;
			}
		}
		@media only screen and (max-width: 1200px) {
			.solo_pc_ancho {
				display: none;
			}
		}
		.material-icons {
			font-size: 36px;
			vertical-align: middle;
		}
	</style>
</head>

<body>
	<c:if test="${not empty(param.mensaje)}">
		<c:if test="${param.error == 2}">
			<div class="error">${param.mensaje}</div>
		</c:if>
		<c:if test="${param.error != 2}">
			<div class="${empty param.error ? 'mensaje' : 'error'}"><fmt:message key="messages.${param.mensaje}" /></div>
		</c:if>
	</c:if>

	<%------------%>
	<%-- IDIOMA --%>
	<%------------%>
	<tags:desplegable onchange="idioma_change()" value="${fn:toUpperCase(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language)}" style="position: fixed; top: 0px; right: 0px; z-index: 3;" class_="dropdown_opacity">
		<c:forEach var="lang" items="<%= new String[] { \"EN\", \"ES\", \"FR\", \"IT\" } %>">
			<li onClick="dropdown_click(this)" data-toggle="${lang}" title="<%= Util.initCap(new Locale(((String)pageContext.getAttribute("lang"))).getDisplayLanguage()) %>">
				<img src="${pageContext.request.contextPath}/img/banderas/${lang == 'EN' ? 'GB' : lang}.png" class="margin-right"/>
			</li>
		</c:forEach>
	</tags:desplegable>

	<div id="menu" style="float: left; padding: 10px; min-width: 300px;">
		<%----------%>
		<%-- LOGO --%>
		<%----------%>
		<div class="menu" style="border-radius: 50px;">
			<div class="bloque doble" style="border-radius: 50px; padding: 10px;">
				<img src="${pageContext.request.contextPath}/img/sa.png" />
			</div>
		</div>
		
		<br />

		<div class="cabecera">Available National Teams</div>
		<div class="fin_bloque doble">
			<div align="left" style="display: inline-block;">
				<%
					HashMap<String, String> urls = Util.leer_hashmap("URLs");
					request.setAttribute("urls", urls);
				%>
				<c:forEach begin="1" end="<%= NtdbBO.paises.length %>" var="i">
					<c:if test="${urls[\"NT_\".concat(i)].indexOf('raqueto.com') > -1 || urls[\"U21_\".concat(i)].indexOf('raqueto.com') > -1}">
						<img src="https://files.sokker.org/pic/flags/<c:out value="${i}" />.png" />&nbsp;&nbsp;&nbsp;
						
						<span style="display: inline-block; min-width: 100px;">
							<c:if test="${urls[\"NT_\".concat(i)].indexOf('raqueto.com') > -1}">
								<a href="${pageContext.request.contextPath}/asistente/ntdb?tid=${i}">
									<%= NtdbBO.paises[(Integer)pageContext.getAttribute("i") - 1] %> NT
								</a>
							</c:if>
						</span>
						
						<c:if test="${empty urls[\"U21_\".concat(i)] || urls[\"U21_\".concat(i)].indexOf('raqueto.com') > -1}">
							<a href="${pageContext.request.contextPath}/asistente/ntdb?tid=${i + 400}">
								<%= NtdbBO.paises[(Integer)pageContext.getAttribute("i") - 1] %> U21
							</a>
						</c:if>
						
						<br />
					</c:if>
				</c:forEach>
			</div>
		</div>

		<br />

		<div class="cabecera">Links</div>
		<div class="fin_bloque doble">
			<div class="boton grande" onclick="javascript:location.href='${pageContext.request.contextPath}/asistente/ntdb'">
				<span class="material-icons" style="color: brown">directions_run</span>
				<div class="peque">
					<fmt:message key="common.append" /> <fmt:message key="common.players" />
				</div>
			</div>

			<div class="boton grande" onclick="javascript:location.href='${pageContext.request.contextPath}/asistente/horario?tid=400'">
				<span class="material-icons azul">outlined_flag</span>
				<div class="peque">
					U21s schedule
				</div>
			</div>

			<div class="boton grande" onclick="javascript:location.href='${pageContext.request.contextPath}/asistente/horario?tid=0'">
				<span class="material-icons azul">flag</span>
				<div class="peque">
					NTs schedule
				</div>
			</div>

			<div class="boton grande" onclick="javascript:location.href='${pageContext.request.contextPath}/asistente'">
				<span class="material-icons gris">arrow_circle_left</span>
				<div class="peque">
					<fmt:message key="common.back" />
				</div>
			</div>
		</div>
		
		<br />
		
		<div style="width:350px;height:800px;" class="solo_pc">
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
	</div>
</body>
</html>

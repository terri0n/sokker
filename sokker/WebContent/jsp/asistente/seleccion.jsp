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
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/desplegable.js"></script>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<meta property="og:title" content="Sokker Asistente">
	<meta property="og:description" content="Nuevo asistente online para gestionar tu equipo de Sokker">
	<meta property="og:type" content="website">
	<meta property="og:image" content="https://raqueto.com${pageContext.request.contextPath}/img/sa_preview.png">
	<meta property="og:url" content="https://raqueto.com${pageContext.request.contextPath}/asistente">
	<link rel="icon" href="favicon.ico" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/desplegable.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/asistente.css?4">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta http-equiv=”Content-Language” content=”${sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language}”/>
	<title><fmt:message key="common.sokker_asistente" /> - NTDB interface</title>

	<style>
		<%-- FILTROS --%>
		
		<c:if test="${empty param.juveniles}">
			.juv {
				display: none;
			}
		</c:if>
	</style>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>
</head>

<body>
	<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto;">
		<%@include file="ntdb_menu.jsp" %>

		<%---------------%>
		<%-- JUGADORES --%>
		<%---------------%>
		
		<c:if test="${not empty jugadores}">
			<div style="display: inline-block; text-align: left; padding-top: 10px;">
				<h2 class="texto_3d">
					<c:out value="${equipo}" />
				</h2>
	
				<table id="jugadores" style="text-align: center; border-spacing: 1px;">
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.GK %>" titulo="common.goalkeepers" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.DEF %>" titulo="common.defenders" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.MID %>" titulo="common.midfielders" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.ATT %>" titulo="common.forwards" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.BANQUILLO %>" titulo="common.bench" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.OTRA %>" titulo="common.other_position" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="${null}" titulo="common.not_assigned" />
				</table>
				<br />
			</div>
		</c:if>
	</div>
</body>
</html>

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

<c:if test="${empty sessionScope['javax.servlet.jsp.jstl.fmt.locale.session']}">
	<fmt:setLocale value="${pageContext.request.locale.language}" scope="session" />
</c:if>

<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />

<!DOCTYPE html>
<html prefix="og: http://ogp.me/ns#">

<head>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/ip.js.jsp?<%= new Date().getTime() %>"></script>
    <script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/desplegable.js"></script>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<meta property="og:title" content="Sokker Asistente">
	<meta property="og:description" content="Nuevo asistente online para gestionar tu equipo de Sokker">
	<meta property="og:type" content="website">
	<meta property="og:image" content="https://raqueto.com${pageContext.request.contextPath}/img/sa_preview.png">
	<meta property="og:url" content="https://raqueto.com${pageContext.request.contextPath}/asistente">
	<link rel="icon" href="favicon.ico" type="image/x-icon" />
	<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/desplegable.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/asistente.css?5">

	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta http-equiv=”Content-Language” content=”${sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language}”/>
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>

	<title><fmt:message key="common.sokker_asistente" /></title>
	<c:if test="${empty usuario}">
		<meta http-equiv="refresh" content="1800">
	</c:if>

	<style type="text/css">
		.md-48 {
			font-size: 48px;
		}
	</style>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>
	
	<script type="text/javascript">
		function acceso_click(enlace) {
			$("#menu_principal .fin_bloque").hide();
			$("#acceso").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("div").addClass("seleccionada");
		}

		function registro_click(enlace) {
			$("#menu_principal .fin_bloque").hide();
			$("#registro").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("div").addClass("seleccionada");
		}
	
		function recuperar_click(enlace) {
			$("#menu_principal .fin_bloque").hide();
			$("#recuperar").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("div").addClass("seleccionada");
		}

		function faq_click(enlace) {
			$("#menu_principal .fin_bloque").hide();
			$("#faq").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("div").addClass("seleccionada");
		}

		function mostrar_actualizando() {
			$(`<div>
					<img style="opacity: 0.8; position: fixed; top: 0; display:block; width:100%; height:100%; object-fit: cover;" src="${pageContext.request.contextPath}/img/cargando.gif"/>
					<h1 class="borde" style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -125%); color: white;"><fmt:message key="common.updating" />...</h1>
				</div>`).appendTo(document.body);
		}

		function form_submit(form) {
			if ($("form[name='" + form + "'] input[name='confirmed']").val() == '1') {
				mostrar_actualizando();
				return true;
			} else {
				if ($("form[name='" + form + "'] input[name='ipassword']").val() != $("form[name='" + form + "'] input[name='password2']").val()) {
					$("form[name='" + form + "'] input[name='ipassword']").select().focus();
					alert("<fmt:message key='login.passwords_error' />");
				} else {
					$("form[name='" + form + "'] input[type='submit']").attr("disabled", true);
					var data = 'ilogin=' + encodeURIComponent($("form[name='" + form + "'] input[name='ilogin']").val()) + '&ipassword=' + encodeURIComponent($("form[name='" + form + "'] input[name='ipassword']").val());
					$.post('https://sokker.org/start.php?session=xml&' + data, data).done(
				        function(data) {
							$("form[name='" + form + "'] input[type='submit']").attr("disabled", false);
				        	if (data.startsWith('OK')) {
				        		$("form[name='" + form + "'] input[name='confirmed']").val('1');
								$("form[name='" + form + "'] input[type='submit']").click();
				            } else {
				            	alert('<fmt:message key="messages.login_error" var="var"/>${fn:replace(var, "'", "\\'")}');
				            }
				        } 
				    ).fail(function() {
				    	alert('<fmt:message key="messages.cors" />');
				    });
				}

				return false;
			}
		}
		
		function idioma_change() {
			location.href = '${pageContext.request.contextPath}/asistente/idioma?juveniles=${param.juveniles}&historico=${param.historico}&lang=' + $('.dropdown').attr('data-toggle');
		}
	</script>
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
	</div>
	
	<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto; max-width: 840px;">
		<div id="menu" style="display: inline-block; padding: 10px;">
			<%----------%>
			<%-- LOGO --%>
			<%----------%>
			<div class="menu" style="border-radius: 50px;">
				<div class="bloque doble" style="border-radius: 50px; padding: 10px;">
					<img src="${pageContext.request.contextPath}/img/sa.png" />
				</div>
			</div>

			<br />

			<%--------------------%>
			<%-- IDENTIFICACIÓN --%>
			<%--------------------%>
			<div id="menu_principal">
				<div class="cabecera cabecera_span seleccionada">
					&nbsp;<a href="#" onclick="javascript:acceso_click(this)"><fmt:message key="login.access" /></a>&nbsp;
				</div>
				<div class="cabecera cabecera_span">
					&nbsp;<a href="#" onclick="javascript:registro_click(this)"><fmt:message key="login.register" /></a>&nbsp;
				</div>
				<div class="cabecera cabecera_span">
					&nbsp;<a href="#" onclick="javascript:recuperar_click(this)"><fmt:message key="login.change_password" /></a>&nbsp;
				</div>
				<div class="cabecera cabecera_span">
					&nbsp;<a href="#" onclick="javascript:faq_click(this)"><fmt:message key="login.faq" /></a>&nbsp;
				</div>
				
				<%-- ACCESO --%>
				
				<div class="inicio_bloque fin_bloque doble" style="text-align: right;" id="acceso">
					<form method="post" action="${pageContext.request.contextPath}/asistente/login" onsubmit="$('input[type=submit]').prop('disabled', true);">
						<fmt:message key="common.login" /> <input type="text" name="alogin" required="required" value="<%= Util.nvl(Util.getCookie(request, "alogin")) %>"><br/>
						<fmt:message key="common.password" /> <input type="password" name="apassword" required="required" value="<%= Util.nvl(Util.getCookie(request, "apassword")) %>"><br/>
						<label class="peque" for="recordar"><input type="checkbox" name="recordar" id="recordar" <c:out value="${empty cookie.apassword.value ? '' : 'checked'}" /> /><fmt:message key="login.remember_password" /></label>
						<br />
						<input type="submit" /><br/>
					</form>
				</div>

				<%-- REGISTRO --%>

				<div class="inicio_bloque fin_bloque doble" style="text-align: right; display: none;" id="registro">
					<form name="form_registro" method="post" action="${pageContext.request.contextPath}/asistente/registro" onsubmit="return form_submit('form_registro')">
						<input type="hidden" name="confirmed">
						<fmt:message key="common.login" /> <input type="text" name="login" required="required"><br/>
						<fmt:message key="login.sokker_login" /> <input type="text" name="ilogin" required="required"><br/>
						<fmt:message key="login.sokker_password" /> <input type="password" name="ipassword" required="required"><br/>
						<fmt:message key="login.repeat_password" /> <input type="password" name="password2" required="required"><br/>
						<input type="submit" /><br/>
					</form>
				</div>

				<%-- CAMBIAR PASSWORD --%>

				<div class="inicio_bloque fin_bloque doble" style="text-align: right; display: none;" id="recuperar">
					<form name="form_cambio" method="post" action="${pageContext.request.contextPath}/asistente/cambiar_password" onsubmit="return form_submit('form_cambio')">
						<input type="hidden" name="confirmed">
						<fmt:message key="common.login" /> <input type="text" name="login" required="required"><br/>
						<fmt:message key="login.sokker_login" /> <input type="text" name="ilogin" required="required"><br/>
						<fmt:message key="login.sokker_password" /> <input type="password" name="ipassword" required="required"><br/>
						<fmt:message key="login.repeat_password" /> <input type="password" name="password2" required="required"><br/>
						<input type="submit" /><br/>
					</form>
				</div>

				<%-- FAQ --%>

				<div class="inicio_bloque fin_bloque doble" style="text-align: left; display: none;" id="faq">
					<%@include file="faq.jsp" %>
				</div>
			</div>

			<div style="opacity: 0.7; width: 100%; line-height: 1.1; margin-top: 10px; vertical-align: bottom;">
				<div style="display: inline-block; color: white; margin: 2px;" title="<%= HttpSessionListener.getActiveSessions() %> <fmt:message key="login.connected" />">
					<span class="material-icons md-48">groups</span>
					<div class="peque" style="margin-top: -6px;">
						<%= UsuarioBO.obtener_usuarios().length %> <fmt:message key="login.users" />
					</div>
				</div>
				
				<div class="boton_menu" onclick="javascript:location.href='${pageContext.request.contextPath}/asistente/estadisticasForma'">
					<span class="material-icons md-48">signal_cellular_alt</span>
					<div class="peque" style="margin-top: -6px;">
						<fmt:message key="login.statistics" />
					</div>
				</div>
				
				<div class="boton_menu" onclick="javascript:window.open('http://github.com/terri0n/sokker', '_blank', 'height='+screen.height+', width='+screen.width);">
					<span class="material-icons md-48">code</span>
					<div class="peque" style="margin-top: -6px;">
						<fmt:message key="login.source_code" />
					</div>
				</div>

				<div class="boton_menu" onclick="javascript:location.href='${pageContext.request.contextPath}/app/SokkerAsistente.apk'">
					<span class="material-icons md-48">android</span>
					<div class="peque" style="margin-top: -6px;">
						Android APP
					</div>
				</div>

				<div class="boton_menu" onclick="javascript:location.href='${pageContext.request.contextPath}/asistente/ntdb'">
					<span class="material-icons md-48">outlined_flag</span>
					<div class="peque" style="margin-top: -6px;">
						NTDB
					</div>
				</div>

				<div class="boton_menu" onclick="javascript:location.href='${pageContext.request.contextPath}/'">
					<span class="material-icons md-48">arrow_circle_left</span>
					<div class="peque" style="margin-top: -6px;">
						<fmt:message key="common.back" />
					</div>
				</div>
			</div>
			
			<%@include file="noticias.jsp" %>

			<!-- InArticle -->
			<br /><br />
			<ins class="adsbygoogle"
			     style="display:block; text-align:center;"
			     data-ad-layout="in-article"
			     data-ad-format="fluid"
			     data-ad-client="ca-pub-7610610063984650"
			     data-ad-slot="7009191822"></ins>
			<script>
			     (adsbygoogle = window.adsbygoogle || []).push({});
			</script>
		</div>
	</div>
</body>
</html>

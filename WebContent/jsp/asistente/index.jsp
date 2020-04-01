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
<%@ taglib uri="/WEB-INF/tags/mistags.tld" prefix="tags" %>

<c:if test="${empty sessionScope['javax.servlet.jsp.jstl.fmt.locale.session']}">
	<fmt:setLocale value="${PageContext.request.locale.language}" scope="session" />
</c:if>

<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />

<!DOCTYPE html>
<html prefix="og: http://ogp.me/ns#">

<head>
	<script src="https://kit.fontawesome.com/6b4c099088.js"></script>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/charts/loader.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/desplegable.js"></script>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<meta property="og:title" content="Sokker Asistente">
	<meta property="og:description" content="Nuevo asistente online para gestionar tu equipo de Sokker">
	<meta property="og:type" content="website">
	<meta property="og:image" content="http://www.formulamanager.com${pageContext.request.contextPath}/img/sa_preview.png">
	<meta property="og:url" content="http://www.formulamanager.com${pageContext.request.contextPath}/asistente">
	<link rel="icon" href="favicon.ico" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/desplegable.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/asistente.css">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	
	<title><fmt:message key="common.sokker_asistente" /></title>

	<style>
		<%-- FILTROS --%>
		
		<c:if test="${sessionScope.usuario.def_tid < 1000}">
			.nont {
				display: none;
			}
		</c:if>
		<c:if test="${sessionScope.usuario.def_tid > 1000}">
			.nt {
				display: none;
			}
		</c:if>
		<c:if test="${empty param.historico}">
			.hist {
				display: none;
			}
		</c:if>
		<c:if test="${not empty param.historico}">
			.nohist {
				display: none;
			}
		</c:if>
		<c:if test="${empty param.juveniles}">
			.juv {
				display: none;
			}
		</c:if>
		<c:if test="${not empty param.juveniles}">
			.nojuv {
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
	
	<script type="text/javascript">
		function acceso_click(enlace) {
			$("#menu_principal .bloque").hide();
			$("#acceso").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("span").addClass("seleccionada");
		}

		function registro_click(enlace) {
			$("#menu_principal .bloque").hide();
			$("#registro").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("span").addClass("seleccionada");
		}
	
		function recuperar_click(enlace) {
			$("#menu_principal .bloque").hide();
			$("#recuperar").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("span").addClass("seleccionada");
		}

		function faq_click(enlace) {
			$("#menu_principal .bloque").hide();
			$("#faq").show();
			$(".cabecera_span").removeClass("seleccionada");
			$(enlace).parent("span").addClass("seleccionada");
		}
		
		function form_submit(form) {
			if ($("form[name='" + form + "'] input[name='password']").val() != $("form[name='" + form + "'] input[name='password2']").val()) {
				$("form[name='" + form + "'] input[name='password']").select().focus();
				alert("Los passwords no coinciden");
				return false;
			} else {
				return true;
			}
		}

		<c:if test="${not empty sessionScope.usuario}">
		
			function desplegar_click(pid) {
				var span = $('#span' + pid);
				var desplegar = span.html() == '▶';
				span.html(desplegar ? '▼' : '▶');
				$('tr[id^="tr' + pid + '"]').toggle();
				$('.noentr').hide();
				$('#menu').hide();
				$('#desplegar_menu').show();
			}
	
			function desplegar_menu_click() {
				$('span[id^="span"]').html('▶');
				$('tr[id^="tr"]').hide();
				$('.noentr').show();
				$('#menu').show();
				$('#desplegar_menu').hide();
			}
	
			function jugadores_click(demarcacion) {
				$('#jugadores' + demarcacion + ' span[id^="span"]').each(function() {
					var pid = $(this).attr("id").split("span")[1];
					desplegar_click(pid);
				});
			}
			
			function entrenamiento_click(span, nombre, puntos_entrenamiento, lesion, jornada, demarcacion_entrenamiento, tipo_entrenamiento, demarcacion, pid, nivel, habilidad) {
				if (habilidad == '<%= TIPO_ENTRENAMIENTO.Condicion.ordinal() %>') {
					<c:forEach begin="12" end="18" var="n">
						$("select[name='nivel_entrenamiento'] option[value='${n}']").hide();
					</c:forEach>
				} else {
					<c:forEach begin="12" end="18" var="n">
						$("select[name='nivel_entrenamiento'] option[value='${n}']").show();
					</c:forEach>
				}
				
				$('#nombre').text(nombre);
				$('#jornada').text((jornada + 1) % 16);
				$("input[name='puntos_entrenamiento']").val(puntos_entrenamiento);
				$("input[name='lesion']").val(lesion);
				$("select[name='demarcacion_entrenamiento']").val(demarcacion_entrenamiento);
				$("select[name='tipo_entrenamiento']").val(tipo_entrenamiento);
				$("select[name='demarcacion_equipo']").val(demarcacion);
				$("input[name='jornada_entrenamiento']").val(jornada);
				$("input[name='pid_entrenamiento']").val(pid);
				$("select[name='nivel_entrenamiento']").val(nivel);
				$("input[name='habilidad_entrenamiento']").val(habilidad);
	
				var entrenamiento = $('#editar_entrenamiento');
				entrenamiento.css('left', span.offset().left);
				entrenamiento.css('top', span.offset().top + span.height() * 2);
				entrenamiento.slideDown('fast');
				$('select[name="nivel_entrenamiento"]').focus();
			}
	
			function editar_click(boton, id) {
				var editar = $('#editar' + id);
				editar.css('left', boton.offset().left + boton.width() - editar.width());
				//editar.css('top', boton.offset().top + boton.height());
				editar.slideDown('fast');
				editar.find("textarea").focus();
			}
	
			function proyectar_nuevo_click(elem) {
				proyectar_click(elem, 15, <c:out value="${jugadores[0].jornadaMod}" />, '0', '0', '0', '0', '0', '0', '0', '0', 0, 0, 0, 0, 0, 0, 0, 0, '4.5', true);
			}
			
			function proyectar_listo_click() {
				var edad = parseInt($('#pr_edad').val());
				$('#pr_edad0').val(edad);
	
				$('#pr_edad').empty();
				for (var i = edad + 1; i <= 30; i++) {
					$('#pr_edad').append('<option>' + i + '</option>');
				}

				// Sumo 1/2 punto porque no sabemos cuáles son sus subniveles
				$('#pr_condicion0').val(parseInt($('#editar_proyeccion').find("select[name='stamina']").val()) + 0.5);
				$('#pr_rapidez0').val(parseInt($('#editar_proyeccion').find("select[name='pace']").val()) + 0.5);
				$('#pr_tecnica0').val(parseInt($('#editar_proyeccion').find("select[name='technique']").val()) + 0.5);
				$('#pr_pases0').val(parseInt($('#editar_proyeccion').find("select[name='passing']").val()) + 0.5);
				$('#pr_porteria0').val(parseInt($('#editar_proyeccion').find("select[name='keeper']").val()) + 0.5);
				$('#pr_defensa0').val(parseInt($('#editar_proyeccion').find("select[name='defender']").val()) + 0.5);
				$('#pr_creacion0').val(parseInt($('#editar_proyeccion').find("select[name='playmaker']").val()) + 0.5);
				$('#pr_anotacion0').val(parseInt($('#editar_proyeccion').find("select[name='striker']").val()) + 0.5);
	
				$('#fila_pr_entrenamientos').show();
				$('#boton_pr_listo').hide();
				proyeccion_change();
			}
			
			function proyectar_click(elem, edad, jornada, condicion, rapidez, tecnica, pases, porteria, defensa, creacion, anotacion, condicionE, rapidezE, tecnicaE, pasesE, porteriaE, defensaE, creacionE, anotacionE, talento, nuevo) {
				// Valores originales
				$('#pr_edad0').val(edad);
				$('#pr_jornada0').val(jornada);
				
				$('#pr_condicion0').val(condicion);
				$('#pr_rapidez0').val(rapidez);
				$('#pr_tecnica0').val(tecnica);
				$('#pr_pases0').val(pases);
				$('#pr_porteria0').val(porteria);
				$('#pr_defensa0').val(defensa);
				$('#pr_creacion0').val(creacion);
				$('#pr_anotacion0').val(anotacion);
	
				// PE extra
				$('#pr_condicionE').val(condicionE);
				$('#pr_rapidezE').val(rapidezE);
				$('#pr_tecnicaE').val(tecnicaE);
				$('#pr_pasesE').val(pasesE);
				$('#pr_porteriaE').val(porteriaE);
				$('#pr_defensaE').val(defensaE);
				$('#pr_creacionE').val(creacionE);
				$('#pr_anotacionE').val(anotacionE);
				
				$('#pr_edad').empty();
				for (var i = edad + 1; i <= 30; i++) {
					$('#pr_edad').append('<option>' + i + '</option>');
				}
	
				$('#pr_talento').val(talento.replace(',', '.'));
	
				var proyeccion = $('#editar_proyeccion');
	
				if (nuevo) {
					$('#editar_proyeccion').find("select[name='stamina']").val('0');
					$('#editar_proyeccion').find("select[name='pace']").val('0');
					$('#editar_proyeccion').find("select[name='technique']").val('0');
					$('#editar_proyeccion').find("select[name='passing']").val('0');
					$('#editar_proyeccion').find("select[name='keeper']").val('0');	// No contamos los residuales de portería
					$('#editar_proyeccion').find("select[name='defender']").val('0');
					$('#editar_proyeccion').find("select[name='playmaker']").val('0');
					$('#editar_proyeccion').find("select[name='striker']").val('0');

					$('#fila_pr_entrenamientos').hide();
					$('#boton_pr_listo').show();
					proyeccion.css('left', elem.offset().left + elem.width());
				} else {
					$('#fila_pr_entrenamientos').show();
					$('#boton_pr_listo').hide();
					proyeccion_change();
					proyeccion.css('left', elem.offset().left + elem.width() - proyeccion.width());
				}
				
				proyeccion.css('top', elem.offset().top + elem.height() - 8);
				proyeccion.stop(false, true);
				proyeccion.slideDown('fast');
				$('#pr_edad').focus();
			}
	
			function sokker(n) {
				var total = 0.0;
				for (var i = 0; i < n; i++) {
			    	total += Math.pow(<%= Jugador.BASE_TALENTO %>, i);
				}
				
				// Penalización por talento
				return total * parseFloat($('#pr_talento').val()) / 3.0;
			}
	
			function unsokker(n) {
				// Penalización por talento
				n = n * 3.0 / parseFloat($('#pr_talento').val());
				
				var total = 0.0;
				for (var i = 0; i < 20; i++) {
			    	total += Math.pow(<%= Jugador.BASE_TALENTO %>, i);
			    	if (total > n) {
			    		return i;
			    	}
				}
				return 20;
			}
			
			function nivel_con_residuales(habilidad, entrenamientos) {
				var plus = habilidad == 'rapidez' || habilidad == 'anotacion' ? 2 : 0;
				return Math.min(18, unsokker(sokker(parseInt($('#pr_' + habilidad + '0').val()) + plus) + parseFloat($('#pr_' + habilidad + 'E').val()) + entrenamientos * 0.1) - plus);
			}
	
			function proyeccion_change() {
				if ($('#boton_pr_listo').is(":hidden")) {
					// Contamos los entrenamientos restantes hasta que se cumpla la edad seleccionada
					var entrenamientos = contar_entrenamientos();
					$('#pr_entrenamientos').text(entrenamientos.toFixed(1));
		
					// Condición
					var condicion = parseInt($('#pr_edad').val()) - parseInt($('#pr_edad0').val()) + parseFloat($('#pr_condicionE').val());
					$('#editar_proyeccion').find("select[name='stamina']").val(parseInt(Math.min(11, parseInt($('#pr_condicion0').val()) + condicion)));
					
					$('#editar_proyeccion').find("select[name='pace']").val(nivel_con_residuales('rapidez', entrenamientos));
					$('#editar_proyeccion').find("select[name='technique']").val(nivel_con_residuales('tecnica', entrenamientos));
					$('#editar_proyeccion').find("select[name='passing']").val(nivel_con_residuales('pases', entrenamientos));
					$('#editar_proyeccion').find("select[name='keeper']").val(nivel_con_residuales('porteria', 0));	// No contamos los residuales de portería
					$('#editar_proyeccion').find("select[name='defender']").val(nivel_con_residuales('defensa', entrenamientos));
					$('#editar_proyeccion').find("select[name='playmaker']").val(nivel_con_residuales('creacion', entrenamientos));
					$('#editar_proyeccion').find("select[name='striker']").val(nivel_con_residuales('anotacion', entrenamientos));
					
					proyeccion_select_change();
				}
			}
			
			function contar_entrenamientos() {
				// Contamos los entrenamientos de las jornadas que restan de la temporada actual
				var entrenamientos = (15.9 - parseInt($('#pr_jornada0').val())) / Math.pow(<%= Jugador.BASE_TALENTO %>, parseInt($('#pr_edad0').val()) - 16);
	
				// Y contamos temporadas completas para el resto
				for (var i = parseInt($('#pr_edad0').val()) + 1; i < parseInt($('#pr_edad').val()); i++) {
					entrenamientos += 15.9 / Math.pow(<%= Jugador.BASE_TALENTO %>, i - 16);
				}
	
				return entrenamientos;
			}
			
			function proyeccion_select_change() {
				if ($('#boton_pr_listo').is(":hidden")) {
					var entrenamientos = contar_entrenamientos();
		
					var rapidez0 = sokker(parseFloat($('#pr_rapidez0').val()) + 2) + parseFloat($('#pr_rapidezE').val()) + entrenamientos * 0.1;
					var tecnica0 = sokker(parseFloat($('#pr_tecnica0').val())) + parseFloat($('#pr_tecnicaE').val()) + entrenamientos * 0.1;
					var pases0 = sokker(parseFloat($('#pr_pases0').val())) + parseFloat($('#pr_pasesE').val()) + entrenamientos * 0.1;
					var porteria0 = sokker(parseFloat($('#pr_porteria0').val())) + parseFloat($('#pr_porteriaE').val());
					var defensa0 = sokker(parseFloat($('#pr_defensa0').val())) + parseFloat($('#pr_defensaE').val()) + entrenamientos * 0.1;
					var creacion0 = sokker(parseFloat($('#pr_creacion0').val())) + parseFloat($('#pr_creacionE').val()) + entrenamientos * 0.1;
					var anotacion0 = sokker(parseFloat($('#pr_anotacion0').val()) + 2) + parseFloat($('#pr_anotacionE').val()) + entrenamientos * 0.1;
					
					var rapidez = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='pace']").val()) + 2) - rapidez0);
					var tecnica = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='technique']").val())) - tecnica0);
					var pases = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='passing']").val())) - pases0);
					var porteria = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='keeper']").val())) - porteria0);
					var defensa = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='defender']").val())) - defensa0);
					var creacion = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='playmaker']").val())) - creacion0);
					var anotacion = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='striker']").val()) + 2) - anotacion0);
		
					$('#pr_entrenamientos').text((entrenamientos - rapidez - tecnica - pases - porteria - defensa - creacion - anotacion).toFixed(1));
					
					if (parseFloat($('#pr_entrenamientos').text()) < 0) {
						$('#pr_entrenamientos').css('color', 'red');
					} else if (parseFloat($('#pr_entrenamientos').text()) > 0) {
						$('#pr_entrenamientos').css('color', 'green');
					} else {
						$('#pr_entrenamientos').css('color', 'black');
					}
				}
			}
			
			google.charts.load('current', {packages: ['corechart']});
	
			function drawChart(span, tipo, titulo, datos, hTicks, min_valor, max_valor, min_jornada, max_jornada, colores) {
				$('#editar_chart').css('left', Math.min(window.innerWidth + window.pageXOffset - $('#editar_chart').width(), span.offset().left));
				$('#editar_chart').css('top', Math.min(window.innerHeight + window.pageYOffset - $('#editar_chart').height(), span.offset().top + span.height()));
				$('#editar_chart').show();
	
				var vTicks = [];
				if (tipo == 'valor') {
					var salto = Math.max(10000, Math.ceil(max_valor / 100000) * 10000);
					for (var i = 0; i - salto < max_valor; i += salto) {
						vTicks.push(i);
					}
				} else if (tipo == 'condición') {
					vTicks = [0,1,2,3,4,5,6,7,8,9,10,11];
				} else {
					vTicks = [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18];
				}
	
				var data = new google.visualization.DataTable();
				data.addColumn('number', 'edad');
				titulo.forEach(function(t) {
					data.addColumn('number', t);
				});
				data.addColumn({type:'boolean', role:'scope'});
				data.addRows( datos );
				if (!colores) {
					colores = ['blue', 'red'];
				}
				var options = {colors: colores, "title": (tipo == 'talento' ? "" : '<fmt:message key="players.changes_graph" />: ') + titulo[0],"legend":{"position":"none"},"explorer":{"axis":"horizontal"},"hAxis":{"ticks":hTicks,"title":'<fmt:message key="common.age" />',"viewWindow":{"min":min_jornada,"max":max_jornada}},"vAxis":{"minValue":min_valor,"maxValue":max_valor,"ticks":vTicks},"pointSize":5};
				var chart = tipo == 'talento' ? new google.visualization.LineChart($('#editar_chart')[0]) : new google.visualization.AreaChart($('#editar_chart')[0]);
				chart.draw(data, options);
			}
			
		</c:if>
		
		function idioma_change() {
			location.href = '${pageContext.request.contextPath}/asistente/idioma?juveniles=${param.juveniles}&historico=${param.historico}&lang=' + $('.dropdown').attr('data-toggle');
		}

		function carga() {
			$(document).on("click", ".minutos", function () {
				$(this).removeClass("minutos");
				var minutos = $($(this).siblings('span')[1]);
				minutos.html("<input type='text' size='1' value='" + minutos.text() + "' />");
				minutos.focus();
			});
			
			$(document).mouseup(function(e) {
			    var container = $("div[id ^= 'editar']");

			    // if the target of the click isn't the container nor a descendant of the container
			    if (!container.is(e.target) && container.has(e.target).length === 0) {
			        container.slideUp('fast');
			    }
			});
			
			$(document).on('change', '.select_proyeccion select', function() {
				proyeccion_select_change();
			});
		}
	</script>
</head>

<body onload="carga()" >
	<c:if test="${not empty(param.mensaje)}">
		<div class="mensaje"><fmt:message key="messages.${param.mensaje}" /></div>
	</c:if>

	<%------------%>
	<%-- IDIOMA --%>
	<%------------%>
	<tags:desplegable onchange="idioma_change()" value="${fn:toUpperCase(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language)}" style="position: fixed; top: 0px; right: 0px; z-index: 3;" class_="dropdown_opacity">
		<c:forEach var="lang" items="<%= new String[] { \"EN\", \"ES\", \"IT\" } %>">
			<li onClick="dropdown_click(this)" data-toggle="${lang}" title="<%= Util.initCap(new Locale(((String)pageContext.getAttribute("lang"))).getDisplayLanguage()) %>">
				<img src="img/banderas/${lang == 'EN' ? 'GB' : lang}.png" class="margin-right"/>
			</li>
		</c:forEach>
	</tags:desplegable>

	<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto;">
		
		<div id="menu" style="${empty sessionScope.usuario ? 'display: inline-block;' : 'float: left;'} padding: 10px;">
			<%----------%>
			<%-- LOGO --%>
			<%----------%>
			<div class="menu sombra" style="border-radius: 50px;">
				<div class="bloque" style="border-radius: 50px; padding: 10px; background-color: #C3E0F5;">
					<img src="img/sa.png" />
				</div>
			</div>

			<br />

			<%--------------------%>
			<%-- IDENTIFICACIÓN --%>
			<%--------------------%>
			<c:if test="${empty sessionScope.usuario}">
				<div class="menu" id="menu_principal">
					<span class="cabecera borde cabecera_span seleccionada">
						&nbsp;<a href="#" onclick="javascript:acceso_click(this)"><fmt:message key="login.access" /></a>&nbsp;
					</span>
					<span class="cabecera borde cabecera_span">
						&nbsp;<a href="#" onclick="javascript:registro_click(this)"><fmt:message key="login.register" /></a>&nbsp;
					</span>
					<span class="cabecera borde cabecera_span">
						&nbsp;<a href="#" onclick="javascript:recuperar_click(this)"><fmt:message key="login.change_password" /></a>&nbsp;
					</span>
					<span class="cabecera borde cabecera_span">
						&nbsp;<a href="#" onclick="javascript:faq_click(this)"><fmt:message key="login.faq" /></a>&nbsp;
					</span>
					<div class="bloque" style="text-align: right; border-top: 1px solid #1d4866; margin-top: -1px;" id="acceso">
						<form method="post" action="${pageContext.request.contextPath}/asistente/login">
							<fmt:message key="common.login" /> <input type="text" name="alogin" required="required"><br/>
							<fmt:message key="common.password" /> <input type="password" name="apassword" required="required"><br/>
							<input type="submit" /><br/>
						</form>
					</div>
					<div class="bloque" style="text-align: right; display: none; border-top: 1px solid #1d4866; margin-top: -1px;" id="registro">
						<form name="form_registro" method="post" action="${pageContext.request.contextPath}/asistente/registro" onsubmit="return form_submit('form_registro')">
							<fmt:message key="common.login" /> <input type="text" name="login" required="required"><br/>
							<fmt:message key="common.password" /> <input type="password" name="password" required="required"><br/>
							<fmt:message key="login.repeat_password" /> <input type="password" name="password2" required="required"><br/>
							<fmt:message key="login.sokker_login" /> <input type="text" name="ilogin" required="required"><br/>
							<fmt:message key="login.sokker_password" /> <input type="password" name="ipassword" required="required"><br/>
							<input type="submit" /><br/>
						</form>
					</div>
					<div class="bloque" style="text-align: right; display: none; border-top: 1px solid #1d4866; margin-top: -1px;" id="recuperar">
						<form name="form_cambio" method="post" action="${pageContext.request.contextPath}/asistente/cambiar_password" onsubmit="return form_submit('form_cambio')">
							<fmt:message key="common.login" /> <input type="text" name="login" required="required"><br/>
							<fmt:message key="login.new_password" /> <input type="password" name="password" required="required"><br/>
							<fmt:message key="login.repeat_password" /> <input type="password" name="password2" required="required"><br/>
							<fmt:message key="login.sokker_login" /> <input type="text" name="ilogin" required="required"><br/>
							<fmt:message key="login.sokker_password" /> <input type="password" name="ipassword" required="required"><br/>
							<input type="submit" /><br/>
						</form>
					</div>
					<div class="bloque" style="text-align: left; display: none; border-top: 1px solid #1d4866; margin-top: -1px;" id="faq">
						<%@include file="faq.jsp" %>
					</div>
				</div>
			</c:if>

			<c:if test="${not empty sessionScope.usuario}">
				<%----------%>
				<%-- MENÚ --%>
				<%----------%>
				<div class="menu">
					<div class="cabecera borde">
						<b><fmt:message key="menu.hello">
							<fmt:param value="${sessionScope.usuario.login}" />
						</fmt:message></b>
					</div>
					<div class="bloque" style="text-align: center; margin: auto;">
						<c:if test="${not empty sessionScope.usuario.tid_nt}">
							<a class="nont" href="${pageContext.request.contextPath}/asistente/cambiar_equipo">
								<fmt:message key="menu.change_to" /> <c:out value="${sessionScope.usuario.equipo_nt}" />
							</a>
							<img class="nont" src="https://files.sokker.org/pic/flags/${sessionScope.usuario.tid_nt < 400 ? sessionScope.usuario.tid_nt : sessionScope.usuario.tid_nt - 400}.png" />
							<a class="nt" href="${pageContext.request.contextPath}/asistente/cambiar_equipo">
								<fmt:message key="menu.change_to" /> <c:out value="${sessionScope.usuario.equipo}" />
							</a>
							<hr/>
						</c:if>

						<div style="text-align: left">						
							<label for="numeros" class="peque">
								<input type="checkbox" <c:out value="${usuario.numeros ? 'checked' : ''}" /> id="numeros" onclick="location.href='${pageContext.request.contextPath}/asistente/numeros?juveniles=${param.juveniles}&historico=${param.historico}&numeros=' + ($('#numeros').is(':checked') ? '1' : '')"><fmt:message key="menu.show_skill_numbers" />
							</label>
						</div>
						<hr/>
						
						<input type="button" value="<fmt:message key="menu.logout" />" onclick="location.href='${pageContext.request.contextPath}/asistente/logout'" />
					</div>
				</div>
				
				<br />

				<div class="menu">
					<div class="cabecera borde">
						<b><fmt:message key="common.players" /></b>
					</div>
					<div class="bloque">
						<div style="display: inline-block; margin: auto; text-align: right;">
							<form method="post" action="${pageContext.request.contextPath}/asistente/actualizar">
								<c:if test="${sessionScope.usuario.def_tid > 1000}">
									<fmt:message key="login.sokker_login" />: <input type="text" name="ilogin" value="${sessionScope.usuario.login_sokker}" required="required" /><br/>
									<fmt:message key="login.sokker_password" />: <input type="password" name="ipassword" required="required" /><br/>
									<label for="ntdb" class="peque">
										<input type="checkbox" id="ntdb" name="ntdb" <c:out value="${sessionScope.usuario.ntdb ? 'checked' : ''}" /> /><fmt:message key="menu.send_players" /><br/>
									</label>
								</c:if>
								<input type="submit" value="<fmt:message key="common.update" />" /><br/>
							</form>
						</div>
						
						<hr/>

						<c:if test="${sessionScope.usuario.def_tid > 1000}">
							<i class="boton fas ${empty param.juveniles ? 'fa-baby' : 'fa-running'}" style="color: brown"
								<c:if test="${empty param.juveniles}">
									title="<fmt:message key="common.juniors" />"
								</c:if>
								<c:if test="${not empty param.juveniles}">
									title="<fmt:message key="common.players" />"
								</c:if>
								onclick="location.href='${pageContext.request.contextPath}/asistente/cambiar_vista?juveniles=${1 - param.juveniles}'" 
							></i>
							<span class="separador">|</span>
						</c:if>
						
						<i class="fas fa-history boton ${empty param.historico ? '' : 'fa-flip-horizontal'}"
							<c:if test="${empty param.historico}">
								title="<fmt:message key="menu.watch_historical" />"
							</c:if>
							<c:if test="${not empty param.historico}">
								 title="<fmt:message key="menu.watch_current" />"
							</c:if>
							onclick="location.href='${pageContext.request.contextPath}/asistente/cambiar_vista?juveniles=${param.juveniles}&historico=${1 - param.historico}'"
						></i>

						<span class="separador">|</span>

 						<i class="fas fa-calculator boton pink" style="color: purple;" title="<fmt:message key="players.project_skills" />" onclick="proyectar_nuevo_click($(this))"></i>
 					</div>
				</div>

				<br class="nt"/>

				<c:set var="j" value="<%= new Jugador() %>" />
				<div class="menu nt">
					<div class="cabecera borde">
						<b><fmt:message key="menu.new_player" /></b>
					</div>
					<div class="bloque" style="text-align: center; margin: auto;">
						<form method="post" action="${pageContext.request.contextPath}/asistente/grabar">
							<div style="display: inline-block; text-align: right; margin: auto">
								<fmt:message key="common.pid" />: <input type="number" name="pid" required="required"/><br/>
							</div>
							
							<br/><br/>
							
							<table style="text-align: left; margin: auto; font-size: smaller;">
								<tr>
									<td><fmt:message key="skills.stamina" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'stamina', 0, usuario.numeros, 11)}</td><td><fmt:message key="skills.keeper" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'keeper', 0, usuario.numeros)}</td>
								</tr>
								<tr>
									<td><fmt:message key="skills.pace" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'pace', 0, usuario.numeros)}</td><td><fmt:message key="skills.defender" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'defender', 0, usuario.numeros)}</td>
								</tr>
								<tr>
									<td><fmt:message key="skills.technique" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'technique', 0, usuario.numeros)}</td><td><fmt:message key="skills.playmaker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'playmaker', 0, usuario.numeros)}</td>
								</tr>
								<tr>
									<td><fmt:message key="skills.passing" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'passing', 0, usuario.numeros)}</td><td><fmt:message key="skills.striker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'striker', 0, usuario.numeros)}</td>
								</tr>
								<tr style="background-color: transparent;">
									<td colspan="4">&nbsp;</td>
								</tr>
								<tr style="background-color: transparent; font-size: initial;">
									<td colspan="2"><fmt:message key="common.position" />: <c:out value="${j.despDemarcacion_asistente(null, usuario)}" escapeXml="false"/></td>
									<td colspan="2">
										<label for="fiable">
											<input type="checkbox" id="fiable" name="fiable" /><fmt:message key="players.reliable" />
										</label>
									</td>
								</tr>
							</table>
		
							<div align="left">
								<fmt:message key="common.notes" />:<br/>
								<textarea name="notas" rows="5" style="width: 100%"></textarea>
							</div>
		
							<input type="submit" value="<fmt:message key="common.append" />" />
						</form>
					</div>
				</div>
				
				<c:if test="${sessionScope.usuario.login == 'terrion'}">
					<br/>	
					<div class="menu">
						<div class="cabecera borde">
							<span style="cursor:pointer;" onclick="$('#lista_usuarios').show(); $(this).hide();">▶</span>
							Usuarios
						</div>
						<div style="display:none" class="bloque" align="left" id="lista_usuarios">
							<%= AsistenteBO.listar_usuarios() %>
						</div>
					</div>
				</c:if>
			</c:if>
		</div>
		
		<div id="desplegar_menu" title="<fmt:message key="menu.show_menu" />" class="borde flecha" style="display: none; float: left; color: #e2eff8;" onclick="desplegar_menu_click()">▶</div>

		<%---------------%>
		<%-- JUGADORES --%>
		<%---------------%>
		
		<c:if test="${not empty sessionScope.usuario}">
			<div style="display: inline-block; text-align: left; padding-top: 10px;">
				<h2 class="texto_3d">
					<i class="fas fa-running" style="opacity: 0.1; font-size: 0.4em"></i>
					<i class="fas fa-running" style="opacity: 0.25; font-size: 0.55em"></i>
					<i class="fas fa-running" style="opacity: 0.4; font-size: 0.7em"></i>
					<i class="fas fa-futbol" style="opacity: 0.55; font-size: 0.15em"></i>
					<c:out value="${sessionScope.usuario.def_tid < 1000 ? sessionScope.usuario.equipo_nt : sessionScope.usuario.equipo}" />
					<c:if test="${not empty param.historico}">
						(<fmt:message key="common.history" />)
					</c:if>
					<i class="fas fa-futbol" style="opacity: 0.55; font-size: 0.15em"></i>
					<i class="fas fa-running fa-flip-horizontal" style="opacity: 0.4; font-size: 0.7em"></i>
					<i class="fas fa-running fa-flip-horizontal" style="opacity: 0.25; font-size: 0.55em"></i>
					<i class="fas fa-running fa-flip-horizontal" style="opacity: 0.1; font-size: 0.4em"></i>
				</h2>
	
				<table style="width: 100%; text-align: center; /* background-color: #1d4866; */ border-spacing: 1px;" id="jugadores${demarcacion}">
					<c:if test="${empty param.juveniles}">
						<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.GK %>" titulo="common.goalkeepers" />
						<tr><td colspan="100">&nbsp;</td></tr>
						<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.DEF %>" titulo="common.defenders" />
						<tr><td colspan="100">&nbsp;</td></tr>
						<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.MID %>" titulo="common.midfielders" />
						<tr><td colspan="100">&nbsp;</td></tr>
						<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.ATT %>" titulo="common.forwards" />
						<tr><td colspan="100">&nbsp;</td></tr>
						<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.OTRA %>" titulo="common.other_position" />
						<tr><td colspan="100">&nbsp;</td></tr>
					</c:if>
					<xtag:jugadores demarcacion="${null}" titulo="common.not_assigned" />
				</table>
				<br />
			</div>
		</c:if>
	</div>

	<%--------------------------%>
	<%-- EDITAR ENTRENAMIENTO --%>
	<%--------------------------%>

	<div id="editar_entrenamiento" class="menu sombra" style="position: absolute; display: none; text-align: center; z-index: 100">
		<div class="cabecera borde">
			<b><span id='nombre'></span> - <fmt:message key="common.round" /> <span id='jornada'></span></b>
		</div>

		<div class="bloque">
			<form method="post" action="${pageContext.request.contextPath}/asistente/actualizar_entrenamiento">
				<input type="hidden" name="jornada_entrenamiento">
				<input type="hidden" name="pid_entrenamiento">
				<input type="hidden" name="habilidad_entrenamiento">
				
				<table style="text-align: left; white-space: nowrap;" align="center">
					<tr style="background-color: transparent;">
						<td colspan="2">
							<b><fmt:message key="training.individual_training" />:</b>
							<hr class="hr" />
						</td>
					</tr>
					<tr style="background-color: transparent;">
						<td><fmt:message key="common.level" />:</td>
						<td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'nivel_entrenamiento', 0, usuario.numeros)}</td>
					</tr>
					<tr style="background-color: transparent;">
						<td><fmt:message key="training.training_points" />:</td>
						<td><input name="puntos_entrenamiento" type="text" required="required" size="3"/></td>
					</tr>
					<tr style="background-color: transparent;">
						<td><fmt:message key="common.injury" />:</td>
						<td><input name="lesion" type="text" size="3"/></td>
					</tr>
					<tr style="background-color: transparent;">
						<td><fmt:message key="common.position" />:</td>
						<td>
							<select name="demarcacion_entrenamiento">
								<option></option>
								<c:forEach items="<%= DEMARCACION.values() %>" var="pos">
									<option>${pos}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
					
					<tr style="background-color: transparent;">
						<td class="peque">&nbsp;</td>
					</tr>
					
					<tr style="background-color: transparent;">
						<td colspan="2">
							<b><fmt:message key="training.team_training" />:</b>
							<hr class="hr" />
						</td>
					</tr>
					<tr style="background-color: transparent;">
						<td><fmt:message key="training.type" />:</td>
						<td>
							<select name="tipo_entrenamiento">
								<c:forEach items="<%= TIPO_ENTRENAMIENTO.values() %>" var="tipo">
									<option value="${tipo}"><fmt:message key="skills.${tipo.ingles}" /></option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr style="background-color: transparent;">
						<td><fmt:message key="common.position" />:</td>
						<td>
							<select name="demarcacion_equipo">
								<c:forEach items="<%= DEMARCACION.values() %>" var="pos">
									<option>${pos}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
				</table>
				<input type="submit" value="<fmt:message key="common.change" />" style="margin-top: 5px;"/>
			</form>
		</div>
	</div>

	<div id="editar_chart" class="sombra grafica"></div>

	<div id="editar_proyeccion" class="sombra" style="position: absolute; display: none;">
		<div class="cabecera borde" style="text-align: center;">
			<fmt:message key="players.skills_projection" />
		</div>

		<div class="bloque">
			<input type="hidden" id="pr_edad0" />
			<input type="hidden" id="pr_jornada0" />

			<input type="hidden" id="pr_condicion0" />
			<input type="hidden" id="pr_rapidez0" />
			<input type="hidden" id="pr_tecnica0" />
			<input type="hidden" id="pr_pases0" />
			<input type="hidden" id="pr_porteria0" />
			<input type="hidden" id="pr_defensa0" />
			<input type="hidden" id="pr_creacion0" />
			<input type="hidden" id="pr_anotacion0" />

			<input type="hidden" id="pr_condicionE" />
			<input type="hidden" id="pr_rapidezE" />
			<input type="hidden" id="pr_tecnicaE" />
			<input type="hidden" id="pr_pasesE" />
			<input type="hidden" id="pr_porteriaE" />
			<input type="hidden" id="pr_defensaE" />
			<input type="hidden" id="pr_creacionE" />
			<input type="hidden" id="pr_anotacionE" />

			<table style="text-align: left; margin: auto; font-size: smaller;">
				<tr style="background-color: transparent; text-align: center;">
					<td colspan="2"><b><fmt:message key="skills.age" />:</b> <select id="pr_edad" onchange="proyeccion_change()"></select></td>
					<td colspan="2"><b><fmt:message key="skills.talent" />:</b> <input type="number" id="pr_talento" onchange="proyeccion_change()" style="width:40px;" step="0.1"/></td>
				</tr>
				<tr style="background-color: transparent; font-size: 0.1em;">
					<td>&nbsp;</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.stamina" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'stamina', 0, usuario.numeros, 11)}</td><td><fmt:message key="skills.keeper" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'keeper', 0, usuario.numeros)}</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.pace" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'pace', 0, usuario.numeros)}</td><td><fmt:message key="skills.defender" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'defender', 0, usuario.numeros)}</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.technique" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'technique', 0, usuario.numeros)}</td><td><fmt:message key="skills.playmaker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'playmaker', 0, usuario.numeros)}</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.passing" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'passing', 0, usuario.numeros)}</td><td><fmt:message key="skills.striker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'striker', 0, usuario.numeros)}</td>
				</tr>
				<tr style="background-color: transparent; font-size: 0.1em;">
					<td>&nbsp;</td>
				</tr>
				<tr style="background-color: transparent; text-align: center;">
					<td colspan="4">
						<span id="fila_pr_entrenamientos"><b><fmt:message key="players.remaining_trainings" />:</b> <span id="pr_entrenamientos"></span></span>
						<input type="button" value="<fmt:message key="common.ready" />" onclick="proyectar_listo_click()" id="boton_pr_listo" />
					</td>
				</tr>
			</table>
		</div>
	</div>
</body>
</html>

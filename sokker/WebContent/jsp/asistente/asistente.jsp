<%@page import="com.formulamanager.sokker.auxiliares.SystemUtil"%>
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
	<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.5.1/chart.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<meta property="og:title" content="Sokker Asistente">
	<meta property="og:description" content="Nuevo asistente online para gestionar tu equipo de Sokker">
	<meta property="og:type" content="website">
	<meta property="og:image" content="https://raqueto.com${pageContext.request.contextPath}/img/sa_preview.png">
	<meta property="og:url" content="https://raqueto.com${pageContext.request.contextPath}/asistente">
	<link rel="icon" href="favicon.ico" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/desplegable.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/asistente.css?7">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta http-equiv=”Content-Language” content=”${sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language}”/>
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>

	<title><fmt:message key="common.sokker_asistente" /></title>
	<c:if test="${empty sessionScope.usuario}">
		<meta http-equiv="refresh" content="1800">
	</c:if>

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
		<c:if test="${empty sessionScope.historico}">
			.hist {
				display: none;
			}
		</c:if>
		<c:if test="${not empty sessionScope.historico}">
			.nohist {
				display: none;
			}
		</c:if>
		<c:if test="${empty sessionScope.juveniles}">
			.juv {
				display: none;
			}
		</c:if>
		<c:if test="${not empty sessionScope.juveniles}">
			.nojuv {
				display: none;
			}
		</c:if>
		
		.material-icons {
			font-size: 16px;
		}
		.md-24 {
			font-size: 24px;
		}
		.md-32 {
			font-size: 32px;
		}
		.vertical {
			vertical-align: middle;
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
		function mostrar_mensaje(mensaje) {
			$(`<div>
					<img style="opacity: 0.8; position: fixed; top: 0; display:block; width:100%; height:100%; object-fit: cover;" src="${pageContext.request.contextPath}/img/cargando.gif"/>
					<h1 class="borde" style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -125%); color: white;">` + mensaje + `...</h1>
				</div>`).appendTo(document.body);
		}

		function ocultar_mensaje() {
			$('div:last').remove();
		}

		function anyadir_mensaje(mensaje) {
			$('.mensaje').remove();
			setTimeout(function() {
			    $('body').prepend('<div class="mensaje">' + mensaje + '</div>');				
			}, 1);
		}
		
		// Hace scroll manual de las filas de entrenamiento de un jugador	
		function mover_entrenamiento_click(pid, direccion, fin) {
			var trs = $('tr[id^="tr' + pid + '"]');
			var edad_inicial = parseInt(trs.filter(":visible:first").attr("id").split("_")[1]) + direccion;
			var edad_min = parseInt(trs.first().attr("id").split("_")[1]);
			var edad_max = parseInt(trs.last().attr("id").split("_")[1]);

			if (trs.filter("#tr" + pid + "_" + edad_inicial).length > 0
					&& trs.filter("#tr" + pid + "_" + (edad_inicial + 2)).length > 0) {
				trs.each(function(i, tr) {
					var edad = $(tr).attr("id").split("_")[1];
					
					if (edad >= edad_inicial && edad <= edad_inicial + 2) {
						if (edad == edad_inicial && edad > edad_min) {
							$(tr).find(".flecha_arr").show();
						} else {
							$(tr).find(".flecha_arr").hide();
						}

						if (edad == edad_inicial + 2 && edad < edad_max) {
							$(tr).find(".flecha_aba").show();
						} else {
							$(tr).find(".flecha_aba").hide();
						}
						
						if ($(tr).is(":hidden")) {
							$(tr).show();
						}
					} else {
						if ($(tr).is(":visible")) {
							$(tr).hide();
						}
					}
				});

				// Si se ha pulsado sobre una flecha del final, ponemos el scroll horizontal al final de la página
				if (fin) {
					$(window).scrollLeft($(window).width());
				}
			}
		}

		function desplegar_click(pid, recalcular) {
			var span = $('#span' + pid);
			var desplegar = span.html() == '▶';
			span.html(desplegar ? '▼' : '▶');

			var tbody = $('tbody[id^="tbody' + pid + '"]');
			tbody.toggle();

			if ($('tbody[id^=tbody]:visible').length == 0) {
			    desplegar_menu_click();
			} else {
				$('.noentr').hide();
				$('#menu').hide();
				$('#desplegar_menu').show();
			}
		}

		function desplegar_menu_click() {
			$('span[id^="span"]').html('▶');
			$('tbody[id^="tbody"]').hide();
			$('.noentr').show();
			$('#menu').show();
			$('#desplegar_menu').hide();
		}

		function jugadores_click(demarcacion) {
			<c:if test="${sessionScope.usuario.def_tid < 1000}">
				// Marcar checkboxes
				$('#jugadores' + demarcacion + ' input[id^="check"]').each(function() {
					$(this).prop('checked', !$(this).prop('checked'));
				});				
			</c:if>
			<c:if test="${sessionScope.usuario.def_tid > 1000}">
				// Desplegar entrenamiento
				$('#jugadores' + demarcacion + ' span[id^="span"]').each(function() {
					var pid = $(this).attr("id").split("span")[1];
					desplegar_click(pid, false);
				});
			</c:if>
		}
		
		function entrenamiento_click(span, nombre, puntos_entrenamiento, lesion, jornada, demarcacion_entrenamiento, tipo_entrenamiento, demarcacion, pid, nivel, habilidad, anyadir, entrenador, asistentes, juveniles, avanzado, tipo_entrenamiento_gk, tipo_entrenamiento_def, tipo_entrenamiento_mid, tipo_entrenamiento_att) {
			if (habilidad == '<%=TIPO_ENTRENAMIENTO.Condicion.ordinal()%>') {
				<c:forEach begin="12" end="18" var="n">
					$("select[name='nivel_entrenamiento'] option[value='${n}']").hide();
				</c:forEach>
			} else {
				<c:forEach begin="12" end="18" var="n">
					$("select[name='nivel_entrenamiento'] option[value='${n}']").show();
				</c:forEach>
			}
			
			$('#nombre').text(nombre);
			$('#jornada').text(jornada + 1 < <%=AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS%> ? (jornada + 1) % 16 : (jornada + 1 - <%=AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS%>) % <%=AsistenteBO.JORNADAS_TEMPORADA%>);
			$("input[name='puntos_entrenamiento']").val(puntos_entrenamiento);
			$("input[name='avanzado']").prop("checked", avanzado);
			$("input[name='lesion']").val(lesion);
			$("select[name='demarcacion_entrenamiento']").val(demarcacion_entrenamiento);
			$("select[name='tipo_entrenamiento']").val(tipo_entrenamiento);
			$("select[name='tipo_entrenamiento_gk']").val(tipo_entrenamiento_gk);
			$("select[name='tipo_entrenamiento_def']").val(tipo_entrenamiento_def);
			$("select[name='tipo_entrenamiento_mid']").val(tipo_entrenamiento_mid);
			$("select[name='tipo_entrenamiento_att']").val(tipo_entrenamiento_att);
			$("select[name='demarcacion_equipo']").val(demarcacion);
			$("input[name='jornada_entrenamiento']").val(jornada);
			$("input[name='pid_entrenamiento']").val(pid);
			$("select[name='nivel_entrenamiento']").val(nivel);
			$("input[name='habilidad_entrenamiento']").val(habilidad);
			$("input[name='tr_entrenador']").val(entrenador);
			$("input[name='tr_asistentes']").val(asistentes);
			$("input[name='tr_juveniles']").val(juveniles);

			var entrenamiento = $('#editar_entrenamiento');
			entrenamiento.css('left', span.offset().left);
			entrenamiento.css('top', span.offset().top + span.height() * 2);
			
			if (jornada < <%=AsistenteBO.JORNADA_NUEVO_ENTRENO%>) {
			    $('.viejo_entreno').show();
			    $('.nuevo_entreno').hide();
			} else {
			    $('.viejo_entreno').hide();
			    $('.nuevo_entreno').show();
			}
			
			entrenamiento.slideDown('fast');
			$('select[name="nivel_entrenamiento"]').focus();
			
			$('#actualizar_entrenamiento_submit').val(anyadir ? '<fmt:message key="common.append" />' : '<fmt:message key="common.change" />');
			anyadir ? $('#tr_semanas').show() : $('#tr_semanas').hide();
			anyadir ? $('#tr_borrar_entrenamiento').hide() : $('#tr_borrar_entrenamiento').show();
		}

		function borrar_entrenamiento_click() {
			var pid = $("input[name='pid_entrenamiento']").val();
			var jornada = $("input[name='jornada_entrenamiento']").val();
			location.href = '${pageContext.request.contextPath}/asistente/borrar_entrenamiento?pid=' + pid + '&jornada=' + jornada;
		}
		
		function editar_click(boton, id) {
			var editar = $('#editar' + id);
			editar.css('left', boton.offset().left + boton.width() - editar.width());
			//editar.css('top', boton.offset().top + boton.height());
			editar.slideDown('fast');
			editar.find("textarea").focus();
		}

		function get_checked() {
			var resp = '';
			$('input[id^="check"]:checked').each(function() {
				resp += $(this).attr('id').split('check')[1] + ",";
			});
			return resp;
		}
		
		function exportar_click(boton) {
		    $.ajax({
				url: "${pageContext.request.contextPath}/servlet/exportar_bbcode?" + get_checked()
			}).done(function(resp) {
			    const textarea = $('#editar_bbcode');
				textarea.find('textarea').val(resp);
				textarea.css('left', boton.offset().left + boton.width());
				textarea.css('top', boton.offset().top - textarea.height() / 2);
				textarea.slideDown('fast');
				textarea.find('textarea').focus();
			    $('#editar_acciones').hide();
			}).fail(function(error) {
				console.log(error);
				if (error.status == 401) {
				    location.href='${pageContext.request.contextPath}/asistente?mensaje=session_expired';
				} else {
					alert(error.responseText);
				}
			});
		}

		function exportar_url_click(boton) {
		    $('#editar_acciones').hide();
			
		    // Esperamos a que se oculte
		    setTimeout(function(){
			    let url = prompt('URL:', 'http://ntdb.sokker.cz/index.php');
	
			    if (url) {
					mostrar_mensaje('<fmt:message key="common.updating" />');
	
					$.ajax({
						url: '${pageContext.request.contextPath}/servlet/exportar_url?url=' + encodeURI(url) + '&pids=' + get_checked()
					}).done(function(resp) {
					    ocultar_mensaje();
					    anyadir_mensaje(resp + ' <fmt:message key="messages.updated" />');
					}).fail(function(error) {
						console.log(error);
						if (error.status == 401) {
						    location.href='${pageContext.request.contextPath}/asistente?mensaje=session_expired';
						} else {
						    ocultar_mensaje();
							alert(error.statusText);
						}
					});
			    }
		    }, 1);
		}
		
		function borrar_jugadores_click(boton) {
			let checked = get_checked();
			console.log(checked);
			if (checked) {
				if (confirm('Remove ' + (checked.split(',').length - 1) + ' players?')) {
				    location.href = '${pageContext.request.contextPath}/asistente/borrar_jugadores?pids=' + checked;
				}
			} else {
			    alert('No players selected');
			}
		}
		
		function cambiar_demarcacion(boton, demarcacion) {
			let checked = get_checked();
			console.log(checked);
			if (checked) {
				if (confirm('Set ' + (checked.split(',').length - 1) + ' players as ' + demarcacion + '?')) {
				    location.href = '${pageContext.request.contextPath}/asistente/cambiar_demarcacion?demarcacion=' + demarcacion + '&pids=' + checked;
				}
			} else {
			    alert('No players selected');
			}
		}
		
		function notas_click(boton) {
			const textarea = $('#editar_notas');
			textarea.css('left', boton.offset().left + boton.width());
			textarea.css('top', boton.offset().top - textarea.height() / 2);
			textarea.slideDown('fast');
			textarea.find('textarea').focus();
		}

		function skmail_click(boton) {
			const textarea = $('#editar_skmail');
			textarea.css('left', boton.offset().left + boton.width());
			textarea.css('top', boton.offset().top - textarea.height() / 2);
			textarea.slideDown('fast');
			textarea.find('input').focus();
		}

		function acciones_click(boton) {
			if ($('input[id^="check"]:visible').size() == 0) {
			    $('input[id^="check"]').show();
			} else {
			    const textarea = $('#editar_acciones');
				textarea.css('left', boton.offset().left + boton.width());
				textarea.css('top', boton.offset().top - textarea.height() / 2);
				textarea.slideDown('fast');
				textarea.find('input').focus();
			}
		}

		function instalacion_click(boton) {
			const textarea = $('#editar_instalacion');
			textarea.css('left', boton.offset().left + boton.width());
			textarea.css('top', boton.offset().top - textarea.height() / 2);
			textarea.slideDown('fast');
			textarea.find('textarea').focus();
		}

		function filtros_click(boton) {
			const textarea = $('#editar_filtros');
			textarea.css('left', boton.offset().left + boton.width());
			textarea.css('top', boton.offset().top - textarea.height() / 2);
			textarea.slideDown('fast');
		}
		
		function guardar_notas_click() {
			$.ajax({
				url: "${pageContext.request.contextPath}/servlet/guardar_notas?" + encodeURI($('#editar_notas').find('textarea').val())
			}).done(function(resp) {
				$('#editar_notas').slideUp('fast');
				$('#boton_notas i:last').removeClass("far fa-comment fas fa-comment-dots");
				$('#boton_notas i:last').addClass($('#editar_notas').find('textarea').val().length > 0 ? "fas fa-comment-dots" : "far fa-comment");
			});
		}

		function exportar_jugador_click(boton, pid) {
			$.ajax({
				url: "${pageContext.request.contextPath}/servlet/jugador_bbcode?pid=" + pid
			}).done(function(resp) {
				const textarea = $('#editar_bbcode_jugador');
				textarea.find('textarea').text(resp);
				textarea.css('left', boton.offset().left - textarea.width());
				textarea.css('top', boton.offset().top - textarea.height() / 2);
				textarea.slideDown('fast');
				textarea.find('textarea').focus();
			}).fail(function(error) {
			    console.log(error);
				if (error.status == 401) {
				    location.href='${pageContext.request.contextPath}/asistente?mensaje=session_expired';
				} else {
					alert(error.statusText);
				}
			});
		}

		function proyectar_nuevo_click(elem) {
			proyectar_click(elem, 15, <c:out value="${jugadores[0].jornadaProyeccion}" />, '0', '0', '0', '0', '0', '0', '0', '0', 0, 0, 0, 0, 0, 0, 0, 0, '3.7', true);
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
				proyeccion.find("select[name='stamina']").val('0');
				proyeccion.find("select[name='pace']").val('0');
				proyeccion.find("select[name='technique']").val('0');
				proyeccion.find("select[name='passing']").val('0');
				proyeccion.find("select[name='keeper']").val('0');	// No contamos los residuales de portería
				proyeccion.find("select[name='defender']").val('0');
				proyeccion.find("select[name='playmaker']").val('0');
				proyeccion.find("select[name='striker']").val('0');

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
		    	total += Math.pow(${sessionScope.usuario.factor_habilidad}, i);
			}
			
			// Penalización por talento
			return total * parseFloat($('#pr_talento').val()) / 3.0;
		}

		function unsokker(n) {
			// Penalización por talento
			n = n * 3.0 / parseFloat($('#pr_talento').val()) + 0.0000000001;	// Corrijo el error de precisión de js
			
			var total = 0.0;
			for (var i = 0; i < 20; i++) {
		    	total += Math.pow(${sessionScope.usuario.factor_habilidad}, i);
		    	if (total > n) {
		    		return i;
		    	}
			}
			return 20;
		}
		
		function nivel_con_residuales(habilidad, entrenamientos) {
			var plus;
			switch (habilidad) {
				case 'rapidez':		plus = ${6.8 - sessionScope.usuario.factor_rapidez * 6.8}; break;
				case 'defensa':		plus = ${6.8 - sessionScope.usuario.factor_defensa * 6.8}; break;
				case 'anotacion':	plus = ${6.8 - sessionScope.usuario.factor_anotacion * 6.8}; break;
				case 'porteria':	plus = ${6.8 - sessionScope.usuario.factor_porteria * 6.8}; break;
				case 'tecnica':		plus = ${6.8 - sessionScope.usuario.factor_tecnica * 6.8}; break;
				case 'pases':		plus = ${6.8 - sessionScope.usuario.factor_pases * 6.8}; break;
				case 'creacion':	plus = ${6.8 - sessionScope.usuario.factor_creacion * 6.8}; break;
			}
			return parseInt(Math.min(18, unsokker(sokker(parseInt($('#pr_' + habilidad + '0').val()) + plus) + parseFloat($('#pr_' + habilidad + 'E').val()) + entrenamientos * ${sessionScope.usuario.factor_residual}) - plus));
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
			let entrenamientos = parseFloat(<%=AsistenteBO.JORNADAS_TEMPORADA%> - parseInt($('#pr_jornada0').val())) / Math.pow(${sessionScope.usuario.factor_edad}, parseInt($('#pr_edad0').val()) - 16);

			// Y contamos temporadas completas para el resto
			for (let i = parseInt($('#pr_edad0').val()) + 1; i < parseInt($('#pr_edad').val()); i++) {
				entrenamientos += parseFloat(<%=AsistenteBO.JORNADAS_TEMPORADA%>) / Math.pow(${sessionScope.usuario.factor_edad}, i - 16);
			}

			return entrenamientos;
		}
		
		function proyeccion_select_change() {
			if ($('#boton_pr_listo').is(":hidden")) {
				var entrenamientos = contar_entrenamientos();

				var rapidez0 = sokker(parseFloat($('#pr_rapidez0').val()) + 2) + parseFloat($('#pr_rapidezE').val()) + entrenamientos * ${sessionScope.usuario.factor_residual};
				var tecnica0 = sokker(parseFloat($('#pr_tecnica0').val()) + 0.5) + parseFloat($('#pr_tecnicaE').val()) + entrenamientos * ${sessionScope.usuario.factor_residual};
				var pases0 = sokker(parseFloat($('#pr_pases0').val())) + parseFloat($('#pr_pasesE').val()) + entrenamientos * ${sessionScope.usuario.factor_residual};
				var porteria0 = sokker(parseFloat($('#pr_porteria0').val())) + parseFloat($('#pr_porteriaE').val());
				var defensa0 = sokker(parseFloat($('#pr_defensa0').val())) + parseFloat($('#pr_defensaE').val()) + entrenamientos * ${sessionScope.usuario.factor_residual};
				var creacion0 = sokker(parseFloat($('#pr_creacion0').val())) + parseFloat($('#pr_creacionE').val()) + entrenamientos * ${sessionScope.usuario.factor_residual};
				var anotacion0 = sokker(parseFloat($('#pr_anotacion0').val()) + 2) + parseFloat($('#pr_anotacionE').val()) + entrenamientos * ${sessionScope.usuario.factor_residual};
				
				var rapidez = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='pace']").val()) + 2) - rapidez0);
				var tecnica = Math.max(0, sokker(parseInt($('#editar_proyeccion').find("select[name='technique']").val()) + 0.5) - tecnica0);
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
		
		function grafica_ajax(span, tipo, pid, historico) {
			$.ajax({
				url: "${pageContext.request.contextPath}/servlet/datos_grafica?pid=" + pid + "&tipo=" + tipo + '&historico=' + (historico || ''),
				dataType: "json"
			}).done(function(resp) {
				console.log(resp);
				drawChart2(span, tipo, resp.titulo, resp.datasets, resp.max_valor);
			}).fail(function(error) {
			    console.log(error);
				if (error.status == 401) {
				    location.href='${pageContext.request.contextPath}/asistente?mensaje=session_expired';
				} else {
					alert(error.responseText);
				}
			});
		}

		let myChart;
		function drawChart2(span, tipo, titulo, datasets, max_valor) {
			$('#editar_chart').css('left', Math.min(window.innerWidth + window.pageXOffset - $('#editar_chart').width() - 15, span.offset().left));
			$('#editar_chart').css('top', Math.min(window.innerHeight + window.pageYOffset - $('#editar_chart').height(), span.offset().top + span.height()));
			$('#editar_chart').show();

			const red='#FF0000';
			const orange='#FFA500';
			const green='#00AA00';
			const gray='#CCCCCC';
			const blue='#0000FF';
			const pink='#FFC0CB';

			if (myChart) {
				myChart.destroy();
			}
			
			myChart = new Chart(
			    $('#editar_chart canvas')[0], {
		        type: 'line',
		        data: {
		        	labels: datasets[0].labels,
		        	datasets: [{
		        	    label: datasets[0].label,
		        	    data: datasets[0].data,
				        pointBackgroundColor: datasets[0].borderColor,
				        borderColor: datasets[0].borderColor,
				        backgroundColor: datasets[0].backgroundColor,
				        segment: {
					        borderColor: (ctx) => {
					  			return myChart ? myChart.data.datasets[0].pointBackgroundColor[ctx.p0DataIndex] : null;
					        },
					  		backgroundColor: (ctx) => {
					  			if (myChart && !datasets[0].backgroundColor) {
					  				let color = myChart.data.datasets[0].pointBackgroundColor[ctx.p0DataIndex];
					  				return color == gray ? 'transparent' : color + '55';
					  			} else {
					  				return datasets[0].backgroundColor;
					  			}
					        }
					  	}
		        	}]
		        },
		        options: {
		        	fill: true,
		        	interaction: {
		        		intersect: false
		        	},
		        	scales: {
		                y: {
		                    suggestedMin: 0,
		                    suggestedMax: max_valor
		                }
		            },
		        	plugins: {
		              title: {
		                display: true,
		                text: (tipo == 'talento' ? '' : '<fmt:message key="players.changes_graph" />: ') + titulo
		              },
		        	  legend: {
		        		labels: {
		        			filter: function(item, chart) {
		        				return item.text;
		        			}
		        		}
		        	  }
		            }
		          }
		      });
			
			for (let i = 1; i < datasets.length; i++) {
			    myChart.data.datasets.push(datasets[i]);
			}
			myChart.update();
		}
		
		function editar_juvenil_click(boton, pid, nombre, niveles) {
			$("#nombre_juvenil").text(nombre);
			$("input[name='pid_juvenil']").val(pid);
			$("input[name='niveles_juvenil']").val(niveles);

			var editar = $('#editar_juvenil');
			editar.css('left', boton.offset().left + boton.width() - editar.width());
			editar.css('top', boton.offset().top + boton.height() - 8);
			editar.stop(false, true);
			editar.slideDown('fast');
			$("input[name='niveles_juvenil']").focus();
		}
		
		function ordenar_click(td, demarcacion) {
			var columna = td.parent().find("td:visible").index(td);
			var ascendente = td.find("span").text() == '▾';

			var trs = $('tr[id="jugadores' + demarcacion + '"]');
			for (var i = trs.size() - 2; i >= 0; i--) {
                   for (var j = 0; j <= i; j++) {
					var tr1, tr2;
					if (td.hasClass('es_texto')) {
						// Mayores: fecha y fiable
						// Juveniles: edad y posición
						tr1 = $($(trs[j]).find('td:visible')[columna]).text();
						tr2 = $($(trs[j+1]).find('td:visible')[columna]).text();
					} else {
                    	tr1 = parseFloat($($(trs[j]).find('td:visible')[columna]).text().replace(/,/g, ''));
						if (isNaN(tr1)) {
							tr1 = 0;
						}

						tr2 = parseFloat($($(trs[j+1]).find('td:visible')[columna]).text().replace(/,/g, ''));
						if (isNaN(tr2)) {
							tr2 = 0;
						}
					}

                   	if (tr1 != tr2 && (tr1 < tr2 ^ ascendente)) {
						<c:if test="${empty sessionScope.juveniles && sessionScope.usuario.def_tid > 1000}">
							// Primero movemos los entrenamientos
							$(trs[j]).parent().next().insertAfter($(trs[j+1]).parent().next());
							// Luego al jugador
							$(trs[j]).parent().insertAfter($(trs[j+1]).parent().next());
						</c:if>
						<c:if test="${not empty sessionScope.juveniles || sessionScope.usuario.def_tid < 1000}">
							$(trs[j]).insertAfter($(trs[j+1]));
						</c:if>
						// Refresco los trs en el nuevo orden
        				trs = $('tr[id="jugadores' + demarcacion + '"]');
					}
                   }
               }
               td.parent().find(".span_ordenar").text('');
               td.find(".span_ordenar").text(ascendente ? '▴' : '▾');
        }
	
		function actualizar_submit() {
			if ($('#confirmed').val() == '1' || !$("#ilogin").val() && '${sessionScope.usuario.def_tid < 1000}' == 'true') {
				mostrar_mensaje('<fmt:message key="common.updating" />');
				return true;
			} else {
				$('#boton_actualizar').attr("disabled", true);
				let data = 'ilogin=' + encodeURIComponent($("#ilogin").val()) + '&ipassword=' + encodeURIComponent($("#ipassword").val());
				$.post('https://sokker.org/start.php?session=xml&' + data, data).done(
			        function(data) {
						$('#boton_actualizar').attr("disabled", false);
			        	if (data.startsWith('OK')) {
							$('#confirmed').val('1');
							$('#boton_actualizar').click();
			            } else {
			            	alert('<fmt:message key="messages.login_error" var="var"/>${fn:replace(var, "'", "\\'")}' + ": " + data);
			            }
			        } 
			    ).fail(function(xhr, textStatus, errorThrown) {
			    	alert('<fmt:message key="messages.cors" />');
			    });
			 	return false;
			}
		}
		
		function comprobar_filtro(j, nombre) {
			let valor = parseInt(j.find('.' + nombre).text());
			return valor < parseInt($('#filtro_' + nombre + '_desde').val()) || $('#filtro_' + nombre + '_hasta').val() && valor > parseInt($('#filtro_' + nombre + '_hasta').val());
		}
		
		function filtro_change() {
			// Oculta los entrenamientos
			desplegar_menu_click();
			
			$('tr[id^="jugador"]').each(function() {
				if (comprobar_filtro($(this), 'edad')
				        || comprobar_filtro($(this), 'forma')
				        || comprobar_filtro($(this), 'condicion')
				        || comprobar_filtro($(this), 'rapidez')
				        || comprobar_filtro($(this), 'tecnica')
				        || comprobar_filtro($(this), 'pases')
				        || comprobar_filtro($(this), 'porteria')
				        || comprobar_filtro($(this), 'defensa')
				        || comprobar_filtro($(this), 'creacion')
				        || comprobar_filtro($(this), 'anotacion')
				        || comprobar_solo_nt($(this))
				        || comprobar_solo_juveniles($(this))) {
					$(this).hide();
				} else {
				    $(this).show();
				}
			});
		}

		function comprobar_solo_nt(j) {
		    return $('#solo_nts').is(":checked") && j.find('.estrella').size() == 0;
		}

		function comprobar_solo_juveniles(j) {
		    return $('#solo_juveniles').is(":checked") && j.find('.col_talento').text().trim().length == 0;
		}

		function idioma_change() {
			location.href = '${pageContext.request.contextPath}/asistente/idioma?lang=' + $('.dropdown').attr('data-toggle');
		}

		function enviar_skmail_click() {
			if ($('#confirmed2').val() == '1') {
				enviar_skmail_confirmado();
			} else {
				$('#boton_enviar_skmail').attr("disabled", true);
				let data = 'ilogin=' + encodeURIComponent($("#ilogin2").val()) + '&ipassword=' + encodeURIComponent($("#ipassword2").val());
				$.post('https://sokker.org/start.php?session=xml&' + data, data).done(
			        function(data) {
						$('#boton_actualizar').attr("disabled", false);
			        	if (data.startsWith('OK')) {
							$('#confirmed2').val('1');
							$('#boton_enviar_skmail').click();
			            } else {
			            	alert('<fmt:message key="messages.login_error" var="var"/>${fn:replace(var, "'", "\\'")}');
			            }
			        } 
			    ).fail(function(xhr, textStatus, errorThrown) {
			    	alert('<fmt:message key="messages.cors" />');
			    });
			 	return false;
			}
		}

		function enviar_skmail_confirmado() {
			mostrar_mensaje('<fmt:message key="menu.sending_skmails" />');
			
			$.ajax({
				url: "${pageContext.request.contextPath}/servlet/enviar_skmail?pids=" + get_checked() + "&" + $('#form_skmail').serialize()
			}).done(function(resp) {
				ocultar_mensaje();
				$('#editar_skmail').slideUp('fast');
				$('#boton_enviar_skmail').attr("disabled", false);
				$('.mensaje').slideUp('fast');
				anyadir_mensaje('<fmt:message key="common.ready" />');
			}).fail(function(error) {
				ocultar_mensaje();
				setTimeout(function() {
					alert(error.statusText);
				}, 1);
			});
		}
		
		function nuevo_jugador_click(boton) {
			var nuevo = $('#editar_nuevo_jugador');
			nuevo.css('left', boton.offset().left + boton.width() / 2);
			nuevo.css('top', boton.offset().top + boton.height() / 2);
			nuevo.slideDown('fast');
		}
		
		function borrar_usuario_click(usuario) {
		    if (confirm('¿Borrar el usuario "' + usuario + '"?')) {
				location.href='${pageContext.request.contextPath}/asistente/borrar_usuario?usuario=' + usuario;
		    }
		}

		function carga() {
			$(document).on("click", ".boton_minutos", function() {
				//$(this).removeClass("boton_minutos");
				const minutos = $($(this).siblings('.minutos')[0]);
				if (minutos.find('input').length == 0) {
					minutos.html("<input type='text' size='1' value='" + minutos.text() + "' />");
				}
				minutos.find('input').select().focus();
			});

			$(document).on("click", ".talento", function() {
				const pid = $(this).closest("tr").attr("id").split("_")[0].substr(2);
				const nuevo = prompt('<fmt:message key="training.new_talent" />:', $('#talento' + pid).val());
				if (nuevo != null) {
					location.href='${pageContext.request.contextPath}/asistente/actualizar_talento?pid=' + pid + '&talento=' + nuevo;
				}
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

<%-- Para los desplegables --%>
<%
request.setAttribute("j", new Jugador());
%>

<body onload="carga()">
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
		<c:forEach var="lang" items="<%=new String[] {\"EN\",\"ES\",\"FR\",\"IT\" }%>">
			<li onClick="dropdown_click(this)" data-toggle="${lang}" title="<%= Util.initCap(new Locale(((String)pageContext.getAttribute("lang"))).getDisplayLanguage()) %>">
				<img src="${pageContext.request.contextPath}/img/banderas/${lang == 'EN' ? 'GB' : lang}.png" class="margin-right"/>
			</li>
		</c:forEach>
	</tags:desplegable>

	<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto;">
		
		<div id="menu" style="float: left; padding: 10px; width: 280px;">
			<%----------%>
			<%-- LOGO --%>
			<%----------%>
			<div class="menu" style="border-radius: 50px;">
				<div class="bloque doble" style="border-radius: 50px; padding: 10px;">
					<img width="175" src="${pageContext.request.contextPath}/img/sa.png" />
				</div>
			</div>

			<br />

			<%----------%>
			<%-- MENÚ --%>
			<%----------%>
			<div class="menu">
				<div class="cabecera">
					<b><fmt:message key="menu.hello">
						<fmt:param value="${sessionScope.usuario.login}" />
					</fmt:message></b>
				</div>
				<div class="bloque doble" style="text-align: center;">
					<c:if test="${sessionScope.usuario.def_tid < 1000}">
						<a href="${pageContext.request.contextPath}/asistente/cambiar_equipo">
							<fmt:message key="menu.change_to" /> <c:out value="${sessionScope.usuario.equipo}" />
						</a>
				</div>
				<div class="bloque doble">
					</c:if>

					<c:if test="${sessionScope.usuario.def_tid > 1000 && not empty sessionScope.usuario.tid_nt}">
						<a href="${pageContext.request.contextPath}/asistente/cambiar_equipo">
							<fmt:message key="menu.change_to" /> <c:out value="${sessionScope.usuario.equipo_nt}" />
						</a>
						<img src="https://files.sokker.org/pic/flags/${sessionScope.usuario.tid_nt < 400 ? sessionScope.usuario.tid_nt : sessionScope.usuario.tid_nt - 400}.png" />
				</div>
				<div class="bloque doble">
					</c:if>

					<c:if test="${sessionScope.usuario.def_tid > 1000 && sessionScope.usuario.scout_de.size() > 0}">
						<c:forEach items="${sessionScope.usuario.scout_de}" var="entry">
							<a href="${pageContext.request.contextPath}/asistente/cambiar_equipo?coach=${entry.key}">
								<fmt:message key="menu.change_to" /> <c:out value="${entry.value.equipo_nt}" />
							</a>
							<img src="https://files.sokker.org/pic/flags/${entry.value.tid_nt < 400 ? entry.value.tid_nt : entry.value.tid_nt - 400}.png" />
							<br />
						</c:forEach>
				</div>
				<div class="bloque doble">
					</c:if>

					<%@include file="config.jsp" %>

				</div>
				<div class="fin_bloque doble">
					<input type="button" value="<fmt:message key="menu.logout" />" onclick="location.href='${pageContext.request.contextPath}/asistente/logout'" />
				</div>
			</div>
			
			<br />

			<div class="menu">
				<div class="cabecera">
					<b><fmt:message key="common.players" /></b>
				</div>
				<div class="bloque doble">
					<div style="display: inline-block; margin: auto; text-align: right;">
						<form method="post" action="${pageContext.request.contextPath}/asistente/actualizar" onsubmit="return actualizar_submit()">
							<input type="hidden" id="confirmed" name="confirmed" value="${pageContext.request.serverName == 'localhost' ? '1' : ''}" />

							<fmt:message key="login.sokker_login" />: <input type="text" id="ilogin" name="ilogin" value="${sessionScope.usuario.login_sokker}" size="10" <c:if test='${sessionScope.usuario.def_tid > 1000}'>required="required"</c:if> /><br/>
							<fmt:message key="login.sokker_password" />: <input type="password" id="ipassword" name="ipassword" size="10" <c:if test='${sessionScope.usuario.def_tid > 1000}'>required="required"</c:if> value="${cookie.apassword.value}" /><br/>

							<c:if test="${sessionScope.usuario.def_tid > 1000}">
								<label class="peque" for="actualizacion_automatica">
									<input type="checkbox" name="actualizacion_automatica" id="actualizacion_automatica" ${empty sessionScope.usuario.actualizacion_automatica ? '' : 'checked'} />
									<fmt:message key="menu.automatic_update" />
								</label>
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_automatic_update" />">help</div>
								<br />
							</c:if>
							
							<input type="submit" id='boton_actualizar' value="<fmt:message key="common.update" />" onclick="return actualizar_submit();" />
							<c:if test="${actualizado}">
								<span class="material-icons verde vertical" title="<fmt:message key="messages.updated" />">done</span>
							</c:if>
							<c:if test="${!actualizado}">
								<span class="material-icons rojo vertical" title="<fmt:message key="messages.not_updated" />">close</span>
							</c:if>
							<br/>
						</form>
					</div>
				</div>

				<div class="fin_bloque doble">
					<c:if test="${sessionScope.usuario.def_tid > 1000}">
						<div class="boton grande" onclick="location.href='${pageContext.request.contextPath}/asistente/cambiar_vista?juveniles=${1 - sessionScope.juveniles}'">
							<span class="material-icons md-32" style="color: ${empty sessionScope.juveniles ? 'black' : 'brown'}"><c:out value="${empty sessionScope.juveniles ? 'school' : 'directions_run'}" /></span>
							<div class="peque">
								<c:if test="${empty sessionScope.juveniles}">
									<fmt:message key="common.juniors" />
								</c:if>
								<c:if test="${not empty sessionScope.juveniles}">
									<fmt:message key="common.players" />
								</c:if>
							</div>
						</div>
					</c:if>
					
					<c:if test="${sessionScope.usuario.def_tid < 1000}">
						<div class="boton grande" onclick="nuevo_jugador_click($(this));">
							<span class="material-icons md-32" style="color: brown">directions_run</span>
							<div class="peque">
								<fmt:message key="menu.new_player" />
							</div>
						</div>
					</c:if>
					
					<c:if test="${sessionScope.usuario.def_tid > 1000}">
						<div class="boton grande" onclick="nuevo_jugador_click($(this));">
							<span class="material-icons md-32" style="color: gray">biotech</span>
							<div class="peque">
								<fmt:message key="menu.test_player" />
							</div>
						</div>
					</c:if>
										
					<div class="boton grande" onclick="location.href='${pageContext.request.contextPath}/asistente/cambiar_vista?juveniles=${sessionScope.juveniles}&historico=${1 - sessionScope.historico}'">
						<span class="material-icons md-32"><c:out value="${empty sessionScope.historico ? 'history' : 'update'}" /></span>
						<div class="peque">
							<c:if test="${empty sessionScope.historico}">
								<fmt:message key="common.history" />
							</c:if>
							<c:if test="${not empty sessionScope.historico}">
								 <fmt:message key="common.current" />
							</c:if>
						</div>
					</div>

					<div class="boton grande" onclick="filtros_click($(this));">
						<span class="material-icons md-32" style="color: green">filter_alt</span>
						<div class="peque">
							<fmt:message key="common.filters" />
						</div>
					</div>
					
					<div class="boton grande" onclick="proyectar_nuevo_click($(this))">
 						<span class="material-icons md-32 borde-morado" style="color: pink;">calculate</span>
						<div class="peque">
							<fmt:message key="common.projection" />
						</div>
					</div>
					
					<div class="boton grande" id="boton_notas" onclick="notas_click($(this))">
						<span class="material-icons md-32 blanco borde-negro" style="opacity: 0.8;"><c:out value="${empty sessionScope.historico ? 'chat_bubble' : 'textsms'}" /></span>
						<div class="peque">
							<fmt:message key="common.notes" />
						</div>
					</div>
 
					<div class="boton grande" id="boton_notas" onclick="instalacion_click($(this))">
						<span class="material-icons md-32 azul">download</span>
						<div class="peque">
							<fmt:message key="menu.installation" />
						</div>
					</div>

					<c:if test="${empty sessionScope.juveniles}">
						<div class="boton grande" onclick="acciones_click($(this))">
							<span class="material-icons md-32 borde-cuadrado" style="opacity: 0.8; background-color: white;">check</span>
							<div class="peque">
								<fmt:message key="menu.actions" />
							</div>
						</div>
					</c:if>

					<c:if test="${sessionScope.usuario.def_tid < 1000}">
						<div class="boton grande" onclick="skmail_click($(this))">
							<span class="material-icons md-32 blanco borde-negro" style="opacity: 0.8;">email</span>
							<div class="peque">
								<fmt:message key="common.skmail" />
							</div>
						</div>
					</c:if>
					
				</div>
			</div>

			<br />

<%-- 			<c:if test="${sessionScope.usuario.def_tid < 1000}"> --%>
				<c:set var="j" value="<%=new Jugador()%>" />
				<div id="editar_nuevo_jugador" class="menu nt sombra" style="position: absolute; display: none; z-index: 100;">
					<div class="cabecera">
						<c:if test="${sessionScope.usuario.def_tid < 1000}">
							<b><fmt:message key="menu.new_player" /></b>
						</c:if>
						<c:if test="${sessionScope.usuario.def_tid > 1000}">
							<b><fmt:message key="menu.test_player" /></b>
						</c:if>
					</div>
					<div class="fin_bloque doble editar" style="text-align: center; margin: auto;">
						<form method="post" action="${pageContext.request.contextPath}/asistente/grabar">
							<div style="display: inline-block; text-align: right; margin: auto">
								<c:if test="${sessionScope.usuario.def_tid < 1000}">
									<fmt:message key="common.pid" />: <input type="number" name="pid" required="required"/><br/>
								</c:if>
								<c:if test="${sessionScope.usuario.def_tid > 1000}">
									<fmt:message key="skills.age" />: <input type="number" name="edad" required="required"/><br/>
								</c:if>
							</div>

							<br/><br/>
							<table style="text-align: left; margin: auto;" class="fondo_tabla peque">
								<tr>
									<td><fmt:message key="skills.stamina" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'stamina', 0, sessionScope.usuario.numeros, 11)}</td><td><fmt:message key="skills.keeper" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'keeper', 0, sessionScope.usuario.numeros)}</td>
								</tr>
								<tr>
									<td><fmt:message key="skills.pace" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'pace', 0, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.defender" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'defender', 0, sessionScope.usuario.numeros)}</td>
								</tr>
								<tr>
									<td><fmt:message key="skills.technique" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'technique', 0, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.playmaker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'playmaker', 0, sessionScope.usuario.numeros)}</td>
								</tr>
								<tr>
									<td><fmt:message key="skills.passing" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'passing', 0, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.striker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'striker', 0, sessionScope.usuario.numeros)}</td>
								</tr>
								<tr style="background-color: transparent;">
									<td colspan="4">&nbsp;</td>
								</tr>
								<tr style="background-color: transparent; font-size: initial;">
									<td colspan="4">
										<fmt:message key="common.position" />: <c:out value="${j.despDemarcacion_asistente(null, sessionScope.usuario)}" escapeXml="false"/>
										<fmt:message key="common.color" />: <input type="color" name="color" />
										<c:if test="${sessionScope.usuario.def_tid < 1000}">
											<label for="fiable">
												<input type="checkbox" id="fiable" name="fiable" /><fmt:message key="players.reliable" />
											</label>
										</c:if>
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
<%-- 			</c:if> --%>

			<c:if test="${sessionScope.usuario.def_tid > 1000}">
				<div class="menu">
					<div class="cabecera">
						<b><fmt:message key="common.training" /></b>
					</div>
					<div class="fin_bloque doble" style="text-align: left; margin: auto;">
						<label class="etiqueta"><fmt:message key="common.trainer" />:</label>
						<c:out value="${sessionScope.usuario.str_entrenador}" />
						<br/>
						<label class="etiqueta"><fmt:message key="common.assistants" />:</label>
						<c:out value="${sessionScope.usuario.str_nivel_asistentes}" />
						<br/>
						<label class="etiqueta"><fmt:message key="common.juniors" />:</label>
						<c:out value="${sessionScope.usuario.str_nivel_juveniles}" />
						<br/>
						<label class="etiqueta"><fmt:message key="common.goalkeepers" />:</label>
						${sessionScope.usuario.getStr_tipo_entrenamiento(0)}
						<br/>
						<label class="etiqueta"><fmt:message key="common.defenders" />:</label>
						${sessionScope.usuario.getStr_tipo_entrenamiento(1)}
						<br/>
						<label class="etiqueta"><fmt:message key="common.midfielders" />:</label>
						${sessionScope.usuario.getStr_tipo_entrenamiento(2)}
						<br/>
						<label class="etiqueta"><fmt:message key="common.forwards" />:</label>
						${sessionScope.usuario.getStr_tipo_entrenamiento(3)}
					</div>
				</div>
			</c:if>
			
			<c:if test="${not empty sessionScope.admin}">
				<br/>
				<div class="cabecera">
					Varios
				</div>
				<div class="fin_bloque doble" align="left">
					<a target="_blank" href="${pageContext.request.contextPath}/asistente/ver?NTDB.properties">NTDB.properties</a>
					|
					<a target="_blank" href="${pageContext.request.contextPath}/asistente/ver?logs/__NTDB.log">logs/__NTDB.log</a>
					<br/>
					<a target="_blank" href="${pageContext.request.contextPath}/asistente/ver?IPs.properties">IPs.properties</a>
					|
					<a target="_blank" href="${pageContext.request.contextPath}/asistente/ver?logs/__BLOQUEO.log">logs/__BLOQUEO.log</a>

					<br /><br />

					<a href="${pageContext.request.contextPath}/asistente/limpiar_ips">Limpiar IPs</a>
					<br/>
					<a href="${pageContext.request.contextPath}/asistente/actualizar_seleccionadores">Actualizar seleccionadores</a>
					<br/>
					<a href="${pageContext.request.contextPath}/asistente/tareas_NTs">Tareas NTs</a>
					<br/>
					<a href="${pageContext.request.contextPath}/asistente/enviar_backups">Enviar backups</a>
					
					<br/><br />
					
					<%= Util.mostrar_espacio_libre() %>
					<%= SystemUtil.mostrar_expiracion_certificado() %>
				</div>

				<br/>
					
				<div class="cabecera">
					<span style="cursor:pointer;" onclick="$('#lista_usuarios').show(); $(this).hide();">▶</span>
					Usuarios
				</div>
				<div style="display:none" class="fin_bloque doble" align="left" id="lista_usuarios">
					<a target="_blank" href="${pageContext.request.contextPath}/asistente/ver?logs/_${sessionScope.usuario.login}.log">Logs</a>
					| Backups:
					<%= AsistenteBO.listar_jornadas((Usuario)session.getAttribute("usuario")) %>
					<label for="recuperar">
						<input type="checkbox" id="recuperar" />Recuperar backup
					</label>
					<br/>
					<hr/>
					<%= AsistenteBO.listar_usuarios() %>
				</div>
			</c:if>

			<br />
			
			<div style="width:280px;height:800px;" class="solo_pc">
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
		
		<div id="desplegar_menu" title="<fmt:message key="menu.show_menu" />" class="borde flecha" style="display: none; float: left; color: #e2eff8;" onclick="desplegar_menu_click()">▶</div>

		<%---------------%>
		<%-- JUGADORES --%>
		<%---------------%>
		
		<div style="display: inline-block; text-align: left; padding-top: 10px;">
			<h2 class="texto_3d">
				<c:out value="${sessionScope.usuario.def_tid < 1000 ? sessionScope.usuario.equipo_nt : sessionScope.usuario.equipo}" />
				<c:if test="${not empty sessionScope.historico}">
					(<fmt:message key="common.history" />)
				</c:if>
			</h2>

			<table id="jugadores" style="text-align: center; border-spacing: 1px;">
				<c:if test="${empty sessionScope.juveniles}">
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.GK %>" titulo="common.goalkeepers" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.DEF %>" titulo="common.defenders" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.MID %>" titulo="common.midfielders" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.ATT %>" titulo="common.forwards" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					<c:if test="${sessionScope.usuario.mostrar_banquillo}">
						<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.BANQUILLO %>" titulo="common.bench" />
						<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
					</c:if>
					<xtag:jugadores demarcacion="<%= DEMARCACION_ASISTENTE.OTRA %>" titulo="common.other_position" />
					<tr><td class="fijo" colspan="100">&nbsp;</td></tr>
				</c:if>
				<xtag:jugadores demarcacion="${null}" titulo="common.not_assigned" />
			</table>
			<br />
			
			<!--  InArticle -->
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

	<%--------------------------%>
	<%-- EDITAR ENTRENAMIENTO --%>
	<%--------------------------%>

	<div id="editar_entrenamiento" class="menu sombra" style="position: absolute; display: none; text-align: center; z-index: 100;">
		<div class="cabecera">
			<b><span id='nombre'></span> - <fmt:message key="common.round" /> <span id='jornada'></span></b>
		</div>

		<div class="fin_bloque doble editar">
			<form method="post" action="${pageContext.request.contextPath}/asistente/actualizar_entrenamiento">
				<input type="hidden" name="jornada_entrenamiento">
				<input type="hidden" name="pid_entrenamiento">
				<input type="hidden" name="habilidad_entrenamiento">
				
				<table style="text-align: left; white-space: nowrap;" align="center">
					<tr>
						<td colspan="2">
							<b><fmt:message key="training.individual_training" />:</b>
							<hr class="hr" />
						</td>
					</tr>
					<tr>
						<td><fmt:message key="common.level" />:</td>
						<td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'nivel_entrenamiento', 0, sessionScope.usuario.numeros)}</td>
					</tr>
					<tr>
						<td><fmt:message key="training.training_points" />:</td>
						<td><input name="puntos_entrenamiento" type="number" min="0" step="any" required="required" style="width: 60px" /></td>
					</tr>
					<tr class="nuevo_entreno">
						<td><label for="avanzado"><fmt:message key="training.advanced_training" />:</label></td>
						<td>
						<input type="checkbox" name="avanzado" id="avanzado" style="width: 60px" /></td>
					</tr>					
					<tr>
						<td><fmt:message key="common.injury" />:</td>
						<td><input name="lesion" type="number" step="1" min="0" style="width: 60px" /></td>
					</tr>
					<tr>
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

					<tr id="tr_semanas" style="display: none;">
						<td>Number of weeks:</td>
						<td><input type="number" name="semanas" min="1" step="1" value="1" style="width: 60px" /></td>
					</tr>
					
					<tr>
						<td class="peque">&nbsp;</td>
					</tr>
					
					<tr>
						<td colspan="2">
							<b><fmt:message key="training.team_training" />:</b>
							<hr class="hr" />
						</td>
					</tr>
					<tr class="viejo_entreno">
						<td><fmt:message key="training.type" />:</td>
						<td>
							<select name="tipo_entrenamiento">
								<c:forEach items="<%= TIPO_ENTRENAMIENTO.values() %>" var="tipo">
									<option value="${tipo}"><fmt:message key="skills.${tipo.ingles}" /></option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr class="viejo_entreno">
						<td><fmt:message key="common.position" />:</td>
						<td>
							<select name="demarcacion_equipo">
								<c:forEach items="<%= DEMARCACION.values() %>" var="pos">
									<option>${pos}</option>
								</c:forEach>
							</select>
						</td>
					</tr>

					<tr class="nuevo_entreno">
						<td><fmt:message key="common.goalkeepers" />:</td>
						<td>
							<select name="tipo_entrenamiento_gk">
								<c:forEach items="<%= TIPO_ENTRENAMIENTO.values() %>" var="tipo">
									<option value="${tipo}"><fmt:message key="skills.${tipo.ingles}" /></option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr class="nuevo_entreno">
						<td><fmt:message key="common.defenders" />:</td>
						<td>
							<select name="tipo_entrenamiento_def">
								<c:forEach items="<%= TIPO_ENTRENAMIENTO.values() %>" var="tipo">
									<option value="${tipo}"><fmt:message key="skills.${tipo.ingles}" /></option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr class="nuevo_entreno">
						<td><fmt:message key="common.midfielders" />:</td>
						<td>
							<select name="tipo_entrenamiento_mid">
								<c:forEach items="<%= TIPO_ENTRENAMIENTO.values() %>" var="tipo">
									<option value="${tipo}"><fmt:message key="skills.${tipo.ingles}" /></option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr class="nuevo_entreno">
						<td><fmt:message key="common.forwards" />:</td>
						<td>
							<select name="tipo_entrenamiento_att">
								<c:forEach items="<%= TIPO_ENTRENAMIENTO.values() %>" var="tipo">
									<option value="${tipo}"><fmt:message key="skills.${tipo.ingles}" /></option>
								</c:forEach>
							</select>
						</td>
					</tr>


					<tr>
						<td><fmt:message key="common.trainer" />:</td>
						<td><input type="text" name="tr_entrenador" size="16"></input></td>
					</tr>
					<tr>
						<td><fmt:message key="common.assistants" />:</td>
						<td><input type="number" step="any" name="tr_asistentes" style="width: 60px;"></input></td>
					</tr>
					<tr>
						<td><fmt:message key="common.juniors" />:</td>
						<td><input type="number" step="any" name="tr_juveniles" style="width: 60px;"></input></td>
					</tr>
				</table>
				<input id="actualizar_entrenamiento_submit" type="submit" value="<fmt:message key="common.change" />" style="margin-top: 5px;"/>
				<c:if test="${not empty sessionScope.admin}">
					<span class="material-icons boton gris vertical" id="tr_borrar_entrenamiento" title="<fmt:message key="training.remove" />" onclick="if (confirm('<fmt:message key="training.remove" />?')) borrar_entrenamiento_click();">delete</span>
				</c:if>
			</form>
		</div>
	</div>

	<div id="editar_chart" class="sombra grafica">
		<canvas></canvas>
	</div>

	<!-- PROYECCIÓN -->
	<div id="editar_proyeccion" class="sombra editar" style="position: absolute; display: none;">
		<div class="cabecera" style="text-align: center;">
			<fmt:message key="players.skills_projection" />
		</div>

		<div class="fin_bloque doble">
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

			<table style="text-align: left; margin: auto;" class="fondo_tabla peque">
				<tr style="background-color: transparent; text-align: center;">
					<td colspan="2"><b><fmt:message key="skills.age" />:</b> <select id="pr_edad" onchange="proyeccion_change()"></select></td>
					<td colspan="2"><b><fmt:message key="skills.talent" />:</b> <input type="number" id="pr_talento" onchange="proyeccion_change()" style="width:40px;" step="0.1"/></td>
				</tr>
				<tr style="background-color: transparent; font-size: 0.1em;">
					<td>&nbsp;</td>
				</tr>
				
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.stamina" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'stamina', 0, sessionScope.usuario.numeros, 11)}</td><td><fmt:message key="skills.keeper" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'keeper', 0, sessionScope.usuario.numeros)}</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.pace" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'pace', 0, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.defender" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'defender', 0, sessionScope.usuario.numeros)}</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.technique" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'technique', 0, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.playmaker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'playmaker', 0, sessionScope.usuario.numeros)}</td>
				</tr>
				<tr class="select_proyeccion">
					<td><fmt:message key="skills.passing" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'passing', 0, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.striker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'striker', 0, sessionScope.usuario.numeros)}</td>
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
	
	<!-- EDITAR JUVENIL -->
	<div id="editar_juvenil" class="sombra" style="position: absolute; display: none;">
		<form method="post" action="${pageContext.request.contextPath}/asistente/actualizar_juvenil">
			<input type="hidden" name="pid_juvenil" />

			<div class="cabecera" style="text-align: center;">
				<span id="nombre_juvenil"></span>
			</div>
	
			<div class="fin_bloque doble editar" align="center">
				<b><fmt:message key="juniors.enter_junior_levels" />:</b>
				<br/>
				<input type="text" style="margin: 5px;" name="niveles_juvenil" size="60"/>
				<br/>
				<input type="submit" />
			</div>
		</form>
	</div>

	<!-- FILTROS -->
	<div class="sombra editar" style="position:absolute; display: none;" id="editar_filtros">
		<table class="fondo_tabla tabla" style="margin-bottom: -10px;">
			<thead>
				<tr>
					<th>&nbsp;</th>
					<th><fmt:message key="common.from" /></th>
					<th><fmt:message key="common.to" /></th>
					<th>&nbsp;</th>
					<th><fmt:message key="common.from" /></th>
					<th><fmt:message key="common.to" /></th>
				</tr>
			</thead>
			<tr>
				<td><b><fmt:message key="skills.age" /></b></td>
				<td>
					<select id="filtro_edad_desde" onchange="filtro_change();">
						<c:forEach begin="16" end="${sessionScope.usuario.def_tid > 400 && sessionScope.usuario.def_tid < 1000 && empty sessionScope.historico ? 21 : 30}" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_edad_hasta" onchange="filtro_change();">
						<c:forEach begin="16" end="${sessionScope.usuario.def_tid > 400 && sessionScope.usuario.def_tid < 1000 && empty sessionScope.historico ? 20 : 30}" var="i">
							<option>${i}</option>
						</c:forEach>
						<c:if test="${sessionScope.usuario.def_tid < 400 || sessionScope.usuario.def_tid > 1000 || not empty sessionScope.historico}">
							<option value="" selected>99</option>
						</c:if>
						<c:if test="${sessionScope.usuario.def_tid > 400 && sessionScope.usuario.def_tid < 1000 && empty sessionScope.historico}">
							<option selected>21</option>
						</c:if>
					</select><br/>
				</td>
				<td><b><fmt:message key="skills.form" /></b></td>
				<td>
					<select id="filtro_forma_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_forma_hasta" onchange="filtro_change();" >
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
			</tr>
	
			<tr>
				<td><fmt:message key="skills.stamina" /></td>
				<td>
					<select id="filtro_condicion_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="11" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_condicion_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="11" var="i">
							<option ${i==11?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td><fmt:message key="skills.keeper" /></td>
				<td>
					<select id="filtro_porteria_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_porteria_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
			</tr>
	
			<tr>
				<td><fmt:message key="skills.pace" /></td>
				<td>
					<select id="filtro_rapidez_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_rapidez_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td><fmt:message key="skills.defender" /></td>
				<td>
					<select id="filtro_defensa_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_defensa_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
			</tr>
	
			<tr>
				<td><fmt:message key="skills.technique" /></td>
				<td>
					<select id="filtro_tecnica_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_tecnica_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td><fmt:message key="skills.playmaker" /></td>
				<td>
					<select id="filtro_creacion_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_creacion_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
			</tr>
	
			<tr>
				<td><fmt:message key="skills.passing" /></td>
				<td>
					<select id="filtro_pases_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_pases_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td><fmt:message key="skills.striker" /></td>
				<td>
					<select id="filtro_anotacion_desde" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
				<td>
					<select id="filtro_anotacion_hasta" onchange="filtro_change();">
						<c:forEach begin="0" end="18" var="i">
							<option ${i==18?'selected':''}>${i}</option>
						</c:forEach>
					</select><br/>
				</td>
			</tr>
		</table>

		<c:if test="${sessionScope.usuario.def_tid > 1000}">
			<div style="margin: 10px;">
				<label for="solo_juveniles">
					<input type="checkbox" id="solo_juveniles" onclick="filtro_change()" />
					<span class="material-icons vertical">school</span>
					<span class="peque">
						<fmt:message key="menu.only_juniors" />
					</span>
				</label>
			</div>
		</c:if>
		<c:if test="${sessionScope.usuario.def_tid < 1000}">
			<div style="margin: 10px;">
				<label for="solo_nts">
					<input type="checkbox" id="solo_nts" onclick="filtro_change()" />
					<span class='estrella'>&#x2605;</span>
					<span class="peque">
						<fmt:message key="menu.only_nts" />
					</span>
				</label>
			</div>
		</c:if>
	</div>

	<!-- EXPORTACIÓN -->
	<div style="position:absolute; display: none;" id="editar_bbcode">
		<textarea class="sombra" cols="100" rows="20" onclick="select()"></textarea>
	</div>

	<!-- EXPORTAR JUGADOR -->
	<div style="position:absolute; display: none;" id="editar_bbcode_jugador">
		<textarea class="sombra" cols="80" rows="12" onclick="select()"></textarea>
	</div>

	<!-- NOTAS GENERALES -->
	<div class="sombra" style="position:absolute; display: none; padding: 5px; background-color: lightgray;" id="editar_notas">
		<textarea cols="100" rows="20">${sessionScope.usuario.notas}</textarea>
		<div style="text-align: right">
			<input type="button" onclick="guardar_notas_click()" value="<fmt:message key="common.save" />" />
		</div>
	</div>

	<c:if test="${sessionScope.usuario.def_tid < 1000}">
		<!-- SK-MAIL -->
		<div class="sombra" style="position:absolute; display: none; padding: 5px; background-color: lightgray; text-align: left;" id="editar_skmail">
			<form method="post" id="form_skmail" action="javascript:enviar_skmail_confirmado()" onsubmit="return false;">
				<b><fmt:message key="login.sokker_login" />:</b>
				<input type="text" id="ilogin2" name="ilogin2" value="${sessionScope.usuario.login_sokker}" size="10" required="required" />
				<br />
				<b><fmt:message key="login.sokker_password" />:</b>
				<input type="password" id="ipassword2" name="ipassword2" size="10" required="required" value="${cookie.apassword.value}" />
				<br /><br />
				<input type="hidden" id="confirmed2" name="confirmed2" value="${pageContext.request.serverName == 'localhost' ? '1' : ''}" />
	
				<b><fmt:message key="common.subject" /></b>
				<br />
				<input type="text" name="asunto" value="%player [%pid]" size="50" />
				<br /><br />
				<b><fmt:message key="common.message" /></b>
				<br />
				<textarea name="mensaje" cols="50" rows="10">Hi %user,

Can you show me [pid=%pid]%player[/pid]'s skills?

Thank you!</textarea>
				<div style="text-align: right">
					<input type="button" id="boton_enviar_skmail" onclick="return enviar_skmail_click()" value="<fmt:message key="menu.send_skmails" />" />
				</div>
			</form>
		</div>
	</c:if>

	<c:if test="${empty sessionScope.juveniles}">
		<!-- ACCIONES -->
		<div id="editar_acciones" class="menu nt sombra" style="position: absolute; display: none; z-index: 100;">
			<div class="cabecera">
				<b><fmt:message key="menu.actions" /></b>
			</div>
			<div class="fin_bloque doble editar" style="text-align: left; margin: auto;">
				<div class="boton" onclick="exportar_click($(this))">
					<fmt:message key="menu.export" />
				</div>
				<c:if test="${sessionScope.usuario.def_tid < 1000}">
					<div class="boton" onclick="exportar_url_click($(this))">
						<fmt:message key="menu.export_to_url" />
					</div>
				</c:if>
				<div class="boton" onclick="borrar_jugadores_click($(this))">
					<fmt:message key="common.remove" />
				</div>
				<div class="boton" onclick="cambiar_demarcacion($(this), 'GK')">
					<fmt:message key="menu.set_as">
						<fmt:param><fmt:message key="common.goalkeepers" /></fmt:param>
					</fmt:message>
				</div>
				<div class="boton" onclick="cambiar_demarcacion($(this), 'DEF')">
					<fmt:message key="menu.set_as">
						<fmt:param><fmt:message key="common.defenders" /></fmt:param>
					</fmt:message>
				</div>
				<div class="boton" onclick="cambiar_demarcacion($(this), 'MID')">
					<fmt:message key="menu.set_as">
						<fmt:param><fmt:message key="common.midfielders" /></fmt:param>
					</fmt:message>
				</div>
				<div class="boton" onclick="cambiar_demarcacion($(this), 'ATT')">
					<fmt:message key="menu.set_as">
						<fmt:param><fmt:message key="common.forwards" /></fmt:param>
					</fmt:message>
				</div>
				<div class="boton" onclick="cambiar_demarcacion($(this), 'BANQUILLO')">
					<fmt:message key="menu.set_as">
						<fmt:param><fmt:message key="common.bench" /></fmt:param>
					</fmt:message>
				</div>
				<div class="boton" onclick="cambiar_demarcacion($(this), 'OTRA')">
					<fmt:message key="menu.set_as">
						<fmt:param><fmt:message key="common.other_position" /></fmt:param>
					</fmt:message>
				</div>
			</div>
		</div>
	</c:if>

	<!-- INSTALACIÓN -->
	<div class="sombra" style="position:absolute; display: none; padding: 5px; background-color: white;" id="editar_instalacion">
		<%@include file="instalacion.jsp" %>
	</div>
</body>
</html>

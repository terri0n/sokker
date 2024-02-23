<%@page import="com.formulamanager.sokker.bo.AsistenteBO"%>
<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="xtag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/mistags.tld" prefix="tags" %>

<!DOCTYPE html>

<html>
<head>
	<script src="https://www.gstatic.com/charts/loader.js"></script>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />

	<title>Sokker Asistente - Form statistics</title>

	<style>
		body{
			background-color: #e2f2f8;
		}
		.titulo {
			color: white;
			background-color: #539bcd;
			font-weight: bolder;
			width: 100%;
			padding: 1px;
		}
		label, .flecha {
			cursor: pointer;
		}
		a, a:visited {
			color: darkblue;
			decoration: none;
		}
		a:hover {
			color: blue;
		}
		hr {
			background-color: #4f8ea2; height: 1px; border: 0;
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
		google.charts.load('current', {packages: ['corechart']});
		
		function drawChart(span, tipo, titulo, datos, hTicks, min_valor, max_valor, min_jornada, max_jornada, colores) {
		    //span.css('left', Math.min(window.innerWidth + window.pageXOffset - span.width(), span.offset().left));
		    //span.css('top', Math.min(window.innerHeight + window.pageYOffset - span.height(), span.offset().top + span.height()));
			//span.show();

			var vTicks = [];
			var salto = parseInt(max_valor / 500) * 100;

			for (var i = 0; i < max_valor + salto; i += salto) {
				vTicks.push(i);
			}
	
			var data = new google.visualization.DataTable();
			data.addColumn('number', 'edad');
			data.addColumn('number', titulo);
			data.addColumn({type:'boolean', role:'scope'});
			data.addRows( datos );
			if (!colores) {
				colores = ['blue', 'red'];
			}
			var options = {colors: colores, "title": "Statistics: form of " + titulo,"legend":{"position":"none"},"explorer":{"axis":"horizontal"},"hAxis":{"ticks":hTicks,"title":'Form',"viewWindow":{"min":min_jornada,"max":max_jornada}},"vAxis":{"minValue":min_valor,"maxValue":max_valor,"ticks":vTicks},"pointSize":5};
			var chart = new google.visualization.AreaChart(span[0]);
			chart.draw(data, options);
		}
		
		function carga() {
			drawChart($('#editar_chart_all'), null, 'all players', <%= AsistenteBO.getDatos_estadistica_forma(true, true) %>);
			<%-- drawChart($('#editar_chart_yes'), null, 'players who played at least 90 minutes in last 4 weeks', <%= AsistenteBO.getDatos_estadistica_forma(false, true) %>); --%>
			drawChart($('#editar_chart_no'), null, 'players who didn\'t play in last 10 weeks', <%= AsistenteBO.getDatos_estadistica_forma(false, false) %>);
		}
	</script>
</head>

<body onload="carga()">
	<div style="background-color: #539bcd; color: white; margin: 1px; text-align: center;">
		Sokker Asistente - Form statistics
	</div>

	<div style="margin: auto; width: 800px; text-align: center;">
		<div id="editar_chart_all" style="display: block; height: 500px; width: 800px;"></div>
		<!-- <div id="editar_chart_yes" style="display: block; height: 500px; width: 800px;"></div> -->
		<div id="editar_chart_no" style="display: block; height: 500px; width: 800px;"></div>
		<ul style="text-align: left">
			<li>Form</li>
			<li><a href="estadisticasEntrenamiento">Training</a></li>
		</ul>
		<br />
		<input type="button" value="<< BACK" onclick="location.href='${pageContext.request.contextPath}/asistente'" />
	</div>
</body>
</html>
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
	<title>Sokker - Juniors calculator</title>
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>
	
	<style>
		body{
			background-color: #e2f2f8;
			margin: 0px;
			padding: 0px;
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
		@media only screen and (max-width: 800px) {
			.solo_pc {
				display: none;
			}
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

	<script type="text/javascript">
		 function addCommas(nStr) {
		     nStr += '';
		     x = nStr.split('.');
		     x1 = x[0];
		     x2 = x.length > 1 ? '.' + x[1] : '';
		     var rgx = /(\d+)(\d{3})/;
		     while (rgx.test(x1)) {
		         x1 = x1.replace(rgx, '$1' + '.' + '$2');
		     }
		     return x1 + x2;
		 }
		 
 		function juvenil(Co, R, T, Pa, Po, D, Cr, A) {
			return (Co + R + T + Pa + Po + D + Cr + A + 1.6) / 3.2;
		}
 
 		function min_val(nivel) {
 			return parseInt(1956.95 * (Math.pow(2, nivel / 2) - 1));
 		}

		function select_change() {
			var Co = parseInt($("select[name='condicion']").val());
			var R = parseInt($("select[name='rapidez']").val());
			var T = parseInt($("select[name='tecnica']").val());
			var Pa = parseInt($("select[name='pases']").val());
			var Po = parseInt($("select[name='porteria']").val());
			var D = parseInt($("select[name='defensa']").val());
			var Cr = parseInt($("select[name='creacion']").val());
			var A = parseInt($("select[name='anotacion']").val());
			var forma = parseInt($("select[name='forma']").val());
			
			$("#nivel").text(Math.round(juvenil(Co, R, T, Pa, Po, D, Cr, A)*10)/10);
			
			var val = min_val(Co) + min_val(R) + min_val(T) + min_val(Pa) + min_val(Po) * 3.65 + min_val(D) + min_val(Cr) + min_val(A);
			val = parseInt(val * (0.55 + (forma / 18.0) * 0.45));
 			val = parseInt(val / 250.0) * 250;

 			$("#min_val").text(addCommas(val));
		}

		function carga() {
			$("select").change(function() {
				select_change();
			});
			
			select_change();
		}
	</script>
</head>

<body onload="carga()">
	<div style="background-color: #4f8ea2; color: white; margin: 1px; text-align: center;">
		<b>Sokker - Juniors calculator</b>
	</div>

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

	<c:set var="j" value="<%= new Jugador() %>" />

	<div style="vertical-align: top; padding: 1px; text-align: center;">
		<br/>
		<table style="text-align: left; display: inline-block">
			<tr>
				<td>
					<c:out value="${j.desp('en', 'condicion', null, true, 11)}" escapeXml="false"/> stamina<br />
					<c:out value="${j.desp('en', 'rapidez', null, true)}" escapeXml="false"/> pace<br />
					<c:out value="${j.desp('en', 'tecnica', null, true)}" escapeXml="false"/> technique<br />
					<c:out value="${j.desp('en', 'pases', null, true)}" escapeXml="false"/> passing<br />
				</td>
				<td>
					<c:out value="${j.desp('en', 'porteria', null, true)}" escapeXml="false"/> keeper<br />
					<c:out value="${j.desp('en', 'defensa', null, true)}" escapeXml="false"/> defender<br />
					<c:out value="${j.desp('en', 'creacion', null, true)}" escapeXml="false"/> playmaker<br />
					<c:out value="${j.desp('en', 'anotacion', null, true)}" escapeXml="false"/> striker<br />
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td><b>Level:</b></td>
				<td><span id="nivel"></span></td>
			</tr>
			<tr>
				<td><b>Form:</b></td>
				<td><c:out value="${j.desp('en', 'forma', 18, true)}" escapeXml="false"/></td>
			</tr>
			<tr>
				<td><b>Minimum value:</b></td>
				<td><span id="min_val"></span> &euro;</td>
			</tr>
		</table>		
		<br/>
		
		<button onclick="location.href='${contextPage.request.contextPath}/'">Back</button>

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
	</div>

</body>
</html>

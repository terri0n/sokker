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
	<title>Sokker</title>
	
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
	<div style="background-color: #4f8ea2; color: white; margin: 1px; text-align: center;">
		<b>Sokker</b>
	</div>

	<div style="vertical-align: top; background-color: #e2f2f8; padding: 1px; text-align: center;">
		<div style="display: inline-block; text-align: left;">
			<ul>
				<li><a href="sete">SETE</a> - Sokker Extended Tactic Editor</li>
				<li><a href="fecha">Date</a> - Creation date of teams by country</li>
				<li><a href="scanner">Scanner</a> - Players scanner</li>
				<li><a href="juniors">Juveniles</a> - Calculate juniors level</li>
				<li><a href="asistente">Asistente</a> - Training assistant</li>
				<li><a href="factorx">Factor X</a> - Factor X (Spain)</li>
				<li><a href="iberia">Copa Iberia</a> - Copa Iberia (Spain)</li>
			</ul>
		</div>
	</div>

</body>
</html>

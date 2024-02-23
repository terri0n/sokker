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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<script type="text/javascript" src="js/ip.js.jsp"></script>

	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="viewport" content="width=device-width, initial-scale=1" />

	<link rel="stylesheet" type="text/css" href="https://sokker.org/static/css/background/court/court.css">
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>

	<title>Sokker Extended Tactic Editor</title>
	
	<style>
		body {
			margin: 0px;
			padding: 0px;
			background-image: url("img/fondo.jpg");
		    background-size: cover;
			background-color: #41637e;
			overflow: auto;
		}
		html {
		    height: 100vh;
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
		.help {
			font-weight: bolder;
			color: #ffffbf;
			background-color: DarkSalmon;
			border: 1px solid black;
			cursor: pointer;
			border-radius: 12px;
			padding-left: 5px;
			padding-right: 5px;
		}
		.help:hover {
			color: yellow;;
			background-color: red;
		}
		.help_popup {
			position: absolute;
			color: black;
			background-color: #ffffbf;
			border: 1px solid black;
			text-align: left;
			display: none;
			margin: auto;
			overflow-y: auto;
			overflow-x: visible;
			padding: 10px;
			text-align: justify;
			text-justify: inter-word;
			z-index: 999;
  		}
	</style>
	
 	<script type="text/javascript" src="${sessionScope.base}js/sete.js.jsp"></script>	

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>
</head>

<body onload="carga();">
	<div style="background-color: #4f8ea2; color: white; margin: 1px; text-align: center;">
		<b>Sokker Extended Tactic Editor, by Terrion :)</b>
		<span class="help" title="Help">?</span>
		<div class="help_popup"><%@include file="ayuda.html" %></div>
	</div>

	<%---------------%>
	<%-- TÁCTICA 1 --%>
	<%---------------%>
	<div style="display:inline-block; vertical-align: top; background-color: #e2f2f8; padding: 1px;">
		<div style="display:inline-block; vertical-align: top">
			<c:if test="${empty sessionScope.ilogin}">
  				<form name="form_login" action="${sessionScope.base}Login" method="post" onsubmit="return form_submit()">
					<input type="hidden" id="confirmed" name="confirmed" value="${pageContext.request.serverName == 'localhost' ? '1' : ''}" />
					Login <input type="text" name="ilogin" size="10">
					<br/>
					Password <input type="password" name="ipassword" size="10">
					<br/>
					<input type="submit" />
					<br/>
					<span class="error"><c:out value="${param.error}" /></span>
					<br/>
				</form>
  			</c:if>

		<form action="https://sokker.org/tactedit.php" method="get">
			<input type="hidden" name="save" value="1" />

			<div style="background-color: lightblue;">
	 			<c:forEach var="t" items="${tacticas}">
	 				<c:if test="${empty t}">
	 					<hr />
	 				</c:if>
	 				<c:if test="${not empty t && t[0] > 18}">
						<input type='radio' name='id' id='${t[0]}' value='${t[0]}' onclick='return cambiar_tactica("", "${t[2]}")' />
						<label for='${t[0]}'>${t[1]}</label><br/>
					</c:if>
				</c:forEach>
				
				<hr />
				
				<input type='radio' name='id' id='manual' />
				<input type='text' size="5" onclick='$("#manual").prop("checked", true);' onchange='$("#manual").val($(this).val())' placeholder='tactID' /><br/>
			</div>
		
			<br/>
	
			<div align="center">
				<input type="submit" value="Save" />
				&nbsp;
				<button onclick="location.href='${pageContext.request.contextPath}/';return false;">&lt;&lt; Back</button>
			</div>
		</div>
		
		<div style="display:inline-block; vertical-align: top">
			<div class="court js-court" style="display: inline-block; width: 334px;" data-tactid="1">
				<div class="ajax-loader-container">
					<div class="ajax-loader"></div>
				</div>
			</div>

			<br />
			
			<img src="${sessionScope.base}img/ojo.png" title="Swap view" style="vertical-align: top; cursor: pointer;" onclick="cambiar_editor_click()" />
			
			<textarea name="tact" id="tact" cols="33" rows="6" onclick="select()" onchange="actualizar_tactica('', this.value)"></textarea>
		</div>

		<%-- JUGADORES --%>	
		<div style="display:inline-block; vertical-align: top;">
			<div style="background-color: lightgreen" class="grupo_checkbox">
				<input type="checkbox" id="jugador2" /><label for="jugador2">Player 2</label><br />
				<input type="checkbox" id="jugador3" /><label for="jugador3">Player 3</label><br />
				<input type="checkbox" id="jugador4" /><label for="jugador4">Player 4</label><br />
				<input type="checkbox" id="jugador5" /><label for="jugador5">Player 5</label><br />
				<input type="checkbox" id="jugador6" /><label for="jugador6">Player 6</label><br />
				<input type="checkbox" id="jugador7" /><label for="jugador7">Player 7</label><br />
				<input type="checkbox" id="jugador8" /><label for="jugador8">Player 8</label><br />
				<input type="checkbox" id="jugador9" /><label for="jugador9">Player 9</label><br />
				<input type="checkbox" id="jugador10" /><label for="jugador10">Player 10</label><br />
				<input type="checkbox" id="jugador11" /><label for="jugador11">Player 11</label><br />
				<div style="background-color: #5cbd5c; color: white;">
					<input type="checkbox" id="todos" onclick="seleccionar_todo_click('todos')" /><label for="todos">Select all</label><br />
				</div>
			</div>
	
			<br />
			
			<div align="center">
				<input type="button" value="Swap" onclick='intercambiar_click("")'>
				&nbsp;
				<input type="button" value="Mirror" onclick='mirror_click("")'>
			</div>
	
			<table>
				<tr>
					<td></td>
					<td><img src="${sessionScope.base}img/long-arrow-up.png" onclick="mover_click(0, 1)" class="flecha" /></td>
					<td></td>
				</tr>
				<tr>
					<td><img src="${sessionScope.base}img/long-arrow-left.png" onclick="mover_click(-1, 0)" class="flecha" /></td>
					<td></td>
					<td><img src="${sessionScope.base}img/long-arrow-right.png"  onclick="mover_click(1, 0)" class="flecha" /></td>
				</tr>
				<tr>
					<td></td>
					<td><img src="${sessionScope.base}img/long-arrow-down.png" onclick="mover_click(0, -1)" class="flecha" /></td>
					<td></td>
				</tr>
			</table>
			
			<div class="grupo_checkbox">
				<table class="campo" style="background-image: url('${sessionScope.base}img/campo.png');">
					<tr>
						<td><input type="checkbox" id="c06" checked="checked" /></td>
						<td><input type="checkbox" id="c16" checked="checked" /></td>
						<td><input type="checkbox" id="c26" checked="checked" /></td>
						<td><input type="checkbox" id="c36" checked="checked" /></td>
						<td><input type="checkbox" id="c46" checked="checked" /></td>
					</tr>
					<tr>
						<td><input type="checkbox" id="c05" checked="checked" /></td>
						<td><input type="checkbox" id="c15" checked="checked" /></td>
						<td><input type="checkbox" id="c25" checked="checked" /></td>
						<td><input type="checkbox" id="c35" checked="checked" /></td>
						<td><input type="checkbox" id="c45" checked="checked" /></td>
					</tr>
					<tr>
						<td><input type="checkbox" id="c04" checked="checked" /></td>
						<td><input type="checkbox" id="c14" checked="checked" /></td>
						<td><input type="checkbox" id="c24" checked="checked" /></td>
						<td><input type="checkbox" id="c34" checked="checked" /></td>
						<td><input type="checkbox" id="c44" checked="checked" /></td>
					</tr>
					<tr>
						<td><input type="checkbox" id="c03" checked="checked" /></td>
						<td><input type="checkbox" id="c13" checked="checked" /></td>
						<td><input type="checkbox" id="c23" checked="checked" /></td>
						<td><input type="checkbox" id="c33" checked="checked" /></td>
						<td><input type="checkbox" id="c43" checked="checked" /></td>
					</tr>
					<tr>
						<td><input type="checkbox" id="c02" checked="checked" /></td>
						<td><input type="checkbox" id="c12" checked="checked" /></td>
						<td><input type="checkbox" id="c22" checked="checked" /></td>
						<td><input type="checkbox" id="c32" checked="checked" /></td>
						<td><input type="checkbox" id="c42" checked="checked" /></td>
					</tr>
					<tr>
						<td><input type="checkbox" id="c01" checked="checked" /></td>
						<td><input type="checkbox" id="c11" checked="checked" /></td>
						<td><input type="checkbox" id="c21" checked="checked" /></td>
						<td><input type="checkbox" id="c31" checked="checked" /></td>
						<td><input type="checkbox" id="c41" checked="checked" /></td>
					</tr>
					<tr>
						<td><input type="checkbox" id="c00" checked="checked" /></td>
						<td><input type="checkbox" id="c10" checked="checked" /></td>
						<td><input type="checkbox" id="c20" checked="checked" /></td>
						<td><input type="checkbox" id="c30" checked="checked" /></td>
						<td><input type="checkbox" id="c40" checked="checked" /></td>
					</tr>
				</table>
	
				<div style="background-color: #5cbd5c; color: white;">
					<input type="checkbox" checked="checked" id="todo_campo" onclick='seleccionar_todo_click("todo_campo")' /><label for="todo_campo">Select all</label>
				</div>
			</div>
		</div>		
	</div>

	<%---------------%>
	<%-- TÁCTICA 2 --%>	
	<%---------------%>
	<div style="display:inline-block; vertical-align: top; background-color: #4f8ea2; padding: 1px;">

		<div style="display:inline-block; vertical-align: top">
			<div style="background-color: lightblue;">
				<c:forEach var="t" items="${tacticas}">
	 				<c:if test="${empty t}">
	 					<hr />
	 				</c:if>
	 				<c:if test="${not empty t}">
						<input type='radio' name='id_' id='${t[0]}_' value='${t[0]}' onclick='return cambiar_tactica("_", "${t[2]}")' style='visibility:hidden' />
						<label for='${t[0]}_'>${t[1]}</label><br/>
	 				</c:if>
				</c:forEach>
			</div>
		</div>
	
		<div style="display:inline-block">
			<div id="court_" class="court" style="display: inline-block; width: 334px;" data-tactid="2">
				<div class="ajax-loader-container_">
					<div class="ajax-loader_"></div>
				</div>
				<canvas id="canvas-boisko_" class="canvas" width="334" height="524" style="cursor: auto;"></canvas>
			</div>
		
			<br />
			
			<textarea id="tact_" cols="39" rows="6" onclick="select()" onchange="actualizar_tactica('_', this.value)"></textarea>
		</div>
	
		<div style="display:inline-block; vertical-align: top;">
			<div style="background-color: lightgreen" class="grupo_checkbox">
				<input type="checkbox" id="jugador_2" /><label for="jugador_2">Player 2</label><br />
				<input type="checkbox" id="jugador_3" /><label for="jugador_3">Player 3</label><br />
				<input type="checkbox" id="jugador_4" /><label for="jugador_4">Player 4</label><br />
				<input type="checkbox" id="jugador_5" /><label for="jugador_5">Player 5</label><br />
				<input type="checkbox" id="jugador_6" /><label for="jugador_6">Player 6</label><br />
				<input type="checkbox" id="jugador_7" /><label for="jugador_7">Player 7</label><br />
				<input type="checkbox" id="jugador_8" /><label for="jugador_8">Player 8</label><br />
				<input type="checkbox" id="jugador_9" /><label for="jugador_9">Player 9</label><br />
				<input type="checkbox" id="jugador_10" /><label for="jugador_10">Player 10</label><br />
				<input type="checkbox" id="jugador_11" /><label for="jugador_11">Player 11</label><br />
				<div style="background-color: #5cbd5c; color: white;">
					<input type="checkbox" id="todos_" onclick="seleccionar_todo_click('todos_')" /><label for="todos_">Select all</label><br />
				</div>
			</div>
	
			<br />
			
			<div align="center">
				<input type="button" value="Swap" onclick='intercambiar_click("_")'>
				&nbsp;
				<input type="button" value="Mirror" onclick='mirror_click("_")'>
			</div>

			<br />

			<div align="center">
				<input type="button" value="<= Pass" onclick="pasar_click()">
			</div>
		</div>
	</div>

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

	</form>
<%-- 
https://sokker.org/read_tact.php?id=2137686
https://sokker.org/tactedit.php?id=2137686&save=1&tact=AHAHAIAJBLCICICJCKCLDIDIDJDKDLDIDIDJDKDKEIEJEJEJEKEJEJEJEKEKFJFJGJFKFKAGBGBHBIAICGCGCHCICJDGDGDHDIDIDGDGDHDIDIEGEHEHEHEIEHEHEHEHEHFHFHGHFHFHBDAFAGAHAHCDCECFCGCHDDDEDFDGDGDEDEDFDGDGEEEFEFEFEGEEEEEFEFEFFEFEGFFFFFBHBICJBKDLDMDMDMDMCNDMDMDMDMDNDLDMDMDMDNELEMEMEMENFLFMFMFMFNHLGMGMGMGNDDBECFBGBHCBDCDCDCDCDBDCDCDCDCDBDCDCDCDDEBECECECEDFBFCFCFCFDGBGCGCGCHDEHFJFKFLEMEHFIFJFKFLFHFIFJFLGLFHGIGKGLGLHHIIIKJLJNJHJIJKKMKNMEKILJLLMNBGCFDHCJBIEDEFEHEIEKFDFFFHFJFKFDFFHHFJFKGEGFGHGJGKGEGFGHGJGKHEHFHHHJHKECFDFEFFFFFCFDFEFEEGGCFCFEFFFFGCGCGEGFFGJCJCIEIGHGKBKCJEJGJGMBMCLFKGMKIIIIIIIJIKJIJIJIJJJKKIKIKIKJKKLILILILJLKMIMIMIMJMJOIOIOIOJOIOIOIPIPIPIIEIFIGIGIGJEJFJGJGJGKEKFKGKGKGLELFLGLGLGMFMFMGMGMGOGOFOGOGOGPGPGOGOGOG
 --%>
</body>

	<script type="text/javascript" src="js/editor.js"></script>	
</html>

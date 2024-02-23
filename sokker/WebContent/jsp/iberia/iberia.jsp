<%@page import="com.formulamanager.sokker.entity.Jornada"%>
<%@page import="com.formulamanager.sokker.entity.Grupo"%>
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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>

<head>
	<link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>Sokker - Arcade Cup</title>
	
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
		h1 {
			color: black;
			text-shadow: -1px -1px white;
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
		.grupo {
			display: inline-block; 
			vertical-align: top;
			background-color: lightblue;
			margin: 5px;
			padding: 0px 10px 10px 10px;
			border: 1px solid #4f8ea2;
		}
		.equipo {
			background-color: #e2f2f8;
			color: black; 
			padding: 1px;
		}
		.partido {
			display: inline-block;
			background-color: #e2f2f8;
			border: 1px solid black;
			padding: 2px;
		}
		.c1 {
			background-color: #e2f2f8;
		}
		.c0 {
			background-color: #cee5ed;
		}
		.cOTROS1 {
			background-color: #f2f8e2;
		}
		.cOTROS0 {
			background-color: #e5edce;
		}
		.cCHAMPIONS1 {
			background-color: #ceedcf;
		}
		.cCHAMPIONS0 {
			background-color: #b3dcb4;
		}
		.cUEFA1 {
			background-color: #ede3ce;
		}
		.cUEFA0 {
			background-color: #e1d4b8;
		}
		.victoria {
			background-color: yellow;
		}
		.derrota {
		}
		.lineasJ0 {
			text-align: right;
			padding-right: 15px; 
		}
		.lineasJU {
			text-align: right;
			padding-left: 15px; 
			border-left: 1px solid #4f8ea2;
		}
		.lineas {
			text-align: right;
			padding-right: 15px; 
			padding-left: 15px; 
			border-left: 1px solid #4f8ea2;
		}
		.equipo_comp {
			border: 1px solid #4f8ea2;
		}
		.lineas1 {
			border-bottom: 1px solid #4f8ea2;
		}
		.lineas0 {
			border-top: 1px solid #4f8ea2;
		}
		.r {
			border-right: 1px solid #4f8ea2;
		}
	</style>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');

	 function jornada_click(grupo, actual, inc) {
		if ($("#jornada" + grupo + "_" + (actual+inc)).size() > 0) {
			 $("#jornada" + grupo + "_" + actual).hide();
			 $("#jornada" + grupo + "_" + (actual+inc)).show();
		}
	 } 
	 </script>
</head>

<body>
	<div style="background-color: #4f8ea2; color: white; margin: 1px; text-align: center;">
		<b>Sokker - Arcade Cup</b>
	</div>

	<div style="background-color: #e2f2f8; padding: 1px;" align="center">
		<c:if test="${empty(competicion)}">
			<div align="left" style="display: inline-block;">
				<br/>
				<form method="post" action="${pageContext.request.contextPath}/iberia/generar">
					<ul style="list-style: none;">
						<li style="vertical-align: top;">
							<label style=" display: inline-block; width: 220px; vertical-align: top">ID Equipos:</label>
							<textarea name="equipos" rows="5" cols="50">5661 91262 9486 91420 67962 36246 20900 75588 36351 52202 21063 63039 19979 10021 110665 91015 115781 17443 87563 116790 17884 31049 9900 73289 9566 9514 9502 116519 131090 75766 27962
							</textarea>
						</li>
						<li>	
							<label style="display: inline-block; width: 220px; vertical-align: top">Tamaño de los grupos:</label>
							<input type="text" size="2" name="equipos_grupo" value="4" /> 
							<span class="gris">En blanco = un solo grupo</span>
						</li>
						<li>	
							<label style="display: inline-block; width: 220px; vertical-align: top">Nº de equipos para la Champions:</label>
							<input type="text" size="2" name="equipos_champions" value="16" />
						</li>
						<li>
							<label style="display: inline-block; width: 220px; vertical-align: top">Nº de equipos para la UEFA:</label>
							<input type="text" size="2" name="equipos_uefa" value="8" />
						</li>
						<li>
							<label style="display: inline-block; width: 220px; vertical-align: top">Nº de equipos para la CP:</label>
							<input type="text" size="2" name="equipos_cp" value="" />
							<span class="gris">En blanco = restantes (múltiplo de 2)</span>
						</li>
						<li>
							<label style="display: inline-block; width: 220px; vertical-align: top">Nº de jornadas:</label>
							<input type="text" size="2" name="num_jornadas" value="" /> 
							<span class="gris">En blanco = todos contra todos</span>
						</li>
						<li>
							<label for="doble_vuelta" style="display: inline-block; width: 220px;">
							 	¿Doble vuelta?
							</label>
							<input type="checkbox" id="doble_vuelta" name="doble_vuelta" />
							<span class="gris">
								(solo si todos contra todos)
							</span>
						</li>
						<li>	
							<br/>
							<label style="display: inline-block; width: 220px; vertical-align: top"></label>
							<input type="submit" value="Generar">
						</li>
					</ul>
				</form>
			</div>
		</c:if>
				
		<div>
			<c:if test="${not empty(competicion)}">
				<form method="post" action="${pageContext.request.contextPath}/iberia/guardar">
					<c:forEach items="${competicion.grupos}" var="g" varStatus="s">

						<!-- GRUPOS -->
						<div style="display: inline-block; vertical-align: top">	
							<div class="grupo">
								<h1>Group <c:out value="${g.numero}" /></h1>
								<table>
									<thead>
										<tr>
											<th>&nbsp;</th>
											<th>Team</th>
											<th>Points</th>
											<th>P</th>
											<th>W</th>
											<th>D</th>
											<th>L</th>
											<th>GF</th>
											<th>GA</th>
											<th>Avg</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${g.equipos}" var="e" varStatus="var">
											<tr class="equipo c${e.tipo_competicion}${var.count % 2}">
												<td align="right"><c:out value="${var.count}" /></td>
												<td><a href="https://sokker.org/team/teamID/${e.tid}" target="_blank"><c:out value="${e.nombre}" /></a></td>
												<td align="center"><b>${e.puntos}</b></td>
												<td align="center">${e.j}</td>
												<td align="center">${e.g}</td>
												<td align="center">${e.e}</td>
												<td align="center">${e.p}</td>
												<td align="center">${e.gf}</td>
												<td align="center">${e.gc}</td>
												<td align="center">${e.avg}</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
				
								<br/>
	
								<!-- JORNADAS -->
				
								<c:forEach items="${g.jornadas}" var="j" varStatus="var">
									<div id="jornada${s.count}_${var.count}" style="${var.count == g.num_jornada?'':'display:none'}">
										<table align="center">
											<thead>
												<tr>
													<th colspan="3"><b><a href="javascript:jornada_click(${s.count}, ${var.count}, -1)">&larr;</a> Day ${var.count} / ${g.num_jornadas} <a href="javascript:jornada_click(${s.count}, ${var.count}, 1)">&rarr;</a></b></th>
												</tr>
											</thead>
					
											<tbody>					
												<c:forEach items="${j.partidos}" var="p" varStatus="var2">
													<tr class="equipo c${var2.count % 2}">
														<td align="right">
															<a href="https://sokker.org/team/teamID/${p.local.tid}" target="_blank"><c:out value="${p.local.nombre}" /></a>
														</td>
														<td>
															<input type="text" size="1" value="${p.goles_l}" name="resultado_${g.numero}_${j.numero}_${p.local.tid}" />
															 - 
															<input type="text" size="1" value="${p.goles_v}" name="resultado_${g.numero}_${j.numero}_${p.visitante.tid}" />
														</td>
														<td>
															<a href="https://sokker.org/team/teamID/${p.visitante.tid}" target="_blank"><c:out value="${p.visitante.nombre}" /></a>
														</td>
													</tr>
												</c:forEach>
											</tbody>
										</table>

										<%-- EQUIPOS --%>
										<br/>
									</div>
									
								</c:forEach>
								<textarea style="display:none;" onclick="select()" rows="${g.equipos.size()}" cols="50"><c:out value="${g.getTexto_foro()}" /></textarea>
							</div>
						</div>
					</c:forEach>
	
					<!-- COMPETICIONES -->

					<div></div>
					
					<c:forEach items="${competicion.competiciones}" var="c">
						<div style="display: inline-block; vertical-align: top">
							<div class="grupo" style="display: inline-block;">
								<h1>${c.tipo_competicion.nombre}</h1>
								<table style="border-collapse: collapse;">
									<c:forEach items="${c.jornadas[0].partidos}" var="p" varStatus="varp">
										<tr>
											<c:forEach items="${c.jornadas}" var="j" varStatus="varj">
												<c:if test="${varp.index % j.rowspan == 0}">
													<td rowspan="${j.rowspan}" style="padding:0; padding-bottom: 5px;">
														<c:set var="p" value="${c.jornadas[varj.index].partidos[varp.index / varj.count]}" />
														<div class="lineas${varj.index == 0 ? 'J0' : varj.count == c.num_jornadas ? 'JU' : ''} ${(varp.index / varj.count) % 2 == 1 ? 'lineas1 r' : ''}">
															<div class="equipo_comp c${c.tipo_competicion}1">
																<a href="https://sokker.org/team/teamID/${p.local.tid}" target="_blank"><c:out value="${p.local.nombre}" escapeXml="false" /></a>
																<input class="${p.claseL}" type="text" size="1" value="${p.goles_l}" name="resultadoc_${c.numero}_${j.numero}_${p.local.tid}" /><br />
															</div>
														</div>
														<div class="lineas${varj.index == 0 ? 'J0' : varj.count == c.num_jornadas ? 'JU' : ''} ${(varp.index / varj.count) % 2 == 0 ? 'lineas0 r' : ''}">
															<div class="equipo_comp c${c.tipo_competicion}0">
																<a href="https://sokker.org/team/teamID/${p.visitante.tid}" target="_blank"><c:out value="${p.visitante.nombre}" escapeXml="false" /></a>
																<input class="${p.claseV}" type="text" size="1" value="${p.goles_v}" name="resultadoc_${c.numero}_${j.numero}_${p.visitante.tid}" /><br />
															</div>
														</div>
													</td>
												</c:if>
											</c:forEach>
										</tr>
									</c:forEach>
								</table>
							</div>
						
							<%-- EQUIPOS --%>
							<br/>
							<textarea style="display:none;" onclick="select()" rows="${c.equipos.size()}" cols="50"><c:out value="${c.getTexto_foro()}" /></textarea>
						</div>
					</c:forEach>
	
					<div>
						<input type="submit" value="Guardar" />
						<input type="button" onclick="if (confirm('¿Crear una competición nueva?')) location.href='${pageContext.request.contextPath}/iberia/nueva';" value="Nueva" />
						<input type="button" onclick="$('textarea').show()" value="Foro" />
					</div>
				</form>
			</c:if>
		</div>
	</div>

</body>
</html>

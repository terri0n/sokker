<%@page import="com.formulamanager.sokker.auxiliares.Util"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.formulamanager.sokker.entity.Jugador"%>
<%@page import="com.formulamanager.sokker.bo.FactorxBO.FORMACIONES"%>
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
	<title>Sokker - Factor X ${param.senior == 1 ? 'senior' : ''}</title>
	
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
		th {
			background-color: #4f8ea2;
			color: white;
			font-weight: bold;
		}
		tr:nth-child(odd) {
			background-color: #bae5f3;
		}
		tr:nth-child(even) {
			background-color: #9dd0e0;
		}
		.menu {
			width: 100%;
			max-width: 130px;
		}
		.menu th {
			background-color: #8ea24f;
		}
		.menu tr:nth-child(odd) {
			background-color: #e5f3ba;
		}
		.menu tr:nth-child(even) {
			background-color: #d0e09d;
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
		.incr {
			color: gray;
		}
		.estrella {
			color: #FFD700; /* Gold */
		}
		.estrella_sup {
			color: #006400; /* DarkGreen */
		}
		.estrella, .estrella_sup {
			text-shadow: -0.4px 0px black, 0px 0.4px black, 0.4px 0px black, 0px -0.4px black, 0.4px 0.4px black, 0.4px -0.4px black, -0.4px 0.4px black, -0.4px -0.4px black;
		}
		.historial {
			color: lightblue;
		}
		.mensaje {
			color: white;
			background-color: orange;
			border: 1px solid black;
		}
	</style>
	
	<script type="text/javascript">
		function validar() {
			if ($('input[name="pid"]').val() == '') {
				alert('Indica un PID');
				$('input[name="pid"]').focus();
				return false;
			}
			return true;
		}
	
		function borrar(pid) {
			if (confirm('¿Borrar al jugador ' + pid + '?')) {
				location.href = '${pageContext.request.contextPath}/factorx/borrar?senior=${param.senior}&pid=' + pid;
			}
		}

		function archivar() {
			if (confirm('¿Archivar la temporada?')) {
				location.href = '${pageContext.request.contextPath}/factorx/archivar?senior=${param.senior}';
			}
		}
	</script>
</head>

<body>
	<div style="background-color: #4f8ea2; color: white; margin: 1px; text-align: center;">
		<c:if test="${historico != null}">
			<b>Sokker - Factor X ${param.senior == 1 ? 'NT' : ''} - ${historico}</b>
		</c:if>
		<c:if test="${historico == null}">
			<b>Sokker - Factor X ${param.senior == 1 ? 'NT' : ''} - Jornada ${jornada} <span class="historial">(<a href="${pageContext.request.contextPath}/factorx?senior=${param.senior}&edicion=${edicion-1}">&larr;</a> ${edicion}ª edición <a href="${pageContext.request.contextPath}/factorx?senior=${param.senior}&edicion=${edicion+1}">&rarr;</a>)</span></b>
		</c:if>
	</div>

	<c:if test="${not empty(param.mensaje)}">
		<div class="mensaje"><c:out value="${param.mensaje}" /></div>
	</c:if>

	<%---------------%>
	<%----- MENÚ ----%>
	<%---------------%>
	<div style="float: left; background-color: transparent; padding: 1px;">
		<table class="menu">
			<thead>
				<tr><th>Clasificación</th></tr>
			</thead>
			<tbody>
				<tr><td><a href="${pageContext.request.contextPath}/factorx?senior=">Factor X</a></td></tr>
				<tr><td><a href="${pageContext.request.contextPath}/factorx?senior=1">Factor X NT</a></td></tr>
			</tbody>
		</table>
		
		<br/>

		<table class="menu">
			<thead>
				<tr><th>Inscripción</th></tr>
			</thead>
			<tbody>
				<tr>
					<td>
						¡Recuerda ponerle a tu jugador un anuncio de transferencia!
					</td>
				</tr>
					
				<tr>
					<td>
						<form action="${pageContext.request.contextPath}/factorx/anyadir" method="post" onsubmit="return validar()">
							<input type="hidden" name="senior" value="${param.senior}" />
							<input type="number" name="pid" placeholder="PID" style="width:100px;"/>
							<br/>
							<select name="demarcacion">
								<option>GK</option>
								<option>DEF</option>
								<option>MID</option>
								<option>ATT</option>
							</select>
							<c:if test="${not empty sessionScope.login && param.senior == sessionScope.senior}">
								<input type="checkbox" name="forzar" id="forzar" />
								<label for="forzar">forzar</label>
							</c:if>
							<input type="submit" value="Añadir" /><br />
						</form>
						<span class="error"><c:out value="${param.error}" /></span>
					</td>
				</tr>
			</tbody>
		</table>

		<br/>
		
		<table class="menu">
			<thead>
				<tr><th>Histórico</th></tr>
			</thead>
			<tbody>
				<tr><td><a href="${pageContext.request.contextPath}/factorx/historico?tipo=puntos&senior=${param.senior}">Más puntos</a></td></tr>
				<tr><td><a href="${pageContext.request.contextPath}/factorx/historico?tipo=subidas&senior=${param.senior}">Más subidas</a></td></tr>
				<tr><td><a href="${pageContext.request.contextPath}/factorx/historico?tipo=valor&senior=${param.senior}">Más dif. valor</a></td></tr>
			</tbody>
		</table>

		<br/>
		
		<c:if test="${(empty sessionScope.login || param.senior != sessionScope.senior) && empty(param.captura)}">
			<form action="${pageContext.request.contextPath}/factorx/login" method="post">
				<input type="hidden" name="senior" value="${param.senior}" />
				<table class="menu">
					<thead>
						<tr>
							<th colspan="2">Administración</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>Login</td>
							<td><input type="text" name="login" size="3"></td>
						</tr>
						<tr>
							<td>Password</td>
							<td><input type="password" name="password" size="3"></td>
						</tr>
						<tr>
							<td colspan="2">
								<input type="submit" />
								<span class="error"><c:out value="${param.error}" /></span>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</c:if>

		<c:if test="${not empty sessionScope.login && param.senior == sessionScope.senior}">
			<table class="menu">
				<thead>
					<tr>
						<th>Admin</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><input type="button" value="Actualizar jugadores" onclick="location.href='${pageContext.request.contextPath}/factorx/actualizar_todos?senior=${param.senior}'" /></td>
					</tr>
					<tr>
						<td><input type="button" value="Capturar pantalla" onclick="location.href='${pageContext.request.contextPath}/factorx/capturar?senior=${param.senior}'" /></td>
					</tr>
					<tr>
						<td><input type="button" value="Archivar temporada" onclick="archivar()" /></td>
					</tr>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td>
							<form action="${pageContext.request.contextPath}/factorx/formacion" method="post">
								<input type="hidden" name="senior" value="${param.senior}" />
								<select name="formacion">
									<c:forEach items="<%= FORMACIONES.values() %>" var="f">
										<option value="${f.name()}"
											<c:if test="${f.name() == formacion.name()}">
												selected
											</c:if>
										><c:out value="${f}" /></option>
									</c:forEach>
								</select>
								<input type="submit" value="Cambiar formación" /><br />
							</form>
						</td>
					</tr>
				</tbody>
			</table>
		</c:if>
		
	</div>

	<%---------------%>
	<%-- JUGADORES --%>
	<%---------------%>
	<div style="vertical-align: top; background-color: #e2f2f8; padding: 1px;">

		<div style="text-align: center;">
			<div style="background-color: transparent; display: inline-block;">
	 			<table style="margin: auto;">
					<thead>
						<tr>
							<th>&nbsp;</th>
							<c:if test="${historico != null}">
								<th>Edición</th>
							</c:if>
							<c:if test="${historico == null}">
								<th>Jornada</th>
							</c:if>
							<th>Nombre</th>
							<th>Equipo</th>
							<th>Edad</th>
							<th>Demarcación</th>
							<th>Puntos</th>
							<c:if test="${historico != null}">
								<th>Subidas</th>
								<th>Dif. valor</th>
							</c:if>
							<th><!--  Borrar --></th>
						</tr>
					</thead>
					
	 				<c:forEach var="j" items="${jugadores}" varStatus="var">
	 					<tr class="${j.demarcacion}">
	 						<td align="right"><c:out value="${var.count}" /></td>
							<c:if test="${historico != null}">
	 							<td><a href="${pageContext.request.contextPath}/factorx?senior=${param.senior}&edicion=${j.edicion}"><c:out value="${j.edicion}" /></a></td>
							</c:if>
							<c:if test="${historico == null}">
	 							<td align="right"><c:out value="${j.jornadaMod}" /></td>
	 						</c:if>
	 						<td><a href="http://sokker.org/player/PID/${j.pid}"><c:out value="${j.nombre}" /></a><c:out escapeXml="false" value="${j.icono}"/></td>
	 						<td><a href="http://sokker.org/team/teamID/${j.tid}"><c:out value="${j.equipo}" /></a></td>
	 						<td><c:out value="${j.edad}" /></td>
	 						<td><c:out value="${j.demarcacion}" /></td>
	 						<td align="right"><c:out value="${j.puntos}" /></td>
							<c:if test="${historico != null}">
		 						<td align="right"><c:out value="${j.subidas}" /></td>
		 						<td align="right"><fmt:formatNumber value="${j.difValor}" /></td>
							</c:if>
							<c:if test="${not empty sessionScope.login && param.senior == sessionScope.senior}">
	 							<td><a href="javascript:borrar(${j.pid})"><i class="fa fa-trash-o"></i></a></td>
	 						</c:if>
	 					</tr>
						<c:if test="${not empty sessionScope.login && param.senior == sessionScope.senior}">
							<form method="post" action="${pageContext.request.contextPath}/factorx/actualizar">
								<input type="hidden" name="senior" value="${param.senior}" />
								<tr align="left">
			 						<td>&nbsp;</td>
			 						<td><input type="text" name="jornada" value="${j.original.jornada}" size="2" /></td>
			 						<td>
										<input type="hidden" name="pid" value="${j.pid}" />
			 							<c:out value="${j.desp('es', 'condicion', j.original.condicion, false, 11)}" escapeXml="false"/> condición <span class="incr">(<c:out value="${j.condicion - j.original.condicion}" />)</span><br />
			 							<c:out value="${j.desp('es', 'rapidez', j.original.rapidez, false)}" escapeXml="false"/> rapidez <span class="incr">(<c:out value="${j.rapidez - j.original.rapidez}" />)</span><br />
			 							<c:out value="${j.desp('es', 'tecnica', j.original.tecnica, false)}" escapeXml="false"/> técnica <span class="incr">(<c:out value="${j.tecnica - j.original.tecnica}" />)</span><br />
			 							<c:out value="${j.desp('es', 'pases', j.original.pases, false)}" escapeXml="false"/> pases <span class="incr">(<c:out value="${j.pases - j.original.pases}" />)</span><br />
			 						</td>
			 						<td>
			 							<c:out value="${j.desp('es', 'porteria', j.original.porteria, false)}" escapeXml="false"/> porteria <span class="incr">(<c:out value="${j.porteria - j.original.porteria}" />)</span><br />
			 							<c:out value="${j.desp('es', 'defensa', j.original.defensa, false)}" escapeXml="false"/> defensa <span class="incr">(<c:out value="${j.defensa - j.original.defensa}" />)</span><br />
			 							<c:out value="${j.desp('es', 'creacion', j.original.creacion, false)}" escapeXml="false"/> creación <span class="incr">(<c:out value="${j.creacion - j.original.creacion}" />)</span><br />
			 							<c:out value="${j.desp('es', 'anotacion', j.original.anotacion, false)}" escapeXml="false"/> anotación <span class="incr">(<c:out value="${j.anotacion - j.original.anotacion}" />)</span><br />
			 						</td>
			 						<td><c:out value="${j.despDemarcacion_asistente(j.demarcacion, null)}" escapeXml="false"/></td>
			 						<td><input type="submit" value="Actualizar" /></td>
			 						<td>&nbsp;</td>
			 						<td>&nbsp;</td>
								</tr>
							</form>
						</c:if>
					</c:forEach>
				</table>
			</div>
		</div>
	</div>

	<%-- Para el foro de Sokker --%>
	<textarea style='width: 100%' onclick="select()">[b]Jornada <c:out value="${jornada}" />[/b]

[family=monospace]Pos Jor. Nombre[color=white]........................[/color] Equipo[color=white]........................[/color] Ed Dem Pts.[/family]
[family=monospace]----------------------------------------------------------------------------------[/family]		
<c:forEach var="j" items="${jugadores}" varStatus="var"
			>[color=<c:out value="${j.color}" />][family=monospace]<fmt:formatNumber value="${var.count}" minIntegerDigits="2" />. <fmt:formatNumber value="${j.jornadaMod}" minIntegerDigits="2" /> <%= Util.ifnull(((Jugador)pageContext.getAttribute("j")).getIcono_foro(), "[/family][/color][color=white][size=16px]..[/size][/color][family=monospace][color=" + ((Jugador)pageContext.getAttribute("j")).getColor() + "]") %>[pid=<c:out value="${j.pid}" />]<%= StringUtils.rightPad(((Jugador)pageContext.getAttribute("j")).getNombre(), 30, ".") %>[/pid] [tid=<c:out value="${j.tid}" />]<%= StringUtils.rightPad(((Jugador)pageContext.getAttribute("j")).getEquipo(), 30, ".") %>[/tid] <fmt:formatNumber value="${j.edad}" minIntegerDigits="2" /> <%= StringUtils.rightPad(((Jugador)pageContext.getAttribute("j")).getDemarcacion().name(), 3, ".") %> <c:out value="${j.puntos}" />[/family][/color]
</c:forEach></textarea>

</body>
</html>

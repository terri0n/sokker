<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:if test="${empty sessionScope['javax.servlet.jsp.jstl.fmt.locale.session']}">
	<fmt:setLocale value="${pageContext.request.locale.language}" scope="session" />
</c:if>

<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />

<!DOCTYPE html>
<html>
<head>
	<title>ChessGoal</title>
	<link rel="icon" href="img/chessgoal/chessgoal.ico" type="image/x-icon">

	<%@ include file="/inc/head.jspf" %>
	<script src="${pageContext.request.contextPath}/js/conexion.js.jsp"></script>
	<script src="${pageContext.request.contextPath}/js/sala.js.jsp"></script>
	<script src="${pageContext.request.contextPath}/js/juego.js.jsp"></script>
	<script src="${pageContext.request.contextPath}/js/chessgoal.js.jsp"></script>
	
	<style>
	@media only screen and (max-width: 800px) {
	    h1 img {
			width: 13%;
		}
		section {
			width: 95%;
		}
	}
	@media only screen and (min-width: 801px) {
	    h1 img {
			width: 8%; 
		}
		section {
			width: 47%;
		}
	}
	body {
	    background: repeating-linear-gradient(
	        45deg,
	        #575 0,
	        #797 10px,
	        #686 10px,
	        #686 50px
	    ) 0 40px;
	    background-size: 78px 78px;
	}
	h4 {
		padding-top: 3px;
	}
	section {
		color: white;
		white-space: nowrap;
	}
	section li {
		margin-bottom: 1px;
	}
	
	/* CHESSGOAL */

	.texto {
	    /*background: linear-gradient(to bottom, rgb(255,255,255) 25%, rgb(127,255,127) 50%, rgb(95,195,95) 75%, rgb(63,127,63) 100%);*/
	    background: linear-gradient(to bottom, rgb(255,255,255) 30%, rgb(195,255,195) 55%, rgb(63,127,63) 100%);
	    -webkit-background-clip: text;
	}

	#chess_board_border {
	    background: linear-gradient(to bottom, rgba(191,63,63,0.6) 0%, rgba(63,0,0,0.5) 100%);
		width: 473px;
		height: 473px;
		padding-left: 25px;
		padding-top: 25px;
		margin-top: 25px;
		margin-bottom: 25px;
		border-radius: 30px;
	    display: inline-block;
	    position: relative;
	}
	#chess_board {
		margin-top: -50px;
		position: absolute;
		height: 550px;
		text-align: left;
	}
	.chess_cell {
		float: left;
		width: 47px;
		height: 47px;
	}
	.chess_cell.bg0 {
		background-color: #559c55;
	}
	.chess_cell.bg1 {
		background-color: #a2d6a2;
	}
	.chess_cell.bg0.sombra {
		background-color: #377a37;
	}
	.chess_cell.bg1.sombra {
		background-color: #84b884;
	}
	.chess_figure {
		position: absolute;
	}
	.chess_figure .img {
		width: 47px;
		height: 47px;
		display: inline-block;
	}

	.ui-draggable {
		cursor: grab;
	}
	.ui-draggable-dragging {
		cursor: grabbing;
	}
 
	.pelota_movida {
		background-color: rgba(255, 255, 0, 0.5);
	}
	.figura_movida {
		background-color: rgba(255, 0, 0, 0.3);
	}
	.figura_rival_movida {
		background-color: rgba(0, 255, 255, 0.3);
	}
	.seleccion {
		/*border: 3px ridge yellow;*/
		box-shadow: 0 0 20px 5px rgba(255, 255, 0, 0.8);
		background-color: rgba(255, 255, 0, 0.51);
	}
	.invisible {
		visibility: hidden;
	}
	.semitransparente {
		opacity: 0.5;
	}
	.invertida_v {
		-moz-transform: scale(-1, 1);
		-webkit-transform: scale(-1, 1);
		-o-transform: scale(-1, 1);
		-ms-transform: scale(-1, 1);
		transform: scale(1, -1);
 	}
	.invertida_h {
		-moz-transform: scale(-1, 1);
		-webkit-transform: scale(-1, 1);
		-o-transform: scale(-1, 1);
		-ms-transform: scale(-1, 1);
		transform: scale(-1, 1);
 	}
 	.guante {
		display: inline-block;
		pointer-events: none;
		width: 20px;
 	}
 	#red1, #red4 {
 		clip-path: polygon(5px 0, 0 15px, 0 100%, 100% 100%, 100% 0);
 	}
 	#red3, #red6 {
		clip-path: polygon(calc(100% - 5px) 0, 100% 15px, 100% 100%, 0 100%, 0 0);
 	}
	<%-- Como las imágenes están invertidas, el clip vale para las de arriba y las de abajo, pero la capa que las contiene tb hay que recortarla para que no se vea verde --%>
 	#fondo_red4 {
 		clip-path: polygon(5px 100%, 0 calc(100% - 15px), 0 0, 100% 0, 100% 100%);
 	}
 	#fondo_red6 {
		clip-path: polygon(calc(100% - 5px) 100%, 100% calc(100% - 15px), 100% 0, 0 0, 0 100%);
 	}
	</style>

	<script type="text/javascript">
		let juego;
		$(function() {
			juego = new ChessGoal("${jugador.nombre}", "${pageContext.request.scheme == 'http' ? 'ws' : 'wss'}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/websocket");
		});
	</script>
</head>

<body>
<%@ include file="/inc/cabecera.jspf" %>

<div id="juego" class="hidden">
	<div>
	 	<div id="chess_board_border" class="sombra">
			<%-- chess_board deja fuera los bordes laterales pero incluye los de las porterías para poder pintarlas --%>
			<div id="chess_board">
				<%-- En Firefox no se superpone --%>
				<object style="position: absolute; width: 100%; height: 100%; margin-left: -5px; margin-top: 0px; opacity: 0.8; pointer-events: none;" data="img/chessgoal/campo.svg" type="image/svg+xml"></object>
			</div>
		</div>
		<div id="contenedor_chat">
			<div id="chat"></div>
			<input type="text" id="texto" onkeypress="texto_keypress(event)" />
			<button class="azul" onclick="juego.sala.conexion.enviar_mensaje();"><fmt:message key="connection.send" /></button>
		</div>
	</div>
</div>

<div style="padding-top: 0; background: linear-gradient(to bottom, rgba(31, 63, 31, 0.3) 0%, rgba(31, 63, 31, 0.3) 80%, rgba(31, 63, 31, 0) 100%); text-align: center;">
	<h1>
	    <span class="texto letra_impar">C</span
	    ><span class="texto letra_par">H</span
	    ><span class="texto letra_impar">E</span
	    ><span class="texto letra_par">S</span
	    ><span class="texto letra_impar">S</span
	    ><img width="130" src="${pageContext.request.contextPath}/img/chessgoal/chessgoal.png" 
	    /><span class="texto letra_par">G</span
	    ><span class="texto letra_impar">O</span
	    ><span class="texto letra_par">A</span
	    ><span class="texto letra_impar">L</span>
	</h1>

	<button class="verde" id="boton_nuevo" onclick="juego.sala.nuevo_click()">
		<span class="material-icons">sports_soccer</span><fmt:message key="menu.newGame" />
	</button>
	<button class="rojo" style="display: none;" id="boton_cancelar" onclick="juego.sala.cancelar_click()">
		<span class="material-icons">cancel</span><fmt:message key="menu.cancel" />
	</button>
	<button class="naranja" onclick="juego.mostrar_ranking_click()">
		<span class="material-icons">emoji_events</span><fmt:message key="menu.ranking" />
	</button>
	<button class="naranja" onclick="juego.mostrar_reglas_click()">
		<span class="material-icons">help</span><fmt:message key="rules.title" />
	</button>
</div>

<br />

<div style="text-align: center;">
	<section>
		<h2><span class="material-icons">people_alt</span> <fmt:message key="menu.users" /> (<span id="num_jugadores">0</span>)</h2>
		<ul id="jugadores" class="list-group pt-1" style="list-style-type: none; height: 335px; overflow-y: auto;"></ul>
	</section>
	
	<section>
		<h2><span class="material-icons">sports_soccer</span> <fmt:message key="menu.games" /> (<span id="num_partidos">0</span>)</h2>
		<ul id="partidos" class="list-group pt-1" style="list-style-type: none; height: 335px; overflow-y: auto;"></ul>
	</section>
</div>
</body>
</html>

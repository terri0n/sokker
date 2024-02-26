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
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<title>ChessGoal</title>
	<link rel="icon" href="img/chessgoal/chessgoal.ico" type="image/x-icon">

	<script src='https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js'></script>
	<script src='https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js'></script>
	<script src='https://cdnjs.cloudflare.com/ajax/libs/jqueryui-touch-punch/0.2.3/jquery.ui.touch-punch.min.js'></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>
	<script src="https://kit.fontawesome.com/6b4c099088.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/axios/0.19.2/axios.js"></script>
	<script src="js/bootbox.all.min.js"></script>
	<script src="js/util.js"></script>
	<script src="js/conexion.js.jsp"></script>
	<script src="js/sala.js.jsp"></script>
	<script src="js/juego.js.jsp"></script>
	<script src="js/chessgoal.js.jsp"></script>
	<script src="js/showToast.js"></script>
	<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
	<link rel="stylesheet" href="css/showToast.css">
	<link rel="stylesheet" href="css/estilo.css">

	<style>
	body{
		background-color: LightGrey;
	}
	h4 {
		padding-top: 3px;
	}

	.modal-content, .gris_claro {
		background-color: #fafafa;
	}

	.btn-sm {
		padding-top: 2px;
	}

	.list-group-item {
		padding: 5px;
	}	

	.chess_board_border {
		display: inline-block;
		vertical-align: middle;
		background-color: hsl(30, 46%, 35%);
		width: 550px;
		position: relative;
		overflow: hidden;
		padding: 1px;
		padding-left: 50px;
	}
	.chess_board {
		margin: auto;
		position: relative;
		height: 550px;
		text-align: left;
	}
	.chess_cell {
		float: left;
		width: 50px;
		height: 50px;
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
		width: 50px;
		height: 50px;
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
		border: 3px ridge yellow;
		background-color: rgba(255, 255, 0, 0.6);
	}
	.invisible {
		visibility: hidden;
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

	span.jugador {
 		border-radius: 10px;
 		border: 1px dashed lightgray;
 		background-color: Ivory;
		cursor: default;
		text-align: center;
     	font-size: small;
	} 	
 	.jugador {
		display: inline-block;
		width: 155px;
	}
	.usuario:nth-child(odd), .partido:nth-child(odd) {
		background-color: WhiteSmoke;
	}
	.usuario:nth-child(even), .partido:nth-child(even) {
		background-color: white;
	}
	.partido {
		text-align: left;
		padding-left: 50px;
	}
 	.puntos {
 		color: white;
 		border-radius: 5px;
		line-height: 1;
 		font-size: 10px;
    	font-weight: bold;
    	padding: 3px;
    	margin: 2px 4px 2px 2px;
     	display: inline-block;
     	float: right;
 	}
 	.principiante {
 		background-color: hsla(300, 60%, 60%, 0.5);
 	}
 	.aficionado {
 		background-color: hsla(240, 60%, 60%, 0.5);
 	}
 	.experto {
 		background-color: hsla(200, 60%, 60%, 0.5);
 	}
 	.maestro {
 		background-color: hsla(120, 60%, 60%, 0.5);
 	}
 	.maestro_internacional {
 		background-color: hsla(60, 60%, 60%, 0.5);
 	}
 	.gran_maestro {
 		background-color: hsla(30, 60%, 60%, 0.5);
 	}
 	.campeon_del_mundo {
 		background-color: hsla(0, 60%, 60%, 0.5);
 	}

	.away { 
	    vertical-align: super; 
	    font-size: 8px;
	    color: red;
	    display: inline-block;
	}
	
	.modal-dialog-scrollable ol li {
		margin-left: 10px;
		text-align: justify;
	} 
	</style>

	<script type="text/javascript">
		var juego;	
		$(function() {
			juego = new ChessGoal("${jugador.nombre}", "${pageContext.request.scheme == 'http' ? 'ws' : 'wss'}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/websocket");
		});
	</script>
</head>

<body translate="no">
	<%@ include file="/inc/cabecera.jspf" %>

	<section style="text-align: center;" class="">
		<div class="chess_board_border rounded m-2">
			<div class="chess_board">
				<object style="position: absolute; width: 100%; height: 100%; margin-left: -5px; opacity: 0.8; pointer-events: none;" data="img/campo.svg" type="image/svg+xml"></object>
			</div>
		</div>

		<div style="display: inline-block; height: 552px; width: 552px; vertical-align: middle;" class="card m-2 p-2 gris_claro">
			<div>
				<div style="display: inline-block; margin-top: 10px; vertical-align: top;">
					<div style="width: 310px; margin-bottom: 10px; margin-left: auto;">
						<div class="input-group">
				        	<span class="input-group-text" style="font-size: small; width: 65px; border-color:gray;">
				        		<fmt:message key="menu.white" />
				        	</span>
					        <label class="form-control" style="font-size: small; border-color:gray;">
								<i id="turno_blancas" class="fas fa-chess-pawn" style="display: none;"></i>
					        	<span class="float-right text-secondary" id="span_tiempo_blancas">
					        		<i class="fas fa-stopwatch"></i>
					        		<span id="tiempo_blancas"></span>
					        	</span>
					        	<span id="jugador1"></span>
					        </label>
						</div>
						<div class="input-group">
				        	<span class="input-group-text" style="font-size: small; width: 65px; color: white; background-color: gray; border-color:black;">
				        		<fmt:message key="menu.black" />
				        	</span>
					        <label class="form-control" style="font-size: small; border-color:black; background-color: Silver;">
								<i id="turno_negras" class="fas fa-chess-pawn" style="display: none;"></i>
					        	<span class="float-right text-white" id="span_tiempo_negras">
					        		<i class="fas fa-stopwatch"></i>
					        		<span id="tiempo_negras"></span>
					        	</span>
					        	<span id="jugador2"></span>
					        </label>
					   	</div>
					</div>
					<button class="btn btn-success" id="boton_nuevo" onclick="juego.sala.nuevo_click()">
						<fmt:message key="menu.newGame" />
					</button>
					<button style="display:none;" class="btn btn-success" disabled="disabled" id="finalizar" onclick="juego.finalizar_click()">
						<fmt:message key="msg.waitingOpponent" />
					</button>
					<button style="display:none;" class="btn btn-success" disabled="disabled" id="boton_otro" onclick="juego.sala.otro_click()">
						<fmt:message key="menu.otro" />
					</button>
					<button style="display:none;" class="btn btn-danger" disabled="disabled" id="boton_cancelar" onclick="juego.sala.cancelar_click()">
						<fmt:message key="menu.cancel" />
					</button>
				</div>
				
				<div style="display: inline-block; margin-top: 10px;">
 					<div style="margin-left: 77px;">
	 					<i class="fas fa-chess-board fa-3x"></i>
						<span class="fa-stack fa-3x" style="margin-left: -70px;">
							<i class="fas fa-circle fa-stack-1x" style="color: white"></i>
							<i class="fas fa-futbol fa-stack-1x"></i>
						</span>
					</div>
					<h3 style="margin-top: -30px; margin-left: 35px;">ChessGoal</h3>
				</div>
			</div>

			<ul class="nav nav-tabs" role="tablist" style="width: 100%; margin: auto; white-space: nowrap; margin-top: 8px;">
				<li class="nav-item">
					<a class="nav-link active" data-toggle="tab" href="#tab_jugadores" role="tab" aria-selected="true" onclick="juego.sala.tab_click('tab_jugadores');">
						<i class="fas fa-users"></i> <fmt:message key="menu.users" />
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" data-toggle="tab" href="#tab_partidos" role="tab" aria-selected="false" onclick="juego.sala.tab_click('tab_partidos');">
						<i class="fas fa-chess-board"></i> <fmt:message key="menu.games" />
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" data-toggle="tab" href="#tab_chat" role="tab" aria-selected="false" onclick="juego.sala.tab_click('tab_chat');">
						<i class="fas fa-comments"></i> <fmt:message key="menu.chat" />
					</a>
				</li>
				<li class="ml-4 mr-4"></li>
				<li class="nav-item" style="background-color: orange;">
					<a class="nav-link" style="color: white;" href="javascript:juego.mostrar_ranking_click();" title="<fmt:message key="menu.ranking" />">
						<i class="fas fa-trophy"></i>
					</a>
				</li>
				<li class="nav-item btn-warning">
					<a class="nav-link" style="color: white;" href="javascript:juego.mostrar_reglas_click()" title="<fmt:message key="rules.title" />">
						<i class="fas fa-question-circle"></i>
					</a>
				</li>
			</ul>
			<div class="tab-content bg-white">
				<div class="tab-pane show active" id="tab_jugadores" role="tabpanel">
					<ul id="jugadores" class="list-group pt-1" style="height: 350px; overflow-y: auto;">
					</ul>
				</div>
				
				<div class="tab-pane" id="tab_partidos" role="tabpanel">
					<ul id="partidos" class="list-group pt-1" style="height: 350px; overflow-y: auto;">
					</ul>
				</div>
				
				<div class="tab-pane" id="tab_chat" role="tabpanel">
					<div class="form-inline card-body" style="display: inline-block; padding-top: 0px; width: 100%;">
						<div id="chat" style="height: 290px; background-color: AliceBlue; border: 1px solid CornflowerBlue; margin-top: 5px; margin-bottom: 5px; text-align: left; overflow-y: auto;"></div>
						<input type="text" id="texto" class="form-control" />
						<button id="boton_enviar" class="btn btn-primary" onclick="juego.sala.conexion.enviar_mensaje();"><fmt:message key="connection.send" /></button>
					</div>
				</div>
			</div>

		</div>
	</section>

</body>
</html>

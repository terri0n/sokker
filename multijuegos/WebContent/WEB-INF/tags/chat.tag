<%@tag import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="nombre" type="java.lang.String" required="true" %>
<%@ attribute name="imagen" type="java.lang.String" required="true" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />
</head>

<body>
	<div id="div_chat" style="position: absolute; display: inline-block; right: -500px;">
		<div class="card p-2 gris_claro" style="height: 100%; width: 500px; vertical-align: middle; float: right">
	 		<div>
				<div style="float: left; margin: 10px;">
					<img src="${pageContext.request.contextPath}/img/${imagen}" />
					<br/>
					${nombre}
				</div>
				
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
					<a class="nav-link" style="color: white;" href="javascript:juego.sala.conexion.enviar('ra')" title="<fmt:message key="menu.ranking" />">
						<i class="fas fa-trophy"></i>
					</a>
				</li>
				<li class="nav-item btn-warning">
					<a class="nav-link" style="color: white;" href="javascript:juego.mostrar_reglas()" title="<fmt:message key="rules.title" />">
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
		<span id="desplegar_chat" title="<fmt:message key="chat.show_menu" />" class="puntero" style="float: right;" onclick="desplegar_chat_click()">◀</span>
	</div>

	<script>
		function desplegar_chat_click() {
			if ($('#div_chat').css("right") == '0px') {
				$('#div_chat').animate({right: '-500px'}, "fast");
				$('#desplegar_chat').html('◀');
			} else {
				$('#div_chat').animate({right: '0px'}, "fast");
				$('#desplegar_chat').html('▶');
			}
			pintar_tablero();
		}
		
		$(document).mouseup(function(e) {
		    var container = $("div[id ^= 'div_chat']");

		    // if the target of the click isn't the container nor a descendant of the container
		    if (!container.is(e.target) && container.has(e.target).length === 0) {
				if ($('#div_chat').css("right") == '0px') {
			    	desplegar_chat_click();
				}
		    }
		});
	</script>
</body>
</html>
<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />

class Conexion {
	constructor(url, sala) {
		this.sala = sala;
		this.url = url;
		this.conectar();
		
		$("#texto").keypress((e) => {
	        var code = (e.keyCode ? e.keyCode : e.which);
	        if (code == 13) {
	        	this.enviar_mensaje();
	            return true;
	        }
	    });
	}

	conectar() {
		this.webSocket = new WebSocket(this.url);
		this.webSocket.onopen = (message) => { this.wsOpen(message);};
		this.webSocket.onmessage = (message) => { this.wsGetMessage(message);};
		this.webSocket.onclose = (message) => { this.wsClose(message);};
		this.webSocket.onerror = (message) => { this.wsError(message);};
	}
	
	reconectar() {
		this.webSocket.close();
		this.conectar();
	}

	enviar_mensaje() {
		this.sala.juego.escribir_mensaje($("#texto").val().trim(), true);
		this.enviar("texto", encodeURIComponent($("#texto").val().trim()));
	    $("#texto").val("");
	    $("#texto").focus();
	}

	wsOpen(message) {
//		this.sala.juego.escribir_servidor("<fmt:message key="connection.connected" />");
	}
	
	// mensaje.data = '<acción>,<parámetros>';
	wsGetMessage(message) {
		console.log(message.data);
		const indice = message.data.indexOf(',');
		const accion = message.data.substring(0, indice);
		const params = message.data.substring(indice + 1);

		switch (accion) {
			case 'aceptar_partido':
				// Aceptar partido (color, duracion, rival)
				var split = params.split(",");
				this.sala.aceptar_partido(split[0], split[1], split[2]);
				break;
			case 'actualizar_ranking':
				// Actualizar ranking (usuario1, puntos1, usuario2, puntos2)
				var split = params.split(',');
				this.sala.juego.actualizar_ranking(split[0], parseInt(split[1]), split[2], parseInt(split[3]));
				break;
			case 'actualizar_tiempos':
				// Actualizar tiempos
				var tiempos = params.split('-');
				this.sala.juego.actualizar_tiempos(parseInt(tiempos[0]), parseInt(tiempos[1]));
				break;
			case 'conexion_abierta':
				// Conexión abierta { jugadores, partidos, nombre_rival }
				this.sala.conectado(JSON.parse(params));
				break;
			case 'movimiento_rival':
				// Fin de turno (tiempo_blancas-tiempo_negras@moviomientos)
				var split = params.split('@');
				var tiempos = split[0].split('-');
				this.sala.juego.movimiento_rival(parseInt(tiempos[0]), parseInt(tiempos[1]), JSON.parse(split[1]));
				break;
			case 'manda_tactica':
				// Rival manda táctica
				this.sala.juego.manda_tactica(JSON.parse(params));
				break;
			case 'nuevo_observador':
				// Nuevo observador (nombre)
				this.sala.nuevo_observador(params);
				break;
			case 'nuevo_partido':
				// nuevo_partido (duracion, nombre_blancas, nombre_negras)
				var split = params.split(',');
				this.sala.nuevo_partido(split[0], split[1], split[2]);
				break;
			case 'nuevo_usuario':
				this.sala.nuevo_usuario(JSON.parse(params));
				break;
			case 'observar_partido':
				// Observar partido (blancas-negras, tiempo_blancas-tiempo_negras, duracion@tablero)
				// Tb para reconectar
				var split_params = params.split('@');
				var split = split_params[0].split(',');
				var nombres = split[0].split('-');
				var tiempos = split[1].split('-');
				var duracion = split[2];
				var estado = split[3];
				var tablero = split_params[1];
				this.sala.juego.observar_partido(nombres[0], nombres[1], parseInt(tiempos[0]), parseInt(tiempos[1]), parseInt(duracion), parseInt(estado), JSON.parse(tablero));
				break;
			case 'partido_aceptado':
				// Partido aceptado -> informar (duracion, blancas-negras)
				var split = params.split(',');
				var jugadores = split[1].split('-');
				this.sala.informar_partido(split[0], jugadores[0], jugadores[1]);
				break;
			case 'partido_cancelado':
				// Partido cancelado (rival)
				this.sala.juego.partido_cancelado(params);
				break;
			case 'partido_privado':
				// Partido privado (color, duracion, rival)
				var split = params.split(',');
				this.sala.partido_privado(split[0], split[1], split[2]);
				break;
			case 'poner_away':
				// Poner away
				this.sala.poner_away(params);
				break;
			case 'quitar_away':
				// Quitar away
				this.sala.quitar_away(params);
				break;
			case 'ranking':
				// Ranking
				this.sala.juego.mostrar_ranking(JSON.parse(params));
				break;
			case 'texto':
				// Texto (quien, mensaje)
				var split = params.split(',');
				if (split[0] == this.sala.juego.nombre_rival) {
					this.sala.juego.escribir_mensaje(decodeURIComponent(split[1]), false);
				} else {
					this.sala.juego.escribir_observador(split[0], split[1]);
				}
				break;
			case 'tiempo_agotado':
				// Tiempo agotado
				var tiempos = params.split('-');
				this.sala.juego.tiempo_agotado(parseInt(tiempos[0]), parseInt(tiempos[1]));
				break;
			case 'usuario_desconectado':
				this.sala.usuario_desconectado(params);
				break;
			case 'volver_a_jugar':
				// Volver a jugar
				this.sala.volver_a_jugar();
				break;
		}
	}
	
	wsClose(message) {
		showToast.show(message.reason, 4000);
		this.sala.juego.escribir_servidor('<fmt:message key="connection.disconnected" />');
	
		console.log(message.code);
		if (message.code == 1002 || message.code == 1003 || message.code == 1008) {
			// 1002 = Nombre o contraseña incorrectos
			// 1003 = Se ha realizado otra conexión desde la misma sesión
			// 1008 = Nombre o contraseña incorrectos
			$('.modal-footer .bootbox-accept').prop('disabled', false);
			$('#span_email').hide();
		} else {
			location.reload();
		}
	}
	
	wsError(message) {
		this.sala.juego.escribir_servidor('Error ... ');
	}
	
	enviar(accion, params) {
		this.webSocket.send(accion + ',' + (params || ''));
		this.sala.iniciar_timeout(false);
	}
}
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
		this.sala.vaciar_sala();
		this.conectar();
	}

	enviar_mensaje() {
		this.sala.juego.escribir_mensaje($("#texto").val().trim(), true);
		this.enviar("texto", encodeURIComponent($("#texto").val().trim()));
	    $("#texto").val("");
	    $("#texto").focus();
	}

	wsOpen(message) {}
	
	<%-- mensaje.data = '<acción>,<parámetros>'; --%>
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
			case 'actualizar_tiempos':
				// Actualizar tiempos
				var tiempos = params.split('-');
				this.sala.juego.actualizar_tiempos(parseInt(tiempos[0]), parseInt(tiempos[1]));
				break;
			case 'conexion_abierta':
				// Conexión abierta { jugadores, partidos, jugador, rival, color }
				this.sala.accion_conexion_abierta(JSON.parse(params));
				break;
			case 'partido_finalizado':
				// (tiempo_blancas-tiempo_negras, ganador, puntos_blancas, puntos_negras)
				var split = params.split(',');
				var tiempos = split[0].split('-');
				this.sala.juego.partido_finalizado(parseInt(tiempos[0]), parseInt(tiempos[1]), split[1], parseInt(split[2]), parseInt(split[3]));
				break;
			case 'manda_tactica':
				// Rival manda táctica
				this.sala.juego.manda_tactica(JSON.parse(params));
				break;
			case 'movimiento_rival':
				// Movimiento del rival (moviomiento)
				this.sala.juego.movimiento_rival(JSON.parse(params));
				break;
			case 'movimientos_turno_rival':
				// Fin de turno (tiempo_blancas-tiempo_negras,estado@moviomientos)
				var split = params.split('@');
				var split2 = split[0].split(',');
				var tiempos = split2[0].split('-');
				this.sala.juego.movimientos_turno_rival(parseInt(tiempos[0]), parseInt(tiempos[1]), split2[1], JSON.parse(split[1]));
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
				this.sala.accion_nuevo_usuario(JSON.parse(params));
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
				// Partido privado { color, duracion, rival }
				this.sala.partido_privado(JSON.parse(params));
				break;
			case 'poner_away':
				// Poner away
				this.sala.poner_away(params);
				break;
			case 'quitar_away':
				// Quitar away
				this.sala.quitar_away(params);
				break;
			case 'mostrar_ranking':
				// Ranking
				this.sala.juego.accion_mostrar_ranking(JSON.parse(params));
				break;
			case 'texto':
				// Texto (quien, mensaje)
				var split = params.split(',');
				if (split[0] == this.sala.juego.rival.nombre) {
					this.sala.juego.escribir_mensaje(decodeURIComponent(split[1]), false);
				} else {
					this.sala.juego.escribir_observador(split[0], split[1]);
				}
				break;
			case 'usuario_desconectado':
				this.sala.usuario_desconectado(params);
				break;
			case 'volver_a_jugar':
				// Volver a jugar
				this.sala.juego.volver_a_jugar();
				break;
			default:
				alert('Acción incorrecta: ' + accion);
		}
	}
	
	wsClose(message) {
		showToast.show(message.reason, 4000);
		this.sala.juego.escribir_servidor(null, '<fmt:message key="connection.disconnected" />');
		this.sala.vaciar_sala();
	
		console.log(message.code, message.reason);
		if (message.code == 1001 || message.code == 1002 || message.code == 1003 || message.code == 1008) {
			// 1001 = La sesión ha caducado
			// 1002 = Nombre o contraseña incorrectos, http 500
			// 1003 = Se ha realizado otra conexión desde la misma sesión
			// 1008 = Nombre o contraseña incorrectos
			$('.modal-footer .bootbox-accept').prop('disabled', false);
			$('#span_email').hide();
			
			mostrar_mensaje(message.reason, true);
		} else {
			location.reload();
		}
	}
	
	wsError(message) {
		showToast.show(message.reason, 4000);
		console.log('Error: ' + message);
		reconectar();
	}
	
	enviar(accion, params) {
		this.enviar_sin_timeout(accion, params);
		this.sala.iniciar_timeout(false);
	}
	
	enviar_sin_timeout(accion, params) {
		var data = {
        	accion: accion,
        	params: params
    	};
		this.webSocket.send(JSON.stringify(data));
	}
}
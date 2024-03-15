<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

class Juego {
	constructor(jugador, url) {
		this.jugador = new Jugador(jugador, this);
		this.rival;				// Jugador rival, null si soy observador

		this.timer;
		this.turno;				// Color del jugador que tiene el turno. Al empezar es null, cuando envía la táctica el turno es del rival, a menos que lleve las blancas y sea el 2º en enviar la táctica
		this.color;				// Color del jugador, si está en una partida pero no como observador
		this.movimientos = [];	// Lista de movimientos del turno
		this.tiempo = [];		// { 'blancas', 'negras' }	// En segundos
		this.duracion;			// Para usarla al jugar un nuevo partido (en minutos)
		this.estado;			// 0: sin iniciar
								// 1: táctica mandada
								// 2: saque inicial, turno blancas
								// 3: partido iniciado, turno blancas
								// 4: partido iniciado, turno negras
								// null: partido finalizado
		
		this.sala = new Sala(this, url);
		this.wakeLock = null;	// Lock para que no se apague la pantalla en el móvil
	}

	async requestWakelock() {
		if ('wakeLock' in navigator) {
			try {
			    this.wakeLock = await navigator.wakeLock.request("screen");
			} catch (err) {
			    console.log(`${err.name}, ${err.message}`);
			}
  		}
	}

	iniciar_cronometros() {
		if (this.tiempo['blancas'] || this.tiempo['negras']) {
			$('#tiempo_blancas').css("color", "");
			$('#tiempo_negras').css("color", "");
			this.pintar_tiempos();
			
			if (!this.timer) {
				this.timer = setInterval(() => {
					if (this.turno) {
						this.tiempo[this.turno]--;
						this.pintar_tiempos();
						if (this.tiempo[this.turno] <= 0) {
							clearInterval(this.timer);
							this.timer = null;
							this.sala.conexion.enviar("tiempo_agotado", null);
						}
					}
				}, 1000);
			}
		}
	}

	actualizar_tiempos(tiempo_blancas, tiempo_negras) {
		this.tiempo['blancas'] = tiempo_blancas;
		this.tiempo['negras'] = tiempo_negras;
	
		this.pintar_tiempos();
	}
	
	pintar_tiempos() {
		if (this.tiempo['blancas'] || this.tiempo['negras']) {
			$('#span_tiempo_blancas').show();
			$('#span_tiempo_negras').show();
		} else {
			$('#span_tiempo_blancas').hide();
			$('#span_tiempo_negras').hide();
		}

		if (this.turno && this.tiempo[this.turno] <= 60) {
			if (this.turno == 'blancas') {
				$('#tiempo_blancas').css("color", "red");
			} else {
				$('#tiempo_negras').css("color", "red");
			}
		}

		$('#tiempo_blancas').text(secondsToTime(this.tiempo['blancas']));
		$('#tiempo_negras').text(secondsToTime(this.tiempo['negras']));
	}
	
	cambiar_color(c) {
		return c == 'blancas' ? 'negras' : 'blancas';
	}

	traducir(texto) {
		switch (texto) {
			case 'blancas': return '<fmt:message key="menu.white" />'.toLowerCase();
			case 'negras': return '<fmt:message key="menu.black" />'.toLowerCase();
			
			case 'principiante': return '<fmt:message key="levels.apprentice" />';
			case 'aficionado': return '<fmt:message key="levels.amateur" />';
			case 'experto': return '<fmt:message key="levels.expert" />';
			case 'maestro': return '<fmt:message key="levels.master" />';
			case 'maestro_internacional': return '<fmt:message key="levels.international_master" />';
			case 'gran_maestro': return '<fmt:message key="levels.great_master" />';
			case 'campeon_del_mundo': return '<fmt:message key="levels.world_champion" />';
			case 'no_registrado': return '<fmt:message key="levels.not_registered" />';
		}
	}

	actualizar_turno(c, mostrar_mensaje) {
		this.turno = c;
		if (this.turno == this.color && mostrar_mensaje) {
			showToast.show("<fmt:message key="msg.yourTurn" />");
		}
		this.actualizar_texto_turno();
	}

	// Tb se llama al reconectar
	observar_partido(blancas, negras, tiempo_blancas, tiempo_negras, duracion, estado, tablero) {
		this.duracion = duracion;
		this.tiempo['blancas'] = tiempo_blancas;
		this.tiempo['negras'] = tiempo_negras;
		this.estado = estado;

		this.pintar_ui();
		this.actualizar_turno(this.estado == 2 || this.estado == 3 ? 'blancas' : 'negras', true);

		// Si no soy observador...
		if (this.rival) {
			// Si el partido aún no ha empezado no decimos nada. Lo escribiremos cuando se manden las tácticas y empiece de verdad
			if (this.estado >= 2) {
				if (this.jugador.nombre == blancas || this.jugador.nombre == negras) {
					this.escribir_servidor(null, '<fmt:message key="connection.resuming" /> ' + blancas + ' vs ' + negras);
				} else {
					this.escribir_servidor(null, '<fmt:message key="msg.watching" /> ' + blancas + ' vs ' + negras);
				}
			}
			$('#boton_nuevo').hide();
		}
	
		if (this.estado < 2) {
			// Hasta que no empieza el partido no hay turno
			this.pintar_tiempos();
			this.actualizar_texto_turno();
			this.habilitar_color(this.estado == 0);
		} else if (this.estado == 2) {
			this.iniciar_cronometros();
			this.actualizar_texto_turno();
			this.habilitar_color(this.color == 'blancas');
		} else if (!isNaN(this.estado)) {
			this.iniciar_cronometros();
			this.actualizar_texto_turno();
			this.habilitar_color(this.turno == this.color);
		} else {
			// Fin del partido
			this.pintar_tiempos();
			this.actualizar_texto_turno();
		}

		this.pintar_movimientos(tablero, 0, 0, null);
	}

	partido_finalizado(tiempo_blancas, tiempo_negras, ganador, puntos_blancas, puntos_negras) {
		this.tiempo['blancas'] = tiempo_blancas;
		this.tiempo['negras'] = tiempo_negras;
		this.pintar_tiempos();
		
		setTimeout(() => {
			if (tiempo_blancas <= 0 || tiempo_negras <= 0) {
				showToast.show("<fmt:message key="msg.timeOut" />");
			} else {
				showToast.show("<fmt:message key="msg.goal" />");
			}

			if (this.color) {
				this.escribir_servidor(null, ganador == this.color ? '<fmt:message key="msg.youWin" />' : '<fmt:message key="msg.youLose" />');
				this.actualizar_texto_turno();
			} else {
				this.escribir_servidor(null, '<fmt:message key="msg.endOfGame" />'.replace('{0}', ganador == 'blancas' ? this.jugador.nombre : this.rival.nombre));
			}
	
			let puntos_rival = this.color == 'negras' ? puntos_blancas : puntos_negras;
			let puntos_jugador = this.color == 'negras' ? puntos_negras : puntos_blancas;
			
			if ((this.jugador.puntos || '') != (puntos_jugador || '')) {
				var mensaje1 = this.jugador.mostrar_jugador();
				this.jugador.puntos = puntos_jugador;
				mensaje1 += '<span class="texto_mensaje"> pasa a </span>' + this.jugador.mostrar_jugador();
				this.escribir_servidor(null, mensaje1);
			}
			
			if ((this.rival.puntos || '') != (puntos_rival || '')) {
				var mensaje2 = this.rival.mostrar_jugador();
				this.rival.puntos = puntos_rival;
				mensaje2 += '<span class="texto_mensaje"> pasa a </span>' + this.rival.mostrar_jugador();
				this.escribir_servidor(null, mensaje2);
			}
	
			// Actualizar partido
			this.sala.borrar_partido('partido_' + this.jugador.nombre + '-' + this.rival.nombre);
			this.sala.nuevo_partido(this.duracion, this.jugador.nombre, this.rival.nombre);
	
			// Actualizar usuarios
			this.sala.borrar_usuario(this.jugador.nombre);
			this.sala.borrar_usuario(this.rival.nombre);
			this.sala.crear_usuario(this.jugador);
			this.sala.crear_usuario(this.rival);
		}, 500);
		
		clearInterval(this.timer);
		this.timer = null;
		this.estado = null;
	}

	accion_mostrar_ranking(ranking) {
		var mensaje = ' <ol id="lista_ranking" style="padding-left: 5px; list-style: decimal inside;">';
		ranking.forEach(j => {
	    	mensaje += '<li style="display: list-item;">' + new Jugador(j, this).mostrar_jugador() + '</li>';
	    });
		mensaje += '</ol>';			
		
		const box = bootbox.alert({ 
		    title: '<span class="material-icons">emoji_events</span> <fmt:message key="menu.ranking" />',
		    size: 'small',
		    scrollable: true,
			backdrop: true,
			closeButton: false,
		    locale: '${pageContext.request.locale.language}',
		    message: mensaje,
			buttons: {
				ok: {
					className: 'button azul'
				}
			}
		});

		box.on('shown.bs.modal', () => {
			$('.scroll ol').scrollTop(0);
		});
	}


	// El propio usuario	
	cancelar_partido(quien) {
   		this.sala.cancelar_partido(quien);
		$('#juego').addClass('hidden');

		if (this.wakeLock) {
			this.wakeLock.release().then(() => {
				this.wakeLock = null;
			}).catch(error => {
            	console.error('Error al liberar el WakeLock:', error);
        	});
		}
	}

	// El rival
	partido_cancelado(quien) {
   		this.sala.partido_cancelado(quien);
		$('#juego').addClass('hidden');
	}
	
	volver_a_jugar() {
		$('#boton_otro').hide();
		$('#boton_cancelar').hide();
	
		// Intercambiamos los colores
		this.empezar(this.cambiar_color(this.color), this.duracion);
	}

	/////////
	// CHAT
	/////////
	
	escribir(mensaje, clase_mensaje, clase_hora) {
		const chat = $("#chat")[0];
		const shouldScroll = chat.scrollTop + chat.clientHeight >= chat.scrollHeight - 1;
		const hora = dos_digitos(new Date().getHours()) + ":" + dos_digitos(new Date().getMinutes());
		
		mensaje = '<div class="mensaje ' + clase_mensaje + '">' + 
				'<small class="' + clase_hora + '">' + hora + '</small> ' + mensaje +
			'</div>';
		$("#chat").append(mensaje);
		if (shouldScroll) {
			chat.scrollTop = chat.scrollHeight;
		}
	}

	escribir_mensaje(mensaje, mio) {
		if (mio) {
			this.escribir('<span class="span_texto">' + mensaje + '</span>', 'mensaje_mio', 'verde_oscuro');
		} else {
			this.escribir('<span class="span_texto">' + mensaje + '</span>', 'mensaje_rival', 'gris');
		}
	}

	escribir_observador(quien, mensaje) {
		this.escribir('<span class="jugador">' + quien + '</span> <span class="span_texto">' + mensaje + '</span>', 'mensaje_observador', 'gris');
	}

	escribir_servidor(quien, mensaje) {
		this.escribir('<b><i>' + (quien ? '<span class="jugador">' + quien + '</span>' : '') + ' <span class="span_texto">' + mensaje + '</span></i></b>', 'mensaje_servidor', 'rojo');
	}

	////////////
	// EVENTOS
	////////////
	
	abandonar_click() {
		bootbox.confirm({ 
			message: '<span class="material-icons">help</span> <fmt:message key="menu.abortGame" />',
			backdrop: true,
			closeButton: false,
			locale: '${pageContext.request.locale.language}',
			swapButtonOrder: true,
			buttons: {
				confirm: {
					className: 'button verde'
				},
				cancel: {
		            className: 'button rojo'
				}
			},
			callback: (result) => { 
		    	if (result) {
		    		this.cancelar_partido(this.jugador.nombre);
					this.sala.conexion.enviar('cancelar_partido', null);
		    	}
		    }
		});
	}

	mostrar_ranking_click() {
		this.sala.conexion.enviar('ranking', null);
	}

	////////////////////////////
	// Funciones a sobrecargar
	////////////////////////////
	
	pintar_tablero() {}
	pintar_figuras() {}
	mostrar_reglas_click() {}
	movimiento_rival(movs) {}
	empezar(c, duracion) {
		this.requestWakelock();
	}
}

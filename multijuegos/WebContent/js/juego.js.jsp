<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />

class Juego {
	constructor(nombre, url) {
		this.nombre = nombre;
		this.turno;				// Color del jugador que tiene el turno. Al empezar es null, cuando envía la táctica el turno es del rival, a menos que lleve las blancas y sea el 2º en enviar la táctica
		this.color;				// Color del jugador, si está en una partida pero no como observador
		this.movimientos = [];	// Lista de movimientos del turno
		this.nombre_rival;		// Nombre del rival, null si soy observador
		this.tiempo = [];		// { 'blancas', 'negras' }
		this.puntos = {};
		this.duracion;			// Para volver a mostrarla al actualizar el ranking
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

	tiempo_agotado(tiempo_blancas, tiempo_negras) {
		this.tiempo['blancas'] = tiempo_blancas;
		this.tiempo['negras'] = tiempo_negras;
		this.pintar_tiempos();
		
		const ganan_blancas = tiempo_blancas > 0;

		this.fin_partido(this.color == 'negras' ^ ganan_blancas);
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
		}
	}

	actualizar_turno(c, mostrar_mensaje) {
		this.turno = c;
		$('#turno_blancas').css('display', this.turno == 'blancas' ? 'inline' : 'none');
		$('#turno_negras').css('display', this.turno == 'negras' ? 'inline' : 'none');
		if (this.turno == this.color && mostrar_mensaje) {
			showToast.show("<fmt:message key="msg.yourTurn" />");
		}
		this.actualizar_texto_turno();
	}

	// Tb se llama al reconectar
	observar_partido(blancas, negras, tiempo_blancas, tiempo_negras, duracion, estado, tablero) {
		this.tiempo['blancas'] = tiempo_blancas;
		this.tiempo['negras'] = tiempo_negras;
		this.estado = estado;

		// Actualizamos nombres
		this.sala.actualizar_nombres(blancas, negras);

		// Si no soy observador...
		if (this.nombre_rival) {
			// Si el partido aún no ha empezado no decimos nada. Lo escribiremos cuando se manden las tácticas y empiece de verdad
			if (this.estado >= 2) {
				if (this.nombre == blancas || this.nombre == negras) {
					this.escribir_servidor(null, '<fmt:message key="connection.resuming" /> ' + blancas + ' vs ' + negras);
				} else {
					this.escribir_servidor(null, '<fmt:message key="msg.watching" /> ' + blancas + ' vs ' + negras);
				}
			}
			$('#boton_nuevo').hide();
			$('#finalizar').show();
		}
	
		this.pintar_ui();

		this.pintar_movimientos_jugador(tablero, 0, 'blancas', () => {
			if (this.estado < 2) {
				this.iniciar_cronometros();
				this.habilitar_color(this.color == this.turno);

				// Hasta que no empieza el partido no hay turno
				this.actualizar_texto_turno();
				this.habilitar_color(this.estado == 0);
			} else if (this.estado == 2) {
				this.pintar_tiempos();
				this.actualizar_texto_turno();
				this.habilitar_color(this.color == 'blancas');
			} else {
				this.pintar_tiempos();
				this.actualizar_turno(this.estado == 2 || this.estado == 3 ? 'blancas' : 'negras', true);
				this.habilitar_color(this.turno == this.color);
			}
		});
	}

	// Pinta una lista de movimientos de dos jugadores para el observador
	// Duracion = 0 para que se pinten instantáneamente
	pintar_movimientos_jugador(movs, i, c, funcion) {
		this.pintar_movimientos(movs, 0, 0, () => {
			this.limpiar_tablero();
		});
		
		if (funcion) {
			funcion();
		}
	}

	// El partido del jugador/observador
	fin_partido(victoria) {
		clearInterval(this.juego.timer);
		this.juego.timer = null;

		if (this.juego.color) {
			this.juego.escribir_servidor(null, victoria ? '<fmt:message key="msg.youWin" />' : '<fmt:message key="msg.youLose" />');
			actualizar_texto_turno();
		} else {
			this.juego.escribir_servidor(null, '<fmt:message key="msg.endOfGame" />'.replace('{0}', victoria ? $('#jugador1').html() : $('#jugador2').html()));
		}
	}

	actualizar_ranking(usuario1, puntos1, usuario2, puntos2) {
		var mensaje1 = this.mostrar_jugador(usuario1, false);
		this.puntos[usuario1] = puntos1;
		mensaje1 += ' pasa a ' + this.mostrar_jugador(usuario1, false);

		var mensaje2 = this.mostrar_jugador(usuario2, false);
		this.puntos[usuario2] = puntos2;
		mensaje2 += ' pasa a ' + this.mostrar_jugador(usuario2, false);

		if (usuario1 == this.nombre || usuario1 == this.nombre_rival) {
			this.escribir_servidor(null, mensaje1);
			this.escribir_servidor(null, mensaje2);
			this.sala.actualizar_nombres(usuario1, usuario2);
		}
		
		// Actualizar partido
		this.sala.borrar_partido('partido_' + usuario1 + '-' + usuario2);
		this.sala.nuevo_partido(this.duracion, usuario1, usuario2);

		// Actualizar usuarios
		this.sala.borrar_usuario(usuario1);
		this.sala.borrar_usuario(usuario2);
		this.sala.crear_usuario(usuario1, puntos1);
		this.sala.crear_usuario(usuario2, puntos2);
	}
	
	mostrar_jugador(jugador, derecha, puntos) {
		var s = '';
		var t = '';
		if (jugador) {
			const p = parseInt(puntos || this.puntos[jugador]);
			if (p) {
				if (p < 1400) {
					var nivel = 'principiante';
				} else if (p < 1800) {
					var nivel = 'aficionado';
				} else if (p < 2100) {
					var nivel = 'experto';
				} else if (p < 2400) {
					var nivel = 'maestro';
				} else if (p < 2600) {
					var nivel = 'maestro_internacional';
				} else if (p < 2800) {
					var nivel = 'gran_maestro';
				} else {
					var nivel = 'campeon_del_mundo';
				}
				// NOTA: no funciona el Locale en español
				s = `<span class="info_jugador ` + nivel + `" title="` + this.traducir(nivel) + `">
						<img class="bandera" src="img/banderas/ES.png" /><span class="puntos">` + p.toLocaleString('de-DE') + `</span>
					</span>`;
				t = `<span class="jugador" id="nombre_` + jugador + `">` + jugador + `</span>
					<div class="away" style="display:none" title="Away">zZ<span>Z</span></div>`;
			}
		}
		return derecha ? t + s : s + t;
	}

	mostrar_ranking_click() {
		this.sala.conexion.enviar('ranking', null);
	}

	mostrar_ranking(ranking) {
		var mensaje = ' <ol id="lista_ranking" style="padding-left: 5px; list-style: decimal inside;">';
		ranking.forEach(j => {
	    	mensaje += '<li style="display: list-item;">' + this.mostrar_jugador(j.nombre, false, j.puntos) + '</li>';
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
			this.escribir(mensaje, 'mensaje_mio', 'verde_oscuro');
		} else {
			this.escribir(mensaje, 'mensaje_rival', 'gris');
		}
	}

	escribir_observador(quien, mensaje) {
		this.escribir('<span class="jugador">' + quien + '</span><span class="span_texto">' + mensaje + '</span>', 'mensaje_observador', 'gris');
	}

	escribir_servidor(quien, mensaje) {
		this.escribir('<b><i>' + (quien ? '<span class="jugador">' + quien + '</span>' : '') + ' <span class="span_texto">' + mensaje + '</span></i></b>', 'mensaje_servidor', 'rojo');
	}

	////////////
	// EVENTOS
	////////////
	
	texto_keypress(event) {
		if (event.keyCode === 13) {
	        this.sala.conexion.enviar_mensaje();
	    }
	}

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
		    		this.cancelar_partido(this.nombre);
					this.sala.conexion.enviar('cancelar_partido', null);
		    	}
		    }
		});
	}

	////////////////////////////
	// Funciones a sobrecargar
	////////////////////////////
	
	pintar_tablero() {}
	pintar_figuras() {}
	mostrar_reglas_click() {}
	movimiento_rival(tiempo_blancas, tiempo_negras, movs) {}
	empezar(c, duracion) {
		this.requestWakelock();
	}
}

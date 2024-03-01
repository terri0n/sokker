<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />

class Sala {
	constructor(juego, url) {
		this.juego = juego;
		this.url = url;
		this.timer = null;
		this.timer_away = null;
		this.conexion = new Conexion(url, this);
	}

	conectado(resp) {
		// Si la sesión ha caducado el nombre no coincidirá
		if (resp.nombre != '${jugador.nombre}') {
			location.reload();
		}

		$('.bootbox.modal').modal('hide');
		
		this.limpiar_botones();
		
		this.juego.nombre_rival = resp.nombre_rival;
		this.juego.color = resp.color;
	
		$('#jugadores').empty();
		resp.jugadores.forEach(j => {
			this.nuevo_usuario(j);
		});
	
		$('#partidos').empty();
		resp.partidos.forEach(p => {
			const indice = p.indexOf(',');
			const duracion = p.substring(0, indice);
			const jugadores = p.substring(indice + 1).split("-");
			this.nuevo_partido(duracion, jugadores[0], jugadores[1]);
		});

		this.iniciar_timeout(true);
		
		// Si el partido ya se ha iniciado, pido la lista de movimientos (como si aceptara el partido otra vez)
		// Lo hacemos así para que se cree cada cosa a su tiempo
		if (this.juego.nombre_rival) {
			this.conexion.enviar('aceptar_partido', (this.juego.color == 'blancas' ? this.juego.nombre : this.juego.nombre_rival));	
		}
	}

	// NOTA: la coma es necesaria para delimitar la acción de los parámetros
	iniciar_timeout(primero) {
		if (!this.timer_away && !primero) {
			this.conexion.webSocket.send("quitar_away,");
		} else {
			clearTimeout(this.timer_away);
		}
		
		this.timer_away = setTimeout(() => {
			this.timer_away = null;
			this.conexion.webSocket.send("poner_away,");
		}, 300000);
	}
	
	poner_away(quien) {
		$('#jugador_' + quien + ' .away').show();
	}

	quitar_away(quien) {
		$('#jugador_' + quien + ' .away').hide();
	}

	borrar_usuario(usuario) {
		delete this.juego.puntos[usuario];
		$('#jugador_' + usuario).remove();
		this.actualizar_numero('jugadores');
	}

	crear_usuario(nombre, puntos) {
		if (this.juego.puntos[nombre]) {
			this.borrar_usuario(nombre);
		}

		this.juego.puntos[nombre] = puntos;
		const disabled = nombre.toUpperCase() == this.juego.nombre.toUpperCase() || this.juego.nombre_rival ? 'disabled = "disabled"' : '';
		
		$('#jugadores').append(`<li id="jugador_` + nombre + `">
				<div>` + this.juego.mostrar_jugador(nombre) + `</div>
				<button class="verde" ` + disabled + ` onclick="juego.sala.invitar_click('` + nombre + `')"><fmt:message key="menu.invite" /></button>
			</li>`);
			
		this.actualizar_numero('jugadores');
	}

	nuevo_usuario(jugador) {
		this.crear_usuario(jugador.nombre, jugador.puntos);

		if (jugador.nombre == this.juego.nombre_rival) {
			this.juego.escribir_servidor(jugador.nombre, '<fmt:message key="msg.enteredGame" />');
			$('#boton_cancelar').hide();
			this.juego.actualizar_texto_turno();
			//this.juego.iniciar_cronometros();
		}
	}

	cancelar_partido(quien) {
		const partido = $('li[id="partido_' + quien + '"], li[id^="partido_' + quien + '-"], li[id^="partido_"][id$="-' + quien + '"]');
		if (partido.length > 0) {
			if (quien == this.juego.nombre || quien == this.juego.nombre_rival) {
				if (quien == this.juego.nombre_rival) {
					showToast.show('<fmt:message key="msg.gameAborted" />');
					this.juego.escribir_servidor(null, '<fmt:message key="msg.gameAborted" />');
					this.conexion.enviar('rival_cancela', null);
				}
				this.finalizar_partido(partido.attr('id'));
			} else {
				this.borrar_partido(partido.attr('id'));
			}
		}

		this.botones_cancelar();

		// Habilito el resto de partidos
		this.habilitar_partidos(true);
	}
	
	habilitar_partidos(habilitar) {
		const partidos = $('li[id^="partido_"]');
		partidos.get().forEach(p => {
			const id = p.id.substring("partido_".length).split('-');
			if (id[0] != this.juego.nombre && (id.length == 1 || id[1] != this.juego.nombre)) {
				$(p).find("button").prop("disabled", !habilitar);
			}
		});
		
		const jugadores = $('li[id^="jugador_"]');
		jugadores.get().forEach(j => {
			const jugador = j.id.substring("jugador_".length);
			if (jugador != this.juego.nombre) {
				$(j).find("button").prop("disabled", !habilitar);
			}
		});
	}

	nuevo_partido(duracion, usuario1 /*opcional*/, usuario2 /*opcional*/) {
		var disabled = this.juego.nombre_rival || this.juego.nombre == usuario1 || this.juego.nombre == usuario2 ? 'disabled = "disabled"' : '';
		
		const join = '<button class="verde" ' + disabled + ' onclick="juego.sala.partido_click(\'' + (usuario1 || usuario2) + '\');"><fmt:message key="menu.join" /></button>';

		const id = "partido_" + (usuario1 || '') + (usuario1 && usuario2 ? '-' : '') + (usuario2 || '');
		var s = '<li id="' + id + '">' + (this.juego.mostrar_jugador(usuario1) || join);
		s += ' vs ' + (this.juego.mostrar_jugador(usuario2) || join);
		if (duracion) {
			s += ' <small><span class="material-icons">timer</span> ' + duracion + ' min</small>';
		}
		
		if (usuario1 && usuario2) {
			// Si el partido ha empezado...
			s += ' <button class="azul" ' + disabled + ' onclick="juego.sala.partido_click(\'' + (usuario1 || usuario2) + '\');"><fmt:message key="menu.watch" /></button>';
		} else {
			// Si es mi partido
			if ((usuario1 == this.juego.nombre || usuario2 == this.juego.nombre) && (!usuario1 || !usuario2)) {
				this.botones_nuevo_partido();
			}
		}
	
		s += '</li>';
		$('#partidos').append(s);
		
		this.actualizar_numero('partidos');
	}
	
	botones_nuevo_partido() {
		$('#boton_nuevo').hide();
		$('#finalizar').text('<fmt:message key="msg.waitingOpponent" />');
		$('#finalizar').prop('disabled', true);
		$('#finalizar').show();
		$('#boton_cancelar').prop('disabled', false);
		$('#boton_cancelar').show();
	}

	botones_cancelar() {
		$('#boton_cancelar').hide();
		$('#finalizar').hide();
		$('#boton_nuevo').attr('disabled', false);
		$('#boton_nuevo').show();
	}
	
	usuario_desconectado(usuario) {
		this.borrar_partido('partido_' + usuario);
		
		if (usuario == this.juego.nombre_rival) {
			$('#finalizar').prop('disabled', true);
			$('#finalizar').text('<fmt:message key="msg.waitingOpponent" />');
			$('#boton_cancelar').show();
			$('#boton_cancelar').prop('disabled', false);
			this.juego.escribir_servidor(usuario, '<fmt:message key="connection.disconnected" />'.toLowerCase());
			clearInterval(this.juego.timer);
			this.juego.timer = null;
		}

		this.borrar_usuario(usuario);
	}

	// El partido se ha aceptado -> empezar (tanto blancas como negras)
	aceptar_partido(c, duracion, rival) {
		this.juego.nombre_rival = rival;
		this.informar_partido(duracion, c == 'blancas' ? this.juego.nombre : rival, c == 'negras' ? this.juego.nombre : rival);
		this.habilitar_partidos(false);
		$('#boton_cancelar').hide();
		this.juego.empezar(c, duracion);
	}

	// Un partido cualquiera se ha iniciado -> informar
	informar_partido(duracion, usuario1, usuario2) {
		this.borrar_partido('partido_' + usuario1);
		this.borrar_partido('partido_' + usuario2);
		this.nuevo_partido(duracion, usuario1, usuario2);
	}

	borrar_partido(id) {
		$('#' + id).remove();
		this.actualizar_numero('partidos');
	}
	
	// Partido cancelado
	finalizar_partido(id) {
		this.borrar_partido(id);
		this.limpiar_botones();
		
		clearInterval(this.juego.timer);
		this.juego.timer = null;
	}

	limpiar_botones() {	
		this.actualizar_nombres('', '');
		$('#finalizar').hide();
		$('#boton_otro').hide();
		$('#boton_cancelar').hide();
		$('#boton_nuevo').show();
		$('#boton_nuevo').prop('disabled', false);
		$('#turno_blancas').hide();
		$('#turno_negras').hide();
		this.juego.nombre_rival = null;
	}

	volver_a_jugar() {
		$('#boton_otro').hide();
		$('#boton_cancelar').hide();
	
		// Intercambiamos los colores
		this.juego.empezar(this.juego.cambiar_color(this.juego.color));
	}

	nuevo_observador(quien) {
		this.juego.escribir_servidor(quien, '<fmt:message key="msg.enteredGame" />');
	}

	actualizar_numero(id) {
		const num = $('#' + id + ' li').length;
		$('#num_' + id).text(num);
	}

	actualizar_nombres(blancas, negras) {
		$('#jugador1').html(this.juego.mostrar_jugador(blancas));
		$('#jugador2').html(this.juego.mostrar_jugador(negras));
	}

	opciones_partido(usuario, color, duracion, titulo, disabled, funcionOK, funcionKO) {
		bootbox.confirm({ 
		    title: '<span class="material-icons">' + (disabled ? 'help' : 'settings') + '</span> ' + titulo,
			backdrop: true,
			closeButton: false,
		    message: '<div style="text-align: center;">\
	    			<div style="display: inline-block; text-align: left; vertical-align: top;" class="card">\
			    		<div class="card-header">\
			    			<b><fmt:message key="menu.color" /></b>\
						</div>\
						<div class="card-body">\
							<div class="custom-control custom-radio">\
							  	<input type="radio" class="custom-control-input" id="blancas" name="color" value="blancas" ' + disabled + (color == "blancas" ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="blancas"><fmt:message key="menu.white" /></label>\
							</div>\
							<div class="custom-control custom-radio mt-2">\
							  	<input type="radio" class="custom-control-input" id="negras" name="color" value="negras" ' + disabled + (color == "negras" ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="negras"><fmt:message key="menu.black" /></label>\
							</div>\
						</div>\
					</div>\
					\
	    			<div style="display: inline-block; text-align: left; vertical-align: top;" class="card ml-5">\
			    		<div class="card-header">\
			    			<b><fmt:message key="menu.duration" /></b>\
						</div>\
						<div class="card-body">\
							<div class="custom-control custom-radio">\
							  	<input type="radio" class="custom-control-input" id="10min" name="duracion" value="10" ' + disabled + (duracion == "10" ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="10min">10 min</label>\
							</div>\
							<div class="custom-control custom-radio mt-2">\
							  	<input type="radio" class="custom-control-input" id="20min" name="duracion" value="20" ' + disabled + (duracion == "20" ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="20min">20 min</label>\
							</div>\
							<div class="custom-control custom-radio mt-2">\
							  	<input type="radio" class="custom-control-input" id="30min" name="duracion" value="30" ' + disabled + (duracion == "30" ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="30min">30 min</label>\
							</div>\
							<div class="custom-control custom-radio mt-2">\
							  	<input type="radio" class="custom-control-input" id="60min" name="duracion" value="60" ' + disabled + (duracion == "60" ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="60min">60 min</label>\
							</div>\
							<div class="custom-control custom-radio mt-2">\
							  	<input type="radio" class="custom-control-input" id="sinTiempo" name="duracion" value="" ' + disabled + (!duracion ? "checked" : "") + '>\
							  	<label class="custom-control-label" for="sinTiempo"><fmt:message key="menu.duration.noTime" /></label>\
							</div>\
	    				</div>\
	    			</div>\
				</div>',
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
		    		funcionOK($('input[name="color"]:checked').val(), $('input[name="duracion"]:checked').val());
		    	} else if (funcionKO) {
		    		funcionKO();
		    	}
		    }
		});
	}

	partido_privado(color, duracion, rival) {
		if (!this.juego.nombre_rival) {
			this.opciones_partido(rival, color, duracion, '<fmt:message key="menu.invitationFrom" /></h5> '.replace('{0}', rival), 'disabled="disabled "', () => {
				// Aceptar partido
				this.partido_click(rival);
			}, () => {
				this.conexion.enviar('cancelar_invitacion', rival);
			});
		}
	}

	// El rival
	partido_cancelado(quien) {
		if (this.juego.estado) {
			bootbox.alert({
				message: '<span class="material-icons">info</span> <fmt:message key="msg.gameAborted" />',
			    locale: '${pageContext.request.locale.language}',
				backdrop: true,
				closeButton: false,
				buttons: {
					ok: {
						className: 'button azul'
					}
				}
			});
		}
		this.juego.cancelar_partido(quien);
	}

	/** EVENTOS **/
	
	nuevo_click() {
		this.opciones_partido(null, 'blancas', 20, '<fmt:message key="menu.newGame" />', '', (color, duracion) => {
			this.conexion.enviar('crear_partido', color + ',' + duracion);
			$('#boton_nuevo').prop('disabled', true);
			$('#boton_cancelar').prop('disabled', false);
			$('#boton_cancelar').show();
		}, null);
	}

	// El propio usuario
	cancelar_click() {
		this.juego.cancelar_partido(this.juego.nombre);
		this.conexion.enviar('cancelar_partido', null);
	}

	otro_click() {
		$('#boton_otro').text('<fmt:message key="msg.waitingOpponent" />');
		$('#boton_otro').prop('disabled', true);
		this.conexion.enviar('otro_partido', null);
	}

	invitar_click(usuario) {
		this.opciones_partido(usuario, 'blancas', 20, '<fmt:message key="menu.newGame" /> <fmt:message key="menu.against" /> <span id="jugador">' + usuario + '</span>', '', (color, duracion) => {
			this.conexion.enviar('partido_privado', color + ',' + duracion + ',' + usuario);
			this.borrar_partido('partido_' + this.juego.nombre);
			this.botones_nuevo_partido();
		}, null); 
	}

	partido_click(usuario) {
		this.conexion.enviar('aceptar_partido', usuario);
	}
	
	tab_click(tab) {
		$('.nav-tabs a[href="#' + tab + '"] span').text('');
		if (tab == 'tab_chat') {
			setTimeout(() => {
				$('#texto').focus();
			}, 10);
		}
	}
}

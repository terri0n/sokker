var vista_resumen = false;

function seleccionar_todo_click(id) {
	var checked = $("#" + id).is(":checked");
	$("#" + id).closest(".grupo_checkbox").find("input[type='checkbox']").each(function() {
		$(this).prop("checked", checked);
	});
}

function actualizar_tactica(sufijo, tact) {
	if (sufijo == '_' || vista_resumen) {
		mostrar_resumen(sufijo);
	} else {
		showTactic(tact);
	}
}

function mostrar_resumen(sufijo) {
	if (vista_resumen) {
		$(ob).off('mousemove');
	}
	
	// Hago esto así porque el código de la táctica no admite que haya dos tácticas en la misma página
	const canvas = document.getElementById('canvas-boisko' + sufijo);
	const ctx = canvas.getContext('2d');
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	ctx.fillStyle = '#ffffff';
	ctx.font = "bold " + _fontSize + "px Arial, sans-serif";

	const player_image = new Image;
	player_image.src = 'https://sokker.org/static/pic/human.svg';
	var tactica = decodificar_tactica('tact' + sufijo);

	// Para cada jugador...
	for (var i = 0; i < 11; i++) {
		var jugador = tactica[i];
		var x = 0;
		var y = 0;
		for (var j = 0; j < 7 * 5; j++) {
			x += jugador[j].charCodeAt(1) - 'A'.charCodeAt(0);
			y += 15 - (jugador[j].charCodeAt(0) - 'A'.charCodeAt(0));
		}

		// Calculo la posición media
		var posX = 21 + 20 * x / 35;
		var posY = 40 + 27 * y / 35;

		// Pinto al jugador y su dorsal
		ctx.drawImage(player_image, posX, posY, 12, 18);
		const _widthTxt = 6 - ctx.measureText(i + 2).width / 2;
		ctx.strokeText(i + 2, posX + _widthTxt, posY - 5);
		ctx.fillText(i + 2, posX + _widthTxt, posY - 5);
	}
}

function cambiar_tactica(sufijo, tact) {
	$("textarea[id='tact" + sufijo + "']").val(tact);
	
	actualizar_tactica(sufijo, tact);
	
	return true;
}

function pasar_click() {
	tactica1 = decodificar_tactica('tact');
	tactica2 = decodificar_tactica('tact_');

	var seleccionados = get_seleccionados('jugador_');

	if (seleccionados.length == 0) {
		alert("Select some players to pass");
	} else {	
		for (var i = 0; i < seleccionados.length; i++) {
			tactica1[seleccionados[i]] = tactica2[seleccionados[i]];
		}
	
		codificar_tactica('', tactica1);
	}
}

function decodificar_tactica(id) {
	var tactica = $("#" + id).val();

	var array = new Array();
	var pos = 0;
	for (var i = 0; i < 11; i++) {
		var jugador = new Array();
		for (var j = 0; j < 7 * 5; j++) {
			jugador.push(tactica.substring(pos, pos + 2));
			pos += 2;
		}
		array.push(jugador);
	}

	return array;
}

function codificar_tactica(sufijo, array) {
	var tactica = '';
	for (var i = 0; i < 11; i++) {
		for (var j = 0; j < 7 * 5; j++) {
			tactica += array[i][j];
		}
	}

	cambiar_tactica(sufijo, tactica);
}

function get_seleccionados(id) {
	var seleccionados = new Array();

	for (var i = 2; i <= 11; i++) {
		if ($("#" + id + i).is(":checked")) {
			seleccionados.push(i - 2);
		}
	}

	return seleccionados;
}

function intercambiar_click(sufijo) {
	tactica = decodificar_tactica('tact' + sufijo);
	var seleccionados = get_seleccionados('jugador' + sufijo);

	if (seleccionados.length != 2) {
		alert("Select two players to swap their positions");
	} else {
		var aux = tactica[seleccionados[0]];
		tactica[seleccionados[0]] = tactica[seleccionados[1]];
		tactica[seleccionados[1]] = aux;
	
		codificar_tactica('' + sufijo, tactica);
	}
}

function mirror_click(sufijo) {
	tactica = decodificar_tactica('tact' + sufijo);
	var seleccionados = get_seleccionados('jugador' + sufijo);

	if (seleccionados.length == 0) {
		alert("Select some players to mirror");
	} else {
		for (var i = 0; i < seleccionados.length; i++) {
			var jugador = tactica[seleccionados[i]];
			var jugador2 = [];
			
			// Hacemos mirror de cada casilla
			for (var j = 0; j < 7 * 5; j++) {
				if (sufijo == '_' || $('#c' + (j % 5) + '' + (parseInt(j / 5))).is(':checked')) {
					var x = 'O'.charCodeAt(0) - (jugador[j].charCodeAt(1) - 'A'.charCodeAt(0));
					var y = jugador[j].charCodeAt(0);
					jugador2[j] = String.fromCharCode(y) + String.fromCharCode(x);
				}
			}

			// Intercambiamos las casillas horizontalmente
			for (var j = 0; j < 7 * 5; j++) {
				if (sufijo == '_' || $('#c' + (j % 5) + '' + (parseInt(j / 5))).is(':checked')) {
					var x = 4 - (j % 5);
					var y = parseInt(j / 5);
					jugador[j] = jugador2[x + y * 5];
				}
			}
		}
		
		codificar_tactica('' + sufijo, tactica);
	}
}

function mover_click(offset_x, offset_y) {
	tactica = decodificar_tactica('tact');
	var seleccionados = get_seleccionados('jugador');

	if (seleccionados.length == 0) {
		alert("Select somes players to move");
	} else {	
		for (var i = 0; i < seleccionados.length; i++) {
			var jugador = tactica[seleccionados[i]];
			for (var j = 0; j < 7 * 5; j++) {
				var seleccionado = $('#c' + (j % 5) + '' + (parseInt(j / 5))).is(':checked');
				var x = jugador[j].charCodeAt(1) + offset_x * seleccionado;
				var y = jugador[j].charCodeAt(0) + offset_y * seleccionado;
				jugador[j] = String.fromCharCode(y) + String.fromCharCode(x);
			}
		}
	
		codificar_tactica('', tactica);
	}
}

function cambiar_editor_click() {
	vista_resumen = !vista_resumen;

	actualizar_tactica('', $("#tact").val());
}

function mostrar_leyendo_tacticas() {
	$(`<div>
			<img style="opacity: 0.8; position: fixed; top: 0; display:block; width:100%; height:100%; object-fit: cover;" src="https://raqueto.com/sokker/img/cargando.gif"/>
			<h1 class="borde" style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -125%); color: white;">Reading tactics...</h1>
		</div>`).appendTo(document.body);
}

function form_submit() {
	if ($("form[name='form_login'] input[name='confirmed']").val() == '1') {
		mostrar_leyendo_tacticas();
		return true;
	} else {
		$("form[name='form_login'] input[type='submit']").attr("disabled", true);
		var data = 'ilogin=' + $("form[name='form_login'] input[name='ilogin']").val() + '&ipassword=' +$("form[name='form_login'] input[name='ipassword']").val();
		$.post('https://sokker.org/start.php?session=xml&' + data, data).done(
	        function(data) {
				$("form[name='form_login'] input[type='submit']").attr("disabled", false);
	        	if (data.startsWith('OK')) {
	        		$("form[name='form_login'] input[name='confirmed']").val('1');
					$("form[name='form_login'] input[type='submit']").click();
	            } else {
	            	alert('Error in login or password');
	            }
	        } 
	    ).fail(function() {
	    	alert('Error: you need to use a browser with CORS restriction disabled. Read the Sokker Asistente FAQ for more details');
	    });

		return false;
	}
}

function carga() {
	$(document).mouseup(function(e) {
	    var container = $(".help_popup");

	    if ($(".help").is(e.target) || $(".help").has(e.target).length > 0) {
	    	container.css("display", "inline-block");
	    } else if (!container.is(e.target) && container.has(e.target).length === 0) {
	    	// if the target of the click isn't the container nor a descendant of the container
	        container.hide();
	    }
	});
}

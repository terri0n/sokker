/* 		function cargar_tactica(id) {
			var editor = $("#tactica");
			editor.html('<embed src="http://sokker.org/UstawienieStats2.swf?id=' + id + '" quality="high" width="300" height="450" name="UstawienieEdycjaPost" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer">');
		}
 */

var editor_swf = 'UstawienieStats2.swf';

function seleccionar_todo_click(id) {
	var checked = $("#" + id).is(":checked");
	$("#" + id).closest(".grupo_checkbox").find("input[type='checkbox']").each(function() {
		$(this).prop("checked", checked);
	});
}

function actualizar_tactica(sufijo, tact) {
	var editor = $("#tactica" + sufijo);
	editor.html('<embed src="http://sokker.org/' + editor_swf + '?'
			+ 'hum_number1=1&hum_minute_in1=0&hum_eff1=50&hum_number2=2&hum_minute_in2=0&hum_eff2=50&hum_number3=3&hum_minute_in3=0&hum_eff3=50&hum_number4=4&hum_minute_in4=0&hum_eff4=50&hum_number5=5&hum_minute_in5=0&hum_eff5=50&hum_number6=6&hum_minute_in6=0&hum_eff6=50&hum_number7=7&hum_minute_in7=0&hum_eff7=50&hum_number8=8&hum_minute_in8=0&hum_eff8=50&hum_number9=9&hum_minute_in9=0&hum_eff9=50&hum_number10=10&hum_minute_in10=0&hum_eff10=50&hum_number11=11&hum_minute_in11=0&hum_eff11=50'
			+ '&tact=' + tact + '" quality="high" width="300" height="450" name="UstawienieEdycjaPost" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer">');
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
		alert("Select somes players to pass");
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
		alert("Select somes players to mirror");
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
	if (editor_swf == 'UstawienieEdycjaPost.swf') {
		editor_swf = 'UstawienieStats2.swf';
	} else {
		editor_swf = 'UstawienieEdycjaPost.swf';
	}

	actualizar_tactica('', $("#tact").val());
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

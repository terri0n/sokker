<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />

const CELL_WIDTH = 47;
const CELL_HEIGHT = 47;
const CASILLAS = 9;

class ChessGoal extends Juego {
	constructor(nombre, url) {
		super(nombre, url);
		this.portero = {};		// { 'blancas', 'negras' }

		this.chess_figures;
		this.chess_figures_9 = [
			{ name: "bK", data: "negras", id: "blackKing", x: 4, y: 1 },
			{ name: "bP", data: "negras", id: "blackPawn1", x: 3, y: 2 },
			{ name: "bP", data: "negras", id: "blackPawn2", x: 4, y: 2 },
			{ name: "bP", data: "negras", id: "blackPawn3", x: 5, y: 2 },
			{ name: "bB", data: "negras", id: "blackBishop1", x: 2, y: 2 },
			{ name: "bB", data: "negras", id: "blackBishop2", x: 6, y: 2 },
			{ name: "bR", data: "negras", id: "blackRook1", x: 0, y: 2 },
			{ name: "bR", data: "negras", id: "blackRook2", x: 8, y: 2 },
			{ name: "bN", data: "negras", id: "blackKnight1", x: 1, y: 2 },
			{ name: "bN", data: "negras", id: "blackKnight2", x: 7, y: 2 },
			{ name: "bQ", data: "negras", id: "blackQueen", x: 4, y: 3 },
			{ name: "wK", data: "blancas", id: "whiteKing", x: 4, y: 9 },
			{ name: "wP", data: "blancas", id: "whitePawn1", x: 3, y: 8 },
			{ name: "wP", data: "blancas", id: "whitePawn2", x: 4, y: 8 },
			{ name: "wP", data: "blancas", id: "whitePawn3", x: 5, y: 8 },
			{ name: "wB", data: "blancas", id: "whiteBishop1", x: 2, y: 8 },
			{ name: "wB", data: "blancas", id: "whiteBishop2", x: 6, y: 8 },
			{ name: "wR", data: "blancas", id: "whiteRook1", x: 0, y: 8 },
			{ name: "wR", data: "blancas", id: "whiteRook2", x: 8, y: 8 },
			{ name: "wN", data: "blancas", id: "whiteKnight1", x: 1, y: 8 },
			{ name: "wN", data: "blancas", id: "whiteKnight2", x: 7, y: 8 },
			{ name: "wQ", data: "blancas", id: "whiteQueen", x: 4, y: 7 },
			{ name: "pelota", data: null, id: "pelota", x: 4, y: 5 }
		];

		this.redes = [
			{ name: "red", data: '', id: "red1", x: 3, y: 0 },
			{ name: "red", data: '', id: "red2", x: 4, y: 0 },
			{ name: "red", data: '', id: "red3", x: 5, y: 0 },
			{ name: "red", data: 'invertida_v', id: "red4", x: 3, y: 10 },
			{ name: "red", data: 'invertida_v', id: "red5", x: 4, y: 10 },
			{ name: "red", data: 'invertida_v', id: "red6", x: 5, y: 10 },
		];
	}
		
	// @Override
	pintar_tablero() {
		// Mostramos el tablero
		$('#juego').removeClass('hidden');
		var board = $("#chess_board");

		// De 0 a 98
		// Necesitamos que la fila de las porterías esté dentro de chess_board para poder depositar la pelota en ellas
		for (var i = 0; i < (CASILLAS * (CASILLAS + 2)); i++) {
			let fila = parseInt(i / CASILLAS);
			let columna = parseInt(i % CASILLAS);

			// Las casillas a los lados de las porterías no tienen fondo
			let clase = 'chess_cell ' + (i % 2 ? 'bg0' : 'bg1');
			if (fila == 0 || fila == CASILLAS + 1) {
				if (columna < 3 || columna > 5) {
					clase += " invisible";
				} else {
					clase += " semitransparente";
				}
			}
		    var html = '<div class="' + clase + '" data-x="' + this.coordenada_x(columna) + '" data-y="' + this.coordenada_y(fila) + '" onmouseover="juego.casilla_over(this)" onmouseout="juego.casilla_out(this)" onclick="juego.casilla_click(this)" ></div>';
		    board.append(html);
		}

		// Esquinas de la red
		$(".chess_cell")[3].id = 'red1';
		$(".chess_cell")[5].id = 'red3';
		$(".chess_cell")[93].id = 'fondo_red4';
		$(".chess_cell")[95].id = 'fondo_red6';
	}

	vaciar_tablero() {
		// Borramos todo menos el tablero
		$("#chess_board_border").children(":not(#chess_board)").remove();
		
		// Borramos todo menos las líneas del campo
		$("#chess_board").find("*:not(object)").remove();

		// Vaciamos el chat
		$('#chat').empty();
		$('#texto').val('');
	}		
	
	// @Override
	pintar_figuras() {
		// Figuras
		// Las tengo que clonar para que las piezas se coloquen en su posición al iniciar un nuevo partido
		this.chess_figures = JSON.parse(JSON.stringify(this.chess_figures_9));
		
		for (let i = 0; i < this.chess_figures.length; i++) {
			if (this.chess_figures[i].name == 'pelota') {
				var figureHTML = '<div class="chess_figure" style="left:' + CELL_WIDTH * this.coordenada_x(this.chess_figures[i].x) + 'px;top:' + CELL_HEIGHT * this.coordenada_y(this.chess_figures[i].y) + 'px" data-figureId="' + this.chess_figures[i].id + '" data-color="' + this.chess_figures[i].data + '"><img src="img/chessgoal/' + this.chess_figures[i].name + '.png" class="pelota" /></div>';
			} else {
				var figureHTML = '<div class="chess_figure" style="left:' + CELL_WIDTH * this.coordenada_x(this.chess_figures[i].x) + 'px;top:' + CELL_HEIGHT * this.coordenada_y(this.chess_figures[i].y) + 'px" data-figureId="' + this.chess_figures[i].id + '" data-color="' + this.chess_figures[i].data + '"><img src="img/chessgoal/' + this.chess_figures[i].name + '.png" class="img" /></div>';
			}
			this.chess_figures[i].figureHTML = $(figureHTML);
			$("#chess_board").append($(this.chess_figures[i].figureHTML));
			
			if (this.chess_figures[i].name == 'bK' || this.chess_figures[i].name == 'wK') {
				this.poner_guantes(this.chess_figures[i]);
			}
		}
		  
		// Redes
		for (let i = 0; i < this.redes.length; i++) {
	  		var figureHTML = '<div style="left:' + CELL_WIDTH * this.redes[i].x + 'px; top:' + CELL_HEIGHT * this.redes[i].y + 'px; position: absolute; pointer-events: none;"><img src="img/chessgoal/' + this.redes[i].name + '.png" class="red img ' + this.redes[i].data + '" id="' + this.redes[i].id + '" /></div>';
	  		$("#chess_board").append(figureHTML);
		}

		// Letras y nºs
		for (let i = 0; i < CASILLAS; i++) {
			let num = this.color == 'blancas' ? CASILLAS - i : i + 1;
			let letra = String.fromCharCode('J'.charCodeAt(0) - num);
			let color = i >= 3 && i<= 5 ? 'black' : 'white';
			$("#chess_board").append(
  				'<div style="opacity: 0.5; left:' + CELL_WIDTH * (i + 0.4) + 'px; top: 24px; color: ' + color + '; position: absolute; pointer-events: none;">' + letra + '</div>'
	  		);
	  		$("#chess_board").append(
  				'<div style="opacity: 0.5; left:' + CELL_WIDTH * (i + 0.4) + 'px; top: ' + (CELL_HEIGHT * 10) + 'px; color: ' + color + '; position: absolute; pointer-events: none;">' + letra + '</div>'
	  		);
	  		$("#chess_board").append(
  				'<div style="opacity: 0.5; left: -18px; top: ' + CELL_HEIGHT * (i + 1.25) + 'px; color: white; position: absolute; pointer-events: none;">' + num + '</div>'
	  		);
	  		$("#chess_board").append(
  				'<div style="opacity: 0.5; left: ' + (CELL_WIDTH * 9 + 10) + 'px; top: ' + CELL_HEIGHT * (i + 1.25) + 'px; color: white; position: absolute; pointer-events: none;">' + num + '</div>'
	  		);
		}
		
		// Rival
		$("#chess_board_border").append(`<div class="reloj negro" style="left: 0px; top: -32px;"><span class="material-icons">timer</span><span id="tiempo_negras"></span> ` + this.mostrar_jugador(this.nombre_rival) + `</div>`);

		// Jugador
		$("#chess_board_border").append(`<div class="reloj negro" style="left: 0px; bottom: -32px;"><span class="material-icons">timer</span><span id="tiempo_blancas"></span> ` + this.mostrar_jugador(this.nombre) + `</div>`);

		// Botones
		$("#chess_board_border").append('<button style="right: 18px; bottom: -30px; display: block; position: absolute;" class="verde" id="finalizar" onclick="juego.finalizar_click()"></button>');
		$("#chess_board_border").append('<button title="Abandonar" style="right: -2px; top: -2px; display: block; position: absolute; font-size: 0.9em; padding: 0;" class="rojo" id="finalizar" onclick="juego.abandonar_click()"><span class="material-icons md-18">close</span></button>');
		$("#chess_board_border").append(`<div style="right: 18px; bottom: -30px; display: block; position: absolute;">
				<button style="display: none;" class="verde" id="boton_otro" onclick="juego.sala.otro_click()"><fmt:message key="menu.playAgain" /></button>
				<button style="display: none;" class="rojo" id="boton_otro_cancelar" onclick="juego.sala.cancelar_click()"><fmt:message key="menu.cancel" /></button>
			</div>`);
	}

	coordenada_x(x) {
		return this.color == 'blancas' ? x : CASILLAS - 1 - x;
	}

	coordenada_y(y) {
		return this.color == 'blancas' ? y : CASILLAS + 1 - y;
	}

	casilla_over(casilla) {
		if ($('.seleccion').length > 0) {
			$(casilla).addClass('sombra');
		}
	}

	casilla_out(casilla) {
		if ($('.seleccion').length > 0) {
			$(casilla).removeClass('sombra');
		}
	}
	
	// La pelota será siempre la última de las figuras
	getPelota() {
		return this.chess_figures[this.chess_figures.length - 1];
	}
	
	getFigure(id) {
		for (var i = 0; i < this.chess_figures.length; ++i) {
			if (this.chess_figures[i].id == id) {
				return this.chess_figures[i];
			};
	    }
	    return;
	}

	getFigureInCell(x, y) {
	    var figura;
	    for (var i = 0; i < this.chess_figures.length; ++i) {
	    	if (x == this.chess_figures[i].x && y == this.chess_figures[i].y) {
	    		// Damos prioridad a las piezas sobre la pelota
	    		if (!figura || !figura.data) {
	    			figura = this.chess_figures[i];
	    		}
	    	};
	    }
	    return figura;
	}

	checkValidMove(figura, newX, newY) {
	    var invalid = false;

	    if (this.turno == null) {
	    	// Sin iniciar
	    	if (!figura.data) {
	    		// La pelota no puede moverse
	    		invalid = true;
	    	} else {
	    		// Comprobamos que las fichas no pasen de su campo
	    		if (figura.data == 'negras' && newY >= (CASILLAS + 1) / 2) {
	    			invalid = true;
	    		} else if (figura.data == 'blancas' && (newY < (CASILLAS + 1) / 2 || newY == (CASILLAS + 1) / 2 && newX != (CASILLAS - 1) / 2)) {
	    			invalid = true;
	    		}
	    	}
	    } else if (this.turno == this.color) {
	    	if (figura.data) {
	    		// Comprobar si es su turno
	        	invalid |= this.turno != figura.data;

	        	// La figura que saca no puede moverse hasta que saque
	        	if (!this.estado < 3 && figura.x == (CASILLAS - 1) / 2 && figura.y == (CASILLAS + 1) / 2 && this.getPelota().x == (CASILLAS - 1) / 2 && this.getPelota().y == (CASILLAS + 1) / 2) {
	        		invalid = true;
	        	}
	    	} else {
	    		// Comprobar fuera de juego
	    		const figura_con_pelota = this.getFigureInCell(figura.x, figura.y);
	    		invalid |= this.comprobar_fuera_de_juego(figura_con_pelota, newY);
	    	}

	    	//check figure can move
		    invalid |= !this.movimiento_correcto(figura, newX, newY);
	    } else {
	    	invalid = true;
	    }
	    
		// Líneas de gol
		invalid |= (newY == 0 || newY == CASILLAS + 1) && (newX < (CASILLAS + 1) / 2 - 2 || newX > (CASILLAS + 1) / 2);
		
	    // Si movemos una pieza sobre otra...
	    var figureInCell = this.getFigureInCell(newX, newY);
	    if (figureInCell && figureInCell.data && figura.data) {
	    	// No permitimos mover una pieza sobre otra del mismo color
		    invalid |= figura.data == figureInCell.data;
		
		    // Ni sobre una figura rival ya movida
		    invalid |= $(figureInCell.figureHTML).find('img:first').hasClass('figura_movida');
		    
		    // Ni sobre una rival, siendo torre, si no se puede empujar
		    invalid |= this.comprobar_empujon(figura, figureInCell);
	    }

	    // Ni sobre si misma (ni siquiera la pelota)
	    invalid |= figura.x == newX && figura.y == newY;
	        
	    return invalid;
	}

	comprobar_empujon(torre, rival) {
		if (torre.name == 'bR' || torre.name == 'wR') {
			// Las torres empujan al rival hacia atrás
			const newX = rival.x * 2 - torre.x;
			const newY = rival.y * 2 - torre.y;

			// Comprobamos que sea dentro del tablero
			if (newX >= 0 && newX <= CASILLAS && newY >= 0 && newY <= CASILLAS + 2) {
				if (!$($('.chess_cell')[newX + newY * CASILLAS]).hasClass('invisible')) {
					// Y que la casilla esté vacía
					if (!this.getFigureInCell(newX, newY)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
	
	comprobar_fuera_de_juego(figura, newY) {
		// 1. Comprobamos si se pasa la pelota detrás de la defensa
		var defensas_detras = 0;
		var ys = [];
	    for (var i = 0; i < this.chess_figures.length; ++i) {
	    	if (this.chess_figures[i].data == this.cambiar_color(this.color)) {
	    		ys.push(this.chess_figures[i].y);
	    		if (this.color == 'blancas' && this.chess_figures[i].y <= newY
	    				|| this.color == 'negras' && this.chess_figures[i].y >= newY) {
	    			defensas_detras++;
	    		} 
	    	}
	    }

	    if (defensas_detras <= 1) {
	    	// 2. Vemos si hay algún jugador en fuera de juego
	        ys.sort();
	        // El límite será la menor altura entre la posición del 2º jugador rival y la altura de la pelota
	        const limite = this.color == 'blancas' ? Math.min(ys[1], figura.y) : Math.max(ys[ys.length - 2], figura.y);

	        for (var i = 0; i < this.chess_figures.length; i++) {
	        	if (this.chess_figures[i].id != figura.id
	        			&& this.chess_figures[i].data == this.color
	        			&& (this.color == 'blancas' && this.chess_figures[i].y < limite
	        			|| this.color == 'negras' && this.chess_figures[i].y > limite)) {
	        		return true;
	        	}
	    	}
	    }
	    
	    return false;
	}

	piezas_entre(figura, x, y) {
		const incrX = Math.sign(figura.x - x);//4-2
		const incrY = Math.sign(figura.y - y);//5-7

		x += incrX;
		y += incrY;

		while (figura.x != x || figura.y != y) {
			if (this.getFigureInCell(x, y)) {
				return true;
			}
			x += incrX;
			y += incrY;
		}
		return false;
	}

	movimiento_correcto(figura, newX, newY) {
		if (figura.name == 'pelota') {
			// La pelota solo se puede mover si la tiene una pieza del jugador que tiene el turno
			const figura_con_pelota = this.getFigureInCell(figura.x, figura.y);

			if (!figura_con_pelota || figura_con_pelota.data != this.turno) {
				return false;	
			} else {
				switch (figura_con_pelota.name) {
			        case 'bK':
			        case 'wK':
			    		// Si la diferencia en valor absoluto de la casilla destino y la origen es mayor que 1 -> error
			    		if (Math.abs(newX - figura_con_pelota.x) > 1 || Math.abs(newY - figura_con_pelota.y) > 1) {
			    			return false;
			    		} else {
			    			return true;
			    		}
			        case "wB":
			        case "bB":
			        	// Si no está en la diagonal
			        	if (Math.abs(figura_con_pelota.x - newX) != Math.abs(figura_con_pelota.y - newY)) {
			        		return false;
			        	}
			        	if (this.piezas_entre(figura_con_pelota, newX, newY)) {
			        		return false;
			        	} else {
			        		return true;
			        	}
			        case 'bR':
			        case 'wR':
			        	// Si no está en la vertical u horizontal
			        	if (newX != figura_con_pelota.x && newY != figura_con_pelota.y) {
			        		return false;
			        	}
			        	if (this.piezas_entre(figura_con_pelota, newX, newY)) {
			        		return false;
			        	} else {
			        		return true;
			        	}
			        case 'bQ':
			        case 'wQ':
			        	// Si está en la diagonal
			        	if (Math.abs(figura_con_pelota.x - newX) == Math.abs(figura_con_pelota.y - newY)
			        			// Si está en la vertical u horizontal
			        			|| (newX == figura_con_pelota.x || newY == figura_con_pelota.y)) {
				        	if (this.piezas_entre(figura_con_pelota, newX, newY)) {
				        		return false;
				        	} else {
				        		return true;
				        	}
						}
			        case 'bP':
			        case 'wP':
			        	// Las blancas mueven hacia arriba y las negras hacia abajo
			        	if (newY == figura_con_pelota.y + (this.color == 'blancas' ? -1 : 1) && Math.abs(newX - figura_con_pelota.x) <= 1) {
			        		return true;
			        	} else {
			        		return false;
			        	}
			        case 'bN':
			        case 'wN':
			        	const difY = Math.abs(newY - figura_con_pelota.y);
			        	const difX = Math.abs(newX - figura_con_pelota.x);

			        	if (difY == 2 && difX == 1 || difY == 1 && difX == 2) {
			        		return true;
			        	} else {
			        		return false;
			        	}
				}
			}
		} else {
			// Si la diferencia en valor absoluto de la casilla destino y la origen es mayor que 1 -> error
			if (Math.abs(newX - figura.x) > 1 || Math.abs(newY - figura.y) > 1) {
				return false;
			} else {
	        	return true;
			}
		}
	}

	limpiar_tablero() {
		// Limpio el tablero de figuras movidas, seleccionadas, sombras...
		$('.chess_figure img').removeClass('figura_movida');
		$('.chess_figure img').removeClass('figura_rival_movida');
		$('.chess_figure img').removeClass('seleccion');
		$('.chess_figure').removeClass('pelota_movida');
		$('.chess_cell').removeClass('sombra');
	}

	habilitar_color(habilitar) {
		// Habilito las fichas del color de turno
		// Pero solo si el jugador no es un observador (color != null)
		for (var i = 0; i < this.chess_figures.length; ++i) {
			$(this.chess_figures[i].figureHTML).draggable(habilitar && this.color && (this.chess_figures[i].data == null || this.chess_figures[i].data == this.color) ? "enable" : "disable");
		}
	}

	mover(figura, newX, newY) {
		$('.chess_figure img').removeClass('seleccion');
		$('.chess_cell').removeClass('sombra');

        if (juego.checkValidMove(figura, newX, newY)) {
  		  	return false;
        } else {
        	// ¿Parada del portero?
        	const figureInCell = juego.getFigureInCell(newX, newY);
        	const parada = juego.comprobar_parada(figura, newX, newY, juego.portero[juego.cambiar_color(juego.color)]);
        	
        	if (parada) {
        		newX = parada.x;
        		newY = parada.y;
        		juego.hacer_parada(newX, juego.cambiar_color(juego.color), 250);
        	}

        	juego.movimientos.push({id: figura.id, x: newX, y: newY });
         	
        	juego.hacer_movimiento(figura, newX, newY, 250, () => {
        		if (juego.comprobar_gol(figura.name, newY)) {
        			juego.finalizar_click();
        		}
        	});

            // Si la partida ya ha comenzado...
            if (juego.turno) {
            	$(figura.figureHTML).find('img:first').removeClass("figura_rival_movida");
            	
            	if (figura.data || $(figura.figureHTML).hasClass("pelota_movida")) {
            		// Las piezas solo se pueden mover una vez por jugada
	            	$(figura.figureHTML).removeClass("pelota_movida");
            		$(figura.figureHTML).find('img:first').addClass("figura_movida");
            		$(figura.figureHTML).draggable("disable");
            		
            		if (figureInCell && figureInCell.data) {
            			// Solo dejamos que se muevan una vez las figuras rivales
                    	$(figureInCell.figureHTML).find('img:first').removeClass("figura_rival_movida");
                		$(figureInCell.figureHTML).find('img:first').addClass("figura_movida");
            		}
            	} else {
            		// Excepto la pelota
            		$(figura.figureHTML).addClass("pelota_movida");
            	}
            	
            	if (!figura.data) {
            		// Si movemos la pelota, deshabilitamos a la figura que la recibe
            		const figura_con_pelota = juego.getFigureInCell(figura.x, figura.y);
        			if (figura_con_pelota.data == juego.color) {
	            		$(figura_con_pelota.figureHTML).find('img:first').addClass("figura_movida");
	            		$(figura_con_pelota.figureHTML).draggable("disable");
        			}
            	}
            }
            return true;
        }
	}
	
	mover_figura(figura, x, y, duration, callback, ignorar_tamanyo) {
		const figura_destino = this.getFigureInCell(x, y);
		const pelota = this.getPelota();
		const tiene_pelota = pelota.x == figura.x && pelota.y == figura.y;
		
		figura.x = x;
		figura.y = y;

		if (!ignorar_tamanyo) {
			if (figura.name == 'pelota') {
				$(figura.figureHTML).find('img').delay(duration).animate({
					"width" : figura_destino ? "20px" : "50px"
				}, {
					duration: duration,
					queue: false
				});
				$(figura.figureHTML).animate({
					"margin-top": figura_destino ? "28px" : "0"
				}, {
					duration: 100,
					queue: false
				});
			} else if (figura_destino && figura_destino.name == 'pelota') {
				$(figura_destino.figureHTML).find('img').delay(duration).animate({
					"width" : "20px"
				}, {
					duration: duration,
					queue: false
				});
				$(figura_destino.figureHTML).animate({
					"margin-top": "28px"
				}, {
					duration: 100,
					queue: false
				});
			} else if (this.turno == null && tiene_pelota) {
				// La partida no ha empezado, se coloca una figura en el centro y luego se quita
				$(pelota.figureHTML).find('img').delay(duration).animate({
					"width" : "50px"
				}, {
					duration: duration,
					queue: false
				});
				$(pelota.figureHTML).animate({
					"margin-top": "0"
				}, {
					duration: 100,
					queue: false
				});
			}
		}

		figura.figureHTML.animate({
			top: this.coordenada_y(y) * CELL_HEIGHT,
			left: this.coordenada_x(x) * CELL_WIDTH
		}, duration, null, callback);
	}
		
	hacer_movimiento(figura, x, y, duration, callback) {
		// Si hay una figura en el destino y ninguna de las dos es la pelota...
		var figureInCell = this.getFigureInCell(x, y);
		if (figura.data && figureInCell && figureInCell.data) {
			// Si es una torre...
			if (figura.name == 'bR' || figura.name == 'wR') {
				// La empujamos
				const newX = figureInCell.x * 2 - figura.x;
				const newY = figureInCell.y * 2 - figura.y;
	    		setTimeout(() => {
	    			this.mover_figura(figureInCell, newX, newY, duration, null, false);
	    		}, duration / 2);	
			} else {
				// Si no, las intercambiamos
				this.mover_figura(figureInCell, figura.x, figura.y, duration, null, false);
			}
		}
		
		// Si no es la pelota
		if (figura.data) {
			// Si el juego ha comenzado y la figura tiene la pelota, la movemos con ella
			const pelota = this.getPelota();
			if (this.turno && pelota.x == figura.x && pelota.y == figura.y) {
				this.mover_figura(pelota, x, y, duration, null, true);
			}
		}
		
		this.mover_figura(figura, x, y, duration, callback, false);
	}

	// Paso los valores en vez de la figura porque esta función puede haberse llamado cuando figura.y haya cambiado de valor
	comprobar_gol(nombre_figura, newY) {
		// Comprobamos si se ha marcado gol
		if (nombre_figura == 'pelota' && (newY == 0 || newY == CASILLAS + 1)) {
			const victoria = newY == 0 ^ this.color == 'negras';
			setTimeout(() => {
				showToast.show("<fmt:message key="msg.goal" />");
				this.sala.fin_partido(victoria);
			}, 500);
			return true;
		} else {
			return false;
		}
	}

	empezar(c, duracion) {
		super.empezar(c, duracion);
		 
		this.duracion = duracion;
		this.tiempo['blancas'] = duracion * 60;
		this.tiempo['negras'] = duracion * 60;
		this.estado = 0;	// sin iniciar

		// Establecemos el color
		this.color = c;

		// Pintamos el tablero
		this.pintar_ui();
		this.actualizar_turno(null, false);
		this.pintar_tiempos();

		this.escribir_servidor('<fmt:message key="msg.chooseTactic" />');
		showToast.show('<fmt:message key="msg.chooseTactic" />');

		// Actualizamos nombres
//		this.sala.actualizar_nombres(c == 'blancas' ? this.nombre : this.nombre_rival, c == 'blancas' ? this.nombre_rival : this.nombre);
		
		$('#boton_nuevo').prop('disabled', false);
		$('#boton_nuevo').hide();
		
	    // Ya podemos ir haciendo la táctica
		this.habilitar_color(true);
	}

	// Lo separo para que el observador pueda ver el tablero con las piezas sin que le afecte lo demás
	pintar_ui() {
		this.vaciar_tablero();
		this.pintar_tablero();
		this.pintar_figuras();
		
		$('.chess_figure').draggable({
			containment: "parent",
			zIndex: 1,
			revert: true
		});

		$('.chess_figure').click(function() {
			juego.figura_click(this);
		});

	    $('.chess_cell').droppable({
	    	accept: ".chess_figure",
	    	tolerance: 'intersect',
	    	classes: { "ui-droppable-hover" : "sombra" },
	    	drop: function(event, ui) {
	    		// Utilizo "juego" en lugar de "this" porque "this" tiene que apuntar a la figura que se ha movido
		        var figura = juego.getFigure($(ui.draggable).attr('data-figureId'));
		        var newX = parseInt($(this).attr('data-x'));
		        var newY = parseInt($(this).attr('data-y'));
	
		        if (juego.mover(figura, newX, newY)) {
		        	return $(ui.draggable).draggable('option', 'revert', false);
		        } else {
		        	return $(ui.draggable).draggable('option', 'revert', true);
		        }
	    	}
	    });
	}

	manda_tactica(movs) {
		this.estado = 2;
		if (this.color == 'blancas') {
			this.limpiar_tablero();
		}
		this.actualizar_turno('blancas', true);
		this.iniciar_cronometros();
		this.escribir_servidor('<fmt:message key="msg.gameStarts" />');
		
		this.pintar_movimientos(movs, 0, 1000, () => {
			this.habilitar_color(this.color == 'blancas');
		});
	}

	getMovimientos() {
		return JSON.stringify(this.movimientos);
	}

	pintar_movimientos(movs, i, duracion, funcion) {
		if (movs.length > i) {
			var figura = this.getFigure(movs[i].id);

	    	// ¿Parada del portero?
	    	const parada = this.comprobar_parada(figura, movs[i].x, movs[i].y, this.portero[this.cambiar_color(this.turno)]);
	    	if (parada) {
	    		movs[i].x = parada.x;
	    		movs[i].y = parada.y;
	    		setTimeout(() => {
	    			this.hacer_parada(movs[i].x, this.color, duracion / 4);
	    		}, duracion * 3 / 4);
	    	}

			// Pintamos la figura
			//$($('.chess_cell')[this.coordenada_x(figura.x) + this.coordenada_y(figura.y) * 9]).addClass("sombra");
			$(figura.figureHTML).find('img:first').addClass('figura_rival_movida');

			// Llamada recursiva hasta que se acaben los movimientos
			this.hacer_movimiento(figura, movs[i].x, movs[i].y, duracion, () => {
				this.pintar_movimientos(movs, i + 1, duracion, funcion);
				if (i + 1 == movs.length) {
					this.comprobar_gol(figura.name, movs[i].y);
				}
			});
		} else {
			if (funcion) {
				funcion();
			}
		}
	}

	movimiento_rival(tiempo_blancas, tiempo_negras, movs) {
		this.tiempo['blancas'] = tiempo_blancas;
		this.tiempo['negras'] = tiempo_negras;

		this.limpiar_tablero();
	
		this.pintar_movimientos(movs, 0, 1000, () => {
			if (!this.nombre_rival) {
				// Los observadores no tienen color pero sí tienen turno
				this.actualizar_turno(this.cambiar_color(this.turno), false);
			} else {
				this.actualizar_turno(this.color, true);
				this.habilitar_color(true);
			}
		});
	}

	poner_guantes(quien) {
		this.portero[quien.data] = quien;
		quien.figureHTML.append('<img class="guante" style="position: relative; left: -60px;" src="img/chessgoal/guante.png" />');
		quien.figureHTML.append('<img class="guante invertida_h" style="position: relative; left: -30px;" src="img/chessgoal/guante.png" />');
	}

	// Comprueba si la pelota ha entrado en la portería, y de ser así, si el portero la ha podido parar
	// En caso de parada, devuelve las coordenadas donde ha ocurrido
	// NOTA: tb consideramos parada la pelota si cae al lado del portero. De esta forma evitamos mandar al servidor el movimiento de que la pelota ha entrado en la portería
	comprobar_parada(figura, newX, newY, port) {
		// Parada del portero?
		if (figura.name == 'pelota') {
			if (newY == 0 || newY == CASILLAS + 1) {
				// Posición de la pelota antes de entrar en la portería
				var x = newX - Math.sign(newX - figura.x);
				var y = newY - Math.sign(newY - figura.y);
			} else if (newY == 1 || newY == CASILLAS) {
				var x = newX;
				var y = newY
			} else {
				return null;
			}

			// Si el que remata es un caballo, es imparable
			const figura_con_pelota = this.getFigureInCell(figura.x, figura.y);
			if (figura_con_pelota.name != 'bN' && figura_con_pelota.name != 'wN') {
				// Si no hay nadie en la celda...
				if (!this.getFigureInCell(x, y)) {
					// y el portero está cerca...
					if (Math.abs(x - port.x) == 1 && y == port.y) {
						return { 'x' : x, 'y' : y };
					}
				}
			}
		}
		
		return null;
	}

	encoger_guantes(duracion) {
		$('.chess_figure').find('.guante:first').animate({width: "20px", left: '-60px'}, duracion);
		$('.chess_figure').find('.guante:last').animate({width: "20px", left: '-30px'}, duracion);
	}

	hacer_parada(newX, quien, duracion) {
		if (newX < this.portero[quien].x ^ this.color == 'negras') {
			this.portero[quien].figureHTML.find('.guante:first').animate({width: "50px", left: "-90px"}, duracion);
			this.portero[quien].figureHTML.find('.guante:last').animate({left: "-60px"}, duracion);
		} else {
			this.portero[quien].figureHTML.find('.guante:last').animate({width: "50px", left: "-30px"}, duracion);
		}

		setTimeout(() => {
			this.encoger_guantes(duracion);
		}, duracion * 2);
	}

	actualizar_texto_turno() {
		// Solo si no soy observador
		if (this.nombre_rival) {
			switch (this.estado) {
				case 0:	// Sin iniciar
					$('#finalizar').text('<fmt:message key="msg.sendTactic" />');
					$('#finalizar').prop('disabled', false);
					break;
				case 1: // Táctica mandada
					$('#finalizar').text('<fmt:message key="msg.waitingTactic" />');
					$('#finalizar').prop('disabled', true);
					break;
				case 2:	// Esperando saque inicial
				case 3: // partido iniciado, turno blancas
				case 4: // partido iniciado, turno negras
					if (this.turno == this.color) {
						$('#finalizar').text('<fmt:message key="msg.endTurn" />');
						$('#finalizar').prop('disabled', false);
					} else {
						$('#finalizar').text('<fmt:message key="msg.opponentTurn" />');
						$('#finalizar').prop('disabled', true);
					}
					break;
				default:
					$('#finalizar').hide();
					$('#boton_otro').show();
					$('#boton_otro_cancelar').show();
			}
		}	
	}

	////////////
	// EVENTOS
	////////////

	figura_click(figura) {
		const seleccion = juego.getFigure($('.seleccion').closest('.chess_figure').attr('data-figureid'));
		const destino = juego.getFigure($(figura).attr('data-figureid'));

		if (seleccion) {
			// Si ya hay una figura seleccionada
			this.mover(seleccion, parseInt(destino.x), parseInt(destino.y));
		} else {
			// Si no, si es nuestra, la seleccionamos
			if (!$(figura).draggable("option", "disabled")) {
				$(figura).find('img:first').addClass('seleccion');
			}
		}
	}

	casilla_click(casilla) {
		const seleccion = juego.getFigure($('.seleccion').closest('.chess_figure').attr('data-figureid'));
		if (seleccion) {
			this.mover(seleccion, parseInt($(casilla).attr('data-x')), parseInt($(casilla).attr('data-y')));
		}
	}
	
	finalizar_click() {
		switch (this.estado) {
			case 0:	// sin iniciar
				if (this.color == 'blancas') {
					if (!this.getFigureInCell((CASILLAS - 1) / 2, (CASILLAS + 1) / 2).data) {
						showToast.show('<fmt:message key="msg.placeCentralPlayer" />');
						return;
					}
				}
 
				// Mando táctica
				this.sala.conexion.enviar('mandar_tactica', this.getMovimientos());
				this.movimientos = [];	// Vacío los movimientos
				this.estado = 1;
				break;
			// 1: esperando táctica del rival
			case 2: // saque inicial
				if (this.getPelota().x == (CASILLAS - 1) / 2 && this.getPelota().y == (CASILLAS + 1) / 2 && !this.getFigureInCell((CASILLAS - 1) / 2, (CASILLAS + 1) / 2).figureHTML.find('img:first').hasClass("figura_movida")) {
					showToast.show('<fmt:message key="msg.moveTheBall" />');
					return;
				} else {
					this.estado = 4;
				}
				break;
			case 3:	// partido iniciado, turno blancas
				this.estado = 4;
				break;
			case 4:	// partido iniciado, turno negras
				this.estado = 3;
				break;
		}
		
		if (this.turno) {
			this.actualizar_turno(this.cambiar_color(this.turno), true);
			this.sala.conexion.enviar('fin_turno', this.getMovimientos());
			this.movimientos = [];	// Vacío los movimientos
		} else {
			this.actualizar_texto_turno();
		}

		this.limpiar_tablero();
		this.habilitar_color(false);
	}

	mostrar_reglas_click() {
		const box = bootbox.alert({ 
		    title: '<span class="material-icons">help</span> <fmt:message key="rules.title" />',
		    locale: '${pageContext.request.locale.language}',
		    scrollable: true,
			backdrop: true,
			closeButton: false,
		    message: '<ol style="padding-left: 25px;">\
				      	<li><fmt:message key="rules.rule1" /></li>\
				    	<li><fmt:message key="rules.rule2" /></li>\
				    	<li><fmt:message key="rules.rule3" /></li>\
				    	<li><fmt:message key="rules.rule4" /></li>\
				    	<li><fmt:message key="rules.rule5" /></li>\
				    	<li><fmt:message key="rules.rule6" /></li>\
				    	<li><fmt:message key="rules.rule7" /></li>\
				    	<li><fmt:message key="rules.rule8" /></li>\
				    	<li><fmt:message key="rules.rule9" /></li>\
				    	<li><fmt:message key="rules.rule10" /></li>\
		      		</ol>',
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
}

<%@page import="com.formulamanager.multijuegos.websockets.EndpointBase"%>
<%@page import="com.formulamanager.multijuegos.listeners.SessionListener"%>
<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/inc/head.jspf" %>

    <title>MultiJuegos</title>
	<link rel="icon" href="img/multijuegos.ico" type="image/x-icon">

    <style type="text/css">
		@media only screen and (max-width: 800px) {
		    h1 img {
				width: 13%;
			}
		}
		@media only screen and (min-width: 801px) {
		    h1 img {
				width: 10%; 
			}
		}
		body {
		    background: repeating-linear-gradient(
		        45deg,
		        #557 0,
		        #779 10px,
		        #668 10px,
		        #668 50px
		    ) 0 40px;
		    background-size: 78px 78px;
		}
		h1 {
		    background: linear-gradient(to bottom, rgba(31,31,63,0.3) 0%, rgba(31, 31, 63, 0.3) 80%, rgba(31, 31, 63, 0) 100%);
		    padding: 20px 20px 40px 20px;
		}
        .juego {
            padding: 5px;
            margin-top: 5px;
            margin-left: 30px;
            margin-right: 30px;
            margin-bottom: 10px;
            background-color: #fafafa;
            display: inline-block;
            border: 2px outset;
            cursor: pointer;
            text-align: center;
            font-size: 25px;
			border-radius: 15px;
            transition: all 0.5s ease;
        	line-height: 0.7;
        }
        .juego > div {
 			height: 120px;
        }
        .juego small {
        	display: inline-block;
        	font-size: 50%;
        	opacity: 0.8;
        	margin-bottom: 3px;
        }
        .juego b {
        	display: inline-block;
        	margin-top: 10px;
        }
        .juego:hover {
            box-shadow: 0 0 20px 5px rgba(255, 255, 255, 0.8); /* Sombra que simula emisión de luz */
            filter: brightness(1.5);
			transform: scale(1.2);
        }
        .juego img {
			position: relative;
		    top: 50%;
		    transform: translate(0%, -50%);
		}

		/* FIGURAS */

        .game-figures {
            opacity: 1;
            position: fixed;
            width: 100%;
            height: 100%;
            pointer-events: none;
			cursor: pointer;
            z-index: -1;
        }

        .game-figure {
            position: absolute;
        }

		/* NAIPE */

		.naipe {
		    height: 81px;
		    font-size: 5em;
			@media (max-width: 600px) {
			    height: 71px;
			    padding-top: 9px;
			    font-size: 5.5em;
			}
		    width: 54px;
		    background-color: white;
		    font-family: sans-serif;
			line-height: 1;
			overflow: hidden;
		    display: flex;
		    justify-content: center;
		    align-items: center;
		}
		
		/* DADO */
		
        .dice {
            border-radius: 10%;
            border: 1px solid black;
            position: relative;
            width: 60px;
            height: 60px;
            margin: 0 auto;
            transform: translateY(0);
        }

        .dot {
            position: absolute;
            width: 9px;
            height: 9px;
            border-radius: 50%;
            background-color: white;
        }

        /* Clases para las posiciones de los puntos en el dado */
        .center-dot { top: 50%; left: 50%; transform: translate(-50%, -50%); }
        .upper-left-dot { top: 12%; left: 12%; }
        .lower-right-dot { bottom: 12%; right: 12%; }
        .upper-right-dot { top: 12%; right: 12%; }
        .lower-left-dot { bottom: 12%; left: 12%; }
        .left-center-dot { top: 50%; left: 20%; transform: translate(-50%, -50%); }
        .right-center-dot { top: 50%; left: 80%; transform: translate(-50%, -50%); }
        
        /* PEÓN */

		.chess {
		    display: flex;
		    align-items: center;
		    justify-content: center;
		    font-size: 80px;
            font-family: arial;	/* Es necesario cambiar la fuente por defecto para que se vea bien el peón negro */
		}
    </style>
</head>
<body translate="no" style="text-align: center;">
    <%@ include file="/inc/cabecera.jspf" %>

	<h1>
	    <span class="texto rojo letra_impar">M</span
	    ><span class="texto naranja letra_par">U</span
	    ><span class="texto amarillo letra_impar">L</span
	    ><span class="texto verde letra_par">T</span
	    ><span class="texto azul letra_impar">I</span
	    ><img width="130" style="margin-left: 8px;" src="${pageContext.request.contextPath}/img/multijuegos.png"
	    /><span class="texto anyil letra_par">J</span
	    ><span class="texto violeta letra_impar">U</span
	    ><span class="texto rojo letra_par">E</span
	    ><span class="texto naranja letra_impar">G</span
	    ><span class="texto amarillo letra_par">O</span
	    ><span class="texto verde letra_impar">S</span>
	</h1>
    
    <section>
    	<h2>JUEGOS DE MESA</h2>

	    <div class="juego verde_oscuro sombra" style="border-color: #008040;" onclick="location.href='chessgoal'">
	        <div>
		        <img width=120 src="${pageContext.request.contextPath}/img/chessgoal/chessgoal.png" /><br />
		    </div>
	        <b>ChessGoal</b><br />
	        <small>(<%= EndpointBase.getSesiones() %>)</small>
	    </div>
	
	    <div class="juego marron sombra" style="border-color: #88362c;" onclick="location.href='multicatan'">
	        <div>
		        <img width=120 src="${pageContext.request.contextPath}/img/multicatan/multicatan.png" /><br />
		    </div>
	        <b>MultiCatan</b><br />
	        <small>(En construcción)</small>
	    </div>
    </section>

    <div class="game-figures"></div>

    <script>
	    $(document).on('visibilitychange', function() {
            if (document.hidden) {
                // La pestaña perdió el foco, pausar las animaciones
                pauseAnimations();
            } else {
                // La pestaña recuperó el foco, reanudar las animaciones
                resumeAnimations();
            }
        });

        function pauseAnimations() {
            $('.game-figure').stop();
        }

        function resumeAnimations() {
            $('.game-figure').each(function() {
                animateFigure($(this));
            });
        }

        // Si es la generación inicial, calculamos una altura aleatoria para que no aparezcan todas a la vez
        function setRandomAttributes(figure, inicial) {
            const windowHeight = window.innerHeight;
            const windowWidth = window.innerWidth;
            var random = Math.random();
            
            figure.css({
            	opacity: inicial ? random + 1 : 1,
                left: Math.random() * windowWidth + 'px',
                top: (inicial ? random * windowHeight - windowHeight :  -40) + 'px'
            });
        }

        function getRandomColor() {
            const hue = Math.floor(Math.random() * 360);
            const saturation = 100;
            const lightness = Math.random() * 80;
            return 'hsl(' + hue + ', ' + saturation + '%, ' + lightness + '%)';
        }

        function animateFigure(figure) {
        	// Obtener la rotación inicial aleatoria
            var initialRotation = Math.random() * 360;
        	
            figure.animate({
                top: '90%',
                left: Math.random() * window.innerWidth + 'px',
                opacity: 0,
            }, {
                duration: (Math.random() * 10000) + 10000,
                easing: 'linear',
                step: function (now, fx) {
                    // Girar el dado durante la animación relativo a la rotación inicial
                    if (fx.prop === 'top') {
                        var relativeRotation = initialRotation + (now / $(window).height()) * 1800;
                        figure.rotate(relativeRotation);
                    }
                },
                complete: function () {
                    // Resetear la posición y reiniciar la animación
                    //setRandomAttributes(figure);
                    //animateFigure(figure);
                    figure.remove();
                    createFigure(false);
                }
            });
		}
        
        function createFigure(inicial) {
            var figure = $('<div class="game-figure"></div>');
            var random = Math.floor(Math.random() * 3);
            switch (random) {
            	case 0:
            		createDice(figure);
            		break;
            	case 1:
            		createChess(figure);
            		break;
            	case 2:
            		createCard(figure);
            		break;
            }
            setRandomAttributes(figure, inicial);
            animateFigure(figure);
            $('.game-figures').append(figure);
        }

        /* FIGURAS */

        const palos = [ 'hearts', 'diamonds', 'clubs', 'spades' ];
  
		function createCard(figure) {
			var suit = palos[Math.floor(Math.random() * 4)];
        	
        	// Define el color del palo basado en el tipo
		    var suitColor = (suit === 'hearts' || suit === 'diamonds') ? '#ff0000' : '#000';
		
		    var card = $('<div>').addClass('naipe').css('color', suitColor).html('&#' + getSuitSymbol(suit) + ';');

		    figure.append(card);
		}
		
		// Función auxiliar para obtener el offset de símbolo del palo
		function getSuitSymbol(suit) {
	    	let rnd = Math.floor(Math.random() * 13);
	    	if (rnd > 10) rnd++;	// Nos saltamos la C

	    	switch (suit) {
		        case 'hearts':
		            return 127153 + rnd;
		        case 'diamonds':
		            return 127169 + rnd;
		        case 'clubs':
		            return 127185 + rnd;
		        case 'spades':
		            return 127137 + rnd;
		    }
		}
        
		function createChess(figure) {
			var color = Math.random() < 0.5 ? 'black' : 'white';
			var bgcolor = color == 'black' ? 'white' : 'black';

			<%-- Piezas blancas: 9817-9822 --%>
			<%-- Piezas negras: 9823-9828 --%>
			var pieza = 9818 + Math.floor(Math.random() * 6);
        	
        	// Define el color del palo basado en el tipo
		    var chess = $('<div>').addClass('chess ' + (color == 'white' ? 'borde-negro' : 'borde-blanco')).css('color', color).html('&#' + pieza + ';');

		    figure.append(chess);
		}

        function createDice(figure) {
            var dice = $('<div class="dice"></div>');
            dice.css({
        		backgroundColor: getRandomColor(),
            });
            var dotCount = Math.floor(Math.random() * 6) + 1;

            if (dotCount % 2 === 1) {
                createDot(dice, 'center-dot');
            }

            if (dotCount > 1) {
                createDot(dice, 'upper-left-dot');
                createDot(dice, 'lower-right-dot');
            }

            if (dotCount > 3) {
                createDot(dice, 'upper-right-dot');
                createDot(dice, 'lower-left-dot');
            }

            if (dotCount == 6) {
                createDot(dice, 'right-center-dot');
                createDot(dice, 'left-center-dot');
            }

            figure.append(dice);
        }

        function createDot(parent, dotClass) {
            var dot = $('<div class="dot ' + dotClass + '"></div>');
            parent.append(dot);
        }
        
	    // Función para agregar la rotación a un elemento jQuery
        $.fn.rotate = function (degrees) {
            $(this).css({
                '-webkit-transform': 'rotate(' + degrees + 'deg)',
                '-moz-transform': 'rotate(' + degrees + 'deg)',
                '-ms-transform': 'rotate(' + degrees + 'deg)',
                'transform': 'rotate(' + degrees + 'deg)'
            });
            return $(this);
        };
        
        $(function() {
            for (var i = 0; i < 10; i++) {
            	createFigure(true);
            }
		});
	</script>

    <%@ include file="/inc/pie.jspf" %>
</body>
</html>

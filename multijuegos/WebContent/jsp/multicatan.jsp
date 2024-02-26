<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="xtag" %>

<%--
<c:if test="${empty sessionScope['javax.servlet.jsp.jstl.fmt.locale.session']}">
	<fmt:setLocale value="${pageContext.request.locale.language}" scope="session" />
</c:if>

 <fmt:setBundle basename="com.formulamanager.chessgoal.idiomas.ApplicationResources" />
--%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=0.64" />

	<script src='https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js'></script>
	<script src='https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js'></script>
	<script src='https://cdnjs.cloudflare.com/ajax/libs/jqueryui-touch-punch/0.2.3/jquery.ui.touch-punch.min.js'></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>
	<script src="https://kit.fontawesome.com/6b4c099088.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/axios/0.19.2/axios.js"></script>
	<script src="https://unpkg.com/konva@7.0.3/konva.min.js"></script>
<!-- 	<script src="js/bootbox.all.min.js"></script>
	<script src="js/util.js"></script>
	<script src="js/conexion.js.jsp"></script>
	<script src="js/sala.js.jsp"></script>
	<script src="js/juego.js.jsp"></script>
	<script src="js/showToast.js"></script>
 -->	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
	<link rel="stylesheet" href="css/showToast.css">
	<link rel="stylesheet" href="css/estilo.css">

	<title>MultiCatan</title>
</head>

<body translate="no" style="background-color: #a72c24; overflow: hidden;">
 	<nav class="navbar justify-content-end small bg-secondary text-light p-0" id="navbar">
<%-- 		<div id="desconectado">
			<fmt:message key="connection.disconnected" />
		</div>
		<div id="conectado" style="display: none">
			<fmt:message key="connection.hello" /> <span id="nombre"><c:out value="${nombre}" /></span> 
			<button class="btn btn-danger btn-sm rounded pt-0 pb-0" title="<fmt:message key="connection.disconnect" />" onclick="juego.sala.conexion.desconectar_click()"><i class="fas fa-times"></i></button>
		</div>
 --%>	</nav>

 	<section>
		<div id="container" style="display: inline-block;"></div>
		
<%--  		<xtag:chat nombre="MultiCatan" imagen="multicatan/multicatan.png" /> --%>
	</section>
	<script>
	    const imagenes = [];
	    const agua = new Image();
	    agua.onload = pintar_tablero;
	    agua.src='img/multicatan/agua.png';
	    imagenes.push(agua);
	    const trigo = new Image();
	    trigo.onload = pintar_tablero;
	    trigo.src='img/multicatan/trigo.png';
	    imagenes.push(trigo);
	    const madera = new Image();
	    madera.onload = pintar_tablero;
	    madera.src='img/multicatan/madera.png';
	    imagenes.push(madera);
	    const piedra = new Image();
	    piedra.onload = pintar_tablero;
	    piedra.src='img/multicatan/piedra.png';
	    imagenes.push(piedra);
	    const arcilla = new Image();
	    arcilla.onload = pintar_tablero;
	    arcilla.src='img/multicatan/arcilla.png';
	    imagenes.push(arcilla);
	    const oveja = new Image();
	    oveja.onload = pintar_tablero;
	    oveja.src='img/multicatan/oveja.png';
	    imagenes.push(oveja);
	    
	    // resize the canvas to fill browser window dynamically
	    window.addEventListener('resize', resizeCanvas, false);

    	var stage = new Konva.Stage({
            container: 'container',
            draggable: true,
            perfectDrawEnabled: false,
            shadowForStrokeEnabled: false
        });
    	stage.transformsEnabled('position');

    	var layer = new Konva.Layer({
            perfectDrawEnabled: false,
    		shadowForStrokeEnabled: false
    	});
    	layer.transformsEnabled('none');
    	stage.add(layer);

	    var mapa = [
	    	<c:forEach items="${mapa}" var="fila">
		    	[ <c:forEach items="${fila}" var="r"><c:out value="${r}" />,</c:forEach> ],
	    	</c:forEach>
	    ];

	    var numeros = [
	    	<c:forEach items="${numeros}" var="fila">
		    	[ <c:forEach items="${fila}" var="n"><c:out value="${n}" />,</c:forEach> ],
	    	</c:forEach>
	    ];

    	function resizeCanvas() {
    		stage.width(window.innerWidth);
    		stage.height(window.innerHeight - $('#navbar').height());
    		pintar_tablero();
	    }
	    resizeCanvas();

	    function pintar_tablero() {
	    	for (var i = 0; i < mapa[0].length; i++) {
	    		for (var j = 0; j < mapa.length - (i & 1); j++) {
	    			var image = new Konva.Image({
	    		    	image: imagenes[parseInt(mapa[j][i])],
	    		        x: i * 100,
	    		        y: j * 117 + (i & 1 ? 58 : 0),
	    		        listening: false,
	    	            perfectDrawEnabled: false,
	    	            shadowForStrokeEnabled: false
	    		    });
	    			layer.add(image);
	    			
	    			if (numeros[j][i] > 0) {
		    			var text = new Konva.Text({
		    				text: numeros[j][i],
		    				fontSize: 30,
		    				fontFamily: 'Calibri',
		    				fill: es_rojo(numeros[j][i]) ? 'red' : 'black',
		    				stroke: es_rojo(numeros[j][i]) ? 'red' : 'black',
		    				shadowBlur: es_rojo(numeros[j][i]) ? 5 : null,
		    				shadowColor: es_rojo(numeros[j][i]) ? 'white' : null,
		    	            perfectDrawEnabled: false,
		    	            shadowForStrokeEnabled: false,
		    		        x: i * 100 + 69,
		    		        y: j * 117 + (i & 1 ? 58 : 0) + 48
		    			});
		    			text.x(text.x() - text.width() / 2);
		    			layer.add(text);
	    			}
	    		}
	    	}

	    	layer.batchDraw();
	    }
	    
	    function es_rojo(i) {
	    	return i == 6 || i == 8;
	    }
	    
	    function poner_recurso(j, i, recurso) {
	    	mapa[j] = mapa[j].substring(0, i) + recurso + mapa[j].substring(i + 1);
	    }
	</script>
</body>
</html>

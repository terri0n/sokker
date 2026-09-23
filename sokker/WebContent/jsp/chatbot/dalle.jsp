<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <title>Enviar formulario a DALL·E</title>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
  <form id="formulario">
    <label for="texto">Texto:</label>
    <input type="text" id="texto" name="texto"><br><br>
    <label for="archivo">Archivo:</label>
    <input type="file" id="archivo" name="archivo"><br><br>
    <input type="submit" value="Enviar">
  </form>

  <img id="imagen-generada" src="" alt="Imagen generada por DALL·E">

  <script>
    // La clave de OpenAI nunca se envía al navegador. Si se vuelve a usar esta
    // integración, el servidor debe recibirla mediante OPENAI_API_KEY.
	$(document).ready(function() {
	  $('#formulario').submit(function(event) {
	    event.preventDefault();
	    var texto = $('#texto').val();
	    var imagen = $('#archivo').prop('files')[0];

	    if (!imagen) {
	      return;
	    }

	    $.ajax({
	      url: '${pageContext.request.contextPath}/dalle?texto=' + encodeURIComponent(texto),
	      method: 'POST',
	      processData: false,
	      contentType: 'application/octet-stream',
	      data: imagen,
	      success: function(data) {
	        $('#imagen-generada').attr('src', data);
	      },
	      error: function(error) {
	        console.log(error);
	        alert(error.responseText || error.statusText);
	      }
	    });
	  });
	});
</script>
</body>
</html>

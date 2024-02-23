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
    const token = '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';

    // Función que se ejecuta al enviar el formulario
	$(document).ready(function() {
	  $('#formulario').submit(function(event) {
	    event.preventDefault();
	    var texto = $('#texto').val();
	    var imagen = $('#archivo').prop('files')[0];
	    
	    var formData = new FormData();
	    formData.append('prompt', texto);
	    formData.append('image', imagen);
	    
	    $.ajax({
	      url: 'https://api.openai.com/v1/images/edits',
	      method: 'POST',
	      headers: {
	        'Authorization': 'Bearer ' + token,
	      },
	      processData: false,
	      contentType: false,
	      data: formData,
	      success: function(data) {
	        $('#imagen-generada').attr('src', 'data:image/jpeg;base64,' + data.data.edits[0].generated_image);
	      },
	      error: function(error) {
	        console.log(error);
	        alert(error.responseJSON.error.message);
	      }
	    });
	  });
	});
</script>
</body>
</html>

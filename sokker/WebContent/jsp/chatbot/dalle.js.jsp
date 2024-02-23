<%-- $(document).ready(function() {
    $('#formulario').submit(function(event) {
        event.preventDefault();
        var formData = new FormData(this);
        $.ajax({
            url: '${pageContext.request.contextPath}/dalle?texto=' + $('textarea[name="texto"]').val(),
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(data) {
                $('#resultado').html('<img src="' + data + '">');
            }
        });
    });
});
 --%>

$(document).ready(function() {
    $('#formulario').submit(function(event) {
        event.preventDefault();
        
		// Obtener el texto y la imagen del formulario
		const texto = document.getElementById('texto').value;
		const imagenInput = document.getElementById('archivo');
		const imagenFile = imagenInput.files[0];
		const imagenBase64 = await getBase64(imagenFile);
		
		// Construir la solicitud POST para la API de DALL·E
		const endpoint = 'https://api.openai.com/v1/images/edits';
		const apiKey = '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';
		const requestBody = {
		  prompt: texto,
		  image: { b64: imagenBase64 },
		};
		const request = {
		  method: 'POST',
		  headers: {
		    'Content-Type': 'application/json',
		    'Authorization': `Bearer ${apiKey}`,
		  },
		  body: JSON.stringify(requestBody),
		};
		
		// Enviar la solicitud a la API de DALL·E
		const response = await fetch(endpoint, request);
		const responseData = await response.json();
		
		// Obtener la imagen de la respuesta de la API de DALL·E
		const imageUrl = responseData.data[0].url;
		const imagenResponse = await fetch(imageUrl);
		const imagenBlob = await imagenResponse.blob();
		const imagenUrl = URL.createObjectURL(imagenBlob);
		
		// Mostrar la imagen en la página
		const imagenPreview = document.getElementById('imagen-preview');
		imagenPreview.src = imagenUrl;
    });
    
    // Función auxiliar para obtener una cadena base64 a partir de un archivo
	function getBase64(file) {
	  return new Promise((resolve, reject) => {
	    const reader = new FileReader();
	    reader.readAsDataURL(file);
	    reader.onload = () => resolve(reader.result.split(',')[1]);
	    reader.onerror = error => reject(error);
	  });
	}
});

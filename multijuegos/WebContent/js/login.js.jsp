<%@page import="com.formulamanager.multijuegos.util.Util"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Iterator"%>
<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.formulamanager.multijuegos.idiomas.Idiomas"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="<%= Idiomas.APPLICATION_RESOURCES %>" />
let box;
function login_click() {
	box = bootbox.confirm({
		size: 'md',
		backdrop: true,
		message: '<ul class="nav nav-tabs" role="tablist">\
				<li class="nav-item">\
					<a class="nav-link active" data-toggle="tab" href="#tab_login" role="tab" aria-selected="true" onclick="tab_click(\'login\')">\
					<i class="fas fa-key"></i> <fmt:message key="menu.login" />\
				</a>\
			</li>\
			<li class="nav-item">\
				<a class="nav-link" data-toggle="tab" href="#tab_registro" role="tab" aria-selected="false" onclick="tab_click(\'registro\')">\
					<i class="fas fa-envelope"></i> <fmt:message key="menu.register" />\
				</a>\
			</li>\
			<li class="nav-item">\
				<a class="nav-link" data-toggle="tab" href="#tab_cambio" role="tab" aria-selected="false" onclick="tab_click(\'cambio\')">\
					<i class="fas fa-exchange-alt"></i> <fmt:message key="menu.change_password" />\
				</a>\
			</li>\
		</ul>\
		\
		<div class="tab-content bg-white text-left">\
			<div class="tab-pane show active" id="tab_login" role="tabpanel">\
				<form id="form_login">\
					<h4>Nombre</h4>\
					<input type="text" name="login_nombre" class="form-control" maxlength="16" required="required" value="" onkeypress="input_keypress(event)" pattern="[0-9a-zA-Z_]+$" oninput="validarInput(event, this)" />\
					<h4>Contraseña</h4>\
					<input type="password" name="login_contrasenya" class="form-control" required="required" onkeypress="input_keypress(event)" />\
				</form>\
			</div>\
			<div class="tab-pane" id="tab_registro" role="tabpanel">\
				<form id="form_registro">\
					<h4>Nombre</h4>\
					<input type="text" name="registro_nombre" class="form-control" maxlength="16" required="required" onkeypress="input_keypress(event)" pattern="^[0-9a-zA-Z_]+$" oninput="validarInput(event, this)" />\
					<h4>Contraseña</h4>\
					<input type="password" name="registro_contrasenya" class="form-control" required="required" onkeypress="input_keypress(event)" />\
					<h4>Repite contraseña</h4>\
					<input type="password" name="registro_contrasenya2" class="form-control" required="required" onkeypress="input_keypress(event)" />\
					<h4>E-mail</h4>\
					<input type="email" name="registro_email" class="form-control" required="required" onkeypress="input_keypress(event)" />\
					<span style="color: red;" id="span_email"></span>\
					<h4>País</h4>\
					<c:set var="iso" value="<%= Locale.getDefault().getCountry() %>" />\
					<c:if test="${not empty iso}">\
						<img src="${pageContext.request.contextPath}/img/banderas/${iso == 'EN' ? 'GB' : iso}.png" class="margin-right"/>\
						<%= Locale.getDefault().getDisplayCountry() %>\
					</c:if>\
				</form>\
			</div>\
			<div class="tab-pane" id="tab_cambio" role="tabpanel">\
				<form id="form_cambio">\
					<h4>Nombre o e-mail</h4>\
					<input type="text" name="cambio_nombre_email" class="form-control" required="required" onkeypress="input_keypress(event)" pattern="^[0-9a-zA-Z_]+$" oninput="validarInput(event, this)" />\
					<h4>Contraseña</h4>\
					<input type="password" name="cambio_contrasenya" class="form-control" required="required" onkeypress="input_keypress(event)" />\
					<h4>Repite contraseña</h4>\
					<input type="password" name="cambio_contrasenya2" class="form-control" required="required" onkeypress="input_keypress(event)" />\
					<span style="color: red;" id="span_email2"></span>\
				</form>\
			</div>\
			<span style="color: red;" id="span_mensaje"></span>\
		</div>',
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
		callback: login_callback,
		onShown: function (e) {
			tab_click('login');
			dropdown_init('${fn:toUpperCase(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language)}');
		}
	});
 	
	box.find('.modal-body').css('padding', '0');
}

function validarInput(event, input) {
    // Eliminar caracteres inválidos del valor actual utilizando el patrón definido en el input
    input.value = input.value.replace(new RegExp('[^0-9a-zA-Z_]', 'g'), '');
}

function input_keypress(e) {
	if (e.keyCode == 13) { //Enter keycode
		login_callback(true);
	}
}

function login_callback(result) {
	if (result) {
		if ($('#tab_login').hasClass("active")) {
			login_submit();
		} else if ($('#tab_registro').hasClass("active")) {
			registro_submit();
		} else {
			cambio_submit();
		}
	} else {
        box.modal('hide');
	}
	return false;
}

function tab_click(tab) {
	setTimeout(() => {
		$('#form_' + tab + ' input:first').focus();
	}, 10);
}

function login_submit() {
	if ($('#form_login')[0].checkValidity()) {
		const nombre = $('input[name="login_nombre"]').val();
		const contrasenya = $('input[name="login_contrasenya"]').val();
		$('.modal-footer .bootbox-accept').prop('disabled', true);
		login(nombre, contrasenya);
	} else {
		showToast.show('Ocurrió un error. Revise los campos');
	}
}

function login(usuario, contrasenya) {
	axios.get('${pageContext.request.contextPath}/login?nombre=' + encodeURIComponent(usuario) + '&contrasenya=' + encodeURIComponent(contrasenya)).then((response) => {
		console.log(response.data);
		if (response.data) {
			crearCookie('usuario', usuario, Number.MAX_SAFE_INTEGER);
			crearCookie('contrasenya', contrasenya, Number.MAX_SAFE_INTEGER);
	        box.modal('hide');
			location.reload();
		} else {
			$('#span_mensaje').html('Usuario o contraseña incorrectos');
			$('#span_mensaje').show();
			$('.modal-footer .bootbox-accept').prop('disabled', false);
			
			eliminarCookie('usuario');
			eliminarCookie('contrasenya');
		}
	}).catch(error => {
	    console.log("err", error);
	    showToast.show('Error al acceder al servidor: ' + error);
	    setTimeout(() => {
			location.reload();
		}, 2000);
	});
}

function registro_submit() {
	$('#span_email').hide();

	if ($('#form_registro')[0].checkValidity()) {
		let nombre = $('input[name="registro_nombre"]').val();
		const contrasenya = $('input[name="registro_contrasenya"]').val();
		const contrasenya2 = $('input[name="registro_contrasenya2"]').val();
		const email = $('input[name="registro_email"]').val();

		if (contrasenya != contrasenya2) {
			$('input[name="registro_contrasenya"]').focus();
			showToast.show('Las contraseñas no coinciden');
			return false;
		} else {
			$('.modal-footer .btn-primary').prop('disabled', true);
			$('#span_email').html('Conectando con el servidor...');
			$('#span_email').show();
			axios.get('${pageContext.request.contextPath}/registro?nombre=' + nombre + '&contrasenya=' + contrasenya + '&email=' + email).then((response) => {
				console.log(response.data);
				$('#span_email').html('E-mail de confirmaci&oacute;n enviado. Haz click en el enlace para confirmar el registro');
			}).catch(error => {
			    console.log(error.response);
			    if (error.response.status == 403) {
					// 403 = Prohibido
			    	// NOTA: la ruta al texto del error depende de la versión de Tomcat. Para Tomcat 9:
			    	$('#span_email').html($(error.response.data).children()[1].nextSibling);
				} else {
					$('#span_email').html(error.response.statusText + ' (Código ' + error.response.status + ')');
				}
				$('.modal-footer .btn-primary').prop('disabled', false);
			});
			return false;
		}
	} else {
		showToast.show('Ocurrió un error. Revise los campos');
		return false;
	}
}

function cambio_submit() {
	$('#span_email2').hide();

	if ($('#form_cambio')[0].checkValidity()) {
		let nombre_email = $('input[name="cambio_nombre_email"]').val();
		const contrasenya = $('input[name="cambio_contrasenya"]').val();
		const contrasenya2 = $('input[name="cambio_contrasenya2"]').val();

		if (contrasenya != contrasenya2) {
			$('input[name="cambio_contrasenya"]').focus();
			showToast.show('Las contraseñas no coinciden');
			return false;
		} else {
			$('.modal-footer .btn-primary').prop('disabled', true);
			$('#span_email2').html('Conectando con el servidor...');
			$('#span_email2').show();
			axios.get('${pageContext.request.contextPath}/cambio_contrasenya?nombre_email=' + nombre_email + '&contrasenya=' + contrasenya).then((response) => {
				console.log(response.data);
				$('#span_email2').html('E-mail de confirmaci&oacute;n enviado. Haz click en el enlace para confirmar el cambio');
			}).catch(error => {
			    console.log(error.response);
			    if (error.response.status == 403) {
					// 403 = Prohibido
			    	$('#span_email2').html($(error.response.data).children('u').html());
				} else {
					$('#span_email2').html(error.response.statusText + ' (Código ' + error.response.status + ')');
				}
				$('.modal-footer .btn-primary').prop('disabled', false);
			});
		}
		return false;
	} else {
		showToast.show('Ocurrió un error. Revise los campos');
		return false;
	}
}

function desconectar_click() {
	bootbox.confirm({
		message: '<span class="material-icons">help</span> <fmt:message key="connection.disconnect_q" />',
		locale: '${pageContext.request.locale.language}',
		swapButtonOrder: true,
		backdrop: true,
		closeButton: false,
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
				eliminarCookie('usuario');
				eliminarCookie('contrasenya');
				location.href = 'desconectar?id=${param.id}';
			}
		}
	});
}

//---------
// COOKIES
//---------

function obtenerCookie(nombre) {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i].trim();
        if (cookie.startsWith(nombre + '=')) {
            return cookie.substring(nombre.length + 1);
        }
    }
    return null;
}

function crearCookie(nombre, valor, dias) {
    var fechaExpiracion = new Date();
    fechaExpiracion.setTime(fechaExpiracion.getTime() + (dias * 24 * 60 * 60 * 1000));
    var expira = "expires=" + fechaExpiracion.toUTCString();
    document.cookie = nombre + "=" + valor + ";" + expira + ";path=/";
}

function eliminarCookie(nombre) {
    document.cookie = nombre + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;";
}

function comprobar_cookie() {
    // Obtener valores de las cookies
    let usuario = obtenerCookie("usuario");
    let contrasenya = obtenerCookie("contrasenya");

    // Realizar operaciones de inicio de sesión basadas en los valores de las cookies
    if (usuario) {
        login(usuario, contrasenya);
    }
}

<c:if test="${jugador.invitado}">
	comprobar_cookie();
</c:if>
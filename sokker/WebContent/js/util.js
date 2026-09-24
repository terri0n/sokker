var SOKKER_CLIENT_KEY = "skc_bae02f2686bf9038d248";

var ASSISTANT_REMEMBER_LOGIN_KEY = "sokkerAsistente.rememberedLogin";
var ASSISTANT_REMEMBER_PASSWORD_KEY = "sokkerAsistente.rememberedPassword";

function sokkerPost(url, data, success, dataType) {
	var deferred = $.Deferred();

	$.ajax({
		url: url,
		type: "POST",
		data: data,
		dataType: dataType,
		headers: {
			"X-Sokker-Client": SOKKER_CLIENT_KEY
		}
	}).done(function() {
		deferred.resolveWith(this, arguments);
	}).fail(function(xhr) {
		var failedContext = this;
		var failedArguments = arguments;

		// Mientras Sokker no acepte el preflight CORS del header identificador,
		// mantenemos el flujo anterior. Con status 0 el POST de credenciales no
		// ha sido enviado porque el navegador ha bloqueado antes el preflight.
		if (xhr && xhr.status === 0) {
			$.post(url, data, null, dataType)
				.done(function() {
					deferred.resolveWith(this, arguments);
				})
				.fail(function() {
					deferred.rejectWith(this, arguments);
				});
		} else {
			deferred.rejectWith(failedContext, failedArguments);
		}
	});

	var request = deferred.promise();
	if ($.isFunction(success)) {
		request.done(success);
	}
	return request;
}

function assistantStorageGet(key) {
	try {
		return localStorage.getItem(key);
	} catch (e) {
		return null;
	}
}

function assistantStorageSet(key, value) {
	try {
		localStorage.setItem(key, value);
	} catch (e) {
		// El almacenamiento local puede estar deshabilitado. El login debe seguir funcionando.
	}
}

function assistantStorageRemove(key) {
	try {
		localStorage.removeItem(key);
	} catch (e) {
		// El almacenamiento local puede estar deshabilitado. El login debe seguir funcionando.
	}
}

function getAssistantLegacyCookie(name) {
	var prefix = name + "=";
	var cookies = document.cookie ? document.cookie.split(";") : [];
	for (var i = 0; i < cookies.length; i++) {
		var cookie = cookies[i].replace(/^\s+/, "");
		if (cookie.indexOf(prefix) === 0) {
			var value = cookie.substring(prefix.length);
			try {
				return decodeURIComponent(value);
			} catch (e) {
				return value;
			}
		}
	}
	return null;
}

function clearAssistantLegacyPasswordCookie(form) {
	var path = "/";
	try {
		var actionPath = new URL(form.action, window.location.href).pathname;
		path = actionPath.replace(/\/login$/, "") || "/";
	} catch (e) {
		// Mantener '/' como respaldo para navegadores antiguos.
	}

	document.cookie = "apassword=; Max-Age=0; path=" + path + "; SameSite=Lax";
	if (path !== "/") {
		document.cookie = "apassword=; Max-Age=0; path=/; SameSite=Lax";
	}
}

function clearAssistantRememberPassword() {
	assistantStorageRemove(ASSISTANT_REMEMBER_LOGIN_KEY);
	assistantStorageRemove(ASSISTANT_REMEMBER_PASSWORD_KEY);
}

function rememberAssistantPassword(form) {
	var $form = $(form);
	var remember = $form.find("input[name='recordar']").prop("checked");
	if (!remember) {
		clearAssistantRememberPassword();
		return;
	}

	assistantStorageSet(ASSISTANT_REMEMBER_LOGIN_KEY, $form.find("input[name='alogin']").val() || "");
	assistantStorageSet(ASSISTANT_REMEMBER_PASSWORD_KEY, $form.find("input[name='apassword']").val() || "");
}

function ensureAssistantRememberPasswordControls($form) {
	var label = window.SOKKER_ASSISTANT_REMEMBER_PASSWORD_LABEL;
	var $login = $form.find("input[name='alogin']");
	var $password = $form.find("input[name='apassword']");
	var $submit = $form.find("input[type='submit']").first();

	if (!label || !$login.length || !$password.length || !$submit.length) {
		return false;
	}

	$login.attr("autocomplete", "username");
	$password.attr("autocomplete", "current-password");

	if (!$form.find("input[name='recordar']").length) {
		var $checkbox = $("<input>", {
			type: "checkbox",
			name: "recordar",
			id: "recordar"
		});
		var $label = $("<label>").addClass("peque").attr("for", "recordar");
		$label.append($checkbox);
		$label.append(document.createTextNode(label));
		$label.insertBefore($submit);
		$("<br>").insertBefore($submit);
	}

	return true;
}

function initAssistantRememberPassword() {
	var $form = $("form[action$='/asistente/login']").first();
	if (!$form.length || !ensureAssistantRememberPasswordControls($form)) {
		return;
	}

	var login = assistantStorageGet(ASSISTANT_REMEMBER_LOGIN_KEY);
	var password = assistantStorageGet(ASSISTANT_REMEMBER_PASSWORD_KEY);
	var legacyPassword = getAssistantLegacyCookie("apassword");

	if (password === null && legacyPassword !== null && legacyPassword !== "") {
		password = legacyPassword;
		login = $form.find("input[name='alogin']").val() || login || "";
		assistantStorageSet(ASSISTANT_REMEMBER_LOGIN_KEY, login);
		assistantStorageSet(ASSISTANT_REMEMBER_PASSWORD_KEY, password);
	}

	if (legacyPassword !== null) {
		clearAssistantLegacyPasswordCookie($form[0]);
	}

	if (password !== null && password !== "") {
		if (login !== null && login !== "") {
			$form.find("input[name='alogin']").val(login);
		}
		$form.find("input[name='apassword']").val(password);
		$form.find("input[name='recordar']").prop("checked", true);
	}

	$form.off("submit.assistantRememberPassword").on("submit.assistantRememberPassword", function() {
		rememberAssistantPassword(this);
	});

	$form.find("input[name='recordar']")
		.off("change.assistantRememberPassword")
		.on("change.assistantRememberPassword", function() {
			if (!this.checked) {
				clearAssistantRememberPassword();
			}
		});
}

$(function() {
	initAssistantRememberPassword();

	$(document).mouseup(function(e) {
	    var container = $(".dropdown-menu");

	    // if the target of the click isn't the container nor a descendant of the container
	    if (!container.is(e.target) && container.has(e.target).length === 0) {
	        container.hide();
	    }
	});
	
	$('.dropdown').click(function() {
		$(this).find("ul").show();
	});
});

function dropdown_select(target) {
	var desplegable = $(target).closest('.dropdown');
	desplegable.attr("data-toggle", $(target).attr("data-toggle"));
	desplegable.find('span').html($(target).html().trim());
	setTimeout(function() { 
		$(target).closest('ul').hide();
	}, 1);
}

function dropdown_click(target) {
	dropdown_select(target);
	var desplegable = $(target).closest('.dropdown');

	if (desplegable.attr('data-onchange')) {
		eval(desplegable.attr('data-onchange'));
	}
}

function dropdown_init(value) {
	$('.dropdown:last li').each(function() {
		if ($(this).attr('data-toggle') == value) {
			dropdown_select(this);
		}
	});
}

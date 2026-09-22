var SOKKER_CLIENT_KEY = "skc_bae02f2686bf9038d248";

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

$(function() {
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

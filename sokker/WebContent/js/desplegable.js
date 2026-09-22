(function($) {
	var SOKKER_CLIENT_KEY = "skc_bae02f2686bf9038d248";
	var originalPost = $.post;

	function isSokkerRequest(url) {
		return typeof url === "string"
				&& (url.indexOf("https://sokker.org/") === 0 || url.indexOf("//sokker.org/") === 0);
	}

	function resolveDeferred(deferred, context, args) {
		deferred.resolveWith(context, args);
	}

	function rejectDeferred(deferred, context, args) {
		deferred.rejectWith(context, args);
	}

	function identifiedSokkerPost(url, data, dataType) {
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
			resolveDeferred(deferred, this, arguments);
		}).fail(function(xhr) {
			var failedContext = this;
			var failedArguments = arguments;

			// Until Sokker accepts the custom-header CORS preflight, keep the existing
			// browser login path working. A rejected preflight has status 0 and the
			// credential POST itself has not been sent.
			if (xhr && xhr.status === 0) {
				originalPost.call($, url, data, null, dataType)
					.done(function() {
						resolveDeferred(deferred, this, arguments);
					})
					.fail(function() {
						rejectDeferred(deferred, this, arguments);
					});
			} else {
				rejectDeferred(deferred, failedContext, failedArguments);
			}
		});

		return deferred.promise();
	}

	$.post = function(url, data, success, dataType) {
		if (!isSokkerRequest(url)) {
			return originalPost.apply(this, arguments);
		}

		var request = identifiedSokkerPost(url, data, dataType);
		if ($.isFunction(success)) {
			request.done(success);
		}
		return request;
	};
})(jQuery);

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

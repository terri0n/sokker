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

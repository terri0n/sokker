function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
};

String.prototype.initCap = function () {
	return this.toLowerCase().replace(/(?:^|\s)[a-z]/g, function (m) {
		return m.toUpperCase();
	});
};

function secondsToTime(segundos) {
	var m = Math.floor(segundos / 60);
	var s = segundos - m * 60;
	return dos_digitos(m) + ":" + dos_digitos(s);
}

function dos_digitos(n) {
	return n < 10 ? '0' + n : n;
}

/**
 * Shuffles array in place. ES6 version
 * @param {Array} a items An array containing the items.
 */
function shuffle(a) {
    for (let i = a.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
}

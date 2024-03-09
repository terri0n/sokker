class Jugador {
	constructor(jugador, juego) {
        for (let prop in jugador) {
            if (jugador.hasOwnProperty(prop)) {
                this[prop] = jugador[prop];
            }
        }
        this.juego = juego;
	}

	mostrar_jugador() {
		let nivel;
		if (this.puntos < 1400) {
			nivel = 'principiante';
		} else if (this.puntos < 1800) {
			nivel = 'aficionado';
		} else if (this.puntos < 2100) {
			nivel = 'experto';
		} else if (this.puntos < 2400) {
			nivel = 'maestro';
		} else if (this.puntos < 2600) {
			nivel = 'maestro_internacional';
		} else if (this.puntos < 2800) {
			nivel = 'gran_maestro';
		} else {
			nivel = 'campeon_del_mundo';
		}

		// NOTA: no funciona el Locale en espa�ol
		let s = `<span class="info_jugador ` + nivel + `" title="` + this.juego.traducir(nivel) + `">
				<img class="bandera" src="img/banderas/` + this.pais + `.png" /><span class="puntos">` + this.puntos.toLocaleString('de-DE') + `</span>
			</span>`;
		let t = `<span class="jugador" id="nombre_` + this.nombre + `">` + this.nombre + `</span>
			<div class="away" style="display:none" title="Away">zZ<span>Z</span></div>`;

		return s + t;
	}
}
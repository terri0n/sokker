const ob = $('.js-court');
const info = $('.js-info');
const loader = $('.ajax-loader-container');
const _canvasName = 'canvas-boisko';
let _canvas;
let tact;
const playersDetals = [];
let ctx;
const _startWidth = 9;
const _fontSize = 11;
const shiftNameTxt = -Math.floor(_fontSize * 1.2);
const _allHuman = 10;
const studio_image = new Image;
const player_image = new Image;
const _widthCanvas = parseInt($(ob).css('width'), 10);
const _heightCanvas = parseInt($(ob).css('height'), 10);
const _widthStudio = _widthCanvas;
const _heightStudio = _heightCanvas - 45;
const _stadioY = 23;
const _stadioX = 0;
let _animationInterval;
const _players = [];
const _buttons = [];
let _mouseX = 0;
let _mouseY = 0;
let _cursor = 'auto';
let _cursorPointer = 'pointer';
let handCursor = false;
let _mouseDown = false;
const _sensitivityDistance = 10;
//------------------------ FLASH
const BALL_X = 5;
const BALL_Y = 7;
const PLAYER_NO = 11;
let ball = undefined;
let tact_arr = [];
//---------------------NET
const startNetX = 12;
const startNetY = 30;
//---------------------BALL
let mesh = true;
let BALL_START_X = _widthStudio / 2;
let BALL_START_Y = _heightStudio / 2;
let bx = 3;
let by = 4;
let WID = _widthStudio;
let HEI = _heightStudio;
let BORD_X = 6 * ( WID / 236.0 );
let BORD_Y = 18 * ( HEI / 344.0 );

let RES_X = 14.0;
let RES_Y = 15.0;

let RES_MESH_X = 15.0;
let RES_MESH_Y = 15.0;

let MOV_X = 0;
let MOV_Y = 19.0;

let MIN_DIST_X = 1.0;
let MIN_DIST_Y = 1.0;
const BALL_RADIUS = 5;

class Ball {
	constructor() {
		this.visible = false;
		this.dragging = false;
		this.drawY = 0;
		this.drawX = 0;
		this._x = 0;
		this._y = 0;
	}

	distance() {
		const _a = Math.abs(this.drawX - _mouseX);
		const _b = Math.abs(this.drawY - _mouseY);
		return Math.sqrt(Math.pow(_a, 2) + Math.pow(_b, 2));
	}

	show() {
		ctx.fillStyle = '#ffffff';
		if (this.distance() < _sensitivityDistance) {
			ctx.fillStyle = 'rgba(0, 0, 0, 1)';
			handCursor = true;
		}

		if (this.dragging) {
			this.x = calculateProportions(_mouseX, _widthStudio, WID);
			this.y = calculateProportions(_mouseY, _heightStudio, HEI);
		}
		ctx.arc(this.drawX, this.drawY, BALL_RADIUS, 0, 2 * Math.PI);
		ctx.fill();
	}

	pressed() {
		if (this.distance() < _sensitivityDistance) {
			this.dragging = true;
		}
	}

	unPressed() {
		this.dragging = false;
	}

	set x(_x) {
		this._x = _x;
		this.drawX = calculateProportions(this._x, WID, _widthStudio);
	}

	get x() {
		return this._x;
	}

	set y(_y) {
		this._y = _y;
		this.drawY = calculateProportions(this._y, HEI, _heightStudio);
	}

	get y() {
		return this._y;
	}
}

class Player {
	constructor() {
		this._x = undefined;
		this._y = undefined;
		this.drawX = 0;
		this.drawY = 0;
		this.x_drag = undefined;
		this.y_drag = undefined;
		this.visible = false;
		this.no = undefined;
		this.active = undefined;
		this.currentframe = undefined;
		this.dragging = false;
		this.mbegx = undefined;
		this.mbegy = undefined;
		this.begx = undefined;
		this.begy = undefined;
	}

	distance() {
		const _a = Math.abs(this.drawX - _mouseX);
		const _b = Math.abs(this.drawY - _mouseY + 10);
		return Math.sqrt(Math.pow(_a, 2) + Math.pow(_b, 2));
	}

	set x(_x) {
		this._x = Math.round(limitX(_x));
		this.drawX = Math.round(calculateProportions(this._x, WID, _widthStudio - _stadioX));
	}

	get x() {
		return this._x;
	}

	set y(_y) {
		this._y = Math.round(limitY(_y));
		this.drawY = Math.round(calculateProportions(this._y, HEI, _heightStudio - startNetY));
	}

	get y() {
		return this._y;
	}

	show() {
		ctx.drawImage(player_image, this.drawX - 5, this.drawY, 12, 18);
		ctx.fillStyle = '#ffffff';
		ctx.font = "bold " + _fontSize + "px Arial, sans-serif";
		let _shirtNumber = this.currentframe;
		const _widthTxt = ctx.measureText(_shirtNumber).width;
		const _textX = this.drawX - _widthTxt / 2;
		const _textY = this.drawY - _fontSize + 5;
		ctx.strokeText(_shirtNumber, _textX, _textY);
		ctx.fillText(_shirtNumber, _textX, _textY);
		const circle = new Path2D();
		ctx.fillStyle = '#ffffff';
		if (this.distance() < _sensitivityDistance) {
			ctx.fillStyle = 'rgba(255, 165, 0, 1)';
			handCursor = true;
		}
		if (this.dragging) {
			handCursor = true;
			this.drag();
			dragHumanJS(this);
		}

		ctx.beginPath();
		ctx.fillStyle = '#00ff00';
	}

	drag(){
		this._x = this.begx + _mouseX - this.mbegx;
		this._y = this.begy + _mouseY - this.mbegy;
	};

	pressed() {
		if (this.distance() < _sensitivityDistance && !ball.dragging) {
			this.dragging = true;
			this.mbegx = _mouseX;
			this.mbegy = _mouseY;
			this.begx = this._x;
			this.begy = this._y;
		}
	}

	unPressed() {
		if (this.currentframe && this.dragging) {
			this.dragging = false;
			const h = fromNumber(this.currentframe);

			let x = Math.round(setX(this.x));
			let y = Math.round(setY(this.y));

			tact_arr[bx][by][h].x = x;
			tact_arr[bx][by][h].y = y;
		}
	}

	gotoAndStop(_nr) {
		this.currentframe = _nr;
	}
}

const limitX = function(x = calculateProportions(_mouseX, _widthStudio, WID)) {
	x = (x < startNetX) ? startNetX : x;
	x = (x > _widthStudio - 10) ? _widthStudio - 16 : x;

	return x;
}

const limitY = function(y = calculateProportions(_mouseY, _widthStudio, WID)) {
	y = y < startNetY ? startNetY : y;
	y = y > _heightStudio ? _heightStudio : y;

	return y;
}

const calculateProportions = function(_val, _def, _length) {
	return Math.floor(_val / _def * _length);
};

const calculateProportionsInverse = function(_val, _def, _length) {
	return Math.floor(_val / _length * _def);
};

const createCanvas = function() {
	const canvas = '<canvas id="' + _canvasName + '" class="canvas" width="' + _widthCanvas + '" height="' + _heightCanvas + '"></canvas>';
	$(ob).append(canvas);
	_canvas = document.getElementById(_canvasName);
	return document.getElementById(_canvasName).getContext('2d');
};
const loadPlayer = function() {
	const _src = 'https://sokker.org/static/pic/human.svg';
	player_image.src = _src;
};
const showPlayers = function() {
	if (ball.dragging) {
		showTactSub();
	}
	for (i = 1; i < _players.length; i++) {
		_players[i].show();
	}
};
const createPlayers = function() {
	for (let i = 0; i <= PLAYER_NO; i++) {
		_players.push(new Player());
	}
};
const createBall = function() {
	ball = new Ball();
	ball.x = BALL_START_X;
	ball.y = BALL_START_Y;
};
const loadStadio = function(_src) {
	studio_image.onload = function() {
		loadPlayer();
	};
	studio_image.src = _src;
};
const drawAll = function() {
	ctx.clearRect(0, 0, _widthCanvas, _heightCanvas);
	handCursor = false;
	showPlayers();
	showBall();
	showButtons();
	showInfo();
	showCursor();
};

class ButtonStadium {
	constructor(_x, _y, _txt, _callFunction) {
		this.x = _x;
		this.y = _y;
		this.txt = _txt;
		this.call = _callFunction;
		this.width = 0;
		this.height = 0;
		this.font = "bold 14px Arial, sans-serif";
		this.mouseIsOver = false;
		this.paddingX = 8;
		this.paddingY = 4;
		this.init();
	}

	init() {
		ctx.font = this.font;
		this.width = ctx.measureText(this.txt).width + (2 * this.paddingX);
		this.height = parseInt(ctx.font.match(/\d+/), 10) - 4;
	}

	show() {
		this.mouseOver();
		ctx.fillStyle = 'rgba(255,255,255,.1)';
		ctx.fillRect(this.x - this.paddingX, this.y - this.height - this.paddingY, this.width, this.height + (2 * this.paddingY));
		ctx.font = this.font;
		ctx.fillStyle = (this.mouseIsOver) ? '#ffffff' : '#000000';
		ctx.fillText(this.txt, this.x, this.y);
	}

	mouseOver() {
		this.mouseIsOver = (_mouseX > this.x - this.paddingX && _mouseX < this.x + this.width - this.paddingX && _mouseY > this.y - this.height - this.paddingY && _mouseY < this.y + this.paddingY);
		if (this.mouseIsOver) {
			handCursor = true;
		}
	}

	pressed() {
		if (this.mouseIsOver) {
			this.call();
		}
	}

	unPressed() {
		if (this.mouseIsOver) {

		}
	}
}

const showButtons = function() {
	for (i = 0; i < _buttons.length; i++) {
		_buttons[i].show();
	}
}

const showInfo = function() {
	let _txt = '';
	_txt += 'X:' + _mouseX + ' Y:' + _mouseY + '<br>';
	_txt += 'ball x:' + ball.x + ' (' + ball.drawX + ') y:' + ball.y + ' (' + ball.drawY + ') <b>' + ball.dragging + '</b><br>';
	for (let i = 0; i < _players.length; i++) {
		const drag = (_players[i].dragging) ? '<b>TRUE</b>' : '-';
		_txt += 'x: ' + _players[i].x + ' (' + _players[i].drawX + ') y:' + _players[i].y + ' (' + _players[i].drawY + ') ' + drag + '<br>';
	}
	info.html(_txt);
};

const showBall = function() {
	ball.show();
};

const showCursor = function() {
	_canvas.style.cursor = (handCursor) ? _cursorPointer : _cursor;
};


const createButtons = function() {
	_buttons.push(new ButtonStadium(20, _stadioY + _heightStudio + 15, 'Mirror', clickMirror));
	_buttons.push(new ButtonStadium(120, _stadioY + _heightStudio + 15, 'Save', clickSave));
}

const clickSave = function() {
	loader.show();
	sendTact();
}

const sendTact = function() {
	const _dane = tactFromArray(tact_arr);
	$.ajax({
		method: "put",
		url: `/api/tactic/${tactId}`,
		contentType: 'application/json; charset=utf-8',
		dataType: "json",
		data: JSON.stringify({
			"config": _dane
		}),
		success: function(response) {
			console.log('ok', response);
			loader.hide()
		}, //gdy wszystko ok
		error: function() {
			console.log('error', _dane);
		}, //gdy błąd połączenia
		complete: function() {
			console.log('complete');
		} //gdy połączenie zakończyło się (błędnie lub pozytywnie)
	});
}

const clickMirror = function() {
	mirrorTact();
	setTimeout(drawAll, 100);
}

const playerPressed = function() {
	for (let i = 0; i < _players.length; i++) {
		_players[i].pressed();
	}
}
const playerUnpressed = function() {
	for (let i = 0; i < _players.length; i++) {
		_players[i].unPressed();
	}
}

const buttonPressed = function() {
	for (let i = 0; i < _buttons.length; i++) {
		_buttons[i].pressed();
	}
}
const buttonUnpressed = function() {
	for (let i = 0; i < _buttons.length; i++) {
		_buttons[i].unPressed();
	}
}

const mousedownCanvas = function() {
	_mouseDown = true;
	ball.pressed();
	playerPressed();
	buttonPressed();
};

const mouseupCanvas = function() {
	if (_mouseDown) {
		_mouseDown = false;
		ball.unPressed();
		playerUnpressed();
		buttonUnpressed();
		showAll(true);
	}
};
const mouseoutCanvas = function() {
	if (_mouseDown) {
		_mouseDown = false;
		ball.unPressed();
		playerUnpressed();
		buttonUnpressed();
		showAll(true);
	}
};

ob.mousedown(mousedownCanvas);
ob.mouseup(mouseupCanvas);
ob.mouseout(mouseoutCanvas);

//------ Global functions ------------------------------------------------------------------------------------------

const swapObject = function(c1, c2) {
	let buf;
	for (const i in c1) {
		buf = c2[i];
		c2[i] = c1[i];
		c1[i] = buf;
	}
};


const toNumber = function(h) {
	return (((h + 1) % PLAYER_NO) + 1);
};


const fromNumber = function(no) {
	return (((no + PLAYER_NO - 2) % PLAYER_NO));
};

//------------------------------------------------------------------------------------------------------------------


function tactToArray(tct) {
	//------ Create table -----------------
	let tab = [];
	for (let i = 0; i < BALL_X; i++) {
		tab[i] = [];
		for (let j = 0; j < BALL_Y; j++) {
			tab[i][j] = [];
			for (let k = 0; k < PLAYER_NO; k++) {
				tab[i][j][k] = {};
			}
		}
	}
	let io;
	let iko;
	let ix;
	let xo;
	let yo;
	for (let j = 0; j < PLAYER_NO - 1; j++) {
		io = 35 * j;
		iko = 35 + 35 * j;
		for (i = io; i < iko; i++) {
			ix = i - io;
			xo = tct.charCodeAt(i * 2 + 1) - 65;
			yo = tct.charCodeAt(i * 2) - 65;

			tab[ix % 5][BALL_Y - 1 - Math.floor(ix / 5)][j].x = xo;
			tab[ix % 5][BALL_Y - 1 - Math.floor(ix / 5)][j].y = yo;
		}
	}

	return (tab);
}

function tactFromArray(tab) {
	let tct = "";
	for (let j = 0; j < PLAYER_NO - 1; j++) {
		for (let i = 0; i < BALL_X * BALL_Y; i++) {
			const ix = i;
			const xo = String.fromCharCode(tab[ix % 5][BALL_Y - 1 - Math.floor(ix / 5)][j].x + 65);
			const yo = String.fromCharCode(tab[ix % 5][BALL_Y - 1 - Math.floor(ix / 5)][j].y + 65);
			tct += yo + xo;
		}
	}

	return (tct);
}

function showMovies(val) {
	ball.visible = val;
	for (let h = 0; h < PLAYER_NO; h++) {
		const no = toNumber(h);
		_players[no].visible = val;
		_players[no].no = h;
		_players[no].active = false;
	}
}

function ConvertToMesh_X(x){
	x = Math.round(RES_MESH_X * (Math.round(x) / RES_X));	
	x = Math.round(RES_X * (Math.round(x) / RES_MESH_X));	
	
	return x;
};

function ConvertToMesh_Y(y){
	y = Math.round((RES_MESH_Y * ((Math.round(y) / RES_Y -0.5) * (0.964426877470356 / 0.88) + 0.5)));
	y = Math.round((RES_Y * ((Math.round(y) / RES_MESH_Y - 0.5) * (0.88 / 0.964426877470356)+0.5)));
	
	return y;
};


function setBall(mod) {
	const wx = WID / 4.0;
	const wy = HEI / 6.0;
	if (mod) {
		ball.x = MOV_X + (WID - wx) * bx / 4.0 + wx * 0.5;
		ball.y = MOV_Y + (HEI - wy) * by / 6.0 + wy * 0.5;
	}
}

function showHuman(nr, x, y) {
	let _x = Math.round((WID - BORD_X * 2) * (x / RES_X) + MOV_X + BORD_X)
	let _y = Math.round((HEI - BORD_Y * 2) * (1.0 - (y / RES_Y)) + MOV_Y + BORD_Y)

	_players[nr].x = _x;
	_players[nr].y = _y;
}

function limitHumanX(x) {
	x = Math.floor(x);
	if (x < 0) {
		x = 0;
	}

	if (x > RES_X) {
		x = RES_X;
	}

	return x;
}

function limitHumanY(y) {
	y = Math.floor(y);

	if (y < 0) {
		y = 0;
	}

	if (y > RES_Y) {
		y = RES_Y;
	}

	return y;
}

const setX = function(_x) {
	return Math.round((_x - BORD_X - MOV_X) / (WID - BORD_X * 2) * RES_X);
}

const setY = function(_y) {
	return Math.round((1.0 - (_y - BORD_Y - MOV_Y) / (HEI - BORD_Y * 2) ) * RES_Y);
}

function moveHuman(_nr) {
	const hum = _players[_nr];
	let x = setX(hum.x);
	let y = setY(hum.y);

	if (mesh && hum._currentframe != 1) {
		x = ConvertToMesh_X(x)
		y = ConvertToMesh_Y(y)
	}	

	return [limitHumanX(x), limitHumanY(y)]
}

function clearHalo() {
	let no;
	for (let h = 0; h < PLAYER_NO; h++) {
		no = toNumber(h);
		_players[no].active = false;
	}
}

function findActive() {
	let no;
	for (let h = 0; h < PLAYER_NO; h++) {
		no = toNumber(h);
		if (_players[no].active) return (no);
	}
	return 1;
}

function dragHumanJS(player) {
	if (player.x_drag == undefined || player.y_drag == undefined){
		player.x_drag = posHumanX(player.currentframe-2, bx, by);
		player.y_drag = posHumanY(player.currentframe-2, bx, by);
	};
	
	let x_y = moveHuman(player.currentframe);
	let xn = x_y[0];
	let yn = x_y[1];
	let ok = true;

	for(let j = 0; ok && j < 10; j++){
		if (j != player.currentframe-2){   
			if (posHumanX(j, bx, by) == xn && posHumanY(j, bx, by) == yn){
				ok = false;
				xn = player.x_drag;
				yn = player.y_drag;
			};
		};
	};

	player.x_drag = xn;
	player.y_drag = yn;
		
	showHuman(player.currentframe, xn, yn);
}

function copyTact() {
	tact_buf = [];
	for (h = 0; h < PLAYER_NO; h++) {
		tact_buf[h] = tact_arr[bx][by][h];
	}
	copied = true;
}

function pasteTact() {
	if (!copied) {
		return;
	}
	for (h = 0; h < PLAYER_NO; h++) {
		tact_arr[bx][by][h] = tact_buf[h];
	}
	showAll(true);

}

function mirrorTact() {
	let ibx2;
	for (let ibx = 0; ibx <= 2; ibx++) {
		for (let iby = 0; iby <= 6; iby++) {
			for (let h = 0; h < PLAYER_NO; h++) {
				ibx2 = 4 - ibx;
				swapObject(tact_arr[ibx][iby][h], tact_arr[ibx2][iby][h]);
			}
		}
	}
	for (let ibx = 0; ibx <= 4; ibx++) {
		for (let iby = 0; iby <= 6; iby++) {
			for (let h = 0; h < PLAYER_NO; h++) {
				x = tact_arr[ibx][iby][h].x;
				tact_arr[ibx][iby][h].x = RES_X - x;
			}
		}
	}
}

function updateHuman(_nr) {
	moveHuman(_nr);
	const hum = _players[_nr];
	hum.active = true;
	let h = fromNumber(hum.currentframe);
	tact_arr[bx][by][h].y = y;
	tact_arr[bx][by][h].x = x;
}

const checkBx = function(bx) {
	if (bx < 0) bx = 0;
	if (bx > 4) bx = 4;
	return Math.floor(bx);
};

const checkBy = function(by) {
	if (by < 0) by = 0;
	if (by > 6) by = 6;
	return Math.floor(by);
};
const checkHuman = function(h) {
	return Math.floor(h);
};

function posHumanX(h, bx, by) {
	by = checkBy(by);
	bx = checkBx(bx);
	h = checkHuman(h);
	return tact_arr[bx][by][h].x;
}

function posHumanY(h, bx, by) {
	by = checkBy(by);
	bx = checkBx(bx);
	h = checkHuman(h);
	return tact_arr[bx][by][h].y;
}

function setPlayers() {
	for (let h = 0; h < PLAYER_NO; h++) {
		const x = posHumanX(h, bx, by);
		const y = posHumanY(h, bx, by);
		const no = toNumber(h);
		showHuman(no, x, y);
		_players[no].gotoAndStop(no);
	}
}


function showTactSub() {
    const tempBX = (ball.x - MOV_X) / WID * 5.0;
    const tempBY = (ball.y - MOV_Y) / HEI * 7.0;

    let bxr = Math.floor(tempBX);
    let byr = Math.floor(tempBY);

    let bxr2 = Math.round(tempBX);
    let byr2 = Math.round(tempBY);
    if (bxr === bxr2) bxr2--;
    if (byr === byr2) byr2--;

    let sx;
    let sy;
    let no;
    let _txt = '';
    for (let h = 0; h < 10; h++) {
        sx = 0.0;
        sy = 0.0;
        let x = posHumanX(h, bxr, byr);
        let y = posHumanY(h, bxr, byr);
        sx += x * (1.0 - Math.abs(tempBX - bxr - 0.5)) * (1.0 - Math.abs(tempBY - byr - 0.5));
        sy += y * (1.0 - Math.abs(tempBX - bxr - 0.5)) * (1.0 - Math.abs(tempBY - byr - 0.5));

        x = posHumanX(h, bxr2, byr);
        y = posHumanY(h, bxr2, byr);
        sx += x * (1.0 - Math.abs(tempBX - bxr2 - 0.5)) * (1.0 - Math.abs(tempBY - byr - 0.5));
        sy += y * (1.0 - Math.abs(tempBX - bxr2 - 0.5)) * (1.0 - Math.abs(tempBY - byr - 0.5));

        x = posHumanX(h, bxr, byr2);
        y = posHumanY(h, bxr, byr2);
        sx += x * (1.0 - Math.abs(tempBX - bxr - 0.5)) * (1.0 - Math.abs(tempBY - byr2 - 0.5));
        sy += y * (1.0 - Math.abs(tempBX - bxr - 0.5)) * (1.0 - Math.abs(tempBY - byr2 - 0.5));

        x = posHumanX(h, bxr2, byr2);
        y = posHumanY(h, bxr2, byr2);
        sx += x * (1.0 - Math.abs(tempBX - bxr2 - 0.5)) * (1.0 - Math.abs(tempBY - byr2 - 0.5));
        sy += y * (1.0 - Math.abs(tempBX - bxr2 - 0.5)) * (1.0 - Math.abs(tempBY - byr2 - 0.5));

        no = toNumber(h);
        showHuman(no, sx, sy);
    }
}

function showAll(mod) {
	bx = checkBx((ball.x - MOV_X) / WID * 5.0);
	by = checkBy((ball.y - MOV_Y) / HEI * 7.0);
	setBall(mod);
	setPlayers();
}

function scrollActive(dx, dy) {
	let _nr = findActive();
	let hum = _players[_nr];
	let h = fromNumber(hum.currentframe);

	let x = tact_arr[bx][by][h].x + dx;
	let y = tact_arr[bx][by][h].y + dy;
	x = limitHumanX(x);
	y = limitHumanY(y);
	showHuman(_nr, x, y);

	tact_arr[bx][by][h].x = x;
	tact_arr[bx][by][h].y = y;
}

////////////
// Terrion
////////////

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

// Example: showTactic('QAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQAQARARARARARARARARARARARARARARARARARARARARARARARARARARARARARARARARARARARAQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBQBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRBRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCRCBGBGBGBHBHCCCFCGCHCIDDDFDGDHDIEDEFEGEHEIGDGFGGGHGIHDHFHGHHHIMBIFIGIHIHBHBHBIBIBICGCHCICJCMDGDHDIDJDLEGEHEIEJELGGGHGIGJGLHGHHHIHJHLIHIHIIIJMNCDCEBFBGBGDDECCECFCGGCGDDEDFDGHCHDEEEFEGJCJDGEGFGGKCKDIEIFIGOELDLEKFPHBIBIBJCKCLCICJCKEMDLDIDJDKGLGMEIEJEKHLHMGIGJGKJLJMIIIJIKKLKMPHKJLKLLOKHEHFHHHJHKFDIFIHIJFLJEJFJHJJJKKEKFKHKJKKLELGLHLILKNFNGNHNINJPGOGOHOIPI');
function showTactic(t) {
	if (t) {
		tact = t;
		tact_arr = tactToArray(tact);
		createPlayers();
		createBall();
//		createButtons();
		showAll(true);
		drawAll();
		
		$(ob).mousemove(function(e) {
			_mouseX = e.pageX - ob.offset().left;
			_mouseY = e.pageY - ob.offset().top;
			drawAll();
		});
	}
}

// ----------------- START PROGRAM, Ladownie danych
ctx = createCanvas();
loadPlayer();

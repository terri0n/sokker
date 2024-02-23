<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no, shrink-to-fit=no">
	<title>SETE Mobile</title>
	<script src='https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js'></script>
	<script src='https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js'></script>
	<%-- This version fixes a bug that didn't let me select a single player with the mobile --%>
	<script type="text/javascript" src='${pageContext.request.contextPath}/js/jquery.ui.touch-punch.js'></script>
 	<link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/smoothness/jquery-ui.css">
	<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7610610063984650" crossorigin="anonymous"></script>
	<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	
	<style>
		body {
			color: white;
			background-color: #428D26;
			padding: 0;
			margin: 0;
		}
		html {
			touch-action: pan-x pan-y;
			-webkit-text-size-adjust: none;
		}
		.court {
		    width: 334px;
		    height: 480px;
		    position: relative;
		    display: inline-block;
		    background-image: url("https://sokker.org/static/pic/boisko.svg");
		    background-position: center;
		    background-repeat: no-repeat;
		}
		.block {
			width: 63px;
			height: 62px;
			float: left;
			z-index: 1;
		}
		.blocks {
			position: absolute;
			padding-left: 7px;
			padding-top: 19px;
			width: 315px;
			height: 430px;
		}
		.box {
			width: 20px;
			height: 26px;
			float: left;
			z-index: 2;
		}
		.pitch {
			padding-left: 14px;
			padding-top: 22px;
			width: 305px;
			height: 420px;
		}
		.player {
			position: absolute;
			z-index: 3;
			width: 20px;
			height: 28px;
		    display: inline-block;
		    background-image: url("https://sokker.org/static/pic/human.svg");
		    background-position: center;
		    background-repeat: no-repeat;
		    color: white;
		    font-size: 8px;
    		cursor: pointer;
		}
		.player span {
    		font-weight: bold; 
			position: relative;
    		top: -4px;
    		left: 1px;
    		text-shadow: 1px 1px black;
    		cursor: default;
		}
		.ball {
			position: absolute;
			z-index: 4;
			width: 20px;
			height: 20px;
		    display: inline-block;
    		cursor: pointer;
    		font-size: 10px;
		}
		.shadow {
			background-color: #005500;
		}
  		.boton {
  			display: inline-block;
			color: black;
			background-color: lightgray; 
			border: 1px solid gray;
			border-radius: 2px;
			padding: 1px;
			min-width: 16px;
			min-height: 16px;
			cursor: pointer;
			margin: 2px;
  		}
		.boton:hover {
			background-color: cyan; 
			border-color: Darkcyan;
		}

		#mix {
			display: none;
			position: absolute;
			background-color: #62AD46;
			border: 1px solid black;
			border-radius: 5px;
			box-shadow: 0.3em 0.3em 0.5em #082133;
			z-index: 5;
		}

		#mix label {
			white-space: nowrap;
			margin-right: 10px;
		}
		
		#login {
			display: inline-block;
			text-align: right;
			background-color: #62AD46;
			padding: 5px;
			border-radius: 5px;
		}
		
		.error {
			color: yellow;
			font-weight: bolder;
		}
		
		.tactics {
			height: 446px;
			overflow-y: scroll;
			background-color: #62AD46;
			padding: 5px;
			border-radius: 5px;
			text-align: left;
		}

		.help {
			font-weight: bolder;
			color: black;
			background-color: yellow;
			border: 1px solid black;
			cursor: pointer;
			border-radius: 12px;
			padding-left: 5px;
			padding-right: 5px;
		}
		.help:hover {
			color: yellow;
			background-color: red;
		}
		.help_popup {
			position: absolute;
			right: 0px;
			color: black;
			background-color: #ffffbf;
			border: 1px solid black;
			text-align: left;
			display: none;
			margin: auto;
			overflow-y: auto;
			overflow-x: visible;
			padding: 10px;
			text-align: justify;
			text-justify: inter-word;
			z-index: 999;
			border-radius: 10px;
  		}
		.close-button {
		  float: right;
		  margin-left: 10px;
		  padding: 2px 5px 2px 5px;
		  background-color: transparent;
		  border: none;
		  font-size: 16px;
		  cursor: pointer;
		  border-radius: 5px;
		  z-index: 1000;
		}
		
		.close-button:hover {
		  color: yellow;
		  background-color: red;
		}
		
		.ui-selecting {
			background-color: yellow;
			opacity: 0.9;
		}
		.ui-selected {
			background-color: yellow;
			opacity: 0.9;
		}
		.ui-draggable-dragging {
			opacity: 0.5;
		}

		@media only screen and (max-width: 800px) {
			.solo_pc {
				display: none;
			}
		}
		
		button span {
			vertical-align: middle;
		}
		.material-icons {
			font-size: 16px;
		}
		.md-24 {
			font-size: 24px;
		}
		
	</style>

	<!-- Global site tag (gtag.js) - Google Analytics -->
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-131138380-1"></script>
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments);}
	  gtag('js', new Date());
	
	  gtag('config', 'UA-131138380-1');
	</script>
</head>
<body>
	<div style="background-color: darkgreen; text-align: center;">
		<b>Sokker Extended Tactic Editor for Mobiles!</b>
		<span class="help" title="Help">?</span>
		<div class="help_popup">
			<button class="close-button" onclick="$('.help_popup').hide();"><span class="material-icons">close</span></button>
			<%@include file="ayuda_sete.html" %>
		</div>
	</div>

	<div style="position: absolute; width:250px;height:800px;" class="solo_pc">
		<!-- Display-columna -->
		<ins class="adsbygoogle"
		     style="display:block"
		     data-ad-client="ca-pub-7610610063984650"
		     data-ad-slot="1565843826"
		     data-ad-format="auto"></ins>
		<script>
		     (adsbygoogle = window.adsbygoogle || []).push({});
		</script>
	</div>
	<div style="position: absolute; right: 0px; width:250px;height:800px;text-align: right;" class="solo_pc">
		<!-- Display-columna -->
		<ins class="adsbygoogle"
		     style="display:block"
		     data-ad-client="ca-pub-7610610063984650"
		     data-ad-slot="1565843826"
		     data-ad-format="auto"></ins>
		<script>
		     (adsbygoogle = window.adsbygoogle || []).push({});
		</script>
	</div>

	<div style="text-align: center;">
		<div style="display: inline;">
			<div style="padding: 5px; display: inline-block; vertical-align: top;">
				<div id="login">
					<c:if test="${not empty sessionScope.login}">
						<div style="text-align: center; margin: 5px;">
							Logged as <b>${sessionScope.login}</b>
							<br /><br />
							<button title="Logout" type="button" onclick="location.href='${pageContext.request.contextPath}/sete/logout'"><span class="material-icons">logout</span> Logout</button>
						</div>
					</c:if>
					
					<c:if test="${empty sessionScope.login}">
						<form name="form_login" method="post" onsubmit="return form_submit();">
							<input type="hidden" id="confirmed" name="confirmed" value="${pageContext.request.serverName == 'localhost' ? '1' : ''}" />
							Login <input type="text" name="ilogin" size="10">
							<br/>
							Password <input type="password" name="ipassword" size="10">
							<br/>
							<input type="submit" />
							<br/>
							<span class="error"><c:out value="${param.error}" /></span>
						</form>
					</c:if>
				</div>
				<br /><br />

<form method="get" action="https://sokker.org/tactedit.php">
				<div class="tactics">
					<c:forEach items="${tacticas}" varStatus="s" var="t">
						<c:if test="${not empty t[1]}">
							<label for="tact_${s.count}">
								<input type="radio" id="tact_${s.count}" name="id" onclick="tactic_click($(this))" value="${t[0]}" data-tact="${t[2]}" />${t[1]}
							</label>
						</c:if>
						<br />
					</c:forEach>
					<label for="manual">
						<input type="radio" name="id" id="manual" checked onclick="$(this).attr('data-tact').val($('#tact').val());" />
						<input type="text" size="4" placeholder="TactID" onclick="$('#manual').prop('checked', true);" onchange="$('#manual').val($(this).val())" />
					</label>
					<button title="Open" onclick="open_click(); return false;"><span class="material-icons">folder_open</span></button>
				</div>
				
				<br />					
				<button title="Save" type="submit"><span class="material-icons">save</span> Save</button>
			</div>		
			
			<div style="display: inline-block;">
				<div class="court">
					<div class="blocks">
						<%-- They should be in the pitch but I don't know why multiple selection doesn't work there --%>
						<div class="main player" id="player2"><span>2</span></div>
						<div class="main player" id="player3"><span>3</span></div>			
						<div class="main player" id="player4"><span>4</span></div>			
						<div class="main player" id="player5"><span>5</span></div>			
						<div class="main player" id="player6"><span>6</span></div>			
						<div class="main player" id="player7"><span>7</span></div>			
						<div class="main player" id="player8"><span>8</span></div>			
						<div class="main player" id="player9"><span>9</span></div>			
						<div class="main player" id="player10"><span>10</span></div>			
						<div class="main player" id="player11"><span>11</span></div>			
					</div>
					<div class="pitch">
						<div class="ball" id="ball">⚽</div>
					</div>
				</div>

				<div>
					<input type="hidden" name="save" value="1" />
					<button title="Mirror" onclick="mirror_click(); return false;"><span class="material-icons md-24" >flip</span></button>
					<button title="Swap" onclick="swap_click(); return false;"><span class="material-icons md-24">flip_camera_android</span></button>
					<button title="Mix" onclick="mix_click($(this), event); return false;"><span class="material-icons md-24">shuffle</span></button>
					<button title="Copy" onclick="copy_click(); return false;"><span class="material-icons md-24">content_copy</span></button>
					<button title="Paste" onclick="paste_click(); return false;"><span class="material-icons md-24">content_paste</span></button>
					<br /><br />
					<textarea name="tact" id="tact" rows="9" cols="42" onclick="select()" onchange="show_tactic()"></textarea>
</form>
					<br /><br />
					<button onclick="location.href='../';"><span class="material-icons">arrow_circle_left</span> BACK</button>
				</div>
			</div>			
		</div>
				
		<div id="mix">
			<div style="background-color: green; text-align: center;">
				<b>Choose players to mix with your tactic</b>
			</div>

			<div style="padding: 5px; padding-bottom: 0px; display: inline;">
				<div class="tactics" style="display: inline-block; vertical-align: top;">
					<label for="tact0"><input type="radio" id="tact0" name="tact2" value="" onclick="custom_click($(this));" />Custom</label><br />
					<c:forEach items="${tacticas}" varStatus="s" var="t">
						<c:if test="${not empty t[1]}">
							<label for="tact${s.count}">
								<input type="radio" id="tact${s.count}" onclick="tactic2_click($(this))" checked name="tact2" value="${t[2]}" />${t[1]}
							</label>
						</c:if>
						<br />
					</c:forEach>
				</div>

				<div style="display: inline-block;">
					<div class="court">
						<div class="blocks">
							<%-- They should be in the pitch but I don't know why multiple selection doesn't work there --%>
							<div class="bench player" id="player12"><span>2</span></div>
							<div class="bench player" id="player13"><span>3</span></div>			
							<div class="bench player" id="player14"><span>4</span></div>			
							<div class="bench player" id="player15"><span>5</span></div>			
							<div class="bench player" id="player16"><span>6</span></div>			
							<div class="bench player" id="player17"><span>7</span></div>			
							<div class="bench player" id="player18"><span>8</span></div>			
							<div class="bench player" id="player19"><span>9</span></div>			
							<div class="bench player" id="player20"><span>10</span></div>			
							<div class="bench player" id="player21"><span>11</span></div>			
						</div>
						<div class="pitch"></div>
					</div>
					
					<br />
					<button title="Mirror" onclick="mirror2_click(); return false;"><span class="material-icons md-24">flip</span></button>
					&nbsp;
					<button title="Swap" onclick="swap2_click(); return false;"><span class="material-icons md-24">flip_camera_android</span></button>
				</div>
			</div>

			<div style="padding: 5px; text-align: right;">
				<input type="button" value="Accept" onclick="accept_click()" />
				<input type="button" value="Cancel" onclick="$('#mix').hide();" />
			</div>
		</div>
	</div>

	<br />

	<!-- InArticle -->
	<ins class="adsbygoogle"
	     style="display:block; text-align:center;"
	     data-ad-layout="in-article"
	     data-ad-format="fluid"
	     data-ad-client="ca-pub-7610610063984650"
	     data-ad-slot="7009191822"></ins>
	<script>
	     (adsbygoogle = window.adsbygoogle || []).push({});
	</script>

	<script type="text/javascript">
		function start() {
			// Help button
			$(document).mouseup(function(e) {
			    var container = $(".help_popup");

			    if ($(".help").is(e.target) || $(".help").has(e.target).length > 0) {
			    	container.css("display", "inline-block");
			    } else if (!container.is(e.target) && container.has(e.target).length === 0) {
			    	// if the target of the click isn't the container nor a descendant of the container
			    	container.slideUp('fast');
			    }
			});
			
			// Add invisible blocks and boxes where the elements will be able to drop
			for (let i = 6; i >= 0; i--) {
				for (let j = 0; j < 5; j++) {
				    let html = '<div class="block" id="block' + (i * 5 + j) + '"></div>';
				    $('.blocks').append(html);
				}
			}
	
			for (let i = 15; i >= 0; i--) {
				for (let j = 0; j < 15; j++) {
				    let html = '<div class="box" id="box' + (i * 15 + j) + '"></div>';
				    $('.pitch').append(html);
				}
			}
	
			// Place the ball in the center
			move_ball(3 * 5 + 2);

			// Show default tactic
			$('#tact').val('${param.tact}');
			show_tactic();

			// Add jQueryUI events
			add_events();
		}
		
		// Add jQueryUI events to ball and players
		function add_events() {
		    $('#mix').draggable();
		    
			$('.ball').draggable({
				containment: "parent",
				zIndex: 1,
				revert: true,
				drag(ev, ui) {
				    //console.log($('#block30').position(), $('#block31').position(), $('#block25').position());
				    let left = (ui.position.left - ($('#block30').position().left - 8)) / $('#block30').width() - 0.5;
				    let top = (ui.position.top - ($('#block30').position().top - 8)) / $('#block30').height() - 0.5;
				    //console.log(left, top);
				    show_tactic2(left, top);
				}
			});
	
			$('.block').droppable({
		    	accept: ".ball",
		    	tolerance: 'intersect',
		    	classes: { "ui-droppable-hover" : "shadow" },
		    	drop: function(event, ui) {
			        let pos = parseInt($(this).attr('id').split('block')[1]);
//	 console.log(ui.position);
			        move_ball(pos);
			        show_tactic();
			        
			        return $(ui.draggable).draggable('option', 'revert', false);
		    	}
		    });

			// NOTE: if I don't let bench players to be draggable, they get unselected when I click on other player
		    $('.player').draggable({
				containment: "parent",
				zIndex: 1,
				revert: true,
				helper: function() {
					// If there are players selected, I return a clone opf them so that we can see them all moving
				    if ($('.main.ui-selected').length) {
					    let div = $('<div style="display: inline-block; margin-left: -' + $(this).position().left + 'px; margin-top: -' + $(this).position().top + 'px"></div>');
			            div.append($('.main.ui-selected').clone());
			            return div;
		            } else {
		                return $(this);
		            }
		        }
			});

			$('.box').droppable({
		    	accept: ".main.player",
		    	tolerance: 'intersect',
		    	classes: { "ui-droppable-hover" : "shadow" },
		    	drop: function(event, ui) {
		    	    let j = parseInt($(ui.draggable).attr('id').split('player')[1]);
		    	    let pos = parseInt($(this).attr('id').split('box')[1]);

		    	    // If there are players selected...
		    	    if ($('.main.ui-selected').length) {
						// Calculate the difference in the moved player. That difference will be applied to all of the selected players
		    	        let old_pos = parseInt($(ui.draggable).attr('data-pos'));
	    	            let dif = pos - old_pos;
						move_players($('.main.ui-selected'), dif);
		    	    } else {
			    	    move_player(j, pos);
		    	    }
		    	    
			        update_tactic();
		    	    
			        return $(ui.draggable).draggable('option', 'revert', false);
		    	}
		    });
			
			$('.blocks').selectable({
			    filter: ".player"
			});
			
			// NOTE: click doesn't work if I don't do it explicitly
			$('.player').click(function() {
			   $(this).toggleClass('ui-selected') ;
			});
		}
		
		// Update the tactic in the text area when a player is moved
		function update_tactic() {
		    let old_tact = $('#tact').val();
	    
		    let tact = '';
			for (let i = 2; i <= 11; i++) {
				for (let j = 0; j < 7 * 5; j++) {
				    if (j == parseInt($('#ball').attr('data-pos'))) {
					    // If we are in the tactic of the ball, we get the current players positions
					    let pos = parseInt($('#player' + i).attr('data-pos'));
			            let y = 'A'.charCodeAt(0) + parseInt(pos / 15);
			            let x = 'A'.charCodeAt(0) + (pos % 15);
			            let yx = String.fromCharCode(y) + String.fromCharCode(x);
						tact += yx;
				    } else {
				        // Otherwise, we keep the current tactic position
				        tact += old_tact.substring(tact.length, tact.length + 2);
				    }
				}
			}

			$('#tact').val(tact);
		}

		// Show the tactic in the area where the ball is placed, between blocks
		function show_tactic2(left, top) {
		    let array = split_tactic($('#tact').val());

		    // Limit the input
		    left = Math.max(0, left);
		    left = Math.min(3.99999, left);
		    top = Math.max(0, top);
		    top = Math.min(5.99999, top);
		    
		    for (let p = 0; p < 10; p++) {
				// Calculate the weighted average of the 4 positions around the coordinates
			    let coords_tl = get_coords(array[p][(6 - parseInt(top)) * 5 + parseInt(left)]);
			    let coords_tr = get_coords(array[p][(6 - parseInt(top)) * 5 + parseInt(left + 1)]);
			    let coords_bl = get_coords(array[p][(6 - parseInt(top + 1)) * 5 + parseInt(left)]);
			    let coords_br = get_coords(array[p][(6 - parseInt(top + 1)) * 5 + parseInt(left + 1)]);
		        
			    let x_percentage = 1 - (left - parseInt(left));
			    let y_percentage = 1 - (top - parseInt(top));
			    
			    let x_top = (x_percentage * coords_tl.left + (1 - x_percentage) * coords_tr.left);
			    let y_top = (x_percentage * coords_tl.top + (1 - x_percentage) * coords_tr.top);
			    let x_bottom = (x_percentage * coords_bl.left + (1 - x_percentage) * coords_br.left);
			    let y_bottom = (x_percentage * coords_bl.top + (1 - x_percentage) * coords_br.top);
			    let x = (y_percentage * x_top + (1 - y_percentage) * x_bottom);
			    let y = (y_percentage * y_top + (1 - y_percentage) * y_bottom);
		    
			    // Move the player to the calculated point
		        move_player_coord(p + 2, x, y);
		    }
		}
		    
		// Get the coordinates of a YX position of a tactic
		function get_coords(yx) {
		    let pos_y = yx.charCodeAt(0) - 'A'.charCodeAt(0);
		    let pos_x = yx.charCodeAt(1) - 'A'.charCodeAt(0);
		    let pos = pos_y * 15 + pos_x;

		    return $('#box' + pos).position() || $('#box0').position();
		}
		
		// Show the tactic in the area where the ball is placed
		function show_tactic() {
			// Clean the string
			let tact = $('#tact').val().trim();
			if (tact.includes("&")) {
				tact = tact.split("&")[0];
			}
			if (tact.includes("=")) {
				tact = tact.split("=")[1];
			}
			
		    if (tact) {
				$('#tact').val(tact);
		    	let array = split_tactic(tact);
			    
			    for (let p = 0; p < 10; p++) {
			        let yx = array[p][$('#ball').attr('data-pos')];
			        let y = yx.charCodeAt(0) - 'A'.charCodeAt(0);
			        let x = yx.charCodeAt(1) - 'A'.charCodeAt(0);
			        move_player(p + 2, y * 15 + x);
			    }

			    $('.player').show();
			} else {
			    $('.player').hide();
			}
		}
		
		// Move a player
		function move_player(player, pos) {
		    if ($('#box' + pos).position()) {
				$('#player' + player).css('left', $('#box' + pos).position().left);
		        $('#player' + player).css('top', $('#box' + pos).position().top);
	    	    $('#player' + player).attr('data-pos', pos);
		    }
		}

		// Move a player to a coordinate
		function move_player_coord(player, left, top) {
		    $('#player' + player).css('left', left);
	        $('#player' + player).css('top', top);
		}

		// Move selected players
		function move_players(players, offset) {
	        players.each(function(i, p) {
	            let old_pos = parseInt($(p).attr('data-pos'));
	            let player = parseInt($(p).attr('id').split('player')[1]);
	            move_player(player, old_pos + offset);
	        });
		}

		// Move the ball
		function move_ball(pos) {
	        $('#ball').css('left', $('#block' + pos).position().left + 20);
	        $('#ball').css('top', $('#block' + pos).position().top + 23);
	        $('#ball').attr('data-pos', pos);
		}
		
		function show_preview(tact) {
		    let array = split_tactic(tact);
		    
		    for (let p = 0; p < 10; p++) {
		        let x = 0;
		        let y = 0;
				// Calculate the average of every position
		        for (let i = 0; i < 35; i++) {
			        let coords = get_coords(array[p][i]);
			        if (coords) {
				        y += coords.top;
				        x += coords.left;
			        }
		        }
		        move_player_coord(p + 12, parseInt(x / 35), parseInt(y / 35));
		    }
		}
		
		// Split the 700 letters tactic into 11 players with 35 yx coordinates
		function split_tactic(tact) {
			let array = new Array();
			let pos = 0;
			for (let i = 0; i < 10; i++) {
			    let player = new Array();
				for (let j = 0; j < 7 * 5; j++) {
				    player.push(tact.substring(pos, pos + 2));
					pos += 2;
				}
				array.push(player);
			}

			return array;
		}

		/*
		 * type: '.main' or '.bench'
		*/
		function swap_players(type, input) {
		    if ($(type + '.ui-selected').length == 2) {
		        // We get both coded and decoded tactic. We'll use the one that's easier at every time
			    let old_tact = input.val();
		        let old_tact_arr = split_tactic(old_tact);
			    
			    let tact = '';
			    let offset = (type == '.main') ? 0 : 10;
				for (let i = offset + 2; i <= offset + 11; i++) {
					for (let j = 0; j < 7 * 5; j++) {
					    if ($('#player' + i).hasClass('ui-selected')) {
						    // If the player is selected...
					        
					        // Find the other player
					        let other = $(type + '.ui-selected')[0] === $('#player' + i)[0] ? $(type + '.ui-selected')[1] : $(type + '.ui-selected')[0];
					        let num_other = parseInt($(other).attr('id').split('player')[1]);

						    // And get his position
					        tact += old_tact_arr[(num_other - 2) % 10][j];
					    } else {
					        // Otherwise, we keep the current tactic position
					        tact += old_tact.substring(tact.length, tact.length + 2);
					    }
				    }
				}
				
				$(type + '.ui-selected').removeClass('ui-selected');
				input.val(tact);
		    } else {
		        alert('Select 2 players to swap their positions');
		    }
		}

		// Mirror the selected players
		function mirror_players(type, input) {
		    if ($(type + '.ui-selected').length) {
		        // We get both coded and decoded tactic. We'll use the one that's easier at every time
			    let old_tact = input.val();
			    let old_tact_arr = split_tactic(old_tact);
			    
			    let tact = '';
			    let offset = (type == '.main') ? 0 : 10;
				for (let i = offset + 2; i <= offset + 11; i++) {
					for (let j = 0; j < 7 * 5; j++) {
					    if ($('#player' + i).hasClass('ui-selected')) {
						    // If the player is selected...
					        let pos_x = 4 - (j % 5);	// We look at the mirror box. Otherwise, players will move in the opposite direction of the ball
						    let pos_y = parseInt(j / 5);
					        let old_yx = old_tact_arr[(i - 2) % 10][pos_y * 5 + pos_x];

						    let y = old_yx[0];
				            let x = 'O'.charCodeAt(0) - (old_yx.charCodeAt(1) - 'A'.charCodeAt(0));	// And the x coordinate
				            let yx = y + String.fromCharCode(x);
							tact += yx;
					    } else {
					        // Otherwise, we keep the current tactic position
					        tact += old_tact.substring(tact.length, tact.length + 2);
					    }
					}
				}

				input.val(tact);
		    } else {
		        alert('No players selected');
		    }
		}

		function show_reading_tactics() {
			$(`<div style="z-index: 999;">
					<img style="opacity: 0.8; position: fixed; top: 0; display:block; width:100%; height:100%; object-fit: cover;" src="https://raqueto.com/sokker/img/cargando.gif"/>
					<h1 class="borde" style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -125%); color: white;">Reading tactics...</h1>
				</div>`).appendTo(document.body);
		}
		
		/**********************
		 *** EVENT HANDLERS ***
		 **********************/
		
		// Mirror the selected players
		function mirror_click() {
			mirror_players('.main', $('#tact'));
		    show_tactic();
		}

		// Swap the 2 selected players
		function swap_click() {
		    swap_players('.main', $('#tact'));
			show_tactic();
		}
		
		function open_click() {
		    window.open('https://sokker.org/read_tact.php?id=' + $('#manual').val(), 'tactica', 'fullscreen=no,width=1600,height=100,location=no,menubar=no,scrollbars=no,status=no,titlebar=no,toolbar=no');
		}
		
		function mix_click(button, event) {
		    $('#mix').show();
		    $('#mix').css('left', Math.max(0, (button.offset().left - $('#mix').width())) + "px");
		    $('#mix').css('top', Math.max(0, (button.offset().top + 50 - $('#mix').height())) + "px");
		    tactic2_click($('#mix input[type=radio]:checked'));
		}
		
		function tactic_click(radio) {
			$('#tact').val(radio.attr('data-tact'));
			show_tactic();
		}

		function tactic2_click(radio) {
			show_preview(radio.val());
		}

		// Combine the selected players into the main tactic
		function accept_click() {
		    $('#mix').hide();
		    let mix_tact_arr = split_tactic($('input[name="tact2"]:checked').val());
		    let old_tact_arr = split_tactic($('#tact').val());

		    let tact = '';
			for (let i = 12; i <= 21; i++) {
			    if ($('#player' + i).hasClass('ui-selected')) {
			        // If the player is selected...
			        tact += mix_tact_arr[i - 12].join('');
			    } else {
			        // Otherwise, we keep the current tactic position
			        tact += old_tact_arr[i - 12].join('');
			    }
			}

			$('#tact').val(tact);
			show_tactic();

			// Select new players mixed
		    $('.main.ui-selected').removeClass('ui-selected');
		    $('.bench.ui-selected').each(function(i, player) {
		        let id = parseInt($(this).attr('id').split('player')[1]);
		        $('#player' + (id - 10)).addClass('ui-selected');
		    });
		}
	
		// Ask for a tactic to show in the auxiliary pitch
		function custom_click(radio) {
		    let tact = prompt('Enter tactic');
		    if (tact) {
			    radio.val(tact);
		    }
		    show_preview(radio.val());
		}
		
		function swap2_click() {
		    let input = $('input[name="tact2"]:checked');
		    swap_players('.bench', input);
		    show_preview(input.val());
		}
		
		function mirror2_click() {
		    let input = $('input[name="tact2"]:checked');
			mirror_players('.bench', input);
		    show_preview(input.val());
		}

		let copied = [];
		function copy_click() {
	        copied = [];
		    if ($('.main.ui-selected').length) {
		        for (let i = 2; i <= 11; i++) {
					if ($('#player' + i).hasClass('ui-selected')) {
					    copied.push($('#player' + i).attr('data-pos'));
					} else {
					    copied.push(null);
					}
				}
		        $('.main.ui-selected').removeClass('ui-selected');
		    } else {
		        alert('No players selected');
		    }
		}
		
		function paste_click() {
			if (copied.length) {
			    for (let i = 2; i <= 11; i++) {
					if (copied[i - 2]) {
					    move_player(i, copied[i - 2]);
					    $('#player' + i).addClass('ui-selected');
					} else {
					    $('#player' + i).removeClass('ui-selected');
					}
				}
		        update_tactic();
			} else {
			    alert('No players copied');
			}
		}

		function form_submit() {
			if ($("form[name='form_login'] input[name='confirmed']").val() == '1') {
				show_reading_tactics();
				return true;
			} else {
				$("form[name='form_login'] input[type='submit']").attr("disabled", true);
				let data = 'ilogin=' + encodeURIComponent($("form[name='form_login'] input[name='ilogin']").val()) + '&ipassword=' + encodeURIComponent($("form[name='form_login'] input[name='ipassword']").val());
				$.post('https://sokker.org/start.php?session=xml&' + data, data).done(
			        function(data) {
						$("form[name='form_login'] input[type='submit']").attr("disabled", false);
			        	if (data.startsWith('OK')) {
			        		$("form[name='form_login'] input[name='confirmed']").val('1');
							$("form[name='form_login'] input[type='submit']").click();
			            } else {
			            	alert('Error in login or password');
			            }
			        } 
			    ).fail(function() {
			    	alert('Error: you need to use a browser with CORS restriction disabled. Read the Sokker Asistente FAQ for more details');
			    });

				return false;
			}
		}
		
		$(function() {
			start();
		});
	</script>
</body>
</html>
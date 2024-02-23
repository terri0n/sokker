<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<style>
		.help {
			cursor: pointer;
		}
		.help:hover {
			color: yellow;
		}
		.popup {
			color: black;
			background-color: #ffffbf;
			border: 1px solid #1d4866;
			border-radius: 5px;
			position: absolute;
			white-space: nowrap;
			padding: 5px;
			z-index: 100;
			margin-top: -42px;
			font-size: 16px;
		}
		#sumskills input[type="number"] {
 			width:35px;
		}
		#config {
			position: relative;
			text-align: left;
			padding: 5px;
			display: none;
			white-space: nowrap;
			background-color: rgba(0,0,0,0.1);
			z-index: 5;	/* fontawesome tiene 4 */
		}
	</style>
	
	<script>
		$(function() {
			$(".help").click(function() {
				$(this).parent().append("<div id='editar' class='sombra popup'>&nbsp; " + $(this).attr("title") + " &nbsp;</div>");
			});
		});
		
		function config_click() {
			if ($('#config').is(":hidden")) {
				$('#config').css("display", "inline-block");
				$('#hide_config').show();
				$('#show_config').hide();
				$('#config').parent().parent().css("display", "inline-block");
			} else {
				$('#config').parent().parent().css("display", "block");
				$('#config').hide();
				$('#show_config').show();
				$('#hide_config').hide();
			}
		}
	</script>
</head>

<body style="text-align: center;">
	<div style="text-align: left;">
		<div class="flecha" onclick="config_click();">
			<span id="show_config">▶</span>
			<span id="hide_config" style="display: none;">▼</span>
			<fmt:message key="menu.configuration" />
		</div>
		
		<div id="config">
			<form action="${pageContext.request.contextPath}/asistente/actualizar_configuracion?juveniles=${param.juveniles}&historico=${param.historico}" onsubmit="$(this).find('button[type=submit]').prop('disabled', true);">						
				<b><fmt:message key="common.training" />:</b>
				<hr class="hr" />
	
				<div>
					<label for="numeros" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.numeros ? 'checked' : ''}" /> id="numeros" name="numeros"><fmt:message key="menu.show_skill_numbers" />
					</label><br/>
					<span class="peque"><fmt:message key="menu.percentages" />:</span><br/>
					<table class="peque fondo_tabla tabla">
						<tr>
							<td><fmt:message key="config.age_factor" /></td>
							<td><input type="number" id="factor_edad" name="factor_edad" min="0" max="2" step="0.001" value="${sessionScope.usuario.factor_edad}" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_age_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.age_pace_factor" /></td>
							<td><input type="number" id="factor_edad_rapidez" name="factor_edad_rapidez" min="0" max="2" step="0.001" value="${sessionScope.usuario.factor_edad_rapidez}" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_age_pace_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.skill_factor" /></td>
							<td><input type="number" id="factor_habilidad" name="factor_habilidad" min="0" max="2" step="0.001" value="${sessionScope.usuario.factor_habilidad}" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_skill_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.talent_factor" /></td>
							<td><input type="number" id="factor_talento" name="factor_talento" min="0" max="2" step="0.001" value="${sessionScope.usuario.factor_talento}" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_talent_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.general_factor" /></td>
							<td><input type="number" id="factor_residual" name="factor_residual" min="0" max="2" step="0.001" value="${sessionScope.usuario.factor_residual}" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_general_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.formation_factor" /></td>
							<td><input type="number" id="factor_formacion" name="factor_formacion" min="0" max="2" step="0.001" value="${sessionScope.usuario.factor_formacion}" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_formation_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.pace" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_rapidez" name="factor_rapidez" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_rapidez}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_pace_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.technique" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_tecnica" name="factor_tecnica" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_tecnica}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_technique_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.passing" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_pases" name="factor_pases" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_pases}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_passing_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.keeper" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_porteria" name="factor_porteria" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_porteria}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_keeper_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.defender" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_defensa" name="factor_defensa" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_defensa}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_defender_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.playmaker" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_creacion" name="factor_creacion" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_creacion}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_playmaker_factor" />">help</div>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="config.factor"><fmt:param><fmt:message key="skills.striker" /></fmt:param></fmt:message></td>
							<td><input type="number" id="factor_anotacion" name="factor_anotacion" min="0" max="1" step="0.001" value="<c:out value="${sessionScope.usuario.factor_anotacion}" />" />
								<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_striker_factor" />">help</div>
							</td>
						</tr>
					</table>
				</div>
				
				<br/>
				
				<b><fmt:message key="common.players" />:</b><br/>
				<hr class="hr" />
	
				<div>
					<label for="mostrar_salario" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_salario ? 'checked' : ''}" /> id="mostrar_salario" name="mostrar_salario"><fmt:message key="menu.show_wage" />
					</label><br/>
					<label for="mostrar_experiencia" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_experiencia ? 'checked' : ''}" /> id="mostrar_experiencia" name="mostrar_experiencia"><fmt:message key="menu.show_experience" />
					</label><br/>
					<label for="mostrar_disciplina_tactica" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_disciplina_tactica ? 'checked' : ''}" /> id="mostrar_disciplina_tactica" name="mostrar_disciplina_tactica"><fmt:message key="menu.show_tactic_discipline" />
					</label><br/>
					<label for="mostrar_trabajo_en_equipo" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_trabajo_en_equipo ? 'checked' : ''}" /> id="mostrar_trabajo_en_equipo" name="mostrar_trabajo_en_equipo"><fmt:message key="menu.show_team_work" />
					</label><br/>
					<label for="mostrar_altura" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_altura ? 'checked' : ''}" /> id="mostrar_altura" name="mostrar_altura"><fmt:message key="menu.show_height" />
					</label><br/>
					<label for="mostrar_peso" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_peso ? 'checked' : ''}" /> id="mostrar_peso" name="mostrar_peso"><fmt:message key="menu.show_weight" />
					</label><br/>
					<label for="mostrar_IMC" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_IMC ? 'checked' : ''}" /> id="mostrar_IMC" name="mostrar_IMC"><fmt:message key="menu.show_BMI" />
					</label><br/>
					<label for="mostrar_suma_habilidades" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_suma_habilidades ? 'checked' : ''}" /> id="mostrar_suma_habilidades" name="mostrar_suma_habilidades"><fmt:message key="menu.show_sumskills" />
					</label><br/>
					<label for="mostrar_banquillo" class="peque">
						<input type="checkbox" <c:out value="${sessionScope.usuario.mostrar_banquillo ? 'checked' : ''}" /> id="mostrar_banquillo" name="mostrar_banquillo"><fmt:message key="menu.show_bench" />
					</label><br/>
					
					<c:if test="${sessionScope.usuario.def_tid > 1000}">
						<label for="ntdb" class="peque">
							<input type="checkbox" <c:out value="${sessionScope.usuario.ntdb ? 'checked' : ''}" /> id="ntdb" name="ntdb"><fmt:message key="menu.send_players" />
						</label><br/>
					</c:if>
					<c:if test="${sessionScope.usuario.def_tid < 1000}">
						<label for="recibir_ntdb" class="peque">
							<input type="checkbox" <c:out value="${sessionScope.usuario.recibir_ntdb ? 'checked' : ''}" /> id="recibir_ntdb" name="recibir_ntdb"><fmt:message key="menu.get_players" />
						</label><br/>
					</c:if>
	
					<span class="peque"><fmt:message key="skills.sumskills" />:</span><br/>
					<table class="peque fondo_tabla tabla" style="text-align: center;" id="sumskills">
						<thead>
							<tr>
								<th>&nbsp;</th>
								<th>GK</th>
								<th>DEF</th>
								<th>MID</th>
								<th>ATT</th>
							</tr>
						</thead>
						<tr>
							<td><fmt:message key="skills.pace" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_rapidez" name="sumskills_gk_rapidez" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_rapidez}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_rapidez" name="sumskills_def_rapidez" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_rapidez}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_rapidez" name="sumskills_mid_rapidez" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_rapidez}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_rapidez" name="sumskills_att_rapidez" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_rapidez}" />" /></td>
						</tr>
						<tr>
							<td><fmt:message key="skills.technique" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_tecnica" name="sumskills_gk_tecnica" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_tecnica}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_tecnica" name="sumskills_def_tecnica" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_tecnica}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_tecnica" name="sumskills_mid_tecnica" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_tecnica}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_tecnica" name="sumskills_att_tecnica" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_tecnica}" />" /></td>
						</tr>
						<tr>
							<td><fmt:message key="skills.passing" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_pases" name="sumskills_gk_pases" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_pases}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_pases" name="sumskills_def_pases" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_pases}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_pases" name="sumskills_mid_pases" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_pases}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_pases" name="sumskills_att_pases" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_pases}" />" /></td>
						</tr>
						<tr>
							<td><fmt:message key="skills.keeper" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_porteria" name="sumskills_gk_porteria" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_porteria}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_porteria" name="sumskills_def_porteria" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_porteria}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_porteria" name="sumskills_mid_porteria" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_porteria}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_porteria" name="sumskills_att_porteria" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_porteria}" />" /></td>
						</tr>
						<tr>
							<td><fmt:message key="skills.defender" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_defensa" name="sumskills_gk_defensa" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_defensa}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_defensa" name="sumskills_def_defensa" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_defensa}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_defensa" name="sumskills_mid_defensa" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_defensa}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_defensa" name="sumskills_att_defensa" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_defensa}" />" /></td>
						</tr>
						<tr>
							<td><fmt:message key="skills.playmaker" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_creacion" name="sumskills_gk_creacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_creacion}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_creacion" name="sumskills_def_creacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_creacion}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_creacion" name="sumskills_mid_creacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_creacion}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_creacion" name="sumskills_att_creacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_creacion}" />" /></td>
						</tr>
						<tr>
							<td><fmt:message key="skills.striker" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_gk_anotacion" name="sumskills_gk_anotacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_gk_anotacion}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_def_anotacion" name="sumskills_def_anotacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_def_anotacion}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_mid_anotacion" name="sumskills_mid_anotacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_mid_anotacion}" />" /></td>
							<td><input type="number" min="0" max="1000" id="sumskills_att_anotacion" name="sumskills_att_anotacion" value="<fmt:parseNumber value="${sessionScope.usuario.sumskills_att_anotacion}" />" /></td>
						</tr>
					</table>
				</div>
				
				<c:if test="${sessionScope.usuario.def_tid < 1000}">
					<br/>
					
					<b><fmt:message key="config.scouts" />:</b><br/>
					<hr class="hr" />
	
					<div>
						<input type="text" name="scouts" value="${sessionScope.usuario.scouts}" />
						<div class="material-icons rojo borde-blanco help vertical" title="<fmt:message key="config.info_scouts" />">help</div>
					</div>
				</c:if>
								
				<br/>
				<div align="center">
					<button type="submit"><fmt:message key="common.save" /></button>
				</div>
			</form>
		</div>
	</div>
</body>
</html>
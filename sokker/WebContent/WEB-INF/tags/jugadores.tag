<%@tag import="java.util.List"%>
<%@tag import="com.formulamanager.sokker.bo.AsistenteBO"%>
<%@tag import="com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO"%>
<%@tag import="com.formulamanager.sokker.entity.Usuario"%>
<%@tag import="com.formulamanager.sokker.entity.Jugador"%>
<%@ attribute name="demarcacion" type="com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE" required="false" %>
<%@ attribute name="titulo" type="java.lang.String" required="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/mistags.tld" prefix="tags" %>
<%@ tag pageEncoding="UTF-8" %>

<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />

	<c:set var="lista" value="<%= AsistenteBO.filtrar_demarcacion((List<Jugador>)request.getAttribute(\"jugadores\"), demarcacion) %>" />

	<thead>
		<tr>
			<th class="cabecera_jugadores fijo"
				<c:if test="${empty sessionScope.juveniles}">
					style="cursor: pointer;" onclick="jugadores_click('${demarcacion}')"
				</c:if>
			>
				<c:if test="${empty sessionScope.juveniles}">
					<span class="material-icons borde nohist" title="<fmt:message key="training.show_training" />" style="opacity: 0.4; color: white;">directions_run</span>
					<span class="titulo uppercase"><fmt:message key="${titulo}" /> (<c:out value="${lista.size()}" />)</span>
				</c:if>
				<c:if test="${not empty sessionScope.juveniles}">
					<span class="titulo uppercase"><fmt:message key="common.juniors" /> (<c:out value="${jugadores.size()}" />)</span>
				</c:if>
			</th>
		</tr>
	
		<tr class="cabecera">
			<td style="border-top-left-radius: 5px;">&nbsp;</td>
			<c:if test="${not empty sessionScope.juveniles}">
				<td class="ordenar es_texto" onclick="ordenar_click($(this), '')"><fmt:message key="skills.age" /><span class="span_ordenar"></span></td>
				<td class="ordenar" onclick="ordenar_click($(this), '')"><fmt:message key="common.level" /><span class="span_ordenar"></span></td>
				<td class="ordenar es_texto" onclick="ordenar_click($(this), '')"><fmt:message key="common.position" /><span class="span_ordenar"></span></td>
				<td class="ordenar" onclick="ordenar_click($(this), '')"><fmt:message key="common.weeks" /><span class="span_ordenar" style="display: inline;">▾</span></td>
				<td class="ordenar" onclick="ordenar_click($(this), '')"><fmt:message key="skills.talent" /><span class="span_ordenar"></span></td>
				<td nowrap style="border-top-right-radius: 5px;" class="ordenar" onclick="ordenar_click($(this), '')"><fmt:message key="common.expected_level" /><span class="span_ordenar"></span></td>
				<td class="ordenar" onclick="ordenar_click($(this), '')"><fmt:message key="juniors.total_weeks" /><span class="span_ordenar" style="display: inline;"></span></td>
				<td class="ordenar" onclick="ordenar_click($(this), '')"><fmt:message key="juniors.pops" /><span class="span_ordenar" style="display: inline;"></span></td>
				<!-- Editar/borrar -->
			</c:if>
			<c:if test="${empty sessionScope.juveniles}">
				<td class="ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.age" /><span class="span_ordenar"></span></td>
				<c:if test="${not empty sessionScope.usuario}">
					<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.value" /><span class="span_ordenar" style="display: inline;">▾</span></td>
					<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.form" /><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.stamina" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.pace" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.technique" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.passing" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.keeper" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.defender" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.playmaker" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.striker" /></tags:string><span class="span_ordenar"></span></td>
					<td class="uppercase ordenar noentr nont" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.talent" /></tags:string><span class="span_ordenar"></span></td>
					<c:if test="${sessionScope.usuario.mostrar_salario}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.wage" /><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_experiencia}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.experience" /></tags:string><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_disciplina_tactica}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.tactical_discipline" /></tags:string><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_trabajo_en_equipo}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.team_work" /></tags:string><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_altura}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.height" /><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_peso}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.weight" /><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_IMC}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="skills.BMI" /><span class="span_ordenar"></span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_suma_habilidades}">
						<td class="noentr ordenar" onclick="ordenar_click($(this), '${demarcacion}')"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.sumskills" /></tags:string><span class="span_ordenar"></span></td>
					</c:if>
				</c:if>
				<td class="nt ordenar es_texto" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="common.date" /><span class="span_ordenar"></span></td>
				<c:if test="${not empty sessionScope.usuario}">
					<td class="nt ordenar es_texto" onclick="ordenar_click($(this), '${demarcacion}')"><fmt:message key="players.reliable" /><span class="span_ordenar"></span></td>
					<td style="border-top-right-radius: 5px;">&nbsp;</td><!-- Editar -->
				</c:if>
			</c:if>
		</tr>
	</thead>
	
	<c:forEach var="j" items="${lista}" varStatus="status">
		<tr class="${status.count % 2 == 0 ? 'par' : 'impar'}" id="jugadores${demarcacion}">
			<c:if test="${empty sessionScope.juveniles}">
				<td nowrap align="left">
					<input type="checkbox" id="check${j.pid}" class="nt" />
					<c:if test="${sessionScope.usuario.def_tid > 1000}">
						<span id="span${j.pid}" title="<c:if test='${j.entrenamiento_avanzado}'><fmt:message key="training.advanced_training" /></c:if><c:if test='${!j.entrenamiento_avanzado}'><fmt:message key="training.formation_training" /></c:if>" class="flecha ${j.entrenamiento_avanzado ? 'avanzado' : ''} nont" onclick="desplegar_click(${j.pid}, true)">▶</span>
					</c:if>
					<img class="nont" src="https://files.sokker.org/pic/flags/${j.pais}.png" />
					<c:if test="${j.nt > 0}">
						<span class='estrella'>&#x2605;</span>
					</c:if>
					<a style="color: ${j.color};" target="_blank" href="https://sokker.org/player/PID/${j.pid}">${j.destacar ? '<b>' : ''}${j.nombre}${j.destacar ? '</b>' : ''}</a>

					<%-- 3 tarjetas = roja. Si hay más (amarillas) se limpian tras el partido de sanción --%>
					<c:if test="${j.tarjetas > 2}">
						<span class="material-icons borde" style="color: red; font-size: 0.6em; transform: scaleY(1.4);">square</span>
					</c:if>
					
					<c:if test="${j.tarjetas <= 2}">
						<c:forEach var="i" begin="${1}" end="${j.tarjetas}"><span class="material-icons borde" style="color: yellow; font-size: 0.6em; transform: scaleY(1.4);">square</span></c:forEach>
					</c:if>
					
					<c:if test="${j.lesion > 7}">
						<span title="<fmt:message key="players.injured" />" class="sombra_corta" style="color: red; background-color: white; font-size: 0.8em; border: 1px solid black; padding-left: 1px; padding-right: 1px;">&#10010;</span><span style="font-size: 0.7em; font-weight: bolder;">(<c:out value="${j.lesion}" />)</span>
					</c:if>
					<c:if test="${j.lesion > 0 && j.lesion <= 7}">
						<span title="<fmt:message key="players.slightly_injured" />" class="material-icons borde vertical" style="color: burlywood;">healing</span><span style="font-size: 0.7em; font-weight: bolder;">(<c:out value="${j.lesion}" />)</span>
					</c:if>
					<c:if test="${j.en_venta == 1}">
						<span title="<fmt:message key="players.on_sale" />" class="material-icons borde vertical" style="font-size: 18px; color: lightgreen">attach_money</span>
					</c:if>
					<c:if test="${j.en_venta == 2}">
						<span title="<fmt:message key="players.transferable" />" class="material-icons vertical" style="color: green">currency_exchange</span>
					</c:if>
				</td>
			</c:if>
			<c:if test="${not empty sessionScope.juveniles}">
				<td nowrap align="left">${j.nombre}</td>
				<td nowrap style="text-align: left">${j.edad_proyectada}</td>
				<td class="${j.clase_nivel}" title="${j.title_nivel}"><span class="span_grafica"
					<c:if test="${j.num_jornadas > 1}">
						onclick="grafica_ajax($(this), 'talento', ${j.pid});"
					</c:if>
				>${j.nivel}</span></td>
				<td><fmt:message key="${j.jugador_campo ? 'players.outfield' : 'players.gk'}" /></td>
				<td>${j.semanas}</td>
				<td>${j.talento}</td>
				<td class="${j.clase_talento}">${j.nivel_proyectado}</td>
				<td>${j.total_semanas}</td>
				<td>${j.nivel - j.nivel_inicial}</td>
<%-- 				<c:if test="${not empty sessionScope.admin}"> --%>
					<td class="nohist"><span title="<fmt:message key="common.edit" />" class="material-icons boton" style="color: darkblue;" onclick="editar_juvenil_click($(this), ${j.pid}, '${j.nombre}', '${j.getNiveles(null)}')">edit</span></td>
<%-- 				</c:if> --%>
			</c:if>
			<c:if test="${empty sessionScope.juveniles}">
				<td class="edad">${j.edad}</td>
				<c:if test="${not empty sessionScope.usuario}">
					<td class="${j.clase_valor} noentr" title="${j.title_valor}" align="right"><span class="span_grafica" onclick="grafica_ajax($(this), 'valor', ${j.pid});"><fmt:formatNumber value="${j.getValor_pais()}" type="number" /></span></td>
					<td class="${j.clase_forma} noentr forma" title="${j.title_forma}"><span class="span_grafica" onclick="grafica_ajax($(this), 'forma', ${j.pid});">${j.forma}</span></td>
					<td class="habilidad ${j.clase_condicion} condicion" title="${j.title_condicion}"><span class="span_grafica" onclick="grafica_ajax($(this), 'condicion', ${j.pid});">${j.condicion}</span></td>
					<td class="habilidad ${j.clase_rapidez} rapidez"><span class="span_grafica" onclick="grafica_ajax($(this), 'rapidez', ${j.pid});">${j.rapidez}</span></td>
					<td class="habilidad ${j.clase_tecnica} tecnica"><span class="span_grafica" onclick="grafica_ajax($(this), 'tecnica', ${j.pid});">${j.tecnica}</span></td>
					<td class="habilidad ${j.clase_pases} pases"><span class="span_grafica" onclick="grafica_ajax($(this), 'pases', ${j.pid});">${j.pases}</span></td>
					<td class="habilidad ${j.clase_porteria} porteria"><span class="span_grafica" onclick="grafica_ajax($(this), 'porteria', ${j.pid});">${j.porteria}</span></td>
					<td class="habilidad ${j.clase_defensa} defensa"><span class="span_grafica" onclick="grafica_ajax($(this), 'defensa', ${j.pid});">${j.defensa}</span></td>
					<td class="habilidad ${j.clase_creacion} creacion"><span class="span_grafica" onclick="grafica_ajax($(this), 'creacion', ${j.pid});">${j.creacion}</span></td>
					<td class="habilidad ${j.clase_anotacion} anotacion"><span class="span_grafica" onclick="grafica_ajax($(this), 'anotacion', ${j.pid});">${j.anotacion}</span></td>
					<td class="noentr nont col_talento">
						<c:if test="${not empty j.juvenil}">
							<span class="span_grafica negro"
								<c:if test="${j.juvenil.num_jornadas > 1}">
									onclick="grafica_ajax($(this), 'talento', ${j.juvenil.pid}, 1);"
								</c:if>
							>${j.juvenil.talento}</span>
						</c:if>
					</td>
					<c:if test="${sessionScope.usuario.mostrar_salario}">
						<td class="noentr" align="right"><fmt:formatNumber value="${j.salario_pais}" type="number" /></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_experiencia}">
						<td class="noentr ${j.clase_experiencia}"><span class="span_grafica" onclick="grafica_ajax($(this), 'experiencia', ${j.pid});">${j.experiencia}</span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_disciplina_tactica}">
						<td class="noentr ${j.clase_disciplina_tactica}"><span class="span_grafica" onclick="grafica_ajax($(this), 'disciplina_tactica', ${j.pid});">${j.disciplina_tactica}</span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_trabajo_en_equipo}">
						<td class="noentr ${j.clase_trabajo_en_equipo}"><span class="span_grafica" onclick="grafica_ajax($(this), 'trabajo_en_equipo', ${j.pid});">${j.trabajo_en_equipo}</span></td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_altura}">
						<td class="noentr">${j.altura}</td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_peso}">
						<td class="noentr">${j.peso / 10}</td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_IMC}">
						<td class="noentr">${j.IMC / 100}</td>
					</c:if>
					<c:if test="${sessionScope.usuario.mostrar_suma_habilidades}">
						<td class="noentr"><span class="span_grafica" onclick="grafica_ajax($(this), 'suma_habilidades', ${j.pid});">${j.suma_habilidades}</span></td>
					</c:if>
				</c:if>
				
				<td nowrap class="nt nojuv ${j.clase_fecha}"><fmt:formatDate value="${j.fecha}" pattern="yyyy-MM-dd" /></td>
				
				<c:if test="${not empty sessionScope.usuario}">
					<td nowrap class="nt negrita nojuv" style="color: green"><c:out value="${j.actualizado ? '&#10004;' : ''}" escapeXml="false" /></td>
					<td class="nojuv" style="white-space: nowrap;"><%--
					--%><c:if test="${empty j.notas}">
							<span class="material-icons blanco borde vertical" style="opacity: 0.5;">chat_bubble</span><%--
					--%></c:if><%--
					--%><c:if test="${not empty j.notas}">
							<span class="material-icons blanco borde vertical" style="opacity: 0.5;" title="${j.notas}">message</span><%--
					--%></c:if><%--
	
					--%><span class="material-icons boton nohist vertical" title="<fmt:message key="common.edit" />" style="color: darkblue;" onclick="editar_click($(this), ${j.pid})">edit</span><%--
						
					--%><span class="material-icons borde-morado boton vertical" style="color: pink;" title="<fmt:message key="players.project_skills" />" onclick="proyectar_click($(this), ${j.edad}, ${j.jornadaProyeccion}, '${j.condicion}', '${j.rapidez}', '${j.tecnica}', '${j.pases}', '${j.porteria}', '${j.defensa}', '${j.creacion}', '${j.anotacion}', <%= ((Jugador)jspContext.getAttribute("j")).getEntrenamientoExtra() %>, '<fmt:formatNumber value="${j.talento_medio}" minFractionDigits="${sessionScope.usuario.numero_decimales}" maxFractionDigits="${sessionScope.usuario.numero_decimales}" />', false)">calculate</span><%--
	
					--%><span class="material-icons boton vertical" title="<fmt:message key="menu.export" />" onclick="exportar_jugador_click($(this), ${j.pid});">code</span><%--
	
					--%><c:if test="${j.pid <= 0 || sessionScope.usuario.def_tid < 1000}"><span class="material-icons boton gris vertical" title="<fmt:message key="common.remove" />" onclick="if (confirm('<fmt:message key="players.remove_player"><fmt:param value="${j.nombre}"/></fmt:message>')) location.href='${pageContext.request.contextPath}/asistente/borrar_jugadores?pids=${j.pid},';">delete</span></c:if><%--
					
					--%><div id="editar${j.pid}" class="menu sombra" style="position: absolute; display: none; text-align: center; z-index: 100">
							<div class="cabecera">
								<b>${j.nombre}</b>
							</div>
	
							<div class="fin_bloque doble editar">
								<form method="post" action="${pageContext.request.contextPath}/asistente/grabar" onsubmit="$(this).find('input[type=submit]').prop('disabled', true);">
									<input type="hidden" name="pid" value="${j.pid}" />
									
									<table style="text-align: left" class="fondo_tabla">
										<tr class="nt">
											<td><fmt:message key="skills.stamina" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'stamina', j.condicion, sessionScope.usuario.numeros, 11)}</td><td><fmt:message key="skills.keeper" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'keeper', j.porteria, sessionScope.usuario.numeros)}</td>
										</tr>
										<tr class="nt">
											<td><fmt:message key="skills.pace" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'pace', j.rapidez, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.defender" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'defender', j.defensa, sessionScope.usuario.numeros)}</td>
										</tr>
										<tr class="nt">
											<td><fmt:message key="skills.technique" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'technique', j.tecnica, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.playmaker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'playmaker', j.creacion, sessionScope.usuario.numeros)}</td>
										</tr>
										<tr class="nt">
											<td><fmt:message key="skills.passing" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'passing', j.pases, sessionScope.usuario.numeros)}</td><td><fmt:message key="skills.striker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'striker', j.anotacion, sessionScope.usuario.numeros)}</td>
										</tr>
										<tr class="nt" style="background-color: transparent;">
											<td colspan="4">&nbsp;</td>
										</tr>
										<tr style="background-color: transparent;">
											<td colspan="3">
												<fmt:message key="common.position" />: <c:out value="${j.despDemarcacion_asistente(j.demarcacion, sessionScope.usuario)}" escapeXml="false"/>
												<fmt:message key="common.color" />: <input type="color" name="color" value="${j.color}" />
												<label class="nt" for="fiable${j.pid}">
													<input type="checkbox" <c:out value="${j.actualizado ? 'checked' : ''}" /> id="fiable${j.pid}" name="fiable" /><fmt:message key="players.reliable" />
												</label>
											</td>
											<td>
												<label for="destacar${j.pid}">
													<input type="checkbox" <c:out value="${j.destacar ? 'checked' : ''}" /> id="destacar${j.pid}" name="destacar" /><fmt:message key="players.highlight" />
												</label>
											</td>
										</tr>
									</table>
										
									<div align="left">
										<fmt:message key="common.notes" />:<br/>
										<textarea name="notas" rows="5" style="width: 100%"><c:out value="${empty j.notas ? '' : j.notas}" escapeXml="false" /></textarea>
									</div>
			
									<input type="submit" value="<fmt:message key="common.update" />" />
								</form>
							</div>
						</div><%--
						
				--%></td>
				</c:if>
			</c:if>
		</tr>
		
		<%-- ENTRENAMIENTO --%>

		<c:if test="${sessionScope.usuario.def_tid > 1000 && empty sessionScope.juveniles}">
			<input type="hidden" id="talento${j.pid}" value="${j.talento_medio}" />
			<tbody id="tbody${j.pid}" style="display: none;">
				<c:forEach begin="${j.min_edad}" end="${j.edad}" var="edad">
					<tr id="tr${j.pid}_${edad}" style="background-color: rgba(240, 240, 240, ${edad == j.edad ? 1 : 0.7}); opacity: 0.8; display: ${edad < j.edad - 2 ? 'none' : ''}">
						<td nowrap>
							<c:if test="${edad == j.edad}">
								<div style="display: inline-block; text-align: left">
									<span class="material-icons boton_minutos" title="<fmt:message key="training.training_intensity_this_week" />">schedule</span> <span class="minutos"><fmt:formatNumber value="${j.minutos}" maxFractionDigits="0"/></span>% ${j.demarcacion_entrenamiento}
									<br/>
		 							<span class="nont" title="<fmt:message key="training.minimum_maximum_talent" />">
		 								<span class="material-icons talento" style="vertical-align: top">diamond</span>
		 								<c:if test="${empty j.talento}">
		 									<fmt:formatNumber value="${j.talento_min}" minFractionDigits="${sessionScope.usuario.numero_decimales}" maxFractionDigits="${sessionScope.usuario.numero_decimales}" /> / <fmt:formatNumber value="${j.talento_max}" minFractionDigits="${sessionScope.usuario.numero_decimales}" maxFractionDigits="${sessionScope.usuario.numero_decimales}" />
		 								</c:if>
		 								<c:if test="${not empty j.talento}">
		 									<fmt:formatNumber value="${j.talento}" />
		 								</c:if>
		 							</span>
		 						</div>
							</c:if>
							
							<span class="flecha_arr gris md-24" style="display: ${edad > j.min_edad && edad == j.edad - 2 ? '' : 'none'}" onclick="mover_entrenamiento_click(${j.pid}, -1, false)">▲</span>
							<span class="flecha_aba gris md-24" style="display: none" onclick="mover_entrenamiento_click(${j.pid}, 1, false)">▼</span>
						</td>
						<td>${edad}</td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Condicion, (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Rapidez,   (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Tecnica,   (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Pases,     (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Porteria,  (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Defensa,   (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Creacion,  (Integer)jspContext.getAttribute("edad")) %></td>
						<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Anotacion, (Integer)jspContext.getAttribute("edad")) %></td>
						<td class="nohist" colspan="${sessionScope.usuario.def_tid < 1000 ? 3 : 1}">
							<span class="flecha_arr gris md-24" style="display: ${edad > j.min_edad && edad == j.edad - 2 ? '' : 'none'}" onclick="mover_entrenamiento_click(${j.pid}, -1, false)">▲</span>
							<span class="flecha_aba gris md-24" style="display: none" onclick="mover_entrenamiento_click(${j.pid}, 1, false)">▼</span>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</c:if>
	</c:forEach>
<!-- </table> -->

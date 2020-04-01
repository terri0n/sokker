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
<%@ taglib uri="/WEB-INF/tags/mistags.tld" prefix="tags" %>
<%@ tag pageEncoding="UTF-8" %>

<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />

<thead>
	<tr>
		<th colspan="100" class="cabecera_jugadores">
			<c:if test="${empty param.juveniles}">
				<i class="fas fa-running borde nohist nont" title="<fmt:message key="training.show_training" />" style="opacity: 0.4; color: white; cursor: pointer;" onclick="jugadores_click('${demarcacion}')"></i>
				<span class="titulo borde"><fmt:message key="${titulo}" /> (<%= AsistenteBO.filtrar_demarcacion((List<Jugador>)session.getAttribute("jugadores"), demarcacion).size() %>)</span>
			</c:if>
			<c:if test="${not empty param.juveniles}">
				<span class="titulo borde uppercase"><fmt:message key="common.juniors" /> (<c:out value="${jugadores.size()}" />)</span>
			</c:if>
		</th>
	</tr>

<%-- <table style="width: 100%; text-align: center; background-color: #1d4866; border-spacing: 1px;" id="jugadores${demarcacion}"> --%>
		<tr class="borde sombra">
			<th>&nbsp;</th>
			<th><fmt:message key="skills.age" /></th>
			<c:if test="${not empty param.juveniles}">
				<th><fmt:message key="common.level" /></th>
				<th><fmt:message key="common.position" /></th>
				<th><fmt:message key="common.weeks" /></th>
				<th><fmt:message key="skills.talent" /></th>
			</c:if>
			<c:if test="${empty param.juveniles}">
				<th class="noentr"><fmt:message key="skills.value" /></th>
				<th class="noentr"><fmt:message key="skills.form" /></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.stamina" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.pace" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.technique" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.passing" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.keeper" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.defender" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.playmaker" /></tags:string></th>
				<th class="uppercase"><tags:string begin_index="0" end_index="3"><fmt:message key="skills.striker" /></tags:string></th>
				<th class="nt"><fmt:message key="common.date" /></th>
				<th class="nt"><fmt:message key="players.reliable" /></th>
				<th class="nohist">&nbsp;</th><!-- Editar/borrar -->
			</c:if>
		</tr>
	</thead>
	
	<c:set var="lista" value="<%= AsistenteBO.filtrar_demarcacion((List<Jugador>)session.getAttribute(\"jugadores\"), demarcacion) %>" />
	
	<c:forEach var="j" items="${lista}" varStatus="status">
		<tr class="${status.count % 2 == 0 ? 'par' : 'impar'} sombra" id="jugadores${demarcacion}">
			<td nowrap align="left">
				<c:if test="${empty param.juveniles}">
					<span id="span${j.pid}" title="<fmt:message key="common.training" />" class="flecha nont" onclick="desplegar_click(${j.pid})">▶</span>
					<img class="nont" src="https://files.sokker.org/pic/flags/${j.pais}.png" />
					<c:if test="${j.nt > 0}">
						<i class="estrella fas fa-star" style="font-size: 0.6em"></i>
					</c:if>
					<a target="_blank" href="http://sokker.org/player/PID/${j.pid}">${j.nombre}</a>
				</c:if>

				<span class="juv">${j.nombre}</span>

				<%-- 3 tarjetas = roja. Si hay más (amarillas) se limpian tras el partido de sanción --%>
				<c:if test="${j.tarjetas > 2}">
					<i class="fas fa-square borde" style="color: red; font-size: 0.6em; transform: scaleY(1.4);"></i>
				</c:if>
				
				<c:if test="${j.tarjetas <= 2}">
					<c:forEach var="i" begin="${1}" end="${j.tarjetas}"><i class="fas fa-square borde" style="color: yellow; font-size: 0.6em; transform: scaleY(1.4);"></i></c:forEach>
				</c:if>
				
				<c:if test="${j.lesion > 7}">
					<span class="fa-stack fa-1x" style="margin: -12px;">
						<i class="far fa-plus-square fa-stack-1x borde" style="color: red;"></i>
						<i class="fas fa-plus-square fa-stack-1x" style="color: white;" title="<fmt:message key="players.injured" />"></i>
					</span><span style="font-size: 0.7em; font-weight: bolder;">(<c:out value="${j.lesion}" />)</span>
				</c:if>
				<c:if test="${j.lesion > 0 && j.lesion <= 7}">
					<i title="<fmt:message key="players.slightly_injured" />" class="fa fa-band-aid borde" style="color: burlywood; vertical-align: middle;"></i><span style="font-size: 0.7em; font-weight: bolder;">(<c:out value="${j.lesion}" />)</span>
				</c:if>
				<c:if test="${j.en_venta == 1}">
					<i title="<fmt:message key="players.on_sale" />" class="fa fa-usd borde" style="color: lightgreen"></i>
				</c:if>
				<c:if test="${j.en_venta == 2}">
					<span class="fa-stack fa-1x" style="margin: -12px;">
						<i class="fas fa-comment fa-stack-1x borde" style="color: green;"></i>
						<i class="fas fa-comment-dollar fa-stack-1x" style="color: white;" title="<fmt:message key="players.transferable" />"></i>
					</span>
				</c:if>
			</td>
			<td class="nojuv">${j.edad}</td>
			<c:if test="${not empty param.juveniles}">
				<fmt:formatNumber pattern="#.#" var="talento">${j.m == 'NaN' || j.m <= 0 || j.num_jornadas < 5 ? '' : 1/j.m}</fmt:formatNumber>
				
				<td nowrap style="text-align: left">${j.edad_proyectada}</td>
				<td class="${j.clase_nivel}" title="${j.title_nivel}"><span class="span_grafica" onclick="drawChart($(this), 'talento', ['<fmt:message key="players.talent_of"><fmt:param value="${j.nombre}" /></fmt:message>: ${talento}', '<fmt:message key="common.maximum" />', '<fmt:message key="common.minimum" />', '<fmt:message key="common.level" />'], ${j.datos_grafica_nivel});">${j.nivel}</span></td>
				<td><fmt:message key="${j.jugador_campo ? 'players.outfield' : 'players.gk'}" /></td>
				<td>${j.semanas}</td>
				<td>${talento}</td>
			</c:if>
			<c:if test="${empty param.juveniles}">
				<td class="${j.clase_valor} noentr nojuv" title="${j.title_valor}" align="right"><span class="span_grafica" onclick="drawChart($(this), 'valor', ['<fmt:message key="skills.value" />', '<fmt:message key="players.original_value" />'], ${j.getDatos_grafica_valor(usuario.countryID)});"><fmt:formatNumber value="${j.getValor_pais(usuario.countryID)}" type="number" /></span></td>
				<td class="${j.clase_forma} noentr nojuv" title="${j.title_forma}"><span class="span_grafica" onclick="drawChart($(this), 'forma', ['<fmt:message key="skills.form" />'], ${j.datos_grafica_forma});">${j.forma}</span></td>
				<td class="${j.clase_condicion} nojuv" title="${j.title_condicion}"><span class="span_grafica" onclick="drawChart($(this), 'condicion', ['<fmt:message key="skills.stamina" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_condicion((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.condicion}</span></td>
				<td class="${j.clase_rapidez} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'rapidez', ['<fmt:message key="skills.pace" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_rapidez((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.rapidez}</span></td>
				<td class="${j.clase_tecnica} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'tecnica', ['<fmt:message key="skills.technique" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_tecnica((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.tecnica}</span></td>
				<td class="${j.clase_pases} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'pases', ['<fmt:message key="skills.passing" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_pases((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.pases}</span></td>
				<td class="${j.clase_porteria} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'porteria', ['<fmt:message key="skills.keeper" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_porteria((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.porteria}</span></td>
				<td class="${j.clase_defensa} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'defensa', ['<fmt:message key="skills.defender" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_defensa((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.defensa}</span></td>
				<td class="${j.clase_creacion} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'creacion', ['<fmt:message key="skills.playmaker" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_creacion((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.creacion}</span></td>
				<td class="${j.clase_anotacion} nojuv"><span class="span_grafica" onclick="drawChart($(this), 'anotacion', ['<fmt:message key="skills.striker" />'], <%= ((Jugador)jspContext.getAttribute("j")).getDatos_grafica_anotacion((Usuario)request.getSession().getAttribute("usuario")) %>);">${j.anotacion}</span></td>
				<td nowrap class="nt nojuv"><fmt:formatDate value="${j.fecha}" pattern="dd-MM-yyyy" /></td>
				<td nowrap class="nt negrita nojuv" style="color: green"><c:out value="${j.actualizado ? '&#10004;' : ''}" escapeXml="false" /></td>
				<td nowrap class="nohist nojuv"><%--
				--%><c:if test="${empty j.notas}">
						<i class="far fa-comment" style="opacity: 0.5;"></i><%--
				--%></c:if><%--
				--%><c:if test="${not empty j.notas}">
						<i class="fas fa-comment-dots" style="opacity: 0.5;" title="${j.notas}"></i><%--
				--%></c:if><%--

				--%><i class="fa fa-edit boton" title="<fmt:message key="common.edit" />" style="color: darkblue;" onclick="editar_click($(this), ${j.pid})"></i><%--
					
				--%><i class="fas fa-calculator boton pink" style="color: purple;" title="<fmt:message key="players.project_skills" />" onclick="proyectar_click($(this), ${j.edad}, ${j.jornadaMod}, '${j.condicion}', '${j.rapidez}', '${j.tecnica}', '${j.pases}', '${j.porteria}', '${j.defensa}', '${j.creacion}', '${j.anotacion}', <%= ((Jugador)jspContext.getAttribute("j")).getEntrenamientoExtra((Usuario)request.getSession().getAttribute("usuario")) %>, '<fmt:formatNumber value="${(j.talento_min + j.talento_max) / 2}" pattern="#.#" />', false)"></i><%--

				--%><i class="nt fa fa-trash-o boton" title="<fmt:message key="common.remove" />" onclick="if (confirm('<fmt:message key="players.remove_player"><fmt:param value="${j.nombre}"/></fmt:message>')) location.href='${pageContext.request.contextPath}/asistente/borrar?pid=${j.pid}'"></i><%--
				
				--%><div id="editar${j.pid}" class="menu sombra" style="position: absolute; display: none; text-align: center; z-index: 100">
						<div class="cabecera borde">
							<b>${j.nombre}</b>
						</div>

						<div class="bloque">
							<form method="post" action="${pageContext.request.contextPath}/asistente/grabar">
								<input type="hidden" name="pid" value="${j.pid}" />
								
								<table style="text-align: left">
									<tr class="nt">
										<td><fmt:message key="skills.stamina" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'stamina', j.condicion, usuario.numeros, 11)}</td><td><fmt:message key="skills.keeper" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'keeper', j.porteria, usuario.numeros)}</td>
									</tr>
									<tr class="nt">
										<td><fmt:message key="skills.pace" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'pace', j.rapidez, usuario.numeros)}</td><td><fmt:message key="skills.defender" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'defender', j.defensa, usuario.numeros)}</td>
									</tr>
									<tr class="nt">
										<td><fmt:message key="skills.technique" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'technique', j.tecnica, usuario.numeros)}</td><td><fmt:message key="skills.playmaker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'playmaker', j.creacion, usuario.numeros)}</td>
									</tr>
									<tr class="nt">
										<td><fmt:message key="skills.passing" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'passing', j.pases, usuario.numeros)}</td><td><fmt:message key="skills.striker" />:</td><td>${j.desp(sessionScope['javax.servlet.jsp.jstl.fmt.locale.session'].language, 'striker', j.anotacion, usuario.numeros)}</td>
									</tr>
									<tr class="nt" style="background-color: transparent;">
										<td colspan="4">&nbsp;</td>
									</tr>
									<tr style="background-color: transparent;">
										<td colspan="2"><fmt:message key="common.position" />: <c:out value="${j.despDemarcacion_asistente(j.demarcacion, usuario)}" escapeXml="false"/></td>
										<td colspan="2">
											<label class="nt" for="fiable${j.pid}">
												<input type="checkbox" <c:out value="${j.actualizado ? 'checked' : ''}" /> id="fiable${j.pid}" name="fiable" /><fmt:message key="players.reliable" />
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
		</tr>
		
		<%-- ENTRENAMIENTO --%>

		<c:if test="${sessionScope.usuario.def_tid > 1000 && empty param.juveniles}">
			<c:forEach begin="${j.min_edad}" end="${j.edad}" var="edad">			
				<tr id="tr${j.pid}_${edad}" style="display: none; background-color: rgba(240, 240, 240, ${edad == j.edad ? 1 : 0.7}); opacity: 0.8;" class="${status.count == lista.size() ? 'sombra' : ''}">
					<td nowrap>
						<c:if test="${edad == j.edad}">
							<div style="display: inline-block; text-align: left">
								<i class="far fa-clock minutos" style="color: darkblue; cursor: pointer;" title="<fmt:message key="training.training_points_this_week" />"></i> <span>${j.minutos}</span>% ${j.demarcacion_entrenamiento}
								<br/>
	 							<span class="nont" title="<fmt:message key="training.minimum_maximum_talent" />"><i class="far fa-gem" style="opacity: 1; color: lightblue;"></i> <fmt:formatNumber value="${j.talento_min}" maxFractionDigits="1" /> / <fmt:formatNumber value="${j.talento_max}" maxFractionDigits="1" /></span>
	 						</div>
						</c:if>
					</td>
					<td>${edad}</td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Condicion, (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Rapidez,   (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Tecnica,   (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Pases,     (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Porteria,  (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Defensa,   (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Creacion,  (Integer)jspContext.getAttribute("edad")) %></td>
					<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)jspContext.getAttribute("j")).getEntrenamiento((Usuario)session.getAttribute("usuario"), TIPO_ENTRENAMIENTO.Anotacion, (Integer)jspContext.getAttribute("edad")) %></td>
					<td class="nohist" colspan="${sessionScope.usuario.def_tid < 1000 ? 3 : 1}">&nbsp;</td>
				</tr>
			</c:forEach>
		</c:if>
	</c:forEach>
<!-- </table> -->

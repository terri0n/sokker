<%@ page pageEncoding="UTF-8" import="com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO,com.formulamanager.sokker.bo.NtdbBO,com.formulamanager.sokker.entity.Jugador" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />
<c:set var="MAX_ID_SELECCION"><%= NtdbBO.MAX_ID_SELECCION %></c:set>

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
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Condicion, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Rapidez, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Tecnica, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Pases, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Porteria, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Defensa, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Creacion, (Integer)pageContext.getAttribute("edad")) %></td>
		<td nowrap valign="top" class="entrenamiento"><%= ((Jugador)request.getAttribute("j")).getEntrenamiento(TIPO_ENTRENAMIENTO.Anotacion, (Integer)pageContext.getAttribute("edad")) %></td>
		<td class="nohist" colspan="${sessionScope.usuario.def_tid < MAX_ID_SELECCION ? 3 : 1}">
			<span class="flecha_arr gris md-24" style="display: ${edad > j.min_edad && edad == j.edad - 2 ? '' : 'none'}" onclick="mover_entrenamiento_click(${j.pid}, -1, false)">▲</span>
			<span class="flecha_aba gris md-24" style="display: none" onclick="mover_entrenamiento_click(${j.pid}, 1, false)">▼</span>
		</td>
	</tr>
</c:forEach>

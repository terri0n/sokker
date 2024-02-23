<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<table>
		<tr><td>login</td><td><c:out value="${usuario.login}" /></td></tr>
		<tr><td>password</td><td><c:out value="${usuario.password}" /></td></tr>
		<tr><td>login_sokker</td><td><c:out value="${usuario.login_sokker}" /></td></tr>
		<tr><td>tid</td><td><c:out value="${usuario.tid}" /></td></tr>
		<tr><td>tid_nt</td><td><c:out value="${usuario.tid_nt}" /></td></tr>
		<tr><td>def_tid</td><td><c:out value="${usuario.def_tid}" /></td></tr>
		<tr><td>equipo</td><td><c:out value="${usuario.equipo}" /></td></tr>
		<tr><td>equipo_nt</td><td><c:out value="${usuario.equipo_nt}" /></td></tr>
		<tr><td>jornada</td><td><c:out value="${usuario.jornada}" /></td></tr>
		<tr><td>jornada_nt</td><td><c:out value="${usuario.jornada_nt}" /></td></tr>
		<tr><td>intentos_fallidos</td><td><c:out value="${usuario.intentos_fallidos}" /></td></tr>
		<tr><td>ntdb</td><td><c:out value="${usuario.ntdb}" /></td></tr>
		<tr><td>locale</td><td><c:out value="${usuario.locale}" /></td></tr>
		<tr><td>countryID</td><td><c:out value="${usuario.countryID}" /></td></tr>
		<tr><td>recibir_ntdb</td><td><c:out value="${usuario.recibir_ntdb}" /></td></tr>
	</table>

	<br />

	<table>	
		<c:forEach items="${usuario.entrenador_principal}" var="e" varStatus="status">
			<tr>
				<td><c:out value="${status.index}"/></td>
				<td><c:out value="${e.key}"/></td>
				<td><c:out value="${e.value.serializar_entrenador()}"/></td>
				<td><c:out value="${usuario.getTipo_entrenamiento(0)[e.key]}"/></td>
				<td><c:out value="${usuario.getTipo_entrenamiento(1)[e.key]}"/></td>
				<td><c:out value="${usuario.getTipo_entrenamiento(2)[e.key]}"/></td>
				<td><c:out value="${usuario.getTipo_entrenamiento(3)[e.key]}"/></td>
			</tr>
		</c:forEach>
	</table>

	<br />

	<table>
		<c:forEach items="${jugadores}" var="j">
			<tr>
				<td><c:out value="${j.pid}" /></td>
				<td><c:out value="${j.nombre}" /></td>
				<td><c:out value="${j.usuario2.login}" /></td>
			</tr>
			<c:forEach items="${j.lista_originales}" var="o">
				<tr>
					<td>&nbsp;</td>
					<td><c:out value="${o.jornada}" /></td>
					<td><c:out value="${o.edad}" /></td>
					<td><c:out value="${o.minutos}" /></td>
					<td><c:out value="${o.demarcacion_entrenamiento}" /></td>
					<td><c:out value="${o.entrenamiento_avanzado}" /></td>
					<td><c:out value="${o.usuario_jornada.login}" /></td>
				</tr>
			</c:forEach>
		</c:forEach>
	</table>	
</body>
</html>
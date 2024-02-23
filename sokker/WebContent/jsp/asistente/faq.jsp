<%@page import="java.util.HashMap"%>
<%@page import="com.formulamanager.sokker.auxiliares.Util"%>
<%@page import="com.formulamanager.sokker.bo.NtdbBO"%>
<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<style>
		.q {
			font-weight: bolder;
			font-size: large;
			margin-bottom: 4px;
		}
		.q .material-icons {
			vertical-align: middle;
		}
		.fa-stack {
			margin: -12px;
		}
		.blanco {
			color: white;
		}
		.columna {
			max-width: 780px; 
			column-width: 380px; 
			display: inline-block;
			vertical-align: top;
			text-align: justify;
			padding: 5px;
		}
		.columna .sombra {
			margin: 10px;
		}
		p {
			text-indent: 20px;
			margin-top: 1em;
			margin-bottom: 1em;
		}
	</style>
</head>

<body style="text-align: center;">
	<div class="columna">
		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question1" />
		</div>
		<fmt:message key="faq.answer1" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question2" />
		</div>
		<fmt:message key="faq.answer2" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question13" />
		</div>
		<fmt:message key="faq.answer13" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question3" />
		</div>
		<fmt:message key="faq.answer3" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question4" />
		</div>
		<fmt:message key="faq.answer4" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question5" />
		</div>
		<fmt:message key="faq.answer5" />
	
		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question6" />
		</div>
		<fmt:message key="faq.answer6" />
	
		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question7" />
		</div>
		<fmt:message key="faq.answer7" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question14" />
		</div>
		<fmt:message key="faq.answer14" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question8" />
		</div>
		<fmt:message key="faq.answer8" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question9" />
		</div>
		<fmt:message key="faq.answer9" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question15" />
		</div>
		<div align="center">
			<img src="${pageContext.request.contextPath}/img/faq/entrenamiento.png" />
		</div>
		<fmt:message key="faq.answer15" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question10" />
		</div>
		<fmt:message key="faq.answer10" />

		<div class="q">
			<div class="material-icons rojo borde-blanco">help</div>
			<fmt:message key="faq.question11" />
		</div>
		<fmt:message key="faq.answer11" />
		
		<div class="tabla">
			<table class="fondo_tabla">
				<thead class="borde">
					<tr>
						<th nowrap><fmt:message key="common.country" /></th>
						<th><fmt:message key="common.url" /></th>
					</tr>
				</thead>
				
				<%
					HashMap<String, String> urls = Util.leer_hashmap("URLs");
					request.setAttribute("urls", urls);
				%>
				<c:forEach begin="1" end="<%= NtdbBO.paises.length %>" var="i">
					<c:if test="${not empty urls[\"NT_\".concat(i)]}">
						<tr>
							<td align="center"><img src="https://files.sokker.org/pic/flags/<c:out value="${i}" />.png" /></td>
							<td>
								<c:out value="${urls[\"NT_\".concat(i)]}" />
								<c:if test="${not empty urls[\"U21_\".concat(i)]}">
									<br />
									<c:out value="${urls[\"U21_\".concat(i)]}" />
								</c:if>
							</td>
						</tr>
					</c:if>
				</c:forEach>
			 </table>
		</div>

		<div class="q">
			<div class="material-icons azul borde-blanco">info</div>
			<fmt:message key="faq.question12" />
		</div>
		
		<div class="tabla">
			<table class="fondo_tabla">
				<tr>
					<td align="center"><img src="${pageContext.request.contextPath}/img/banderas/GB.png" /></td>
					<td><a href="https://sokker.org/team/teamID/5661" target="blank_">Terrion</a></td>
				</tr>
				<tr>
					<td align="center"><img src="${pageContext.request.contextPath}/img/banderas/ES.png" /></td>
					<td><a href="https://sokker.org/team/teamID/5661" target="blank_">Terrion</a></td>
				</tr>
 				<tr>
					<td align="center"><img src="${pageContext.request.contextPath}/img/banderas/FR.png" /></td>
					<td><a href="https://sokker.org/team/teamID/11917" target="_blank">clarom</a></td>
				</tr>
				<tr>
					<td align="center"><img src="${pageContext.request.contextPath}/img/banderas/IT.png" /></td>
					<td><a href="https://sokker.org/team/teamID/44454" target="_blank">ringhiostarr</a></td>
				</tr>
<!-- 				<tr>
					<td align="center"><img src="${pageContext.request.contextPath}/img/banderas/PL.png" /></td>
					<td><a href="https://sokker.org/team/teamID/24878" target="_blank">kryminator</a></td>
				</tr>
 -->
 <!-- 				<tr>
					<td align="center"><img src="${pageContext.request.contextPath}/img/banderas/DE.png" /></td>
					<td><a href="https://sokker.org/team/teamID/18169" target="_blank">slamtam</a></td>
				</tr>
 -->			 </table>
		</div>
	</div>
</body>
</html>
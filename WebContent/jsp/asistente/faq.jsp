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
		.fa-stack {
			margin: -12px;
		}
		.blanco {
			color: white;
		}
		.rojo {
			color: red;
		}
		.columna {
			max-width: 780px; 
			column-width: 380px; 
			display: inline-block;
			vertical-align: top;
			text-align: justify;
			padding: 5px;
		}
		p {
			text-indent: 20px;
		}
	</style>
</head>

<body style="text-align: center;">
	<div class="columna">
		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question1" />
		</div>
		<fmt:message key="faq.answer1" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question13" />
		</div>
		<fmt:message key="faq.answer13" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question2" />
		</div>
		<fmt:message key="faq.answer2" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question3" />
		</div>
		<fmt:message key="faq.answer3" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question4" />
		</div>
		<fmt:message key="faq.answer4" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question5" />
		</div>
		<fmt:message key="faq.answer5" />
	
		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question6" />
		</div>
		<fmt:message key="faq.answer6" />
	
		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question7" />
		</div>
		<fmt:message key="faq.answer7" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question8" />
		</div>
		<fmt:message key="faq.answer8" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question9" />
		</div>
		<fmt:message key="faq.answer9" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question10" />
		</div>
		<fmt:message key="faq.answer10" />

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco far fa-question-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-question-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question11" />
		</div>
		<fmt:message key="faq.answer11" />
		
		<div class="tabla sombra">
			<table>
				<thead class="borde">
					<tr>
						<th nowrap><fmt:message key="common.country" /></th>
						<th><fmt:message key="common.url" /></th>
					</tr>
				</thead>
				<c:forEach items="<%= NtdbBO.urls %>" var="url" varStatus="status">
					<c:if test="${not empty url}">
						<tr>
							<td align="center"><img src="https://files.sokker.org/pic/flags/<c:out value="${status.count}" />.png" /></td>
							<td><c:out value="${url}" /></td>
						</tr>
					</c:if>
				</c:forEach>
			 </table>
		</div>
		<br/><br/>

		<div class="q">
			<span class="fa-stack fa-1x">
				<i class="blanco fas fa-circle fa-stack-1x borde"></i>
				<i class="rojo fas fa-exclamation-circle fa-stack-1x"></i>
			</span>
			<fmt:message key="faq.question12" />
		</div>
		<br/>
		
		<div class="tabla sombra">
			<table>
				<tr>
					<td align="center"><img src="img/banderas/ES.png" /></td>
					<td><a href="http://sokker.org/team/teamID/5661" target="blank_">Terrion</a></td>
				</tr>
				<tr>
					<td align="center"><img src="img/banderas/GB.png" /></td>
					<td><a href="http://sokker.org/team/teamID/5661" target="blank_">Terrion</a></td>
				</tr>
				<tr>
					<td align="center"><img src="img/banderas/IT.png" /></td>
					<td><a href="http://sokker.org/team/teamID/44454" target="_blank">ringhiostarr</a></td>
				</tr>
<!-- 				<tr>
					<td align="center"><img src="img/banderas/PL.png" /></td>
					<td><a href="http://sokker.org/team/teamID/24878" target="_blank">kryminator</a></td>
				</tr>
 -->			 </table>
		</div>
	</div>
</body>
</html>
<%@page import="com.formulamanager.sokker.bo.NtdbBO"%>
<%@page import="com.formulamanager.sokker.entity.Jugador"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="xtag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/mistags.tld" prefix="tags" %>
<!DOCTYPE html>
<html>

<head>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
	<link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
	
	<style>
		body{
			background-color: #e2f2f8;
		}
		.titulo {
			color: white;
			background-color: #539bcd;
			font-weight: bolder;
			width: 100%;
			padding: 1px;
		}
		label, .flecha {
			cursor: pointer;
		}
		a, a:visited {
			color: darkblue;
			decoration: none;
		}
		a:hover {
			color: blue;
		}
		hr {
			background-color: #4f8ea2; height: 1px; border: 0;
		}
		.mensaje {
			color: white;
			background-color: orange;
			border: 1px solid black;
		}
	</style>
</head>

<body>
	<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto;">
		<%@include file="ntdb_menu.jsp" %>

		<div style="float: right; width:250px;height:800px;text-align: right;" class="solo_pc_ancho">
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

		<div style="vertical-align: top; padding: 1px; text-align: center; margin: auto;">
			<div style="display: inline-block; padding-top: 10px;">
				<div class="cabecera" style="font-size: 1.5em;">
					NTDB interface
				</div>
				
				<div class="fin_bloque doble">
					<form method="post" action="${pageContext.request.contextPath}/asistente/ntdb/send">
						<div style="vertical-align: top; padding: 1px; text-align: center;">
							<div style="display: inline-block; text-align: left;">
								<div align="center">
									<b>Add or update a player in Sokker Asistente DB:</b>
								</div>
								<br/>
								
								<table>
									<tr>
										<td><b>Player ID:</b></td>
										<td><input type="number" name="pid" value="${param.pid}" required="required" /></td>
									</tr>
									<tr>
										<td><b>Stamina:</b></td>
										<td>
											<c:set var="j" value="<%= new Jugador() %>" />
											<c:out value="${j.desp('en', 'sta', param.sta, true, 11)}" escapeXml="false"/>
										</td>
										<td><b>Keeper:</b></td>
										<td><c:out value="${j.desp('en', 'kee', param.kee, true)}" escapeXml="false"/></td>
									</tr>
									<tr>
										<td><b>Pace:</b></td>
										<td><c:out value="${j.desp('en', 'pac', param.pac, true)}" escapeXml="false"/></td>
										<td><b>Defender:</b></td>
										<td><c:out value="${j.desp('en', 'def', param.def, true)}" escapeXml="false"/></td>
									</tr>
									<tr>
										<td><b>Technique:</b></td>
										<td><c:out value="${j.desp('en', 'tec', param.tec, true)}" escapeXml="false"/></td>
										<td><b>Playmaker:</b></td>
										<td><c:out value="${j.desp('en', 'pla', param.pla, true)}" escapeXml="false"/></td>
									</tr>
									<tr>
										<td><b>Passing:</b></td>
										<td><c:out value="${j.desp('en', 'pas', param.pas, true)}" escapeXml="false"/></td>
										<td><b>Striker:</b></td>
										<td><c:out value="${j.desp('en', 'str', param.str, true)}" escapeXml="false"/></td>
									</tr>
									<tr>
										<td><b>Notes:</b></td>
										<td colspan="3"><textarea cols="50" rows="10" name="obs"><c:out value="${param.obs}" escapeXml="yes"/></textarea></td>
									</tr>
								</table>
				
								<div align="center">
									<input type="submit" />
								</div>
							</div>
						</div>
					</form>
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
			</div>
		</div>
	</div>	
</body>
</html>

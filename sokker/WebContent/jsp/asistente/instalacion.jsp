<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; UTF-8">
	<style>
		.codigo {
			font-family: monospace;
			font-size: small;
			color: darkblue;
		}
		.pl-e {
			color: #d2a8ff;
		}
		.pl-ent {
			color: #7ee787;
		}
		.pl-s, .pl-pds {
			color: #a5d6ff;
		}
		pre {
			color: white;
			background-color: black;
		}
	</style>
</head>

<body>
	<h2 style="color: darkblue;">STEPS TO INSTALL SOKKER ASISTENTE IN YOUR HARD DISK</h2>
	
	<div style="text-align: left;">
		<ol>
			<li>Install <a href="https://www.oracle.com/es/java/technologies/javase/javase-jdk8-downloads.html">Java SE Development Kit 8</a>. You will be asked to register in Oracle to download the instalation file. Then follow the indications</li>
			<li>Install <a href="https://tomcat.apache.org/download-90.cgi">Apache Tomcat 9</a>. The easiest way is to choose "32-bit/64-bit Windows Service Installer"</li>
			<li>Download <a href="/var/sokker.war"><span class="codigo">sokker.war</span></a> and place it in <span class="codigo">C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps</span>
			<li>Edit <span class="codigo">C:\Program Files\Apache Software Foundation\Tomcat 9.0\conf\server.xml</span> with Notepad and add the three lines to the <span class="codigo">&lt;GlobalNamingResources&gt;</span> tag, writing your Sokker login and password instead of the xxx:
<pre>  &lt;<span class="pl-ent">GlobalNamingResources</span>&gt;
      ...
      &lt;<span class="pl-ent">Environment</span> <span class="pl-e">name</span>=<span class="pl-s"><span class="pl-pds">"</span>login<span class="pl-pds">"</span></span> <span class="pl-e">value</span>=<span class="pl-s"><span class="pl-pds">"</span>xxx<span class="pl-pds">"</span></span> <span class="pl-e">type</span>=<span class="pl-s"><span class="pl-pds">"</span>java.lang.String<span class="pl-pds">"</span></span> <span class="pl-e">override</span>=<span class="pl-s"><span class="pl-pds">"</span>false<span class="pl-pds">"</span></span>/&gt;
      &lt;<span class="pl-ent">Environment</span> <span class="pl-e">name</span>=<span class="pl-s"><span class="pl-pds">"</span>password<span class="pl-pds">"</span></span> <span class="pl-e">value</span>=<span class="pl-s"><span class="pl-pds">"</span>xxx<span class="pl-pds">"</span></span> <span class="pl-e">type</span>=<span class="pl-s"><span class="pl-pds">"</span>java.lang.String<span class="pl-pds">"</span></span> <span class="pl-e">override</span>=<span class="pl-s"><span class="pl-pds">"</span>false<span class="pl-pds">"</span></span>/&gt;
      &lt;<span class="pl-ent">Environment</span> <span class="pl-e">name</span>=<span class="pl-s"><span class="pl-pds">"</span>path<span class="pl-pds">"</span></span> <span class="pl-e">value</span>=<span class="pl-s"><span class="pl-pds">"</span>D:\\home\\asistente\\<span class="pl-pds">"</span></span> <span class="pl-e">type</span>=<span class="pl-s"><span class="pl-pds">"</span>java.lang.String<span class="pl-pds">"</span></span> <span class="pl-e">override</span>=<span class="pl-s"><span class="pl-pds">"</span>false<span class="pl-pds">"</span></span>/&gt;
  &lt;/<span class="pl-ent">GlobalNamingResources</span>&gt;
</pre></li>
			<li>Create the folder <span class="codigo">D:/home/asistente</span></li>
			<li>Download
				<a href="${pageContext.request.contextPath}/servlet/descargar?tipo=1"><span class="codigo">_${usuario.login}.properties</span></a>,
				<a href="${pageContext.request.contextPath}/servlet/descargar?tipo=2"><span class="codigo">${usuario.def_tid}.properties</span></a>, 
				<a href="${pageContext.request.contextPath}/servlet/descargar?tipo=3"><span class="codigo">${usuario.def_tid}_juveniles.properties</span></a>, 
				<a href="${pageContext.request.contextPath}/servlet/descargar?tipo=4"><span class="codigo">${usuario.def_tid}_historico.properties</span></a> and 
				<a href="${pageContext.request.contextPath}/servlet/descargar?tipo=5"><span class="codigo">${usuario.def_tid}_juveniles_historico.properties</span></a>
				and place them in <span class="codigo">D:/home/asistente</span>
			<li>Restart Tomcat. You can do it by clicking on the Tomcat task bar icon and then clicking on stop and start service.</li>
			<li>Go to <span class="codigo">http://localhost:8080/sokker/asistente</span></li>
		</ol>
	</div>
</body>
</html>
from pathlib import Path
import re

bo = Path('sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java')
text = bo.read_text(encoding='utf-8')
pattern = re.compile(r'\tpublic static String listar_usuarios\(\) throws UnsupportedEncodingException \{.*?\n\t\}\n\n\tpublic static String listar_jornadas', re.S)
replacement = '''\tpublic static String listar_usuarios() throws UnsupportedEncodingException {
\t\tString[] archivos = UsuarioBO.obtener_usuarios();
\t\tList<String> pendientes = UsuarioBO.obtener_usuarios_con_borrado_solicitado();
\t\tSet<String> pendientesSet = new HashSet<String>(pendientes);

\t\tString salida = "Total: " + archivos.length + "<br/>"
\t\t\t\t+ "<label id='' for='reset_intentos'><input type='checkbox' id='reset_intentos' /> Resetear</label><br/>";

\t\tif (!pendientes.isEmpty()) {
\t\t\tsalida += "<strong>Pending account deletion requests</strong><br/>";
\t\t\tfor (String usuario : pendientes) {
\t\t\t\tsalida += "<a href='asistente/login_como?usuario=" + URLEncoder.encode(usuario, "UTF-8") + "'>" + usuario + "</a>"
\t\t\t\t\t\t+ " <a href='asistente/examinar_usuario?usuario=" + URLEncoder.encode(usuario, "UTF-8") + "'><i class=\\\"fa-solid fa-binoculars azul\\\"></i></a><br/>";
\t\t\t}
\t\t\tsalida += "<hr/>";
\t\t}

\t\tArrays.sort(archivos);
\t\tfor (String s : archivos) {
\t\t\ttry {
\t\t\t\tString usuario = s.split(".properties")[0].substring(1);
\t\t\t\tif (pendientesSet.contains(usuario)) continue;
\t\t\t\tFile fUsuario = new File(SystemUtil.getVar(SystemUtil.PATH) + "/" + s);
\t\t\t\tDate fecha = new Date(fUsuario.lastModified());
\t\t\t\tUsuario usr = UsuarioBO.leer_usuario(usuario, false);
\t\t\t\tsalida += Util.dateToString(fecha)
\t\t\t\t\t\t+ " <a href='javascript:void(0)' onclick=\\\"location.href='asistente/login_como?usuario=" + URLEncoder.encode(usuario, "UTF-8") + "&reset_intentos=' + $('#reset_intentos').val()\\\">" + usuario + "</a> ";
\t\t\t\tsalida += "[" + usr.getTid() + "]"
\t\t\t\t\t\t+ " <a href='javascript:void(0)' onclick=\\\"location.href='asistente/examinar_usuario?usuario=" + URLEncoder.encode(usuario, "UTF-8") + "'\\\"><i class=\\\"fa-solid fa-binoculars azul\\\"></i></a> <br/>";
\t\t\t} catch (Exception e) {
\t\t\t\te.printStackTrace();
\t\t\t\tsalida += "-- Error leyendo " + s + "<br />";
\t\t\t}
\t\t}
\t\treturn salida;
\t}

\tpublic static String listar_jornadas'''
text2, n = pattern.subn(lambda m: replacement, text, count=1)
if n != 1:
    raise SystemExit('listar_usuarios replacement failed: %d' % n)
bo.write_text(text2, encoding='utf-8', newline='')

jsp = Path('sokker/WebContent/jsp/asistente/asistente.jsp')
text = jsp.read_text(encoding='utf-8')
if 'id="account_deletion_request"' not in text:
    block = '''\n\t<c:if test="${not empty sessionScope.usuario and empty sessionScope.admin}">\n\t\t<div id="account_deletion_request" style="margin: 12px; text-align: center;">\n\t\t\t<% Usuario deletionUser = (Usuario) session.getAttribute("usuario");\n\t\t\t   Long deletionRequestedAt = deletionUser == null ? null : UsuarioBO.obtener_fecha_solicitud_borrado(deletionUser.getLogin());\n\t\t\t   if (deletionRequestedAt == null) { %>\n\t\t\t<form method="post" action="${pageContext.request.contextPath}/asistente/solicitar_eliminacion_cuenta" onsubmit="return confirm('Request deletion of this account?');">\n\t\t\t\t<button type="submit">Request account deletion</button>\n\t\t\t</form>\n\t\t\t<% } else { %><strong>Account deletion requested</strong><% } %>\n\t\t</div>\n\t</c:if>\n'''
    if '</body>' not in text:
        raise SystemExit('body marker missing')
    text = text.replace('</body>', block + '</body>', 1)
jsp.write_text(text, encoding='utf-8', newline='')

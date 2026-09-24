from pathlib import Path

jsp = Path('sokker/WebContent/jsp/asistente/asistente.jsp')
text = jsp.read_text(encoding='utf-8')
if 'id="admin_account_delete"' not in text:
    marker = '<c:if test="${not empty sessionScope.usuario and empty sessionScope.admin}">'
    if marker not in text:
        raise SystemExit('account deletion request marker not found')
    block = '''<c:if test="${not empty sessionScope.admin and not empty sessionScope.admin_impersonated_login and sessionScope.admin_impersonated_login == sessionScope.usuario.login and sessionScope.usuario.login != sessionScope.admin_original_user.login}">\n\t\t<div id="admin_account_delete" style="margin: 12px; text-align: center;">\n\t\t\t<a href="${pageContext.request.contextPath}/asistente/eliminar_cuenta_admin?usuario=${sessionScope.usuario.login}">Delete this account and all associated data</a>\n\t\t</div>\n\t</c:if>\n\t'''
    text = text.replace(marker, block + marker, 1)
jsp.write_text(text, encoding='utf-8', newline='')

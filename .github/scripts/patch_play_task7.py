from pathlib import Path

ROOT = Path('.')

TRANSLATIONS = {
    'en': {
        'account.delete.request': 'Request account deletion',
        'account.delete.pending': 'Account deletion requested',
        'account.delete.admin.title': 'Delete account and associated data',
        'account.delete.admin.confirm': 'Type the account login to confirm permanent deletion',
        'account.delete.admin.button': 'Delete permanently',
        'account.delete.admin.link': 'Delete this account and all associated data',
        'account.delete.completed': 'Account and associated data deleted',
        'account.delete.public.title': 'Delete Sokker Asistente account',
        'account.delete.public.explanation': 'Submit this form to request deletion of your Sokker Asistente account and associated data. The administrator will review the request before deletion.',
        'account.delete.public.login': 'Sokker Asistente login',
        'account.delete.public.contact': 'Contact email (optional)',
        'account.delete.public.message': 'Message (optional)',
        'account.delete.public.submit': 'Send deletion request',
        'account.delete.public.received': 'Your deletion request has been received for review.',
        'privacy.title': 'Privacy policy',
        'privacy.account_data': 'Account and preferences',
        'privacy.sokker_data': 'Sokker team data',
        'privacy.logs': 'Logs',
        'privacy.google_services': 'Google services',
        'privacy.retention': 'Data retention',
        'privacy.deletion': 'Account and data deletion',
        'privacy.contact': 'Contact',
    },
    'es': {
        'account.delete.request': 'Solicitar eliminaci\\u00F3n de cuenta',
        'account.delete.pending': 'Eliminaci\\u00F3n de cuenta solicitada',
        'account.delete.admin.title': 'Eliminar cuenta y datos asociados',
        'account.delete.admin.confirm': 'Escribe el login de la cuenta para confirmar la eliminaci\\u00F3n definitiva',
        'account.delete.admin.button': 'Eliminar definitivamente',
        'account.delete.admin.link': 'Eliminar esta cuenta y todos sus datos asociados',
        'account.delete.completed': 'Cuenta y datos asociados eliminados',
        'account.delete.public.title': 'Eliminar cuenta de Sokker Asistente',
        'account.delete.public.explanation': 'Env\\u00EDa este formulario para solicitar la eliminaci\\u00F3n de tu cuenta de Sokker Asistente y sus datos asociados. El administrador revisar\\u00E1 la solicitud antes de borrar la cuenta.',
        'account.delete.public.login': 'Login de Sokker Asistente',
        'account.delete.public.contact': 'Email de contacto (opcional)',
        'account.delete.public.message': 'Mensaje (opcional)',
        'account.delete.public.submit': 'Enviar solicitud de eliminaci\\u00F3n',
        'account.delete.public.received': 'Tu solicitud de eliminaci\\u00F3n se ha recibido para revisi\\u00F3n.',
        'privacy.title': 'Pol\\u00EDtica de privacidad',
        'privacy.account_data': 'Cuenta y preferencias',
        'privacy.sokker_data': 'Datos del equipo de Sokker',
        'privacy.logs': 'Registros',
        'privacy.google_services': 'Servicios de Google',
        'privacy.retention': 'Conservaci\\u00F3n de datos',
        'privacy.deletion': 'Eliminaci\\u00F3n de cuenta y datos',
        'privacy.contact': 'Contacto',
    },
    'fr': {
        'account.delete.request': 'Demander la suppression du compte',
        'account.delete.pending': 'Suppression du compte demand\\u00E9e',
        'account.delete.admin.title': 'Supprimer le compte et les donn\\u00E9es associ\\u00E9es',
        'account.delete.admin.confirm': 'Saisissez l\\u2019identifiant du compte pour confirmer la suppression d\\u00E9finitive',
        'account.delete.admin.button': 'Supprimer d\\u00E9finitivement',
        'account.delete.admin.link': 'Supprimer ce compte et toutes les donn\\u00E9es associ\\u00E9es',
        'account.delete.completed': 'Compte et donn\\u00E9es associ\\u00E9es supprim\\u00E9s',
        'account.delete.public.title': 'Supprimer le compte Sokker Asistente',
        'account.delete.public.explanation': 'Envoyez ce formulaire pour demander la suppression de votre compte Sokker Asistente et des donn\\u00E9es associ\\u00E9es. L\\u2019administrateur examinera la demande avant la suppression.',
        'account.delete.public.login': 'Identifiant Sokker Asistente',
        'account.delete.public.contact': 'E-mail de contact (facultatif)',
        'account.delete.public.message': 'Message (facultatif)',
        'account.delete.public.submit': 'Envoyer la demande de suppression',
        'account.delete.public.received': 'Votre demande de suppression a \\u00E9t\\u00E9 re\\u00E7ue pour examen.',
        'privacy.title': 'Politique de confidentialit\\u00E9',
        'privacy.account_data': 'Compte et pr\\u00E9f\\u00E9rences',
        'privacy.sokker_data': 'Donn\\u00E9es de l\\u2019\\u00E9quipe Sokker',
        'privacy.logs': 'Journaux',
        'privacy.google_services': 'Services Google',
        'privacy.retention': 'Conservation des donn\\u00E9es',
        'privacy.deletion': 'Suppression du compte et des donn\\u00E9es',
        'privacy.contact': 'Contact',
    },
    'it': {
        'account.delete.request': 'Richiedi eliminazione account',
        'account.delete.pending': 'Eliminazione account richiesta',
        'account.delete.admin.title': 'Elimina account e dati associati',
        'account.delete.admin.confirm': 'Digita il login dell\\u2019account per confermare l\\u2019eliminazione definitiva',
        'account.delete.admin.button': 'Elimina definitivamente',
        'account.delete.admin.link': 'Elimina questo account e tutti i dati associati',
        'account.delete.completed': 'Account e dati associati eliminati',
        'account.delete.public.title': 'Elimina account Sokker Asistente',
        'account.delete.public.explanation': 'Invia questo modulo per richiedere l\\u2019eliminazione del tuo account Sokker Asistente e dei dati associati. L\\u2019amministratore esaminer\\u00E0 la richiesta prima dell\\u2019eliminazione.',
        'account.delete.public.login': 'Login Sokker Asistente',
        'account.delete.public.contact': 'Email di contatto (facoltativa)',
        'account.delete.public.message': 'Messaggio (facoltativo)',
        'account.delete.public.submit': 'Invia richiesta di eliminazione',
        'account.delete.public.received': 'La richiesta di eliminazione \\u00E8 stata ricevuta per la revisione.',
        'privacy.title': 'Informativa sulla privacy',
        'privacy.account_data': 'Account e preferenze',
        'privacy.sokker_data': 'Dati della squadra Sokker',
        'privacy.logs': 'Log',
        'privacy.google_services': 'Servizi Google',
        'privacy.retention': 'Conservazione dei dati',
        'privacy.deletion': 'Eliminazione account e dati',
        'privacy.contact': 'Contatti',
    },
    'sk': {
        'account.delete.request': 'Po\\u017Eiada\\u0165 o odstr\\u00E1nenie \\u00FA\\u010Dtu',
        'account.delete.pending': 'Odstr\\u00E1nenie \\u00FA\\u010Dtu bolo vy\\u017Eiadan\\u00E9',
        'account.delete.admin.title': 'Odstr\\u00E1ni\\u0165 \\u00FA\\u010Det a s\\u00FAvisiace \\u00FAdaje',
        'account.delete.admin.confirm': 'Na potvrdenie trval\\u00E9ho odstr\\u00E1nenia zadajte prihlasovacie meno \\u00FA\\u010Dtu',
        'account.delete.admin.button': 'Trvalo odstr\\u00E1ni\\u0165',
        'account.delete.admin.link': 'Odstr\\u00E1ni\\u0165 tento \\u00FA\\u010Det a v\\u0161etky s\\u00FAvisiace \\u00FAdaje',
        'account.delete.completed': '\\u00DA\\u010Det a s\\u00FAvisiace \\u00FAdaje boli odstr\\u00E1nen\\u00E9',
        'account.delete.public.title': 'Odstr\\u00E1nenie \\u00FA\\u010Dtu Sokker Asistente',
        'account.delete.public.explanation': 'Odo\\u0161lite tento formul\\u00E1r a po\\u017Eiadajte o odstr\\u00E1nenie \\u00FA\\u010Dtu Sokker Asistente a s\\u00FAvisiacich \\u00FAdajov. Spr\\u00E1vca \\u017Eiados\\u0165 pred odstr\\u00E1nen\\u00EDm skontroluje.',
        'account.delete.public.login': 'Prihlasovacie meno Sokker Asistente',
        'account.delete.public.contact': 'Kontaktn\\u00FD e-mail (volite\\u013En\\u00E9)',
        'account.delete.public.message': 'Spr\\u00E1va (volite\\u013En\\u00E1)',
        'account.delete.public.submit': 'Odosla\\u0165 \\u017Eiados\\u0165 o odstr\\u00E1nenie',
        'account.delete.public.received': 'Va\\u0161a \\u017Eiados\\u0165 o odstr\\u00E1nenie bola prijat\\u00E1 na kontrolu.',
        'privacy.title': 'Z\\u00E1sady ochrany s\\u00FAkromia',
        'privacy.account_data': '\\u00DA\\u010Det a nastavenia',
        'privacy.sokker_data': '\\u00DAdaje t\\u00EDmu Sokker',
        'privacy.logs': 'Z\\u00E1znamy',
        'privacy.google_services': 'Slu\\u017Eby Google',
        'privacy.retention': 'Uchov\\u00E1vanie \\u00FAdajov',
        'privacy.deletion': 'Odstr\\u00E1nenie \\u00FA\\u010Dtu a \\u00FAdajov',
        'privacy.contact': 'Kontakt',
    },
}

for lang, values in TRANSLATIONS.items():
    path = ROOT / f'sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_{lang}.properties'
    data = path.read_bytes()
    newline = b'\r\n' if b'\r\n' in data else b'\n'
    for key in values:
        marker = (key + ' ').encode('ascii')
        if marker in data or (key + '=').encode('ascii') in data:
            raise SystemExit(f'{key} already exists in {path}')
    block = newline + newline.join((f'{key} = {value}').encode('ascii') for key, value in values.items()) + newline
    path.write_bytes(data.rstrip(b'\r\n') + newline + block)

assistant = ROOT / 'sokker/WebContent/jsp/asistente/asistente.jsp'
text = assistant.read_text(encoding='utf-8')
text = text.replace('>Delete this account and all associated data</a>', '><fmt:message key="account.delete.admin.link" /></a>')
text = text.replace("onsubmit=\"return confirm('Request deletion of this account?');\"", "onsubmit=\"return confirm('<fmt:message key=\"account.delete.request\" />?');\"")
text = text.replace('<button type="submit">Request account deletion</button>', '<button type="submit"><fmt:message key="account.delete.request" /></button>')
text = text.replace('<% } else { %><strong>Account deletion requested</strong><% } %>', '<% } else { %><strong><fmt:message key="account.delete.pending" /></strong><% } %>')
needle = '\n</body>\n</html>'
footer = '''\n\t<div id="privacy_links" style="margin: 12px; text-align: center;">\n\t\t<a href="${pageContext.request.contextPath}/asistente/privacidad"><fmt:message key="privacy.title" /></a>\n\t\t&nbsp;|&nbsp;\n\t\t<a href="${pageContext.request.contextPath}/asistente/eliminar_cuenta"><fmt:message key="account.delete.request" /></a>\n\t</div>'''
if '/asistente/privacidad' not in text:
    if needle not in text:
        raise SystemExit('assistant footer anchor not found')
    text = text.replace(needle, footer + needle)
assistant.write_text(text, encoding='utf-8', newline='')

admin = ROOT / 'sokker/WebContent/jsp/asistente/eliminar_cuenta_admin.jsp'
text = admin.read_text(encoding='utf-8')
if 'prefix="fmt"' not in text:
    text = text.replace('<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>', '<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>\n<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>\n<fmt:setBundle basename="com.formulamanager.sokker.idiomas.ApplicationResources" />')
text = text.replace('<title>Delete Sokker Asistente account</title>', '<title><fmt:message key="account.delete.admin.title" /></title>')
text = text.replace('<h1>Delete account and associated data</h1>', '<h1><fmt:message key="account.delete.admin.title" /></h1>')
text = text.replace('Type the exact login to confirm:', '<fmt:message key="account.delete.admin.confirm" />:')
text = text.replace('<button type="submit">Delete permanently</button>', '<button type="submit"><fmt:message key="account.delete.admin.button" /></button>')
admin.write_text(text, encoding='utf-8', newline='')

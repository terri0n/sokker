from pathlib import Path
import subprocess

BASE = 'fb166432226f21fd432476b4dea6dccdabd8513b'
PATH = 'sokker/src/com/formulamanager/sokker/bo/UsuarioBO.java'
text = subprocess.check_output(['git', 'show', f'{BASE}:{PATH}']).decode('utf-8')

text = text.replace(
    'import java.util.Calendar;\nimport java.util.Date;\n',
    'import java.util.Calendar;\nimport java.util.Collections;\nimport java.util.Comparator;\nimport java.util.Date;\n',
    1)

helpers = '''\tpublic static final String ACCOUNT_DELETION_REQUESTED_AT = "account_deletion_requested_at";\n\n\tprivate static String ruta_usuario(String login) {\n\t\tString loginNormalizado = login.toLowerCase();\n\t\treturn SystemUtil.getVar("path") + (loginNormalizado.startsWith("prueba/") ? "" : "_") + loginNormalizado + ".properties";\n\t}\n\n\tprivate static Properties cargar_properties_usuario(String login) throws IOException {\n\t\tString ruta = ruta_usuario(login);\n\t\tFile file = new File(ruta);\n\t\tif (!file.exists()) {\n\t\t\tthrow new IOException("Usuario no encontrado: " + login);\n\t\t}\n\n\t\tProperties prop = new Properties();\n\t\ttry (InputStream input = new FileInputStream(file)) {\n\t\t\tprop.load(input);\n\t\t}\n\t\treturn prop;\n\t}\n\n\tprivate static Properties cargar_properties_usuario_sin_excepcion(String login) {\n\t\ttry {\n\t\t\treturn cargar_properties_usuario(login);\n\t\t} catch (Exception e) {\n\t\t\treturn new Properties();\n\t\t}\n\t}\n\n\tpublic static void solicitar_borrado(String login, long timestamp) throws IOException {\n\t\tProperties prop = cargar_properties_usuario(login);\n\t\tprop.setProperty(ACCOUNT_DELETION_REQUESTED_AT, String.valueOf(timestamp));\n\t\tUtil.guardar_properties(prop, ruta_usuario(login));\n\t}\n\n\tpublic static Long obtener_fecha_solicitud_borrado(String login) {\n\t\tProperties prop = cargar_properties_usuario_sin_excepcion(login);\n\t\tString value = prop.getProperty(ACCOUNT_DELETION_REQUESTED_AT);\n\t\tif (value == null || value.trim().isEmpty()) {\n\t\t\treturn null;\n\t\t}\n\t\ttry {\n\t\t\treturn Long.valueOf(value);\n\t\t} catch (NumberFormatException e) {\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tpublic static List<String> obtener_usuarios_con_borrado_solicitado() {\n\t\tString[] archivos = obtener_usuarios();\n\t\tfinal HashMap<String, Long> fechas = new HashMap<String, Long>();\n\t\tList<String> usuarios = new ArrayList<String>();\n\n\t\tfor (String archivo : archivos) {\n\t\t\tif (archivo == null || !archivo.startsWith("_") || !archivo.endsWith(".properties")) {\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\tString login = archivo.substring(1, archivo.length() - ".properties".length());\n\t\t\tLong fecha = obtener_fecha_solicitud_borrado(login);\n\t\t\tif (fecha != null) {\n\t\t\t\tfechas.put(login, fecha);\n\t\t\t\tusuarios.add(login);\n\t\t\t}\n\t\t}\n\n\t\tCollections.sort(usuarios, new Comparator<String>() {\n\t\t\t@Override\n\t\t\tpublic int compare(String a, String b) {\n\t\t\t\tint fecha = fechas.get(a).compareTo(fechas.get(b));\n\t\t\t\treturn fecha != 0 ? fecha : a.compareToIgnoreCase(b);\n\t\t\t}\n\t\t});\n\t\treturn usuarios;\n\t}\n\n'''
text = text.replace('public class UsuarioBO {\n', 'public class UsuarioBO {\n' + helpers, 1)
text = text.replace(
    '\t\t\tString BD = SystemUtil.getVar("path") + (login.startsWith("prueba/") ? "" : "_") + login.toLowerCase() + ".properties";',
    '\t\t\tString BD = ruta_usuario(login);',
    1)

purge = '''\tprivate static String purgar_secreto_actualizacion_automatica(String serializado) {\n\t\tList<String> valores = Arrays.asList(serializado.split(",", -1));\n\t\tint fin = valores.indexOf("*");\n\t\tif (fin > 0) {\n\t\t\tvalores.set(fin - 1, "");\n\t\t}\n\t\treturn String.join(",", valores);\n\t}\n\n'''
text = text.replace('\tpublic static void grabar_usuario(Usuario usuario) throws IOException {\n', purge + '\tpublic static void grabar_usuario(Usuario usuario) throws IOException {\n', 1)
text = text.replace(
    '\t\tString ruta = SystemUtil.getVar("path") + (usuario.getLogin().startsWith("prueba/") ? "" : "_") + usuario.getLogin() + ".properties";',
    '\t\tString ruta = ruta_usuario(usuario.getLogin());',
    1)
text = text.replace(
    '\t\tprop.setProperty("usuario", conservar_valores_usuario_desconocidos(usuario_anterior, usuario.serializar()));',
    '\t\tString usuario_serializado = purgar_secreto_actualizacion_automatica(usuario.serializar());\n\t\tprop.setProperty("usuario", conservar_valores_usuario_desconocidos(usuario_anterior, usuario_serializado));',
    1)

Path(PATH).write_text(text, encoding='utf-8', newline='')

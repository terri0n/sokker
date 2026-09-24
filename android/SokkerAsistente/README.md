# Sokker Asistente Android

Aplicación Android ligera que carga Sokker Asistente en un `WebView` y actúa como proxy nativo únicamente para las peticiones directas a `https://sokker.org` que el navegador no puede realizar por CORS.

## Requisitos de build

- JDK 17
- Android SDK platform 36
- Android build-tools 36.0.0
- application id: `com.formulamanager.sokker.asistente`
- `minSdk`: 21
- `targetSdk`: 36
- `versionCode`: 11
- `versionName`: 1.11

## Build y tests

Desde `android/SokkerAsistente`:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew bundleRelease
```

Artefactos esperados:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/bundle/release/app-release.aab
```

El AAB producido por CI sirve para verificar compilación/empaquetado. Antes de subirlo a Google Play debe firmarse con la clave de subida de la cuenta de Play.

Los APK/AAB son artefactos generados y no deben versionarse.

## Proxy de Sokker

El proxy nativo sólo intercepta el host HTTPS exacto `sokker.org`.

- conserva o añade `X-Sokker-Client`;
- no trata ese identificador como secreto;
- sólo reconstruye `ilogin`/`ipassword` para el POST heredado `/start.php?session=xml`;
- no añade credenciales a otros endpoints;
- devuelve también las respuestas HTTP de error para que el JavaScript pueda procesarlas;
- no registra credenciales ni cuerpos de login.

Si en el futuro una petición POST distinta necesita un body arbitrario, hay que diseñar ese caso explícitamente; no ampliar el proxy por defecto.

## Firma y Google Play

La aplicación anterior se distribuía fuera de Google Play, por lo que esta primera publicación puede establecer una firma nueva.

Para Google Play se usará Play App Signing. Se puede generar una clave de subida nueva fuera del repositorio, por ejemplo:

```bash
keytool -genkeypair \
  -keystore "$HOME/.android/sokker-asistente-upload.jks" \
  -alias sokker-asistente-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Generar primero el bundle:

```bash
./gradlew bundleRelease
```

Firmarlo después con la clave de subida. `jarsigner` pedirá la contraseña de forma interactiva, por lo que no hace falta escribirla en scripts ni argumentos:

```bash
jarsigner \
  -keystore "$HOME/.android/sokker-asistente-upload.jks" \
  app/build/outputs/bundle/release/app-release.aab \
  sokker-asistente-upload
```

Comprobar la firma antes de subirlo:

```bash
jarsigner -verify -verbose -certs \
  app/build/outputs/bundle/release/app-release.aab
```

Sólo después de esa verificación se debe subir `app/build/outputs/bundle/release/app-release.aab` a Google Play y enrolar la aplicación en Play App Signing.

Reglas:

- no copiar el keystore al repositorio;
- no versionar `*.jks`, `*.keystore`, `key.properties` ni passwords;
- no incluir credenciales de firma en commits o documentación;
- conservar de forma segura la clave de subida fuera de Git;
- no tratar el AAB sin firmar que produce CI como artefacto listo para subir a Play.

Como la firma será distinta de la del APK antiguo distribuido manualmente, un usuario que conserve ese APK con el mismo package id tendrá que desinstalarlo antes de instalar la versión nueva firmada para Play.

## Checklist de internal testing

Antes de promover una release desde el canal interno de Google Play:

1. Abrir la app y comprobar que carga `https://raqueto.com/sokker/asistente`.
2. Navegar por varias pantallas y comprobar que Atrás usa primero el historial del `WebView`.
3. Comprobar `alert` y `confirm` de JavaScript.
4. Iniciar sesión en Sokker mediante el flujo heredado de la página.
5. Confirmar que una respuesta de login correcta llega al JavaScript.
6. Probar deliberadamente una contraseña Sokker incorrecta y confirmar que el error HTTP llega al JavaScript, sin pantalla en blanco ni excepción del proxy.
7. Confirmar que las peticiones a Sokker llevan `X-Sokker-Client`.
8. Confirmar que URLs que no son de Sokker no son interceptadas por el proxy.
9. Probar mediante Google Play internal testing en al menos un dispositivo/emulador compatible con API 21 si está disponible y en un dispositivo Android moderno.

## CI

`.github/workflows/android.yml` usa JDK 17, Android API 36 y el wrapper Gradle 8.14.2 versionado con el proyecto. Ejecuta tests unitarios, construye APK debug y AAB release, y comprueba que el repositorio no contiene artefactos generados ni material de firma.

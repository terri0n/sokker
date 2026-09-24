# Play Console Compliance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Sokker Asistente ready for Google Play account-deletion/privacy review while preserving the existing player reset, existing `.properties` data, and the recovered Android package `com.raqueto.sokkerasistente`.

**Architecture:** Keep account-deletion state as an independent key in each existing user `.properties` file, isolate destructive filesystem work in a new `AccountDeletionService`, and keep public request, authenticated request, admin review, and final deletion as distinct flows. Public requests send email only; authenticated requests mark the account; final deletion requires an admin session explicitly impersonating the target and executes an allowlisted, retryable deletion manifest.

**Tech Stack:** Java 8, Servlet/JSP/JSTL, Java `Properties`, JavaMail/Mailgun, existing Sokker Asistente BO/entity layer, GitHub Actions, Android WebView app targeting API 36.

**Spec:** `docs/superpowers/specs/2026-09-24-play-console-compliance-design.md`

## Global Constraints

- Do not change or remove the existing player reset/recovery workflow at `/asistente/borrar_jugadores`.
- Do not migrate `.properties` storage or change the known ordering of `Usuario.serializar()` except to blank the retired automatic-update secret field already present in the known format.
- Preserve unknown `.properties` keys, future `usuario=` fields, and future `entrenamiento...` fields across every user save.
- Store deletion-request state as independent property key `account_deletion_requested_at`, not as a new serialized `Usuario` field.
- Club deletion ownership is determined from `usuario.tid`, never `def_tid` or `tid_nt`.
- Never delete national-team datasets when deleting a selector account.
- Never recursively delete a directory or delete by a loose filename prefix; every deleted path must be produced by an exact path or anchored allowlist regex.
- Delete `_<login>.properties` last. If an earlier deletion step fails, the user account and pending marker must remain retryable.
- Final account deletion requires `admin` session state plus an explicit impersonation marker matching `session.usuario.login`.
- Public email links identify the login but contain no authorization secret and never execute deletion by GET.
- New user-visible application text uses resource keys present in `ApplicationResources_en/es/fr/it/sk.properties`.
- Keep Android `applicationId`/namespace `com.raqueto.sokkerasistente`, `targetSdk 36`, and current proxy behavior unchanged.
- Do not commit APK/AAB/signing keys/passwords.

## Review Focus

1. **Malicious/invalid public login such as `../../x`, slash, backslash, comma, or control characters:** public request must never turn untrusted input into a filesystem path; reject it before any `UsuarioBO` read and return a generic response.
2. **Two active accounts with the same real `tid`:** deleting one account removes only login-scoped data and cross-references; every TID-scoped current/history/junior/backup file remains.
3. **Deletion retry after a mid-operation filesystem failure:** missing already-deleted files are treated as completed work, but the account file remains until all prior steps succeed.
4. **Admin session without explicit target impersonation:** knowing or supplying `usuario=<login>` is insufficient to POST a deletion; the impersonation marker must equal the current session user's login.
5. **Legacy user record containing both a retired Base64 automatic-update password and unknown future fields:** the next save blanks the retired known secret while preserving every unknown key/future field and rereads successfully.

---

### Task 1: Enable branch CI and persist deletion requests without changing `Usuario` format

**Files:**
- Modify: `.github/workflows/java8-compile.yml`
- Modify: `.github/harness/PersistenceRoundTrip.java`
- Modify: `sokker/src/com/formulamanager/sokker/bo/UsuarioBO.java`
- Modify: `sokker/src/com/formulamanager/sokker/entity/Usuario.java`

**Interfaces:**
- Produces: `UsuarioBO.ACCOUNT_DELETION_REQUESTED_AT`
- Produces: `public static void solicitar_borrado(String login, long timestamp) throws IOException`
- Produces: `public static Long obtener_fecha_solicitud_borrado(String login)`
- Produces: `public static List<String> obtener_usuarios_con_borrado_solicitado()` sorted oldest request first, then login for deterministic ties.
- Preserves: `UsuarioBO.leer_usuario(...)` and `UsuarioBO.grabar_usuario(...)` external behavior for old files.

- [ ] **Step 1: Make Java 8 CI run on this feature branch**

Add the permanent branch family alongside the existing `main` and `fix/**` triggers:

```yaml
on:
  push:
    branches:
      - main
      - 'fix/**'
      - 'feature/**'
```

Do not change the existing compile/harness/WAR steps.

- [ ] **Step 2: Extend the persistence harness with failing deletion-marker and retired-secret cases**

In `PersistenceRoundTrip.main`, add calls to `userDeletionRequestRoundTrip()` and `retiredAutomaticUpdateSecretIsPurged()`.

The first test must create `_alice.properties` containing:

```properties
future.key=future-value
account_deletion_requested_at=1727186400000
```

then perform two `leer_usuario -> grabar_usuario -> leer` cycles and assert both keys still exist and the request timestamp returned by `UsuarioBO.obtener_fecha_solicitud_borrado("alice")` remains `1727186400000L`.

The second test must seed a serialized user whose known final automatic-update field contains Base64 of `legacy-secret`, append `future-one,future-two` before `,*`, save through `UsuarioBO.grabar_usuario`, then assert:

```java
require(!savedUsuario.contains("bGVnYWN5LXNlY3JldA=="), "Retired automatic-update secret survived save");
require(savedUsuario.endsWith(",future-one,future-two,*"), "Future usuario fields were lost while purging secret");
```

- [ ] **Step 3: Run the harness before implementation and verify the new assertions fail**

Use the same Java 8 classpath setup as `.github/workflows/java8-compile.yml`, compile all sources plus `PersistenceRoundTrip.java`, then run:

```bash
java -cp "/tmp/compat-harness:/tmp/sokker-classes:${CP}" PersistenceRoundTrip
```

Expected: FAIL because the deletion-request API does not exist and the retired Base64 field is still serialized.

- [ ] **Step 4: Add deletion-request property helpers in `UsuarioBO`**

Add:

```java
public static final String ACCOUNT_DELETION_REQUESTED_AT = "account_deletion_requested_at";

public static void solicitar_borrado(String login, long timestamp) throws IOException {
    Properties prop = cargar_properties_usuario(login);
    prop.setProperty(ACCOUNT_DELETION_REQUESTED_AT, String.valueOf(timestamp));
    Util.guardar_properties(prop, ruta_usuario(login));
}

public static Long obtener_fecha_solicitud_borrado(String login) {
    Properties prop = cargar_properties_usuario_sin_excepcion(login);
    String value = prop.getProperty(ACCOUNT_DELETION_REQUESTED_AT);
    if (value == null || value.trim().isEmpty()) return null;
    try {
        return Long.valueOf(value);
    } catch (NumberFormatException e) {
        return null;
    }
}
```

Factor the existing path/load logic into private helpers `ruta_usuario(String)` and `cargar_properties_usuario(String)` so `leer_usuario`, `grabar_usuario`, and request helpers resolve the exact same file. Keep the existing `prueba/` special case.

Implement `obtener_usuarios_con_borrado_solicitado()` by enumerating actual user `.properties` files, reading only the independent timestamp property, dropping malformed/missing timestamps, and sorting by timestamp then login. Do not instantiate `Usuario` merely to inspect the marker.

- [ ] **Step 5: Stop reserializing the retired automatic-update secret**

Keep the historical slots in `Usuario.serializar()` but write both as blank:

```java
valores.add(""); // historical plaintext automatic-update password slot
valores.add(factor_edad_rapidez + "");
valores.add(""); // retired Base64 automatic-update password slot
```

Keep the constructor tolerant of both old plaintext and Base64 records so existing files remain readable until they are next saved.

- [ ] **Step 6: Run persistence tests twice**

Run `PersistenceRoundTrip` twice against fresh temp data and then run the existing full compatibility harness set from the workflow. Expected: PASS both times, with unknown fields intact and no retired secret written.

- [ ] **Step 7: Commit**

```bash
git add .github/workflows/java8-compile.yml .github/harness/PersistenceRoundTrip.java \
  sokker/src/com/formulamanager/sokker/bo/UsuarioBO.java \
  sokker/src/com/formulamanager/sokker/entity/Usuario.java
git commit -m "feat: persist account deletion requests safely"
```

---

### Task 2: Build the allowlisted, retryable account-deletion service

**Files:**
- Create: `sokker/src/com/formulamanager/sokker/bo/AccountDeletionService.java`
- Create: `.github/harness/AccountDeletionHarness.java`
- Modify: `.github/workflows/java8-compile.yml`

**Interfaces:**
- Consumes: `UsuarioBO.leer_usuario`, `UsuarioBO.leer_usuarios`, `UsuarioBO.ACCOUNT_DELETION_REQUESTED_AT`, `Util.leer_hashmap("NTDB")`, `Util.guardar_hashmap(...)`.
- Produces: `public static DeletionManifest buildManifest(String login) throws IOException`
- Produces: `public static void execute(DeletionManifest manifest) throws IOException`
- Produces nested immutable `DeletionManifest` getters: `getLogin()`, `getTid()`, `isDeleteTeamData()`, `getFilesToDeleteBeforeAccount()`, `getAccountFile()`, `getScoutOwnersReferencingLogin()`.
- Produces package-private test seam `static void execute(DeletionManifest manifest, FileOperations files) throws IOException`.

- [ ] **Step 1: Write a failing `AccountDeletionHarness`**

Put the harness in package `com.formulamanager.sokker.bo` so it can use the package-private file-operation seam.

Cover these fixtures in isolated temp `SystemUtil.path` directories:

```text
unique-user:
  _alice.properties -> tid 123
  123.properties
  123_historico.properties
  123_juveniles.properties
  123_juveniles_historico.properties
  prueba/123.properties
  backup/123_1199.properties
  backup/123_1199_historico.properties
  backup/999_1199.properties
  logs/_alice.log
  NTDB entry alice=bob,charlie and owner=alice,bob

shared-tid:
  _alice.properties -> tid 123
  _other.properties -> tid 123
  same TID files as above

national-team:
  _alice.properties -> tid 123, tid_nt 34
  34.properties and 34_historico.properties
```

Assert unique-user deletion removes only exact 123 club files/backups, personal log, test-user file, own NTDB entry, and exact `alice` scout tokens. Assert `999_1199.properties` remains.

Assert shared-tid deletion preserves every 123-scoped file while removing Alice's login-scoped data and NTDB references.

Assert national-team files `34.properties` and `34_historico.properties` remain.

- [ ] **Step 2: Add failing tests for Review Focus conditions**

Add:

```java
require(serviceBuildForAdminFails(), "Configured admin account was deletable");
require(secondExecutionSucceeds(), "Deletion was not idempotent");
require(accountFileSurvivesInjectedFailure(), "Account file disappeared after a prior deletion failure");
```

The injected `FileOperations` implementation must throw `IOException` when deleting one chosen TID file; after the exception, `_alice.properties` must still exist and retain `account_deletion_requested_at`.

- [ ] **Step 3: Run the new harness and verify it fails because the service is absent**

Compile it with the normal compatibility classpath and run:

```bash
java -cp "/tmp/compat-harness:/tmp/sokker-classes:${CP}" com.formulamanager.sokker.bo.AccountDeletionHarness
```

Expected: compile failure for missing `AccountDeletionService`.

- [ ] **Step 4: Implement `DeletionManifest` with exact allowlisted paths**

`buildManifest(login)` must:

1. load the existing user or throw `IOException("User not found: " + login)`;
2. reject `SystemUtil.getVar(SystemUtil.LOGIN)` case-insensitively;
3. use `usuario.getTid()` as the club TID;
4. enumerate every other user and set `deleteTeamData=false` if any other non-null `getTid()` equals the target TID;
5. always include exact personal log `logs/_<login>.log` when present;
6. when TID is exclusive, include exact current/history/junior files and `prueba/<tid>.properties` when present;
7. when TID is exclusive, enumerate only `backup/` regular files matching anchored regex:

```java
Pattern.compile("^" + Pattern.quote(String.valueOf(tid))
        + "_[0-9]+(?:_historico)?(?:_juveniles(?:_historico)?)?\\.properties$")
```

8. set the account file separately so it cannot be deleted until the end.

Sort file lists by canonical path for deterministic display/tests.

- [ ] **Step 5: Implement exact NTDB/scout reference cleanup**

Read `NTDB` as a map. Remove `map.remove(login)`. For every other entry, split its CSV scout list, trim tokens, remove only tokens `equalsIgnoreCase(login)`, preserve the order of every other token, then save the map once.

Do not use `String.replace(login, "")`.

- [ ] **Step 6: Implement retryable execution order**

`execute` must run in this order:

```text
1. rewrite NTDB/scout references
2. delete each allowlisted pre-account file
3. delete personal log if present
4. delete _<login>.properties last
```

A missing file is success. Any other deletion failure throws immediately. Never catch an `IOException` and continue to the account-file deletion.

The real `FileOperations` must use `Files.deleteIfExists(file.toPath())`; the test seam may inject failure.

- [ ] **Step 7: Run deletion and persistence harnesses**

Expected: `AccountDeletionHarness` PASS, `PersistenceRoundTrip` PASS, and `Borrar_jugadores.java` remains byte-unchanged relative to the task start.

- [ ] **Step 8: Add the harness to Java 8 CI and commit**

Append compile/run entries for `AccountDeletionHarness.java` without removing any existing harness.

```bash
git add sokker/src/com/formulamanager/sokker/bo/AccountDeletionService.java \
  .github/harness/AccountDeletionHarness.java .github/workflows/java8-compile.yml
git commit -m "feat: add safe complete account deletion service"
```

---

### Task 3: Remove cleartext password cookie/log behavior

**Files:**
- Modify: `sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java`
- Modify: `sokker/src/com/formulamanager/sokker/auxiliares/LoginExceptionExt.java`
- Modify: `sokker/src/com/formulamanager/sokker/auxiliares/Navegador.java`
- Modify: `sokker/src/com/formulamanager/sokker/auxiliares/SERVLET_ASISTENTE.java`
- Modify: `sokker/WebContent/jsp/asistente/login.jsp`
- Modify: `sokker/WebContent/jsp/asistente/asistente.jsp`
- Create: `.github/harness/CredentialHygieneHarness.java`
- Modify: `.github/workflows/java8-compile.yml`

**Interfaces:**
- Produces: `LoginExceptionExt(String message, String usuario)` without a stored password.
- Produces compatibility constructor `LoginExceptionExt(String message, String usuario, String ignoredPassword)` that delegates to the safe constructor and never stores the third argument.
- Produces package-private `static Cookie expiredLegacyPasswordCookie(String contextPath)` in `Login` for deterministic testing.

- [ ] **Step 1: Write the failing credential harness**

The harness must assert:

```java
Cookie cookie = Login.expiredLegacyPasswordCookie("/sokker");
require("apassword".equals(cookie.getName()), "Wrong legacy cookie name");
require("".equals(cookie.getValue()), "Legacy password cookie was not blanked");
require(cookie.getMaxAge() == 0, "Legacy password cookie was not expired");
require("/sokker/asistente".equals(cookie.getPath()), "Legacy cookie path would not match old cookie scope");
```

It must also read source/JSP files and forbid all of:

```text
cookie.apassword
getCookie(request, "apassword")
getContrasenya()
": " + ex.getContrasenya()
```

and require `LoginExceptionExt` to have no `contrasenya` field.

- [ ] **Step 2: Run it before implementation**

Expected: FAIL on the current cookie prefill and exception password field/log use.

- [ ] **Step 3: Make `LoginExceptionExt` password-blind**

Keep `usuario`; remove the password field/getter/setter. Add:

```java
public LoginExceptionExt(String msg, String usuario) {
    super(msg);
    this.usuario = usuario;
}

@Deprecated
public LoginExceptionExt(String msg, String usuario, String ignoredPassword) {
    this(msg, usuario);
}
```

Update `Login.java` and `Navegador.java` to call the two-argument constructor.

- [ ] **Step 4: Stop logging submitted passwords**

In `SERVLET_ASISTENTE`, keep the failed-user log entry but log only the exception message/username:

```java
if (ex.getUsuario() != null) {
    _log_linea(ex.getUsuario(), ex.getMessage());
}
```

Do not log a replacement hash or redacted copy; do not include the password at all.

- [ ] **Step 5: Expire the legacy cookie and remove password-prefill UI**

`Login.expiredLegacyPasswordCookie(contextPath)` returns an empty `apassword` cookie with `maxAge=0`, `HttpOnly=true`, `Secure=true`, and path `contextPath + "/asistente"`.

On every assistant login POST, add this expired cookie before redirecting, regardless of login success.

Keep the `alogin` convenience cookie. Remove the `recordar` checkbox because it no longer controls anything. Password inputs in `login.jsp` and `asistente.jsp` must have no value sourced from `apassword`.

- [ ] **Step 6: Run credential, persistence, client-identification, and Java 8 compile harnesses**

Expected: PASS and no regression in Sokker login/API client identification.

- [ ] **Step 7: Commit**

```bash
git add sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java \
  sokker/src/com/formulamanager/sokker/auxiliares/LoginExceptionExt.java \
  sokker/src/com/formulamanager/sokker/auxiliares/Navegador.java \
  sokker/src/com/formulamanager/sokker/auxiliares/SERVLET_ASISTENTE.java \
  sokker/WebContent/jsp/asistente/login.jsp sokker/WebContent/jsp/asistente/asistente.jsp \
  .github/harness/CredentialHygieneHarness.java .github/workflows/java8-compile.yml
git commit -m "fix: remove cleartext password retention"
```

---

### Task 4: Add authenticated deletion requests, admin ordering, and explicit impersonation state

**Files:**
- Create: `sokker/src/com/formulamanager/sokker/acciones/asistente/SolicitarEliminacionCuenta.java`
- Modify: `sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java`
- Modify: `sokker/src/com/formulamanager/sokker/acciones/asistente/LoginComo.java`
- Modify: `sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java`
- Modify: `sokker/WebContent/jsp/asistente/asistente.jsp`
- Create: `.github/harness/AccountDeletionUiHarness.java`
- Modify: `.github/workflows/java8-compile.yml`

**Interfaces:**
- Produces session key constant `LoginComo.ADMIN_IMPERSONATED_LOGIN = "admin_impersonated_login"`.
- Produces session key constant `Login.ADMIN_ORIGINAL_USER = "admin_original_user"`.
- Produces authenticated POST `/asistente/solicitar_eliminacion_cuenta` with no target-login parameter; it always marks `session.usuario`.
- Consumes `UsuarioBO.obtener_usuarios_con_borrado_solicitado()`.

- [ ] **Step 1: Write failing UI/admin harness cases**

Create two temp users with timestamps `1000` and `2000`, plus a normal user. Call `AsistenteBO.listar_usuarios()` and assert the HTML positions are:

```text
Pending account deletion requests
oldest-pending-login
newest-pending-login
normal-login
```

Read `Borrar_jugadores.java` and `asistente.jsp` and require the existing `/asistente/borrar_jugadores` route and `borrar_jugadores_click` still exist.

Read `LoginComo.java` and require it to set `ADMIN_IMPERSONATED_LOGIN` only after a real target user was successfully loaded.

- [ ] **Step 2: Run and verify failure**

Expected: FAIL because there is no pending section, request servlet, or impersonation marker.

- [ ] **Step 3: Preserve the original admin user in session**

On a successful login of the configured admin account, `Login.java` must set:

```java
request.getSession().setAttribute("admin", true);
request.getSession().setAttribute(ADMIN_ORIGINAL_USER, usuario);
request.getSession().removeAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
```

A normal login must clear stale admin-only deletion-target/impersonation attributes.

- [ ] **Step 4: Mark explicit impersonation in `LoginComo`**

After `UsuarioBO.leer_usuario(target, true)` succeeds and before redirect:

```java
request.getSession().setAttribute("usuario", usuario);
request.getSession().setAttribute(ADMIN_IMPERSONATED_LOGIN, usuario.getLogin());
```

Do not set the marker if the target does not exist or the caller is not admin.

- [ ] **Step 5: Add authenticated request servlet**

`SolicitarEliminacionCuenta` extends `SERVLET_ASISTENTE`; it accepts POST through the inherited flow, requires `login(request)`, gets `getUsuario(request).getLogin()`, and calls:

```java
UsuarioBO.solicitar_borrado(login, System.currentTimeMillis());
```

It must ignore/reject any supplied `usuario` parameter so a logged-in user cannot mark somebody else.

- [ ] **Step 6: Add the user-facing request control**

In the logged-in assistant account/settings area add a POST form to `/asistente/solicitar_eliminacion_cuenta`. If `UsuarioBO.obtener_fecha_solicitud_borrado(sessionScope.usuario.login)` is already present, show the pending state instead of creating duplicate timestamps on every render.

Do not place the final destructive admin button here; that arrives in Task 5 and is gated by explicit impersonation.

- [ ] **Step 7: Put pending deletion users first in the existing admin list**

Keep `AsistenteBO.listar_usuarios()` as the existing admin surface. Render a separated pending section first, sorted by the BO helper, each with the same `login_como`/inspection links used by ordinary users. Then render all remaining users with current behavior and count.

Never delete from this list.

- [ ] **Step 8: Run UI harness + full Java 8 suite and commit**

```bash
git add sokker/src/com/formulamanager/sokker/acciones/asistente/SolicitarEliminacionCuenta.java \
  sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java \
  sokker/src/com/formulamanager/sokker/acciones/asistente/LoginComo.java \
  sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java \
  sokker/WebContent/jsp/asistente/asistente.jsp \
  .github/harness/AccountDeletionUiHarness.java .github/workflows/java8-compile.yml
git commit -m "feat: add in-app account deletion requests"
```

---

### Task 5: Add admin-only deletion review and final confirmation

**Files:**
- Create: `sokker/src/com/formulamanager/sokker/acciones/asistente/EliminarCuentaAdmin.java`
- Create: `sokker/WebContent/jsp/asistente/eliminar_cuenta_admin.jsp`
- Modify: `sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java`
- Modify: `sokker/src/com/formulamanager/sokker/acciones/asistente/LoginComo.java`
- Modify: `sokker/WebContent/jsp/asistente/asistente.jsp`
- Create: `.github/harness/AdminAccountDeletionHarness.java`
- Modify: `.github/workflows/java8-compile.yml`

**Interfaces:**
- Produces GET/POST `/asistente/eliminar_cuenta_admin`.
- Produces session key `EliminarCuentaAdmin.PENDING_ADMIN_DELETE_TARGET = "pending_admin_delete_target"`.
- Consumes `AccountDeletionService.buildManifest(...)` and `execute(...)`.
- Requires `LoginComo.ADMIN_IMPERSONATED_LOGIN` to equal the current session user's login.

- [ ] **Step 1: Write failing authorization/flow harness**

Test the pure gate through a package-private static method:

```java
static boolean canDelete(boolean admin, String currentLogin, String impersonatedLogin, String configuredAdminLogin)
```

Required cases:

```text
normal user -> false
admin flag, no impersonation -> false
admin flag, impersonation for different login -> false
admin flag, exact impersonation -> true
configured admin as target -> false
```

Also source-check that the final servlet's destructive operation is only in `doPost`/`execute` after exact confirmation comparison, never in GET.

- [ ] **Step 2: Implement the authorization gate and review GET**

GET behavior:

1. normalize the `usuario` target only as an identifier, never as a filesystem path;
2. if not admin, save the target under `PENDING_ADMIN_DELETE_TARGET` and redirect to `/asistente` login;
3. if admin but the current explicit impersonation does not match target, redirect through `/asistente/login_como?usuario=<encoded>&destino=eliminar_cuenta_admin`;
4. once explicitly impersonating the target, call `AccountDeletionService.buildManifest(currentLogin)`, set it as request attribute `deletionManifest`, and forward to `eliminar_cuenta_admin.jsp`.

`LoginComo` must accept only a fixed destination enum/value (`eliminar_cuenta_admin`), not an arbitrary URL, to prevent open redirects.

- [ ] **Step 3: Resume email/public targets after admin login**

After successful configured-admin login, `Login.java` checks `PENDING_ADMIN_DELETE_TARGET`; if present it removes the session value and redirects only to:

```text
/asistente/login_como?usuario=<urlencoded-target>&destino=eliminar_cuenta_admin
```

A non-admin login clears the target and follows the normal redirect.

- [ ] **Step 4: Render the deletion manifest and exact confirmation**

The JSP must display:

```text
login
tid
whether TID data will be removed or preserved as shared
all allowlisted file paths relative to SystemUtil.path
scout/NTDB cleanup summary
```

The final form is POST-only and contains a text field `confirm_login`. Do not include a hidden authorization token as a substitute for the session checks.

- [ ] **Step 5: Execute only after exact confirmation**

POST must re-run `canDelete(...)`, require `confirm_login.equals(sessionUser.getLogin())`, rebuild the manifest server-side, then call `AccountDeletionService.execute(manifest)`.

Never trust a file list, TID, or login hidden field from the browser as the deletion manifest.

- [ ] **Step 6: Restore the original admin session after successful deletion**

After `execute` succeeds:

```java
Usuario adminOriginal = (Usuario) session.getAttribute(Login.ADMIN_ORIGINAL_USER);
session.setAttribute("usuario", adminOriginal);
session.removeAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
session.removeAttribute(PENDING_ADMIN_DELETE_TARGET);
```

If the original object is unavailable, reread the configured admin login. Redirect to `/asistente?mensaje=account_deleted`.

If deletion throws, do not restore/clear the impersonation state before rendering/redirecting the error; the target account remains present and pending for retry.

- [ ] **Step 7: Show the destructive button only while explicitly impersonating**

In `asistente.jsp`, render “Delete this account and all associated data” only if:

```text
sessionScope.admin is present
sessionScope.admin_impersonated_login == sessionScope.usuario.login
sessionScope.usuario.login != configured admin login
```

Link to GET review, not POST deletion.

- [ ] **Step 8: Run authorization, deletion, UI, persistence, and full compile harnesses and commit**

```bash
git add sokker/src/com/formulamanager/sokker/acciones/asistente/EliminarCuentaAdmin.java \
  sokker/WebContent/jsp/asistente/eliminar_cuenta_admin.jsp \
  sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java \
  sokker/src/com/formulamanager/sokker/acciones/asistente/LoginComo.java \
  sokker/WebContent/jsp/asistente/asistente.jsp \
  .github/harness/AdminAccountDeletionHarness.java .github/workflows/java8-compile.yml
git commit -m "feat: add admin-confirmed account deletion"
```

---

### Task 6: Add the public deletion request page and direct admin email link

**Files:**
- Modify: `sokker/src/com/formulamanager/sokker/auxiliares/EmailSenderService.java`
- Create: `sokker/src/com/formulamanager/sokker/acciones/asistente/EliminarCuenta.java`
- Create: `sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp`
- Create: `.github/harness/PublicDeletionRequestHarness.java`
- Modify: `.github/workflows/java8-compile.yml`

**Interfaces:**
- Produces: `public static void sendHtmlEmail(String email, String asunto, String html) throws AddressException, MessagingException, UnsupportedEncodingException`
- Produces public GET/POST `/asistente/eliminar_cuenta` without requiring a logged-in session.
- Produces package-private static `String buildAdminReviewUrl(String contextPath, String login)` and `String buildDeletionEmailHtml(...)` for deterministic tests.

- [ ] **Step 1: Write failing HTML-email and public-input harness cases**

Test `EmailSenderService` through a package-private message-builder shared by the transport method and assert an HTML-only message has `text/html` content without requiring attachments.

Test `EliminarCuenta.isSafePublicLogin(...)` with:

```text
alice -> true
" alice " -> true after trim
../../alice -> false
a/b -> false
a\\b -> false
a,b -> false
"alice\r\nBcc:x@y" -> false
```

Test `buildAdminReviewUrl("/sokker", "a b")` equals:

```text
https://raqueto.com/sokker/asistente/eliminar_cuenta_admin?usuario=a+b
```

and test the generated HTML escapes `<`, `>`, `&`, quotes in contact/message fields.

- [ ] **Step 2: Run before implementation**

Expected: FAIL because public deletion route and HTML-only mail helper do not exist.

- [ ] **Step 3: Refactor email construction without changing existing attachment behavior**

Keep existing `sendEmail(email, asunto, texto, listaArchivos)` signature and SMTP behavior. Extract common message creation and add `sendHtmlEmail(...)` so HTML content no longer requires a fake attachment.

Do not log SMTP password, request credentials, or form content beyond existing transport debug behavior; set JavaMail `mail.debug` to `false` so production SMTP exchanges are not dumped by default.

- [ ] **Step 4: Implement a public servlet independent of `SERVLET_ASISTENTE` login enforcement**

`EliminarCuenta` extends `HttpServlet` directly.

GET forwards to `/jsp/asistente/eliminar_cuenta.jsp`.

POST validates sizes before use:

```text
login: 1..100 characters after trim and no slash/backslash/comma/control chars
email: optional, <=254 characters
mensaje: optional, <=2000 characters
```

Do not call `UsuarioBO.leer_usuario` with unsafe input. The public form sends an email request only; it does not write `account_deletion_requested_at` and does not delete anything.

Return the same user-facing “request received for review” result regardless of whether the supplied login currently exists, so the endpoint does not become an account-enumeration oracle.

- [ ] **Step 5: Send the administrator email with a protected direct link**

Recipient selection:

```java
String recipient = SystemUtil.getVar("account_deletion_email");
if (Util.nnvl(recipient) == null) recipient = "tejedor@gmail.com";
```

The HTML body contains escaped login/contact/message, request timestamp, source `public web form`, and one link built by `buildAdminReviewUrl`.

The link contains only the URL-encoded login. Authorization and confirmation remain entirely server-side in Task 5.

- [ ] **Step 6: Run public-request and credential harnesses, add CI entries, and commit**

```bash
git add sokker/src/com/formulamanager/sokker/auxiliares/EmailSenderService.java \
  sokker/src/com/formulamanager/sokker/acciones/asistente/EliminarCuenta.java \
  sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp \
  .github/harness/PublicDeletionRequestHarness.java .github/workflows/java8-compile.yml
git commit -m "feat: add public account deletion requests"
```

---

### Task 7: Publish privacy policy, link both deletion routes, and complete i18n

**Files:**
- Create: `sokker/src/com/formulamanager/sokker/acciones/asistente/Privacidad.java`
- Create: `sokker/WebContent/jsp/asistente/privacidad.jsp`
- Modify: `sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp`
- Modify: `sokker/WebContent/jsp/asistente/eliminar_cuenta_admin.jsp`
- Modify: `sokker/WebContent/jsp/asistente/asistente.jsp`
- Modify: `sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_en.properties`
- Modify: `sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_es.properties`
- Modify: `sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_fr.properties`
- Modify: `sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_it.properties`
- Modify: `sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_sk.properties`
- Create: `.github/harness/PrivacyPagesHarness.java`
- Modify: `.github/workflows/java8-compile.yml`

**Interfaces:**
- Produces public GET `/asistente/privacidad`.
- Keeps public deletion URL `/asistente/eliminar_cuenta`.
- Consumes existing `I18nParityHarness` for exact key parity across all five bundles.

- [ ] **Step 1: Write failing public-page contract tests**

`PrivacyPagesHarness` must require:

```text
@WebServlet("/asistente/privacidad")
@WebServlet("/asistente/eliminar_cuenta")
```

It must read both public JSPs and forbid:

```text
googletagmanager
adsbygoogle
google_ad_client
```

It must read `asistente.jsp` and require visible links to both `/asistente/privacidad` and `/asistente/eliminar_cuenta`.

Add explicit required key names to the test, including:

```text
account.delete.request
account.delete.pending
account.delete.admin.title
account.delete.admin.confirm
account.delete.completed
privacy.title
privacy.account_data
privacy.sokker_data
privacy.logs
privacy.google_services
privacy.retention
privacy.deletion
privacy.contact
```

- [ ] **Step 2: Run privacy and i18n harnesses before changes**

Expected: FAIL for missing routes/keys/links.

- [ ] **Step 3: Add a public privacy servlet/JSP**

`Privacidad` extends `HttpServlet` and only forwards GET to `privacidad.jsp`; no login check.

The page must accurately state:

- Sokker Asistente stores account login, hashed account/Sokker password representation, preferences, notes/training configuration, and related team/player/training data needed by the service;
- the service contacts Sokker on the user's behalf when the user supplies Sokker credentials for an operation, but no cleartext password is intentionally retained after the credential-hygiene changes;
- per-user/security logs exist, with shared security/diagnostic logs retained up to 30 days under the current maintenance routine;
- the main controlled WebView pages contain Google advertising/analytics scripts, so Google may process browser/device/network identifiers under Google's own policies;
- users can request deletion in-app or at `/asistente/eliminar_cuenta`;
- club data is deleted when it is not shared by another active account; national-team shared datasets are not treated as the personal data of one selector;
- contact is through the public deletion/support path.

Do not claim encryption-at-rest, anonymity, sale/no-sale, or a legal basis not supported by the code/spec.

- [ ] **Step 4: Add links from the assistant experience**

Add a small footer/account-help area in `asistente.jsp` with resource-backed links to privacy and account deletion so they are reachable inside the Android WebView whether the user is logged in or not.

The logged-in deletion link points to the in-app request control from Task 4; the public deletion URL remains independently reachable for Play Console and users without the app.

- [ ] **Step 5: Add the exact same new keys to all language bundles**

Provide complete EN/ES/FR/IT/SK values. Preserve each file's existing encoding convention and special characters. Do not leave English missing-key placeholders such as `???privacy.title???`.

- [ ] **Step 6: Run `PrivacyPagesHarness` and `I18nParityHarness`**

Expected: PASS. Additionally grep for new hardcoded English/Spanish UI labels outside the legal policy body and move them to resource keys.

- [ ] **Step 7: Commit**

```bash
git add sokker/src/com/formulamanager/sokker/acciones/asistente/Privacidad.java \
  sokker/WebContent/jsp/asistente/privacidad.jsp \
  sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp \
  sokker/WebContent/jsp/asistente/eliminar_cuenta_admin.jsp \
  sokker/WebContent/jsp/asistente/asistente.jsp \
  sokker/src/com/formulamanager/sokker/idiomas/ApplicationResources_*.properties \
  .github/harness/PrivacyPagesHarness.java .github/workflows/java8-compile.yml
git commit -m "feat: add privacy and deletion policy pages"
```

---

### Task 8: Document Play Console declarations and make Android CI cover the web compliance contract

**Files:**
- Create: `docs/play-console-release-checklist.md`
- Modify: `.github/workflows/android.yml`
- Modify: `.github/workflows/java8-compile.yml` only if any new harness is not already registered.

**Interfaces:**
- Documents public URLs:
  - `https://raqueto.com/sokker/asistente/privacidad`
  - `https://raqueto.com/sokker/asistente/eliminar_cuenta`
- Documents Android package `com.raqueto.sokkerasistente`, target SDK 36, versionCode 11/versionName 1.11 at this stage.

- [ ] **Step 1: Write the release checklist from verified code behavior**

Include these fixed entries:

```text
Package: com.raqueto.sokkerasistente
Target SDK: 36
Native Android permission: INTERNET
Contains ads: Yes (web content loaded in controlled WebView includes AdSense)
Privacy policy URL: https://raqueto.com/sokker/asistente/privacidad
Account deletion URL: https://raqueto.com/sokker/asistente/eliminar_cuenta
App access: login required for core account content; provide reusable reviewer credentials
Play App Signing/upload key: manual Console/release step; private key never stored in Git
```

For Data Safety, enumerate observed data rather than guessing Console answers: account identifier/login, user content/preferences/notes, Sokker team/player/training data, credentials used transiently for authentication/operations, IP/security logs, and Google web advertising/analytics processing. Mark Console classification fields that depend on Google's current questionnaire as manual decisions to be answered from this inventory.

- [ ] **Step 2: Expand Android workflow branch/path coverage for this feature**

Allow feature branches to run Android CI:

```yaml
branches:
  - main
  - 'feature/**'
  - 'fix/android-*'
```

Add web compliance paths that affect the WebView experience:

```yaml
- 'sokker/WebContent/jsp/asistente/asistente.jsp'
- 'sokker/WebContent/jsp/asistente/privacidad.jsp'
- 'sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp'
```

- [ ] **Step 3: Add Android workflow contract checks without changing native app logic**

After proxy verification, add:

```bash
grep -q '/asistente/privacidad' sokker/WebContent/jsp/asistente/asistente.jsp
grep -q '/asistente/eliminar_cuenta' sokker/WebContent/jsp/asistente/asistente.jsp
! grep -Eq 'googletagmanager|adsbygoogle' sokker/WebContent/jsp/asistente/privacidad.jsp
! grep -Eq 'googletagmanager|adsbygoogle' sokker/WebContent/jsp/asistente/eliminar_cuenta.jsp
```

Keep existing APK/AAB application-id checks and artifact uploads unchanged.

- [ ] **Step 4: Run complete Java 8 and Android workflows**

Java expected: all sources compile under Java 8; every old and new compatibility harness passes; WAR assembles.

Android expected: Gradle tests, debug APK, release AAB, package `com.raqueto.sokkerasistente`, proxy integration, repository safety, compliance link checks, and both artifact uploads pass.

- [ ] **Step 5: Commit**

```bash
git add docs/play-console-release-checklist.md .github/workflows/android.yml .github/workflows/java8-compile.yml
git commit -m "docs: add Google Play release compliance checklist"
```

---

### Task 9: Final regression, round-trip, and deletion audit

**Files:**
- Verify only; modify files only if a failing check identifies a defect inside this spec's scope.

**Interfaces:**
- Consumes all tasks above.
- Produces no new product API.

- [ ] **Step 1: Re-run the critical persistence round trip**

Run `PersistenceRoundTrip` and explicitly inspect a generated `_alice.properties` after two saves. Confirm:

```text
unknown key retained
future usuario fields retained
future entrenamiento fields retained
account_deletion_requested_at retained
retired automatic-update secret absent
file rereads successfully
```

- [ ] **Step 2: Re-run account deletion against all ownership scenarios**

Run `AccountDeletionHarness` twice. Confirm unique TID cleanup, shared TID preservation, NT preservation, exact scout cleanup, idempotence, and failure-before-account-file behavior.

- [ ] **Step 3: Verify reset behavior was not repurposed**

Compare `sokker/src/com/formulamanager/sokker/acciones/asistente/Borrar_jugadores.java` against the branch base commit. It must be unchanged. Confirm `/asistente/borrar_jugadores` remains present in the UI contract harness.

- [ ] **Step 4: Search for cleartext credential retention regressions**

Run:

```bash
git grep -nE 'cookie\.apassword|getCookie\([^\n]*apassword|getContrasenya\(|actualizacion_automatica == null \? "" : Base64' -- sokker/src sokker/WebContent && exit 1 || true
```

Then run `CredentialHygieneHarness`.

- [ ] **Step 5: Verify public pages and admin authorization**

Run `PrivacyPagesHarness`, `PublicDeletionRequestHarness`, `AccountDeletionUiHarness`, and `AdminAccountDeletionHarness`. Confirm the public request creates no internal pending marker in its harness fixture, while the authenticated request does.

- [ ] **Step 6: Verify translation parity**

Run `I18nParityHarness`; manually inspect accented Spanish, French/Italian special characters, and Slovak diacritics in the new keys.

- [ ] **Step 7: Verify branch integration state before any merge**

Compare `main...feature/play-console-compliance`. If `main` advanced, integrate current `main` into the feature branch first, resolve only genuine overlaps, and rerun both workflows on the resulting head. Do not merge while behind main or while either workflow is red.

- [ ] **Step 8: Record remaining non-code release steps**

Confirm the checklist still marks these as manual and incomplete until the developer performs them in Play Console:

```text
Play App Signing / upload key
signed production AAB
Data Safety form
Ads declaration
App access reviewer credentials
Target audience/content rating
privacy/deletion URLs entered in Console
```

Do not claim the Play release is ready until those Console/signing steps are actually completed.

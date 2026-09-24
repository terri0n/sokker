# Sokker Asistente — Google Play compliance and account deletion design

Date: 2026-09-24

## Goal

Prepare Sokker Asistente for a new Google Play listing under Android package `com.raqueto.sokkerasistente`, while preserving existing server data and behavior. The priority is to meet Google Play account deletion/privacy requirements without changing the existing player-reset workflow or introducing destructive migrations.

The Android package rename already exists on the base branch used for this work. This design covers the remaining web/server/privacy changes and the associated validation.

## Non-goals

- Do not change or remove the existing player reset/recovery workflow. It remains available for users who return after several seasons or need to rebuild player data after compatibility issues.
- Do not migrate `.properties` storage to another persistence engine.
- Do not redesign authentication in this task. Existing stored password hashes remain compatible.
- Do not delete national-team datasets just because a selector account is deleted.
- Do not automatically delete an account from a public request or from an email link.
- Do not add a native Android account-deletion implementation that duplicates the web application.

## Google Play requirements being addressed

Current Google Play policy requires an app that allows account creation to provide both an in-app path to request account deletion and an external web resource where deletion can be requested. Account deletion must remove the account and associated user data rather than merely disable the account.

The app also requires a public privacy policy linked both from the application and Play Console. Data Safety declarations must reflect data collected/handled by the controlled WebView and third parties used by the web application. As of 2026-08-31, new mobile apps submitted to Play must target Android 16 / API 36; the recovered Android app already targets API 36.

Official references:

- https://support.google.com/googleplay/android-developer/answer/13327111
- https://support.google.com/googleplay/android-developer/answer/10144311
- https://support.google.com/googleplay/android-developer/answer/10787469
- https://support.google.com/googleplay/android-developer/answer/11926878

## User-facing deletion flows

### 1. Public web request

Provide a public, unauthenticated account-deletion page on Raqueto. It must clearly identify Sokker Asistente and be suitable for the Play Console “account deletion URL”.

The public form will request the Sokker Asistente login plus optional contact information/message needed for manual review. Submitting the form sends an email directly to the administrator using the existing `EmailSenderService`. It does not create an internal deletion queue and does not delete anything automatically.

The email is HTML and contains:

- requested login;
- submitted contact/message information;
- timestamp/source;
- a direct link to the admin deletion-review flow for that login.

The link itself contains no secret and is not an authorization token. Opening it requires an authenticated admin session. If the admin is not logged in, the requested target is retained in the session through the admin login flow. After a successful login as the configured administrator, the flow enters the requested account using the same authorization semantics as `LoginComo`, records an explicit impersonation marker in the session and redirects to the deletion review. A non-admin login must clear/ignore the pending admin target.

If the administrator is already logged in, the direct email link performs the same explicit admin-only “enter this user for deletion review” transition before showing the manifest. It never deletes from a GET request.

The email link never performs a one-click deletion.

### 2. Authenticated in-app request

A logged-in user gets a clear “Request account deletion” option inside Sokker Asistente. After confirmation, the application marks the account as pending deletion and records the request timestamp.

The pending state is stored as an independent property key in the existing user `.properties` file, for example:

`account_deletion_requested_at=<timestamp>`

It is intentionally not added to `Usuario.serializar()`. `UsuarioBO.grabar_usuario()` already loads the existing Properties object and preserves unknown/unmanaged keys, so this avoids changing the critical serialized `usuario=` field and avoids a migration of every account.

Round-trip requirement:

`existing properties -> Usuario -> save -> reread`

must retain `account_deletion_requested_at` and every other unknown key.

### 3. Admin queue

The existing administrator user list remains the main admin surface. It is not replaced by a separate panel.

When the administrator logs in, users with `account_deletion_requested_at` appear first in a clearly separated “Pending account deletion requests” section, ordered by request date. Each pending user has a review/enter button that uses the existing `LoginComo` flow. The rest of the users appear afterward with the current behavior preserved.

There is no destructive delete button directly in the list.

### 4. Final deletion from an impersonated account

`LoginComo` must set an explicit session marker such as `admin_impersonated_login=<login>`. Normal login/logout must clear it. This makes the destructive authorization test observable and prevents a `usuario=` request parameter from being sufficient.

The destructive action is only exposed when all of the following are true:

- the session still has the admin flag;
- `session.usuario` is the user being reviewed;
- `admin_impersonated_login` exactly matches `session.usuario.login`;
- the target is not the configured administrator account.

Only then is a “Delete this account and all associated data” action shown.

The action first displays a deletion manifest: account file, personal log, cross-account references and any club/TID data that is safe to delete. Final confirmation requires entering the exact login. The deletion request is POST-only.

The existing player reset controls remain completely separate and unchanged.

## Deletion ownership rules

Deletion must remove all data genuinely attributable to the account without deleting shared data.

### Always delete / clean for the account login

- `_<login>.properties` account file, last in the operation;
- `logs/_<login>.log` individual log;
- the account deletion request marker (implicitly removed with the account file);
- the login’s own entry in the `NTDB` map;
- every occurrence of the login inside other users’ scout lists stored in `NTDB`;
- any other explicitly identified persisted login reference found during implementation review.

No broad textual search-and-delete will be used. Shared files are parsed according to their known format and rewritten while preserving unrelated keys/values.

### Club data: delete only when the club is no longer referenced

Club data is keyed by the account’s real `usuario.tid`, not the currently selected `def_tid`.

Before deleting TID data, enumerate all remaining user accounts. If another active user references the same club TID, no TID-scoped file or backup is deleted.

If no other account references that TID, delete the known club-specific data:

- `<tid>.properties`;
- `<tid>_historico.properties`;
- `<tid>_juveniles.properties`;
- `<tid>_juveniles_historico.properties`;
- test-user data `prueba/<tid>.properties` where present;
- TID backups matching explicit safe filename patterns under the backup directory, including current/historical variants that actually exist in the codebase.

Deletion must use an explicit manifest/regex allowlist. It must not recursively remove arbitrary directories or files sharing only a loose prefix.

### National-team data

Do not delete national-team player/history datasets merely because the selector account is deleted. Those datasets are shared selection data, not private data owned by that login. Personal login references are still removed.

### Global security logs

Do not rewrite global/shared security logs by arbitrary string substitution. Existing maintenance already trims logs to a 30-day retention window. The privacy policy will disclose this limited retention for security/diagnostic purposes.

## Failure, idempotency and atomicity behavior

Filesystem deletion cannot be fully transactional, so the executor must be preflighted, idempotent and retry-safe.

Before changing anything it builds the full deletion manifest, validates that every target is inside the allowed data directories/patterns, computes whether the TID is shared and checks that shared files to be rewritten are readable/writable.

Execution order:

1. Build and validate the complete deletion manifest.
2. Remove cross-account references such as NTDB/scout links using safe parse/rewrite operations.
3. Remove TID-scoped data only if ownership checks permit it.
4. Remove the individual account log.
5. Remove `_<login>.properties` last.

Every operation is idempotent: an intended file that is already absent or an already-removed reference counts as completed, not as a new error. If a failure occurs after some intended data has already been deleted, the account file remains present and marked pending; a second execution recomputes the manifest and safely continues the remaining work. The UI reports failure rather than declaring success.

The account is considered deleted only after the account file itself is removed successfully. The admin account is explicitly protected from deletion.

## Privacy policy

Add a public privacy policy page accessible without login and link it visibly from the application. It must describe, accurately and narrowly:

- account identifiers and preferences stored by Sokker Asistente;
- Sokker team/player/training data downloaded and stored for the service;
- authentication handling;
- server/security logs and their retention;
- Google Analytics / advertising behavior present in the controlled WebView;
- purposes of processing;
- account/data deletion routes;
- what data may be retained temporarily for security/legal reasons;
- contact path.

The public privacy and deletion pages should be minimal pages and should not unnecessarily load advertising/analytics scripts themselves.

A repository document will map actual application behavior to the fields the developer must complete manually in Play Console: Data Safety, ads, app access/reviewer credentials, target audience/content rating and deletion/privacy URLs.

## Credential and secret hygiene

The review found two unsafe legacy behaviors that must be corrected as part of this work:

1. the “remember password” flow stores `apassword` in a browser cookie in clear text;
2. login failures can write the entered password to a log via `LoginExceptionExt.getContrasenya()`.

Required changes:

- stop writing `apassword` to cookies;
- expire/remove an existing `apassword` cookie when encountered so legacy cleartext copies disappear from active browsers;
- keep only the login identifier as the optional remembered browser value;
- where Sokker authentication is required later, require the password again or keep it only for the minimum lifetime needed by the current request/session; never persist it to disk/cookie;
- never include entered passwords in application logs or deletion-request emails;
- remove/neutralize future persistence of the retired `actualizacion_automatica` plaintext/Base64 secret while keeping old files readable and preserving all unrelated/unknown properties.

The existing account password hash format is not migrated in this task. A safe password-hash migration is independent work and must not be mixed into Play compliance without a dedicated compatibility plan.

## Email implementation

Reuse `EmailSenderService` and extend it cleanly so HTML mail is supported without requiring attachments. Existing attachment email behavior must remain compatible.

The deletion-request email’s direct admin link contains only the requested login as an identifier. Authorization is always enforced server-side by the existing admin session mechanism, the explicit impersonation marker and final confirmation.

The recipient should be an administrator/support address controlled by Sokker Asistente. Prefer an existing configurable server value if available; otherwise introduce a single explicit configuration point rather than scattering the address across new code.

## Internationalization

All new user-visible application text must use resource keys. New keys must be present across all `ApplicationResources_xx.properties` bundles so key parity is maintained. Spanish/English text must be correct; existing language conventions are preserved. Slovak resources must also contain the same new keys when affected.

The public privacy/legal content may use a stable primary language plus translated UI/navigation, but must never show missing-resource placeholders.

## Android scope

Keep the already-approved Android identity:

- package/application ID: `com.raqueto.sokkerasistente`;
- target SDK: 36.

No duplicate native account-deletion flow is added. Because the Android app is a controlled WebView shell, the web application’s privacy and deletion links must be reachable in the app.

CI should continue validating the actual built Android application ID and output artifacts.

## Tests and validation

Implementation is not complete until all relevant checks pass.

### Persistence

- Existing `Usuario.properties` loads and saves without losing unknown keys.
- `account_deletion_requested_at` survives a `leer_usuario -> grabar_usuario -> leer` round trip.
- Old user files without the key remain valid.
- Retired plaintext automatic-update secrets are not newly persisted while unrelated unknown fields remain intact.

### Account deletion

Create harness/test coverage for at least:

- unique user + unique TID: account data, club data, allowed backups, personal log and cross-references are removed;
- two users sharing a TID: deleting one removes personal data/references but preserves all TID data;
- national-team relationship: shared NT datasets remain;
- pending request: user appears in the admin pending section before normal users;
- deletion is idempotent and a retry completes a deliberately interrupted run;
- admin account cannot be deleted;
- final action cannot be invoked by a normal user, by passing an arbitrary `usuario` parameter, or without a matching `admin_impersonated_login` marker;
- normal login/logout clears the impersonation marker;
- existing player reset behavior remains present and unchanged.

### Credential hygiene

- login no longer writes a cleartext password cookie;
- existing `apassword` cookie is expired/cleared;
- login exceptions/logs never contain the submitted password;
- deletion request email never contains credentials.

### Public policy/deletion pages

- accessible without login;
- deletion form sends an email but does not delete/mark an internal account automatically;
- email admin link is correctly URL-encoded, contains no secret and cannot delete via GET;
- unauthenticated email-link flow returns to deletion review only after a successful admin login;
- privacy and deletion links are visible from the application.

### Build / regression

- Java 8 compile workflow passes;
- existing compatibility harnesses pass, especially persistence-related ones;
- Android tests/build pass and APK/AAB still report `com.raqueto.sokkerasistente`;
- translation key parity is checked;
- no unrelated refactorings.

## Play Console work that remains manual

Code can prepare the app, but the developer still must complete Play Console declarations and publishing steps:

- set privacy-policy URL;
- set external account-deletion URL;
- complete Data Safety according to the verified web/WebView behavior, including relevant third parties;
- declare ads accurately;
- provide reusable review credentials/app access instructions;
- complete target audience and content rating;
- configure Play App Signing/upload key and upload the signed AAB.

The repository checklist produced by this work will record the exact values known from the code and clearly mark items that depend on Play Console/account-specific decisions.
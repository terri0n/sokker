# Sokker Asistente — Google Play release checklist

Date: 2026-09-24

This checklist records values verified from the repository. Items marked **Manual** must still be completed in Google Play Console or release infrastructure.

## Android identity and platform

- Package / application ID: `com.raqueto.sokkerasistente`
- Namespace: `com.raqueto.sokkerasistente`
- Target SDK: 36
- Compile SDK: 36
- Current release metadata: versionCode 11, versionName 1.11
- Native Android permission: `INTERNET`
- APK/AAB CI verifies the built APK application ID with `apkanalyzer`.
- Private signing keys and passwords are intentionally not stored in Git.

## Store declarations

- Contains ads: **Yes**. The controlled Sokker Asistente WebView loads web pages that include Google AdSense.
- Privacy policy URL: `https://raqueto.com/sokker/asistente/privacidad`
- Account deletion URL: `https://raqueto.com/sokker/asistente/eliminar_cuenta`
- App access: core account content requires login. **Manual:** provide Google reviewers with reusable review credentials and any instructions needed to reach authenticated functionality.
- Target audience / content rating: **Manual:** complete the current Play Console questionnaires from the real app content.
- Play App Signing / upload key: **Manual:** create and retain the upload key securely, configure release signing outside Git, and upload the signed AAB.

## Account deletion behavior

- A logged-in user can mark their Sokker Asistente account for deletion from the application.
- Pending deletion requests are shown first in the administrator user list.
- Public requests are accepted without requiring the app and are emailed to the administrator.
- The email contains a direct link to the administrator review page for the requested login; the link is not an authorization token and never deletes by GET.
- Final deletion requires an authenticated administrator, explicit impersonation of the target account, a POST request and exact-login confirmation.
- The existing player-reset feature remains separate and is not account deletion.
- Account-specific data and exact NTDB/scout references are removed.
- Club/TID data is removed only when no other active account references the same real club TID.
- Shared national-team datasets are preserved.
- The account `.properties` file is deleted last so a failed deletion remains visible and retryable.

## Data Safety inventory

Use this inventory when answering the current Google Play Data Safety questionnaire. Do not mechanically map it to a Console category without checking Google's current wording.

Observed data handled by Sokker Asistente:

- Sokker Asistente account identifier/login.
- Stored password hash/representation used for the existing account authentication flow; legacy clear-text/Base64 automatic-update storage is no longer persisted on save.
- User preferences, notes and training configuration.
- Sokker account/login identifier.
- Sokker credentials supplied transiently when the user requests authentication or an operation; clear-text password is not intentionally retained by the application after the credential-hygiene changes.
- Sokker team, player, junior, training and historical data required for assistant functionality.
- Server/security/diagnostic logs, including normal web/network metadata such as IP information where produced by the server stack. Shared security/diagnostic logs are retained for up to 30 days under the existing maintenance routine.
- Google advertising/analytics processing on the main controlled web pages. Google may receive browser, device, network and advertising identifiers according to the Google services in use.

**Manual:** answer the Play Console collection/sharing/ephemeral/required-vs-optional questions from this verified inventory and the exact current questionnaire.

## Public policy pages

The following pages must remain accessible without a Sokker Asistente login:

- `https://raqueto.com/sokker/asistente/privacidad`
- `https://raqueto.com/sokker/asistente/eliminar_cuenta`

They intentionally do not load the application's AdSense/Analytics scripts.

## Pre-upload checks

Before production upload:

1. Java 8 compile and compatibility harnesses green.
2. Persistence round-trip green, including unknown/future `.properties` fields and deletion marker.
3. Complete deletion harness green for unique TID, shared TID, national-team preservation, retry and admin protection.
4. Credential-hygiene harness green.
5. i18n parity green for EN/ES/FR/IT/SK.
6. Android tests/build green with package `com.raqueto.sokkerasistente`.
7. Release AAB signed with the securely retained upload key.
8. **Manual:** complete Privacy Policy, Account deletion URL, Data Safety, Ads, App access, target audience and content rating in Play Console.
9. **Manual:** perform a Play internal-test install and real login/navigation smoke test before production rollout.

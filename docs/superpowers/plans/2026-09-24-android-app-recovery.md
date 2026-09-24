# Sokker Asistente Android Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Recover the existing Sokker Asistente Android app into the repository, preserve its deployed WebView behavior, fix the Sokker proxy so `X-Sokker-Client` survives, and produce an API-36 Android App Bundle suitable for a first Google Play release.

**Architecture:** Keep the recovered app as a thin Java WebView shell. Preserve `MainActivity` behavior and extract only pure proxy-policy decisions into a small Java helper so URL scoping, header forwarding, legacy-login body reconstruction and HTTP error handling can be unit-tested without a device. Keep server code and Android release signing independent.

**Tech Stack:** Java, Android SDK 36, Android Gradle Plugin 8.10.1, Gradle >= 8.11.1, JDK 17, AndroidX, JUnit 4, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-24-android-app-recovery-design.md`

## Global Constraints

- Preserve application id `com.formulamanager.sokker.asistente`.
- Use `versionCode 11` and `versionName "1.11"` for the recovered Play-ready build.
- Use `compileSdk 36` and `targetSdk 36`.
- Preserve `minSdk 21` unless a separately reviewed blocker proves that impossible.
- Preserve the WebView entry URL `https://raqueto.com/sokker/asistente`.
- Preserve the existing Java implementation; do not migrate the app to Kotlin.
- Keep `X-Sokker-Client` aligned with the server/web identifier and treat it as a public client identifier, not a secret.
- Reconstruct credentials only for the known legacy `https://sokker.org/start.php?session=xml...` login request.
- Do not commit `.gradle/`, `.idea/`, `build/`, `app/build/`, `local.properties`, APK/AAB files, keystores, upload keys or signing passwords.
- Do not couple Android CI to `.github/workflows/java8-compile.yml`.
- Do not merge unrelated server recovery branches merely to build Android.

## Review Focus

1. A lookalike host such as `https://sokker.org.evil.example/...` must never be treated as Sokker.
2. Encoded credentials containing `%`, `+`, `&`, spaces or non-ASCII characters must remain byte-for-byte URL encoded when the legacy form body is reconstructed.
3. An existing `X-Sokker-Client` header must be preserved case-insensitively and must not be duplicated under a second casing.
4. HTTP 4xx/5xx responses, especially failed login, must return their status/error stream to WebView instead of becoming transport exceptions.
5. Missing `ilogin` or `ipassword` on `/start.php?session=xml` must never cause Android to synthesize a partial credential body or log credential material.

---

## File Structure

The implementation branch will add the recovered project under `android/SokkerAsistente/`.

Recovered files kept essentially as-is:

- `android/SokkerAsistente/build.gradle` — root Android build configuration; keep recovered AGP 8.10.1 unless verification proves otherwise.
- `android/SokkerAsistente/settings.gradle` — recovered project/module declaration.
- `android/SokkerAsistente/gradle.properties` — recovered Gradle project settings.
- `android/SokkerAsistente/gradlew` — recovered Gradle wrapper launcher.
- `android/SokkerAsistente/gradlew.bat` — recovered Windows wrapper launcher.
- `android/SokkerAsistente/gradle/wrapper/gradle-wrapper.jar` — recovered wrapper binary.
- `android/SokkerAsistente/gradle/wrapper/gradle-wrapper.properties` — wrapper version/configuration.
- `android/SokkerAsistente/app/build.gradle` — Android application module configuration.
- `android/SokkerAsistente/app/proguard-rules.pro` — recovered release rules.
- `android/SokkerAsistente/app/src/main/AndroidManifest.xml` — recovered app manifest.
- `android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java` — WebView lifecycle/UI and native Sokker interception.
- `android/SokkerAsistente/app/src/main/res/layout/activity_main.xml` — recovered WebView layout.
- `android/SokkerAsistente/app/src/main/res/xml/network_security_config.xml` — recovered network security configuration.
- `android/SokkerAsistente/app/src/main/res/values/*.xml` and `mipmap-*/*` — recovered resources/icons.

New focused files:

- `android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/SokkerProxy.java` — pure/testable URL, header, form-body and response-stream policy used by `MainActivity`.
- `android/SokkerAsistente/app/src/test/java/com/formulamanager/sokker/asistente/SokkerProxyTest.java` — unit tests for proxy policy and failure modes.
- `android/SokkerAsistente/README.md` — local build, Play bundle and signing/upload-key instructions without secrets.
- `.github/workflows/android.yml` — isolated Android test/build workflow.

Files deliberately not imported from the archive:

- `SokkerAsistente APP/.gradle/**`
- `SokkerAsistente APP/.idea/**`
- `SokkerAsistente APP/build/**`
- `SokkerAsistente APP/app/build/**`
- `SokkerAsistente APP/local.properties`
- generated APK/AAB outputs

---

### Task 1: Recover a clean, reproducible Android project

**Files:**
- Create: `android/SokkerAsistente/.gitignore`
- Create: `android/SokkerAsistente/build.gradle`
- Create: `android/SokkerAsistente/settings.gradle`
- Create: `android/SokkerAsistente/gradle.properties`
- Create: `android/SokkerAsistente/gradlew`
- Create: `android/SokkerAsistente/gradlew.bat`
- Create: `android/SokkerAsistente/gradle/wrapper/gradle-wrapper.jar`
- Create: `android/SokkerAsistente/gradle/wrapper/gradle-wrapper.properties`
- Create: `android/SokkerAsistente/app/build.gradle`
- Create: `android/SokkerAsistente/app/proguard-rules.pro`
- Create: `android/SokkerAsistente/app/src/main/**` from the recovered project inputs
- Preserve initially: recovered `app/src/test/.../ExampleUnitTest.java` and `app/src/androidTest/.../ExampleInstrumentedTest.java`

**Interfaces:**
- Consumes: project file `SokkerAsistente APP.rar` as the behavioral/build reference.
- Produces: a clean `android/SokkerAsistente` Gradle project that can be built without archive-only generated state.

- [ ] **Step 1: Create the implementation branch from the latest `main`, not from any unmerged server-fix branch**

```bash
git fetch origin
git switch -c feature/android-play-release origin/main
```

Copy the approved spec and this plan into the implementation branch unchanged if they are not already present there.

- [ ] **Step 2: Extract the recovered project into a temporary directory**

Use a RAR5-capable extractor, for example:

```bash
rm -rf /tmp/sokker-android-recovered
mkdir -p /tmp/sokker-android-recovered
7z x "/mnt/data/project_files/SokkerAsistente APP.rar" -o/tmp/sokker-android-recovered
```

Expected root after extraction:

```text
/tmp/sokker-android-recovered/SokkerAsistente APP/
```

- [ ] **Step 3: Copy only reproducible project inputs**

```bash
rm -rf android/SokkerAsistente
mkdir -p android/SokkerAsistente
rsync -a \
  --exclude '.gradle/' \
  --exclude '.idea/' \
  --exclude 'build/' \
  --exclude 'app/build/' \
  --exclude 'local.properties' \
  "/tmp/sokker-android-recovered/SokkerAsistente APP/" \
  android/SokkerAsistente/
```

Do not copy any APK/AAB from archive build outputs.

- [ ] **Step 4: Harden the Android subtree ignore rules**

Set `android/SokkerAsistente/.gitignore` to include at least:

```gitignore
.gradle/
.idea/
/local.properties
/build/
/app/build/
*.apk
*.aab
*.jks
*.keystore
key.properties
```

Keep any additional useful recovered ignore rules that do not weaken these exclusions.

- [ ] **Step 5: Verify no generated or secret-like files are staged/tracked**

Run:

```bash
git add android/SokkerAsistente
git status --short
! git ls-files android/SokkerAsistente | grep -E '(^|/)(\.gradle|\.idea|build)/|local\.properties$|\.(apk|aab|jks|keystore)$|key\.properties$'
```

Expected: the grep command returns no matches.

- [ ] **Step 6: Verify recovered identity before changing versions**

Run:

```bash
grep -R -n 'com.formulamanager.sokker.asistente' android/SokkerAsistente/app
find android/SokkerAsistente/app/src/main -type f | sort
```

Confirm the package/application id is `com.formulamanager.sokker.asistente`, `MainActivity.java` exists, and the recovered `sa*` icon resources are present.

- [ ] **Step 7: Establish the recovered baseline build**

Use JDK 17 and the recovered wrapper:

```bash
cd android/SokkerAsistente
chmod +x gradlew
./gradlew --version
./gradlew testDebugUnitTest assembleDebug
```

Expected: Java 17 is used and both Gradle tasks succeed before proxy behavior is modified.

If the recovered wrapper is older than AGP 8.10's supported minimum, change only `gradle/wrapper/gradle-wrapper.properties` to Gradle 8.11.1 and rerun the same commands. AGP 8.10 supports API 36 and requires Gradle >= 8.11.1 / JDK 17.

- [ ] **Step 8: Commit the clean recovered baseline**

```bash
git add android/SokkerAsistente docs/superpowers/specs docs/superpowers/plans
git commit -m "build: recover Android application project"
```

---

### Task 2: Move the recovered app to the Play-ready API/version baseline

**Files:**
- Modify: `android/SokkerAsistente/app/build.gradle`
- Modify only if required by Task 1 compatibility check: `android/SokkerAsistente/gradle/wrapper/gradle-wrapper.properties`

**Interfaces:**
- Consumes: reproducible recovered Gradle project from Task 1.
- Produces: API-36 app build with package identity unchanged and version 11/1.11.

- [ ] **Step 1: Write a build-contract test as a shell assertion before changing Gradle values**

Run against the recovered baseline:

```bash
cd "$(git rev-parse --show-toplevel)"
set -e
APP_GRADLE=android/SokkerAsistente/app/build.gradle
grep -Eq 'compileSdk(Version)?[[:space:]]+36' "$APP_GRADLE"
grep -Eq 'targetSdk(Version)?[[:space:]]+36' "$APP_GRADLE"
grep -Eq 'versionCode[[:space:]]+11' "$APP_GRADLE"
grep -Eq 'versionName[[:space:]]+["\x27]1\.11["\x27]' "$APP_GRADLE"
```

Expected: FAIL on the recovered version 1.10/API-35 file.

- [ ] **Step 2: Make the minimal build configuration change**

In `app/build.gradle` set the existing Android fields to:

```gradle
compileSdkVersion 36

// inside defaultConfig
targetSdkVersion 36
versionCode 11
versionName "1.11"
```

If the recovered file uses modern property syntax (`compileSdk 35`, `targetSdk 35`), preserve that syntax and change only the numeric values.

Do not change `minSdkVersion 21` / `minSdk 21`.

Keep recovered AGP 8.10.1. Official AGP 8.10 compatibility includes API 36, JDK 17 and Gradle >= 8.11.1, so an AGP-major upgrade is not part of this migration.

- [ ] **Step 3: Run the build-contract assertion again**

Run the exact Step 1 shell assertion.

Expected: PASS.

- [ ] **Step 4: Build and run existing tests on API-36 configuration**

```bash
cd android/SokkerAsistente
./gradlew clean testDebugUnitTest assembleDebug bundleRelease
```

Expected: all tasks succeed; `app/build/outputs/bundle/release/app-release.aab` exists and is non-empty.

- [ ] **Step 5: Verify identity and version from Gradle output/config**

```bash
cd "$(git rev-parse --show-toplevel)"
grep -R -n 'com.formulamanager.sokker.asistente' android/SokkerAsistente/app
stat -c '%s %n' android/SokkerAsistente/app/build/outputs/bundle/release/app-release.aab
```

Expected: package id unchanged; AAB size > 0.

- [ ] **Step 6: Commit the API/version migration**

```bash
git add android/SokkerAsistente/app/build.gradle android/SokkerAsistente/gradle/wrapper/gradle-wrapper.properties
git commit -m "build: target Android API 36"
```

---

### Task 3: Pin the Sokker proxy policy with unit tests

**Files:**
- Create: `android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/SokkerProxy.java`
- Replace: `android/SokkerAsistente/app/src/test/java/com/formulamanager/sokker/asistente/ExampleUnitTest.java` with focused proxy tests, or delete it after adding `SokkerProxyTest.java`
- Create: `android/SokkerAsistente/app/src/test/java/com/formulamanager/sokker/asistente/SokkerProxyTest.java`

**Interfaces:**
- Consumes: Java standard library only for policy helpers; no Android `Context` required.
- Produces these package-visible/static methods for `MainActivity`:
  - `static boolean isSokkerUrl(String url)`
  - `static boolean isLegacyXmlSessionLogin(String method, String url)`
  - `static String buildLegacyLoginBody(String url)`
  - `static Map<String,String> buildForwardHeaders(Map<String,String> requestHeaders)`
  - `static InputStream responseStream(HttpURLConnection connection) throws IOException`
  - constant `static final String SOKKER_CLIENT_HEADER = "X-Sokker-Client"`
  - constant `static final String SOKKER_CLIENT_KEY = "skc_bae02f2686bf9038d248"`

- [ ] **Step 1: Write failing URL-scope tests**

Create tests equivalent to:

```java
@Test
public void exactSokkerHttpsHostIsProxied() {
    assertTrue(SokkerProxy.isSokkerUrl("https://sokker.org/api/current"));
}

@Test
public void lookalikeHostIsNotProxied() {
    assertFalse(SokkerProxy.isSokkerUrl("https://sokker.org.evil.example/api/current"));
    assertFalse(SokkerProxy.isSokkerUrl("http://sokker.org/api/current"));
    assertFalse(SokkerProxy.isSokkerUrl("https://raqueto.com/sokker/asistente"));
}
```

- [ ] **Step 2: Write failing legacy-login recognition/body tests**

```java
@Test
public void legacyLoginRequiresExactEndpointAndBothCredentials() {
    assertTrue(SokkerProxy.isLegacyXmlSessionLogin(
        "POST",
        "https://sokker.org/start.php?session=xml&ilogin=terrion&ipassword=a%2Bb%26c"));

    assertFalse(SokkerProxy.isLegacyXmlSessionLogin(
        "GET",
        "https://sokker.org/start.php?session=xml&ilogin=terrion&ipassword=x"));
    assertFalse(SokkerProxy.isLegacyXmlSessionLogin(
        "POST",
        "https://sokker.org/start.php?session=xml&ilogin=terrion"));
    assertFalse(SokkerProxy.isLegacyXmlSessionLogin(
        "POST",
        "https://sokker.org/api/auth/login?ilogin=terrion&ipassword=x"));
}

@Test
public void legacyBodyPreservesRawEncodedCredentialValues() {
    assertEquals(
        "ilogin=jos%C3%A9+test&ipassword=a%2Bb%26c%25",
        SokkerProxy.buildLegacyLoginBody(
            "https://sokker.org/start.php?session=xml&ilogin=jos%C3%A9+test&ipassword=a%2Bb%26c%25"));
}
```

`buildLegacyLoginBody` must return `null` when the URL is not a complete legacy login request.

- [ ] **Step 3: Write failing client-header tests including casing**

```java
@Test
public void addsClientIdentifierWhenMissing() {
    Map<String,String> result = SokkerProxy.buildForwardHeaders(Collections.singletonMap("Accept", "*/*"));
    assertEquals(SokkerProxy.SOKKER_CLIENT_KEY, result.get("X-Sokker-Client"));
}

@Test
public void preservesExistingIdentifierWithoutCaseDuplicate() {
    Map<String,String> incoming = new LinkedHashMap<>();
    incoming.put("x-sokker-client", "custom-existing-value");

    Map<String,String> result = SokkerProxy.buildForwardHeaders(incoming);

    int clientHeaderCount = 0;
    String value = null;
    for (Map.Entry<String,String> entry : result.entrySet()) {
        if (entry.getKey().equalsIgnoreCase("X-Sokker-Client")) {
            clientHeaderCount++;
            value = entry.getValue();
        }
    }
    assertEquals(1, clientHeaderCount);
    assertEquals("custom-existing-value", value);
}
```

Also assert that hop-by-hop/request-derived headers `Host`, `Connection` and `Content-Length` are not blindly forwarded.

- [ ] **Step 4: Write a failing HTTP-error-stream test**

In the test class define a tiny fake `HttpURLConnection` subclass whose `getResponseCode()` returns 401, `getErrorStream()` returns a known stream and `getInputStream()` throws. Assert:

```java
@Test
public void httpErrorsUseErrorStream() throws Exception {
    byte[] body = readAll(SokkerProxy.responseStream(new Fake401Connection("bad login")));
    assertEquals("bad login", new String(body, StandardCharsets.UTF_8));
}
```

Add the mirror test for HTTP 200 using `getInputStream()`.

- [ ] **Step 5: Run the tests to verify RED**

```bash
cd android/SokkerAsistente
./gradlew testDebugUnitTest --tests 'com.formulamanager.sokker.asistente.SokkerProxyTest'
```

Expected: FAIL because `SokkerProxy` does not yet exist.

- [ ] **Step 6: Implement the minimal pure helper**

`isSokkerUrl` must parse with `java.net.URI` and require:

```java
"https".equalsIgnoreCase(uri.getScheme())
&& "sokker.org".equalsIgnoreCase(uri.getHost())
```

`isLegacyXmlSessionLogin` must require:

```text
POST
scheme=https
host=sokker.org
path=/start.php
raw query session=xml
raw query ilogin present
raw query ipassword present
```

Parse the raw query by splitting only on `&` and the first `=`; compare parameter names using decoded-safe ASCII names, but return the original raw values for body reconstruction so encoded credential bytes are not decoded/re-encoded.

`buildForwardHeaders` must:

- copy incoming headers except `Host`, `Connection`, and `Content-Length` (case-insensitive);
- preserve an existing client header under its existing casing/value;
- add canonical `X-Sokker-Client: skc_bae02f2686bf9038d248` only when no case-insensitive equivalent exists.

`responseStream` must call `getResponseCode()` first; for status >= 400 return `getErrorStream()` when non-null, otherwise fall back to `getInputStream()`.

- [ ] **Step 7: Run the focused tests to verify GREEN**

```bash
cd android/SokkerAsistente
./gradlew testDebugUnitTest --tests 'com.formulamanager.sokker.asistente.SokkerProxyTest'
```

Expected: PASS.

- [ ] **Step 8: Run the full Android unit suite**

```bash
./gradlew testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 9: Commit the tested proxy policy**

```bash
git add android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/SokkerProxy.java \
        android/SokkerAsistente/app/src/test/java/com/formulamanager/sokker/asistente
git commit -m "test: define Sokker Android proxy contract"
```

---

### Task 4: Integrate the tested proxy policy into `MainActivity`

**Files:**
- Modify: `android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java`
- Test: `android/SokkerAsistente/app/src/test/java/com/formulamanager/sokker/asistente/SokkerProxyTest.java`

**Interfaces:**
- Consumes: `SokkerProxy` methods from Task 3.
- Produces: WebView interception that preserves deployed behavior while forwarding/inserting the Sokker identifier and restricting credential reconstruction to legacy login.

- [ ] **Step 1: Add one source-contract regression assertion for the current header-loss behavior**

Before modifying `MainActivity`, run:

```bash
cd "$(git rev-parse --show-toplevel)"
! grep -q 'buildForwardHeaders' android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java
```

Expected: PASS, documenting that the recovered `MainActivity` does not yet use the tested header-forwarding policy.

- [ ] **Step 2: Replace only the proxy decision/body/header portions of the existing interception code**

Keep the recovered WebView setup, JavaScript dialogs, zoom, orientation/configuration handling, back behavior and response/CORS handling intact.

In the existing `shouldInterceptRequest(WebView, WebResourceRequest)` path:

```java
String url = request.getUrl().toString();
if (!SokkerProxy.isSokkerUrl(url)) {
    return null;
}

HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
connection.setRequestMethod(request.getMethod());

for (Map.Entry<String,String> header :
        SokkerProxy.buildForwardHeaders(request.getRequestHeaders()).entrySet()) {
    connection.setRequestProperty(header.getKey(), header.getValue());
}
```

For legacy login only:

```java
if (SokkerProxy.isLegacyXmlSessionLogin(request.getMethod(), url)) {
    String body = SokkerProxy.buildLegacyLoginBody(url);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    try (OutputStream out = connection.getOutputStream()) {
        out.write(body.getBytes(StandardCharsets.UTF_8));
    }
}
```

For every other Sokker request, do not synthesize a body and do not append `ilogin`/`ipassword`.

Use `SokkerProxy.responseStream(connection)` instead of assuming `getInputStream()` succeeds for 4xx/5xx.

Preserve the recovered response status/content-type/encoding/header mapping and the CORS response headers already needed by the hosted page.

- [ ] **Step 3: Ensure credentials are not logged**

Run:

```bash
cd "$(git rev-parse --show-toplevel)"
if grep -R -nE 'Log\.[a-zA-Z]+\([^\n]*(ilogin|ipassword|buildLegacyLoginBody|body)' \
  android/SokkerAsistente/app/src/main/java; then
  echo 'Credential/body logging detected' >&2
  exit 1
fi
```

Expected: no matches.

- [ ] **Step 4: Run proxy unit tests**

```bash
cd android/SokkerAsistente
./gradlew testDebugUnitTest --tests 'com.formulamanager.sokker.asistente.SokkerProxyTest'
```

Expected: PASS.

- [ ] **Step 5: Compile/package both debug and release bundle**

```bash
./gradlew assembleDebug bundleRelease
```

Expected: PASS and non-empty debug APK/AAB outputs.

- [ ] **Step 6: Verify the integration source contract**

```bash
cd "$(git rev-parse --show-toplevel)"
grep -q 'SokkerProxy.isSokkerUrl' android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java
grep -q 'SokkerProxy.buildForwardHeaders' android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java
grep -q 'SokkerProxy.isLegacyXmlSessionLogin' android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java
```

Expected: all assertions pass.

- [ ] **Step 7: Commit the native proxy integration**

```bash
git add android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/MainActivity.java
git commit -m "fix: preserve Sokker client header in Android proxy"
```

---

### Task 5: Add isolated Android CI and repository-safety checks

**Files:**
- Create: `.github/workflows/android.yml`

**Interfaces:**
- Consumes: Android project from Tasks 1-4.
- Produces: independent CI gate for unit tests, debug build, release AAB build and secret/artifact tracking checks.

- [ ] **Step 1: Create the Android workflow**

Use this structure:

```yaml
name: Android build

on:
  push:
    branches:
      - main
      - 'feature/android-*'
      - 'fix/android-*'
  pull_request:
    paths:
      - 'android/SokkerAsistente/**'
      - '.github/workflows/android.yml'
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: android/SokkerAsistente
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: gradle

      - name: Make Gradle wrapper executable
        run: chmod +x gradlew

      - name: Unit tests
        run: ./gradlew testDebugUnitTest

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Build release bundle
        run: ./gradlew bundleRelease

      - name: Verify outputs
        run: |
          test -s app/build/outputs/apk/debug/app-debug.apk
          test -s app/build/outputs/bundle/release/app-release.aab
```

Add a final step with `working-directory: .` or an explicit `cd "$GITHUB_WORKSPACE"` that fails if tracked files match:

```text
.gradle/
.idea/
/build/
/app/build/
local.properties
*.apk
*.aab
*.jks
*.keystore
key.properties
```

- [ ] **Step 2: Validate workflow YAML and local Gradle tasks**

Run locally:

```bash
cd android/SokkerAsistente
./gradlew testDebugUnitTest assembleDebug bundleRelease
```

Expected: PASS.

- [ ] **Step 3: Push and inspect the first Android workflow run**

```bash
git add .github/workflows/android.yml
git commit -m "ci: verify Android application build"
git push -u origin feature/android-play-release
```

Expected CI: unit tests, debug APK build, release AAB build and repository-safety check all green.

Do not accept a green server Java-8 workflow as evidence for Android; the Android workflow itself must pass.

---

### Task 6: Document Play signing/release flow without storing secrets

**Files:**
- Create: `android/SokkerAsistente/README.md`

**Interfaces:**
- Consumes: verified API-36 Android project and successful AAB build.
- Produces: repeatable local/Play release procedure without committed credentials.

- [ ] **Step 1: Write build prerequisites and commands**

Document exactly:

```text
JDK: 17
Android SDK: platform 36
App id: com.formulamanager.sokker.asistente
minSdk: 21
targetSdk: 36
versionCode: 11
versionName: 1.11
```

Build commands:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew bundleRelease
```

State the AAB path:

```text
app/build/outputs/bundle/release/app-release.aab
```

- [ ] **Step 2: Document new signing identity / upload key policy**

Explain that the old sideload signing key is intentionally not required because the app has never been on Google Play.

Include an example upload-key generation command that writes outside the repository, for example:

```bash
keytool -genkeypair \
  -keystore "$HOME/.android/sokker-asistente-upload.jks" \
  -alias sokker-asistente-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

State explicitly:

- do not copy the keystore into the repository;
- do not commit passwords;
- enroll the first Play release in Play App Signing;
- users of the old sideloaded APK must uninstall it before installing the new differently signed build with the same package id.

- [ ] **Step 3: Document internal-testing checklist**

Include these manual checks:

1. launch app and load `https://raqueto.com/sokker/asistente`;
2. navigate normally and use WebView back history;
3. verify JavaScript alert/confirm behavior;
4. perform Sokker legacy login through the page;
5. confirm successful login response reaches JavaScript;
6. test a deliberately wrong Sokker password and confirm HTTP failure reaches JavaScript rather than producing a blank/proxy crash;
7. confirm Sokker requests carry `X-Sokker-Client`;
8. confirm non-Sokker URLs are not intercepted;
9. install through Play internal testing on at least one API-21-compatible device/emulator if available and one modern Android device.

- [ ] **Step 4: Commit release documentation**

```bash
git add android/SokkerAsistente/README.md
git commit -m "docs: document Android Play release process"
```

---

### Task 7: Final Android verification and branch audit

**Files:**
- No new production files expected.
- Update tests/docs only if verification exposes a concrete gap.

**Interfaces:**
- Consumes: completed Android implementation branch.
- Produces: evidence that the branch is safe to review/merge; no claim of Play publication itself.

- [ ] **Step 1: Run a clean full Android build**

```bash
cd android/SokkerAsistente
./gradlew clean testDebugUnitTest assembleDebug bundleRelease
```

Expected: PASS.

- [ ] **Step 2: Verify required artifacts and version configuration**

```bash
test -s app/build/outputs/apk/debug/app-debug.apk
test -s app/build/outputs/bundle/release/app-release.aab
grep -Eq 'compileSdk(Version)?[[:space:]]+36' app/build.gradle
grep -Eq 'targetSdk(Version)?[[:space:]]+36' app/build.gradle
grep -Eq 'versionCode[[:space:]]+11' app/build.gradle
grep -Eq 'versionName[[:space:]]+["\x27]1\.11["\x27]' app/build.gradle
```

Expected: all pass.

- [ ] **Step 3: Re-run the repository secret/artifact audit**

From repo root:

```bash
! git ls-files | grep -E '(^|/)(\.gradle|\.idea|build)/|local\.properties$|\.(apk|aab|jks|keystore)$|key\.properties$'
! git grep -nE 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|storePassword|keyPassword' -- ':!docs/**'
```

Expected: no matches.

- [ ] **Step 4: Verify Android client id matches the web/server client id**

From repo root:

```bash
ANDROID_KEY=$(grep -o 'skc_[a-zA-Z0-9]*' android/SokkerAsistente/app/src/main/java/com/formulamanager/sokker/asistente/SokkerProxy.java | head -1)
WEB_KEY=$(grep -o 'skc_[a-zA-Z0-9]*' sokker/WebContent/js/util.js | head -1)
test -n "$ANDROID_KEY"
test "$ANDROID_KEY" = "$WEB_KEY"
```

Expected: PASS.

- [ ] **Step 5: Confirm CI for the exact branch head is green**

Inspect the latest `Android build` workflow run for the branch head and verify every build step is green. Do not infer success from an earlier SHA.

- [ ] **Step 6: Compare branch against current main**

Expected functional scope:

```text
android/SokkerAsistente/**
.github/workflows/android.yml
docs/superpowers/specs/2026-09-24-android-app-recovery-design.md
docs/superpowers/plans/2026-09-24-android-app-recovery.md
```

No `sokker/src/**` or `sokker/WebContent/**` production changes belong in this Android branch.

- [ ] **Step 7: Perform final code review before merge**

Review specifically:

- exact-host URL parsing rather than prefix matching;
- credential reconstruction only for legacy XML-session POST;
- no credential logging;
- HTTP error stream/status propagation;
- no accidental removal of existing WebView UI/navigation behavior;
- package id/version/API levels;
- no signing secrets/artifacts in Git.

Only after this review and green exact-head CI should the Android branch be considered ready to merge. Google Play Console publication/internal-testing remains a separate operational step after merge/build verification.

from pathlib import Path


def require(condition, message):
    if not condition:
        raise AssertionError(message)


build_gradle = Path("android/SokkerAsistente/app/build.gradle").read_text(encoding="utf-8")
workflow = Path(".github/workflows/android.yml").read_text(encoding="utf-8")

for name in (
    "ANDROID_UPLOAD_STORE_FILE",
    "ANDROID_UPLOAD_STORE_PASSWORD",
    "ANDROID_UPLOAD_KEY_ALIAS",
    "ANDROID_UPLOAD_KEY_PASSWORD",
):
    require(name in build_gradle, f"build.gradle does not read {name}")

require("signingConfigs" in build_gradle, "release signing config is missing")
require("signingConfig signingConfigs.release" in build_gradle,
        "release build does not use the upload signing config")

for name in (
    "ANDROID_UPLOAD_KEYSTORE_BASE64",
    "ANDROID_UPLOAD_STORE_PASSWORD",
    "ANDROID_UPLOAD_KEY_ALIAS",
    "ANDROID_UPLOAD_KEY_PASSWORD",
):
    require(f"secrets.{name}" in workflow, f"workflow does not use repository secret {name}")

require("base64 --decode" in workflow, "workflow does not reconstruct the upload keystore")
require("8D:44:8A:8D:5D:A4:54:B3:94:2E:5B:D3:A8:12:04:FC:65:87:B0:D4" in workflow,
        "workflow does not pin the expected upload certificate SHA-1")
require("jarsigner -verify" in workflow, "workflow does not verify the signed AAB")
require("app-release.aab" in workflow, "workflow does not publish the release AAB")

print("Android signing configuration contract OK")

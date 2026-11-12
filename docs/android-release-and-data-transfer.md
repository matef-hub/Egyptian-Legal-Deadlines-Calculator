# Meead Android — owner-signed release and safe data transfer

> **Current status:** Release signing workflow and encrypted transfer code are prepared; **not** an already-signed production APK. Android CI is a code/build check, not on-device migration or independent statutory accuracy certification.

## Why the previously downloaded APK did not replace the old one

Android updates require the **same package name** and **compatible signing certificate**, plus a suitable version code. Previous CI APKs used distinct names:
- Historic debug app: `com.ateflaw.legaldeadlines.debug`
- Legal-review debug app: `com.ateflaw.legaldeadlines.legalreview`
- Owner-signed production app: `com.ateflaw.legaldeadlines` (base application ID)

The two debug identities have private Room stores and cannot be silently merged or upgraded into the release app. GitHub CI-generated debug certificates also vary. **Do not uninstall either existing app until you have verified the copied records in the new app.**

## 1. Create/secure a stable owner-controlled signing key (Windows)

First check whether a *previously published production APK* and its **original signing key** exist. If you need to update it under the same package, **reuse that original private key**. Otherwise create the key for your *first* official signed release:

```powershell
# JDK 17+ in PATH; perform on a trusted Windows computer.
keytool -genkeypair -v -keystore "meead-release.jks" -storetype PKCS12 -keyalg RSA -keysize 3072 -validity 10000 -alias meead-release
```

Use strong unique passwords. Securely back up the keystore and all passwords **outside GitHub/the repository**, e.g. encrypted removable storage. Losing the key can prevent future compatible upgrades. Do not share it in chat or commit it.

Create a GitHub Actions environment named `android-release`, preferably with required reviewer protection, and store **environment secrets**:
- `MEEAD_KEYSTORE_BASE64`: Base64 *encoding* of the binary keystore (**not encryption**)
- `MEEAD_STORE_PASSWORD`: keystore password
- `MEEAD_KEY_ALIAS`: alias (e.g. `meead-release`)
- `MEEAD_KEY_PASSWORD`: private key password

For PowerShell, you can obtain the Base64 string locally with:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path ".\meead-release.jks")))
```

Never paste that string into a public issue, logs, repository or this chat. GitHub Secrets encrypt and restrict these values; storing them in Base64 without Secrets does **not** protect them.

After merging and validating the release branch, open GitHub → Actions → **Android signed release (manual, owner-key only)** → Run workflow → select **main** and enter `SIGN`. The workflow fails if secrets are absent, verifies the APK certificate, and uploads a signed APK and SHA-256 to the run's artifacts. It does **not** publish a public GitHub Release automatically.

**Before distribution:** Test this APK on an additional Android device; test local Room migration, Arabic UI, source status, warnings, calendar entries, backup/restore, and upgrades using the **same key**. An APK with a different certificate cannot replace an installed package with the same ID.

## 2. Back up from a version that already has the new UI

Open **دليل المواعيد → نسخ احتياطي**, select a long password (at least 12 characters), and **تصدير نسخة احتياطية مشفرة**. A system file picker chooses where you save the `.meead` file. The backup encrypts the local legal-rule catalog, review history, saved deadlines and holiday-source data with AES-256-GCM. The file does not contain the password and is not automatically uploaded. Store the backup and password **separately**. Do not share backups containing client details.

In the fresh signed app, open the same page and select **استعادة من ملف سابق**. The import rejects files with the wrong password, unsupported schema, tampering, or non-empty destination user data. Database writes are transactional. Confirm saved deadlines and adjusted legal rules **before** retiring the old installation.

## 3. For your existing old debug app: local ADB bridge

The old installed APK doesn't contain the encrypted export screen, and a different package cannot read its app-private database. If it is a **debuggable** APK, the Android platform-tools `adb run-as` command can copy the old Room SQLite database without root or uninstalling it. If `run-as` fails, **stop**; it is not safe to claim migration is completed.

On your own Windows computer, install Android Platform Tools, Python 3.10+ and cryptography:

```powershell
python -m pip install cryptography
adb devices
adb shell pm list packages | findstr "com.ateflaw.legaldeadlines"
```

Enable Android **Developer options → USB debugging**, authorize your own PC, then run **one** of these commands with the package actually installed:

```powershell
# Old .debug app
python tools/export_legacy_debug_backup.py --package com.ateflaw.legaldeadlines.debug --output ".\meead-from-old-debug.meead"

# Legal review app (if it holds the data you want instead)
python tools/export_legacy_debug_backup.py --package com.ateflaw.legaldeadlines.legalreview --output ".\meead-from-legalreview.meead"
```

The tool stops the source app to reduce SQLite write races; it extracts the database with WAL/SHM into a temporary PC folder, runs SQLite integrity checks, and emits only the encrypted `.meead` backup as its durable output. It never uninstalls anything. Do not run two imports into a single already-used destination: the import policy refuses data overwrites. When both debug apps contain different records, export both and reconcile them **locally** before importing.

Move the encrypted `.meead` backup to the phone or a trusted provider chosen by you, open the signed app → **نسخ احتياطي → استعادة**, enter the same password, and verify the number of saved deadlines and at least a few original case entries.

**If the old app is not debuggable:** Do not assume `adb backup`, file-manager access, or OS backup will work on Android 16. You must obtain the original signing key and introduce an exporter into an upgrade-compatible build, or use another user-approved and tested recovery path. **Never uninstall the old app as a workaround**.

## 4. Safety rules

- Release signing key is never part of `main`, artifacts, debug builds, or Gradle source.
- Export/import is user-triggered, offline and encrypted; no broad file-system permissions.
- A fresh-install import preserves owner-edited rules, deadline snapshots, revision history and candidate holiday provenance. Baseline historical rules that were never edited are supplied by the current release seed, rather than overwriting current references with old seed versions.
- A saved user approval is not an independent legal-source validation. Unsupported calculations and warnings stay governed by the legal safety layer.
- This workflow requires real device testing before a production rollout.

## Source references

- Android app signing: https://developer.android.com/studio/publish/app-signing
- Android update requirements: https://developer.android.com/google/play/app-updates
- Storage Access Framework: https://developer.android.com/training/data-storage/shared/documents-files
- GitHub encrypted secrets: https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets

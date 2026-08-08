# Changelog

All notable changes to EggSPEED are documented here.

> Earlier releases exist (v0.2.0-M2 through v0.3.5) but predate this repository's history - no reliable change notes are available for them, so they're intentionally omitted rather than listed with empty placeholders.

## v0.3.8 - 2026-08-08 (versionCode 10)
- Bumped compile/target SDK to Android 16 (API 36), required by Google Play's target API level policy (deadline 2026-08-31)

## v0.3.7 - 2026-08-08 (versionCode 9)
- Enabled native debug symbol packaging for release builds (`ndk.debugSymbolLevel = FULL`), addressing the Play Console "missing debug symbols" warning

## v0.3.6 - 2026-08-08 (versionCode 8)
- Fixed `applicationId` mismatch (`com.bafspeed.app` → `app.spotrobotics.eggspeed`) to match the registered Play Console listing
- Switched release build signing from the debug keystore to a proper release keystore
- About screen now reads the version name and build stamp dynamically from `BuildConfig` instead of a hardcoded string that could silently drift out of sync with the actual build

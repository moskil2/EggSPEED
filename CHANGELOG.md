# Changelog

All notable changes to EggSPEED are documented here.

> **Note on early versions:** this repository was only initialized starting from the v0.3.5-era codebase, so no commit history exists for earlier releases. The entries below for v0.2.0-M2 through v0.3.5 are reconstructed solely from the version metadata embedded in the historical APK files (`versionCode`/`versionName`) and each file's timestamp - detailed change descriptions for those releases were not preserved and are not available.

## v0.3.8 - 2026-08-08 (versionCode 10)
- Bumped compile/target SDK to Android 16 (API 36), required by Google Play's target API level policy (deadline 2026-08-31)

## v0.3.7 - 2026-08-08 (versionCode 9)
- Enabled native debug symbol packaging for release builds (`ndk.debugSymbolLevel = FULL`), addressing the Play Console "missing debug symbols" warning

## v0.3.6 - 2026-08-08 (versionCode 8)
- Fixed `applicationId` mismatch (`com.bafspeed.app` → `app.spotrobotics.eggspeed`) to match the registered Play Console listing
- Switched release build signing from the debug keystore to a proper release keystore
- About screen now reads the version name and build stamp dynamically from `BuildConfig` instead of a hardcoded string that could silently drift out of sync with the actual build

## v0.3.5 - 2026-08-07 (versionCode 7)
_No detailed change notes available - predates this repository's history._

## v0.3.4 - 2026-08-07 (versionCode 6)
_No detailed change notes available - predates this repository's history._

## v0.3.3 - 2026-08-07 (versionCode 5)
_No detailed change notes available - predates this repository's history._

## v0.3.2 - 2026-08-07 (versionCode 4)
_No detailed change notes available - predates this repository's history._

## v0.3.1 - 2026-07-14 (versionCode 3)
_No detailed change notes available - predates this repository's history._

## v0.2.0-M2 - 2026-07-13 (versionCode 2)
_No detailed change notes available - predates this repository's history. Earliest known build (oldest APK found on disk; note it was distributed under the filename `EggSPEED_v0.3.0.apk`, even though its embedded `versionName` reads `0.2.0-M2`)._

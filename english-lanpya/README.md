# English LanPya V2.7.1 — Android Build QA Branch

**Scope:** Safe public build-validation bootstrap, not the complete V2.7 learning software or a production APK.

This branch preserves the existing AAII Kid Creator project in `main`.
The independent English LanPya app resides in `english-lanpya/`.
Runs in GitHub Actions: `.github/workflows/english-lanpya-v271.yml`.

## Included
- Kotlin/Jetpack Compose sample native screens: activation, home, A1 lessons, question practice, speaking recorder, exam and certificate release status.
- HTTPS activation endpoint integration. It requires a real deployed V2.3+ Cloud API and valid test credentials.
- Demo does not grant login, access to actual online lessons, exams, or certificates.
- No copyrighted third-party videos, Google Drive tokens, service credentials, real student records or keys are bundled.

## Important
This build is a debug QA app only. Certificate issuance, audio upload/human grading, remote media playback and full CEFR content are **not proven end to end**. The full prior V2.7 source package remains separate in the conversation and is NOT represented as uploaded here.

## QA
Workflow attempts `:app:testDebugUnitTest` and `:app:assembleDebug`, verifies debug APK and publishes artifact if successful.
Cloud API default is a candidate target, **not confirmed live**.

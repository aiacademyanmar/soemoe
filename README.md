# AAII Kid Creator Academy — Android Build Validation

> **Build-validation source only. Not the full V2.1.1 teaching APK.**

Owner: aiacademyanmar  
Application: AAII AI for Kid Creator Academy  
Repository role: Verify a real Kotlin/Jetpack Compose Android build via GitHub Actions.

This repository currently contains a **public-safe, standalone diagnostic prototype**. It will exercise an offline Android Home → Levels → Modules → Lesson navigator. The full 72 lesson text, teacher illustrations, 288 quizzes, and recorded-media/Drive/AI/Zoom integrations from the user's original 8.6 MB V2.1.1 archive are **NOT** included and must be migrated to a **private repo** before any production release.

The debug app will be clearly labeled **BUILD TEST**. No student accounts, private assets, API keys, or child information are committed.

## Build

Go to [Actions](../../actions) → **AAII Kid Creator APK - Public-Safe Build Test** and click **Run workflow**, or push to `main` to trigger the workflow. If successful, download the `AAII-Kid-Creator-Build-Test-Debug-APK` artifact.

See `app/src/main/java/asia/aaii/kidcreator/MainActivity.kt` for the Compose UI.

# Soe Moe AI Trainer V2.0 — 100% Native Android Starter

Website reference: https://soemoe.aaii.asia/

## Architecture
* Real Kotlin + Jetpack Compose Material 3 UI; **no Android WebView / iframe**.
* Screens: onboarding/splash, Home, Courses, Course Details, Lesson Reader, Books & Study Notes, Native Reader, Projects, Certificates, Teacher Profile.
* Teacher image app icon and original teacher portrait in native header/profile.
* Fully offline local seed curriculum: 4 courses, 12 original demonstration lessons, 4 practice projects, search and local progress.
* State saved in private SharedPreferences on the device. A website link opens in the user's external browser.

## Scope — important
The live website could not be fetched during this build. This app uses **original demonstration educational content**, NOT full website migration. It does not import student users, memberships, books, published lessons, official certificates, billing or login. Certificate screen displays only local milestones and deliberately does not issue official certificates. Do not interpret the app as the complete production website.

## Windows build
Install Android Studio with Android SDK Platform 35, Android Build Tools 35.0.0, JDK 17. Open this folder in Android Studio. Run **Build > Build Bundle(s)/APK(s) > Build APK(s)**, or run BUILD_APK_WINDOWS.bat with Gradle 8.10.2 on PATH. Debug output is app/build/outputs/apk/debug/app-debug.apk.

## CI build
GitHub Actions workflow in .github/workflows/build-soemoe-native-v2.yml checks source, runs Gradle unit tests and assembles APK, then verifies Android signing and SHA256, and publishes artifact. Tested CI != tested on phone. Do physical device QA before distribution.

## Package
applicationId: asia.aaii.soemoe, versionName: 2.0.0-native, minSdk: 24.

## Signing / updates
This debug build is not for production. Debug signing key differs between machines, so an update over older local/debug builds may require uninstalling the previous APK first (which can erase app-local data). Use a fixed release keystore only after QA and explicit release approval.

# ပိဋကတ်သုံးပုံ — Native Android Prototype

Native Java Android app, not WebView. Offline catalog, local JSON import, reader, search, bookmarks, notes, adjustable font, dark theme, optional device Myanmar TTS.

**Scripture corpus not included.** Sample/demo.json is test text only. Obtain lawful permission and verified original sources before importing full Tipitaka content.

GitHub Actions workflow: .github/workflows/myanmar-tipitaka-apk.yml

Run CI on branch `build/myanmar-tipitaka-v1-20261030`; download the `Myanmar-Tipitaka-Native-V1-Debug-APK` artifact after a successful build. Android SDK 35, Gradle 8.9, AGP 8.7.3, Java 17.

This is an early-stage prototype; build and Samsung device testing must be completed before public release.


V1.3 update: offline Myanmar research catalog (45 volumes), Dhamma learning guides (Dhammapada, Mangala Sutta, Nibbana, Satipatthana), five Nikaya overview and original Tipitaka.cloud source links. The full original scripture corpus is not bundled; online original texts require internet.


V1.4 compile fix: propagate database asset IO exceptions to Activity initialization handler and capture page count as an effectively-final variable for reader pagination.

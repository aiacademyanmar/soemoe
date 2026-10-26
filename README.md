# AAII Music Creator Android — crash-safe build

Branch: music-creator-apk. The main branch of soemoe is left intact.

Native Android WebView host, Song Library, Lyrics Editor, manual Suno links, optional **authorized HTTPS JSON catalog sync**, Online Radio custom URLs and playback, Music Video links, Favorites, Playlists and Settings.

**Not** a Suno scraping client. No fake song library; import song data or provide your own HTTPS JSON feed in Settings. Music playback needs a real public or licensed audio stream URL.

Build: GitHub Actions > Build AAII Music Creator APK > push or run workflow. Debug artifact: AAII-Music-Creator-Samsung-CrashFix-APK.

App target: 28; minimum 26; supports modern Samsung devices. This is a fresh Android Gradle build, not a re-signing of previously crashing hand-packed APK. It does not yet include the original portrait and artwork binaries and is not visually pixel-identical to the prior 12 UI concept images.

Older APK must generally be uninstalled before the newly signed debug APK can install; song data in older app might be lost. Keep backups.

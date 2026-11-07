@echo off
setlocal
cd /d "%~dp0"
echo =====================================
echo Soe Moe AI Trainer V2 - NATIVE BUILD
 echo No WebView - Kotlin Jetpack Compose
 echo =====================================
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Install JDK 17. See README.md
  pause
  exit /b 1
)
if exist gradlew.bat (
  call gradlew.bat --no-daemon :app:assembleDebug
) else (
  where gradle >nul 2>&1
  if errorlevel 1 (
    echo Gradle not found. Open this folder with Android Studio, then use Build APK.
    echo Or install Gradle 8.10.2 and add it to PATH.
    pause
    exit /b 1
  )
  call gradle --no-daemon :app:assembleDebug
)
if errorlevel 1 (
  echo BUILD FAILED. Review output above.
  pause
  exit /b 1
)
set "FILE=app\build\outputs\apk\debug\app-debug.apk"
if not exist "%FILE%" (
  echo ERROR: APK was not generated.
  pause
  exit /b 1
)
copy /Y "%FILE%" "SoeMoe-AI-Trainer-V2.0-native-debug.apk"
echo BUILD SUCCESS: SoeMoe-AI-Trainer-V2.0-native-debug.apk
pause

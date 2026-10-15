@echo off
setlocal enabledelayedexpansion

title DigiView Android - Easy APK Builder
echo ============================================================
echo           DIGIVIEW ANDROID - ONE-CLICK BUILDER
echo ============================================================
echo.

cd /d "%~dp0.."
set "PROJECT_ROOT=%CD%\"
set "ANDROID_DIR=%PROJECT_ROOT%android"
set "DIST_DIR=%PROJECT_ROOT%dist"

:: 1. Detect Android SDK
echo [1/4] Checking Android SDK environment...
set "SDK_PATH="

if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%" set "SDK_PATH=%ANDROID_HOME%"
)
if "!SDK_PATH!"=="" (
    if defined ANDROID_SDK_ROOT (
        if exist "%ANDROID_SDK_ROOT%" set "SDK_PATH=%ANDROID_SDK_ROOT%"
    )
)
if "!SDK_PATH!"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "SDK_PATH=%LOCALAPPDATA%\Android\Sdk"
    )
)
if "!SDK_PATH!"=="" (
    if exist "C:\Android\sdk" (
        set "SDK_PATH=C:\Android\sdk"
    )
)

if "!SDK_PATH!"=="" (
    if exist "%ANDROID_DIR%\local.properties" (
        echo [OK] Using existing android\local.properties
        goto :sdk_configured
    )
    if exist "%DIST_DIR%\DigiView-Android.apk" (
        echo [WARNING] Android SDK was not found on this machine.
        echo [INFO] Preserving existing release APK: dist\DigiView-Android.apk
        exit /b 0
    )
    echo [ERROR] Android SDK was not found automatically.
    echo Please set the ANDROID_HOME environment variable or install Android Studio.
    echo Default expected location: %LOCALAPPDATA%\Android\Sdk
    if "%~1"=="" pause
    exit /b 1
)

:: Format SDK path for local.properties (replace backslashes with forward slashes)
set "FORMATTED_SDK=!SDK_PATH:\=/!"
echo # Automatically configured by DigiView Builder> "%ANDROID_DIR%\local.properties"
echo sdk.dir=!FORMATTED_SDK!>> "%ANDROID_DIR%\local.properties"
echo [OK] Using Android SDK: !SDK_PATH!

:sdk_configured

:: 2. Check Java / JDK
echo [2/4] Verifying Java compiler...
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if not defined JAVA_HOME (
        echo [ERROR] Java Development Kit is not found in PATH or JAVA_HOME.
        if "%~1"=="" pause
        exit /b 1
    )
)

if not defined JAVA_HOME (
    for /f "delims=" %%I in ('where javac 2^>nul') do (
        set "BIN_DIR=%%~dpI"
        set "JAVA_HOME=!BIN_DIR:~0,-5!"
        goto :java_ready
    )
)
:java_ready
echo [OK] Java environment ready: %JAVA_HOME%

:: 3. Run Gradle Build
echo [3/4] Compiling Android APK with Gradle...
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

pushd "%ANDROID_DIR%"
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat was not found in %ANDROID_DIR%.
    popd
    if "%~1"=="" pause
    exit /b 1
)

call gradlew.bat assembleDebug
set "BUILD_STATUS=%ERRORLEVEL%"
popd

if %BUILD_STATUS% neq 0 (
    echo.
    echo [ERROR] Android build failed with error code %BUILD_STATUS%.
    if "%~1"=="" pause
    exit /b %BUILD_STATUS%
)

:: 4. Verify and Copy APK
echo [4/4] Packaging APK to dist directory...
set "BUILT_APK=%ANDROID_DIR%\app\build\outputs\apk\debug\app-debug.apk"
set "TARGET_APK=%DIST_DIR%\DigiView-Android.apk"

if exist "%BUILT_APK%" (
    copy /Y "%BUILT_APK%" "%TARGET_APK%" >nul
    echo.
    echo ============================================================
    echo           BUILD SUCCESSFUL!
    echo ============================================================
    echo  APK Location: %TARGET_APK%
    for %%F in ("%TARGET_APK%") do (
        set /a "SIZE_KB=%%~zF / 1024"
        echo  File Size:    !SIZE_KB! KB
    )
    echo.
    echo  To run on device or emulator:
    echo    1. Type 'manage-android.bat run'
    echo    2. Or run: adb install -r "%TARGET_APK%"
    echo ============================================================
) else (
    echo [ERROR] Expected APK not found at %BUILT_APK%.
    if "%~1"=="" pause
    exit /b 1
)

if "%~1"=="" (
    echo.
    echo Press any key to exit...
    pause >nul
)
exit /b 0

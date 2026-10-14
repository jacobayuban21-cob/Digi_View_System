@echo off
setlocal enabledelayedexpansion

title DigiView Android Manager
set "SCRIPT_DIR=%~dp0"
set "DIST_DIR=%SCRIPT_DIR%dist"
set "APK_PATH=%DIST_DIR%\DigiView-Android.apk"
set "PACKAGE_NAME=com.digiview.lms"
set "MAIN_ACTIVITY=.MainActivity"

:: Locate adb if not in PATH
where adb >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;!PATH!"
    )
)

:: Route argument commands
if /i "%~1"=="build" goto :cmd_build
if /i "%~1"=="run" goto :cmd_run
if /i "%~1"=="install" goto :cmd_install
if /i "%~1"=="devices" goto :cmd_devices
if /i "%~1"=="logs" goto :cmd_logs
if /i "%~1"=="logcat" goto :cmd_logs
if /i "%~1"=="clean" goto :cmd_clean
if /i "%~1"=="help" goto :cmd_help
if not "%~1"=="" (
    echo [ERROR] Unknown command: %~1
    echo Run 'manage-android.bat help' for available commands.
    exit /b 1
)

:menu
cls
echo ============================================================
echo           DIGIVIEW ANDROID MANAGEMENT TOOLKIT
echo ============================================================
echo.
echo   [1] Build APK (Compile and package to dist\)
echo   [2] Run on Device (Build + Install + Launch App)
echo   [3] Install APK to Connected Device / Emulator
echo   [4] Check Connected Devices (adb devices)
echo   [5] View App Logs (adb logcat -s DigiView)
echo   [6] Clean Build Caches (gradlew clean)
echo   [Q] Quit
echo.
echo ============================================================
set /p "CHOICE=Enter choice [1-6 or Q]: "

if "%CHOICE%"=="1" goto :cmd_build
if "%CHOICE%"=="2" goto :cmd_run
if "%CHOICE%"=="3" goto :cmd_install
if "%CHOICE%"=="4" goto :cmd_devices
if "%CHOICE%"=="5" goto :cmd_logs
if "%CHOICE%"=="6" goto :cmd_clean
if /i "%CHOICE%"=="q" exit /b 0

echo Invalid choice. Please try again.
timeout /t 2 >nul
goto :menu

:: -------------------------------------------------------------------------
:: COMMAND: BUILD
:: -------------------------------------------------------------------------
:cmd_build
echo.
echo [*] Triggering Android APK Build...
call "%SCRIPT_DIR%build-android.bat" auto
if "%~1"=="" (
    echo.
    echo Press any key to return to menu...
    pause >nul
    goto :menu
)
exit /b %ERRORLEVEL%

:: -------------------------------------------------------------------------
:: COMMAND: RUN (Build + Install + Launch)
:: -------------------------------------------------------------------------
:cmd_run
echo.
echo [*] Step 1/3: Checking APK...
if not exist "%APK_PATH%" (
    echo [*] APK not found. Building now...
    call "%SCRIPT_DIR%build-android.bat" auto
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Build failed. Cannot run app.
        if "%~1"=="" pause & goto :menu
        exit /b 1
    )
)

echo [*] Step 2/3: Checking connected devices...
where adb >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] adb (Android Debug Bridge) is not found in PATH or Android SDK platform-tools.
    if "%~1"=="" pause & goto :menu
    exit /b 1
)

for /f "skip=1 tokens=1" %%d in ('adb devices 2^>nul ^| findstr /r /v "^$"') do (
    set "DEVICE_FOUND=%%d"
    goto :device_detected
)
:device_detected
if "!DEVICE_FOUND!"=="" (
    echo [ERROR] No connected Android devices or emulators found.
    echo Please connect a phone with USB Debugging enabled, or start an emulator.
    echo Run 'manage-android.bat devices' to verify.
    if "%~1"=="" pause & goto :menu
    exit /b 1
)

echo [OK] Device detected: !DEVICE_FOUND!
echo [*] Step 3/3: Installing and Launching DigiView APK...
adb install -r "%APK_PATH%"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to install APK on device !DEVICE_FOUND!.
    if "%~1"=="" pause & goto :menu
    exit /b 1
)

echo [*] Starting DigiView Activity...
adb shell am start -n "%PACKAGE_NAME%/%PACKAGE_NAME%%MAIN_ACTIVITY%"
echo.
echo [SUCCESS] DigiView LMS is now running on !DEVICE_FOUND!!
if "%~1"=="" (
    echo.
    echo Press any key to return to menu...
    pause >nul
    goto :menu
)
exit /b 0

:: -------------------------------------------------------------------------
:: COMMAND: INSTALL
:: -------------------------------------------------------------------------
:cmd_install
echo.
if not exist "%APK_PATH%" (
    echo [ERROR] APK not found at %APK_PATH%.
    echo Please build first using 'manage-android.bat build'.
    if "%~1"=="" pause & goto :menu
    exit /b 1
)
echo [*] Installing %APK_PATH% onto active device...
adb install -r "%APK_PATH%"
if "%~1"=="" (
    echo.
    echo Press any key to return to menu...
    pause >nul
    goto :menu
)
exit /b %ERRORLEVEL%

:: -------------------------------------------------------------------------
:: COMMAND: DEVICES
:: -------------------------------------------------------------------------
:cmd_devices
echo.
echo [*] Attached Android Devices and Emulators:
adb devices -l
echo.
if "%~1"=="" (
    echo Press any key to return to menu...
    pause >nul
    goto :menu
)
exit /b 0

:: -------------------------------------------------------------------------
:: COMMAND: LOGS
:: -------------------------------------------------------------------------
:cmd_logs
echo.
echo [*] Streaming logs for DigiView (Press Ctrl+C to stop)...
adb logcat -s DigiView:V AndroidRuntime:E *:S
if "%~1"=="" goto :menu
exit /b 0

:: -------------------------------------------------------------------------
:: COMMAND: CLEAN
:: -------------------------------------------------------------------------
:cmd_clean
echo.
echo [*] Cleaning Android build caches...
pushd "%SCRIPT_DIR%android"
if exist "gradlew.bat" call gradlew.bat clean
popd
if exist "%APK_PATH%" del /Q "%APK_PATH%"
echo [OK] Android build artifacts cleaned.
if "%~1"=="" (
    echo.
    echo Press any key to return to menu...
    pause >nul
    goto :menu
)
exit /b 0

:: -------------------------------------------------------------------------
:: COMMAND: HELP
:: -------------------------------------------------------------------------
:cmd_help
echo.
echo DigiView Android Management CLI
echo Usage: manage-android.bat [command]
echo.
echo Commands:
echo   build     - Compiles the Android project into dist\DigiView-Android.apk
echo   run       - Builds (if needed), installs, and launches on connected device/emulator
echo   install   - Installs existing dist\DigiView-Android.apk via adb
echo   devices   - Lists all connected Android hardware phones and emulators
echo   logs      - Displays live app logs and runtime exceptions (logcat)
echo   clean     - Cleans build directories and deletes generated APK
echo   help      - Shows this command reference
echo.
echo Running without arguments opens the interactive management menu.
exit /b 0

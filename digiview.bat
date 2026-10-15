@echo off
setlocal enabledelayedexpansion

title DigiView LMS -- Unified Master CLI
cd /d "%~dp0"
set "PROJECT_ROOT=%~dp0"
set "DIST_DIR=%PROJECT_ROOT%dist"
set "APK_PATH=%DIST_DIR%\DigiView-Android.apk"
set "PORTABLE_ZIP=%DIST_DIR%\DigiView-Portable.zip"
set "JAR_PATH=%DIST_DIR%\DigiViewSystem.jar"

:: Detect ADB in PATH or Android SDK platform-tools
where adb >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;!PATH!"
    )
)

:: =========================================================================
:: DIRECT CLI ARGUMENT ROUTING
:: =========================================================================
:: No arguments -> Open interactive menu directly
if "%~1"==""               goto :interactive_menu

:: Root Redirection Symbols (@, ~, /@, root)
if "%~1"=="@"              goto :interactive_menu
if "%~1"=="~"              goto :interactive_menu
if "%~1"=="/@"             goto :interactive_menu
if "%~1"=="root"           goto :interactive_menu
if "%~1"=="/"              goto :interactive_menu
if "%~1"==".\"             goto :interactive_menu

if /i "%~1"=="run"         goto :cmd_run_desktop
if /i "%~1"=="desktop"     goto :cmd_run_desktop
if /i "%~1"=="gui"         goto :cmd_run_desktop
if /i "%~1"=="server"      goto :cmd_run_server
if /i "%~1"=="backend"     goto :cmd_run_server
if /i "%~1"=="apk"         goto :cmd_build_apk
if /i "%~1"=="android"     goto :cmd_build_apk
if /i "%~1"=="install"     goto :cmd_install_apk
if /i "%~1"=="mobile"      goto :cmd_run_mobile
if /i "%~1"=="android-run" goto :cmd_run_mobile
if /i "%~1"=="devices"     goto :cmd_devices
if /i "%~1"=="logs"        goto :cmd_logs
if /i "%~1"=="logcat"      goto :cmd_logs
if /i "%~1"=="dist"        goto :cmd_dist_all
if /i "%~1"=="release"     goto :cmd_dist_all
if /i "%~1"=="portable"    goto :cmd_build_portable
if /i "%~1"=="test"        goto :cmd_run_tests
if /i "%~1"=="health"      goto :cmd_check_health
if /i "%~1"=="firewall"    goto :cmd_allow_firewall
if /i "%~1"=="allow-firewall" goto :cmd_allow_firewall
if /i "%~1"=="net"         goto :cmd_allow_firewall
if /i "%~1"=="status"      goto :cmd_git_status
if /i "%~1"=="clean"       goto :cmd_clean_all
if /i "%~1"=="help"        goto :cmd_help
if /i "%~1"=="menu"        goto :interactive_menu
if /i "%~1"=="exit"        goto :cmd_exit
if /i "%~1"=="quit"        goto :cmd_exit
if /i "%~1"=="q"           goto :cmd_exit

if not "%~1"=="" (
    echo [ERROR] Unknown command: '%~1'
    echo.
    echo Valid commands:
    echo   digiview run         - Launch Desktop Swing GUI
    echo   digiview server      - Start Spring Boot Backend Server
    echo   digiview apk         - Build Android APK to dist\
    echo   digiview install     - Install APK on connected device/emulator
    echo   digiview mobile      - Build, install and launch on Android
    echo   digiview dist        - Build all release artifacts [APK, JAR, Portable ZIP]
    echo   digiview health      - Check backend server health and uptime
    echo   digiview clean       - Clean temporary build caches
    echo   digiview help        - Show help guide
    echo   digiview exit        - Exit
    echo.
    echo Run 'digiview' without arguments to open the interactive menu.
    exit /b 1
)

:: =========================================================================
:: INTERACTIVE MENU
:: =========================================================================
:interactive_menu
cls
echo ======================================================================
echo    DIGIVIEW LEARNING MANAGEMENT SYSTEM -- UNIFIED MASTER CLI
echo ======================================================================
echo.
echo   [ DESKTOP SWING APPLICATION ]
echo     [1]  Run Desktop App          (Compile and launch Swing GUI)
echo     [2]  Build Desktop Universal  (Universal Java 8-25+ JAR + Zero-Install ZIP)
echo.
echo   [ SPRING BOOT AND SUPABASE BACKEND ]
echo     [3]  Run Backend Server       (Start Spring Boot on :8085 with .env)
echo     [4]  Build Backend JAR        (Package app.jar with Dino Arcade)
echo     [5]  Check Server Health      (Test live /health, uptime, Supabase)
echo     [6]  Run Database Tests       (DatabaseSyncTest via Gradle)
echo.
echo   [ ANDROID MOBILE CLIENT ]
echo     [7]  Build Android APK        (Compile to dist\DigiView-Android.apk)
echo     [8]  Install APK to Device    (Install via ADB to connected phone)
echo     [9]  Run on Mobile Device     (Build + Install + Launch on Android)
echo     [10] View Connected Devices   (adb devices -l)
echo     [11] Stream Android Logs      (adb logcat -s DigiView)
echo.
echo   [ FULL DISTRIBUTION AND MAINTENANCE ]
echo     [12] Build ALL Distributions  (Desktop JAR + Portable ZIP + Android APK)
echo     [13] Clean All Build Caches   (Desktop bin\, dist\, Android, Backend)
echo     [14] Git Status and Sync Info (Check branch, commits, remote sync)
echo     [15] Allow Firewall & Check Net (Windows Defender Firewall Whitelist + Pre-Flight)
echo     [0]  Exit CLI
echo.
echo ======================================================================
set "CHOICE="
set /p "CHOICE=Enter option [0-15, or 'apk', 'install', 'dist', 'run', 'firewall', 'exit']: "

if not defined CHOICE goto :interactive_menu
set "CHOICE=!CHOICE: =!"
if "!CHOICE!"=="" goto :interactive_menu

if "!CHOICE!"=="0" goto :cmd_exit
if /i "!CHOICE!"=="exit" goto :cmd_exit
if /i "!CHOICE!"=="quit" goto :cmd_exit
if /i "!CHOICE!"=="q" goto :cmd_exit

if "!CHOICE!"=="15" goto :cmd_allow_firewall
if /i "!CHOICE!"=="firewall" goto :cmd_allow_firewall
if /i "!CHOICE!"=="allow-firewall" goto :cmd_allow_firewall
if /i "!CHOICE!"=="net" goto :cmd_allow_firewall

if "!CHOICE!"=="1" goto :cmd_run_desktop
if /i "!CHOICE!"=="run" goto :cmd_run_desktop
if /i "!CHOICE!"=="desktop" goto :cmd_run_desktop

if "!CHOICE!"=="2" goto :cmd_build_portable
if /i "!CHOICE!"=="portable" goto :cmd_build_portable

if "!CHOICE!"=="3" goto :cmd_run_server
if /i "!CHOICE!"=="server" goto :cmd_run_server
if /i "!CHOICE!"=="backend" goto :cmd_run_server

if "!CHOICE!"=="4" goto :cmd_build_server_jar
if /i "!CHOICE!"=="jar" goto :cmd_build_server_jar

if "!CHOICE!"=="5" goto :cmd_check_health
if /i "!CHOICE!"=="health" goto :cmd_check_health

if "!CHOICE!"=="6" goto :cmd_run_tests
if /i "!CHOICE!"=="test" goto :cmd_run_tests

if "!CHOICE!"=="7" goto :cmd_build_apk
if /i "!CHOICE!"=="apk" goto :cmd_build_apk

if "!CHOICE!"=="8" goto :cmd_install_apk
if /i "!CHOICE!"=="install" goto :cmd_install_apk

if "!CHOICE!"=="9" goto :cmd_run_mobile
if /i "!CHOICE!"=="mobile" goto :cmd_run_mobile

if "!CHOICE!"=="10" goto :cmd_devices
if /i "!CHOICE!"=="devices" goto :cmd_devices

if "!CHOICE!"=="11" goto :cmd_logs
if /i "!CHOICE!"=="logs" goto :cmd_logs

if "!CHOICE!"=="12" goto :cmd_dist_all
if /i "!CHOICE!"=="dist" goto :cmd_dist_all
if /i "!CHOICE!"=="release" goto :cmd_dist_all

if "!CHOICE!"=="13" goto :cmd_clean_all
if /i "!CHOICE!"=="clean" goto :cmd_clean_all

if "!CHOICE!"=="14" goto :cmd_git_status
if /i "!CHOICE!"=="status" goto :cmd_git_status

echo.
echo [ERROR] Invalid selection: '!CHOICE!'
timeout /t 2 >nul
goto :interactive_menu

:: =========================================================================
:: COMMAND HANDLERS
:: =========================================================================

:: [1] Run Desktop
:cmd_run_desktop
echo.
echo [*] Launching Desktop Swing Application...
call "%PROJECT_ROOT%scripts\run.bat"
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [2] Build Desktop Portable
:cmd_build_portable
echo.
echo [*] Building Portable Desktop Release...
call "%PROJECT_ROOT%scripts\build-release.bat"
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [3] Run Backend Server
:cmd_run_server
echo.
echo [*] Starting Spring Boot Backend Server...
echo Press Ctrl+C at any time to stop the server.
echo.
call "%PROJECT_ROOT%backend\run-backend.bat"
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [4] Build Backend JAR
:cmd_build_server_jar
echo.
echo [*] Packaging Spring Boot app.jar with static Dino Health Dashboard...
set "ORIG_JH=%JAVA_HOME%"
for /d %%D in ("%ProgramFiles%\Microsoft\jdk-17*" "%ProgramFiles%\Java\jdk-17*" "%ProgramFiles%\Eclipse Adoptium\jdk-17*" "%ProgramFiles%\Amazon Corretto\jdk-17*" "%ProgramFiles%\Zulu\zulu-17*" "%ProgramFiles%\Microsoft\jdk-21*" "%ProgramFiles%\Java\jdk-21*" "%ProgramFiles%\Eclipse Adoptium\jdk-21*" "%ProgramFiles%\Amazon Corretto\jdk-21*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        goto :gradle_jh_set_4
    )
)
:gradle_jh_set_4
cd /d "%PROJECT_ROOT%backend"
call gradlew.bat bootJar
cd /d "%PROJECT_ROOT%"
if defined ORIG_JH (set "JAVA_HOME=%ORIG_JH%") else (set "JAVA_HOME=")
if exist "backend\build\libs\app.jar" (
    echo [SUCCESS] Backend JAR built: backend\build\libs\app.jar
) else if exist "%DIST_DIR%\DigiView-Backend.jar" (
    echo [INFO] Preserving existing backend package: dist\DigiView-Backend.jar
) else (
    echo [WARNING] Backend JAR could not be packaged. Gradle requires Java 17 or 21 LTS.
)
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [5] Check Server Health
:cmd_check_health
echo.
echo [*] Checking DigiView Server Health (Render Cloud ^& Local)...
powershell -NoProfile -Command "$urls = @('https://digi-view-system.onrender.com/api/health', 'http://localhost:8085/api/health', 'http://localhost:8080/api/health'); $found = $false; foreach ($u in $urls) { try { $res = Invoke-RestMethod -Uri $u -TimeoutSec 5 -ErrorAction Stop; Write-Host '[ONLINE] ' -ForegroundColor Green -NoNewline; Write-Host $u; Write-Host ('  Status: ' + $res.status + ' | Service: ' + $res.service + ' | Uptime: ' + $res.uptimeFormatted); if ($res.database) { Write-Host ('  Database: ' + $res.database.status + ' (' + $res.database.databaseProduct + ')'); } if ($res.jvm) { Write-Host ('  JVM Memory: ' + $res.jvm.usedMemoryMb + ' MB / ' + $res.jvm.maxMemoryMb + ' MB | Threads: ' + $res.jvm.threadCount); } $found = $true; } catch { if (-not $u.Contains('localhost')) { Write-Host ('[CLOUD NOTICE] ' + $u + ' -> ' + $_.Exception.Message) -ForegroundColor Yellow; } } } if (-not $found) { Write-Host '[NOTICE] No servers reachable right now. Use ''digiview server'' for local development.' -ForegroundColor DarkGray; }"
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [6] Run Database and Backend Tests
:cmd_run_tests
echo.
echo [*] Running Backend Integration and Database Tests...
cd /d "%PROJECT_ROOT%backend"
call gradlew.bat test
cd /d "%PROJECT_ROOT%"
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [7] Build Android APK
:cmd_build_apk
echo.
echo [*] Compiling Android Application...
call "%PROJECT_ROOT%scripts\build-android.bat" auto
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [8] Install Android APK
:cmd_install_apk
echo.
echo [*] Installing Android APK onto connected device...
call "%PROJECT_ROOT%scripts\manage-android.bat" install
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [9] Run Mobile App (Build + Install + Launch)
:cmd_run_mobile
echo.
echo [*] Running Mobile App workflow on Android...
call "%PROJECT_ROOT%scripts\manage-android.bat" run
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [10] View Connected Devices
:cmd_devices
echo.
echo [*] Querying ADB Connected Devices...
call "%PROJECT_ROOT%scripts\manage-android.bat" devices
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [11] Stream Android Logs
:cmd_logs
echo.
echo [*] Streaming Android Logcat (Filter: DigiView)...
call "%PROJECT_ROOT%scripts\manage-android.bat" logs
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [12] Build ALL Distributions (dist)
:cmd_dist_all
echo.
echo ======================================================================
echo    BUILDING ALL SYSTEM DISTRIBUTIONS (DESKTOP + PORTABLE + ANDROID)
echo ======================================================================
echo.
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

echo [1/3] Packaging Desktop Runnable JAR and Portable Zero-Java ZIP...
call "%PROJECT_ROOT%scripts\build-release.bat"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Portable packaging encountered an issue.
)

echo.
echo [2/3] Compiling Native Android Application APK...
call "%PROJECT_ROOT%scripts\build-android.bat" auto
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Android build encountered an issue.
)

echo.
echo [3/3] Packaging Spring Boot Backend Production JAR...
set "ORIG_JH=%JAVA_HOME%"
for /d %%D in ("%ProgramFiles%\Microsoft\jdk-17*" "%ProgramFiles%\Java\jdk-17*" "%ProgramFiles%\Eclipse Adoptium\jdk-17*" "%ProgramFiles%\Amazon Corretto\jdk-17*" "%ProgramFiles%\Zulu\zulu-17*" "%ProgramFiles%\Microsoft\jdk-21*" "%ProgramFiles%\Java\jdk-21*" "%ProgramFiles%\Eclipse Adoptium\jdk-21*" "%ProgramFiles%\Amazon Corretto\jdk-21*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        goto :gradle_jh_set_dist
    )
)
:gradle_jh_set_dist
cd /d "%PROJECT_ROOT%backend"
call gradlew.bat bootJar
cd /d "%PROJECT_ROOT%"
if defined ORIG_JH (set "JAVA_HOME=%ORIG_JH%") else (set "JAVA_HOME=")
if exist "backend\build\libs\app.jar" (
    copy /y "backend\build\libs\app.jar" "%DIST_DIR%\DigiView-Backend.jar" >nul 2>&1
    echo [SUCCESS] Backend production JAR built: dist\DigiView-Backend.jar
) else if exist "%DIST_DIR%\DigiView-Backend.jar" (
    echo [INFO] Preserved existing backend package: dist\DigiView-Backend.jar
) else (
    echo [WARNING] Backend JAR could not be packaged. Gradle requires Java 17 or 21 LTS.
)

echo.
echo ======================================================================
echo    ALL DISTRIBUTIONS PACKAGED SUCCESSFULLY IN: dist\
echo ======================================================================
if exist "%DIST_DIR%\DigiViewSystem.jar"     echo   [JAR]       dist\DigiViewSystem.jar
if exist "%DIST_DIR%\DigiView-Portable.zip"  echo   [PORTABLE]  dist\DigiView-Portable.zip
if exist "%DIST_DIR%\DigiView-Android.apk"   echo   [ANDROID]   dist\DigiView-Android.apk
if exist "%DIST_DIR%\DigiView-Backend.jar"   echo   [BACKEND]   dist\DigiView-Backend.jar
echo ======================================================================
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [13] Clean All Build Caches
:cmd_clean_all
echo.
echo [*] Cleaning Desktop build caches...
if exist "bin" rmdir /s /q "bin"
if exist "build" rmdir /s /q "build"

echo [*] Cleaning Android build caches...
if exist "android\.gradle" rmdir /s /q "android\.gradle"
if exist "android\app\build" rmdir /s /q "android\app\build"

echo [*] Cleaning Backend build caches...
if exist "backend\.gradle" rmdir /s /q "backend\.gradle"
if exist "backend\build" rmdir /s /q "backend\build"

echo [SUCCESS] Clean completed cleanly.
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [14] Git Status
:cmd_git_status
echo.
echo [*] Checking Repository Status...
git status
echo.
echo [*] Recent 3 Commits:
git log -n 3 --oneline
if "%~1"=="" (
    echo.
    pause
    goto :interactive_menu
)
exit /b 0

:: [15] Allow Windows Defender Firewall & Pre-Flight Check
:cmd_allow_firewall
echo.
echo [*] Launching Windows Defender Firewall Auto-Configurator ^& Network Pre-Flight...
call "%PROJECT_ROOT%scripts\allow-firewall.bat"
if "%~1"=="" (
    echo.
    goto :interactive_menu
)
exit /b 0

:: Help
:cmd_help
echo.
echo ======================================================================
echo    DIGIVIEW UNIFIED CLI -- QUICK REFERENCE
echo ======================================================================
echo.
echo USAGE:
echo   digiview [command]
echo.
echo COMMANDS:
echo   run, desktop       Compile and launch Desktop Swing application
echo   server, backend    Start Spring Boot Backend on port 8085
echo   apk, android       Build Android APK to dist\DigiView-Android.apk
echo   install            Install APK to connected Android device or emulator
echo   mobile             Build, install, and run on Android device
echo   devices            List connected Android devices and emulators
echo   logs               Stream live Android logcat output
echo   dist, release      Build all packages (Desktop JAR, Portable ZIP, Android APK)
echo   portable           Build standalone zero-install Java 17 ZIP
echo   health             Check live server health and database status
echo   firewall, net      Configure Windows Defender Firewall and check cloud connectivity
echo   test               Run backend database integration tests
echo   clean              Clean all compiler and build directories
echo   status             View git branch status and recent commits
echo   help               Show this reference manual
echo   exit, q            Exit the CLI
echo.
echo (Run 'digiview' without arguments for the interactive menu)
echo ======================================================================
exit /b 0

:: Exit
:cmd_exit
echo Exiting DigiView CLI. Happy coding!
exit /b 0

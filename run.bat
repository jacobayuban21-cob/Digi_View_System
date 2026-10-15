@echo off
setlocal enabledelayedexpansion
title DigiView LMS - Development Console & Runtime Log Listener
cd /d "%~dp0"

echo ===============================================================================
echo   DIGIVIEW LMS -- DEVELOPMENT RUNNER ^& RUNTIME LOG LISTENER
echo ===============================================================================
echo   Base Directory: %CD%
echo   Java Mode     : Active Console Stream
echo ===============================================================================
echo.

if not exist "bin\System\DigiView.class" (
    echo [*] Compiling DigiView Java sources...
    if not exist "bin" mkdir bin
    javac -encoding UTF-8 -sourcepath "shared;src" -d bin src\System\*.java
    if errorlevel 1 (
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo [OK] Compilation successful.
)

echo [*] Starting DigiView LMS and actively listening for runtime logs...
echo [*] Retaining terminal window. Close DigiView application to finish.
echo ===============================================================================
echo.

java -Ddigiview.debug=true -cp bin System.DigiView

echo.
echo ===============================================================================
echo [INFO] DigiView LMS application closed (Exit Code: %ERRORLEVEL%).
echo ===============================================================================
if "%~1"=="" pause

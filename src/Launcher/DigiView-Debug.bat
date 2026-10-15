@echo off
setlocal enabledelayedexpansion
title DigiView LMS - Runtime Log Monitor & Development Console
cd /d "%~dp0"
cls
color 0B
echo ===============================================================================
echo   DIGIVIEW LMS -- LIVE RUNTIME LOG MONITOR ^& DEBUG CONSOLE (v1.1.0)
echo ===============================================================================
echo   Mode     : Active Real-Time Log Listener
echo   Target   : Desktop Java Runtime (Internal Log Stream)
echo   Directory: %CD%
echo   Timestamp: %DATE% %TIME%
echo ===============================================================================
echo   [INFO] Launching DigiView desktop application with active log streaming...
echo   [INFO] Retaining terminal session. All internal logs will stream live below.
echo   [INFO] Close the DigiView LMS application window or press Ctrl+C to stop.
echo ===============================================================================
echo.

if exist "jre\bin\java.exe" (
    "jre\bin\java.exe" -Ddigiview.debug=true -cp "DigiViewSystem.jar" System.DigiView %*
) else if exist "DigiView.exe" (
    start /wait "" "DigiView.exe" --debug %*
) else (
    java -Ddigiview.debug=true -cp "DigiViewSystem.jar" System.DigiView %*
)

echo.
echo ===============================================================================
echo   [INFO] DigiView session closed (Process Exit Code: %ERRORLEVEL%).
echo ===============================================================================
pause

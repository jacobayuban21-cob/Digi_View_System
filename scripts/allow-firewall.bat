@echo off
setlocal enabledelayedexpansion
title DigiView LMS -- Windows Defender Firewall Whitelist Utility

:: -------------------------------------------------------------------------
:: Check for Administrator Privileges & Self-Elevate
:: -------------------------------------------------------------------------
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [*] Requesting Administrator privileges to configure Windows Firewall...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

cd /d "%~dp0.."
set "BASE_DIR=%CD%"
if exist "%~dp0DigiView.exe" set "BASE_DIR=%~dp0"

echo ===============================================================================
echo   DIGIVIEW LMS -- WINDOWS DEFENDER FIREWALL AUTO-CONFIGURATOR
echo ===============================================================================
echo   Working Directory: %BASE_DIR%
echo ===============================================================================
echo.

:: 1. Add rules for DigiView.exe
if exist "%BASE_DIR%\DigiView.exe" (
    echo [*] Allowing DigiView.exe (Outbound ^& Inbound)...
    netsh advfirewall firewall add rule name="DigiView LMS Application (Outbound)" dir=out action=allow program="%BASE_DIR%\DigiView.exe" enable=yes >nul 2>&1
    netsh advfirewall firewall add rule name="DigiView LMS Application (Inbound)" dir=in action=allow program="%BASE_DIR%\DigiView.exe" enable=yes >nul 2>&1
    echo [OK] DigiView.exe rules added.
)

:: 2. Add rules for bundled JRE (if present)
if exist "%BASE_DIR%\jre\bin\java.exe" (
    echo [*] Allowing Portable Java Runtime (Outbound ^& Inbound)...
    netsh advfirewall firewall add rule name="DigiView LMS Portable Java (Outbound)" dir=out action=allow program="%BASE_DIR%\jre\bin\java.exe" enable=yes >nul 2>&1
    netsh advfirewall firewall add rule name="DigiView LMS Portable Java (Inbound)" dir=in action=allow program="%BASE_DIR%\jre\bin\java.exe" enable=yes >nul 2>&1
    netsh advfirewall firewall add rule name="DigiView LMS Portable Javaw (Outbound)" dir=out action=allow program="%BASE_DIR%\jre\bin\javaw.exe" enable=yes >nul 2>&1
    netsh advfirewall firewall add rule name="DigiView LMS Portable Javaw (Inbound)" dir=in action=allow program="%BASE_DIR%\jre\bin\javaw.exe" enable=yes >nul 2>&1
    echo [OK] Portable JRE rules added.
)

:: 3. Add rules for system Java in PATH (if present)
where java.exe >nul 2>&1
if %errorlevel% equ 0 (
    for /f "delims=" %%I in ('where java.exe 2^>nul') do (
        echo [*] Allowing System Java: %%I
        netsh advfirewall firewall add rule name="DigiView LMS System Java (Outbound)" dir=out action=allow program="%%I" enable=yes >nul 2>&1
        goto :sys_java_done
    )
)
:sys_java_done

where javaw.exe >nul 2>&1
if %errorlevel% equ 0 (
    for /f "delims=" %%I in ('where javaw.exe 2^>nul') do (
        netsh advfirewall firewall add rule name="DigiView LMS System Javaw (Outbound)" dir=out action=allow program="%%I" enable=yes >nul 2>&1
        goto :sys_javaw_done
    )
)
:sys_javaw_done

:: 4. Allow backend port 8085
echo [*] Opening local backend port 8085 (TCP)...
netsh advfirewall firewall add rule name="DigiView LMS Backend Port 8085 (Inbound)" dir=in action=allow protocol=TCP localport=8085 enable=yes >nul 2>&1
netsh advfirewall firewall add rule name="DigiView LMS Backend Port 8085 (Outbound)" dir=out action=allow protocol=TCP localport=8085 enable=yes >nul 2>&1
echo [OK] Port 8085 rules added.

echo.
echo ===============================================================================
echo [SUCCESS] Windows Defender Firewall is now configured for DigiView LMS!
echo Outbound ^& Inbound permissions granted for DigiView.exe, Java runtimes, and :8085.
echo ===============================================================================
echo.

:: -------------------------------------------------------------------------
:: 5. Automated Network Connectivity Check for End User
:: -------------------------------------------------------------------------
echo ===============================================================================
echo   NETWORK CONNECTIVITY PRE-FLIGHT CHECK
echo ===============================================================================
echo [*] Testing HTTPS connection to DigiView Cloud Server (Render / Cloudflare)...
echo     Target: https://digi-view-system.onrender.com/health
echo.

powershell -NoProfile -Command ^
    "$sw = [System.Diagnostics.Stopwatch]::StartNew();" ^
    "try {" ^
    "    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 -bor 3072;" ^
    "    $res = Invoke-WebRequest -Uri 'https://digi-view-system.onrender.com/health' -TimeoutSec 10 -UseBasicParsing;" ^
    "    $sw.Stop();" ^
    "    Write-Host ('[OK] Cloud Server is ONLINE and reachable!') -ForegroundColor Green;" ^
    "    Write-Host ('     HTTP Status: ' + $res.StatusCode + ' ' + $res.StatusDescription) -ForegroundColor Green;" ^
    "    Write-Host ('     Response Latency: ' + $sw.ElapsedMilliseconds + ' ms') -ForegroundColor Cyan;" ^
    "} catch {" ^
    "    $sw.Stop();" ^
    "    Write-Host ('[WARNING] Cloud Server test returned: ' + $_.Exception.Message) -ForegroundColor Yellow;" ^
    "    Write-Host ('     Note: Render free tier spins down on inactivity. It may take ~30s to wake up.') -ForegroundColor DarkGray;" ^
    "    Write-Host ('     DigiView will automatically use local offline student data if offline.') -ForegroundColor DarkGray;" ^
    "}"

echo.
echo ===============================================================================
echo   FIREWALL AND CONNECTIVITY VERIFICATION COMPLETE
echo ===============================================================================
echo.

if exist "%BASE_DIR%\DigiView.exe" (
    echo   [1] Launch DigiView LMS Desktop Now
    echo   [2] Exit
    echo.
    set /p "RUN_NOW=Select option [1-2, default=1]: "
    if "!RUN_NOW!"=="" set "RUN_NOW=1"
    if "!RUN_NOW!"=="1" (
        echo [*] Starting DigiView LMS...
        start "" "%BASE_DIR%\DigiView.exe"
        exit /b 0
    )
) else (
    pause
)

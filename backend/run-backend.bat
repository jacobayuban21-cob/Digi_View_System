@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ===================================================
echo   DigiView - Spring Boot & Supabase Backend Server
echo ===================================================

rem Load variables from .env if present
if exist ".env" (
    echo [INFO] Loading database credentials from .env...
    for /f "usebackq tokens=1* delims==" %%a in (".env") do (
        set "key=%%a"
        if not "!key:~0,1!"=="#" (
            if not "%%a"=="" set "%%a=%%b"
        )
    )
)

set "GRADLE_BIN="
where gradle >nul 2>&1
if %ERRORLEVEL% equ 0 set "GRADLE_BIN=gradle"

if not defined GRADLE_BIN (
    if exist "%USERPROFILE%\.gradle\wrapper\dists" (
        for /r "%USERPROFILE%\.gradle\wrapper\dists" %%G in (gradle.bat) do (
            if exist "%%G" (
                set "GRADLE_BIN="%%G""
                goto :gradle_found
            )
        )
    )
)
:gradle_found

if defined GRADLE_BIN (
    call %GRADLE_BIN% bootRun
) else (
    where gradle >nul 2>nul
    if %errorlevel% equ 0 (
        call gradle bootRun
    ) else (
        echo [ERROR] Gradle not found. Please install Gradle or run using your IDE.
        pause
    )
)

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

set "GRADLE_BIN=C:\Users\131fgh\.gradle\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat"
if not exist %GRADLE_BIN% (
    set "GRADLE_BIN=C:\Users\131fgh\.gradle\wrapper\dists\gradle-8.14.3-bin\cv11ve7ro1n3o1j4so8xd9n66\gradle-8.14.3\bin\gradle.bat"
)

if exist %GRADLE_BIN% (
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

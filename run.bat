@echo off
setlocal enabledelayedexpansion
title DigiView LMS - Development Console & Runtime Log Listener
cd /d "%~dp0"

echo ===============================================================================
echo   DIGIVIEW LMS -- UNIVERSAL DEVELOPMENT RUNNER ^& RUNTIME LOG LISTENER
echo ===============================================================================
echo   Base Directory: %CD%
echo   Java Mode     : Active Console Stream (Universal JVM Compatibility)
echo ===============================================================================
echo.

:: ---------------------------------------------------------------------
:: 1. Auto-Detect Java Development Kit (JDK) / Java Runtime
:: ---------------------------------------------------------------------
where javac >nul 2>&1
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javac.exe" (
            set "PATH=%JAVA_HOME%\bin;!PATH!"
        )
    )
)

where javac >nul 2>&1
if %errorlevel% neq 0 (
    for /d %%D in ("%ProgramFiles%\Java\jdk*" "%ProgramFiles%\Eclipse Adoptium\jdk*" "%ProgramFiles%\Microsoft\jdk*" "%ProgramFiles%\Amazon Corretto\jdk*" "%ProgramFiles%\Zulu\zulu*") do (
        if exist "%%D\bin\javac.exe" (
            set "PATH=%%D\bin;!PATH!"
            goto :jdk_run_found
        )
    )
)
:jdk_run_found

:: ---------------------------------------------------------------------
:: 2. Compile if binary is missing
:: ---------------------------------------------------------------------
if not exist "bin\System\DigiView.class" (
    echo [*] Compiling DigiView Java sources for universal JVM compatibility...
    if not exist "bin" mkdir bin

    set "JVM_COMPAT_FLAG="
    javac --release 8 -version >nul 2>&1
    if !errorlevel! equ 0 (
        set "JVM_COMPAT_FLAG=--release 8"
        echo [*] Compiler Target: --release 8 [Universal Java 8+ bytecode, class v52.0]
    ) else (
        set "JVM_COMPAT_FLAG=-source 1.8 -target 1.8"
        echo [*] Compiler Target: -source 1.8 -target 1.8 [Java 8 bytecode, class v52.0]
    )

    javac !JVM_COMPAT_FLAG! -encoding UTF-8 -sourcepath "shared;src" -d bin src\System\ApiClient.java src\System\DigiView.java
    if errorlevel 1 (
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo [OK] Compilation successful.
)

:: ---------------------------------------------------------------------
:: 3. Locate java.exe for launch
:: ---------------------------------------------------------------------
set "JAVA_CMD=java"
where java >nul 2>&1
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

echo [*] Starting DigiView LMS and actively listening for runtime logs...
echo [*] Retaining terminal window. Close DigiView application to finish.
echo ===============================================================================
echo.

"%JAVA_CMD%" -Ddigiview.debug=true -cp bin System.DigiView

echo.
echo ===============================================================================
echo [INFO] DigiView LMS application closed (Exit Code: %ERRORLEVEL%).
echo ===============================================================================
if "%~1"=="" pause

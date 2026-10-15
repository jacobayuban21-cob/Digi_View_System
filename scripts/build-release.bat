@echo off
setlocal enabledelayedexpansion

echo ===================================================================
echo   DigiView System - Universal Desktop Release Packager
echo   Target: Compatible with ALL JVM versions (Java 8 through 25+)
echo ===================================================================

cd /d "%~dp0.."

:: ---------------------------------------------------------------------
:: 1. Auto-Detect Java Development Kit (JDK)
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
            goto :jdk_detected
        )
    )
)
:jdk_detected

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java Development Kit compiler [javac] was not found in PATH, JAVA_HOME, or Program Files.
    echo Please install a JDK [version 8, 11, 17, 21+] and ensure it is in PATH.
    pause
    exit /b 1
)

:: ---------------------------------------------------------------------
:: 2. Determine Universal Bytecode Target Flag
:: ---------------------------------------------------------------------
set "JVM_COMPAT_FLAG="
javac --release 8 -version >nul 2>&1
if %errorlevel% equ 0 (
    set "JVM_COMPAT_FLAG=--release 8"
    echo [*] Compiler Target: --release 8 [Universal Java 8+ bytecode, class v52.0]
) else (
    set "JVM_COMPAT_FLAG=-source 1.8 -target 1.8"
    echo [*] Compiler Target: -source 1.8 -target 1.8 [Java 8 bytecode, class v52.0]
)

echo.
echo [1/4] Compiling Java classes for universal JVM compatibility...
if exist "bin\System" rmdir /s /q "bin\System"
if exist "bin\com" rmdir /s /q "bin\com"
if not exist "bin" mkdir bin
javac !JVM_COMPAT_FLAG! -encoding UTF-8 -sourcepath "shared;src" -d bin src\System\ApiClient.java src\System\DigiView.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo.
echo [2/4] Packaging universal DigiViewSystem.jar...
if not exist "dist" mkdir dist
if not exist "bin\System\sounds" mkdir "bin\System\sounds"
if not exist "bin\sounds" mkdir "bin\sounds"
copy /y "public\sounds\*" "bin\System\sounds\" >nul 2>&1
copy /y "public\sounds\*" "bin\sounds\" >nul 2>&1
if not exist "bin\Icons" mkdir "bin\Icons"
copy /y "public\Icons\*" "bin\Icons\" >nul 2>&1
jar cfe "dist\DigiViewSystem.jar" System.DigiView -C bin .
if %errorlevel% neq 0 (
    echo [ERROR] JAR creation failed!
    pause
    exit /b 1
)
echo [OK] Universal JAR packaged: dist\DigiViewSystem.jar

echo.
echo [3/4] Preparing portable distribution package...
set "RELEASE_DIR=dist\DigiView-Portable"
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%\src\System\sounds" 2>nul
copy /y "src\System\sounds\*" "%RELEASE_DIR%\src\System\sounds\" >nul 2>&1
if exist "public" (
    xcopy /s /e /y /i "public" "%RELEASE_DIR%\public\" >nul 2>&1
)
if exist "config" (
    xcopy /s /e /y /i "config" "%RELEASE_DIR%\config\" >nul 2>&1
)
copy /y "dist\DigiViewSystem.jar" "%RELEASE_DIR%\" >nul

:: Bundle zero-install JRE with jlink if available (JDK 9+)
where jlink >nul 2>&1
if %errorlevel% equ 0 (
    echo [*] Bundling zero-install Java runtime using jlink with modern TLS/SSL modules...
    jlink --add-modules java.base,java.desktop,java.logging,java.naming,java.net.http,java.security.jgss,java.security.sasl,java.sql,jdk.crypto.ec,jdk.crypto.cryptoki,jdk.crypto.mscapi,jdk.unsupported --strip-debug --no-man-pages --no-header-files --output "%RELEASE_DIR%\jre"
    if %errorlevel% equ 0 (
        echo [OK] Bundled portable JRE with TLS 1.2/1.3 and EC cryptography successfully created!
    ) else (
        echo [WARNING] jlink bundling failed; package will use auto-detected host JVM.
    )
) else (
    echo [INFO] jlink not present in this JDK [requires JDK 9+].
    echo Package will use host JVM auto-detection.
)

:: Compile native Windows launcher (DigiView.exe) with universal JVM discovery
echo [*] Compiling native DigiView.exe with universal JVM discovery...
set "CSC=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if not exist "%CSC%" (
    set "CSC=C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe"
)

if exist "%CSC%" (
    "%CSC%" /nologo /target:winexe /out:"%RELEASE_DIR%\DigiView.exe" /win32icon:"public\Icons\Icon.ico" "src\Launcher\DigiViewLauncher.cs"
    if %errorlevel% equ 0 (
        echo [OK] Native DigiView.exe compiled successfully!
    ) else (
        echo [WARNING] DigiView.exe compilation failed; relying on batch launchers.
    )
) else (
    echo [WARNING] .NET Framework csc compiler not found; relying on batch launchers.
)

:: Copy Debug and Console Log Listener batch files
copy /y "src\Launcher\DigiView-Debug.bat" "%RELEASE_DIR%\DigiView-Debug.bat" >nul
copy /y "src\Launcher\DigiView-Debug.bat" "%RELEASE_DIR%\DigiView.bat" >nul
if exist "scripts\allow-firewall.bat" copy /y "scripts\allow-firewall.bat" "%RELEASE_DIR%\allow-firewall.bat" >nul

echo.
echo [4/4] Creating ZIP package for distribution...
powershell -NoProfile -Command "Compress-Archive -Path '%RELEASE_DIR%\*' -DestinationPath 'dist\DigiView-Portable.zip' -Force"

echo ===================================================================
echo [SUCCESS] Universal DigiView Desktop Release Ready!
echo.
echo Outputs:
echo   - Universal Executable : dist\DigiView-Portable\DigiView.exe
echo   - Universal JAR        : dist\DigiViewSystem.jar [Java 8 - 25+ compatible]
echo   - Portable Bundle ZIP  : dist\DigiView-Portable.zip
echo ===================================================================

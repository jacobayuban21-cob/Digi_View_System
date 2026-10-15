@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   DigiView System - Portable Release Packager
echo ===================================================

cd /d "%~dp0"

echo [1/4] Compiling Java classes...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -sourcepath "shared;src" -d bin src\System\ApiClient.java src\System\DigiView.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [2/4] Packaging DigiViewSystem.jar...
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

echo [3/4] Creating trimmed portable Java runtime (jlink) and native DigiView.exe...
set "RELEASE_DIR=dist\DigiView-Portable"
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%\src\System\sounds" 2>nul
copy /y "src\System\sounds\*" "%RELEASE_DIR%\src\System\sounds\" >nul 2>&1
if exist "public" (
    xcopy /s /e /y /i "public" "%RELEASE_DIR%\public\" >nul 2>&1
)
copy /y "dist\DigiViewSystem.jar" "%RELEASE_DIR%\" >nul

jlink --add-modules java.base,java.desktop --strip-debug --no-man-pages --no-header-files --output "%RELEASE_DIR%\jre"

echo Compiling native DigiView.exe with icon...
set "CSC=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if exist "%CSC%" (
    "%CSC%" /nologo /target:winexe /out:"%RELEASE_DIR%\DigiView.exe" /win32icon:"public\Icons\Icon.ico" "src\Launcher\DigiViewLauncher.cs"
    if %errorlevel% equ 0 (
        echo [OK] DigiView.exe compiled successfully!
    ) else (
        echo [WARNING] DigiView.exe compilation failed; creating fallback batch launcher...
    )
) else (
    echo [WARNING] .NET csc compiler not found at %CSC%; creating fallback launcher...
)

rem Copy Debug and Console Log Listener batch files
copy /y "src\Launcher\DigiView-Debug.bat" "%RELEASE_DIR%\DigiView-Debug.bat" >nul
copy /y "src\Launcher\DigiView-Debug.bat" "%RELEASE_DIR%\DigiView.bat" >nul

echo [4/4] Creating ZIP package for easy distribution...
powershell -NoProfile -Command "Compress-Archive -Path '%RELEASE_DIR%\*' -DestinationPath 'dist\DigiView-Portable.zip' -Force"

echo ===================================================
echo [SUCCESS] Release package ready!
echo Output: dist\DigiView-Portable.zip
echo ===================================================

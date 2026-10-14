@echo off
cd /d "%~dp0"

if not exist "bin\System\DigiView.class" (
    echo Compiling DigiView...
    javac -encoding UTF-8 -d bin src\System\*.java
    if errorlevel 1 (
        echo Compilation failed.
        pause
        exit /b 1
    )
)

echo Starting DigiView...
start "" javaw -cp bin System.DigiView

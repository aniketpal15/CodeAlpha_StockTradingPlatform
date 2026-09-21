@echo off
cd /d "%~dp0"
echo ==========================================================
echo        Compiling Stock Trading Platform
echo ==========================================================

if not exist bin mkdir bin

javac -d bin src\com\app\stocktrading\*.java src\com\app\stocktrading\model\*.java src\com\app\stocktrading\cli\*.java src\com\app\stocktrading\gui\*.java src\com\app\stocktrading\util\*.java test\com\app\stocktrading\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation finished successfully! Classes located in bin/
) else (
    echo [ERROR] Compilation failed. Please check your Java installation.
)

pause

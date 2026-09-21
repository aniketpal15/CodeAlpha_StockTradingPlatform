@echo off
cd /d "%~dp0"
if not exist bin (
    echo bin directory not found. Compiling first...
    call compile.bat
)

java -cp bin com.app.stocktrading.Main --cli
pause

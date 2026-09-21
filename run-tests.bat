@echo off
cd /d "%~dp0"
if not exist bin\com (
    echo Compiled classes not found. Compiling first...
    call compile.bat
)

java -cp bin com.app.stocktrading.StockTradingPlatformTest
pause

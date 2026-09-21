Set-Location $PSScriptRoot

if (-not (Test-Path "bin")) {
    Write-Host "bin directory not found. Compiling first..." -ForegroundColor Yellow
    .\compile.ps1
}

java -cp bin com.app.stocktrading.Main --cli

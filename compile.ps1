Set-Location $PSScriptRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "       Compiling Stock Trading Platform         " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sourceFiles = Get-ChildItem -Recurse -Filter *.java src,test | ForEach-Object { $_.FullName }
javac -d bin $sourceFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation finished successfully! Classes located in bin/" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed. Please check your Java installation." -ForegroundColor Red
}

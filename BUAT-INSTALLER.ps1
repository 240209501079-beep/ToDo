# --- MEGA INSTALLER BUILDER (CLEAN VERSION) ---
$ErrorActionPreference = "Stop"
Clear-Host
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   PENGEMAS APLIKASI To-Do List" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

# 1. Deteksi WiX
$wixPath = "C:\Program Files (x86)\WiX Toolset v3.11\bin"
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue)) {
    if (Test-Path $wixPath) { $env:PATH += ";$wixPath" }
    else {
        Write-Host "[!] ERROR: WiX Toolset tidak ditemukan!" -ForegroundColor Red
        pause; exit
    }
}

# Fungsi untuk Animasi Loading
function Show-Progress {
    param([int]$target, [string]$task)
    for ($i = 0; $i -le $target; $i += 5) {
        Write-Progress -Activity "Sedang Memproses..." -Status "$task ($i%)" -PercentComplete $i
        Start-Sleep -Milliseconds 50
    }
}

# --- LANGKAH 1/4 ---
Write-Host "[1/4] Membersihkan paket lama & membangun JAR baru..." -ForegroundColor Yellow
if (Test-Path "ToDoApp.jar") { Remove-Item "ToDoApp.jar" -Force }
if (Test-Path "To-Do List.exe") { Remove-Item "To-Do List.exe" -Force }
.\launch.ps1
Show-Progress 30 "Mengkompilasi Java"

# --- LANGKAH 2/4 ---
Write-Host "[2/4] Menyiapkan Area Distribusi..." -ForegroundColor Yellow
Show-Progress 50 "Menyalin Library"
$distDir = "dist_temp"
if (Test-Path $distDir) { Remove-Item -Recurse -Force $distDir }
New-Item -ItemType Directory -Path $distDir | Out-Null
Copy-Item "ToDoApp.jar" -Destination $distDir
if (Test-Path "lib") { Copy-Item -Recurse "lib" -Destination $distDir }
if (Test-Path "icon.ico") { Copy-Item "icon.ico" -Destination $distDir }
if (Test-Path "firebase.properties") { Copy-Item "firebase.properties" -Destination $distDir }

# --- LANGKAH 3/4 ---
Write-Host "[3/4] Menciptakan Setup.exe (Proses Utama)..." -ForegroundColor Yellow
$outputDir = "HASIL-INSTALLER"

Stop-Process -Name "To-Do List*" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

if (Test-Path $outputDir) { 
    Get-ChildItem $outputDir -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
} else {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$appName = "To-Do List"
$appVersion = "1.2.6.1"
$installerName = "$appName-$appVersion.exe"

$installerArgs = @(
    "--name", $appName,
    "--input", $distDir,
    "--main-jar", "ToDoApp.jar",
    "--main-class", "com.todoapp.Main",
    "--type", "exe",
    "--dest", $outputDir,
    "--icon", "icon.ico",
    "--vendor", "Kelompok 7 PBO",
    "--app-version", $appVersion,
    "--win-upgrade-uuid", "e1f82e5b-3a5e-4c7b-9d8f-123456789abc",
    "--win-dir-chooser",
    "--win-shortcut",
    "--win-menu",
    "--win-per-user-install",
    "--win-menu-group", "To-Do List App",
    "--verbose"
)

Write-Progress -Activity "Membungkus Aplikasi" -Status "Menjalankan JPackage..." -PercentComplete 60
jpackage @installerArgs
Write-Progress -Activity "Membungkus Aplikasi" -Status "Selesai!" -PercentComplete 100 -Completed

# --- LANGKAH 4/4 ---
Write-Host "[4/4] Membersihkan File Sementara..." -ForegroundColor Yellow
Remove-Item -Recurse -Force $distDir
Show-Progress 100 "Finalisasi"

Write-Host "`n===============================================" -ForegroundColor Green
$finalPath = Join-Path $outputDir $installerName
if (Test-Path $finalPath) {
    Write-Host "   BERHASIL! SETUP INSTALLER TELAH SIAP" -ForegroundColor Green
    Write-Host "===============================================" -ForegroundColor Green
    Write-Host "Lokasi File: $finalPath"
} else {
    Write-Host "   PERINGATAN: File installer tidak terbentuk!" -ForegroundColor Red
}
Write-Host "-----------------------------------------------"
pause

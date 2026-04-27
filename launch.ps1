# Skrip otomatisasi untuk menyiapkan file sebelum pembuatan EXE dengan Launch4j
Write-Host "--- Memulai Persiapan Launch4j ---" -ForegroundColor Cyan

# 1. Jalankan Build JAR untuk memastikan kode terbaru (Firebase, dll) sudah masuk
Write-Host "`n[1/2] Membangun JAR terbaru..." -ForegroundColor Yellow
powershell -ExecutionPolicy Bypass -File .\build-jar.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Gagal membangun JAR. Proses dihentikan." -ForegroundColor Red
    exit 1
}

# 2. Jalankan skrip persiapan Launch4j (update versi, icon, dan XML config)
Write-Host "`n[2/2] Memperbarui konfigurasi XML Launch4j..." -ForegroundColor Yellow
powershell -ExecutionPolicy Bypass -File .\siapkan-launch4j.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Gagal menyiapkan konfigurasi Launch4j." -ForegroundColor Red
    exit 1
}

Write-Host "`n--- Persiapan Selesai! ---" -ForegroundColor Green
Write-Host "Sekarang Anda bisa membuka Launch4j dan memuat file: launch4j-config.xml" -ForegroundColor White
Write-Host "Atau jika Launch4j ada di PATH, jalankan: launch4jc .\launch4j-config.xml" -ForegroundColor Gray

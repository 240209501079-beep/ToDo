# Build JAR file for ToDo App
Write-Host "Building JAR file..." -ForegroundColor Green

$fileVersi = "app-version.properties"
if (-not (Test-Path $fileVersi)) {
    throw "File $fileVersi tidak ditemukan."
}

$barisVersi = Get-Content $fileVersi | Where-Object { $_ -match '^app\.version=' } | Select-Object -First 1
if (-not $barisVersi) {
    throw "Kunci app.version tidak ditemukan di $fileVersi."
}

$versiAplikasi = ($barisVersi -split '=', 2)[1].Trim()
if (-not $versiAplikasi) {
    throw "Nilai app.version kosong di $fileVersi."
}

Write-Host "App Version: $versiAplikasi" -ForegroundColor Yellow

# 1. Bersihkan dan buat direktori bin
if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin | Out-Null

# 2. Kompilasi
Write-Host "Compiling Java sources..."
# Compile dimulai dari Main.java, lalu javac akan menarik semua class terkait dari src/
# termasuk hasil refactor seperti TemaAplikasi, KonfigurasiUi, PembantuUi, dan WaktuSapaan.
javac -encoding UTF-8 -d bin -cp "lib/*" -sourcepath src src/com/todoapp/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    
    # 3. Ekstrak library ke bin agar JAR bisa jalan mandiri (Fat JAR)
    Write-Host "Extracting libraries..."
    Push-Location bin
    foreach ($jar in Get-ChildItem ../lib/*.jar) {
        jar xf $jar.FullName
    }
    Pop-Location

    # 4. Buat manifest dengan versi aplikasi
    $manifestSementara = "manifest-temp.mf"
    @"
Manifest-Version: 1.0
Main-Class: com.todoapp.Main
Implementation-Version: $versiAplikasi

"@ | Set-Content -Path $manifestSementara -Encoding Ascii

    # 5. Buat JAR
    Write-Host "Creating JAR file..."
    jar cfm ToDoApp.jar $manifestSementara -C bin .
    Remove-Item $manifestSementara -ErrorAction SilentlyContinue
    
    if (Test-Path ToDoApp.jar) {
        Write-Host "[OK] JAR created: ToDoApp.jar" -ForegroundColor Green
        Write-Host ""
        Write-Host "Sekarang kamu bisa jalankan dengan: java -jar ToDoApp.jar" -ForegroundColor Cyan
    } 
} else {
        Write-Host "[FAIL] Compilation failed!" -ForegroundColor Red
}

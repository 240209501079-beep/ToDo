# Menyiapkan config Launch4j dari versi aplikasi yang tersentral
$ErrorActionPreference = "Stop"

function Ubah-KeVersiWindows {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Versi
    )

    $angka = $Versi.Split('.') | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }
    if ($angka.Count -lt 1 -or $angka.Count -gt 4) {
        throw "Format versi tidak valid: $Versi"
    }

    foreach ($item in $angka) {
        if ($item -notmatch '^\d+$') {
            throw "Versi harus numerik dipisahkan titik, contoh 1.2.3"
        }
    }

    while ($angka.Count -lt 4) {
        $angka += "0"
    }

    return ($angka -join '.')
}

$akar = Get-Location
$fileVersi = Join-Path $akar "app-version.properties"
if (-not (Test-Path $fileVersi)) {
    throw "File app-version.properties tidak ditemukan di $akar"
}

$barisVersi = Get-Content $fileVersi | Where-Object { $_ -match '^app\.version=' } | Select-Object -First 1
if (-not $barisVersi) {
    throw "Kunci app.version tidak ditemukan di app-version.properties"
}

$versiAplikasi = ($barisVersi -split '=', 2)[1].Trim()
if (-not $versiAplikasi) {
    throw "Nilai app.version kosong"
}

$versiWindows = Ubah-KeVersiWindows -Versi $versiAplikasi
$namaProduk = "ToDoApp"
$namaPerusahaan = "ToDo Team"
$deskripsi = "Aplikasi ToDo Desktop"

$xml = @"
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>ToDoApp.jar</jar>
  <outfile>ToDoApp.exe</outfile>
  <errTitle>$namaProduk</errTitle>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://adoptium.net/</downloadUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon></icon>
  <jre>
    <path></path>
    <minVersion>17</minVersion>
    <maxVersion></maxVersion>
  </jre>
  <versionInfo>
    <fileVersion>$versiWindows</fileVersion>
    <txtFileVersion>$versiAplikasi</txtFileVersion>
    <fileDescription>$deskripsi</fileDescription>
    <copyright>2026</copyright>
    <productVersion>$versiWindows</productVersion>
    <txtProductVersion>$versiAplikasi</txtProductVersion>
    <productName>$namaProduk</productName>
    <companyName>$namaPerusahaan</companyName>
    <internalName>$namaProduk</internalName>
    <originalFilename>ToDoApp.exe</originalFilename>
  </versionInfo>
</launch4jConfig>
"@

$keluaran = Join-Path $akar "launch4j-config.xml"
$xml | Set-Content -Path $keluaran -Encoding Ascii

Write-Host "Config Launch4j berhasil dibuat:" -ForegroundColor Green
Write-Host $keluaran -ForegroundColor Cyan
Write-Host "Versi aplikasi: $versiAplikasi" -ForegroundColor Yellow

# Menyiapkan config Launch4j dari versi aplikasi yang tersentral
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

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

function Buat-IconDariPng {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PathPng,
        [Parameter(Mandatory = $true)]
        [string]$PathIco
    )

    $lebarTarget = 256
    $tinggiTarget = 256

    $gambarAsli = [System.Drawing.Image]::FromFile($PathPng)
    try {
        $bitmapTarget = New-Object System.Drawing.Bitmap($lebarTarget, $tinggiTarget)
        try {
            $grafik = [System.Drawing.Graphics]::FromImage($bitmapTarget)
            try {
                $grafik.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
                $grafik.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                $grafik.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
                $grafik.DrawImage($gambarAsli, 0, 0, $lebarTarget, $tinggiTarget)
            } finally {
                $grafik.Dispose()
            }

            $streamPng = New-Object System.IO.MemoryStream
            try {
                $bitmapTarget.Save($streamPng, [System.Drawing.Imaging.ImageFormat]::Png)
                $bytesPng = $streamPng.ToArray()

                $streamIco = New-Object System.IO.FileStream($PathIco, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write)
                try {
                    $writer = New-Object System.IO.BinaryWriter($streamIco)
                    try {
                        # ICONDIR
                        $writer.Write([UInt16]0)
                        $writer.Write([UInt16]1)
                        $writer.Write([UInt16]1)

                        # ICONDIRENTRY
                        $writer.Write([Byte]0) # 0 berarti 256 px
                        $writer.Write([Byte]0) # 0 berarti 256 px
                        $writer.Write([Byte]0)
                        $writer.Write([Byte]0)
                        $writer.Write([UInt16]1)
                        $writer.Write([UInt16]32)
                        $writer.Write([UInt32]$bytesPng.Length)
                        $writer.Write([UInt32]22)

                        # Data gambar PNG
                        $writer.Write($bytesPng)
                    } finally {
                        $writer.Dispose()
                    }
                } finally {
                    $streamIco.Dispose()
                }
            } finally {
                $streamPng.Dispose()
            }
        } finally {
            $bitmapTarget.Dispose()
        }
    } finally {
        $gambarAsli.Dispose()
    }
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
$namaProduk = "ToDoTask"
$namaPerusahaan = "ToDo Team"
$deskripsi = "Aplikasi ToDo Desktop"
$ikonAplikasi = ""
$pathJar = Join-Path $akar "ToDoApp.jar"
$pathExe = Join-Path $akar "ToDoTask.exe"

$pathIco = Join-Path $akar "icon.ico"
$pathPng = Join-Path $akar "icon.png"

if (Test-Path $pathIco) {
    $ikonAplikasi = $pathIco
} elseif (Test-Path $pathPng) {
    Buat-IconDariPng -PathPng $pathPng -PathIco $pathIco
    $ikonAplikasi = $pathIco
}

$xml = @"
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
    <jar>$pathJar</jar>
        <outfile>$pathExe</outfile>
  <errTitle>$namaProduk</errTitle>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://adoptium.net/</downloadUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
    <icon>$ikonAplikasi</icon>
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
                <originalFilename>ToDoTask.exe</originalFilename>
  </versionInfo>
</launch4jConfig>
"@

$keluaran = Join-Path $akar "launch4j-config.xml"
$xml | Set-Content -Path $keluaran -Encoding Ascii

Write-Host "Config Launch4j berhasil dibuat:" -ForegroundColor Green
Write-Host $keluaran -ForegroundColor Cyan
Write-Host "Versi aplikasi: $versiAplikasi" -ForegroundColor Yellow

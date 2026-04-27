# 🛠️ Cara Build EXE — To-Do List (Java Desktop)

Panduan lengkap dari kompilasi source code Java hingga menghasilkan file `.exe` siap pakai.

---

## 📋 Ringkasan Dua Metode Build

| | **Metode A: Launch4j** | **Metode B: JPackage + WiX** |
|---|---|---|
| **Hasil** | `.exe` ringan (~2 MB) | Installer mandiri (~50+ MB) |
| **Java di PC target** | **Wajib ada** (min Java 17) | **Tidak perlu** (JRE dibundel) |
| **Alat tambahan** | Launch4j | JPackage + WiX Toolset v3 |
| **Kecepatan build** | Cepat | Lebih lambat |
| **Cocok untuk** | Developer / internal | Distribusi ke user umum |

---

## ⚙️ Prasyarat

Pastikan semua alat berikut sudah terinstall:

- ✅ **Java JDK 17+** → [Download Adoptium](https://adoptium.net/)
- ✅ **Launch4j** (Metode A) → [Download](https://launch4j.sourceforge.net/)
- ✅ **WiX Toolset v3.11** (Metode B) → [Download](https://github.com/wixtoolset/wix3/releases)

Verifikasi Java sudah terdeteksi di terminal:

```powershell
java -version
javac -version
```

---

## 📁 File yang Terlibat

```
ToDo/
├── app-version.properties    ← (1) Sumber versi terpusat
├── build-jar.ps1             ← (2) Script kompilasi → ToDoApp.jar
├── siapkan-launch4j.ps1      ← (3) Script generate launch4j-config.xml
├── launch4j-config.xml       ← (4) Config Launch4j (auto-generated, jangan edit manual)
├── BUAT-INSTALLER.ps1        ← (5) Script build installer mandiri (Metode B)
└── HASIL-INSTALLER/          ← Output installer .exe final
```

---

## 🅰️ Metode A — Launch4j (Cepat, untuk Developer)

### Langkah 1 — Atur Versi Aplikasi

Buka `app-version.properties`:

```properties
app.version=1.2.6
```

> Gunakan format `MAJOR.MINOR.PATCH` (contoh: `1.0.0`, `1.2.6`).  
> Nilai ini akan otomatis masuk ke manifest JAR dan metadata EXE Windows.

---

### Langkah 2 — Build JAR (Fat JAR)

Buka terminal PowerShell di folder project, lalu jalankan:

```powershell
powershell -ExecutionPolicy Bypass -File build-jar.ps1
```

**Yang terjadi secara otomatis:**

| Langkah | Aksi |
|---|---|
| 1 | Bersihkan folder `bin/` |
| 2 | Kompilasi semua `.java` di `src/` dengan classpath `lib/` |
| 3 | Salin aset gambar (`icon.png`, `iconmin.png`) ke `bin/` |
| 4 | Ekstrak semua `.jar` di `lib/` ke dalam `bin/` → Fat JAR |
| 5 | Buat `manifest-temp.mf` berisi `Main-Class` dan `Implementation-Version` |
| 6 | Hasilkan `ToDoApp.jar` |

**Output:** `ToDoApp.jar` di folder project.

---

### Langkah 3 — Verifikasi & Test JAR

```powershell
# Pastikan file terbentuk
dir ToDoApp.jar

# Jalankan untuk test
java -jar ToDoApp.jar
```

Jika aplikasi terbuka normal → **lanjut ke langkah berikutnya**.

---

### Langkah 4 — Generate Konfigurasi Launch4j

```powershell
powershell -ExecutionPolicy Bypass -File siapkan-launch4j.ps1
```

**Yang terjadi secara otomatis:**

1. Baca versi dari `app-version.properties`
2. Konversi ke format Windows: `1.2.6` → `1.2.6.0`
3. Jika `icon.ico` belum ada → generate otomatis dari `icon.png` (256×256)
4. Generate file `launch4j-config.xml` dengan metadata lengkap:

```xml
<versionInfo>
  <fileVersion>1.2.6.0</fileVersion>
  <productName>To-Do List</productName>
  <companyName>Kelompok 7</companyName>
  <fileDescription>Aplikasi ToDo Desktop</fileDescription>
</versionInfo>
```

> ⚠️ **Selalu gunakan file config yang di-generate script ini.**  
> Jangan edit `launch4j-config.xml` secara manual agar versi tetap konsisten.

---

### Langkah 5 — Build EXE dengan Launch4j GUI

1. Buka aplikasi **Launch4j**
2. Klik **Open** → pilih file `launch4j-config.xml`
3. Periksa field utama sudah benar:

| Field | Nilai yang diharapkan |
|---|---|
| Jar | `...\ToDo\ToDoApp.jar` |
| Output file | `...\ToDo\To-Do List.exe` |
| Icon | `...\ToDo\icon.ico` |
| Min JRE version | `17` |

4. Klik tombol **Build wrapper** (ikon gear ⚙️)

**Output:** `To-Do List.exe` di folder project (~2 MB).

---

### Langkah 6 — Test EXE

```powershell
.\"To-Do List.exe"
```

Jika berjalan normal → EXE siap dipakai/dibagikan ke sesama developer yang punya Java 17+.

---

### ♻️ Alur Rilis Versi Baru (Metode A)

Setiap rilis versi baru, ulangi urutan berikut:

```
1. Ubah app.version di app-version.properties
        ↓
2. Jalankan: build-jar.ps1
        ↓
3. Jalankan: siapkan-launch4j.ps1
        ↓
4. Buka Launch4j → load launch4j-config.xml → Build wrapper
```

---

## 🅱️ Metode B — JPackage + WiX (Installer Mandiri)

Metode ini menghasilkan installer yang **sudah menyertakan JRE** sehingga user tidak perlu install Java sama sekali.

### Prasyarat Tambahan

Pastikan **WiX Toolset v3.11** sudah terinstall di:
```
C:\Program Files (x86)\WiX Toolset v3.11\bin\
```

---

### Jalankan Script All-in-One

```powershell
powershell -ExecutionPolicy Bypass -File BUAT-INSTALLER.ps1
```

**Yang terjadi secara otomatis (4 langkah dalam 1 script):**

| Langkah | Aksi |
|---|---|
| 1/4 | Hapus JAR & EXE lama → build JAR baru via `launch.ps1` |
| 2/4 | Buat folder `dist_temp/` → salin JAR, library, ikon, `firebase.properties` |
| 3/4 | Jalankan `jpackage` → bungkus JAR + JRE → hasilkan installer `.exe` di `HASIL-INSTALLER/` |
| 4/4 | Hapus folder `dist_temp/` (bersihkan sisa build) |

**Output:** `HASIL-INSTALLER\To-Do List-x.x.x.exe`

---

### Parameter JPackage yang Digunakan

```powershell
jpackage --name "To-Do List"
         --input dist_temp
         --main-jar ToDoApp.jar
         --main-class com.todoapp.Main
         --type exe
         --dest HASIL-INSTALLER
         --icon icon.ico
         --win-dir-chooser      # User bisa pilih folder instalasi
         --win-shortcut         # Buat shortcut di Desktop
         --win-menu             # Daftarkan ke Start Menu
         --vendor "Kelompok 7 PBO"
         --app-version "x.x.x"
```

---

## 🆚 Via VS Code Task (Alternatif Terminal)

Selain lewat terminal, build bisa dijalankan via **VS Code Task**:

Tekan `Ctrl+Shift+P` → ketik **Run Task** → pilih:

| Task Name | Setara dengan |
|---|---|
| `Build JAR ToDo App` | `build-jar.ps1` |
| `Generate Launch4j Config` | `siapkan-launch4j.ps1` |

---

## ❗ Catatan Penting

> [!IMPORTANT]
> EXE hasil **Metode A (Launch4j)** tetap membutuhkan **Java Runtime 17+** di PC target.
> Jika user tidak punya Java, gunakan **Metode B (JPackage + WiX)**.

> [!WARNING]
> Jangan edit `launch4j-config.xml` secara manual.
> Selalu generate ulang via `siapkan-launch4j.ps1` agar versi konsisten dengan `app-version.properties`.

> [!NOTE]
> File `firebase.properties` ikut disalin saat build Metode B agar aplikasi bisa login setelah diinstall di PC lain.

---

## 🔍 Troubleshooting

| Masalah | Solusi |
|---|---|
| `javac` tidak dikenal | Pastikan JDK (bukan JRE) terinstall dan masuk ke PATH |
| `ToDoApp.jar belum ada` | Jalankan `build-jar.ps1` terlebih dahulu |
| Launch4j gagal build | Pastikan path JAR dan icon di config sudah benar dan file ada |
| `candle.exe tidak ditemukan` | Install WiX Toolset v3.11 atau tambahkan ke PATH |
| Aplikasi tidak bisa login setelah install | Pastikan `firebase.properties` ikut disalin ke `dist_temp/` |

---

*Dikembangkan oleh **Kelompok 7 — PBO***

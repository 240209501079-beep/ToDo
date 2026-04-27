# 📋 To-Do List (Java Desktop)

Aplikasi To-Do List modern berbasis **Java Swing** dengan sinkronisasi Firebase Cloud, fitur System Tray, notifikasi deadline, dan Smart Re-Open.

---

## ✨ Fitur Utama

| Fitur | Keterangan |
|---|---|
| ☁️ **Sinkronisasi Cloud** | Data tersimpan aman di Firebase Firestore secara real-time |
| 🔔 **Notifikasi Deadline** | Peringatan otomatis mendekati batas waktu tugas |
| 🖥️ **System Tray** | Berjalan di latar belakang tanpa memenuhi taskbar |
| 🪟 **Smart Re-Open** | Klik ikon tray → jendela langsung muncul ke depan |
| 🎨 **Premium UI** | Desain Dark Slate modern dan responsif |
| 📦 **Installer Mandiri** | Bisa dijalankan di Windows tanpa perlu install Java |

---

## 🚀 Cara Instalasi (Untuk User)

1. Unduh file **`To-Do List-x.x.x.exe`** dari folder `HASIL-INSTALLER/`.
2. Jalankan installer dan ikuti petunjuknya.
3. Buka **Start Menu** → cari **"To-Do List"** untuk memulai.

---

## 🧑‍💻 Panduan Pengembangan (Untuk Developer)

### Prasyarat

- **Java JDK 17** atau lebih baru → [Download](https://adoptium.net/)
- **Launch4j** (untuk buat `.exe` ringan) → [Download](https://launch4j.sourceforge.net/)
- **WiX Toolset v3** (opsional, untuk buat installer `.exe` profesional)

### Struktur Folder

```
ToDo/
├── src/com/todoapp/
│   ├── Main.java           # Entry point & logika Single Instance
│   ├── service/            # Logika bisnis (Auth & Firebase)
│   └── ui/                 # Komponen tampilan (Swing)
├── lib/                    # Library pihak ketiga (Firebase SDK, GSON)
├── bin/                    # Output hasil kompilasi (auto-generated)
├── HASIL-INSTALLER/        # Output file installer .exe final
├── app-version.properties  # Versi aplikasi terpusat
├── build-jar.ps1           # Script build JAR
├── siapkan-launch4j.ps1    # Script generate konfigurasi Launch4j
├── launch4j-config.xml     # Konfigurasi Launch4j (auto-generated)
└── BUAT-INSTALLER.ps1      # Script build installer WiX
```

---

## 🛠️ Cara Build EXE — Langkah demi Langkah

### Langkah 1 — Atur Versi Aplikasi

Buka `app-version.properties` dan ubah nilai versi:

```properties
app.version=1.2.6
```

> Gunakan format `MAJOR.MINOR.PATCH`, contoh: `1.0.0`, `1.2.6`.
> Versi ini akan otomatis masuk ke manifest JAR dan metadata EXE.

---

### Langkah 2 — Build JAR

Jalankan di terminal PowerShell dari folder project:

```powershell
powershell -ExecutionPolicy Bypass -File build-jar.ps1
```

Yang dilakukan script ini secara otomatis:
1. Bersihkan folder `bin/`
2. Kompilasi seluruh source Java (`src/`) dengan classpath library
3. Ekstrak dependency dari `lib/` ke `bin/` *(membuat Fat JAR)*
4. Buat file `ToDoApp.jar` dengan manifest berisi versi aplikasi

---

### Langkah 3 — Verifikasi & Test JAR

```powershell
# Cek file terbentuk
dir ToDoApp.jar

# Jalankan untuk test
java -jar ToDoApp.jar
```

Jika aplikasi terbuka normal → lanjut ke langkah berikutnya.

---

### Langkah 4 — Generate Konfigurasi Launch4j

```powershell
powershell -ExecutionPolicy Bypass -File siapkan-launch4j.ps1
```

Script ini akan menghasilkan file `launch4j-config.xml` dengan:
- Versi EXE terisi otomatis dari `app-version.properties`
- Metadata produk: *To-Do List*, *Kelompok 7*, *Aplikasi ToDo Desktop*
- Mapping versi: `1.2.6` → `fileVersion: 1.2.6.0`

---

### Langkah 5 — Build EXE dengan Launch4j

1. Buka aplikasi **Launch4j**
2. **Open** file `launch4j-config.xml`
3. Pastikan field ini sudah benar:
   - **Jar:** `...\ToDo\ToDoApp.jar`
   - **Output:** `...\ToDo\To-Do List.exe`
   - **Icon:** `...\ToDo\icon.ico`
   - **Min JRE version:** `17`
4. Klik tombol **Build wrapper** (ikon gear ⚙️)

✅ Output: **`To-Do List.exe`** di folder project.

---

### Langkah 6 — Test EXE

```powershell
.\"To-Do List.exe"
```

> **Catatan:** EXE hasil Launch4j tetap membutuhkan **Java Runtime 17+** di komputer target.
> Untuk distribusi tanpa syarat Java, gunakan `BUAT-INSTALLER.ps1` (metode JPackage + WiX).

---

### (Opsional) Langkah 7 — Build Installer Mandiri

Installer ini sudah **menyertakan JRE** sehingga user tidak perlu install Java sama sekali.

```powershell
powershell -ExecutionPolicy Bypass -File BUAT-INSTALLER.ps1
```

Output: file installer `.exe` di folder `HASIL-INSTALLER/`.

---

### ♻️ Alur Rilis Versi Baru

Setiap kali ingin rilis versi baru, ulangi urutan ini:

```
1. Ubah app.version di app-version.properties
2. Jalankan: build-jar.ps1
3. Jalankan: siapkan-launch4j.ps1
4. Buka Launch4j → load launch4j-config.xml → Build wrapper
```

Atau via **VS Code Task** (Ctrl+Shift+P → Run Task):
- `Build JAR ToDo App`
- `Generate Launch4j Config`

---

## 🔬 Penjelasan Teknis Java

### 1. Mekanisme Smart Re-Open (IPC via ServerSocket)

Aplikasi menggunakan `ServerSocket` pada port **55667** untuk mencegah banyak instance berjalan bersamaan:

- Saat pertama dibuka → berhasil menduduki port → berjalan normal sebagai **server**
- Saat dibuka lagi → gagal menduduki port (port sudah dipakai) → berfungsi sebagai **client**, mengirim sinyal "bangun" ke instance pertama → lalu menutup diri
- Instance pertama menerima sinyal → memanggil `setVisible(true)` dan `toFront()` agar jendela muncul

### 2. Sinkronisasi Firebase (Async)

- Semua operasi baca/tulis ke Firestore dijalankan di **thread terpisah** (bukan EDT) untuk menjaga UI tetap responsif
- Perubahan data ditampilkan **langsung ke UI** via cache lokal, sementara sinkronisasi cloud berjalan di background
- Menggunakan **Firebase REST API** + library **GSON** untuk parsing JSON

### 3. Keamanan Sesi

- Token autentikasi Firebase disimpan lokal di `session.json`
- Setiap kali aplikasi dibuka, token di-**refresh otomatis** jika sesi masih berlaku
- Refresh Token dilakukan secara diam-diam tanpa mengganggu pengguna

### 4. Metode Pengemasan (Packaging)

| Metode | Alat | Kebutuhan di PC Target | Ukuran |
|---|---|---|---|
| Ringan | **Launch4j** | Java 17+ wajib ada | ~2 MB |
| Mandiri | **JPackage + WiX** | Tidak perlu Java | ~50+ MB |

---

## 📌 Catatan Penting

- Selalu gunakan `launch4j-config.xml` yang di-generate script agar versi terisi otomatis
- Jalankan EXE dari folder yang sama dengan data project agar path data konsisten
- File `firebase.properties` dan `session.json` **jangan di-commit** ke Git (sudah ada di `.gitignore`)

---

*Dikembangkan oleh **Kelompok 7 — PBO***

# 🔬 Penjelasan Teknis — To-Do List (Java Desktop)

Dokumen ini menjelaskan arsitektur, desain, dan mekanisme internal aplikasi secara mendalam.

---

## 📦 Stack Teknologi

| Komponen | Teknologi |
|---|---|
| Bahasa | **Java 17** |
| UI Framework | **Java Swing** + **FlatLaf** (tema modern) |
| Backend Cloud | **Firebase Firestore** (via REST API) |
| HTTP Client | `java.net.http.HttpClient` (bawaan Java 11+) |
| JSON Parser | **GSON** (Google) |
| Pengemasan | **Launch4j** / **JPackage + WiX Toolset** |

---

## 🗂️ Struktur Package

```
src/com/todoapp/
│
├── Main.java                    # Entry point, Single Instance Guard, IPC Server
├── TemaAplikasi.java            # Konfigurasi tema FlatLaf global
│
├── model/
│   └── Tugas.java               # Model data tugas (POJO + serializer)
│
├── persistence/
│   └── FirebaseStorage.java     # Lapisan akses data ke Firestore REST API
│
├── service/
│   ├── AuthService.java         # Login, logout, refresh token Firebase Auth
│   ├── KonfigurasiFirebase.java # Konstanta konfigurasi Firebase (project ID, API key)
│   ├── LayananTugas.java        # Logika bisnis CRUD tugas + pengingat deadline
│   └── SessionManager.java     # Manajemen sesi login (baca/tulis session.json)
│
└── ui/
    ├── FrameManajerTugas.java   # Jendela utama aplikasi (tabel tugas, System Tray)
    ├── LoginFrame.java          # Form login pengguna
    ├── KonfigurasiUi.java       # Konstanta warna & font UI
    ├── NotifikasiToast.java     # Komponen notifikasi toast pop-up
    ├── PembantuUi.java          # Utilitas helper untuk komponen Swing
    └── WaktuSapaan.java         # Generator teks sapaan berdasarkan jam
```

---

## ⚙️ Mekanisme Utama

### 1. Single Instance Guard + Smart Re-Open (IPC)

Mencegah lebih dari satu instance aplikasi berjalan bersamaan.

**Alur kerja (`Main.java`):**

```
Aplikasi dibuka
    │
    ├─► Coba kunci file "todoapp.lock" di folder home user
    │       │
    │       ├─ Berhasil → Instance PERTAMA
    │       │       └─► Mulai IPC Server di port 55667
    │       │           └─► Jalankan aplikasi normal
    │       │
    │       └─ Gagal → Instance KEDUA (ada yang sudah jalan)
    │               └─► Kirim koneksi ke port 55667
    │               └─► System.exit(0) — tutup diri
    │
Instance PERTAMA menerima koneksi dari port 55667
    └─► SwingUtilities.invokeLater → setVisible(true) + toFront()
```

**Mengapa `FileLock` bukan hanya `ServerSocket`?**
- `FileLock` lebih andal; file lock otomatis dilepas oleh OS jika proses crash
- `ServerSocket` dipakai khusus untuk **jalur komunikasi** sinyal "bangun"

---

### 2. Alur Startup & Autentikasi

```
main()
  │
  ├─ TemaAplikasi.terapkanTemaDefault()   ← Terapkan tema FlatLaf
  ├─ SessionManager.muatSesi()            ← Baca session.json
  │
  ├─ [Sesi ada] → AuthService.refreshFirebaseToken()
  │       │           (async, tidak memblokir UI)
  │       │
  │       ├─ Sukses → Simpan token baru → Buka FrameManajerTugas
  │       │                               └─ refreshData() di background thread
  │       └─ Gagal  → Buka LoginFrame
  │
  └─ [Sesi tidak ada] → Buka LoginFrame langsung
```

**Kenapa refresh token tiap startup?**
- ID Token Firebase hanya berlaku **1 jam**
- Refresh Token tidak kedaluwarsa (kecuali user logout manual)
- Dengan refresh otomatis, user tidak perlu login ulang setiap hari

---

### 3. Arsitektur Lapisan Data

```
UI (FrameManajerTugas)
    │
    ▼
LayananTugas          ← Logika bisnis, validasi, pengurutan, filter
    │
    ▼
FirebaseStorage       ← Komunikasi HTTP ke Firestore REST API
    │
    ▼
Firebase Firestore    ← Database cloud (per user: users/{uid}/tasks/{id})
```

**Pola "Optimistic Update":**
1. Data diperbarui **langsung ke cache lokal** (`daftarTugas` di `LayananTugas`)
2. UI di-refresh seketika → terasa instan ke pengguna
3. Sinkronisasi ke Firestore berjalan **asinkron** di background
4. Jika sinkronisasi gagal, error dicatat di console (tidak crash UI)

---

### 4. Model Data — `Tugas.java`

```java
class Tugas {
    int id;               // ID unik tugas
    String judul;         // Nama/judul tugas
    String deskripsi;     // Keterangan detail
    LocalDateTime tenggat; // Batas waktu (tanggal + jam)
    Prioritas prioritas;  // Enum: TINGGI, SEDANG, RENDAH
    boolean selesai;      // Status centang
}
```

- **Serialisasi ke Firestore:** Field disimpan sebagai `stringValue` / `booleanValue` sesuai format Firestore REST
- **Serialisasi ke file lama:** Format `id|judul_base64|deskripsi_base64|tanggal|prioritas|selesai` dengan encoding Base64 untuk keamanan karakter khusus

---

### 5. Firebase Storage — Komunikasi REST

**Endpoint dasar:**
```
https://firestore.googleapis.com/v1/projects/{PROJECT_ID}
    /databases/(default)/documents/users/{userId}/tasks/{id}
```

| Operasi | HTTP Method | Keterangan |
|---|---|---|
| Muat semua tugas | `GET` | Sinkron (blocking), dijalankan di background thread |
| Simpan/Update tugas | `PATCH` | Asinkron (`sendAsync`), tidak blocking UI |
| Hapus tugas | `DELETE` | Asinkron (`sendAsync`), tidak blocking UI |

**Kenapa PATCH bukan PUT/POST?**
- `PATCH` ke URL spesifik berfungsi sebagai **Upsert** (buat jika belum ada, update jika sudah ada)
- Tidak perlu dua operasi berbeda untuk create vs update

---

### 6. Manajemen Sesi — `SessionManager.java`

Data sesi disimpan di file `session.json` di folder project:

```json
{
  "token": "eyJhbGci...",       ← ID Token Firebase (berlaku 1 jam)
  "refresh": "AMf-vBx...",      ← Refresh Token (tidak kedaluwarsa)
  "email": "user@email.com",
  "uid": "abc123xyz"
}
```

> ⚠️ File ini mengandung token autentikasi sensitif.  
> **Jangan commit `session.json` ke Git** — sudah masuk `.gitignore`.

---

### 7. Sistem Pengingat Deadline

`LayananTugas.ambilPengingat()` menghitung sisa hari dari setiap tugas yang belum selesai:

| Sisa Hari | Label Pengingat |
|---|---|
| 0 hari | `[Hari H]` |
| 1 hari | `[H-1]` |
| 3 hari | `[H-3]` |

Pengingat ini ditampilkan sebagai **notifikasi toast** (`NotifikasiToast`) di sudut layar.

---

### 8. Tema UI — `TemaAplikasi.java`

Aplikasi menggunakan **FlatLaf** (`FlatMacLightLaf`) sebagai Look & Feel modern pengganti tema bawaan Java Swing yang lawas.

Kustomisasi global yang diterapkan:

| Properti | Nilai |
|---|---|
| Sudut komponen | 10px (rounded) |
| Scrollbar width | 10px |
| Tabel | Hanya garis horizontal, tanpa garis vertikal |
| Warna seleksi tabel | Biru muda (`WARNA_BIRU_MUDA`) |
| Tombol default | Latar biru, teks putih |

---

## 🔄 Alur Lengkap — Tambah Tugas Baru

```
User klik tombol "Tambah"
    │
    ▼
FrameManajerTugas → buka dialog input
    │
    ▼ (user isi form & klik Simpan)
LayananTugas.tambahTugas(judul, deskripsi, tenggat, prioritas)
    │
    ├─► Buat objek Tugas baru (id otomatis incremental)
    ├─► Tambah ke daftarTugas (cache lokal) ← UI langsung update
    └─► FirebaseStorage.simpanTugas(tugas)
              └─► httpClient.sendAsync(PATCH ...) ← background, tidak blocking
```

---

## 📚 Library Pihak Ketiga

| Library | Versi | Fungsi |
|---|---|---|
| `flatlaf-*.jar` | 3.x | Tema UI modern untuk Swing |
| `gson-*.jar` | 2.x | Parsing & serialisasi JSON |

Semua library tersimpan di folder `lib/` dan akan dimasukkan ke dalam **Fat JAR** saat proses build.

---

*Dikembangkan oleh **Kelompok 7 — PBO***

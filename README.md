# Java To-Do GUI App (Firebase Edition)

Aplikasi To-Do List berbasis GUI (Swing) modern dengan integrasi **Firebase Firestore** dan **Google Login**.

## Catatan Update Utama
- **Cloud Storage:** Menggunakan Firebase Firestore alih-alih file lokal.
- **Google Login:** Otentikasi aman menggunakan Google OAuth2.
- **Modern UI:** Tampilan premium dengan FlatLaf Mac Light theme dan ikon Unicode.
- **Stay Logged In:** Sesi login disimpan secara lokal agar tidak perlu login berulang.

## Fitur
- CRUD tugas tersinkronisasi online
- Google Sign-In terintegrasi browser
- Filter berdasarkan status dan prioritas
- Pengingat otomatis (H-3, H-1, Hari H)
- Metadata EXE otomatis untuk Launch4j

## Struktur Utama
- `src/com/todoapp/Main.java` - Entry point (Session Checker)
- `src/com/todoapp/service/AuthService.java` - Logika Login Google & Firebase
- `src/com/todoapp/persistence/FirebaseStorage.java` - Firestore REST Client
- `src/com/todoapp/ui/LoginFrame.java` - Layar Login Modern
- `src/com/todoapp/ui/FrameManajerTugas.java` - Dashboard Utama
- `launch.ps1` - Skrip otomasi build & persiapan EXE

## Cara Menjalankan

### Build dan Persiapan EXE Otomatis
Gunakan skrip master untuk melakukan kompilasi JAR dan persiapan config Launch4j sekaligus:
```powershell
.\launch.ps1
```

### Menjalankan JAR Langsung
```powershell
java -jar ToDoApp.jar
```

## Setup Firebase (Wajib untuk Dev)
Aplikasi membutuhkan API Key dan Client ID Google yang valid di `KonfigurasiFirebase.java`.
1. Siapkan project di Firebase Console.
2. Aktifkan Authentication (Google) dan Firestore.
3. Ambil Web API Key dan Web OAuth Client ID.
4. Masukkan ke `src/com/todoapp/service/KonfigurasiFirebase.java`.

## Build EXE dengan Launch4j
1. Jalankan `.\launch.ps1`
2. Buka `launch4j-config.xml` di Launch4j.
3. Klik Build wrapper (ikon gerigi).
4. Hasil: `ToDoTask.exe`.

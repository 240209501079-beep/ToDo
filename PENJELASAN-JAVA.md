# Panduan Teknis To-Do List (Java)

## Arsitektur Aplikasi
Aplikasi ini dibangun menggunakan Java Swing dengan beberapa fitur canggih:

### 1. Mekanisme Smart Re-Open (IPC)
Aplikasi menggunakan `ServerSocket` pada port `55667`. 
- Saat aplikasi dibuka, ia mencoba menduduki port tersebut.
- Jika gagal (port sibuk), berarti ada instance lain yang sedang jalan.
- Instance baru akan mengirim sinyal ke instance lama untuk "bangun" dan menampilkan jendela, lalu instance baru akan menutup diri.

### 2. Keamanan Sesi
- Token Firebase disimpan secara lokal di `session.json`.
- Aplikasi otomatis melakukan *Refresh Token* setiap kali dibuka jika sesi masih berlaku.

### 3. Pengemasan (Packaging)
- **Launch4j:** Digunakan untuk mengubah JAR menjadi EXE ringan (membutuhkan Java di PC user).
- **JPackage + WiX:** Digunakan untuk menciptakan "Fat Installer" yang sudah berisi JRE (Java Runtime) di dalamnya, sehingga user tidak perlu install Java sama sekali.

## Struktur File
- `src/com/todoapp/Main.java`: Pintu masuk utama & logika Single Instance.
- `src/com/todoapp/service/`: Logika bisnis (Auth & Firebase).
- `src/com/todoapp/ui/`: Semua komponen tampilan.
- `lib/`: Library pihak ketiga (Firebase & GSON).

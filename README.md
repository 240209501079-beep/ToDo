# To-Do List (Java Desktop)

Aplikasi To-Do List modern dengan sinkronisasi Firebase Cloud, fitur System Tray, dan Smart Re-Open.

## Fitur Utama
- **Sinkronisasi Cloud:** Data tersimpan aman di Firebase Firestore.
- **Smart Re-Open:** Jika aplikasi diklik saat sudah berjalan di Tray, ia akan otomatis muncul ke depan.
- **System Tray:** Aplikasi bisa berjalan di latar belakang tanpa mengganggu taskbar.
- **Premium UI:** Desain Dark Slate yang bersih dan responsif.
- **Installer Mandiri:** Bisa dijalankan di Windows mana pun tanpa perlu install Java secara manual.

## Cara Instalasi (Untuk User)
1. Unduh file `To-Do List-Setup.exe` dari folder `HASIL-INSTALLER`.
2. Jalankan installer dan ikuti petunjuknya.
3. Cari "To-Do List" di Start Menu untuk memulai.

## Panduan Pengembangan (Untuk Developer)
### Prasyarat
- Java 17 atau lebih baru.
- WiX Toolset v3 (untuk membuat installer .exe).

### Skrip Penting
- `.\launch.ps1`: Membangun JAR dan menyiapkan konfigurasi Launch4j.
- `.\SETUP-APLIKASI.ps1`: Membangun JAR sekaligus mendaftarkan shortcut ke Start Menu lokal.
- `.\BUAT-INSTALLER.ps1`: Membuat file installer `.exe` profesional untuk dibagikan ke user lain.

---
*Dikembangkan oleh Kelompok 7 PBO*

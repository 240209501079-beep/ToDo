# Penjelasan File Java - ToDo App

Dokumen ini menjelaskan fungsi masing-masing file Java di project, termasuk relasi antar file dan titik ubah yang paling sering dipakai saat maintenance.

## Gambaran Arsitektur

Aplikasi dibagi menjadi 5 bagian utama:

1. Entry point: menjalankan aplikasi dan mengatur tema.
2. UI: menampilkan layar dan interaksi pengguna.
3. Service: logika bisnis tugas.
4. Persistence: baca/tulis data ke file.
5. Model: struktur data tugas dan format serialisasi.

Alur data singkat:

`Main -> FrameManajerTugas (UI) -> LayananTugas (service) -> PengelolaFile (data.txt)`

## 1) Main.java
Lokasi: `src/com/todoapp/Main.java`

Peran utama:

- Titik awal aplikasi (`public static void main`).
- Inisialisasi tema FlatLaf (light theme).
- Set global style Swing lewat `UIManager` (warna, radius, focus color).
- Membuat objek `PengelolaFile` dan `LayananTugas`.
- Menjalankan `FrameManajerTugas` di Event Dispatch Thread.

Kapan file ini diubah:

- Saat ingin ganti tema global.
- Saat ingin ubah warna default semua komponen.
- Saat ingin ganti lokasi file data default.

## 2) FrameManajerTugas.java
Lokasi: `src/com/todoapp/ui/FrameManajerTugas.java`

Peran utama:

- Menyusun tampilan utama aplikasi model dashboard (header atas, sidebar kiri, konten kanan, action bar bawah).
- Menangani event tombol: Tambah, Edit, Hapus, Ubah Status, Muat Ulang, dan Filter.
- Menampilkan tabel tugas dari service.
- Menyediakan pencarian teks (`fieldCari`) dan filter kombinasi.
- Menampilkan pengingat tenggat (H-3, H-1, Hari H).
- Menampilkan dialog tambah/edit tugas.
- Menampilkan sapaan dinamis di header: `Hai, Selamat [Pagi/Siang/Sore/Malam]` sesuai jam laptop.

Konstanta penting yang sering diubah:

- `LEBAR_WINDOW`, `TINGGI_WINDOW`: ukuran window.
- `LEBAR_PANEL_KIRI`: lebar sidebar kiri.
- `TINGGI_TOMBOL_SIDEBAR`: tinggi tombol sidebar kiri.
- `TINGGI_TOMBOL_FILTER`: tinggi tombol Filter.
- `JARAK_ANTAR_TOMBOL_ATAS`: jarak vertikal antar tombol sidebar.

Method penting:

- `muatTugas(...)`: isi ulang data tabel.
- `terapkanFilter()`: filter gabungan status/prioritas + pencarian judul/deskripsi.
- `buatSapaanWaktu()`: menentukan sapaan berdasarkan jam saat ini.
- `tampilkanDialogTugas(...)`: form tambah/edit.

## 3) LayananTugas.java
Lokasi: `src/com/todoapp/service/LayananTugas.java`

Peran utama:

- Menjadi pusat logika bisnis tugas.
- Menyimpan daftar tugas di memori selama aplikasi berjalan.
- Menentukan ID berikutnya untuk tugas baru.
- Menyediakan operasi CRUD untuk UI.
- Menyediakan fungsi filter/sort.
- Menghasilkan daftar pengingat berdasarkan selisih hari.

Method penting:

- `tambahTugas(...)`
- `ubahTugas(...)`
- `hapusTugas(...)`
- `ubahStatus(...)`
- `ambilSemuaTugas()`
- `saringBerdasarkanStatus(...)`
- `saringBerdasarkanPrioritas(...)`
- `ambilPengingat()`

Catatan:

- Setiap perubahan data akan memanggil `simpan()` agar tersimpan ke file.

## 4) PengelolaFile.java
Lokasi: `src/com/todoapp/persistence/PengelolaFile.java`

Peran utama:

- Membaca data tugas dari file (`muatTugas`).
- Menulis ulang semua data tugas ke file (`simpanTugas`).
- Menjadi lapisan persistence sederhana berbasis file teks.

Catatan format data:

- Satu baris file mewakili satu tugas.
- Parsing/format baris didelegasikan ke class `Tugas`.

Kapan file ini diubah:

- Jika ingin ganti storage (misalnya JSON, database, atau append strategy).

## 5) Tugas.java
Lokasi: `src/com/todoapp/model/Tugas.java`

Peran utama:

- Model data untuk entitas tugas.
- Menyimpan field: `id`, `judul`, `deskripsi`, `tenggat`, `prioritas`, `selesai`.
- Mendefinisikan enum `Prioritas`.
- Menyediakan serialisasi/deserialisasi ke format baris data file.

Method penting:

- `keBarisData()`: ubah objek jadi string baris file.
- `dariBarisData(...)`: parse string baris jadi objek.
- `enkode(...)` dan `dekode(...)`: Base64 untuk aman terhadap karakter pemisah.

## Relasi Antar File

- `Main` membuat `LayananTugas` dengan dependency `PengelolaFile`.
- `FrameManajerTugas` hanya berinteraksi ke `LayananTugas`.
- `LayananTugas` memakai `Tugas` sebagai model data.
- `PengelolaFile` dan `Tugas` bekerja sama untuk format simpan/muat data.

## Checklist Cepat Saat Mengubah Fitur

1. Jika tambah field baru di tugas:
   - Update `Tugas` (field, constructor, getter/setter, serialisasi).
   - Update `FrameManajerTugas` (form input, tabel, pencarian/filter jika perlu).
2. Jika ubah logika filter/sort:
   - Update `LayananTugas`.
   - Pastikan `FrameManajerTugas.terapkanFilter()` tetap sinkron.
3. Jika ubah warna/tema:
   - Utama di `Main` (`UIManager`) dan konstanta warna di `FrameManajerTugas`.
4. Jika ubah sapaan waktu di header:
   - Update `FrameManajerTugas.buatSapaanWaktu()`.
5. Jika ubah mekanisme simpan data:
   - Update `PengelolaFile` dan pastikan format di `Tugas` ikut sesuai.

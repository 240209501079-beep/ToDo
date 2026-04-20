# Penjelasan File Java - ToDo App

Dokumen ini menjelaskan fungsi file Java setelah refactor agar kode lebih terstruktur dan mudah dirawat.

## Gambaran Arsitektur

Aplikasi dibagi menjadi beberapa lapisan:

1. Entry point: menjalankan aplikasi.
2. Theme setup: pengaturan tema global Swing/FlatLaf.
3. UI screen: layout dan event utama.
4. UI config/helper: konstanta dan helper tampilan terpusat.
5. Service: logika bisnis tugas.
6. Persistence + model: simpan/muat data dan struktur data.

Alur data singkat:

`Main -> TemaAplikasi + FrameManajerTugas -> LayananTugas -> PengelolaFile -> data.txt`

## 1) Main.java
Lokasi: `src/com/todoapp/Main.java`

Peran utama:

- Titik awal aplikasi (`public static void main`).
- Memanggil `TemaAplikasi.terapkanTemaDefault()`.
- Membuat `PengelolaFile` dan `LayananTugas`.
- Menjalankan `FrameManajerTugas` di Event Dispatch Thread.

## 2) TemaAplikasi.java
Lokasi: `src/com/todoapp/TemaAplikasi.java`

Peran utama:

- Menjadi pusat konfigurasi tema global FlatLaf + `UIManager`.
- Menjaga `Main` tetap ringkas.

## 3) FrameManajerTugas.java
Lokasi: `src/com/todoapp/ui/FrameManajerTugas.java`

Peran utama:

- Menyusun dashboard utama (header, sidebar, konten, action bar).
- Menangani event CRUD dan filter.
- Menampilkan tabel, pengingat, dan dialog tambah/edit.

Catatan:

- Konstanta UI dipusatkan ke `KonfigurasiUi`.
- Styling tombol dibantu `PembantuUi`.
- Sapaan waktu diambil dari `WaktuSapaan`.

## 4) KonfigurasiUi.java
Lokasi: `src/com/todoapp/ui/KonfigurasiUi.java`

Peran utama:

- Menyimpan konstanta warna, ukuran, dan formatter UI.
- Menjadi titik ubah cepat untuk tema dan layout.

## 5) PembantuUi.java
Lokasi: `src/com/todoapp/ui/PembantuUi.java`

Peran utama:

- Menyediakan helper gaya tombol sidebar (termasuk hover).
- Menyediakan helper ukuran tombol.
- Menyediakan style FlatLaf untuk tombol Filter.

## 6) WaktuSapaan.java
Lokasi: `src/com/todoapp/ui/WaktuSapaan.java`

Peran utama:

- Menentukan sapaan waktu: Pagi/Siang/Sore/Malam.

## 7) LayananTugas.java
Lokasi: `src/com/todoapp/service/LayananTugas.java`

Peran utama:

- Pusat logika bisnis (CRUD, sort, filter, pengingat).
- Menyimpan daftar tugas dalam memori saat aplikasi berjalan.
- Menyimpan perubahan ke file lewat `simpan()`.

## 8) PengelolaFile.java
Lokasi: `src/com/todoapp/persistence/PengelolaFile.java`

Peran utama:

- Membaca data dari file (`muatTugas`).
- Menulis ulang data ke file (`simpanTugas`).

## 9) Tugas.java
Lokasi: `src/com/todoapp/model/Tugas.java`

Peran utama:

- Model data tugas.
- Serialisasi/deserialisasi baris data.
- Enkode/dekode Base64 untuk judul/deskripsi.

## Checklist Cepat Saat Mengubah Fitur

1. Ubah tema global: `TemaAplikasi`.
2. Ubah warna/ukuran UI: `KonfigurasiUi`.
3. Ubah perilaku tampilan tombol: `PembantuUi`.
4. Ubah sapaan waktu: `WaktuSapaan`.
5. Ubah logika CRUD/filter/pengingat: `LayananTugas`.
6. Ubah format simpan data: `Tugas` + `PengelolaFile`.

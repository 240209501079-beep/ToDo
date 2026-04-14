# Alur Build JAR Sampai Build EXE (Launch4j)

Panduan ini mengikuti setup terbaru project, termasuk versi aplikasi terpusat.

Lokasi project:

D:\VisualSC\Java\ToDo\ToDo

## Ringkasan File yang Dipakai

1. app-version.properties
2. build-jar.ps1
3. siapkan-launch4j.ps1
4. launch4j-config.xml (hasil generate)

## 1. Atur Versi Aplikasi

Buka file app-version.properties lalu ubah nilainya.

Contoh:

```properties
app.version=1.0.0
```

Catatan:
1. Gunakan format angka bertitik, contoh 1.0.0 atau 1.2.3
2. Nilai ini akan dipakai untuk manifest JAR dan metadata EXE Launch4j

## 2. Build JAR

Jalankan:

```powershell
powershell -ExecutionPolicy Bypass -File build-jar.ps1
```

Yang dilakukan script ini:
1. Bersihkan folder bin
2. Compile source dengan classpath library
3. Ekstrak dependency dari folder lib ke bin (fat jar)
4. Buat ToDoApp.jar
5. Tambahkan Implementation-Version ke manifest JAR berdasarkan app-version.properties

## 3. Verifikasi dan Test JAR

Verifikasi file:

```powershell
dir ToDoApp.jar
```

Test jalan:

```powershell
java -jar ToDoApp.jar
```

Kalau aplikasi terbuka normal, lanjut ke langkah Launch4j.

## 4. Generate Konfigurasi Launch4j Otomatis

Jalankan:

```powershell
powershell -ExecutionPolicy Bypass -File siapkan-launch4j.ps1
```

Output script:
1. launch4j-config.xml
2. Metadata versi EXE terisi otomatis dari app-version.properties
3. Metadata produk otomatis:
	1. Product Name: ToDoTask
	2. Company Name: Kelompok 7
	3. File Description: Aplikasi ToDo Desktop

Contoh mapping versi:
1. app.version=1.0.0
2. fileVersion di Launch4j menjadi 1.0.0.0

## 5. Build EXE di Launch4j

1. Buka launch4j.exe
2. Open config: launch4j-config.xml
3. Cek field utama:
	1. jar: D:\VisualSC\Java\ToDo\ToDo\ToDoApp.jar
	2. outfile: D:\VisualSC\Java\ToDo\ToDo\ToDoTask.exe
	3. icon: D:\VisualSC\Java\ToDo\ToDo\icon.ico
	4. minVersion: 17
4. Klik Build wrapper

Hasilnya: ToDoTask.exe di folder project.

## 6. Test EXE

```powershell
.\ToDoTask.exe
```

## 7. Alur Rilis Berikutnya

Setiap mau rilis versi baru, ulangi urutan ini:
1. Ubah app.version di app-version.properties
2. Jalankan build-jar.ps1
3. Jalankan siapkan-launch4j.ps1
4. Buka launch4j-config.xml
5. Build wrapper

## Opsi Via VS Code Task

Selain lewat terminal, Anda bisa jalankan task berikut di VS Code:
1. Build JAR ToDo App
2. Generate Launch4j Config

## Catatan Penting

1. Launch4j tidak otomatis membaca versi dari project jika isi form manual.
2. Agar otomatis, selalu gunakan launch4j-config.xml yang di-generate script.
3. EXE Launch4j tetap butuh Java Runtime minimal 17 di komputer target, kecuali Anda bundling JRE.
4. Jalankan EXE dari folder yang sama dengan data.txt agar lokasi data konsisten.

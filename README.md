# Java To-Do GUI App

Aplikasi To-Do List berbasis GUI (Swing) dengan struktur package Java, fitur CRUD, tenggat, prioritas, filter, pengingat, dan simpan data ke file.

## Catatan Update
- Login sudah dihapus
- Aplikasi langsung masuk ke layar manajer tugas

## Fitur
- CRUD tugas
- Status selesai / belum
- Tenggat dengan format `yyyy-MM-dd`
- Prioritas (`TINGGI`, `SEDANG`, `RENDAH`)
- Filter berdasarkan status dan prioritas
- Pengingat otomatis untuk H-3, H-1, dan Hari H
- Persistensi data ke file `data.txt`

## Struktur Utama
- `src/com/todoapp/Main.java`
- `src/com/todoapp/model/Tugas.java`
- `src/com/todoapp/service/LayananTugas.java`
- `src/com/todoapp/persistence/PengelolaFile.java`
- `src/com/todoapp/ui/FrameManajerTugas.java`
- `app-version.properties`
- `build-jar.ps1`
- `siapkan-launch4j.ps1`

## Menjalankan dari VS Code Task
- Build: `Build ToDo App`
- Run: `Run ToDo App`
- Build JAR: `Build JAR ToDo App`
- Generate config Launch4j: `Generate Launch4j Config`

## Build dan Run Manual

Compile:

```powershell
if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin | Out-Null
javac -d bin -cp "lib/*" -sourcepath src src/com/todoapp/Main.java
```

Run:

```powershell
java -cp "bin;lib/*" com.todoapp.Main
```

## Build JAR

```powershell
powershell -ExecutionPolicy Bypass -File build-jar.ps1
```

Output:
- `ToDoApp.jar`

Test JAR:

```powershell
java -jar ToDoApp.jar
```

## Build EXE dengan Launch4j (Alur Otomatis)

1. Atur versi di `app-version.properties`
2. Jalankan `build-jar.ps1`
3. Jalankan `siapkan-launch4j.ps1`
4. Buka `launch4j-config.xml` di Launch4j
5. Klik Build wrapper

Output:
- `ToDoTask.exe`

Metadata EXE yang diisi otomatis dari script:
- Product Name: `ToDoTask`
- Company Name: `Kelompok 7`
- File Description: `Aplikasi ToDo Desktop`
- Version: dari `app-version.properties`

Catatan:
- EXE Launch4j tetap membutuhkan Java Runtime minimal 17 di komputer target, kecuali bundling JRE.
- Agar metadata versi EXE konsisten, selalu generate config dari script, jangan isi manual dari nol.

## Panduan Lengkap

Lihat file `CARA-BUAT-EXE.md` untuk alur detail dari versi aplikasi sampai build EXE.

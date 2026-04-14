# Alur Buat JAR Sampai Build EXE (Launch4j)

Panduan ini khusus untuk project ToDoApp di folder:

`D:\VisualSC\Java\ToDo\ToDo`

## 1. Build class Java ke folder bin

Jalankan dari terminal PowerShell:

```powershell
if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin | Out-Null
javac -d bin -sourcepath src src\com\todoapp\Main.java
```

Jika tidak ada error, lanjut ke langkah 2.

## 2. Buat file JAR executable

```powershell
jar cfe ToDoApp.jar com.todoapp.Main -C bin .
```

Arti singkat:
- `c`: create jar
- `f`: output ke file
- `e`: set entry point (main class)

## 3. Verifikasi JAR sudah jadi

```powershell
dir ToDoApp.jar
```

## 4. Test JAR sebelum dibungkus jadi EXE

```powershell
java -jar ToDoApp.jar
```

Kalau aplikasi terbuka normal, lanjut ke Launch4j.

## 5. Buka Launch4j

Download: https://sourceforge.net/projects/launch4j/

Lalu jalankan `launch4j.exe`.

## 6. Isi konfigurasi Launch4j

Di form Launch4j isi minimal seperti ini:

- Output file: `D:\VisualSC\Java\ToDo\ToDo\ToDoApp.exe`
- Jar: `D:\VisualSC\Java\ToDo\ToDo\ToDoApp.jar`
- Chdir (disarankan): `D:\VisualSC\Java\ToDo\ToDo`
- Min JRE version: `17`
- Icon (opsional): file `.ico`

Catatan:
- Main class tidak perlu diisi jika JAR sudah dibuat dengan `jar cfe`.

## 7. Build wrapper

Klik tombol **Build wrapper**.

Jika sukses, file `ToDoApp.exe` akan muncul di folder project.

## 8. Test EXE

Jalankan:

```powershell
.\ToDoApp.exe
```

atau double-click file `ToDoApp.exe`.

## 9. Simpan konfigurasi (opsional tapi disarankan)

Di Launch4j, simpan konfigurasi sebagai file XML agar build berikutnya tinggal buka config lalu klik build.

## Catatan penting

- EXE dari Launch4j tetap membutuhkan Java Runtime di komputer target (minimal versi 17), kecuali Anda bundling JRE.
- Karena app menyimpan data ke `data.txt`, sebaiknya jalankan EXE dari folder yang sama agar lokasi data konsisten.

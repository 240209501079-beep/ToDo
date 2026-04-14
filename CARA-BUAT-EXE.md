# Panduan Membuat .EXE dari Java App

## Status: ✓ JAR File Siap
File `ToDoApp.jar` sudah dibuat dan siap diubah menjadi .exe

---

## **PILIHAN 1: Launch4j (Paling Profesional)**

### Kelebihan:
- Hasilnya genuine .exe file
- Bisa set custom icon
- Support untuk bundling JRE (optional)

### Langkah-langkah:
1. Download dari: https://sourceforge.net/projects/launch4j/
2. Extract & jalankan `launch4j.exe`
3. File → New:
   ```
   Output file: ToDoApp.exe
   Jar: D:\VisualSC\Java\ToDo\ToDo\ToDoApp.jar
   Main class: com.todoapp.Main
   Min JRE version: 17
   Bundled JRE: (optional) Path ke JRE folder
   ```
4. Klik "Build wrapper" → DUN!

**Catatan:** Hasilnya masih butuh Java 17 di target computer (kecuali direcompress dengan JRE)

---

## **PILIHAN 2: Batch Script (Paling Gampang)**

File `run-todoapp.bat` sudah siap di folder project.
Tinggal double-click untuk jalankan.

**Kelebihan:** Instant, gampang  
**Kekurangan:** Terlihat seperti command prompt, bukan application

---

## **PILIHAN 3: GraalVM Native Image (Paling Advanced)**

Jika ingin truly native executable tanpa Java dependency:

```bash
# Install GraalVM SDK
# Kemudian:
native-image -cp ToDoApp.jar -H:Name=ToDoApp com.todoapp.Main
```

Hasilnya: `ToDoApp.exe` (standalone, ~100MB)

---

## **Rekomendasi untuk Anda:**
➡️ **Gunakan Launch4j** (Opsi 1) - paling balance antara kemudahan vs hasil profesional

## File yang sudah ada:
- ✓ `ToDoApp.jar` - JAR executable (16 KB)
- ✓ `run-todoapp.bat` - Batch runner alternative

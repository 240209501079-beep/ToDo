# Java To-Do GUI App

Aplikasi To-Do List berbasis GUI (Swing) dengan struktur package Java, fitur login, CRUD, deadline, prioritas, filter, reminder, dan simpan data ke file.

## Login Default
- Username: `admin` X
- Password: `12345` X

##Update
- Login Di hilangkan

## Fitur
- Login sederhana
- CRUD task
- Status selesai / belum
- Deadline (`yyyy-MM-dd`)
- Prioritas (`High`, `Medium`, `Low`)
- Filter task berdasarkan status atau prioritas
- Reminder otomatis untuk H-3, H-1, dan Hari H
- Persistensi data ke `data.txt`

## Struktur
- `src/com/todoapp/Main.java`
- `src/com/todoapp/model/Task.java`
- `src/com/todoapp/service/TaskService.java`
- `src/com/todoapp/persistence/FileHandler.java`
- `src/com/todoapp/ui/LoginFrame.java`
- `src/com/todoapp/ui/TaskManagerFrame.java`
- `data.txt`

## Compile & Run
```bash
javac -d out -sourcepath src src/com/todoapp/Main.java
java -cp out com.todoapp.Main
```

## Opsi Menjadi .exe (Direkomendasikan: Launch4j)
Kenapa Launch4j?
- Konfigurasi mudah untuk pemula
- Masih umum dipakai untuk membungkus `.jar` jadi `.exe`
- Bisa set icon, nama output, dan minimum JRE

Langkah singkat:
1. Compile semua class: `javac -d out -sourcepath src src/com/todoapp/Main.java`
2. Buat JAR: `jar cfe ToDoApp.jar com.todoapp.Main -C out . data.txt`
3. Buka Launch4j
4. Set:
   - Output file: `ToDoApp.exe`
   - Jar: `ToDoApp.jar`
   - Min JRE version: misalnya `17`
5. Build wrapper EXE

Catatan: EXE dari Launch4j tetap butuh Java Runtime di komputer target (kecuali Anda bundling JRE).

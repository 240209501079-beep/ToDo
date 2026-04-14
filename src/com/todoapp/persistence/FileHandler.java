package com.todoapp.persistence;

import com.todoapp.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private final Path dataPath;

    public FileHandler(String fileName) {
        this.dataPath = Paths.get(fileName);
    }

    public List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();

        if (!Files.exists(dataPath)) {
            return tasks;
        }

        try (BufferedReader reader = Files.newBufferedReader(dataPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                Task task = Task.fromDataLine(line);
                if (task != null) {
                    tasks.add(task);
                }
            }
        } catch (IOException ex) {
            System.out.println("Gagal membaca data: " + ex.getMessage());
        }

        return tasks;
    }

    public void saveTasks(List<Task> tasks) {
        try (BufferedWriter writer = Files.newBufferedWriter(dataPath)) {
            for (Task task : tasks) {
                writer.write(task.toDataLine());
                writer.newLine();
            }
        } catch (IOException ex) {
            System.out.println("Gagal menyimpan data: " + ex.getMessage());
        }
    }
}

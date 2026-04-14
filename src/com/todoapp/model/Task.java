package com.todoapp.model;

import java.time.LocalDate;
import java.util.Base64;

public class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate deadline;
    private Priority priority;
    private boolean completed;

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    public Task(int id, String title, String description, LocalDate deadline, Priority priority, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String toDataLine() {
        return id + "|" + encode(title) + "|" + encode(description) + "|" + deadline + "|" + priority + "|" + completed;
    }

    public static Task fromDataLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 6) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0]);
            String title = decode(parts[1]);
            String description = decode(parts[2]);
            LocalDate deadline = LocalDate.parse(parts[3]);
            Priority priority = Priority.valueOf(parts[4]);
            boolean completed = Boolean.parseBoolean(parts[5]);
            return new Task(id, title, description, deadline, priority, completed);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value));
    }
}

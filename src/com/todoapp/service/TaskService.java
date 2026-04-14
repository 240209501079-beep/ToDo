package com.todoapp.service;

import com.todoapp.model.Task;
import com.todoapp.persistence.FileHandler;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private final List<Task> tasks;
    private final FileHandler fileHandler;
    private int nextId;

    public TaskService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.tasks = new ArrayList<>(fileHandler.loadTasks());
        this.nextId = tasks.stream().map(Task::getId).max(Integer::compareTo).orElse(0) + 1;
    }

    public Task addTask(String title, String description, LocalDate deadline, Task.Priority priority) {
        Task task = new Task(nextId++, title, description, deadline, priority, false);
        tasks.add(task);
        save();
        return task;
    }

    public boolean updateTask(int id, String title, String description, LocalDate deadline, Task.Priority priority) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }

        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(deadline);
        task.setPriority(priority);
        save();
        return true;
    }

    public boolean deleteTask(int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean toggleStatus(int id) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }

        task.setCompleted(!task.isCompleted());
        save();
        return true;
    }

    public List<Task> getAllTasks() {
        return tasks.stream()
            .sorted(Comparator.comparing(Task::getDeadline).thenComparing(Task::getPriority))
            .collect(Collectors.toList());
    }

    public List<Task> filterByStatus(boolean completed) {
        return tasks.stream()
            .filter(t -> t.isCompleted() == completed)
            .sorted(Comparator.comparing(Task::getDeadline))
            .collect(Collectors.toList());
    }

    public List<Task> filterByPriority(Task.Priority priority) {
        return tasks.stream()
            .filter(t -> t.getPriority() == priority)
            .sorted(Comparator.comparing(Task::getDeadline))
            .collect(Collectors.toList());
    }

    public Task findById(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    public List<String> getReminders() {
        LocalDate today = LocalDate.now();
        List<String> reminders = new ArrayList<>();

        for (Task task : tasks) {
            if (task.isCompleted()) {
                continue;
            }

            long daysLeft = ChronoUnit.DAYS.between(today, task.getDeadline());
            if (daysLeft == 3 || daysLeft == 1 || daysLeft == 0) {
                String marker = daysLeft == 0 ? "Hari H" : "H-" + daysLeft;
                reminders.add(String.format("[%s] #%d %s (Deadline: %s)", marker, task.getId(), task.getTitle(), task.getDeadline()));
            }
        }

        return reminders;
    }

    private void save() {
        fileHandler.saveTasks(tasks);
    }
}

package com.todoapp;

import com.todoapp.persistence.FileHandler;
import com.todoapp.service.TaskService;
import com.todoapp.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FileHandler fileHandler = new FileHandler("data.txt");
        TaskService taskService = new TaskService(fileHandler);

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(taskService);
            loginFrame.setVisible(true);
        });
    }
}

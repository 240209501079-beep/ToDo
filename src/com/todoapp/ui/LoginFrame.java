package com.todoapp.ui;

import com.todoapp.service.TaskService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "12345";

    private final TaskService taskService;

    public LoginFrame(TaskService taskService) {
        this.taskService = taskService;
        setup();
    }

    private void setup() {
        setTitle("Login ToDoTask");
        setSize(360, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                dispose();
                SwingUtilities.invokeLater(() -> new TaskManagerFrame(taskService).setVisible(true));
            } else {
                JOptionPane.showMessageDialog(this, "Login gagal", "Info", JOptionPane.WARNING_MESSAGE);
            }
        });

        add(formPanel, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
    }
}

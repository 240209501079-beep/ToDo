package com.todoapp.ui;

import com.todoapp.model.Task;
import com.todoapp.service.TaskService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskManagerFrame extends JFrame {
    private final TaskService taskService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextArea reminderArea;
    private final JComboBox<String> filterCombo;

    public TaskManagerFrame(TaskService taskService) {
        this.taskService = taskService;

        setTitle("To-Do App GUI");
        setSize(920, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        reminderArea = new JTextArea(3, 30);
        reminderArea.setEditable(false);
        reminderArea.setLineWrap(true);
        reminderArea.setWrapStyleWord(true);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Reminder"));
        topPanel.add(new JScrollPane(reminderArea), BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new String[]{"ID", "Judul", "Deskripsi", "Deadline", "Priority", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Tambah");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Hapus");
        JButton toggleButton = new JButton("Toggle Status");
        JButton refreshButton = new JButton("Refresh");

        filterCombo = new JComboBox<>(new String[]{"All", "Completed", "Pending", "High", "Medium", "Low"});
        JButton filterButton = new JButton("Filter");

        addButton.addActionListener(e -> showTaskDialog(null));
        editButton.addActionListener(e -> editSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        toggleButton.addActionListener(e -> toggleSelectedTask());
        refreshButton.addActionListener(e -> loadTasks(taskService.getAllTasks()));
        filterButton.addActionListener(e -> applyFilter());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(toggleButton);
        actionPanel.add(refreshButton);
        actionPanel.add(new JLabel("Filter:"));
        actionPanel.add(filterCombo);
        actionPanel.add(filterButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        loadTasks(taskService.getAllTasks());
    }

    private void loadTasks(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            tableModel.addRow(new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDeadline(),
                task.getPriority(),
                task.isCompleted() ? "Selesai" : "Belum"
            });
        }
        updateReminder();
    }

    private void updateReminder() {
        List<String> reminders = taskService.getReminders();
        if (reminders.isEmpty()) {
            reminderArea.setText("Tidak ada reminder untuk H-3, H-1, atau Hari H.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (String reminder : reminders) {
            builder.append(reminder).append(System.lineSeparator());
        }
        reminderArea.setText(builder.toString());
    }

    private void applyFilter() {
        String option = String.valueOf(filterCombo.getSelectedItem());
        switch (option) {
            case "Completed":
                loadTasks(taskService.filterByStatus(true));
                break;
            case "Pending":
                loadTasks(taskService.filterByStatus(false));
                break;
            case "High":
                loadTasks(taskService.filterByPriority(Task.Priority.HIGH));
                break;
            case "Medium":
                loadTasks(taskService.filterByPriority(Task.Priority.MEDIUM));
                break;
            case "Low":
                loadTasks(taskService.filterByPriority(Task.Priority.LOW));
                break;
            default:
                loadTasks(taskService.getAllTasks());
        }
    }

    private Integer getSelectedTaskId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih task terlebih dahulu.");
            return null;
        }

        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void editSelectedTask() {
        Integer id = getSelectedTaskId();
        if (id == null) {
            return;
        }

        Task task = taskService.findById(id);
        if (task == null) {
            JOptionPane.showMessageDialog(this, "Task tidak ditemukan.");
            return;
        }

        showTaskDialog(task);
    }

    private void deleteSelectedTask() {
        Integer id = getSelectedTaskId();
        if (id == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this, "Hapus task ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION && taskService.deleteTask(id)) {
            loadTasks(taskService.getAllTasks());
        }
    }

    private void toggleSelectedTask() {
        Integer id = getSelectedTaskId();
        if (id == null) {
            return;
        }

        if (taskService.toggleStatus(id)) {
            loadTasks(taskService.getAllTasks());
        }
    }

    private void showTaskDialog(Task task) {
        JDialog dialog = new JDialog(this, task == null ? "Tambah Task" : "Edit Task", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField deadlineField = new JTextField();
        JComboBox<String> priorityField = new JComboBox<>(new String[]{"HIGH", "MEDIUM", "LOW"});

        if (task != null) {
            titleField.setText(task.getTitle());
            descField.setText(task.getDescription());
            deadlineField.setText(task.getDeadline().toString());
            priorityField.setSelectedItem(task.getPriority().name());
        }

        form.add(new JLabel("Judul"));
        form.add(titleField);
        form.add(new JLabel("Deskripsi"));
        form.add(descField);
        form.add(new JLabel("Deadline (yyyy-MM-dd)"));
        form.add(deadlineField);
        form.add(new JLabel("Priority"));
        form.add(priorityField);

        JButton saveButton = new JButton("Simpan");
        saveButton.setHorizontalAlignment(SwingConstants.CENTER);
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descField.getText().trim();
            String deadlineText = deadlineField.getText().trim();
            Task.Priority priority = Task.Priority.valueOf(String.valueOf(priorityField.getSelectedItem()));

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Judul wajib diisi.");
                return;
            }

            LocalDate deadline;
            try {
                deadline = LocalDate.parse(deadlineText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Format deadline harus yyyy-MM-dd.");
                return;
            }

            if (task == null) {
                taskService.addTask(title, description, deadline, priority);
            } else {
                taskService.updateTask(task.getId(), title, description, deadline, priority);
            }

            dialog.dispose();
            loadTasks(taskService.getAllTasks());
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(420, 280));
        dialog.setVisible(true);
    }
}

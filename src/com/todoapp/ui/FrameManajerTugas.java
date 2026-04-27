package com.todoapp.ui;

import com.todoapp.model.Tugas;
import com.todoapp.service.LayananTugas;
import com.todoapp.service.SessionManager;
import com.todoapp.persistence.FirebaseStorage;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class FrameManajerTugas extends JFrame {
    private static final String STATUS_DONE = "✓ Selesai";
    private static final String STATUS_TODO = "○ Belum";

    private final LayananTugas layananTugas;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel labelReminder;
    private final JTextField searchField;
    private final JComboBox<String> filterCombo;

    public FrameManajerTugas(LayananTugas layananTugas) {
        this.layananTugas = layananTugas;

        initWindow();

        // Components initialization
        searchField = new JTextField();
        filterCombo = new JComboBox<>(new String[] { "Semua", "Selesai", "Belum", "Tinggi", "Sedang", "Rendah" });
        labelReminder = new JLabel("Memuat pengingat...");
        labelReminder.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);

        tableModel = createTableModel();
        table = createTable();

        // Layout Assembly
        JPanel sidebar = createSidebar();
        JPanel mainContent = createMainContent();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, mainContent);
        splitPane.setDividerLocation(KonfigurasiUi.LEBAR_PANEL_KIRI);
        splitPane.setDividerSize(1);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);

        add(createHeader(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);

        refreshData();
    }

    private void initWindow() {
        setTitle("Task Manager Pro");
        setSize(KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW);
        setMinimumSize(new Dimension(KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(KonfigurasiUi.WARNA_BG_KONTEN);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new String[] { "ID", "Judul", "Deskripsi", "Tenggat", "Prioritas", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable() {
        JTable t = new JTable(tableModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(40);
        t.setShowGrid(true);
        t.setGridColor(KonfigurasiUi.WARNA_GARIS);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(KonfigurasiUi.WARNA_BIRU_MUDA);
        t.setSelectionForeground(KonfigurasiUi.WARNA_HITAM);

        applyCustomRenderers(t);
        return t;
    }

    private void applyCustomRenderers(JTable t) {
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                String status = String.valueOf(table.getModel().getValueAt(row, 5));
                boolean isDone = STATUS_DONE.equals(status);

                // Strike-through for done tasks
                if (isDone && (col == 1 || col == 2)) {
                    Map<TextAttribute, Object> attributes = new HashMap<>(getFont().getAttributes());
                    attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                    setFont(getFont().deriveFont(attributes));
                    setForeground(KonfigurasiUi.WARNA_ABU_TEKS);
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                    if (!isSelected)
                        setForeground(KonfigurasiUi.WARNA_HITAM);
                }

                // Priority coloring
                if (col == 4 && !isSelected) {
                    String prio = String.valueOf(value);
                    if ("TINGGI".equals(prio))
                        setForeground(KonfigurasiUi.WARNA_MERAH);
                    else if ("SEDANG".equals(prio))
                        setForeground(KonfigurasiUi.WARNA_ORANYE);
                    else if ("RENDAH".equals(prio))
                        setForeground(KonfigurasiUi.WARNA_HIJAU);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(KonfigurasiUi.WARNA_BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, KonfigurasiUi.WARNA_GARIS));
        p.setPreferredSize(new Dimension(0, 60));

        JLabel title = new JLabel("  Task Dashboard");
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(KonfigurasiUi.WARNA_HITAM);

        JLabel greeting = new JLabel("Hai, Selamat " + WaktuSapaan.buatSapaanWaktu() + "  ");
        greeting.setFont(new Font("Inter", Font.PLAIN, 14));
        greeting.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);

        p.add(title, BorderLayout.WEST);
        p.add(greeting, BorderLayout.EAST);
        return p;
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(KonfigurasiUi.WARNA_BG_SIDEBAR);
        p.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel menuLabel = new JLabel("MENU UTAMA");
        menuLabel.setForeground(KonfigurasiUi.WARNA_TULISAN_SIDEMENU);
        menuLabel.setFont(new Font("Inter", Font.BOLD, 11));

        JButton btnAll = new JButton(" 📋 Semua Tugas");
        JButton btnProgress = new JButton(" 🕒 Sedang Berjalan");
        JButton btnDone = new JButton(" ✅ Sudah Selesai");
        JButton btnLogout = new JButton(" 🚪 Keluar");

        for (JButton b : new JButton[] { btnAll, btnProgress, btnDone, btnLogout }) {
            PembantuUi.aturGayaTombolSidebar(b);
        }

        btnAll.addActionListener(e -> {
            filterCombo.setSelectedItem("Semua");
            applyFilter();
        });
        btnProgress.addActionListener(e -> {
            filterCombo.setSelectedItem("Belum");
            applyFilter();
        });
        btnDone.addActionListener(e -> {
            filterCombo.setSelectedItem("Selesai");
            applyFilter();
        });
        btnLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION) == 0) {
                new SessionManager().hapusSesi();
                new LoginFrame(new SessionManager()).setVisible(true);
                dispose();
            }
        });

        p.add(menuLabel);
        p.add(Box.createVerticalStrut(15));
        p.add(btnAll);
        p.add(Box.createVerticalStrut(5));
        p.add(btnProgress);
        p.add(Box.createVerticalStrut(5));
        p.add(btnDone);
        p.add(Box.createVerticalGlue());
        p.add(btnLogout);

        return p;
    }

    private JPanel createMainContent() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.setBackground(KonfigurasiUi.WARNA_BG_KONTEN);

        // Search & Filter Bar
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);

        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.putClientProperty("JTextField.placeholderText", "Cari tugas...");

        filterCombo.setPreferredSize(new Dimension(120, 35));

        JButton btnRefresh = new JButton("🔄");
        btnRefresh.setPreferredSize(new Dimension(40, 35));
        btnRefresh.addActionListener(e -> refreshData());

        gbc.weightx = 1.0;
        topBar.add(searchField, gbc);
        gbc.weightx = 0.0;
        topBar.add(filterCombo, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        topBar.add(btnRefresh, gbc);

        // Reminder Panel
        JPanel reminderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reminderPanel.setBackground(KonfigurasiUi.WARNA_BIRU_MUDA);
        reminderPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        reminderPanel.add(new JLabel("💡 "));
        reminderPanel.add(labelReminder);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(reminderPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        p.add(topBar, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);

        searchField.addActionListener(e -> applyFilter());
        filterCombo.addActionListener(e -> applyFilter());

        return p;
    }

    private JPanel createActionBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        p.setBackground(KonfigurasiUi.WARNA_BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, KonfigurasiUi.WARNA_GARIS));

        JButton btnAdd = new JButton("+ Tambah Tugas");
        btnAdd.setBackground(KonfigurasiUi.WARNA_BIRU);
        btnAdd.setForeground(Color.WHITE);

        JButton btnEdit = new JButton("📝 Edit");
        JButton btnDelete = new JButton("🗑️ Hapus");
        JButton btnStatus = new JButton("✅ Tandai Selesai");

        btnAdd.addActionListener(e -> showTaskDialog(null));
        btnEdit.addActionListener(e -> editSelectedTask());
        btnDelete.addActionListener(e -> deleteSelectedTask());
        btnStatus.addActionListener(e -> toggleTaskStatus());

        p.add(btnStatus);
        p.add(new JSeparator(JSeparator.VERTICAL));
        p.add(btnEdit);
        p.add(btnDelete);
        p.add(Box.createHorizontalStrut(10));
        p.add(btnAdd);

        return p;
    }

    private void refreshData() {
        loadTasks(layananTugas.ambilSemuaTugas());
    }

    private void loadTasks(List<Tugas> tasks) {
        tableModel.setRowCount(0);
        for (Tugas t : tasks) {
            tableModel.addRow(new Object[] {
                    t.getId(),
                    t.getJudul(),
                    t.getDeskripsi(),
                    t.getTenggat().format(KonfigurasiUi.FORMAT_TENGGAT),
                    t.getPrioritas(),
                    t.isSelesai() ? STATUS_DONE : STATUS_TODO
            });
        }
        updateReminderLabel();
    }

    private void updateReminderLabel() {
        List<String> reminders = layananTugas.ambilPengingat();
        if (reminders.isEmpty()) {
            labelReminder.setText("Semua tugas terkendali. Tidak ada tenggat dekat.");
        } else {
            labelReminder.setText(String.join(" | ", reminders));
        }
    }

    private void applyFilter() {
        String filter = (String) filterCombo.getSelectedItem();
        List<Tugas> base;

        if ("Selesai".equals(filter))
            base = layananTugas.saringBerdasarkanStatus(true);
        else if ("Belum".equals(filter))
            base = layananTugas.saringBerdasarkanStatus(false);
        else if ("Tinggi".equals(filter))
            base = layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.TINGGI);
        else if ("Sedang".equals(filter))
            base = layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.SEDANG);
        else if ("Rendah".equals(filter))
            base = layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.RENDAH);
        else
            base = layananTugas.ambilSemuaTugas();

        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadTasks(base);
        } else {
            List<Tugas> filtered = new ArrayList<>();
            for (Tugas t : base) {
                if (t.getJudul().toLowerCase().contains(query) || t.getDeskripsi().toLowerCase().contains(query)) {
                    filtered.add(t);
                }
            }
            loadTasks(filtered);
        }
    }

    private Integer getSelectedTaskId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih tugas terlebih dahulu.", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void editSelectedTask() {
        Integer id = getSelectedTaskId();
        if (id != null)
            showTaskDialog(layananTugas.cariBerdasarkanId(id));
    }

    private void deleteSelectedTask() {
        Integer id = getSelectedTaskId();
        if (id != null && JOptionPane.showConfirmDialog(this, "Hapus tugas ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == 0) {
            if (layananTugas.hapusTugas(id))
                refreshData();
        }
    }

    private void toggleTaskStatus() {
        Integer id = getSelectedTaskId();
        if (id != null && layananTugas.ubahStatus(id))
            refreshData();
    }

    private void showTaskDialog(Tugas task) {
        JDialog d = new JDialog(this, task == null ? "Tambah Tugas" : "Edit Tugas", true);
        d.setSize(450, 400);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;

        JTextField fJudul = new JTextField();
        JTextArea fDesc = new JTextArea(4, 20);
        fDesc.setLineWrap(true);
        fDesc.setWrapStyleWord(true);
        JSpinner fDate = new JSpinner(new SpinnerDateModel());
        fDate.setEditor(new JSpinner.DateEditor(fDate, "yyyy-MM-dd HH:mm"));
        JComboBox<Tugas.Prioritas> fPrio = new JComboBox<>(Tugas.Prioritas.values());

        if (task != null) {
            fJudul.setText(task.getJudul());
            fDesc.setText(task.getDeskripsi());
            fDate.setValue(Date.from(task.getTenggat().atZone(ZoneId.systemDefault()).toInstant()));
            fPrio.setSelectedItem(task.getPrioritas());
        }

        gbc.gridy = 0;
        p.add(new JLabel("Judul"), gbc);
        gbc.gridy = 1;
        p.add(fJudul, gbc);
        gbc.gridy = 2;
        p.add(new JLabel("Deskripsi"), gbc);
        gbc.gridy = 3;
        p.add(new JScrollPane(fDesc), gbc);
        gbc.gridy = 4;
        p.add(new JLabel("Tenggat"), gbc);
        gbc.gridy = 5;
        p.add(fDate, gbc);
        gbc.gridy = 6;
        p.add(new JLabel("Prioritas"), gbc);
        gbc.gridy = 7;
        p.add(fPrio, gbc);

        JButton btnSave = new JButton("Simpan");
        btnSave.addActionListener(e -> {
            String j = fJudul.getText().trim();
            if (j.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Judul tidak boleh kosong.");
                return;
            }

            LocalDateTime dt = LocalDateTime.ofInstant(((Date) fDate.getValue()).toInstant(), ZoneId.systemDefault());
            Tugas.Prioritas pr = (Tugas.Prioritas) fPrio.getSelectedItem();

            if (task == null)
                layananTugas.tambahTugas(j, fDesc.getText(), dt, pr);
            else
                layananTugas.ubahTugas(task.getId(), j, fDesc.getText(), dt, pr);

            d.dispose();
            refreshData();
        });

        d.add(p, BorderLayout.CENTER);
        d.add(btnSave, BorderLayout.SOUTH);
        d.setVisible(true);
    }
}

package com.todoapp.ui;

import com.todoapp.model.Tugas;
import com.todoapp.service.LayananTugas;
import com.todoapp.service.SessionManager;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class FrameManajerTugas extends JFrame {
    private static final String STATUS_DONE = "✓ Selesai";
    private static final String STATUS_TODO = "○ Belum";

    private final LayananTugas layananTugas;
    private final SessionManager sessionManager;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel labelReminder;
    private final JTextField searchField;
    private final JComboBox<String> filterCombo;

    private TrayIcon trayIcon;
    private final java.util.Timer reminderTimer = new java.util.Timer(true);

    private void log(String msg) {
        String time = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date());
        System.out.println("[" + time + "] " + msg);
    }

    public FrameManajerTugas(LayananTugas layananTugas, SessionManager sessionManager) {
        log("DEBUG [F1]: Memulai Konstruktor Frame...");
        this.layananTugas = layananTugas;
        this.sessionManager = sessionManager;

        initWindow();
        PembantuUi.aturIkonWindow(this); // Paksa muat ikon lagi
        log("DEBUG [F2]: initWindow Selesai.");
        initSystemTray();
        log("DEBUG [F3]: initSystemTray Selesai.");
        startBackgroundReminder();
        startAlarmChecker();
        log("DEBUG [F4]: startBackgroundReminder Selesai.");
        searchField = new JTextField();
        filterCombo = new JComboBox<>(new String[] { "Semua", "Selesai", "Belum", "Tinggi", "Sedang", "Rendah" });
        labelReminder = new JLabel("Memuat pengingat...");
        labelReminder.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);

        tableModel = createTableModel();
        table = createTable();
        System.out.println("DEBUG [F5]: Table created.");

        // Layout Assembly
        JPanel sidebar = createSidebar();
        JPanel mainContent = createMainContent();
        log("DEBUG [F6]: UI Panels created.");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, mainContent);
        splitPane.setDividerLocation(KonfigurasiUi.LEBAR_PANEL_KIRI);
        splitPane.setDividerSize(1);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);

        add(createHeader(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);
        log("DEBUG [F7]: Layout assembled.");
    }

    private void initWindow() {
        setTitle("To-Do List");
        PembantuUi.aturIkonWindow(this);
        setSize(KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW);
        setMinimumSize(new Dimension(KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW));
        setLocationRelativeTo(null);

        // Ganti perilaku tombol X agar sembunyi ke Tray, bukan mati total
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                setVisible(false);
            }
        });

        setLayout(new BorderLayout());
        getContentPane().setBackground(KonfigurasiUi.WARNA_BG_KONTEN);
    }

    private void initSystemTray() {
        try {
            if (!SystemTray.isSupported()) {
                log("System Tray tidak didukung di sistem ini.");
                return;
            }

            SystemTray tray = SystemTray.getSystemTray();

            // Load Ikon lewat PembantuUi (cache)
            PembantuUi.aturIkonWindow(this);
            Image image = getIconImage();

            if (image == null) {
                image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            }

            PopupMenu popup = new PopupMenu();
            MenuItem openItem = new MenuItem("Buka Aplikasi");
            MenuItem exitItem = new MenuItem("Matikan Total");

            openItem.addActionListener(e -> SwingUtilities.invokeLater(() -> setVisible(true)));
            exitItem.addActionListener(e -> System.exit(0));

            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon = new TrayIcon(image, "ToDo Task Manager", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> SwingUtilities.invokeLater(() -> setVisible(true)));

            tray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("Gagal menginisialisasi System Tray: " + e.getMessage());
        }
    }

    private void startBackgroundReminder() {
        reminderTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                log("DEBUG [Reminder]: Memeriksa tugas mendesak dari cloud...");
                // Ambil data terbaru dari cloud, bukan dari cache
                layananTugas.ambilDataDariCloud();
                List<String> pengingat = layananTugas.ambilPengingat();

                if (!pengingat.isEmpty()) {
                    log("DEBUG [Reminder]: Ditemukan " + pengingat.size() + " tugas mendesak. Menampilkan notifikasi...");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(pengingat.size(), 3); i++) {
                        sb.append("• ").append(pengingat.get(i)).append("<br>");
                    }

                    // Gunakan custom toast saja (tidak ada duplikat dari JDK)
                    NotifikasiToast.tampilkan(
                        "Pengingat Tugas! (" + pengingat.size() + " tugas)",
                        sb.toString()
                    );
                } else {
                    log("DEBUG [Reminder]: Tidak ada tugas mendesak saat ini.");
                }
            }
        }, 10000, 30 * 60 * 1000); // Mulai 10 detik, ulang tiap 30 menit
    }

    /** Cek setiap menit apakah ada tugas yang jatuh tempo TEPAT di menit ini (alarm). */
    private void startAlarmChecker() {
        new java.util.Timer(true).scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                for (com.todoapp.model.Tugas t : layananTugas.ambilSemuaTugas()) {
                    if (t.isSelesai()) continue;
                    java.time.LocalDateTime tenggat = t.getTenggat();
                    // Cek apakah tahun, bulan, hari, jam, MENIT sama persis
                    if (tenggat.getYear()       == now.getYear()   &&
                        tenggat.getMonthValue() == now.getMonthValue() &&
                        tenggat.getDayOfMonth() == now.getDayOfMonth() &&
                        tenggat.getHour()       == now.getHour()   &&
                        tenggat.getMinute()     == now.getMinute()) {
                        log("DEBUG [Alarm]: Tenggat tepat waktu untuk tugas: " + t.getJudul());
                        // Gunakan custom toast saja (tidak ada duplikat dari JDK)
                        NotifikasiToast.tampilkan(
                            "⏰ ALARM! Tenggat Tiba!",
                            "Tugas <b>" + t.getJudul() + "</b> sudah waktunya sekarang!"
                        );
                    }
                }
            }
        }, 0, 60 * 1000); // Cek setiap 1 menit
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

        String user = sessionManager.getUserEmail() != null ? sessionManager.getUserEmail() : "Pengguna";
        if (user.contains("@")) {
            user = user.split("@")[0]; // Ambil nama depan dari email
        }

        JLabel greeting = new JLabel("Halo, " + user + " | Selamat " + WaktuSapaan.buatSapaanWaktu() + "  ");
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
        JButton btnLogout = new JButton(" 🔓 Logout Akun");
        JButton btnExit = new JButton(" ❌ Tutup Aplikasi");

        for (JButton b : new JButton[] { btnAll, btnProgress, btnDone, btnLogout, btnExit }) {
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
            log("DEBUG [Logout]: Menekan tombol logout...");
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin Logout (pindah akun)?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                log("DEBUG [Logout]: Menghapus sesi lokal...");
                new SessionManager().hapusSesi();
                SwingUtilities.invokeLater(() -> {
                    log("DEBUG [Logout]: Kembali ke layar Login.");
                    new LoginFrame(new SessionManager()).setVisible(true);
                    this.dispose();
                });
            }
        });

        JButton btnHide = createSidebarButton("Tutup (Latar Belakang)", "📥");
        btnHide.addActionListener(e -> setVisible(false));

        // Tombol untuk matikan total
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Matikan aplikasi? Pengingat tidak akan aktif.",
                    "Konfirmasi Matikan",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                tampilkanLoadingMatikan();
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
        p.add(Box.createVerticalStrut(10));
        p.add(btnHide);
        p.add(Box.createVerticalStrut(10));
        p.add(btnExit);

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
        btnRefresh.addActionListener(e -> {
            System.out.println("DEBUG [Refresh-Manual]: Tombol refresh ditekan.");
            refreshData();
        });

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

    /** Ambil data dari cloud dan update tampilan. Harus dipanggil dari background thread! */
    private void reloadUiDariCloud() {
        log("DEBUG [Refresh]: Mengambil data terbaru dari Firestore...");
        List<Tugas> dataBaru = layananTugas.ambilDataDariCloud();
        SwingUtilities.invokeLater(() -> {
            loadTasks(dataBaru);
            labelReminder.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);
            log("DEBUG [Refresh]: Tampilan tabel diperbarui.");
        });
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            labelReminder.setText("⌛ Menyinkronkan dengan Cloud...");
            labelReminder.setForeground(KonfigurasiUi.WARNA_BIRU);
        });
        new Thread(this::reloadUiDariCloud, "RefreshThread").start();
    }

    private JButton createSidebarButton(String text, String icon) {
        JButton btn = new JButton("<html><div style='text-align:left; width:150px;'>&nbsp;" + icon + "&nbsp;&nbsp;"
                + text + "</div></html>");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);
        btn.setFont(new Font("Inter", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(56, 189, 248, 40)); // Biru transparan untuk hover
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);
            }
        });
        return btn;
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
            if (layananTugas.hapusTugas(id)) {
                loadTasks(layananTugas.ambilSemuaTugas());
                labelReminder.setText("✅ Tugas dihapus.");
                labelReminder.setForeground(new Color(34, 197, 94));
            }
        }
    }

    private void toggleTaskStatus() {
        Integer id = getSelectedTaskId();
        if (id == null) return;
        if (layananTugas.ubahStatus(id)) {
            loadTasks(layananTugas.ambilSemuaTugas());
            labelReminder.setText("✅ Status diperbarui.");
            labelReminder.setForeground(new Color(34, 197, 94));
        }
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

            // 1. Tulis ke cache lokal (instant, di EDT)
            if (task == null) {
                layananTugas.tambahTugas(j, fDesc.getText(), dt, pr);
            } else {
                layananTugas.ubahTugas(task.getId(), j, fDesc.getText(), dt, pr);
            }

            // 2. Tutup dialog
            d.dispose();

            // 3. Refresh tabel INSTAN dari cache lokal
            loadTasks(layananTugas.ambilSemuaTugas());
            labelReminder.setText("✅ Tersimpan!");
            labelReminder.setForeground(new Color(34, 197, 94));

            // 4. Cloud sync di background - TANPA update ulang UI agar tidak timpa data baru
            new Thread(() -> {
                log("DEBUG [CloudSync]: Memverifikasi data ke Firestore...");
                layananTugas.ambilDataDariCloud(); // hanya sync cache internal
            }, "CloudSyncThread").start();
        });

        d.add(p, BorderLayout.CENTER);
        d.add(btnSave, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void tampilkanLoadingMatikan() {
        JDialog d = new JDialog(this, "Shutdown", true);
        d.setUndecorated(true);
        d.setSize(400, 150);
        d.setLocationRelativeTo(this);
        d.setBackground(new Color(0, 0, 0, 0));

        JPanel p = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42)); // Dark Slate
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(56, 189, 248, 100)); // Glow border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lIcon = new JLabel("🛑");
        lIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel lText = new JLabel(
                "<html><b>Mematikan Aplikasi...</b><br><font color='#94a3b8'>Membersihkan sesi dan pengingat latar belakang.</font></html>");
        lText.setForeground(Color.WHITE);
        lText.setFont(new Font("Inter", Font.PLAIN, 14));

        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        pb.setPreferredSize(new Dimension(300, 4));
        pb.setBackground(new Color(30, 41, 59));
        pb.setForeground(new Color(56, 189, 248));
        pb.setBorder(null);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 5));
        center.setOpaque(false);
        center.add(lText);
        center.add(pb);

        p.add(lIcon, BorderLayout.WEST);
        p.add(center, BorderLayout.CENTER);

        d.add(p);

        // Timer untuk menutup aplikasi setelah jeda singkat (agar user melihat
        // loading-nya)
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                System.out.println("DEBUG: Shutdown Selesai. Keluar.");
                System.exit(0);
            }
        }, 1500); // Tampilkan loading selama 1.5 detik

        d.setVisible(true);
    }
}

package com.todoapp.ui;

import com.todoapp.model.Tugas;
import com.todoapp.service.LayananTugas;

import javax.swing.JSplitPane;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameManajerTugas extends JFrame {
    // PETA UBAH CEPAT (UI):
    // 1) Ukuran window: KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW
    // 2) Lebar sidebar kiri: KonfigurasiUi.LEBAR_PANEL_KIRI
    // 3) Tinggi tombol: KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR, KonfigurasiUi.TINGGI_TOMBOL_FILTER
    // 4) Jarak tombol sidebar: KonfigurasiUi.JARAK_ANTAR_TOMBOL_ATAS
    // 5) Tinggi baris tabel: tabel.setRowHeight(...)
    private static final String LABEL_STATUS_SELESAI = "[Selesai]";
    private static final String LABEL_STATUS_BELUM = "[Belum]";

    private final LayananTugas layananTugas;
    private final DefaultTableModel modelTabel;
    private final JTable tabel;
    private final JTextArea areaPengingat;
    private final JTextField fieldCari;
    private final JComboBox<String> comboFilter;

    public FrameManajerTugas(LayananTugas layananTugas) {
        this.layananTugas = layananTugas;

        setTitle("To-Do App");
        setSize(KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW);
        setMinimumSize(new Dimension(KonfigurasiUi.LEBAR_WINDOW, KonfigurasiUi.TINGGI_WINDOW));
        setResizable(false); // kunci ukuran window supaya isi tidak saling numpuk
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(KonfigurasiUi.WARNA_BG_KONTEN);

        areaPengingat = new JTextArea(3, 30);
        areaPengingat.setEditable(false);
        areaPengingat.setLineWrap(true);
        areaPengingat.setWrapStyleWord(true);

        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panelAtas.add(new JScrollPane(areaPengingat), BorderLayout.CENTER);
        panelAtas.setBackground(KonfigurasiUi.WARNA_BG_KONTEN);

        modelTabel = new DefaultTableModel(
                new String[] { "ID", "Judul", "Deskripsi", "Tenggat", "Prioritas", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(modelTabel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(28); // Ubah angka ini kalau tinggi baris tabel ingin disesuaikan.
        tabel.setFillsViewportHeight(true);
        tabel.setBackground(KonfigurasiUi.WARNA_PUTIH);
        tabel.setSelectionBackground(KonfigurasiUi.WARNA_BIRU_MUDA);
        tabel.setSelectionForeground(Color.BLACK);
        pasangRendererStatusSelesai();

        // Header atas: nama dashboard dan sapaan dinamis sesuai jam laptop.
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        panelHeader.setBackground(KonfigurasiUi.WARNA_BG_HEADER);
        JLabel labelSapaan = new JLabel("Hai, Selamat " + WaktuSapaan.buatSapaanWaktu());
        labelSapaan.setFont(labelSapaan.getFont().deriveFont((float) KonfigurasiUi.UKURAN_FONT_SAPAAN));
        panelHeader.add(labelSapaan, BorderLayout.EAST);

        JButton tombolTambah = new JButton("+ Tambah Tugas");
        JButton tombolEdit = new JButton("/ Edit");
        JButton tombolHapus = new JButton("X Hapus");
        JButton tombolUbahStatus = new JButton("Tandai Selesai");
        JButton tombolMuatUlang = new JButton("Muat Ulang");

        comboFilter = new JComboBox<>(new String[] { "Semua", "Selesai", "Belum", "Tinggi", "Sedang", "Rendah" });
        fieldCari = new JTextField();
        JButton tombolFilter = new JButton("Filter");

        tombolTambah.setBackground(KonfigurasiUi.WARNA_HIJAU);
        tombolTambah.setForeground(Color.WHITE);
        tombolFilter.setBackground(KonfigurasiUi.WARNA_BIRU);
        tombolFilter.setForeground(Color.WHITE);
        tombolFilter.putClientProperty(
            "FlatLaf.style",
            PembantuUi.gayaTombolFilter());
        tombolFilter.setFocusPainted(false);
        tombolFilter.setContentAreaFilled(true);
        tombolFilter.setOpaque(true);
        tombolFilter.setBorderPainted(true);
        tombolTambah.addActionListener(e -> tampilkanDialogTugas(null));
        tombolEdit.addActionListener(e -> editTugasTerpilih());
        tombolHapus.addActionListener(e -> hapusTugasTerpilih());
        tombolUbahStatus.addActionListener(e -> ubahStatusTugasTerpilih());
        tombolMuatUlang.addActionListener(e -> muatTugas(layananTugas.ambilSemuaTugas()));
        tombolFilter.addActionListener(e -> terapkanFilter());
        fieldCari.addActionListener(e -> terapkanFilter());

        // Sidebar kiri: shortcut filter status + menu umum.
        JPanel panelSidebar = new JPanel();
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));
        panelSidebar.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));
        panelSidebar.setPreferredSize(new Dimension(KonfigurasiUi.LEBAR_PANEL_KIRI, 0));
        panelSidebar.setBackground(KonfigurasiUi.WARNA_BG_SIDEBAR);

        JButton tombolSemua = new JButton("Semua Tugas");
        JButton tombolOnProgress = new JButton("On-Progress");
        JButton tombolCompleted = new JButton("Completed");
        JButton tombolSettings = new JButton("Settings");
        JButton tombolLogout = new JButton("Keluar");

        PembantuUi.aturGayaTombolSidebar(tombolSemua);
        PembantuUi.aturGayaTombolSidebar(tombolOnProgress);
        PembantuUi.aturGayaTombolSidebar(tombolCompleted);
        PembantuUi.aturGayaTombolSidebar(tombolSettings);
        PembantuUi.aturGayaTombolSidebar(tombolLogout);

        tombolSemua.addActionListener(e -> {
            comboFilter.setSelectedItem("Semua");
            terapkanFilter();
        });
        tombolOnProgress.addActionListener(e -> {
            comboFilter.setSelectedItem("Belum");
            terapkanFilter();
        });
        tombolCompleted.addActionListener(e -> {
            comboFilter.setSelectedItem("Selesai");
            terapkanFilter();
        });
        tombolSettings.addActionListener(e -> JOptionPane.showMessageDialog(this, "Belum ada halaman pengaturan."));
        tombolLogout.addActionListener(e -> {
            int jawaban = JOptionPane.showConfirmDialog(this, "Keluar dari aplikasi?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (jawaban == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        JLabel labelSideMenu = new JLabel("Side Menu");
        labelSideMenu.setForeground(KonfigurasiUi.WARNA_TULISAN_SIDEMENU);
        panelSidebar.add(labelSideMenu);
        panelSidebar.add(Box.createVerticalStrut(12));
        panelSidebar.add(tombolSemua);
        panelSidebar.add(Box.createVerticalStrut(KonfigurasiUi.JARAK_ANTAR_TOMBOL_ATAS));
        panelSidebar.add(tombolOnProgress);
        panelSidebar.add(Box.createVerticalStrut(KonfigurasiUi.JARAK_ANTAR_TOMBOL_ATAS));
        panelSidebar.add(tombolCompleted);
        panelSidebar.add(Box.createVerticalStrut(20));
        panelSidebar.add(tombolSettings);
        panelSidebar.add(Box.createVerticalStrut(KonfigurasiUi.JARAK_ANTAR_TOMBOL_ATAS));
        panelSidebar.add(tombolLogout);
        panelSidebar.add(Box.createVerticalGlue());

        // Konten tengah: pencarian + filter + panel pengingat + tabel tugas.
        JPanel panelKonten = new JPanel(new BorderLayout(8, 8));
        panelKonten.setBorder(BorderFactory.createEmptyBorder());
        panelKonten.setBackground(KonfigurasiUi.WARNA_BG_KONTEN);

        JPanel panelCariFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panelCariFilter.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        panelCariFilter.setBackground(KonfigurasiUi.WARNA_BG_KONTEN);
        JLabel labelCari = new JLabel("Cari Tugas:");
        labelCari.setForeground(KonfigurasiUi.WARNA_TULISAN_LABEL_FILTER);
        panelCariFilter.add(labelCari);
        fieldCari.setPreferredSize(new Dimension(220, 30));
        panelCariFilter.add(fieldCari);
        JLabel labelFilter = new JLabel("Filter:");
        labelFilter.setForeground(KonfigurasiUi.WARNA_TULISAN_LABEL_FILTER);
        panelCariFilter.add(labelFilter);
        comboFilter.setPreferredSize(new Dimension(130, 30));
        panelCariFilter.add(comboFilter);
        PembantuUi.aturUkuranTombol(tombolFilter, KonfigurasiUi.TINGGI_TOMBOL_FILTER, KonfigurasiUi.LEBAR_TOMBOL_FILTER);
        panelCariFilter.add(tombolFilter);

        JPanel panelAtasKonten = new JPanel(new BorderLayout(8, 8));
        panelAtasKonten.setBorder(BorderFactory.createEmptyBorder());
        panelAtasKonten.setBackground(KonfigurasiUi.WARNA_BG_KONTEN);
        panelAtasKonten.add(panelCariFilter, BorderLayout.NORTH);
        panelAtasKonten.add(panelAtas, BorderLayout.CENTER);

        JScrollPane scrollTabel = new JScrollPane(tabel);
        scrollTabel.setBorder(BorderFactory.createEmptyBorder());
        panelKonten.add(panelAtasKonten, BorderLayout.NORTH);
        panelKonten.add(scrollTabel, BorderLayout.CENTER);

        // Split utama: sidebar kiri dan konten kanan.
        JSplitPane splitUtama = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelSidebar, panelKonten);
        splitUtama.setDividerLocation(KonfigurasiUi.LEBAR_PANEL_KIRI);
        splitUtama.setResizeWeight(0.0);
        splitUtama.setOneTouchExpandable(false);
        splitUtama.setEnabled(false);
        splitUtama.setDividerSize(KonfigurasiUi.LEBAR_PEMBATAS_TENGAH);
        splitUtama.setBackground(KonfigurasiUi.WARNA_BG_KONTEN);
        splitUtama.setBorder(BorderFactory.createEmptyBorder());

        // Action bar bawah: tombol aksi data.
        JPanel panelAksiBawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelAksiBawah.setBackground(KonfigurasiUi.WARNA_BG_HEADER);
        panelAksiBawah.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        panelAksiBawah.add(tombolTambah);
        panelAksiBawah.add(tombolEdit);
        panelAksiBawah.add(tombolHapus);
        panelAksiBawah.add(tombolUbahStatus);
        panelAksiBawah.add(tombolMuatUlang);

        add(panelHeader, BorderLayout.NORTH);
        add(splitUtama, BorderLayout.CENTER);
        add(panelAksiBawah, BorderLayout.SOUTH);

        muatTugas(layananTugas.ambilSemuaTugas());
    }

    private void muatTugas(List<Tugas> daftarTugas) {
        // Method ini sumber data tabel. Kalau mau tambah kolom, ubah di sini + modelTabel.
        modelTabel.setRowCount(0);
        for (Tugas tugas : daftarTugas) {
            modelTabel.addRow(new Object[] {
                    tugas.getId(),
                    tugas.getJudul(),
                    tugas.getDeskripsi(),
                    tugas.getTenggat().format(KonfigurasiUi.FORMAT_TENGGAT),
                    tugas.getPrioritas(),
                    tugas.isSelesai() ? LABEL_STATUS_SELESAI : LABEL_STATUS_BELUM
            });
        }
        perbaruiPengingat();
    }

    private void perbaruiPengingat() {
        List<String> daftarPengingat = layananTugas.ambilPengingat();
        if (daftarPengingat.isEmpty()) {
            areaPengingat.setText("Tidak ada pengingat untuk H-3, H-1, atau Hari H.");
            return;
        }

        StringBuilder pembuatTeks = new StringBuilder();
        for (String pengingat : daftarPengingat) {
            pembuatTeks.append(pengingat).append(System.lineSeparator());
        }
        areaPengingat.setText(pembuatTeks.toString());
    }

    private void terapkanFilter() {
        // Filter gabungan: status/prioritas dari ComboBox + kata kunci judul/deskripsi.
        String opsi = String.valueOf(comboFilter.getSelectedItem());
        List<Tugas> hasilDasar;

        switch (opsi) {
            case "Selesai":
                hasilDasar = layananTugas.saringBerdasarkanStatus(true);
                break;
            case "Belum":
                hasilDasar = layananTugas.saringBerdasarkanStatus(false);
                break;
            case "Tinggi":
                hasilDasar = layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.TINGGI);
                break;
            case "Sedang":
                hasilDasar = layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.SEDANG);
                break;
            case "Rendah":
                hasilDasar = layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.RENDAH);
                break;
            default:
                hasilDasar = layananTugas.ambilSemuaTugas();
        }

        String kataKunci = fieldCari.getText().trim().toLowerCase();
        if (kataKunci.isEmpty()) {
            muatTugas(hasilDasar);
            return;
        }

        List<Tugas> hasilAkhir = new ArrayList<>();
        for (Tugas tugas : hasilDasar) {
            String judul = tugas.getJudul() == null ? "" : tugas.getJudul().toLowerCase();
            String deskripsi = tugas.getDeskripsi() == null ? "" : tugas.getDeskripsi().toLowerCase();
            if (judul.contains(kataKunci) || deskripsi.contains(kataKunci)) {
                hasilAkhir.add(tugas);
            }
        }

        muatTugas(hasilAkhir);
    }

    private Integer ambilIdTugasTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih tugas terlebih dahulu.");
            return null;
        }

        return (Integer) modelTabel.getValueAt(baris, 0);
    }

    private void editTugasTerpilih() {
        Integer id = ambilIdTugasTerpilih();
        if (id == null) {
            return;
        }

        Tugas tugas = layananTugas.cariBerdasarkanId(id);
        if (tugas == null) {
            JOptionPane.showMessageDialog(this, "Tugas tidak ditemukan.");
            return;
        }

        tampilkanDialogTugas(tugas);
    }

    private void hapusTugasTerpilih() {
        Integer id = ambilIdTugasTerpilih();
        if (id == null) {
            return;
        }

        int jawaban = JOptionPane.showConfirmDialog(this, "Hapus tugas ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (jawaban == JOptionPane.YES_OPTION && layananTugas.hapusTugas(id)) {
            muatTugas(layananTugas.ambilSemuaTugas());
        }
    }

    private void ubahStatusTugasTerpilih() {
        Integer id = ambilIdTugasTerpilih();
        if (id == null) {
            return;
        }

        if (layananTugas.ubahStatus(id)) {
            muatTugas(layananTugas.ambilSemuaTugas());
        }
    }

    private void tampilkanDialogTugas(Tugas tugas) {
        // Dialog ini dipakai untuk mode tambah (tugas == null) dan edit (tugas != null).
        JDialog dialog = new JDialog(this, tugas == null ? "Tambah Tugas" : "Edit Tugas", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField fieldJudul = new JTextField();
        JTextArea fieldDeskripsi = new JTextArea(5, 20);
        fieldDeskripsi.setLineWrap(true);
        fieldDeskripsi.setWrapStyleWord(true);
        JScrollPane scrollDeskripsi = new JScrollPane(fieldDeskripsi);
        scrollDeskripsi.setPreferredSize(new Dimension(0, 110));
        SpinnerDateModel modelTenggat = new SpinnerDateModel();
        JSpinner spinnerTenggat = new JSpinner(modelTenggat);
        spinnerTenggat.setEditor(new JSpinner.DateEditor(spinnerTenggat, "yyyy-MM-dd HH:mm"));
        spinnerTenggat.setValue(new Date());
        JComboBox<String> fieldPrioritas = new JComboBox<>(new String[] { "TINGGI", "SEDANG", "RENDAH" });

        if (tugas != null) {
            fieldJudul.putClientProperty("JTextField.placeholderText", "Masukkan judul tugas...");
            fieldJudul.setText(tugas.getJudul());
            fieldDeskripsi.setText(tugas.getDeskripsi());
            Date tanggalLama = Date.from(tugas.getTenggat().atZone(ZoneId.systemDefault()).toInstant());
            spinnerTenggat.setValue(tanggalLama);
            fieldPrioritas.setSelectedItem(tugas.getPrioritas().name());
        }

        form.add(new JLabel("Judul"));
        form.add(fieldJudul);
        form.add(new JLabel("Deskripsi"));
        form.add(scrollDeskripsi);
        form.add(new JLabel("Tenggat (Tanggal & Jam)"));
        form.add(spinnerTenggat);
        form.add(new JLabel("Prioritas"));
        form.add(fieldPrioritas);

        JButton tombolSimpan = new JButton("Simpan");
        tombolSimpan.setHorizontalAlignment(SwingConstants.CENTER);
        tombolSimpan.addActionListener(e -> {
            String judul = fieldJudul.getText().trim();
            String deskripsi = fieldDeskripsi.getText().trim();
            Tugas.Prioritas prioritas = Tugas.Prioritas.valueOf(String.valueOf(fieldPrioritas.getSelectedItem()));

            if (judul.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Judul wajib diisi.");
                return;
            }

            Date tanggalDipilih = (Date) spinnerTenggat.getValue();
            LocalDateTime tenggat = LocalDateTime.ofInstant(tanggalDipilih.toInstant(), ZoneId.systemDefault());

            if (tugas == null) {
                layananTugas.tambahTugas(judul, deskripsi, tenggat, prioritas);
            } else {
                layananTugas.ubahTugas(tugas.getId(), judul, deskripsi, tenggat, prioritas);
            }

            dialog.dispose();
            muatTugas(layananTugas.ambilSemuaTugas());
        });

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTombol.add(tombolSimpan);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(panelTombol, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(460, 380));
        dialog.setVisible(true);
    }

    private void pasangRendererStatusSelesai() {
        // Jika status selesai, judul/deskripsi dibuat tercoret agar progres lebih jelas.
        final int kolomStatus = 5;
        final int kolomJudul = 1;
        final int kolomDeskripsi = 2;

        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Object nilaiStatus = table.getModel().getValueAt(row, kolomStatus);
                String status = String.valueOf(nilaiStatus);
                boolean selesai = LABEL_STATUS_SELESAI.equals(status) || "[✓]".equals(status);

                java.awt.Font fontNormal = table.getFont();
                setFont(fontNormal);

                if (selesai && (column == kolomJudul || column == kolomDeskripsi)) {
                    Map<TextAttribute, Object> atribut = new HashMap<>(fontNormal.getAttributes());
                    atribut.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                    setFont(fontNormal.deriveFont(atribut));
                }

                if (!isSelected) {
                    setForeground(selesai ? new Color(120, 120, 120) : Color.BLACK);
                }

                return this;
            }
        });
    }

}

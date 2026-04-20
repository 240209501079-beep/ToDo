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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FrameManajerTugas extends JFrame {
    // PETA UBAH CEPAT (UI):
    // 1) Ukuran window: LEBAR_WINDOW, TINGGI_WINDOW
    // 2) Lebar sidebar kiri: LEBAR_PANEL_KIRI
    // 3) Tinggi tombol: TINGGI_TOMBOL_SIDEBAR, TINGGI_TOMBOL_FILTER
    // 4) Jarak tombol sidebar: JARAK_ANTAR_TOMBOL_ATAS
    // 5) Tinggi baris tabel: tabel.setRowHeight(...)
    private static final Color WARNA_PUTIH = Color.WHITE;
    private static final Color WARNA_BIRU = new Color(38, 128, 235);
    private static final Color WARNA_BIRU_MUDA = new Color(233, 244, 255);
    private static final Color WARNA_GARIS = new Color(210, 215, 223);
    // Ubah angka di sini kalau mau resize tampilan utama.
    private static final int LEBAR_WINDOW = 1080;
    private static final int TINGGI_WINDOW = 720;
    // Ubah ini kalau mau lebar sidebar kiri.
    private static final int LEBAR_PANEL_KIRI = 360;
    // Ubah ini kalau mau tombol sidebar lebih tinggi/rendah.
    private static final int TINGGI_TOMBOL_SIDEBAR = 50;
    // Ubah ini kalau mau tombol filter punya tinggi berbeda dari tombol utama.
    private static final int TINGGI_TOMBOL_FILTER = 30;
    // Ubah ini untuk jarak vertikal antar tombol di panel kiri bagian atas.
    private static final int JARAK_ANTAR_TOMBOL_ATAS = 8;
    private static final DateTimeFormatter FORMAT_TENGGAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LayananTugas layananTugas;
    private final DefaultTableModel modelTabel;
    private final JTable tabel;
    private final JTextArea areaPengingat;
    private final JTextField fieldCari;
    private final JComboBox<String> comboFilter;

    public FrameManajerTugas(LayananTugas layananTugas) {
        this.layananTugas = layananTugas;

        setTitle("To-Do App");
        setSize(LEBAR_WINDOW, TINGGI_WINDOW);
        setMinimumSize(new Dimension(LEBAR_WINDOW, TINGGI_WINDOW));
        setResizable(false); // kunci ukuran window supaya isi tidak saling numpuk
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(WARNA_PUTIH);

        areaPengingat = new JTextArea(3, 30);
        areaPengingat.setEditable(false);
        areaPengingat.setLineWrap(true);
        areaPengingat.setWrapStyleWord(true);

        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.setBorder(BorderFactory.createTitledBorder("Pengingat"));
        panelAtas.add(new JScrollPane(areaPengingat), BorderLayout.CENTER);
        panelAtas.setBackground(WARNA_PUTIH);

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
        tabel.setBackground(WARNA_PUTIH);
        tabel.setSelectionBackground(WARNA_BIRU_MUDA);
        tabel.setSelectionForeground(Color.BLACK);

        // Header atas: nama dashboard dan sapaan dinamis sesuai jam laptop.
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_GARIS),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        panelHeader.setBackground(WARNA_PUTIH);
        panelHeader.add(new JLabel("TaskMaster Dashboard"), BorderLayout.WEST);
        panelHeader.add(new JLabel("Hai, Selamat " + buatSapaanWaktu()), BorderLayout.EAST);

        JButton tombolTambah = new JButton("+ Tambah Tugas");
        JButton tombolEdit = new JButton("/ Edit");
        JButton tombolHapus = new JButton("X Hapus");
        JButton tombolUbahStatus = new JButton("Tandai Selesai");
        JButton tombolMuatUlang = new JButton("Muat Ulang");

        comboFilter = new JComboBox<>(new String[] { "Semua", "Selesai", "Belum", "Tinggi", "Sedang", "Rendah" });
        fieldCari = new JTextField();
        JButton tombolFilter = new JButton("Filter");

        tombolTambah.setBackground(WARNA_BIRU);
        tombolTambah.setForeground(Color.WHITE);
        tombolFilter.setBackground(WARNA_BIRU);
        tombolFilter.setForeground(Color.WHITE);
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
        panelSidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_GARIS),
            BorderFactory.createEmptyBorder(15, 12, 15, 12)));
        panelSidebar.setPreferredSize(new Dimension(LEBAR_PANEL_KIRI, 0));
        panelSidebar.setBackground(WARNA_PUTIH);

        JButton tombolSemua = new JButton("Semua Tugas");
        JButton tombolOnProgress = new JButton("On-Progress");
        JButton tombolCompleted = new JButton("Completed");
        JButton tombolSettings = new JButton("Settings");
        JButton tombolLogout = new JButton("Logout");

        aturUkuranTombolSidebar(tombolSemua);
        aturUkuranTombolSidebar(tombolOnProgress);
        aturUkuranTombolSidebar(tombolCompleted);
        aturUkuranTombolSidebar(tombolSettings);
        aturUkuranTombolSidebar(tombolLogout);

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

        panelSidebar.add(new JLabel("Side Menu"));
        panelSidebar.add(Box.createVerticalStrut(12));
        panelSidebar.add(tombolSemua);
        panelSidebar.add(Box.createVerticalStrut(JARAK_ANTAR_TOMBOL_ATAS));
        panelSidebar.add(tombolOnProgress);
        panelSidebar.add(Box.createVerticalStrut(JARAK_ANTAR_TOMBOL_ATAS));
        panelSidebar.add(tombolCompleted);
        panelSidebar.add(Box.createVerticalStrut(20));
        panelSidebar.add(tombolSettings);
        panelSidebar.add(Box.createVerticalStrut(JARAK_ANTAR_TOMBOL_ATAS));
        panelSidebar.add(tombolLogout);
        panelSidebar.add(Box.createVerticalGlue());

        // Konten tengah: pencarian + filter + panel pengingat + tabel tugas.
        JPanel panelKonten = new JPanel(new BorderLayout(8, 8));
        panelKonten.setBorder(BorderFactory.createLineBorder(WARNA_GARIS));
        panelKonten.setBackground(WARNA_PUTIH);

        JPanel panelCariFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panelCariFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_GARIS),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        panelCariFilter.setBackground(WARNA_PUTIH);
        panelCariFilter.add(new JLabel("Cari Tugas:"));
        fieldCari.setPreferredSize(new Dimension(220, 30));
        panelCariFilter.add(fieldCari);
        panelCariFilter.add(new JLabel("Filter:"));
        comboFilter.setPreferredSize(new Dimension(130, 30));
        panelCariFilter.add(comboFilter);
        aturUkuranTombolCustom(tombolFilter, TINGGI_TOMBOL_FILTER);
        panelCariFilter.add(tombolFilter);

        JPanel panelAtasKonten = new JPanel(new BorderLayout(8, 8));
        panelAtasKonten.setBorder(BorderFactory.createLineBorder(WARNA_GARIS));
        panelAtasKonten.setBackground(WARNA_PUTIH);
        panelAtasKonten.add(panelCariFilter, BorderLayout.NORTH);
        panelAtasKonten.add(panelAtas, BorderLayout.CENTER);

        JScrollPane scrollTabel = new JScrollPane(tabel);
        scrollTabel.setBorder(BorderFactory.createLineBorder(WARNA_GARIS));
        panelKonten.add(panelAtasKonten, BorderLayout.NORTH);
        panelKonten.add(scrollTabel, BorderLayout.CENTER);

        // Split utama: sidebar kiri dan konten kanan.
        JSplitPane splitUtama = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelSidebar, panelKonten);
        splitUtama.setDividerLocation(LEBAR_PANEL_KIRI);
        splitUtama.setResizeWeight(0.0);
        splitUtama.setOneTouchExpandable(false);
        splitUtama.setEnabled(false);
        splitUtama.setDividerSize(2);

        // Action bar bawah: tombol aksi data.
        JPanel panelAksiBawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelAksiBawah.setBackground(WARNA_PUTIH);
        panelAksiBawah.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_GARIS),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
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
                    tugas.getTenggat().format(FORMAT_TENGGAT),
                    tugas.getPrioritas(),
                    tugas.isSelesai() ? "Selesai" : "Belum"
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

    private String buatSapaanWaktu() {
        int jam = LocalTime.now().getHour();
        if (jam < 5) {
            return "Malam";
        }
        if (jam < 11) {
            return "Pagi";
        }
        if (jam < 15) {
            return "Siang";
        }
        if (jam < 18) {
            return "Sore";
        }
        return "Malam";
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
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField fieldJudul = new JTextField();
        JTextField fieldDeskripsi = new JTextField();
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
        form.add(fieldDeskripsi);
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
        dialog.setMinimumSize(new Dimension(420, 280));
        dialog.setVisible(true);
    }

    private void aturUkuranTombolSidebar(JButton tombol) {
        aturUkuranTombolCustom(tombol, TINGGI_TOMBOL_SIDEBAR);
    }

    private void aturUkuranTombolCustom(JButton tombol, int tinggi) {
        int lebarDefault = tombol.getPreferredSize().width;
        tombol.setMinimumSize(new Dimension(lebarDefault, tinggi));
        tombol.setPreferredSize(new Dimension(lebarDefault, tinggi));
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, tinggi));
    }
}

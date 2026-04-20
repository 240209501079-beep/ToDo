package com.todoapp.ui;

import com.todoapp.model.Tugas;
import com.todoapp.service.LayananTugas;

import javax.swing.JSplitPane;
import java.awt.Color;
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

public class FrameManajerTugas extends JFrame {
    private static final Color WARNA_PUTIH = Color.WHITE;
    private static final Color WARNA_BIRU = new Color(38, 128, 235);
    private static final Color WARNA_BIRU_MUDA = new Color(233, 244, 255);

    private final LayananTugas layananTugas;
    private final DefaultTableModel modelTabel;
    private final JTable tabel;
    private final JTextArea areaPengingat;
    private final JComboBox<String> comboFilter;
    // Komponen panel detail (kolom kanan)
    private final JLabel labelDetailJudul;
    private final JLabel labelDetailTenggat;
    private final JLabel labelDetailPrioritas;
    private final JLabel labelDetailStatus;
    private final JTextArea areaDetailDeskripsi;

    public FrameManajerTugas(LayananTugas layananTugas) {
        this.layananTugas = layananTugas;

        setTitle("To-Do App");
        setSize(1080, 720);
        setMinimumSize(new Dimension(1080, 720));
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
        tabel.setRowHeight(28);
        tabel.setFillsViewportHeight(true);
        tabel.setBackground(WARNA_PUTIH);
        tabel.setSelectionBackground(WARNA_BIRU_MUDA);
        tabel.setSelectionForeground(Color.BLACK);

        JPanel panelAksi = new JPanel(new BorderLayout(0, 14));
        JButton tombolTambah = new JButton("Tambah");
        JButton tombolEdit = new JButton("Edit");
        JButton tombolHapus = new JButton("Hapus");
        JButton tombolUbahStatus = new JButton("Ubah Status");
        JButton tombolMuatUlang = new JButton("Muat Ulang");

        comboFilter = new JComboBox<>(new String[] { "Semua", "Selesai", "Belum", "Tinggi", "Sedang", "Rendah" });
        JButton tombolFilter = new JButton("Filter");

        tombolTambah.putClientProperty("JButton.buttonType", "roundRect");
        tombolTambah.setBackground(WARNA_BIRU);
        tombolTambah.setForeground(Color.WHITE);
        tombolEdit.setBackground(WARNA_PUTIH);
        tombolEdit.setForeground(new Color(50, 50, 50));
        tombolHapus.setBackground(WARNA_PUTIH);
        tombolHapus.setForeground(new Color(50, 50, 50));
        tombolUbahStatus.setBackground(WARNA_PUTIH);
        tombolUbahStatus.setForeground(new Color(50, 50, 50));
        tombolMuatUlang.setBackground(WARNA_PUTIH);
        tombolMuatUlang.setForeground(new Color(50, 50, 50));
        tombolFilter.setBackground(WARNA_BIRU);
        tombolFilter.setForeground(Color.WHITE);
        tombolTambah.addActionListener(e -> tampilkanDialogTugas(null));
        tombolEdit.addActionListener(e -> editTugasTerpilih());
        tombolHapus.addActionListener(e -> hapusTugasTerpilih());
        tombolUbahStatus.addActionListener(e -> ubahStatusTugasTerpilih());
        tombolMuatUlang.addActionListener(e -> muatTugas(layananTugas.ambilSemuaTugas()));
        tombolFilter.addActionListener(e -> terapkanFilter());

        panelAksi.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelAksi.setPreferredSize(new Dimension(360, 0)); // panel kiri dibuat sepertiga lebar window
        panelAksi.setBackground(WARNA_PUTIH);

        // Area atas: tombol utama ditumpuk vertikal
        JPanel panelTombolUtama = new JPanel();
        panelTombolUtama.setLayout(new BoxLayout(panelTombolUtama, BoxLayout.Y_AXIS));
        panelTombolUtama.setBackground(WARNA_PUTIH);

        // Samakan ukuran agar tiap tombol terasa seperti menu sidebar
        Dimension ukuranTombol = new Dimension(Integer.MAX_VALUE, 34);
        tombolTambah.setMaximumSize(ukuranTombol);
        tombolEdit.setMaximumSize(ukuranTombol);
        tombolHapus.setMaximumSize(ukuranTombol);
        tombolUbahStatus.setMaximumSize(ukuranTombol);
        tombolMuatUlang.setMaximumSize(ukuranTombol);

        panelTombolUtama.add(tombolTambah);
        panelTombolUtama.add(tombolEdit);
        panelTombolUtama.add(tombolHapus);
        panelTombolUtama.add(tombolUbahStatus);
        panelTombolUtama.add(tombolMuatUlang);

        // Area bawah: filter terpisah supaya lebih rapi
        JPanel panelFilter = new JPanel();
        panelFilter.setLayout(new BoxLayout(panelFilter, BoxLayout.Y_AXIS));
        panelFilter.setBackground(WARNA_PUTIH);

        JLabel labelFilter = new JLabel("Filter");
        labelFilter.setForeground(new Color(50, 50, 50));
        comboFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        comboFilter.setForeground(new Color(50, 50, 50));
        tombolFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        panelFilter.add(labelFilter);
        panelFilter.add(comboFilter);
        panelFilter.add(tombolFilter);

        panelAksi.add(panelTombolUtama, BorderLayout.NORTH);
        panelAksi.add(panelFilter, BorderLayout.SOUTH);

        // Panel tengah: pengingat + tabel (daftar tugas)
        JPanel panelDaftar = new JPanel(new BorderLayout(8, 8));
        panelDaftar.add(panelAtas, BorderLayout.NORTH);
        panelDaftar.add(new JScrollPane(tabel), BorderLayout.CENTER);
        panelDaftar.setBackground(WARNA_PUTIH);

        // Panel kanan: detail tugas terpilih (meniru panel detail Microsoft To Do)
        JPanel panelDetail = new JPanel(new BorderLayout(10, 10));
        panelDetail.setBorder(BorderFactory.createTitledBorder("Detail Tugas"));
        panelDetail.setBackground(WARNA_PUTIH);

        JPanel panelInfoDetail = new JPanel();
        panelInfoDetail.setLayout(new BoxLayout(panelInfoDetail, BoxLayout.Y_AXIS));
        panelInfoDetail.setBackground(WARNA_PUTIH);

        labelDetailJudul = new JLabel("Judul: -");
        labelDetailTenggat = new JLabel("Tenggat: -");
        labelDetailPrioritas = new JLabel("Prioritas: -");
        labelDetailStatus = new JLabel("Status: -");
        labelDetailJudul.setForeground(new Color(50, 50, 50));
        labelDetailTenggat.setForeground(new Color(50, 50, 50));
        labelDetailPrioritas.setForeground(new Color(50, 50, 50));
        labelDetailStatus.setForeground(new Color(50, 50, 50));

        panelInfoDetail.add(labelDetailJudul);
        panelInfoDetail.add(labelDetailTenggat);
        panelInfoDetail.add(labelDetailPrioritas);
        panelInfoDetail.add(labelDetailStatus);

        areaDetailDeskripsi = new JTextArea(8, 20);
        areaDetailDeskripsi.setEditable(false);
        areaDetailDeskripsi.setLineWrap(true);
        areaDetailDeskripsi.setWrapStyleWord(true);
        areaDetailDeskripsi.setBorder(BorderFactory.createTitledBorder("Catatan / Deskripsi"));
        areaDetailDeskripsi.setBackground(WARNA_PUTIH);

        panelDetail.add(panelInfoDetail, BorderLayout.NORTH);
        panelDetail.add(new JScrollPane(areaDetailDeskripsi), BorderLayout.CENTER);

        // Split dalam: tengah (list tugas) dan kanan (detail)
        JSplitPane splitKonten = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelDaftar, panelDetail);
        splitKonten.setDividerLocation(360); // panel tengah dan kanan dibuat sama besar
        splitKonten.setResizeWeight(0.5);
        splitKonten.setOneTouchExpandable(false);
        splitKonten.setEnabled(false); // kunci divider agar tidak bisa di-drag user
        splitKonten.setDividerSize(2); // tetap ada garis pembatas tipis

        // Split utama: kiri (sidebar) dan kanan (konten utama)
        JSplitPane splitUtama = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelAksi, splitKonten);
        splitUtama.setDividerLocation(360); // panel kiri dibuat sama lebar dengan masing-masing panel kanan
        splitUtama.setResizeWeight(0.333);
        splitUtama.setOneTouchExpandable(false);
        splitUtama.setEnabled(false); // kunci divider agar tidak bisa di-drag user
        splitUtama.setDividerSize(2); // tetap ada garis pembatas tipis

        add(splitUtama, BorderLayout.CENTER);

        // Saat user klik baris tabel, panel detail ikut berubah
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                perbaruiPanelDetail();
            }
        });

        muatTugas(layananTugas.ambilSemuaTugas());
    }

    private void muatTugas(List<Tugas> daftarTugas) {
        modelTabel.setRowCount(0);
        for (Tugas tugas : daftarTugas) {
            modelTabel.addRow(new Object[] {
                    tugas.getId(),
                    tugas.getJudul(),
                    tugas.getDeskripsi(),
                    tugas.getTenggat(),
                    tugas.getPrioritas(),
                    tugas.isSelesai() ? "Selesai" : "Belum"
            });
        }
        perbaruiPengingat();
        perbaruiPanelDetail(); // sinkronkan panel detail setelah tabel di-refresh
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
        String opsi = String.valueOf(comboFilter.getSelectedItem());
        switch (opsi) {
            case "Selesai":
                muatTugas(layananTugas.saringBerdasarkanStatus(true));
                break;
            case "Belum":
                muatTugas(layananTugas.saringBerdasarkanStatus(false));
                break;
            case "Tinggi":
                muatTugas(layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.TINGGI));
                break;
            case "Sedang":
                muatTugas(layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.SEDANG));
                break;
            case "Rendah":
                muatTugas(layananTugas.saringBerdasarkanPrioritas(Tugas.Prioritas.RENDAH));
                break;
            default:
                muatTugas(layananTugas.ambilSemuaTugas());
        }
    }

    private void perbaruiPanelDetail() {
        int baris = tabel.getSelectedRow();

        // Jika belum ada pilihan, tampilkan placeholder
        if (baris < 0) {
            labelDetailJudul.setText("Judul: -");
            labelDetailTenggat.setText("Tenggat: -");
            labelDetailPrioritas.setText("Prioritas: -");
            labelDetailStatus.setText("Status: -");
            areaDetailDeskripsi.setText("Pilih satu tugas di tabel untuk melihat detail.");
            return;
        }

        Integer id = (Integer) modelTabel.getValueAt(baris, 0);
        Tugas tugas = layananTugas.cariBerdasarkanId(id);

        if (tugas == null) {
            labelDetailJudul.setText("Judul: -");
            labelDetailTenggat.setText("Tenggat: -");
            labelDetailPrioritas.setText("Prioritas: -");
            labelDetailStatus.setText("Status: -");
            areaDetailDeskripsi.setText("Data tugas tidak ditemukan.");
            return;
        }

        labelDetailJudul.setText("Judul: " + tugas.getJudul());
        labelDetailTenggat.setText("Tenggat: " + tugas.getTenggat());
        labelDetailPrioritas.setText("Prioritas: " + tugas.getPrioritas());
        labelDetailStatus.setText("Status: " + (tugas.isSelesai() ? "Selesai" : "Belum"));
        areaDetailDeskripsi.setText(
                tugas.getDeskripsi() == null || tugas.getDeskripsi().isBlank()
                        ? "(Tidak ada deskripsi)"
                        : tugas.getDeskripsi());
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
        JDialog dialog = new JDialog(this, tugas == null ? "Tambah Tugas" : "Edit Tugas", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField fieldJudul = new JTextField();
        JTextField fieldDeskripsi = new JTextField();
        JTextField fieldTenggat = new JTextField();
        JComboBox<String> fieldPrioritas = new JComboBox<>(new String[] { "TINGGI", "SEDANG", "RENDAH" });

        if (tugas != null) {
            fieldJudul.putClientProperty("JTextField.placeholderText", "Masukkan judul tugas...");
            fieldJudul.setText(tugas.getJudul());
            fieldDeskripsi.setText(tugas.getDeskripsi());
            fieldTenggat.setText(tugas.getTenggat().toString());
            fieldPrioritas.setSelectedItem(tugas.getPrioritas().name());
        }

        form.add(new JLabel("Judul"));
        form.add(fieldJudul);
        form.add(new JLabel("Deskripsi"));
        form.add(fieldDeskripsi);
        form.add(new JLabel("Tenggat (yyyy-MM-dd)"));
        form.add(fieldTenggat);
        form.add(new JLabel("Prioritas"));
        form.add(fieldPrioritas);

        JButton tombolSimpan = new JButton("Simpan");
        tombolSimpan.setHorizontalAlignment(SwingConstants.CENTER);
        tombolSimpan.addActionListener(e -> {
            String judul = fieldJudul.getText().trim();
            String deskripsi = fieldDeskripsi.getText().trim();
            String teksTenggat = fieldTenggat.getText().trim();
            Tugas.Prioritas prioritas = Tugas.Prioritas.valueOf(String.valueOf(fieldPrioritas.getSelectedItem()));

            if (judul.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Judul wajib diisi.");
                return;
            }

            LocalDate tenggat;
            try {
                tenggat = LocalDate.parse(teksTenggat);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Format tenggat harus yyyy-MM-dd.");
                return;
            }

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
}

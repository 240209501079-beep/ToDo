package com.todoapp.ui;

import com.todoapp.model.Tugas;
import com.todoapp.service.LayananTugas;

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
    private final LayananTugas layananTugas;
    private final DefaultTableModel modelTabel;
    private final JTable tabel;
    private final JTextArea areaPengingat;
    private final JComboBox<String> comboFilter;

    public FrameManajerTugas(LayananTugas layananTugas) {
        this.layananTugas = layananTugas;

        setTitle("To-Do App GUI");
        setSize(920, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        areaPengingat = new JTextArea(3, 30);
        areaPengingat.setEditable(false);
        areaPengingat.setLineWrap(true);
        areaPengingat.setWrapStyleWord(true);

        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.setBorder(BorderFactory.createTitledBorder("Pengingat"));
        panelAtas.add(new JScrollPane(areaPengingat), BorderLayout.CENTER);

        modelTabel = new DefaultTableModel(new String[]{"ID", "Judul", "Deskripsi", "Tenggat", "Prioritas", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(modelTabel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton tombolTambah = new JButton("Tambah");
        JButton tombolEdit = new JButton("Edit");
        JButton tombolHapus = new JButton("Hapus");
        JButton tombolUbahStatus = new JButton("Ubah Status");
        JButton tombolMuatUlang = new JButton("Muat Ulang");

        comboFilter = new JComboBox<>(new String[]{"Semua", "Selesai", "Belum", "Tinggi", "Sedang", "Rendah"});
        JButton tombolFilter = new JButton("Filter");
        
        tombolTambah.putClientProperty("JButton.buttonType", "roundRect");
        tombolTambah.setBackground(new Color(52, 152, 219));
        tombolTambah.setForeground(Color.WHITE);
        tombolTambah.addActionListener(e -> tampilkanDialogTugas(null));
        tombolEdit.addActionListener(e -> editTugasTerpilih());
        tombolHapus.addActionListener(e -> hapusTugasTerpilih());
        tombolUbahStatus.addActionListener(e -> ubahStatusTugasTerpilih());
        tombolMuatUlang.addActionListener(e -> muatTugas(layananTugas.ambilSemuaTugas()));
        tombolFilter.addActionListener(e -> terapkanFilter());

        panelAksi.add(tombolTambah);
        panelAksi.add(tombolEdit);
        panelAksi.add(tombolHapus);
        panelAksi.add(tombolUbahStatus);
        panelAksi.add(tombolMuatUlang);
        panelAksi.add(new JLabel("Filter:"));
        panelAksi.add(comboFilter);
        panelAksi.add(tombolFilter);
        panelAksi.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(panelAtas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(panelAksi, BorderLayout.SOUTH);

        muatTugas(layananTugas.ambilSemuaTugas());
    }

    private void muatTugas(List<Tugas> daftarTugas) {
        modelTabel.setRowCount(0);
        for (Tugas tugas : daftarTugas) {
            modelTabel.addRow(new Object[]{
                tugas.getId(),
                tugas.getJudul(),
                tugas.getDeskripsi(),
                tugas.getTenggat(),
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
        JComboBox<String> fieldPrioritas = new JComboBox<>(new String[]{"TINGGI", "SEDANG", "RENDAH"});

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

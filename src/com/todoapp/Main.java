package com.todoapp;

import com.todoapp.persistence.PengelolaFile;
import com.todoapp.service.LayananTugas;
import com.todoapp.ui.FrameManajerTugas;

import javax.swing.SwingUtilities;

public class Main {
    // PETA UBAH CEPAT:
    // 1) Ganti tema global di TemaAplikasi.terapkanTemaDefault()
    // 2) Ubah warna/radius komponen di class TemaAplikasi
    // 3) Ubah lokasi file data di PengelolaFile("data.txt")
    public static void main(String[] args) {
        TemaAplikasi.terapkanTemaDefault();

        // File data utama aplikasi.
        PengelolaFile pengelolaFile = new PengelolaFile("data.txt");
        // Service ini yang dipakai UI untuk tambah, edit, hapus, dan filter tugas.
        LayananTugas layananTugas = new LayananTugas(pengelolaFile);

        SwingUtilities.invokeLater(() -> {
            FrameManajerTugas tampilanUiMenu = new FrameManajerTugas(layananTugas);
            tampilanUiMenu.setVisible(true);
        });
    }
}

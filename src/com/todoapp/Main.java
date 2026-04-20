package com.todoapp;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.todoapp.persistence.PengelolaFile;
import com.todoapp.service.LayananTugas;
import com.todoapp.ui.FrameManajerTugas;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

public class Main {
    // PETA UBAH CEPAT:
    // 1) Ganti tema global di FlatLaf.setup(...)
    // 2) Ubah warna/radius komponen di UIManager.put(...)
    // 3) Ubah lokasi file data di PengelolaFile("data.txt")
    public static void main(String[] args) {
        // Ubah bagian ini kalau mau ganti tema aplikasi secara global.
        FlatLaf.setup(new FlatMacLightLaf());

        // Nilai UIManager di bawah ini mengontrol gaya komponen Swing.
        // Kalau warna atau radius berubah, cukup edit di sini.
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("CheckBox.arc", 12);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("Component.focusWidth", 1);

        UIManager.put("Panel.background", java.awt.Color.WHITE);
        UIManager.put("Table.background", java.awt.Color.WHITE);
        UIManager.put("Table.alternateRowColor", new java.awt.Color(245, 249, 255));
        UIManager.put("Table.selectionBackground", new java.awt.Color(185, 215, 255));
        UIManager.put("Table.selectionForeground", java.awt.Color.BLACK);
        UIManager.put("TextField.background", java.awt.Color.WHITE);
        UIManager.put("TextArea.background", java.awt.Color.WHITE);
        UIManager.put("ComboBox.background", java.awt.Color.WHITE);
        UIManager.put("Button.background", java.awt.Color.WHITE);
        UIManager.put("Button.foreground", new java.awt.Color(50, 50, 50));
        UIManager.put("Button.default.background", new java.awt.Color(38, 128, 235));
        UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
        UIManager.put("Component.focusColor", new java.awt.Color(38, 128, 235));

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

package com.todoapp;

import com.formdev.flatlaf.FlatDarkLaf;
import com.todoapp.persistence.PengelolaFile;
import com.todoapp.service.LayananTugas;
import com.todoapp.ui.FrameManajerTugas;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        PengelolaFile pengelolaFile = new PengelolaFile("data.txt");
        LayananTugas layananTugas = new LayananTugas(pengelolaFile);

        SwingUtilities.invokeLater(() -> {
            FrameManajerTugas tampilanUiMenu = new FrameManajerTugas(layananTugas);
            tampilanUiMenu.setVisible(true);
        });
    }
}

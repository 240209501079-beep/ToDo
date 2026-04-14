package com.todoapp;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.todoapp.persistence.PengelolaFile;
import com.todoapp.service.LayananTugas;
import com.todoapp.ui.FrameManajerTugas;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        PengelolaFile pengelolaFile = new PengelolaFile("data.txt");
        LayananTugas layananTugas = new LayananTugas(pengelolaFile);

        SwingUtilities.invokeLater(() -> {
            FrameManajerTugas tampilanUiMenu = new FrameManajerTugas(layananTugas);
            tampilanUiMenu.setVisible(true);
        });
    }
}

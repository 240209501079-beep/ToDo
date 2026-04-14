package com.todoapp;

import com.todoapp.persistence.PengelolaFile;
import com.todoapp.service.LayananTugas;
import com.todoapp.ui.FrameManajerTugas;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        PengelolaFile pengelolaFile = new PengelolaFile("data.txt");
        LayananTugas layananTugas = new LayananTugas(pengelolaFile);

        SwingUtilities.invokeLater(() -> {
            FrameManajerTugas tampilanUi = new FrameManajerTugas(layananTugas);
            tampilanUi.setVisible(true);
        });
    }
}

package com.todoapp;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.todoapp.persistence.PengelolaFile;
import com.todoapp.service.LayananTugas;
import com.todoapp.ui.FrameManajerTugas;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FlatLaf.setup(new FlatMacLightLaf());

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

        PengelolaFile pengelolaFile = new PengelolaFile("data.txt");
        LayananTugas layananTugas = new LayananTugas(pengelolaFile);

        SwingUtilities.invokeLater(() -> {
            FrameManajerTugas tampilanUiMenu = new FrameManajerTugas(layananTugas);
            tampilanUiMenu.setVisible(true);
        });
    }
}

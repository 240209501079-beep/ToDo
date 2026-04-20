package com.todoapp;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.UIManager;
import java.awt.Color;

public final class TemaAplikasi {
    private TemaAplikasi() {
    }

    // Pusat konfigurasi tema global aplikasi Swing.
    public static void terapkanTemaDefault() {
        FlatLaf.setup(new FlatMacLightLaf());

        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("CheckBox.arc", 12);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("Component.focusWidth", 1);

        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.alternateRowColor", new Color(245, 249, 255));
        UIManager.put("Table.selectionBackground", new Color(185, 215, 255));
        UIManager.put("Table.selectionForeground", Color.BLACK);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.foreground", new Color(50, 50, 50));
        UIManager.put("Button.default.background", new Color(38, 128, 235));
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Component.focusColor", new Color(38, 128, 235));
    }
}

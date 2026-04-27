package com.todoapp;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.todoapp.ui.KonfigurasiUi;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

public final class TemaAplikasi {
    private TemaAplikasi() {
    }

    public static void terapkanTemaDefault() {
        FlatLaf.setup(new FlatMacLightLaf());

        // Global Rounding
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("CheckBox.arc", 6);
        UIManager.put("ProgressBar.arc", 10);
        
        // Focus & Borders
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 10);
        UIManager.put("ScrollBar.trackArc", 10);

        // Table Styling
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 0));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", KonfigurasiUi.WARNA_GARIS);
        UIManager.put("Table.selectionBackground", KonfigurasiUi.WARNA_BIRU_MUDA);
        UIManager.put("Table.selectionForeground", KonfigurasiUi.WARNA_HITAM);
        UIManager.put("TableHeader.background", KonfigurasiUi.WARNA_PUTIH);
        UIManager.put("TableHeader.bottomSeparatorColor", KonfigurasiUi.WARNA_GARIS);

        // Colors
        UIManager.put("Panel.background", KonfigurasiUi.WARNA_BG_KONTEN);
        UIManager.put("Button.background", KonfigurasiUi.WARNA_PUTIH);
        UIManager.put("Button.default.background", KonfigurasiUi.WARNA_BIRU);
        UIManager.put("Button.default.foreground", Color.WHITE);
        
        // TabbedPane
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
    }
}

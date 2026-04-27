package com.todoapp.ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class PembantuUi {
    private PembantuUi() {
    }

    public static void aturGayaTombolSidebar(JButton tombol) {
        tombol.setMinimumSize(new Dimension(KonfigurasiUi.LEBAR_PANEL_KIRI - 30, KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR));
        tombol.setPreferredSize(new Dimension(KonfigurasiUi.LEBAR_PANEL_KIRI - 30, KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR));
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR));
        
        tombol.setBackground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_NORMAL);
        tombol.setForeground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_TEKS);
        tombol.setFont(tombol.getFont().deriveFont(Font.BOLD, 13f));
        tombol.setHorizontalAlignment(SwingConstants.LEFT);
        tombol.setBorderPainted(false);
        tombol.setFocusPainted(false);
        tombol.setContentAreaFilled(true);

        tombol.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tombol.setBackground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_HOVER);
                tombol.setForeground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_HOVER_TEKS);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tombol.setBackground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_NORMAL);
                tombol.setForeground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_TEKS);
            }
        });
    }

    public static void aturUkuranTombol(JButton tombol, int tinggi, int lebar) {
        Dimension d = new Dimension(lebar, tinggi);
        tombol.setMinimumSize(d);
        tombol.setPreferredSize(d);
        tombol.setMaximumSize(d);
    }
}

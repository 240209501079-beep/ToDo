package com.todoapp.ui;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class PembantuUi {
    private PembantuUi() {
    }

    public static void aturGayaTombolSidebar(JButton tombol) {
        aturUkuranTombol(tombol, KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR);
        tombol.setBackground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_NORMAL);
        tombol.setForeground(KonfigurasiUi.WARNA_TOMBOL_SIDEBAR_TEKS);
        tombol.setFocusPainted(false);
        tombol.setContentAreaFilled(true);
        tombol.setOpaque(true);

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

    public static void aturUkuranTombol(JButton tombol, int tinggi) {
        int lebarDefault = tombol.getPreferredSize().width;
        tombol.setMinimumSize(new Dimension(lebarDefault, tinggi));
        tombol.setPreferredSize(new Dimension(lebarDefault, tinggi));
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, tinggi));
    }

    public static void aturUkuranTombol(JButton tombol, int tinggi, int lebar) {
        tombol.setMinimumSize(new Dimension(lebar, tinggi));
        tombol.setPreferredSize(new Dimension(lebar, tinggi));
        tombol.setMaximumSize(new Dimension(lebar, tinggi));
    }

    public static String gayaTombolFilter() {
        return "arc: " + KonfigurasiUi.ROUNDNESS_TOMBOL_FILTER
                + "; borderWidth: " + KonfigurasiUi.KETEBALAN_OUTLINE_TOMBOL_FILTER
                + "; borderColor: #FFFFFF; focusWidth: 0"
                + "; hoverBackground: #ffffff; hoverForeground: #2680EB";
    }
}

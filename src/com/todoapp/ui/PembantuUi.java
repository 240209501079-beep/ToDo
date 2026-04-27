package com.todoapp.ui;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class PembantuUi {
    private PembantuUi() {
    }

    public static void aturGayaTombolSidebar(JButton tombol) {
        tombol.setMinimumSize(new Dimension(KonfigurasiUi.LEBAR_PANEL_KIRI - 30, KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR));
        tombol.setPreferredSize(
                new Dimension(KonfigurasiUi.LEBAR_PANEL_KIRI - 30, KonfigurasiUi.TINGGI_TOMBOL_SIDEBAR));
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

    private static Image ikonAplikasi = null;

    public static void aturIkonWindow(java.awt.Window window) {
        try {
            if (ikonAplikasi == null) {
                // Gunakan ikon yang paling ringan (90KB) sebagai prioritas utama
                String[] resourcePaths = {"/iconmin.png", "iconmin.png", "/iconmini.png", "iconmini.png", "/icon.png", "icon.png"};
                for (String path : resourcePaths) {
                    java.net.URL url = PembantuUi.class.getResource(path);
                    if (url != null) {
                        ikonAplikasi = javax.imageio.ImageIO.read(url);
                        break;
                    }
                }
                
                // Jika resource gagal, coba file fisik
                if (ikonAplikasi == null) {
                    String[] filePaths = {"iconmin.png", "app/iconmin.png", "iconmini.png", "app/iconmini.png", "icon.png", "app/icon.png"};
                    for (String path : filePaths) {
                        java.io.File f = new java.io.File(path);
                        if (f.exists()) {
                            ikonAplikasi = javax.imageio.ImageIO.read(f);
                            break;
                        }
                    }
                }

                // Kecilkan ikon agar ringan (64x64 pixel)
                if (ikonAplikasi != null) {
                    ikonAplikasi = ikonAplikasi.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                }
            }

            if (ikonAplikasi != null) {
                window.setIconImage(ikonAplikasi);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon: " + e.getMessage());
        }
    }
}

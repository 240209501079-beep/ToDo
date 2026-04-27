package com.todoapp.ui;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

public final class KonfigurasiUi {
    private KonfigurasiUi() {
    }

    // Modern Color Palette (Tailwind-inspired)
    public static final Color WARNA_PUTIH = new Color(255, 255, 255);
    public static final Color WARNA_HITAM = new Color(15, 23, 42); // Slate 900
    
    // Accents
    public static final Color WARNA_BIRU = new Color(37, 99, 235); // Blue 600
    public static final Color WARNA_BIRU_MUDA = new Color(219, 234, 254); // Blue 100
    public static final Color WARNA_HIJAU = new Color(5, 150, 105); // Emerald 600
    public static final Color WARNA_MERAH = new Color(220, 38, 38); // Red 600
    public static final Color WARNA_ORANYE = new Color(217, 119, 6); // Amber 600
    public static final Color WARNA_ABU_MUDA = new Color(241, 245, 249); // Slate 100
    public static final Color WARNA_ABU_TEKS = new Color(100, 116, 139); // Slate 500

    // UI Structure Colors
    public static final Color WARNA_BG_HEADER = WARNA_PUTIH;
    public static final Color WARNA_BG_SIDEBAR = new Color(15, 23, 42); // Slate 900
    public static final Color WARNA_BG_KONTEN = new Color(248, 250, 252); // Slate 50

    public static final int LEBAR_PEMBATAS_TENGAH = 1;
    public static final Color WARNA_GARIS = new Color(226, 232, 240); // Slate 200

    public static final Color WARNA_TULISAN_SIDEMENU = new Color(148, 163, 184); // Slate 400
    public static final Color WARNA_TULISAN_LABEL_FILTER = new Color(71, 85, 105); // Slate 600

    public static final Color WARNA_TOMBOL_SIDEBAR_NORMAL = new Color(15, 23, 42); // Transparent look
    public static final Color WARNA_TOMBOL_SIDEBAR_HOVER = new Color(30, 41, 59); // Slate 800
    public static final Color WARNA_TOMBOL_SIDEBAR_TEKS = new Color(241, 245, 249); // Slate 100
    public static final Color WARNA_TOMBOL_SIDEBAR_HOVER_TEKS = Color.WHITE;

    // Dimensions
    public static final int LEBAR_WINDOW = 1100;
    public static final int TINGGI_WINDOW = 750;
    public static final int LEBAR_PANEL_KIRI = 260; // Slightly narrower sidebar
    public static final int TINGGI_TOMBOL_SIDEBAR = 45;
    public static final int TINGGI_TOMBOL_FILTER = 32;
    public static final int LEBAR_TOMBOL_FILTER = 90;
    public static final int JARAK_ANTAR_TOMBOL_ATAS = 4;
    public static final int UKURAN_FONT_SAPAAN = 18;

    public static final DateTimeFormatter FORMAT_TENGGAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
}

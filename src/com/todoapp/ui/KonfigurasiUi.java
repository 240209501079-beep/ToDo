package com.todoapp.ui;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

public final class KonfigurasiUi {
    private KonfigurasiUi() {
    }

    public static final Color WARNA_PUTIH = Color.WHITE;
    public static final Color WARNA_HITAM = Color.BLACK;
    public static final Color WARNA_HIJAU = Color.GREEN.darker();
    public static final Color WARNA_BIRU = new Color(38, 128, 235);
    public static final Color WARNA_BIRU_MUDA = new Color(233, 244, 255);
    public static final Color WARNA_GARIS = new Color(28, 127, 226);

    public static final Color WARNA_BG_HEADER = Color.WHITE;
    public static final Color WARNA_BG_SIDEBAR = new Color(38, 128, 235);
    public static final Color WARNA_BG_KONTEN = new Color(38, 128, 235);

    public static final int LEBAR_PEMBATAS_TENGAH = 2;

    public static final Color WARNA_TULISAN_SIDEMENU = Color.WHITE;
    public static final Color WARNA_TULISAN_LABEL_FILTER = Color.WHITE;

    public static final Color WARNA_OUTLINE_TOMBOL_FILTER = Color.WHITE;
    public static final int KETEBALAN_OUTLINE_TOMBOL_FILTER = 1;
    public static final int ROUNDNESS_TOMBOL_FILTER = 30;

    public static final Color WARNA_TOMBOL_SIDEBAR_NORMAL = Color.WHITE;
    public static final Color WARNA_TOMBOL_SIDEBAR_HOVER = new Color(233, 244, 255);
    public static final Color WARNA_TOMBOL_SIDEBAR_TEKS = Color.BLACK;
    public static final Color WARNA_TOMBOL_SIDEBAR_HOVER_TEKS = Color.BLACK;

    public static final int LEBAR_WINDOW = 1080;
    public static final int TINGGI_WINDOW = 720;
    public static final int LEBAR_PANEL_KIRI = 360;
    public static final int TINGGI_TOMBOL_SIDEBAR = 50;
    public static final int TINGGI_TOMBOL_FILTER = 30;
    public static final int LEBAR_TOMBOL_FILTER = 80;
    public static final int JARAK_ANTAR_TOMBOL_ATAS = 8;
    public static final int UKURAN_FONT_SAPAAN = 16;

    public static final DateTimeFormatter FORMAT_TENGGAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
}

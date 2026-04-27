package com.todoapp.ui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom Toast Notification yang muncul di pojok kanan bawah layar.
 * Tidak bergantung pada System Tray API Windows, sehingga bekerja 100%
 * pada aplikasi yang di-install via jpackage maupun dijalankan langsung.
 */
public class NotifikasiToast extends JWindow {

    private static final int LEBAR = 340;
    private static final int TINGGI = 100;
    private static final int DURASI_MS = 5000; // Tampil selama 5 detik


    private NotifikasiToast(String judul, String pesan) {
        // Jendela tanpa border, selalu di atas
        setAlwaysOnTop(true);

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(30, 30, 46)); // Dark background
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 1), // Biru garis
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        // Ikon
        JLabel lblIkon = new JLabel("🔔");
        lblIkon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        // Teks
        JPanel txtPanel = new JPanel(new BorderLayout(0, 4));
        txtPanel.setOpaque(false);

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("Inter", Font.BOLD, 13));
        lblJudul.setForeground(new Color(56, 189, 248)); // Biru

        JLabel lblPesan = new JLabel("<html><body style='width:220px'>" + pesan + "</body></html>");
        lblPesan.setFont(new Font("Inter", Font.PLAIN, 12));
        lblPesan.setForeground(new Color(200, 200, 220)); // Putih pucat

        txtPanel.add(lblJudul, BorderLayout.NORTH);
        txtPanel.add(lblPesan, BorderLayout.CENTER);

        // Tombol tutup
        JLabel btnTutup = new JLabel("✕");
        btnTutup.setForeground(new Color(120, 120, 140));
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.setVerticalAlignment(SwingConstants.TOP);
        btnTutup.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { tutup(); }
            @Override public void mouseEntered(MouseEvent e) { btnTutup.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { btnTutup.setForeground(new Color(120, 120, 140)); }
        });

        panel.add(lblIkon, BorderLayout.WEST);
        panel.add(txtPanel, BorderLayout.CENTER);
        panel.add(btnTutup, BorderLayout.EAST);

        setContentPane(panel);
        setSize(LEBAR, TINGGI);

        // Posisi di pojok kanan bawah layar
        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int x = layar.width - LEBAR - 15 - insets.right;
        int y = layar.height - TINGGI - 50 - insets.bottom;
        setLocation(x, y);

        // Set ikon aplikasi
        PembantuUi.aturIkonWindow(this);
    }

    private void tampilkan() {
        setVisible(true);
        // Bunyikan suara notifikasi sistem
        java.awt.Toolkit.getDefaultToolkit().beep();
        // Timer untuk auto-tutup
        new Timer(DURASI_MS, e -> {
            ((Timer) e.getSource()).stop();
            tutup();
        }).start();
    }

    private void tutup() {
        setVisible(false);
        dispose();
    }

    /**
     * Metode statis utama untuk menampilkan toast.
     * Selalu dipanggil dari Event Dispatch Thread.
     */
    public static void tampilkan(String judul, String pesan) {
        SwingUtilities.invokeLater(() -> {
            NotifikasiToast toast = new NotifikasiToast(judul, pesan);
            toast.tampilkan();
        });
    }
}

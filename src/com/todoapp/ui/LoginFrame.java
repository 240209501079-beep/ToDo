package com.todoapp.ui;

import com.todoapp.service.AuthService;
import com.todoapp.service.LayananTugas;
import com.todoapp.service.SessionManager;
import com.todoapp.persistence.FirebaseStorage;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final SessionManager sessionManager;

    public LoginFrame(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        setTitle("Login - To-Do List");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Set Window Icon
        PembantuUi.aturIkonWindow(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Logo Image
        JLabel logo = new JLabel();
        try {
            java.net.URL iconURL = getClass().getResource("/icon.png");
            ImageIcon iconOrig = (iconURL != null) ? new ImageIcon(iconURL) : new ImageIcon("icon.png");
            Image scaled = iconOrig.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            logo.setText("📝");
            logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        }

        JLabel title = new JLabel("To-Do List");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(KonfigurasiUi.WARNA_HITAM);

        JLabel subtitle = new JLabel("Kelola tugas Anda di mana saja");
        subtitle.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitle.setForeground(KonfigurasiUi.WARNA_ABU_TEKS);

        JButton btnLogin = new JButton("Masuk dengan Google");
        btnLogin.setPreferredSize(new Dimension(250, 45));
        btnLogin.setBackground(KonfigurasiUi.WARNA_BIRU);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Inter", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);

        btnLogin.addActionListener(e -> {
            btnLogin.setEnabled(false);
            btnLogin.setText("Menghubungkan...");

            authService.loginWithGoogle().thenAccept(json -> {
                handleLoginSuccess(json);
            }).exceptionally(ex -> {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Login gagal: " + ex.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Masuk dengan Google");
                });
                return null;
            });
        });

        gbc.gridy = 0;
        p.add(logo, gbc);
        gbc.gridy = 1;
        p.add(title, gbc);
        gbc.gridy = 2;
        p.add(subtitle, gbc);
        gbc.gridy = 3;
        p.add(Box.createVerticalStrut(30), gbc);
        gbc.gridy = 4;
        p.add(btnLogin, gbc);

        add(p, BorderLayout.CENTER);
    }

    private void handleLoginSuccess(JsonObject json) {
        System.out.println("DEBUG [Login]: Data otentikasi diterima dari Google.");
        String token = json.get("idToken").getAsString();
        String refresh = json.get("refreshToken").getAsString();
        String email = json.get("email").getAsString();
        String uid = json.get("localId").getAsString();

        System.out.println("DEBUG [Login]: Menyimpan sesi untuk: " + email);
        sessionManager.simpanSesi(token, refresh, email, uid);

        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG [Login]: Membuka Dashboard Utama...");
            FirebaseStorage storage = new FirebaseStorage(token, uid);
            LayananTugas service = new LayananTugas(storage);
            FrameManajerTugas frame = new FrameManajerTugas(service, sessionManager);
            frame.setVisible(true);

            // Trigger auto-reload data dari Cloud seketika setelah login
            new Thread(() -> {
                System.out.println("DEBUG [Login-Auto]: Memulai sinkronisasi data awal...");
                frame.refreshData();
            }).start();

            this.dispose();
        });
    }
}

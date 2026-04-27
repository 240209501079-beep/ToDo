package com.todoapp;

import com.todoapp.persistence.FirebaseStorage;
import com.todoapp.service.AuthService;
import com.todoapp.service.LayananTugas;
import com.todoapp.service.SessionManager;
import com.todoapp.ui.FrameManajerTugas;
import com.todoapp.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    private static java.nio.channels.FileLock lock;
    private static java.nio.channels.FileChannel channel;
    private static FrameManajerTugas instanceFrame;

    private static void log(String msg) {
        String time = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date());
        System.out.println("[" + time + "] " + msg);
    }

    private static final int PORT_KOMUNIKASI = 55667;

    public static void main(String[] args) {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), "todoapp.lock");
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw");
            channel = raf.getChannel();
            lock = channel.tryLock();

            if (lock == null) {
                log("DEBUG: Aplikasi sudah jalan. Mengirim perintah untuk memunculkan jendela...");
                try (java.net.Socket socket = new java.net.Socket("localhost", PORT_KOMUNIKASI)) {
                } catch (Exception e) {
                    log("DEBUG: Gagal menghubungi aplikasi utama.");
                }
                raf.close();
                System.exit(0);
            }
            file.deleteOnExit();
            mulaiServerKomunikasi();
        } catch (Exception e) {
            System.err.println("Peringatan: Gagal membuat sistem proteksi instance.");
        }

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("CRITICAL ERROR di thread " + t.getName() + ":");
            e.printStackTrace();
        });

        boolean startMinimized = args.length > 0 && args[0].equalsIgnoreCase("--startup");

        log("DEBUG [1]: Aplikasi dimulai...");
        log("DEBUG [2]: Menerapkan Tema UI...");
        TemaAplikasi.terapkanTemaDefault();
        
        log("DEBUG [3]: Menginisialisasi SessionManager...");
        SessionManager sessionManager = new SessionManager();
        
        log("DEBUG [4]: Masuk ke thread UI (EDT)...");
        SwingUtilities.invokeLater(() -> {
            log("DEBUG [5]: Di dalam thread UI.");
            try {
                if (sessionManager.muatSesi()) {
                    log("DEBUG [6]: Sesi ditemukan. Mencoba refresh token...");
                    AuthService auth = new AuthService();
                    auth.refreshFirebaseToken(sessionManager.getRefreshToken())
                        .thenAccept(json -> {
                            try {
                                log("DEBUG [7]: Refresh token sukses.");
                                if (json != null && json.has("id_token")) {
                                    String newToken = json.get("id_token").getAsString();
                                    String newRefresh = json.get("refresh_token").getAsString();
                                    sessionManager.simpanSesi(newToken, newRefresh, sessionManager.getUserEmail(), sessionManager.getUserId());
                                    
                                    log("DEBUG [8]: Menyiapkan Frame Utama...");
                                    FirebaseStorage storage = new FirebaseStorage(newToken, sessionManager.getUserId());
                                    LayananTugas service = new LayananTugas(storage);
                                    
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            log("DEBUG [9]: Menyiapkan Jendela Utama...");
                                            instanceFrame = new FrameManajerTugas(service, sessionManager);
                                            log("DEBUG [9.5]: Frame berhasil dibuat.");
                                            
                                            if (!startMinimized) {
                                                log("DEBUG [10]: Menampilkan jendela...");
                                                instanceFrame.setVisible(true);
                                                log("DEBUG [11]: Jendela aktif.");
                                            } else {
                                                log("DEBUG [10b]: Berjalan di latar belakang.");
                                            }
                                            
                                            new Thread(() -> {
                                                log("DEBUG [12]: Memulai pengunduhan data latar belakang...");
                                                instanceFrame.refreshData();
                                                log("DEBUG [13]: Pengunduhan data selesai.");
                                            }).start();

                                        } catch (Exception e) {
                                            System.err.println("CRITICAL UI ERROR: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    });
                                } else {
                                    log("DEBUG [7-FAIL]: ID Token tidak ditemukan.");
                                    SwingUtilities.invokeLater(() -> new LoginFrame(sessionManager).setVisible(true));
                                }
                            } catch (Exception e) {
                                log("DEBUG [ERR-INSIDE]: " + e.getMessage());
                                SwingUtilities.invokeLater(() -> new LoginFrame(sessionManager).setVisible(true));
                            }
                        }).exceptionally(ex -> {
                            log("DEBUG [ERR-AUTH]: " + ex.getMessage());
                            SwingUtilities.invokeLater(() -> new LoginFrame(sessionManager).setVisible(true));
                            return null;
                        });
                } else {
                    log("DEBUG [6b]: Sesi tidak ada. Membuka LoginFrame...");
                    new LoginFrame(sessionManager).setVisible(true);
                }
            } catch (Exception e) {
                log("DEBUG [ERR-EDT]: " + e.getMessage());
                new LoginFrame(sessionManager).setVisible(true);
            }
        });
    }

    private static void mulaiServerKomunikasi() {
        new Thread(() -> {
            try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(PORT_KOMUNIKASI)) {
                while (true) {
                    try (java.net.Socket clientSocket = serverSocket.accept()) {
                        log("DEBUG [IPC]: Instance lain terdeteksi. Memunculkan jendela utama...");
                        if (instanceFrame != null) {
                            SwingUtilities.invokeLater(() -> {
                                instanceFrame.setVisible(true);
                                instanceFrame.toFront();
                                instanceFrame.repaint();
                            });
                        }
                    } catch (Exception e) {}
                }
            } catch (Exception e) {
                log("DEBUG [IPC-ERR]: Gagal memulai listener komunikasi.");
            }
        }, "IPC-Listener").start();
    }
}

package com.todoapp;

import com.todoapp.persistence.FirebaseStorage;
import com.todoapp.service.AuthService;
import com.todoapp.service.LayananTugas;
import com.todoapp.service.SessionManager;
import com.todoapp.ui.FrameManajerTugas;
import com.todoapp.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        TemaAplikasi.terapkanTemaDefault();
        SessionManager sessionManager = new SessionManager();
        
        SwingUtilities.invokeLater(() -> {
            if (sessionManager.muatSesi()) {
                // Sesi ada, coba refresh token lalu masuk
                AuthService auth = new AuthService();
                auth.refreshFirebaseToken(sessionManager.getRefreshToken())
                    .thenAccept(json -> {
                        String newToken = json.get("access_token").getAsString();
                        sessionManager.setFirebaseToken(newToken);
                        
                        FirebaseStorage storage = new FirebaseStorage(newToken, sessionManager.getUserId());
                        LayananTugas service = new LayananTugas(storage);
                        new FrameManajerTugas(service).setVisible(true);
                    }).exceptionally(ex -> {
                        new LoginFrame(sessionManager).setVisible(true);
                        return null;
                    });
            } else {
                new LoginFrame(sessionManager).setVisible(true);
            }
        });
    }
}

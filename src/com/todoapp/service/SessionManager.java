package com.todoapp.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SessionManager {
    private static final String SESSION_FILE = "session.json";
    private static final Gson gson = new Gson();
    
    private String firebaseToken;
    private String refreshToken;
    private String userEmail;
    private String userId;

    public void simpanSesi(String token, String refresh, String email, String uid) {
        this.firebaseToken = token;
        this.refreshToken = refresh;
        this.userEmail = email;
        this.userId = uid;
        
        JsonObject json = new JsonObject();
        json.addProperty("token", token);
        json.addProperty("refresh", refresh);
        json.addProperty("email", email);
        json.addProperty("uid", uid);
        
        try {
            Files.writeString(Paths.get(SESSION_FILE), gson.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean muatSesi() {
        if (!Files.exists(Paths.get(SESSION_FILE))) return false;
        
        try {
            String content = Files.readString(Paths.get(SESSION_FILE));
            JsonObject json = gson.fromJson(content, JsonObject.class);
            this.firebaseToken = json.get("token").getAsString();
            this.refreshToken = json.get("refresh").getAsString();
            this.userEmail = json.get("email").getAsString();
            this.userId = json.get("uid").getAsString();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void hapusSesi() {
        try {
            Files.deleteIfExists(Paths.get(SESSION_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.firebaseToken = null;
        this.refreshToken = null;
        this.userEmail = null;
        this.userId = null;
    }

    public String getFirebaseToken() { return firebaseToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUserEmail() { return userEmail; }
    public String getUserId() { return userId; }
    public void setFirebaseToken(String token) { this.firebaseToken = token; }
}

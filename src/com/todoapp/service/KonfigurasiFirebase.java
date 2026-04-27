package com.todoapp.service;

public final class KonfigurasiFirebase {
    private KonfigurasiFirebase() {}

    // Ganti dengan Web Client ID yang Anda dapatkan dari Authentication > Google > Web SDK Configuration
    public static final String GOOGLE_CLIENT_ID = "274871462279-pui8oqccevgpnkfe6os9uar9kmr22r1g.apps.googleusercontent.com";
    
    // API Key dari Firebase Project Settings
    public static final String FIREBASE_API_KEY = "AIzaSyDWeDJEkf36IgoAjVTztsAyaeJldID6Ns0";
    
    // Project ID Firebase
    public static final String FIREBASE_PROJECT_ID = "todolist-4b67f";

    // Redirect URI untuk menangkap token (harus didaftarkan di Google Cloud Console)
    public static final String REDIRECT_URI = "http://localhost:8888";
    public static final int REDIRECT_PORT = 8888;
}

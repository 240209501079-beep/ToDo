package com.todoapp.service;

public final class KonfigurasiFirebase {
    private KonfigurasiFirebase() {}

    // PENTING: Ganti nilai di bawah ini dengan kredensial Firebase Anda sendiri.
    // Jangan pernah commit nilai asli ke Git! Gunakan environment variable atau file .env
    public static final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID") != null
            ? System.getenv("GOOGLE_CLIENT_ID")
            : "YOUR_GOOGLE_CLIENT_ID_HERE";

    public static final String GOOGLE_CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET") != null
            ? System.getenv("GOOGLE_CLIENT_SECRET")
            : "YOUR_GOOGLE_CLIENT_SECRET_HERE";

    // API Key dari Firebase Project Settings
    public static final String FIREBASE_API_KEY = System.getenv("FIREBASE_API_KEY") != null
            ? System.getenv("FIREBASE_API_KEY")
            : "YOUR_FIREBASE_API_KEY_HERE";

    // Project ID Firebase
    public static final String FIREBASE_PROJECT_ID = "todolist-4b67f";

    // Redirect URI untuk menangkap token (harus didaftarkan di Google Cloud Console)
    public static final String REDIRECT_URI = "http://127.0.0.1:8888";
    public static final int REDIRECT_PORT = 8888;
}

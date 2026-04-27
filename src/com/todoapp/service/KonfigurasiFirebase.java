package com.todoapp.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

public final class KonfigurasiFirebase {
    private KonfigurasiFirebase() {}

    private static final Properties props = muatKonfigurasi();

    private static Properties muatKonfigurasi() {
        Properties p = new Properties();

        // Tentukan direktori JAR/EXE dengan benar untuk Windows (pakai toURI!)
        String jarDir = ".";
        try {
            URI location = KonfigurasiFirebase.class
                .getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(location);
            jarDir = jarFile.isDirectory()
                ? jarFile.getAbsolutePath()
                : jarFile.getParentFile().getAbsolutePath();
        } catch (Exception e) {
            System.err.println("DEBUG [Config]: Gagal deteksi direktori JAR: " + e.getMessage());
        }

        // Cari firebase.properties di berbagai lokasi
        String[] lokasi = {
            jarDir + "/firebase.properties",            // sama dengan JAR / app/
            jarDir + "/../firebase.properties",         // parent dari app/ (folder instalasi)
            System.getProperty("user.dir") + "/firebase.properties",  // working dir
            System.getProperty("user.home") + "/firebase.properties", // home user
            "firebase.properties",                                      // fallback relatif
        };

        for (String path : lokasi) {
            File f = new File(path);
            try {
                if (f.exists() && f.isFile()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        p.load(fis);
                        System.out.println("DEBUG [Config]: Konfigurasi dimuat dari: "
                            + f.getCanonicalPath());
                        return p;
                    }
                }
            } catch (Exception e) {
                System.err.println("DEBUG [Config-ERR]: Gagal membaca " + path + ": " + e.getMessage());
            }
        }

        System.err.println("PERINGATAN: firebase.properties tidak ditemukan di semua lokasi!");
        System.err.println("           Gunakan Environment Variable sebagai fallback.");
        return p;
    }

    private static String get(String key, String defaultVal) {
        // Prioritas: file properties → environment variable → default
        String val = props.getProperty(key);
        if (val != null && !val.trim().isEmpty()) return val.trim();
        val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) return val.trim();
        return defaultVal;
    }

    public static final String GOOGLE_CLIENT_ID     = get("GOOGLE_CLIENT_ID", "");
    public static final String GOOGLE_CLIENT_SECRET = get("GOOGLE_CLIENT_SECRET", "");
    public static final String FIREBASE_API_KEY     = get("FIREBASE_API_KEY", "");
    public static final String FIREBASE_PROJECT_ID  = "todolist-4b67f";
    public static final String REDIRECT_URI         = "http://127.0.0.1:8888";
    public static final int    REDIRECT_PORT        = 8888;
}

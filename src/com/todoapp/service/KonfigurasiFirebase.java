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

        // 1. Coba muat dari Internal Resources (dalam JAR) sebagai default
        try (java.io.InputStream is = KonfigurasiFirebase.class.getResourceAsStream("/firebase.properties")) {
            if (is != null) {
                p.load(is);
                System.out.println("DEBUG [Config]: Konfigurasi default dimuat dari internal resource.");
            }
        } catch (Exception e) {
            System.err.println("DEBUG [Config-ERR]: Gagal membaca internal resource: " + e.getMessage());
        }

        // 2. Cek lokasi eksternal untuk override (penting untuk pengembangan)
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

        String[] lokasi = {
            jarDir + "/firebase.properties",
            jarDir + "/../firebase.properties",
            System.getProperty("user.dir") + "/firebase.properties",
            System.getProperty("user.home") + "/firebase.properties",
            "firebase.properties",
        };

        for (String path : lokasi) {
            File f = new File(path);
            try {
                if (f.exists() && f.isFile()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        Properties overrideProps = new Properties();
                        overrideProps.load(fis);
                        p.putAll(overrideProps); // Override nilai internal dengan nilai eksternal
                        System.out.println("DEBUG [Config]: Konfigurasi di-override dari: " + f.getCanonicalPath());
                        return p;
                    }
                }
            } catch (Exception e) {
                // Abaikan jika satu lokasi gagal
            }
        }

        if (p.isEmpty()) {
            System.err.println("PERINGATAN: firebase.properties tidak ditemukan di resource maupun lokasi eksternal!");
        }
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

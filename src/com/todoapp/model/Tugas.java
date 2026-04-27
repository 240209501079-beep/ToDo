package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Tugas {
    private int id;
    private String judul;
    private String deskripsi;
    private LocalDateTime tenggat;
    private Prioritas prioritas;
    private boolean selesai;
    private static final DateTimeFormatter FORMAT_TANGGAL_WAKTU = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public enum Prioritas {
        TINGGI,
        SEDANG,
        RENDAH
    }

    public Tugas(int id, String judul, String deskripsi, LocalDateTime tenggat, Prioritas prioritas, boolean selesai) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.tenggat = tenggat;
        this.prioritas = prioritas;
        this.selesai = selesai;
    }

    public int getId() {
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public LocalDateTime getTenggat() {
        return tenggat;
    }

    public void setTenggat(LocalDateTime tenggat) {
        this.tenggat = tenggat;
    }

    public Prioritas getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(Prioritas prioritas) {
        this.prioritas = prioritas;
    }

    public boolean isSelesai() {
        return selesai;
    }

    public void setSelesai(boolean selesai) {
        this.selesai = selesai;
    }

    public String keBarisData() {
        // Format simpan ke file: kolom dipisah dengan karakter |.
        // Judul dan deskripsi dienkode agar simbol | aman disimpan.
        return id + "|" + enkode(judul) + "|" + enkode(deskripsi) + "|" + tenggat.format(FORMAT_TANGGAL_WAKTU) + "|" + prioritas + "|" + selesai;
    }

    public static Tugas dariBarisData(String baris) {
        // Balikkan format file ke objek Tugas.
        String[] bagian = baris.split("\\|", -1);
        if (bagian.length != 6) {
            return null;
        }

        try {
            int id = Integer.parseInt(bagian[0]);
            String judul = dekode(bagian[1]);
            String deskripsi = dekode(bagian[2]);
            LocalDateTime tenggat;
            if (bagian[3].contains(":")) {
                tenggat = LocalDateTime.parse(bagian[3], FORMAT_TANGGAL_WAKTU);
            } else {
                // Kompatibilitas data lama yang masih format tanggal saja.
                tenggat = LocalDate.parse(bagian[3]).atTime(23, 59);
            }
            Prioritas prioritas = Prioritas.valueOf(bagian[4]);
            boolean selesai = Boolean.parseBoolean(bagian[5]);
            return new Tugas(id, judul, deskripsi, tenggat, prioritas, selesai);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String enkode(String nilai) {
        // Base64 dipakai supaya teks aman saat disimpan ke file teks biasa.
        return Base64.getEncoder().encodeToString(nilai.getBytes());
    }

    private static String dekode(String nilai) {
        // Pasangan dari enkode().
        return new String(Base64.getDecoder().decode(nilai));
    }
}

package com.todoapp.model;

import java.time.LocalDate;
import java.util.Base64;

public class Tugas {
    private int id;
    private String judul;
    private String deskripsi;
    private LocalDate tenggat;
    private Prioritas prioritas;
    private boolean selesai;

    public enum Prioritas {
        TINGGI,
        SEDANG,
        RENDAH
    }

    public Tugas(int id, String judul, String deskripsi, LocalDate tenggat, Prioritas prioritas, boolean selesai) {
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

    public LocalDate getTenggat() {
        return tenggat;
    }

    public void setTenggat(LocalDate tenggat) {
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
        return id + "|" + enkode(judul) + "|" + enkode(deskripsi) + "|" + tenggat + "|" + prioritas + "|" + selesai;
    }

    public static Tugas dariBarisData(String baris) {
        String[] bagian = baris.split("\\|", -1);
        if (bagian.length != 6) {
            return null;
        }

        try {
            int id = Integer.parseInt(bagian[0]);
            String judul = dekode(bagian[1]);
            String deskripsi = dekode(bagian[2]);
            LocalDate tenggat = LocalDate.parse(bagian[3]);
            Prioritas prioritas = Prioritas.valueOf(bagian[4]);
            boolean selesai = Boolean.parseBoolean(bagian[5]);
            return new Tugas(id, judul, deskripsi, tenggat, prioritas, selesai);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String enkode(String nilai) {
        return Base64.getEncoder().encodeToString(nilai.getBytes());
    }

    private static String dekode(String nilai) {
        return new String(Base64.getDecoder().decode(nilai));
    }
}

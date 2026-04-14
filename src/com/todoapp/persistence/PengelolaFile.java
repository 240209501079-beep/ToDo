package com.todoapp.persistence;

import com.todoapp.model.Tugas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PengelolaFile {
    private final Path jalurData;

    public PengelolaFile(String namaFile) {
        this.jalurData = Paths.get(namaFile);
    }

    public List<Tugas> muatTugas() {
        List<Tugas> daftarTugas = new ArrayList<>();

        if (!Files.exists(jalurData)) {
            return daftarTugas;
        }

        try (BufferedReader pembaca = Files.newBufferedReader(jalurData)) {
            String baris;
            while ((baris = pembaca.readLine()) != null) {
                if (baris.trim().isEmpty()) {
                    continue;
                }

                Tugas tugas = Tugas.dariBarisData(baris);
                if (tugas != null) {
                    daftarTugas.add(tugas);
                }
            }
        } catch (IOException ex) {
            System.out.println("Gagal membaca data: " + ex.getMessage());
        }

        return daftarTugas;
    }

    public void simpanTugas(List<Tugas> daftarTugas) {
        try (BufferedWriter penulis = Files.newBufferedWriter(jalurData)) {
            for (Tugas tugas : daftarTugas) {
                penulis.write(tugas.keBarisData());
                penulis.newLine();
            }
        } catch (IOException ex) {
            System.out.println("Gagal menyimpan data: " + ex.getMessage());
        }
    }
}

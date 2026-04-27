package com.todoapp.service;

import com.todoapp.model.Tugas;
import com.todoapp.persistence.FirebaseStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class LayananTugas {
    private final List<Tugas> daftarTugas;
    private final FirebaseStorage firebaseStorage;
    private int idBerikutnya;
    private static final DateTimeFormatter FORMAT_TENGGAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public LayananTugas(FirebaseStorage firebaseStorage) {
        this.firebaseStorage = firebaseStorage;
        this.daftarTugas = new ArrayList<>(firebaseStorage.muatTugas());
        this.idBerikutnya = daftarTugas.stream().map(Tugas::getId).max(Integer::compareTo).orElse(0) + 1;
    }

    public Tugas tambahTugas(String judul, String deskripsi, LocalDateTime tenggat, Tugas.Prioritas prioritas) {
        Tugas tugas = new Tugas(idBerikutnya++, judul, deskripsi, tenggat, prioritas, false);
        daftarTugas.add(tugas);
        firebaseStorage.simpanTugas(tugas);
        return tugas;
    }

    public boolean ubahTugas(int id, String judul, String deskripsi, LocalDateTime tenggat, Tugas.Prioritas prioritas) {
        Tugas tugas = cariBerdasarkanId(id);
        if (tugas == null) return false;

        tugas.setJudul(judul);
        tugas.setDeskripsi(deskripsi);
        tugas.setTenggat(tenggat);
        tugas.setPrioritas(prioritas);
        firebaseStorage.updateTugas(tugas);
        return true;
    }

    public boolean hapusTugas(int id) {
        boolean berhasilDihapus = daftarTugas.removeIf(t -> t.getId() == id);
        if (berhasilDihapus) {
            firebaseStorage.hapusTugas(id);
        }
        return berhasilDihapus;
    }

    public boolean ubahStatus(int id) {
        Tugas tugas = cariBerdasarkanId(id);
        if (tugas == null) return false;

        tugas.setSelesai(!tugas.isSelesai());
        firebaseStorage.updateTugas(tugas);
        return true;
    }

    public List<Tugas> ambilSemuaTugas() {
        return daftarTugas.stream()
            .sorted(Comparator.comparing(Tugas::getTenggat).thenComparing(Tugas::getPrioritas))
            .collect(Collectors.toList());
    }

    public List<Tugas> saringBerdasarkanStatus(boolean selesai) {
        return daftarTugas.stream()
            .filter(t -> t.isSelesai() == selesai)
            .sorted(Comparator.comparing(Tugas::getTenggat))
            .collect(Collectors.toList());
    }

    public List<Tugas> saringBerdasarkanPrioritas(Tugas.Prioritas prioritas) {
        return daftarTugas.stream()
            .filter(t -> t.getPrioritas() == prioritas)
            .sorted(Comparator.comparing(Tugas::getTenggat))
            .collect(Collectors.toList());
    }

    public Tugas cariBerdasarkanId(int id) {
        for (Tugas tugas : daftarTugas) {
            if (tugas.getId() == id) return tugas;
        }
        return null;
    }

    public List<String> ambilPengingat() {
        LocalDate hariIni = LocalDate.now();
        List<String> daftarPengingat = new ArrayList<>();

        for (Tugas tugas : daftarTugas) {
            if (tugas.isSelesai()) continue;

            long sisaHari = ChronoUnit.DAYS.between(hariIni, tugas.getTenggat().toLocalDate());
            if (sisaHari == 3 || sisaHari == 1 || sisaHari == 0) {
                String penanda = sisaHari == 0 ? "Hari H" : "H-" + sisaHari;
                String tenggatFormat = tugas.getTenggat().format(FORMAT_TENGGAT);
                daftarPengingat.add(String.format("[%s] %s", penanda, tugas.getJudul()));
            }
        }
        return daftarPengingat;
    }
}

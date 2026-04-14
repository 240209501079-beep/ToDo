package com.todoapp.service;

import com.todoapp.model.Tugas;
import com.todoapp.persistence.PengelolaFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LayananTugas {
    private final List<Tugas> daftarTugas;
    private final PengelolaFile pengelolaFile;
    private int idBerikutnya;

    public LayananTugas(PengelolaFile pengelolaFile) {
        this.pengelolaFile = pengelolaFile;
        this.daftarTugas = new ArrayList<>(pengelolaFile.muatTugas());
        this.idBerikutnya = daftarTugas.stream().map(Tugas::getId).max(Integer::compareTo).orElse(0) + 1;
    }

    public Tugas tambahTugas(String judul, String deskripsi, LocalDate tenggat, Tugas.Prioritas prioritas) {
        Tugas tugas = new Tugas(idBerikutnya++, judul, deskripsi, tenggat, prioritas, false);
        daftarTugas.add(tugas);
        simpan();
        return tugas;
    }

    public boolean ubahTugas(int id, String judul, String deskripsi, LocalDate tenggat, Tugas.Prioritas prioritas) {
        Tugas tugas = cariBerdasarkanId(id);
        if (tugas == null) {
            return false;
        }

        tugas.setJudul(judul);
        tugas.setDeskripsi(deskripsi);
        tugas.setTenggat(tenggat);
        tugas.setPrioritas(prioritas);
        simpan();
        return true;
    }

    public boolean hapusTugas(int id) {
        boolean berhasilDihapus = daftarTugas.removeIf(t -> t.getId() == id);
        if (berhasilDihapus) {
            simpan();
        }
        return berhasilDihapus;
    }

    public boolean ubahStatus(int id) {
        Tugas tugas = cariBerdasarkanId(id);
        if (tugas == null) {
            return false;
        }

        tugas.setSelesai(!tugas.isSelesai());
        simpan();
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
            if (tugas.getId() == id) {
                return tugas;
            }
        }
        return null;
    }

    public List<String> ambilPengingat() {
        LocalDate hariIni = LocalDate.now();
        List<String> daftarPengingat = new ArrayList<>();

        for (Tugas tugas : daftarTugas) {
            if (tugas.isSelesai()) {
                continue;
            }

            long sisaHari = ChronoUnit.DAYS.between(hariIni, tugas.getTenggat());
            if (sisaHari == 3 || sisaHari == 1 || sisaHari == 0) {
                String penanda = sisaHari == 0 ? "Hari H" : "H-" + sisaHari;
                daftarPengingat.add(String.format("[%s] #%d %s (Tenggat: %s)", penanda, tugas.getId(), tugas.getJudul(), tugas.getTenggat()));
            }
        }

        return daftarPengingat;
    }

    private void simpan() {
        pengelolaFile.simpanTugas(daftarTugas);
    }
}

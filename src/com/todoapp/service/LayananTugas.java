package com.todoapp.service;

import com.todoapp.model.Tugas;
import com.todoapp.persistence.PengelolaFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LayananTugas {
    // PETA UBAH CEPAT:
    // 1) Urutan default list tugas: ambilSemuaTugas()
    // 2) Logika pengingat: ambilPengingat()
    // 3) Simpan otomatis setelah perubahan: method simpan()
    private final List<Tugas> daftarTugas;
    private final PengelolaFile pengelolaFile;
    private int idBerikutnya;
    private static final DateTimeFormatter FORMAT_TENGGAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public LayananTugas(PengelolaFile pengelolaFile) {
        this.pengelolaFile = pengelolaFile;
        // Data awal dimuat dari file saat aplikasi dibuka.
        this.daftarTugas = new ArrayList<>(pengelolaFile.muatTugas());
        // ID baru selalu mengikuti ID terbesar yang sudah ada.
        this.idBerikutnya = daftarTugas.stream().map(Tugas::getId).max(Integer::compareTo).orElse(0) + 1;
    }

    public Tugas tambahTugas(String judul, String deskripsi, LocalDateTime tenggat, Tugas.Prioritas prioritas) {
        // Method ini dipakai UI saat tombol Tambah ditekan.
        Tugas tugas = new Tugas(idBerikutnya++, judul, deskripsi, tenggat, prioritas, false);
        daftarTugas.add(tugas);
        simpan();
        return tugas;
    }

    public boolean ubahTugas(int id, String judul, String deskripsi, LocalDateTime tenggat, Tugas.Prioritas prioritas) {
        // Cari data dulu, lalu update field yang berubah.
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
        // removeIf dipakai supaya penghapusan berdasarkan ID tetap singkat.
        boolean berhasilDihapus = daftarTugas.removeIf(t -> t.getId() == id);
        if (berhasilDihapus) {
            simpan();
        }
        return berhasilDihapus;
    }

    public boolean ubahStatus(int id) {
        // Status selesai/belum dibuat toggle.
        Tugas tugas = cariBerdasarkanId(id);
        if (tugas == null) {
            return false;
        }

        tugas.setSelesai(!tugas.isSelesai());
        simpan();
        return true;
    }

    public List<Tugas> ambilSemuaTugas() {
        // Sort default: tenggat dulu, lalu prioritas.
        return daftarTugas.stream()
            .sorted(Comparator.comparing(Tugas::getTenggat).thenComparing(Tugas::getPrioritas))
            .collect(Collectors.toList());
    }

    public List<Tugas> saringBerdasarkanStatus(boolean selesai) {
        // Dipakai UI saat filter status dipilih.
        return daftarTugas.stream()
            .filter(t -> t.isSelesai() == selesai)
            .sorted(Comparator.comparing(Tugas::getTenggat))
            .collect(Collectors.toList());
    }

    public List<Tugas> saringBerdasarkanPrioritas(Tugas.Prioritas prioritas) {
        // Dipakai UI saat filter prioritas dipilih.
        return daftarTugas.stream()
            .filter(t -> t.getPrioritas() == prioritas)
            .sorted(Comparator.comparing(Tugas::getTenggat))
            .collect(Collectors.toList());
    }

    public Tugas cariBerdasarkanId(int id) {
        // Helper sederhana supaya UI tidak perlu loop sendiri.
        for (Tugas tugas : daftarTugas) {
            if (tugas.getId() == id) {
                return tugas;
            }
        }
        return null;
    }

    public List<String> ambilPengingat() {
        // Logika pengingat H-3, H-1, dan Hari H.
        LocalDate hariIni = LocalDate.now();
        List<String> daftarPengingat = new ArrayList<>();

        for (Tugas tugas : daftarTugas) {
            if (tugas.isSelesai()) {
                continue;
            }

            long sisaHari = ChronoUnit.DAYS.between(hariIni, tugas.getTenggat().toLocalDate());
            if (sisaHari == 3 || sisaHari == 1 || sisaHari == 0) {
                String penanda = sisaHari == 0 ? "Hari H" : "H-" + sisaHari;
                String tenggatFormat = tugas.getTenggat().format(FORMAT_TENGGAT);
                daftarPengingat.add(String.format("[%s] #%d %s (Tenggat: %s)", penanda, tugas.getId(), tugas.getJudul(), tenggatFormat));
            }
        }

        return daftarPengingat;
    }

    private void simpan() {
        // Semua perubahan data disimpan lewat satu pintu ini.
        pengelolaFile.simpanTugas(daftarTugas);
    }
}

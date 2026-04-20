package com.todoapp.ui;

import java.time.LocalTime;

public final class WaktuSapaan {
    private WaktuSapaan() {
    }

    public static String buatSapaanWaktu() {
        int jam = LocalTime.now().getHour();
        if (jam < 5) {
            return "Malam";
        }
        if (jam < 11) {
            return "Pagi";
        }
        if (jam < 15) {
            return "Siang";
        }
        if (jam < 18) {
            return "Sore";
        }
        return "Malam";
    }
}

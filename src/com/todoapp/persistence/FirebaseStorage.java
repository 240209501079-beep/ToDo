package com.todoapp.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.todoapp.model.Tugas;
import com.todoapp.service.KonfigurasiFirebase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FirebaseStorage {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final String baseUrl;
    private String authToken;

    public FirebaseStorage(String authToken, String userId) {
        this.authToken = authToken;
        this.baseUrl = "https://firestore.googleapis.com/v1/projects/" + KonfigurasiFirebase.FIREBASE_PROJECT_ID
                + "/databases/(default)/documents/users/" + userId + "/tasks";
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public List<Tugas> muatTugas() {
        List<Tugas> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + authToken)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                return list;

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!root.has("documents"))
                return list;

            JsonArray docs = root.getAsJsonArray("documents");
            for (JsonElement el : docs) {
                list.add(mapToTugas(el.getAsJsonObject()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void simpanTugas(Tugas tugas) {
        // Gunakan PATCH ke URL spesifik dokumen untuk 'Upsert' (Create if not exists,
        // else Update)
        String url = baseUrl + "/" + tugas.getId();
        try {
            JsonObject fields = mapFromTugas(tugas);
            JsonObject doc = new JsonObject();
            doc.add("fields", fields);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(doc.toString()))
                    .build();

            // Jalankan secara asinkron agar UI tidak lag
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() >= 400) {
                            System.err.println(
                                    "Gagal sinkronisasi Firestore (HTTP " + res.statusCode() + "): " + res.body());
                        } else {
                            System.out.println("DEBUG: Tugas #" + tugas.getId() + " berhasil disinkronkan ke cloud.");
                        }
                    }).exceptionally(ex -> {
                        System.err.println("ERROR Jaringan saat sinkronisasi: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTugas(Tugas tugas) {
        simpanTugas(tugas); // Logikanya sama karena menggunakan PATCH (Upsert)
    }

    public void hapusTugas(int id) {
        String url = baseUrl + "/" + id;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .DELETE()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() >= 400) {
                            System.err.println(
                                    "DEBUG [DEL]: Gagal hapus di Cloud (HTTP " + res.statusCode() + "): " + res.body());
                        } else {
                            System.out.println("DEBUG [DEL]: Tugas #" + id + " berhasil dihapus dari Cloud.");
                        }
                    }).exceptionally(ex -> {
                        System.err.println("DEBUG [DEL-ERR]: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Tugas mapToTugas(JsonObject doc) {
        String name = doc.get("name").getAsString();
        String idStr = name.substring(name.lastIndexOf("/") + 1);
        JsonObject f = doc.getAsJsonObject("fields");

        String judul = f.has("judul") ? f.getAsJsonObject("judul").get("stringValue").getAsString() : "Tanpa Judul";
        String desc = f.has("deskripsi") ? f.getAsJsonObject("deskripsi").get("stringValue").getAsString() : "";
        String tenggatStr = f.has("tenggat") ? f.getAsJsonObject("tenggat").get("stringValue").getAsString()
                : LocalDateTime.now().toString();
        String prioStr = f.has("prioritas") ? f.getAsJsonObject("prioritas").get("stringValue").getAsString()
                : "SEDANG";
        boolean selesai = f.has("selesai") && f.getAsJsonObject("selesai").get("booleanValue").getAsBoolean();

        return new Tugas(
                Integer.parseInt(idStr),
                judul,
                desc,
                LocalDateTime.parse(tenggatStr),
                Tugas.Prioritas.valueOf(prioStr),
                selesai);
    }

    private JsonObject mapFromTugas(Tugas t) {
        JsonObject f = new JsonObject();
        f.add("judul", createStringField(t.getJudul()));
        f.add("deskripsi", createStringField(t.getDeskripsi()));
        f.add("tenggat", createStringField(t.getTenggat().toString()));
        f.add("prioritas", createStringField(t.getPrioritas().name()));
        f.add("selesai", createBooleanField(t.isSelesai()));
        return f;
    }

    private JsonObject createStringField(String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("stringValue", value);
        return obj;
    }

    private JsonObject createBooleanField(boolean value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("booleanValue", value);
        return obj;
    }
}

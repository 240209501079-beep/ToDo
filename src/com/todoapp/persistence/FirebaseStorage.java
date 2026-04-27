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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FirebaseStorage {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl;
    private String authToken;
    private final String userId;

    public FirebaseStorage(String authToken, String userId) {
        this.authToken = authToken;
        this.userId = userId;
        this.baseUrl = "https://firestore.googleapis.com/v1/projects/" + KonfigurasiFirebase.FIREBASE_PROJECT_ID + "/databases/(default)/documents/users/" + userId + "/tasks";
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
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return list;

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!root.has("documents")) return list;

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
        try {
            JsonObject fields = mapFromTugas(tugas);
            JsonObject doc = new JsonObject();
            doc.add("fields", fields);

            // Kita gunakan ID tugas sebagai document ID di Firestore
            String url = baseUrl + "?documentId=" + tugas.getId();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(doc.toString()))
                    .build();

            // Gunakan PATCH jika dokumen sudah ada (UPSERT)
            // Namun REST API Firebase untuk POST dengan documentId hanya untuk dokumen baru.
            // Untuk update kita gunakan PATCH ke URL spesifik dokumen.
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 409) { // Conflict, already exists
                updateTugas(tugas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTugas(Tugas tugas) {
        try {
            JsonObject fields = mapFromTugas(tugas);
            JsonObject doc = new JsonObject();
            doc.add("fields", fields);

            String url = baseUrl + "/" + tugas.getId();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(doc.toString()))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hapusTugas(int id) {
        try {
            String url = baseUrl + "/" + id;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Tugas mapToTugas(JsonObject doc) {
        String name = doc.get("name").getAsString();
        String idStr = name.substring(name.lastIndexOf("/") + 1);
        JsonObject f = doc.getAsJsonObject("fields");
        
        return new Tugas(
                Integer.parseInt(idStr),
                f.getAsJsonObject("judul").get("stringValue").getAsString(),
                f.getAsJsonObject("deskripsi").get("stringValue").getAsString(),
                LocalDateTime.parse(f.getAsJsonObject("tenggat").get("stringValue").getAsString()),
                Tugas.Prioritas.valueOf(f.getAsJsonObject("prioritas").get("stringValue").getAsString()),
                f.getAsJsonObject("selesai").get("booleanValue").getAsBoolean()
        );
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

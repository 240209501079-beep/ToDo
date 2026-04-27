package com.todoapp.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CompletableFuture<JsonObject> loginWithGoogle() {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(KonfigurasiFirebase.REDIRECT_PORT), 0);

            server.createContext("/", exchange -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String code = null;
                    if (query != null && query.contains("code=")) {
                        code = query.split("code=")[1].split("&")[0];
                    }

                    String responseBody = "<!DOCTYPE html><html><head><title>Login Berhasil</title>" +
                            "<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap' rel='stylesheet'>" +
                            "<style>" +
                            "  :root { --bg: #020617; --primary: #38bdf8; --text-main: #f8fafc; --text-dim: #94a3b8; }" +
                            "  body { background: var(--bg); color: var(--text-main); font-family: 'Inter', sans-serif; " +
                            "         display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; overflow: hidden; text-align: center; }" +
                            "  .glow { position: absolute; width: 400px; height: 400px; background: radial-gradient(circle, rgba(56, 189, 248, 0.12) 0%, transparent 70%); z-index: -1; }" +
                            "  @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }" +
                            "  .content { animation: fadeInUp 0.8s ease-out; z-index: 1; padding: 20px; }" +
                            "  h1 { color: var(--primary); margin: 0 0 16px 0; font-size: 32px; font-weight: 700; letter-spacing: -1px; }" +
                            "  p { color: var(--text-dim); line-height: 1.6; font-size: 18px; max-width: 450px; margin: 0 auto; }" +
                            "  .footer { margin-top: 40px; font-size: 13px; color: #475569; font-weight: 500; text-transform: uppercase; letter-spacing: 1px; }" +
                            "</style></head><body>" +
                            "  <div class='glow'></div>" +
                            "  <div class='content'>" +
                            "    <h1>Login Berhasil!</h1>" +
                            "    <p>Otentikasi berhasil. Anda dapat menutup tab ini dan kembali melanjutkan aktivitas di aplikasi desktop.</p>" +
                            "    <div class='footer'>Bisa tutup page ini sekarang</div>" +
                            "  </div>" +
                            "  <script src='https://cdn.jsdelivr.net/npm/canvas-confetti@1.6.0/dist/confetti.browser.min.js'></script>" +
                            "  <script>" +
                            "    window.onload = () => {" +
                            "      confetti({ particleCount: 80, spread: 60, origin: { y: 0.7 }, colors: ['#38bdf8', '#ffffff'] });" +
                            "    };" +
                            "  </script>" +
                            "</body></html>";

                    byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);
                    exchange.close();

                    if (code != null) {
                        final String finalCode = code;
                        // Hentikan server di thread terpisah agar respon HTTP terkirim dulu
                        new Thread(() -> {
                            try { Thread.sleep(500); } catch (Exception ex) {}
                            server.stop(0);
                            handleGoogleCode(finalCode, future);
                        }).start();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    server.stop(0);
                }
            });

            server.start();

            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=" + KonfigurasiFirebase.GOOGLE_CLIENT_ID +
                    "&redirect_uri=" + URLEncoder.encode(KonfigurasiFirebase.REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode("email profile openid", StandardCharsets.UTF_8);

            Desktop.getDesktop().browse(new URI(authUrl));

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private void handleGoogleCode(String code, CompletableFuture<JsonObject> future) {
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";
            String params = "code=" + code +
                    "&client_id=" + KonfigurasiFirebase.GOOGLE_CLIENT_ID +
                    "&client_secret=" + KonfigurasiFirebase.GOOGLE_CLIENT_SECRET +
                    "&redirect_uri=" + URLEncoder.encode(KonfigurasiFirebase.REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject googleJson = JsonParser.parseString(response.body()).getAsJsonObject();

            if (googleJson.has("id_token")) {
                String idToken = googleJson.get("id_token").getAsString();
                exchangeForFirebase(idToken, future);
            } else {
                future.completeExceptionally(new RuntimeException("Gagal mendapatkan ID Token dari Google"));
            }

        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    private void exchangeForFirebase(String googleIdToken, CompletableFuture<JsonObject> future) {
        try {
            String firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key="
                    + KonfigurasiFirebase.FIREBASE_API_KEY;

            JsonObject body = new JsonObject();
            body.addProperty("postBody", "id_token=" + googleIdToken + "&providerId=google.com");
            body.addProperty("requestUri", KonfigurasiFirebase.REDIRECT_URI);
            body.addProperty("returnIdpCredential", true);
            body.addProperty("returnSecureToken", true);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(firebaseAuthUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            future.complete(JsonParser.parseString(response.body()).getAsJsonObject());

        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    public CompletableFuture<JsonObject> refreshFirebaseToken(String refreshToken) {
        System.out.println("DEBUG [Auth]: Memulai request refresh token...");
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        try {
            String url = "https://securetoken.googleapis.com/v1/token?key=" + KonfigurasiFirebase.FIREBASE_API_KEY;
            String params = "grant_type=refresh_token&refresh_token=" + refreshToken;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            System.err.println("DEBUG [Auth-ERR]: Server merespon dengan status " + response.statusCode());
                            // Lempar exception agar masuk ke exceptionally, bukan thenAccept
                            throw new RuntimeException("TOKEN_EXPIRED: status=" + response.statusCode());
                        }
                        return JsonParser.parseString(response.body()).getAsJsonObject();
                    })
                    .thenAccept(future::complete)
                    .exceptionally(ex -> {
                        System.err.println("DEBUG [Auth-Fatal]: " + ex.getMessage());
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("DEBUG [Auth-Catch]: Terjadi error saat menyiapkan request: " + e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }
}

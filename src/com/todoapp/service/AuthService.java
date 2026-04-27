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
                String query = exchange.getRequestURI().getQuery();
                String code = null;
                if (query != null && query.contains("code=")) {
                    code = query.split("code=")[1].split("&")[0];
                }
                
                String responseBody = "<html><body><h1>Login Berhasil!</h1><p>Anda bisa menutup jendela ini dan kembali ke aplikasi.</p></body></html>";
                exchange.sendResponseHeaders(200, responseBody.length());
                exchange.getResponseBody().write(responseBody.getBytes());
                exchange.close();
                
                if (code != null) {
                    server.stop(0);
                    handleGoogleCode(code, future);
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
            // 1. Exchange code for Google ID Token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            String params = "code=" + code +
                    "&client_id=" + KonfigurasiFirebase.GOOGLE_CLIENT_ID +
                    "&redirect_uri=" + URLEncoder.encode(KonfigurasiFirebase.REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject googleJson = JsonParser.parseString(response.body()).getAsJsonObject();
            String idToken = googleJson.get("id_token").getAsString();

            // 2. Exchange Google ID Token for Firebase Token
            exchangeForFirebase(idToken, future);

        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    private void exchangeForFirebase(String googleIdToken, CompletableFuture<JsonObject> future) {
        try {
            String firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=" + KonfigurasiFirebase.FIREBASE_API_KEY;
            
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
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        try {
            String url = "https://securetoken.googleapis.com/v1/token?key=" + KonfigurasiFirebase.FIREBASE_API_KEY;
            String params = "grant_type=refresh_token&refresh_token=" + refreshToken;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> JsonParser.parseString(res.body()).getAsJsonObject())
                    .thenAccept(future::complete)
                    .exceptionally(ex -> { future.completeExceptionally(ex); return null; });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}

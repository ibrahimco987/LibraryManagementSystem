package com.library.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.io.IOException;

public class SupabaseClient {

    private final String url;
    private final String apiKey;
    private final HttpClient client;

    public SupabaseClient() {

        url = System.getenv("SUPABASE_URL");
        apiKey = System.getenv("SUPABASE_KEY");

        if (url == null || apiKey == null
                || url.isBlank() || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Supabase configuration is missing."
            );
        }

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    private String getErrorMessage(int status) {

    switch (status) {

        case 400:
            return "Invalid request or credentials.";

        case 401:
            return "Authentication failed. Please log in.";

        case 403:
            return "Access denied.";

        case 404:
            return "Requested resource not found.";

        case 409:
            return "Record conflict. Check for duplicate data.";

        case 429:
            return "Too many requests. Please try again later.";

        default:
            return "Supabase request failed. HTTP status: " + status;
    }
}

    public String request(String method, String path,
                          String body, String token)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url + path))
                .timeout(Duration.ofSeconds(15))
                .header("apikey", apiKey)
                .header("Content-Type", "application/json");

        if (path.startsWith("/rest/v1/")
        && !method.equals("GET")) {

    builder.header("Prefer", "return=representation");
}        
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        if (body == null) {
            builder.method(method,
                    HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(method,
                    HttpRequest.BodyPublishers.ofString(body));
        }

        HttpResponse<String> response = client.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
        || response.statusCode() >= 300) {

    throw new IOException(
            getErrorMessage(response.statusCode())
    );
}

        return response.body();
    }
}
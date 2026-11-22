package com.library.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.io.IOException;

/**
 * Sends synchronous HTTP requests to Supabase with bounded timeouts and sanitized HTTP error messages.
 *
 * @author Ibrahim Muhammad Abbas and Maaz
 */
public class SupabaseClient {

    private final String url;
    private final String apiKey;
    private final HttpClient client;

    /**
     * Loads application configuration and creates an HTTP client with a ten-second connection timeout.
     */
    public SupabaseClient() {

        AppConfig config = AppConfig.load();
        url = config.url();
        apiKey = config.key();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    /**
     * Maps an HTTP status to a credential-safe error message.
     * @param status book status
     * @return the user-readable error description
     */
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

    /**
     * Sends a synchronous request with the client key and optional bearer token. Non-success HTTP responses become sanitized IOExceptions.
     * @param method HTTP method
     * @param path Supabase endpoint path including query parameters
     * @param body JSON request body, or null
     * @param token bearer token, or null for unauthenticated endpoints
     * @return the successful response body
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
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
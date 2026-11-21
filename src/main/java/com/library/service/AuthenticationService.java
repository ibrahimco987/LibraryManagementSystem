package com.library.service;

import com.library.config.SupabaseClient;
import com.library.model.Librarian;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

/**
 * Handles librarian authentication, session management
 * and access token renewal using Supabase Auth.
 *
 * @author Ibrahim Muhammad Abbas
 * @version 1.0
 */
public class AuthenticationService {

    private final SupabaseClient client;
    private final Gson gson;

    private String accessToken;
    private String refreshToken;
    private long expiresAt;

    private Librarian currentLibrarian;

    public AuthenticationService(SupabaseClient client) {
        this.client = client;
        this.gson = new Gson();
    }

    /**
 * Authenticates a librarian using an email and password.
 *
 * @param email registered email address
 * @param password librarian password
 * @return the authenticated librarian profile
 * @throws IOException if authentication or the request fails
 * @throws InterruptedException if the request is interrupted
 * @throws IllegalArgumentException if credentials are empty
 * @throws IllegalStateException if the user is unauthorized
 *         or already logged in
 */
    public synchronized Librarian login(String email, String password)
            throws IOException, InterruptedException {

        if (isLoggedIn()) {
            throw new IllegalStateException("Already logged in.");
        }

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Email and password are required."
            );
        }

        JsonObject credentials = new JsonObject();

        credentials.addProperty("email", email.trim());
        credentials.addProperty("password", password);

        String response = client.request(
                "POST",
                "/auth/v1/token?grant_type=password",
                credentials.toString(),
                null
        );

        JsonObject data = JsonParser.parseString(response)
                .getAsJsonObject();

        String token = data.get("access_token").getAsString();

        String userId = data.getAsJsonObject("user")
                .get("id").getAsString();

        String profileResponse = client.request(
                "GET",
                "/rest/v1/librarians?id=eq." + userId,
                null,
                token
        );

        Librarian[] librarians = gson.fromJson(
                profileResponse,
                Librarian[].class
        );

        if (librarians.length == 0) {
            throw new IllegalStateException(
                    "User is not an authorized librarian."
            );
        }

        saveSession(data);
        currentLibrarian = librarians[0];

        return currentLibrarian;
    }

    private void saveSession(JsonObject data) {

        accessToken = data.get("access_token").getAsString();
        refreshToken = data.get("refresh_token").getAsString();

        expiresAt = System.currentTimeMillis()
                + data.get("expires_in").getAsLong() * 1000;
    }

    public synchronized String getAccessToken()
            throws IOException, InterruptedException {

        if (!isLoggedIn()) {
            throw new IllegalStateException("Please log in first.");
        }

        if (System.currentTimeMillis() >= expiresAt - 30000) {

            JsonObject body = new JsonObject();

            body.addProperty("refresh_token", refreshToken);

            try {
                String response = client.request(
                        "POST",
                        "/auth/v1/token?grant_type=refresh_token",
                        body.toString(),
                        null
                );

                saveSession(JsonParser.parseString(response)
                        .getAsJsonObject());

            } catch (IOException | InterruptedException
                     | RuntimeException e) {

                clearSession();
                throw e;
            }
        }

        return accessToken;
    }

    /**
 * Terminates the current authentication session
 * and clears local session information.
 *
 * @throws IOException if the logout request fails
 * @throws InterruptedException if the request is interrupted
 */
    public synchronized void logout()
            throws IOException, InterruptedException {

        try {
            if (isLoggedIn()) {

                client.request(
                        "POST",
                        "/auth/v1/logout?scope=local",
                        null,
                        getAccessToken()
                );
            }
        } finally {
            clearSession();
        }
    }

    private void clearSession() {
        accessToken = null;
        refreshToken = null;
        expiresAt = 0;
        currentLibrarian = null;
    }

    public synchronized boolean isLoggedIn() {
        return currentLibrarian != null;
    }

    public synchronized Librarian getCurrentLibrarian() {
        return currentLibrarian;
    }
}
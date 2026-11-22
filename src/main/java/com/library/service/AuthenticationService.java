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

    /**
     * Creates an initially signed-out authentication service for the configured client.
     * @param client configured Supabase HTTP client
     */
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

    /**
     * Stores access and refresh tokens and computes their expiry time in memory.
     * @param data Supabase token response
     */
    private void saveSession(JsonObject data) {

        accessToken = data.get("access_token").getAsString();
        refreshToken = data.get("refresh_token").getAsString();

        expiresAt = System.currentTimeMillis()
                + data.get("expires_in").getAsLong() * 1000;
    }

    /**
     * Returns the session token, refreshing it within 30 seconds of expiry. Refresh failures clear the local session.
     * @return the current access token
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     * @throws IllegalStateException if no librarian is logged in
     */
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

    /**
     * Clears tokens, expiry and the current librarian profile.
     */
    private void clearSession() {
        accessToken = null;
        refreshToken = null;
        expiresAt = 0;
        currentLibrarian = null;
    }

    /**
     * Checks whether an authenticated librarian profile is held locally.
     * @return true when a librarian profile is present
     */
    public synchronized boolean isLoggedIn() {
        return currentLibrarian != null;
    }

    /**
     * Reads the current in-memory librarian profile.
     * @return the current librarian, or null when signed out
     */
    public synchronized Librarian getCurrentLibrarian() {
        return currentLibrarian;
    }
}
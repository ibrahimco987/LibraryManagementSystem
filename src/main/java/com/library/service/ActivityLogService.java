package com.library.service;

import com.library.config.SupabaseClient;
import com.library.model.ActivityLog;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Retrieves the latest database-generated activity events for an authenticated librarian.
 *
 * @author Ibrahim Muhammad Abbas
 */
public class ActivityLogService {

    private final SupabaseClient client;
    private final AuthenticationService auth;
    private final Gson gson;

    /**
     * Connects activity retrieval to the configured client and librarian session.
     * @param client configured Supabase HTTP client
     * @param auth authentication service for the active librarian
     */
    public ActivityLogService(SupabaseClient client,
                              AuthenticationService auth) {

        this.client = client;
        this.auth = auth;
        this.gson = new Gson();
    }

    /**
     * Fetches at most 100 recent activity events ordered by descending ID.
     * @return the latest activity entries
     * @throws IOException if the HTTP request fails or Supabase rejects it
     * @throws InterruptedException if the request is interrupted
     */
    public List<ActivityLog> getActivityLogs()
            throws IOException, InterruptedException {

        String response = client.request(
                "GET",
                "/rest/v1/activity_logs?order=id.desc&limit=100",
                null,
                auth.getAccessToken()
        );

        ActivityLog[] logs = gson.fromJson(
                response, ActivityLog[].class
        );

        return Arrays.asList(logs);
    }
}
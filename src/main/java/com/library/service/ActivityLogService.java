package com.library.service;

import com.library.config.SupabaseClient;
import com.library.model.ActivityLog;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ActivityLogService {

    private final SupabaseClient client;
    private final AuthenticationService auth;
    private final Gson gson;

    public ActivityLogService(SupabaseClient client,
                              AuthenticationService auth) {

        this.client = client;
        this.auth = auth;
        this.gson = new Gson();
    }

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
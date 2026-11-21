package com.library;

import com.library.config.SupabaseClient;

public class App {

    public static void main(String[] args) {

        try {
            SupabaseClient client = new SupabaseClient();

            client.request(
                "GET",
                "/auth/v1/settings",
                null,
                null
            );

            System.out.println("Connected to Supabase successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
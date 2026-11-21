package com.library;

import com.library.config.SupabaseClient;
import com.library.service.AuthenticationService;
import com.library.model.Librarian;

import java.io.Console;

public class App {

    public static void main(String[] args) {

        try {
            SupabaseClient client = new SupabaseClient();

            AuthenticationService auth =
                    new AuthenticationService(client);

            Console console = System.console();

            if (console == null) {
                System.out.println("Run the application from a terminal.");
                return;
            }

            String email = console.readLine("Enter email: ");

            String password = new String(
                    console.readPassword("Enter password: ")
            );

            Librarian librarian = auth.login(email, password);

            System.out.println("Login successful!");
            System.out.println("Welcome, " + librarian.getName());

            System.out.println("Logged in: " + auth.isLoggedIn());

            auth.logout();

            System.out.println("Logout successful!");
            System.out.println("Logged in: " + auth.isLoggedIn());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
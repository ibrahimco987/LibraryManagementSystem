package com.library.ui;

import com.library.config.SupabaseClient;
import com.library.service.AuthenticationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Set;

public final class LoginController {
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private Button submit;
    @FXML private ProgressIndicator progress;
    @FXML private Label error;
    private LibraryFxApp app;

    void configure(LibraryFxApp app, String message) { this.app = app; error.setText(message); }

    @FXML private void signIn() {
        if (submit.isDisabled()) return;
        String address = email.getText().trim();
        String secret = password.getText();
        if (address.isBlank() || secret.isBlank()) {
            error.setText("Email and password are required."); return;
        }
        password.clear();
        busy(true);
        error.setText("");
        Task<AuthenticationService> task = new Task<>() {
            @Override protected AuthenticationService call() throws Exception {
                AuthenticationService auth = new AuthenticationService(new SupabaseClient());
                auth.login(address, secret);
                return auth;
            }
        };
        task.setOnSucceeded(event -> {
            busy(false);
            try { app.signedIn(task.getValue()); }
            catch (Exception failure) {
                error.setText("Unable to open the workspace. Please try again.");
                app.execute(() -> { try { task.getValue().logout(); } catch (Exception ignored) {} });
            }
        });
        task.setOnFailed(event -> { busy(false); error.setText(safeError(task.getException())); });
        app.execute(task);
    }

    private void busy(boolean busy) {
        submit.setDisable(busy); email.setDisable(busy); password.setDisable(busy);
        progress.setVisible(busy); progress.setManaged(busy);
    }

    private String safeError(Throwable failure) {
        String message = failure.getMessage();
        if (message != null && (message.startsWith("Missing SUPABASE_") || message.startsWith("Cannot read .env.")))
            return message;
        if (Set.of("User is not an authorized librarian.", "Invalid request or credentials.",
                "Authentication failed. Please log in.", "Access denied.",
                "Too many requests. Please try again later.").contains(message == null ? "" : message)) return message;
        return "Unable to sign in. Check your connection and Supabase configuration, then try again.";
    }
}

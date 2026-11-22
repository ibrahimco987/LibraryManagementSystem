package com.library.ui;

import com.library.service.AuthenticationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Owns the window, background worker and in-memory authenticated session. */
public final class LibraryFxApp extends Application {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "library-requests");
        thread.setDaemon(true);
        return thread;
    });
    private Stage stage;
    private volatile AuthenticationService session;
    private volatile boolean stopped;

    @Override public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("Library Desk");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        showLogin("");
        stage.show();
    }

    private void display(Parent root) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
            stage.setScene(scene);
        } else stage.getScene().setRoot(root);
    }

    void showLogin(String message) throws IOException {
        session = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        ((LoginController) loader.getController()).configure(this, message);
        display(root);
    }

    void signedIn(AuthenticationService auth) throws IOException {
        if (stopped) return;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("shell.fxml"));
        Parent root = loader.load();
        session = auth;
        ((ShellController) loader.getController()).configure(this, auth);
        display(root);
    }

    void signOut() throws IOException {
        AuthenticationService old = session;
        showLogin("");
        executor.submit(() -> {
            try { if (old != null) old.logout(); }
            catch (Exception ignored) { /* Backend clears local session in finally. */ }
        });
    }

    boolean current(AuthenticationService auth) { return !stopped && session == auth; }

    boolean expired(AuthenticationService auth, Throwable failure) {
        String message = failure.getMessage();
        if (!auth.isLoggedIn() || "Authentication failed. Please log in.".equals(message)
                || "Please log in first.".equals(message)) {
            try { signOut(); showLogin("Your session expired. Please sign in again."); }
            catch (IOException ignored) { stage.close(); }
            return true;
        }
        return false;
    }

    void execute(Runnable task) { executor.execute(task); }
    @Override public void stop() { stopped = true; session = null; executor.shutdownNow(); }
    public static void main(String[] args) { launch(args); }
}

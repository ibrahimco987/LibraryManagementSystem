package com.library.ui;

import com.library.service.AuthenticationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import java.util.Objects;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Owns the window, background worker and in-memory authenticated session.
 * @author Maaz
 */
public final class LibraryFxApp extends Application {
    /** Application name shared by the JavaFX window and project documentation. */
    public static final String APP_NAME = "Library Desk";

    /** Creates the JavaFX application and its background executor. */
    public LibraryFxApp() {}

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "library-requests");
        thread.setDaemon(true);
        return thread;
    });
    private Stage stage;
    private volatile AuthenticationService session;
    private volatile boolean stopped;

    /**
     * Creates the primary window and displays the login screen.
     * @param stage primary JavaFX window
     * @throws IOException if a required FXML resource cannot be loaded
     */
    @Override public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle(APP_NAME);
        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("app-icon.png"), "Application icon is missing.")));
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        showLogin("");
        stage.show();
    }

    /**
     * Creates the scene once or replaces its root while preserving window size.
     * @param root new scene root
     */
    private void display(Parent root) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
            stage.setScene(scene);
        } else stage.getScene().setRoot(root);
    }

    /**
     * Clears the application session reference and replaces the scene with the login view.
     * @param message user-facing login notice
     * @throws IOException if a required FXML resource cannot be loaded
     */
    void showLogin(String message) throws IOException {
        session = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        ((LoginController) loader.getController()).configure(this, message);
        display(root);
    }

    /**
     * Loads the authenticated shell and attaches the successful session.
     * @param auth authentication service for the active librarian
     * @throws IOException if a required FXML resource cannot be loaded
     */
    void signedIn(AuthenticationService auth) throws IOException {
        if (stopped) return;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("shell.fxml"));
        Parent root = loader.load();
        session = auth;
        ((ShellController) loader.getController()).configure(this, auth);
        display(root);
    }

    /**
     * Returns to login and schedules backend logout, whose finally block clears local tokens.
     * @throws IOException if a required FXML resource cannot be loaded
     */
    void signOut() throws IOException {
        AuthenticationService old = session;
        showLogin("");
        executor.submit(() -> {
            try { if (old != null) old.logout(); }
            catch (Exception ignored) { /* Backend clears local session in finally. */ }
        });
    }

    /**
     * Checks session identity and application lifetime without a server request.
     * @param auth authentication service for the active librarian
     * @return true when this is the active application session
     */
    boolean current(AuthenticationService auth) { return !stopped && session == auth; }

    /**
     * Returns to login when a failed request indicates an unusable authenticated session.
     * @param auth authentication service for the active librarian
     * @param failure request failure to inspect
     * @return true when the session was treated as expired
     */
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

    /**
     * Queues work on the owned single background executor.
     * @param task background work to queue
     */
    void execute(Runnable task) { executor.execute(task); }
    /**
     * Clears the application session reference and interrupts the owned worker.
     */
    @Override public void stop() { stopped = true; session = null; executor.shutdownNow(); }
    /**
     * Starts the JavaFX application.
     * @param args command-line arguments
     */
    public static void main(String[] args) { launch(args); }
}

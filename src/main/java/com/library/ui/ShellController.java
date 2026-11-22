package com.library.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;
import java.util.List;

public final class ShellController {
    @FXML private Label librarian;
    @FXML private Label heading;
    @FXML private Label description;
    @FXML private Button dashboard;
    @FXML private Button books;
    @FXML private Button activity;
    private LibraryFxApp app;

    void configure(LibraryFxApp app, String name) {
        this.app = app;
        librarian.setText(name == null || name.isBlank() ? "Librarian" : name);
        dashboard();
    }
    private void select(Button selected, String title, String text) {
        for (Button button : List.of(dashboard, books, activity)) {
            button.getStyleClass().remove("active");
            if (button == selected) button.getStyleClass().add("active");
        }
        heading.setText(title); description.setText(text);
    }
    @FXML private void dashboard() { select(dashboard, "Dashboard", "Library statistics and recent books will be available in Phase 2."); }
    @FXML private void books() { select(books, "Book Management", "Book search and management will be available in Phase 2."); }
    @FXML private void activity() { select(activity, "Activity Logs", "Read-only activity history will be available in Phase 3."); }
    @FXML private void signOut() {
        try { app.signOut(); }
        catch (IOException failure) { description.setText("Unable to open the login screen. Please close the application."); }
    }
}

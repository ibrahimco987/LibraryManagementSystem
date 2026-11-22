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
    @FXML private javafx.scene.layout.StackPane workspace;
    private javafx.scene.Parent workspaceRoot;
    private WorkspaceController controller;
    private javafx.scene.Parent activityRoot;
    private ActivityController activityController;
    private LibraryFxApp app;

    void configure(LibraryFxApp app, com.library.service.AuthenticationService auth) throws IOException {
        String name = auth.getCurrentLibrarian().getName();
        this.app = app;
        librarian.setText(name == null || name.isBlank() ? "Librarian" : name);
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("workspace.fxml"));
        workspaceRoot = loader.load();
        controller = loader.getController();
        controller.configure(app, auth);
        javafx.fxml.FXMLLoader activityLoader = new javafx.fxml.FXMLLoader(getClass().getResource("activity.fxml"));
        activityRoot = activityLoader.load();
        activityController = activityLoader.getController();
        activityController.configure(app, auth);
        dashboard();
    }
    private void select(Button selected, String title, String text) {
        for (Button button : List.of(dashboard, books, activity)) {
            button.getStyleClass().remove("active");
            if (button == selected) button.getStyleClass().add("active");
        }
        heading.setText(title); description.setText(text);
        description.setVisible(false); description.setManaged(false);
        workspace.getChildren().setAll(selected == activity ? activityRoot : workspaceRoot);
        if (selected == activity) activityController.refresh();
        else controller.show(selected == dashboard);
    }
    @FXML private void dashboard() { select(dashboard, "Dashboard", "Library overview."); }
    @FXML private void books() { select(books, "Book Management", "Manage your collection."); }
    @FXML private void activity() { select(activity, "Activity Logs", "Recent collection changes."); }
    @FXML private void signOut() {
        try { app.signOut(); }
        catch (IOException failure) { description.setText("Unable to open the login screen. Please close the application."); }
    }
}

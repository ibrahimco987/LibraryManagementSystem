package com.library.ui;
import com.library.config.SupabaseClient;
import com.library.model.ActivityLog;
import com.library.service.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.ZoneId;
import java.util.*;
/**
 * Displays read-only activity history and refreshes it through background tasks guarded by the active session.
 *
 * @author Maaz
 */
public final class ActivityController {
    /** Creates a controller for FXML loading. */
    public ActivityController() {}

    @FXML private TableView<ActivityLog> table;
    @FXML private Button refresh;
    @FXML private Label message;
    @FXML private ProgressIndicator progress;
    private LibraryFxApp app;
    private AuthenticationService auth;
    private ActivityLogService service;
    /**
     * Connects the controller to its application and authenticated services.
     * @param app application owning the session and executor
     * @param auth authentication service for the active librarian
     */
    void configure(LibraryFxApp app, AuthenticationService auth) {
        this.app=app; this.auth=auth;
        service=new ActivityLogService(new SupabaseClient(),auth);
        column("Action", l -> ActivityFormat.action(l.getAction()),150);
        column("Book ID", l -> l.getBookId()==null ? "—" : l.getBookId().toString(),90);
        column("Librarian", l -> l.getLibrarianName()==null || l.getLibrarianName().isBlank()
            ? Objects.toString(l.getLibrarianId(),"—") : l.getLibrarianName(),210);
        column("Time (" + ZoneId.systemDefault() + ")", l -> ActivityFormat.timestamp(l.getCreatedAt(),ZoneId.systemDefault()),240);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No activity yet."));
    }
    /**
     * Creates a read-only text column using the supplied value formatter.
     * @param name setting name, column heading or librarian name
     * @param format column text formatter
     * @param width preferred column width in pixels
     */
    private void column(String name, java.util.function.Function<ActivityLog,String> format,int width) {
        TableColumn<ActivityLog,String> col=new TableColumn<>(name); col.setPrefWidth(width);
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(format.apply(c.getValue()))); table.getColumns().add(col);
    }
    /**
     * Loads current server state asynchronously and ignores results from an obsolete session.
     */
    @FXML void refresh() {
        if (refresh.isDisabled() || !app.current(auth)) return;
        busy(true); message.setText("Loading recent activity…");
        Task<List<ActivityLog>> task=new Task<>() {
            protected List<ActivityLog> call() throws Exception {
                if (!app.current(auth)) throw new InterruptedException();
                return service.getActivityLogs().stream().sorted(Comparator.comparingLong(ActivityLog::getId).reversed()).toList();
            }
        };
        task.setOnSucceeded(e -> { if (!app.current(auth)) return; busy(false); table.getItems().setAll(task.getValue());
            message.setText("Latest " + task.getValue().size() + " events · read-only · maximum 100"); });
        task.setOnFailed(e -> { if (!app.current(auth)) return; busy(false);
            if (app.expired(auth,task.getException())) return;
            message.setText("Unable to load activity. Check your connection and permissions, then refresh. Previously loaded rows may be out of date."); });
        app.execute(task);
    }
    /**
     * Updates controls to reflect an in-progress operation.
     * @param busy whether an operation is running
     */
    private void busy(boolean busy) { refresh.setDisable(busy); progress.setVisible(busy); progress.setManaged(busy); }
}

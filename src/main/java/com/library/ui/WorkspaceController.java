package com.library.ui;

import com.library.config.SupabaseClient;
import com.library.model.*;
import com.library.repository.BookRepository;
import com.library.service.*;
import com.library.util.InputValidator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.concurrent.Callable;

/** Shared collection state keeps dashboard and book management consistent.
 * @author Maaz
 */
public final class WorkspaceController {
    /** Creates a controller for FXML loading. */
    public WorkspaceController() {}

    @FXML private VBox root;
    @FXML private HBox stats;
    @FXML private HBox filters;
    @FXML private TextField search;
    @FXML private ComboBox<String> status;
    @FXML private Label message;
    @FXML private Label count;
    @FXML private Label recentActivity;
    @FXML private TableView<Book> table;
    private LibraryFxApp app;
    private AuthenticationService auth;
    private BookService service;
    private ActivityLogService logs;
    private List<Book> collection = List.of();
    private boolean dashboard;
    private boolean busy;

    /**
     * Connects the controller to its application and authenticated services.
     * @param app application owning the session and executor
     * @param auth authentication service for the active librarian
     */
    void configure(LibraryFxApp app, AuthenticationService auth) {
        this.app = app; this.auth = auth;
        SupabaseClient client = new SupabaseClient();
        service = new BookService(new BookRepository(client, auth));
        logs = new ActivityLogService(client, auth);
        status.getItems().setAll("All statuses", "Available", "Issued", "Reserved"); status.setValue("All statuses");
        search.textProperty().addListener((o,a,b) -> render());
        status.valueProperty().addListener((o,a,b) -> render());
        column("Book ID", b -> String.valueOf(b.getId()), 80);
        column("Title", Book::getTitle, 220); column("Author", Book::getAuthor, 180);
        column("Status", b -> label(b.getStatus()), 100);
        TableColumn<Book, String> actions = new TableColumn<>("Actions"); actions.setPrefWidth(230);
        actions.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Button edit = new Button("Edit"), state = new Button("Status"), delete = new Button("Delete");
                edit.setOnAction(e -> form(getTableRow().getItem()));
                state.setOnAction(e -> changeStatus(getTableRow().getItem()));
                delete.setOnAction(e -> delete(getTableRow().getItem()));
                setGraphic(new HBox(5, edit, state, delete));
            }
        });
        table.getColumns().add(actions);
        table.setPlaceholder(new Label("No books match your search."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        refresh();
    }
    /**
     * Creates a read-only text column using the supplied value formatter.
     * @param title column heading
     * @param value column value extractor
     * @param width preferred column width in pixels
     */
    private void column(String title, java.util.function.Function<Book,String> value, int width) {
        TableColumn<Book,String> col = new TableColumn<>(title); col.setPrefWidth(width);
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(value.apply(c.getValue()))); table.getColumns().add(col);
    }
    /**
     * Converts a status enum name to a readable label.
     * @param s status to display
     * @return the title-case status name
     */
    private static String label(BookStatus s) { return s.name().substring(0,1) + s.name().substring(1).toLowerCase(Locale.ROOT); }
    /**
     * Switches between dashboard summary and filtered book-table presentation.
     * @param dashboard true to show summary cards and recent books
     */
    void show(boolean dashboard) {
        this.dashboard = dashboard;
        stats.setVisible(dashboard); stats.setManaged(dashboard);
        recentActivity.setVisible(dashboard); recentActivity.setManaged(dashboard);
        filters.setVisible(!dashboard); filters.setManaged(!dashboard); render();
    }
    /**
     * Rebuilds visible rows from the shared collection and current view selection.
     */
    private void render() {
        List<Book> found;
        if (dashboard) found = collection.stream().sorted(Comparator.comparing(
                (Book b) -> Objects.toString(b.getCreatedAt(), "")).reversed()).limit(5).toList();
        else found = BookFilter.apply(collection, search.getText(), status.getSelectionModel().getSelectedIndex() <= 0
                ? null : BookStatus.valueOf(status.getValue().toUpperCase(Locale.ROOT)));
        table.getItems().setAll(found);
        count.setText((dashboard ? "Recent books: " : "Showing ") + found.size() + " of " + collection.size());
    }
    /**
     * Clears search text and restores the all-status filter.
     */
    @FXML private void reset() { search.clear(); status.setValue("All statuses"); }
    /**
     * Opens the Add New Book dialog.
     */
    @FXML private void add() { form(null); }
    /**
     * Loads current server state asynchronously and ignores results from an obsolete session.
     */
    @FXML private void refresh() {
        run(() -> {
            List<Book> books = service.getAllBooks();
            LibraryStatistics summary = service.getStatistics();
            String activity;
            try { activity = logs.getActivityLogs().stream().limit(4).map(l ->
                    l.getAction() + " · Book " + l.getBookId() + " · " + Objects.toString(l.getLibrarianName(), "Librarian")
                    + " · " + Objects.toString(l.getCreatedAt(), "")).reduce((a,b) -> a + "\n" + b).orElse("No recent activity."); }
            catch (Exception e) { activity = "Recent activity could not be loaded."; }
            return new Snapshot(books, summary, activity);
        }, snapshot -> {
            collection = snapshot.books(); stats.getChildren().clear();
            LibraryStatistics s = snapshot.stats();
            String[] names = {"Total Books", "Available", "Issued", "Reserved"};
            int[] values = {s.getTotal(), s.getAvailable(), s.getIssued(), s.getReserved()};
            for (int i=0;i<4;i++) {
                Label number = new Label(String.valueOf(values[i])); number.getStyleClass().add("heading");
                VBox card = new VBox(12, new Label(names[i]), number); card.getStyleClass().add("panel");
                HBox.setHgrow(card, Priority.ALWAYS); card.setMaxWidth(Double.MAX_VALUE); stats.getChildren().add(card);
            }
            recentActivity.setText("Recent Activity\n\n" + snapshot.activity()); render();
        });
    }
    /** Bundles separately fetched books, counts and activity preview for one UI update. */
    private record Snapshot(List<Book> books, LibraryStatistics stats, String activity) {}
    /**
     * Queues an operation and delivers its result only to the current session.
     * @param <T> operation result type
     * @param operation background service call
     * @param success JavaFX-thread callback after a successful result
     */
    private <T> void run(Callable<T> operation, java.util.function.Consumer<T> success) {
        if (busy || !app.current(auth)) return;
        busy = true; root.setDisable(true); message.setText("Working…");
        Task<T> task = new Task<>() { protected T call() throws Exception {
            if (!app.current(auth)) throw new InterruptedException();
            return operation.call();
        }};
        task.setOnSucceeded(e -> { if (!app.current(auth)) return; busy=false; root.setDisable(false); message.setText(""); success.accept(task.getValue()); });
        task.setOnFailed(e -> {
            if (!app.current(auth)) return;
            busy=false; root.setDisable(false);
            Throwable failure = task.getException();
            if (app.expired(auth, failure)) return;
            String text = failure.getMessage();
            Set<String> safe = Set.of("Book ID already exists.", "Book not found or access denied.", "Access denied.",
                "Book not found.", "Book ID must be positive.", "Book title cannot be empty.", "Author name cannot be empty.");
            message.setText(safe.contains(text == null ? "" : text) ? text :
                "Request failed. Check your connection and permissions. Refresh before retrying a change; it may have reached the server.");
        }); app.execute(task);
    }
    /**
     * Shows an Add or Edit dialog and delegates accepted values to the service.
     * @param book book to persist or display
     */
    private void form(Book book) {
        if (book == null && busy) return;
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.initOwner(root.getScene().getWindow());
        dialog.setTitle(book == null ? "Add New Book" : "Edit Book");
        TextField id = new TextField(book == null ? "" : String.valueOf(book.getId())); id.setDisable(book != null);
        TextField title = new TextField(book == null ? "" : book.getTitle());
        TextField author = new TextField(book == null ? "" : book.getAuthor());
        Label error = new Label(); error.setWrapText(true);
        VBox fields = new VBox(10, new Label("Book ID"), id, new Label("Title"), title, new Label("Author"), author,
                new Label(book == null ? "New books start as Available." : "Use Status to change availability."), error);
        fields.setPrefWidth(400); dialog.getDialogPane().setContent(fields);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            try { InputValidator.validateId(Integer.parseInt(id.getText().trim())); InputValidator.validateDetails(title.getText(),author.getText()); }
            catch (Exception invalid) { error.setText("Enter a positive whole-number ID, title and author."); e.consume(); }
        });
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        int number = Integer.parseInt(id.getText().trim()); String t = title.getText(), a = author.getText();
        run(() -> book == null ? service.addBook(number,t,a) : service.updateBook(number,t,a), result -> refresh());
    }
    /**
     * Shows a status-choice dialog and submits an accepted change through BookService.
     * @param book book to persist or display
     */
    private void changeStatus(Book book) {
        if (book == null) return;
        ChoiceDialog<BookStatus> dialog = new ChoiceDialog<>(book.getStatus(), BookStatus.values());
        dialog.initOwner(root.getScene().getWindow()); dialog.setTitle("Change book status"); dialog.setHeaderText(book.getTitle());
        dialog.showAndWait().filter(s -> s != book.getStatus()).ifPresent(s -> run(() -> service.changeStatus(book.getId(),s), result -> refresh()));
    }
    /**
     * Confirms deletion before submitting the selected book ID to BookService.
     * @param book book to persist or display
     */
    private void delete(Book book) {
        if (book == null) return;
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "Delete ‘" + book.getTitle() + "’? This cannot be undone.", ButtonType.CANCEL, ButtonType.OK);
        dialog.initOwner(root.getScene().getWindow()); dialog.setHeaderText("Delete book #" + book.getId());
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
            run(() -> { service.deleteBook(book.getId()); return true; }, result -> refresh());
    }
}

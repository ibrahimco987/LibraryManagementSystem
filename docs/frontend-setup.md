# Library Desk: Phases 1�3

Run from the repository root on Windows with JDK 21, JAVA_HOME pointing to that JDK, and Maven 3.9+ on PATH. Maven downloads JavaFX 21.0.11 controls/FXML and Windows native libraries; no separate JavaFX SDK or module-info.java is needed. A desktop session and network access to Maven Central and Supabase are required.

Copy `.env.example` to `.env` and fill in your Supabase project URL and publishable/anon client key locally. Never use a service-role/secret key. Do not share credentials or commit `.env`. Each nonblank OS setting overrides the corresponding file setting; values are trimmed. Missing files are allowed. Missing settings are reported on sign-in. The optional file is read from the working directory.

```powershell
java -version
mvn -version
mvn clean test
mvn clean package
mvn javafx:run
```

The original `com.library.App` console entry point remains unchanged. The JavaFX launcher is `com.library.ui.LibraryFxApp`. Controllers use the existing authentication service; requests run on a single owned background executor. Logout immediately removes the authenticated screen and clears the application reference, then calls backend logout (which clears tokens even on failure). Closing the application interrupts the worker. Password fields are cleared after submission; session tokens are held only in memory.

Dashboard loads backend statistics, five recent books and up to four recent activity entries. Book Management supports combined case-insensitive search and status filtering, add/edit, status changes and confirmed deletion. New books start Available; status is changed separately to avoid a partially successful two-request Add operation. Activity Logs displays the latest 100 events, newest first, with local timestamps and refresh. Search/filter runs on the fetched collection without server pagination. Refresh retrieves current records; mutations refresh shared dashboard/table state. No sample records are used at runtime.

## Manual review

- Launch at 1200×760 and resize to 960×640; check keyboard focus and readable labels.
- Check missing configuration, OS override, file-only configuration and wrong credentials.
- Use a registered test librarian locally; verify the name, all three navigation buttons and Sign out.
- Try an account without a librarian profile and disconnect the network during login; verify errors and responsive controls.
- Screenshot only the empty login screen or a test librarian shell. Never capture passwords, keys, tokens or real email addresses.

The approved reference is `library_ui_preview.html` at the repository root (the plan's `design/` path is absent). It is a visual reference only; the application renders native FXML/CSS.

Dependency references: https://openjfx.io/openjfx-docs/ and https://github.com/cdimascio/dotenv-java

## Phase 2 manual checks

Use a registered test librarian and an unused positive book ID. Add a test book, refresh and verify persistence; reject duplicate IDs and blank fields. Edit title/author, change each status, combine author/title/ID searches with status filters, and reset. Cancel deletion first, then confirm it and verify refreshed dashboard counts and database-trigger activity. Disconnect the network during refresh and verify the UI remains responsive. Logout during a request and verify no old-session result reaches the login screen. A failed mutation may have reached the server; refresh before retrying.

Counts and collection are separate backend requests, so concurrent changes by another librarian can produce a transient discrepancy; refresh to reconcile. Recent activity failure does not prevent books from loading.

## Phase 3 review

Verify Activity Logs is read-only, newest first, and refreshes after a book mutation. Check missing librarian names fall back to the returned librarian ID. Timestamps use the system time zone displayed in the column header. Expired or irrecoverably unauthorized sessions return to login; a network error retains the screen with a retry message. Test logout during refresh, keyboard navigation, and resizing. Screenshots and authenticated live checks require a local test account and have not been completed by the agent.

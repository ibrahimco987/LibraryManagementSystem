# Library Desk: Phase 1

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

Dashboard, Book Management and Activity Logs are explicit placeholders. No book/activity requests, CRUD, statistics, or sample records are implemented. Future search/filter operations in the existing backend fetch the collection without server pagination.

## Manual review

- Launch at 1200×760 and resize to 960×640; check keyboard focus and readable labels.
- Check missing configuration, OS override, file-only configuration and wrong credentials.
- Use a registered test librarian locally; verify the name, all three navigation buttons and Sign out.
- Try an account without a librarian profile and disconnect the network during login; verify errors and responsive controls.
- Screenshot only the empty login screen or a test librarian shell. Never capture passwords, keys, tokens or real email addresses.

The approved reference is `library_ui_preview.html` at the repository root (the plan's `design/` path is absent). It is a visual reference only; the application renders native FXML/CSS.

Dependency references: https://openjfx.io/openjfx-docs/ and https://github.com/cdimascio/dotenv-java

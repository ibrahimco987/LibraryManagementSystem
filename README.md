# Library Desk

Java 21 / JavaFX desktop library management backed by Supabase. Librarians can sign in, view dashboard counts, search/filter books, add/edit/delete books, change availability and review recent activity.

## Run

Install JDK 21 and Maven 3.9+, then run from the project root. Copy `.env.example` to `.env` and supply your project URL and publishable/anon key locally. OS environment values take precedence. Never use a service-role key in the desktop app.

```powershell
mvn clean test
mvn clean package
mvn javafx:run
```

Create Auth users through the Supabase dashboard and associate their UUID with a `public.librarians` row. No signup or student portal is provided.

## Architecture

FXML/CSS views and Java controllers call existing services. SupabaseClient supplies HTTP transport; AuthenticationService owns in-memory tokens and refresh. Background tasks prevent network calls from blocking the JavaFX thread, and session identity checks reject results after logout. Book mutations use BookService and database triggers record activity. The original `com.library.App` console entry point remains available.

Frontend owner: Maaz. Backend owner: Ibrahim. Develop and review frontend changes on `frontend`; merge into `main` only after approval.

## Verification and limits

See [frontend setup and manual checks](docs/frontend-setup.md). Automated tests cover backend rules, configuration precedence, combined search and activity formatting. Live authentication and database mutations require a test librarian. Collection searches run in memory after fetching all books; there is no server pagination. Activity history is limited to the latest 100 events. Internet access is required. Separate statistics/list requests can briefly disagree during concurrent edits.

Safe screenshots may be placed in `docs/screenshots/` after local review; none are included yet. Never capture credentials or tokens.

# Codex implementation contract — Library Desk JavaFX frontend

**Project:** Java-Based Library Book Management System Using Modern Software Construction Tools  
**Frontend owner:** Maaz | **Backend owner:** Ibrahim  
**Repository (SSH):** `git@github.com:ibrahimco987/LibraryManagementSystem.git`  
**Required working branch:** `frontend`  
**Local project path:** `D:\College\5TH SEMESTER\SC_Lab\LibraryManagementSystem`  
**Target runtime:** Java 21, Maven, JavaFX 21.x, Windows PowerShell  
**Visual specification:** `design/library_ui_preview.html` packaged with this document.

## 0. Read this first — binding requirements

This is a **desktop JavaFX** implementation. The HTML file is an approved **visual and interaction reference only**. Do not use a browser/WebView/embedded web server to render the HTML as the application's frontend. Recreate the layout with JavaFX scene graph, FXML, and CSS; write event handling in Java controllers.

The backend is already implemented. **Inspect real files and method signatures before writing an adapter.** The contracts below are based on the development history and are not a substitute for reading the repository; resolve any mismatches from source code, preserving existing successful backend behavior.

**Hard scope limits:** librarian-only login; book CRUD; Available/Issued/Reserved status; search and filter; dashboard statistics; activity history; logout. **Do not add** a student portal, signup, borrowing workflow, fines, payment, due dates, PDF export, roles/permissions editor, local persistence, or a second database. Never replace Supabase with mocked data at runtime.

**Do not run `git push`, merge, or create commits automatically unless the operator explicitly tells you the phase's review and tests passed.** The commands are provided at each checkpoint for Maaz to run after review. Commit only the intended phase files; never commit `.env` or generated files.

### 0.1 Existing backend to inspect

Expected paths/classes (validate from repository):

- `pom.xml` — Java 21 Maven project; keep existing dependencies and test plugins.
- `src/main/java/com/library/App.java` — existing console-based backend test harness. Preserve it; a **new JavaFX entry point** will be used.
- `src/main/java/com/library/SupabaseClient.java` — HTTP client with request method and environment configuration.
- `src/main/java/com/library/model/{Book,BookStatus,Librarian,LibraryStatistics,ActivityLog}.java`.
- `src/main/java/com/library/repository/BookRepository.java`.
- `src/main/java/com/library/service/{AuthenticationService,BookService,ActivityLogService}.java`.
- `src/main/java/com/library/util/InputValidator.java`.
- `src/test/java/...` — existing backend JUnit tests (12 tests expected from prior work, verify count).
- `database/` — SQL schema, RLS policies, book-activity trigger; frontend MUST NOT mutate database schema unless a real blocker is shown and approved.

Known backend behavior: `AuthenticationService` logs in through Supabase password grant, maintains a librarian session and refreshes JWTs; `BookService` validates book operations and handles CRUD/search/filter/statistics; `ActivityLogService` fetches up to 100 recent events; database trigger records book mutations. Names/parameters/constructors/return types must be checked locally.

### 0.2 Approved UI specification

Read `design/library_ui_preview.html` before implementing UI. Preserve the **intent, not HTML/CSS implementation details**:

1. **Login:** two-column navy/blue branding + white form, email/password, sign in, visible errors; no signup.
2. **Dashboard:** left sidebar, header, cards for Total / Available / Issued / Reserved, recent books, recent activities, Add New Book quick action.
3. **Book Management:** searchable/filterable table with columns Book ID, Title, Author, Status, Actions; Add New Book modal; Edit modal; Delete confirmation; status change.
4. **Activity Logs:** reverse-chronological list/table of book actions, librarian name and date/time when present; read-only.
5. **Shared shell:** app name LIBRARY DESK, Dashboard / Book Management / Activity Logs navigation, signed-in librarian, Sign out.

Visual language sampled from approved prototype: background `#f4f6fa`, panels `#ffffff`, primary ink `#17233c`, muted text `#718096`, blue accent `#3558d4`, borders `#e6eaf1`, Available green `#15845d`, Reserved amber `#b77a18`, destructive red `#bf4b50`. Match a clean desktop layout and maintain readable text at roughly 1200×760; do not over-engineer responsivity.

### 0.3 Architecture and coding constraints

- Use a **non-modular** Maven JavaFX app by default unless repository already has a working `module-info.java`; do not add one without need.
- Add JavaFX `javafx-controls` and `javafx-fxml` dependencies at compatible **21.x** release; use `org.openjfx:javafx-maven-plugin`, configured with `mainClass` pointing to the new `com.library.ui.LibraryFxApp` (or actual agreed package). Confirm plugin/dependency version compatibility and successful Windows launch.
- Use `src/main/java/com/library/ui/` for FX application/controller/helper classes and `src/main/resources/com/library/ui/` for FXML and CSS. Suggested names below, adapt based on repository.
- Keep business rules inside existing services. Controllers **call services**; no direct Supabase REST calls from UI and no replicated validation logic beyond form usability.
- Avoid UI freezes: backend HTTP requests must not execute on JavaFX Application Thread. Use `javafx.concurrent.Task` or a small `ExecutorService` with safe `Platform.runLater` callbacks. Disable buttons/show loading during requests. Shut down owned executor on application exit.
- Keep an **in-memory authenticated session** only; no persistent JWT/password storage. Make session logout clear the UI and authenticated references.
- Handle expected failures: missing config; bad credentials; non-librarian account; network timeout; forbidden/RLS; duplicate ID; invalid inputs; empty lists; expired/refresh token. Show human-readable errors **without exposing keys, passwords, or raw tokens**.
- Use standard JavaFX widgets: `TableView`, `TextField`, `PasswordField`, `ComboBox`, `Alert`/`Dialog`, `Label`, `Button`, `BorderPane`, `VBox`, etc. No third-party UI framework required.
- For search/filter, existing `BookService` methods may fetch all records: acceptable for a small university lab exercise. Clearly document that this is not server-paginated.
- Prefer testable controller-independent helpers (`UiInput`, formatters, state/action coordinator) and preserve existing JUnit suite.
- Keep JavaDocs concise and accurate; do not rewrite unrelated backend code or SQL.

### 0.4 Mandatory setup and safety

Before any phase, run in PowerShell:

```powershell
cd "D:\College\5TH SEMESTER\SC_Lab\LibraryManagementSystem"
git status --short
git branch --show-current
git remote -v
java -version
mvn -version
mvn clean test
```

If branch is not `frontend`, switch with `git switch frontend` **only if it already exists**; do not force, reset, or discard changes. If worktree is dirty, inspect/resolve it before beginning. The `main` branch is the merged backend baseline; do not develop on `main` or `backend`.

**Security baseline:** local `.env` is useful for developer configuration but is *not a secure secret store*. A Supabase **publishable/anon client key** is designed for untrusted clients; security is enforced by Supabase Authentication, grants and RLS. **Never put the Supabase `service_role`/secret key in a desktop application**, even inside `.env`, and never expose a librarian's password. Existing backend expects `SUPABASE_URL` and `SUPABASE_KEY` via `System.getenv`; add optional `.env` fallback without breaking environment variables. In no case should the absence of `.env` prevent a valid OS environment setup.

`/.env` (local only; fill with user's actual Supabase project URL and **publishable** key):

```dotenv
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_KEY=YOUR_SUPABASE_PUBLISHABLE_KEY
```

`/.env.example` (commit this placeholder file):

```dotenv
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_KEY=YOUR_SUPABASE_PUBLISHABLE_KEY
```

Append to `.gitignore` without erasing existing patterns:

```gitignore
.env
.env.*
!.env.example
```

Recommended Java library: `io.github.cdimascio:dotenv-java:3.2.0` (confirm Maven Central availability). Implement one small config utility (e.g., `com.library.config.AppConfig`): resolve each setting in order **nonblank `System.getenv` → nonblank `.env` → descriptive missing-config error**; support `.env` absent without throwing if valid OS vars exist; trim values; avoid logging full URLs/keys where possible and **never print the key**. Modify only the appropriate constructor/bootstrap path or the existing `SupabaseClient` configuration method; minimize backend changes. The desktop launch and the existing CLI app must keep working where practical. For `.env`, load from the project working directory; document that `mvn javafx:run` must be invoked from the project root.

Security checks:

```powershell
git check-ignore .env
git ls-files .env
git status --short
```

Expected: `.env` ignored; `git ls-files .env` prints **nothing**. If `.env` was already tracked, stop and request explicit remediation before committing. Never paste real values in Codex chat, tests, screenshots, logs or commits.

---

# Phase 1 of 3 — Environment, JavaFX shell, librarian login

## Goal

A real JavaFX application launches with the approved Login screen, loads Supabase config from OS variables or local `.env`, logs in a librarian using existing `AuthenticationService`, and opens a navigable (initially placeholder-content) shell matching the approved HTML. No student signup. Existing CLI app and backend tests still pass.

## Deliverables

- Maven JavaFX controls/fxml dependencies and launcher plugin configured; no standalone JS/HTML runtime.
- `.gitignore`, `.env.example`, `AppConfig` (or analogous) with test coverage for precedence/error handling (design testable so it can use injected env/dotenv maps rather than real global env vars).
- `LibraryFxApp` (JavaFX `Application`), `login.fxml`, `shell.fxml`, `app.css`, `LoginController`, `ShellController`, and a thin application/session wiring class as needed.
- Login email/password inputs, loading spinner/disabled submit, user-friendly errors, successful login -> shell, logout -> login.
- Shared sidebar navigation with Dashboard / Book Management / Activity Logs placeholders, signed-in librarian display.
- `docs/frontend-setup.md` covering JDK/Maven, `.env` example, launch/test commands, Windows prerequisites, safe screenshots (no credential exposure).

## Suggested structure (subject to source inspection)

```text
src/main/java/com/library/
  config/AppConfig.java
  ui/LibraryFxApp.java
  ui/LoginController.java
  ui/ShellController.java
  ui/SessionContext.java
  ui/UiAsync.java
src/main/resources/com/library/ui/
  login.fxml
  shell.fxml
  app.css
src/test/java/com/library/config/AppConfigTest.java
.env.example
.gitignore
```

**Do not blindly create all helpers:** keep only those that reduce duplication.

## Exact Codex prompt — paste as one message

```text
You are implementing PHASE 1 ONLY of the approved Library Desk JavaFX frontend.
Read CODEX_JAVAFX_IMPLEMENTATION_PLAN.md and design/library_ui_preview.html in full first. Inspect pom.xml, existing service/model/client constructors, the full Java source tree, Git status/branch, .gitignore, and tests. Do not assume historical signatures are exact. Work ONLY on the frontend branch. Do not rewrite backend business logic, database scripts, RLS policies, or the existing console App unless the smallest configuration integration genuinely requires a change.

Implement JavaFX 21-compatible non-modular Maven dependencies, a new JavaFX launcher, reusable CSS/FXML login and sidebar shell visually faithful to the approved HTML, existing AuthenticationService login/logout integration, an in-memory session, background HTTP requests with busy state and clear errors, optional .env fallback for SUPABASE_URL and SUPABASE_KEY (OS environment variables take precedence), .env.example, .gitignore protection, config unit tests, and setup documentation. Never include actual keys/passwords in code, tests, output or git. The shell's dashboard, books and activity views are placeholders until later phases. Keep the original console App working.

First report the exact backend signatures you found and your file-level plan; then implement. Run mvn clean test and mvn clean package, then attempt mvn javafx:run if a GUI is available. If GUI execution is unavailable, report that limitation rather than claiming visual validation. Show git diff --stat and an explicit changed-files list. Do not commit, push or merge until I review and authorize the checkpoint.
```

## Phase 1 acceptance criteria

- [ ] `git branch --show-current` is `frontend`; no unexpected file changes.
- [ ] `mvn clean test` succeeds, including original backend JUnit tests and new config tests.
- [ ] `mvn clean package` succeeds.
- [ ] `mvn javafx:run` opens JavaFX login from the repository root on Maaz's Windows desktop.
- [ ] `.env` fallback works; an OS override wins; missing config produces a clear error.
- [ ] `git check-ignore .env` returns `.env`, while `git ls-files .env` is blank.
- [ ] Bad login produces an understandable message; valid registered librarian login opens the shell; Sign out returns to login.
- [ ] Shell navigation responds; empty placeholders are clearly indicated (not fake data).
- [ ] CSS/layout resembles prototype and can be explained in a viva.
- [ ] JavaFX thread is not blocked by network requests.

## Phase 1 Git checkpoints — two commits

After Maaz reviews the changed files and tests:

**Checkpoint F1.1 — Runtime/config:**

```powershell
git status --short
git add pom.xml .gitignore .env.example src/main/java/com/library/config src/test/java/com/library/config
# Also stage the specific existing client/bootstrap file ONLY if Codex legitimately updated it.
git diff --cached --stat
git commit -m "Configure JavaFX runtime and local Supabase environment"
git push origin frontend
```

**Checkpoint F1.2 — Login/shell:**

```powershell
git add src/main/java/com/library/ui src/main/resources/com/library/ui docs/frontend-setup.md
# Stage any remaining intended phase-1 files explicitly after reviewing git status.
git diff --cached --stat
git commit -m "Add JavaFX librarian login and navigation shell"
git push origin frontend
```

If Codex implements everything at once, use `git add -p` / explicit paths to split the commits; never fabricate an empty commit. Don't stage `.env`.

---

# Phase 2 of 3 — Dashboard and book-management workflows

## Goal

Replace Phase 1 placeholders for Dashboard and Book Management with fully functional JavaFX screens backed by existing `BookService`; reproduce the approved table/dialogs and reflect changes in statistics and recent books. The database remains Supabase, not local arrays at runtime.

## Deliverables

- `dashboard.fxml` / `DashboardController`: four statistic cards from `getStatistics()`, recent books via backend, recent activity if safely supported by `ActivityLogService`; quick Add Book.
- `books.fxml` / `BooksController`: table with Book ID, Title, Author, Status, Actions, search TextField, status ComboBox with All/Available/Issued/Reserved, reset and refresh, empty/loading/error states.
- Modal Add/Edit Book form; unique positive ID, required trimmed title and author; appropriate status options; existing `InputValidator` + service-side validation; validation messages on screen.
- Status changes via existing `BookService.changeStatus` (or actual method), not raw repository in controller; status chip styling; delete confirmation before mutating data.
- Refresh dashboard and book table after successful add/edit/delete/status change. Search/filter interactions should be coherent when combined: inspect backend methods; if separate methods cannot combine safely, filter the returned list in-memory within a tiny UI helper after fetching once, without altering persistent data or duplicating business rules.
- Preserve authenticated bearer token, session reauthentication errors, and server-side activity trigger; **do not insert activity rows manually from JavaFX**.
- Controller/service boundary unit tests for pure helpers/formatters. For live Supabase integration, provide manual instructions; don't run destructive automated tests against production data.

## Exact Codex prompt — paste after Phase 1 is approved/pushed

```text
Implement PHASE 2 ONLY of CODEX_JAVAFX_IMPLEMENTATION_PLAN.md, using the approved design/library_ui_preview.html. We are on frontend and Phase 1 is tested and committed. First inspect exact signatures of BookService, BookRepository, Book, BookStatus, LibraryStatistics, ActivityLogService, and current FXML/controllers. Keep existing backend services and Supabase RLS intact. Do not add a student portal or fake runtime records.

Build Dashboard and Book Management JavaFX FXML/controllers/styles: summary counts, recent books, activity preview when existing API permits, a real TableView with ID/title/author/status/actions, case-insensitive searching and status filter (including combined search+filter), Add/Edit dialogs, status updates, delete confirmation, refresh/reset, validation, loading/empty/error states. All network calls must run off the JavaFX Application Thread; update UI on FX thread. Route mutations through BookService, let database trigger record activity, and refresh dashboard/table after success. Avoid double submissions and prevent stale updates after logout. Do not expose credentials in messages/screenshots.

First state your verified service calls and proposed file changes. Implement incrementally. Run mvn clean test and mvn clean package; if desktop GUI is available, manually exercise add/edit/status/delete/search/filter with a safe test book and report results. Provide exact changed files and git diff --stat. Do not commit or push until I approve.
```

## Phase 2 acceptance criteria

- [ ] `mvn clean test` and `mvn clean package` succeed.
- [ ] Dashboard counts match current book records in Supabase.
- [ ] Book table loads actual book IDs/titles/authors/statuses.
- [ ] Search works for ID, title, author; case-insensitivity for text; status filters All/Available/Issued/Reserved; combined search+filter works.
- [ ] Add book persists after refresh/restart; duplicate/invalid ID and empty strings are rejected.
- [ ] Edit title/author persists; status change persists; available enum values are exactly Available/Issued/Reserved.
- [ ] Delete always asks for confirmation; canceled delete makes no changes; confirmed deletion persists.
- [ ] Successful mutations update visible dashboard/table; database-trigger activity is created (verify in Supabase only with safe test record).
- [ ] Network failures don't freeze/crash UI or leak internal tokens.
- [ ] There are no hardcoded runtime mock books, and no direct database calls in controller classes.

## Phase 2 Git checkpoints — three commits

Commit only after each coherent implementation slice is verified; if partial slices depend on shared foundational files, stage those specific files appropriately.

**Checkpoint F2.1 — Dashboard:**

```powershell
git add src/main/java/com/library/ui/DashboardController.java src/main/resources/com/library/ui/dashboard.fxml src/main/resources/com/library/ui/app.css
# Include other actual dashboard-related files after verifying paths.
git diff --cached --stat
git commit -m "Build JavaFX dashboard with live library statistics"
git push origin frontend
```

**Checkpoint F2.2 — Book listing/search/filter:**

```powershell
git add src/main/java/com/library/ui/BooksController.java src/main/resources/com/library/ui/books.fxml
# Include shared navigation/CSS modifications and relevant tests intentionally.
git diff --cached --stat
git commit -m "Implement JavaFX book table search and status filtering"
git push origin frontend
```

**Checkpoint F2.3 — Mutations/dialogs:**

```powershell
# Review actual paths; this phase may introduce BookDialogController.java / book-dialog.fxml.
git status --short
git add -p
git diff --cached --stat
git commit -m "Add book CRUD dialogs and status management actions"
git push origin frontend
```

Do not issue an empty commit if a file name differs or changes are already included in an earlier checkpoint.

---

# Phase 3 of 3 — Activity logs, polish, final verification and handoff

## Goal

Implement full read-only Activity Logs UI, harden cross-screen integration and error handling, add unit tests and documentation, then hand over the completed frontend branch for review/merge **without automatically merging**.

## Deliverables

- `activity.fxml` / `ActivityController`: fetch latest activity from existing `ActivityLogService`, newest first; columns/rows for event, book ID, librarian, timestamp (whatever fields the model actually provides); clear empty/error/loading state; refresh button.
- Consistent navigation, active sidebar selection, signed-in librarian label, logout, responsive window resizing at ordinary desktop widths, accessible labels/keyboard focus, confirmations and validation messages.
- Optional timeline/action badges styled consistently with HTML (`ADD`, `UPDATE`, `STATUS_CHANGE`, `DELETE` as represented by backend); do not invent log data or allow log editing.
- Handle auth/session expiry: existing refresh logic may recover automatically; if irrecoverably unauthorized, show clear notice and return to Login without exposing JWT.
- Tests for configuration, pure UI formatting/validation/combined-filter logic; existing backend tests still pass. GUI smoke checklist with test librarian and known sample book IDs.
- Update `README.md` with architecture, launch steps, screenshots location, responsibilities, branch workflow. Generate/verify Javadoc where appropriate.
- Capture safe screenshots into `docs/screenshots/` **only if** the local app can run; use demonstration librarian and nonsensitive book titles, avoid real email addresses if possible, never display keys/passwords.
- Document limitations (e.g., small dataset client-side filtering, 100-activity limit, Supabase internet requirement).

## Exact Codex prompt — paste after Phase 2 is approved/pushed

```text
Implement PHASE 3 ONLY of CODEX_JAVAFX_IMPLEMENTATION_PLAN.md. The approved frontend design is design/library_ui_preview.html. Phase 1 login/shell and Phase 2 dashboard/books are complete, reviewed and committed on frontend.

Inspect the real ActivityLogService/ActivityLog model and the current FXML/controllers first. Build a read-only latest-activity JavaFX view with correct newest-first ordering, descriptive action badges, optional book ID and librarian fields only if actually returned, readable local time formatting, refresh/loading/empty/error states. Polish sidebar navigation, logout, session expiry UX, TableView responsiveness and accessibility. Add meaningful isolated JUnit tests for config and pure UI logic, preserve all preexisting tests, update README and docs/frontend-setup.md, and provide a precise manual GUI test matrix.

No demo data at runtime. No direct REST requests from controllers, new SQL tables, extra roles or student accounts, secrets in commits, or automatic merge. Run mvn clean test, mvn clean package, mvn javadoc:javadoc, and attempt mvn javafx:run on the GUI host. Audit git status, git diff --check, git check-ignore .env and git ls-files .env. Identify any tests/manual GUI checks that you cannot run and report them honestly. Provide a final changed-file inventory and proposed git checkpoints, but do not commit, push, create a PR or merge without my approval.
```

## Phase 3 acceptance criteria

- [ ] Activity list loads actual Supabase-generated logs, newest first, and is not editable.
- [ ] Add, update, status change, delete produce expected visible activity entries where trigger/RLS permits.
- [ ] Dashboard, books and logs are consistent after navigation/refresh.
- [ ] Successful logout stops authenticated UI actions; bad/expired sessions handled gracefully.
- [ ] All unit tests pass and Maven builds successfully; Javadoc completes without a build-breaking error.
- [ ] Real GUI manually smoke-tested on Maaz's Windows machine (or explicitly marked unverified).
- [ ] README/docs accurate; `.env.example` placeholders only; no `.env`, password, token, service-role key or generated `target/` checked in.
- [ ] `git diff --check` passes; `git status` is clean after approved commits.
- [ ] Final frontend branch pushed; no merge to `main` without Ibrahim/Maaz's final review.

## Phase 3 Git checkpoints — two commits

**Checkpoint F3.1 — Activity logs:**

```powershell
git add src/main/java/com/library/ui/ActivityController.java src/main/resources/com/library/ui/activity.fxml
# Stage associated styles/navigation files after checking actual names.
git diff --cached --stat
git commit -m "Add read-only JavaFX activity log screen"
git push origin frontend
```

**Checkpoint F3.2 — Tests/docs/final polish:**

```powershell
mvn clean test
mvn clean package
mvn javadoc:javadoc
git diff --check
git check-ignore .env
git ls-files .env
git status --short
git add -p
# Stage intended README, documentation, tests, styling and final fixes; never .env.
git diff --cached --stat
git commit -m "Polish JavaFX frontend and complete tests and documentation"
git push origin frontend
```

**Total intended frontend commits: 7** (F1.1, F1.2, F2.1, F2.2, F2.3, F3.1, F3.2), adjusted only when real logical slices justify it. Do not create commits merely to hit a number.

---

# Final QA: manual test matrix

Perform with a **test librarian** permitted by Supabase policies, using non-sensitive test books; verify effects in both the UI and Supabase where necessary. Avoid running destructive tests on shared coursework records.

| ID | Action | Expected result |
|---|---|---|
| L1 | Launch with `.env` and no OS vars | Login screen appears; no config error |
| L2 | Launch with OS vars | OS vars override `.env` |
| L3 | Launch with missing config | Clear setup error, no credential dump |
| L4 | Wrong password | Error shown, no authenticated shell |
| L5 | Valid librarian login | Dashboard visible, librarian name shown |
| D1 | Refresh dashboard | Four counts match Supabase |
| B1 | Add book with unique ID/title/author | Book appears and survives refresh/restart |
| B2 | Add duplicate/invalid ID or empty title | Rejected; no new row |
| B3 | Search by ID/title/author | Matching rows visible |
| B4 | Filter Available/Issued/Reserved | Correct rows, with combined search |
| B5 | Edit book title/author | Persisted and refreshed |
| B6 | Change status | Correct persisted status and dashboard count |
| B7 | Cancel deletion | Book remains |
| B8 | Confirm deletion | Book removed; dashboard count updated |
| A1 | Open Activity Logs | Actual recent events visible, newest first |
| A2 | Trigger a book change | New activity appears after refresh |
| S1 | Logout | Login shown, no authenticated actions possible |
| S2 | Disconnect network during refresh | App remains responsive, clear error |
| S3 | Resize window/keyboard navigation | UI remains usable |

Suggested demo record: choose an unused positive book ID (not an existing production record), title `UI Integration Test`, author `Coursework Test`. Clean it up at the end; deletion will produce a log, as expected.

# Final operator command sequence

Run from the project root on Maaz's Windows machine:

```powershell
cd "D:\College\5TH SEMESTER\SC_Lab\LibraryManagementSystem"
git switch frontend
git status
mvn clean test
mvn clean package
mvn javadoc:javadoc
mvn javafx:run
git diff --check
git check-ignore .env
git ls-files .env
git log --oneline -10
git push origin frontend
```

`mvn javafx:run` starts a GUI and normally remains active until the window is closed; run subsequent Git commands in a second terminal or after closing the UI.

**After Phase 3 approval:** Open a GitHub pull request **base `main` ← compare `frontend`**. Review build results and all commits; merge only after joint approval. Do not automatically delete source branches. Resolve any conflicts carefully; do not force-push `main`.

# Technical references (verify versions against the project's local JDK and Maven environment)

- OpenJFX Maven guide: https://openjfx.io/openjfx-docs/
- Maven Central JavaFX FXML: https://central.sonatype.com/artifact/org.openjfx/javafx-fxml
- dotenv-java documentation: https://github.com/cdimascio/dotenv-java
- Supabase security principle: client publishable keys are not substitutes for RLS; keep elevated secret keys on trusted servers only.

# Codex operator protocol

For **each** of the three phases:

1. Maaz opens the repository in Codex, provides this file, and sends the exact prompt for that phase.
2. Codex reports backend contracts observed and a file-by-file plan **before editing**.
3. Codex implements *only* the selected phase and shows changed files and tests.
4. Maaz checks the GUI on his actual Windows laptop; credentials stay local.
5. Codex fixes failed tests or observed UI discrepancies; repeat test/build.
6. Maaz reviews diffs and explicitly authorizes the corresponding Git commits/pushes.
7. Only then start the next phase.

**Deliverable standard:** every phase leaves the app buildable and testable, with no false claims about unexecuted GUI tests.

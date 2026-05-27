# AGENTS.md — Diary Lite

This repository contains **Diary Lite**, a local-first Android diary app for writing, browsing, searching, and exporting personal diary entries.

Use this file as the durable project instruction for Codex. Follow it before making code changes.

---

## 0. Non-negotiable Rules

These rules override lower-level implementation choices:

* User-facing UI text must be Korean and stored in Android string resources.
* Korean strings must exist in the default `res/values/strings.xml`; the app must show Korean text even when the device locale is not Korean.
* Diary data must remain local by default.
* Do not add network APIs, analytics, ads, tracking, cloud sync, account systems, social features, or AI features.
* Do not log diary title, content, mood selections tied to entries, or search queries.
* Implement only the requested `/goal`.
* Do not implement Post-MVP features unless explicitly requested.
* Release builds must not call Room `fallbackToDestructiveMigration` or use any destructive migration path.

---

## 1. Product Intent

Diary Lite is a private, local-first Android diary app for one person to write and manage personal journal entries.

Core product principle:

> Make daily writing fast, keep diary data private and local, and provide simple date-based browsing, search, and export without cloud accounts, social features, or server-side storage.

Do not expand the product beyond this principle unless the user explicitly asks.

Diary Lite is a separate app from MoneyFlow Lite. Do not mix diary features into the MoneyFlow Lite codebase unless explicitly requested.

---

## 2. Current Product Scope

### MVP Scope

Implement only the following for the MVP:

* Create diary entry
* Edit diary entry
* Delete diary entry with confirmation
* Diary entry detail view
* Diary entry list ordered by the canonical recent/list ordering
* Calendar month view with entry indicators
* Date-specific diary entry list
* Basic title/content search
* Optional mood selection using fixed seed moods
* Markdown export for all diary entries
* Light/Dark mode using Material Design 3
* Offline-first local data storage
* Korean user-facing UI text

### Post-MVP Scope

Do not implement these unless explicitly requested:

* Image attachment
* Audio attachment
* Video attachment
* Drawing/handwriting
* Rich text editor
* Full Markdown editor with preview
* Tags
* Favorite/bookmark entries
* Trash/recycle bin
* App lock with PIN or biometrics
* Database encryption
* JSON backup/restore
* Encrypted backup file
* Google Drive backup/restore
* Cloud sync
* Multi-device sync
* Account system
* Social sharing/community features
* AI writing assistant
* AI sentiment analysis
* AI summary
* AI memory/search
* Location tracking
* Weather auto-fetch
* Push notification reminders
* Widgets
* Ads, analytics SDKs, or tracking SDKs

---

## 3. Technology Decisions

Use the following stack unless the repository already contains a different, working standard.

* Language: Kotlin
* Platform: Android
* UI: Jetpack Compose
* Design: Material Design 3
* Persistence: Room over SQLite
* Architecture: MVVM + Repository pattern
* Async/state: Kotlin Coroutines, Flow, StateFlow
* Date/time API: Use `java.time` (`LocalDate`, `LocalTime`) with core library desugaring when `minSdk < 26`; if `minSdk >= 26`, desugaring is optional
* Build: Gradle Kotlin DSL if creating a new project
* DI: Prefer manual dependency wiring for MVP unless Hilt is already configured
* Search: Use simple Room queries for MVP; do not add a search dependency
* Editor: Use plain text input for MVP; do not add rich text or Markdown editor dependencies
* Calendar: Prefer custom Compose calendar UI for MVP; do not add calendar libraries unless clearly justified
* Export: Use Android Storage Access Framework for user-visible file creation

Avoid unnecessary production dependencies. If a dependency is required, explain why in the final response and prefer stable AndroidX/Jetpack libraries.

---

## 4. Expected Repository Shape

If creating the project from scratch, prefer this structure:

```text
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    java/com/diarylite/app/
      MainActivity.kt
      DiaryLiteApp.kt
      data/
        local/
          DiaryDatabase.kt
          dao/
            DiaryEntryDao.kt
            MoodDao.kt
          entity/
            DiaryEntryEntity.kt
            MoodEntity.kt
        repository/
          DiaryRepositoryImpl.kt
      domain/
        model/
          DiaryEntry.kt
          Mood.kt
        repository/
          DiaryRepository.kt
        usecase/
          CreateDiaryEntryUseCase.kt
          UpdateDiaryEntryUseCase.kt
          DeleteDiaryEntryUseCase.kt
          GetEntriesByDateUseCase.kt
          SearchDiaryEntriesUseCase.kt
          ExportDiaryMarkdownUseCase.kt
      presentation/
        navigation/
          DiaryNavGraph.kt
          DiaryRoute.kt
        screen/
          home/
          entries/
          calendar/
          editor/
          detail/
          search/
          settings/
        component/
        theme/
      util/
        DateTimeFormatter.kt
        MarkdownExporter.kt
    res/
      values/
        strings.xml      # Korean default user-facing strings; required
        colors.xml
        themes.xml
      values-ko/         # optional; do not make this the only Korean string source
        strings.xml

gradle/
  libs.versions.toml

README.md
AGENTS.md
```

If the repo already has a structure, follow the existing structure instead of forcing this one.

For the Korean-only MVP, default strings in `res/values/strings.xml` must be Korean. `res/values-ko/strings.xml` is optional and should not be the only place where Korean text exists. The app must show Korean text even when the device locale is not Korean. Use locale-specific folders only when maintaining multiple app languages.

---

## 5. Data Model Requirements

### DiaryEntry

Use this as the canonical diary entry model.

```text
DiaryEntry
- id: Long
- title: String?
- content: String
- entryDateEpochDay: Long
- entryTimeMinute: Int?
- moodCode: String?
- createdAt: Long
- updatedAt: Long
```

Rules:

* `content` is required.
* `content` must not be blank after trimming.
* Validate content using `content.trim().isNotEmpty()`.
* Store the original content as entered, and do not transform line breaks.
* `title` is optional.
* `entryDateEpochDay` represents the local date using `LocalDate.toEpochDay()`.
* `entryTimeMinute` is optional and represents minutes after midnight in local time.
* If `entryTimeMinute` is present, it must be between `0` and `1439`.
* `createdAt` and `updatedAt` are epoch milliseconds.
* `createdAt` is set once on insert.
* `updatedAt` changes on every update.
* Diary entries must use deterministic ordering:
  * Recent/list/search order: `entryDateEpochDay` descending, timed entries before untimed entries on the same date, `entryTimeMinute` descending with nulls last, `updatedAt` descending, then `id` descending.
  * Export order: `entryDateEpochDay` ascending, timed entries before untimed entries on the same date, `entryTimeMinute` ascending with nulls last, `createdAt` ascending, then `id` ascending.
* Multiple diary entries per date are allowed.
* Do not store rendered Markdown or HTML in the database.
* Do not store diary content in logs.

### Mood

Use fixed seed moods for MVP.

```text
Mood
- code: String
- sortOrder: Int
- isActive: Boolean
```

Rules:

* `Mood.code` is a stable internal identifier.
* User-facing mood labels must come from string resources.
* User-facing mood labels must be Korean.
* Internal mood identifiers must remain stable and may use English.
* Mood selection is optional.
* MVP mood list is fixed seed data.
* Do not implement mood editing for MVP.

Default mood codes:

```text
happy
calm
normal
tired
sad
angry
anxious
excited
grateful
```

Suggested Korean display names:

```text
happy: 기쁨
calm: 평온
normal: 보통
tired: 피곤
sad: 슬픔
angry: 화남
anxious: 불안
excited: 설렘
grateful: 감사
```

Implementation rule:

* Store only `moodCode` in diary entries.
* Display names must be resolved through string resources.
* Do not store Korean display names in the database.

---

## 6. Date and Time Rules

Diary Lite is date-centered.

Rules:

* The diary date is based on the device local timezone.
* Use `LocalDate` for diary calendar dates.
* Store diary date as `entryDateEpochDay`.
* Store optional diary time as `entryTimeMinute`.
* Use `createdAt` and `updatedAt` only for metadata.
* Do not use `createdAt` as the diary date.
* Date-specific queries must use `entryDateEpochDay`.
* Month-specific queries must calculate local month start and end as epoch day range:

  * `entryDateEpochDay >= startEpochDay`
  * `entryDateEpochDay < endEpochDay`
* Do not group diary entries by formatted date strings in SQL.
* Calendar week starts on Sunday for Korean-style month display unless explicitly changed later.

---

## 7. UX Requirements

### 7.0 Language and Localization Policy

Diary Lite is intended for Korean personal use.

Rules:

* All user-facing app text must be written in Korean.
* Compose UI must not contain hardcoded Korean strings directly.
* Put user-facing strings in Android string resources.
* Use Korean strings in the default `res/values/strings.xml`.
* `res/values-ko/strings.xml` is optional and should not be the only place where Korean text exists.
* The app must show Korean text even when the device locale is not Korean.
* Internal code identifiers, package names, class names, function names, database column names, and enum values should remain in English.
* Mood display names shown to the user must be Korean.
* Mood internal identifiers may remain English for stability.
* Error messages, empty states, buttons, labels, dialogs, settings text, and export-related messages must be Korean.
* Developer comments should be minimal. If comments are necessary, English or Korean is acceptable, but user-facing copy must always be Korean.

### Main Navigation

MVP screens:

* Home

  * Today writing shortcut
  * Recent diary entries
  * Calendar shortcut
  * Search shortcut

* Entries

  * Diary entry list
  * Sorted by the canonical recent/list ordering
  * Shows title, date, time, mood, and content preview

* Calendar

  * Month view
  * Dates with diary entries are visually indicated
  * Selecting a date shows entries for that date

* Entry Detail

  * Full title/content
  * Date/time
  * Mood
  * Edit action
  * Delete action

* Add/Edit Entry

  * Create new diary entry
  * Edit existing diary entry

* Search

  * Search title and content
  * Shows matching diary entries

* Settings

  * Markdown export
  * Privacy note
  * App info

Use Compose Navigation if navigation is needed.

### Navigation Routes

Navigation should support:

```text
home
entries
calendar
entry/add
entry/add?entryDateEpochDay={entryDateEpochDay}
entry/edit/{entryId}
entry/detail/{entryId}
search
settings
```

Rules:

* `entry/add` creates a new diary entry.
* `entry/add?entryDateEpochDay={entryDateEpochDay}` creates a new diary entry with the date preselected from a local-date epoch day.
* `entry/edit/{entryId}` edits an existing diary entry.
* `entry/detail/{entryId}` displays an existing diary entry.
* Do not pass full diary content through navigation arguments.
* Pass only stable IDs or simple date parameters through routes.

### Home Screen

Home should be simple and writing-focused.

MVP Home elements:

* App title
* Today date
* Primary button: write today’s diary
* Recent diary entries
* Empty state when no entries exist
* Shortcut to Calendar
* Shortcut to Search

Home recent entries:

* Show latest 5 entries.
* Sort using the canonical recent/list ordering from the data model rules.
* Show title if present.
* Show content preview if title is missing or as secondary text.
* Do not show full long content on Home.

### Entries Screen

The Entries screen shows diary entries using the canonical recent/list ordering.

Each list item should show:

* Date
* Optional time
* Optional title
* Content preview
* Optional mood label

Rules:

* Use the canonical recent/list ordering from the data model rules.
* Do not rely on color alone to show mood.
* Content preview should be limited.
* Empty state must be shown when there are no entries.
* Tapping an entry opens Entry Detail.

### Calendar Screen

The Calendar screen provides date-based browsing.

Required behavior:

* Shows current month by default.
* User can move to previous/next month.
* User can return to today.
* Dates with at least one diary entry must be indicated.
* Selecting a date shows entries for that date.
* Empty selected dates show a proper empty state.
* User can start writing for the selected date.

Implementation guidance:

* Calendar indicators should be subtle.
* Do not show full diary content directly inside calendar cells.
* Avoid heavy custom layout logic that makes accessibility difficult.
* Keep touch targets adequate.

### Entry Input

Diary input must be simple and predictable.

Required fields:

* Content

Optional fields:

* Title
* Date
* Time
* Mood

Defaults:

* Date: current device local date
* Time: current device local time rounded down to the minute
* Title: empty
* Content: empty
* Mood: none

Validation:

* Disable save if content is blank after trimming.
* Validate content using `content.trim().isNotEmpty()`.
* Store content as the user entered it, preserving line breaks and leading/trailing whitespace.
* Title may be blank.
* Mood may be empty.
* Date must always be valid.
* Time may be cleared if the UI supports time removal.

Unsaved changes:

* If the user attempts to leave the editor with unsaved changes, show a confirmation dialog.
* MVP uses explicit Save.
* Auto-save is not required for MVP.

### Delete Behavior

* Deleting a diary entry must show a confirmation dialog.
* MVP deletion may be hard delete.
* Trash/restore is not required for MVP.
* The confirmation copy must be Korean.
* After deletion, navigate back to the previous appropriate screen.

### Search

MVP search scope:

* Search diary `title`.
* Search diary `content`.

Rules:

* Search is local only.
* Search query must be trimmed before use.
* Escape SQL `LIKE` wildcard characters and the escape character itself: `\`, `%`, and `_`.
* Use an escaped query parameter with Room queries that include `LIKE ... ESCAPE '\'`.
* Use parameterized Room queries only.
* Do not build raw SQL strings from user input.
* Search query must not be logged.
* Search results should use the canonical recent/list ordering from the data model rules.
* Blank search query should show either an empty state or recent entries; choose the simpler MVP-compatible behavior.
* Do not implement fuzzy search for MVP.
* Do not add server-side search.
* Do not add AI search for MVP.

### Accessibility

* Do not rely on color alone to distinguish mood or entry state.
* Use adequate touch targets.
* Respect system font scale.
* Respect system dark mode.
* Use content descriptions for meaningful icons.
* Use string resources, not hardcoded UI text in composables.

---

## 8. Privacy and Data Rules

This app handles highly personal diary content.

Required:

* Store diary data locally by default.
* Do not add external servers.
* Do not add analytics SDKs.
* Do not add ad SDKs.
* Do not add tracking SDKs.
* Do not add crash/reporting SDKs unless explicitly requested.
* Do not log diary titles.
* Do not log diary content.
* Do not log search queries.
* Do not log mood selections tied to entries.
* Do not send diary data over the network.
* Do not request unnecessary permissions.
* Do not request location permission for MVP.
* Do not request contacts permission.
* Do not request microphone permission.
* Do not request camera permission.
* Do not request notification permission for MVP.
* Export is allowed because it is a user-initiated local data portability feature.

Privacy copy in Settings should clearly state:

```text
이 앱은 일기 데이터를 기본적으로 기기에만 저장합니다.
서버 동기화, 광고, 분석 기능은 사용하지 않습니다.
```

---

## 9. Security Guidance

MVP does not require database encryption or app lock unless explicitly requested.

However, code should be structured so these can be added later.

Guidance:

* Keep persistence access behind repositories.
* Avoid spreading database access throughout the UI layer.
* Do not introduce network APIs.
* Do not store diary data in SharedPreferences.
* Do not expose diary content in notifications for MVP.
* Do not write diary content to temporary files except during explicit export.
* During export, write only to the user-selected destination.

Post-MVP security features may include:

* PIN lock
* Biometric unlock
* SQLCipher or equivalent database encryption
* Encrypted backup files
* Auto-lock after inactivity

Do not implement these in MVP unless explicitly requested.

---

## 10. Performance Targets

Design and verify with these targets in mind:

* App startup to recent diary display: under 1 second on a typical development device/emulator
* Save diary entry and reflect in UI: under 300 ms
* Calendar month entry indicator query with 10,000 entries: under 500 ms
* Search over 10,000 entries: under 700 ms for simple local LIKE query on a typical development device/emulator

Implementation guidance:

* Use Room queries and Flow for reactive updates.
* Keep calendar month queries efficient.
* Query only the month range needed for calendar indicators.
* Avoid loading all rows into memory for every calendar refresh.
* Avoid blocking the main thread.
* Use paging only if entry counts become large enough to justify it; do not add Paging for MVP unless clearly needed.

Required MVP indexes:

```text
DiaryEntry.entryDateEpochDay
DiaryEntry.updatedAt
```

Optional post-MVP index if mood filtering is added:

```text
DiaryEntry.moodCode
```

For search, simple MVP queries may use title/content `LIKE` with escaped parameters and an explicit `ESCAPE` clause. If search becomes slow, consider FTS as a post-MVP improvement.

---

## 11. Markdown Export Requirements

MVP export format: Markdown only.

Export all diary entries in MVP.

### Export File

Suggested filename:

```text
diary_export_yyyyMMdd_HHmm.md
```

Rules:

* Use UTF-8.
* Use Android Storage Access Framework with `ACTION_CREATE_DOCUMENT` or a safe user-visible file creation flow.
* Suggested MIME type is `text/markdown`; fall back to `text/plain` if needed.
* Suggested file extension is `.md`.
* Do not require internet for export.
* Export all entries.
* Sort exported entries using the canonical export ordering from the data model rules.
* Export should be user-initiated only.
* Export errors must be surfaced to the user in Korean.
* Do not silently fail.

### Markdown Structure

Use this general structure:

```markdown
# Diary Lite 내보내기

내보낸 시각: yyyy-MM-dd HH:mm

---

## yyyy-MM-dd

### 제목

- 시간: HH:mm
- 기분: 기쁨

일기 내용

---
```

Rules:

* If title is empty, use a localized fallback heading such as `제목 없음`.
* If time is missing, omit the `시간` line.
* If mood is missing, omit the `기분` line.
* Preserve diary content line breaks.
* Do not alter user content unnecessarily.
* Do not include internal database IDs by default.
* Do not include deleted entries because MVP uses hard delete.
* Fixed labels in exported Markdown must be Korean user-facing text resolved from string resources.
* `MarkdownExporter` must not hardcode Korean labels directly. Pass localized labels from the Android layer or a resource-backed provider.

### Post-MVP Export/Backup

Do not implement these in MVP unless explicitly requested:

* JSON backup
* JSON restore
* Encrypted backup
* Per-date export
* Export selected entries
* PDF export
* DOCX export
* HTML export
* Cloud backup

---

## 12. Architecture Rules

Use a simple layered architecture.

Recommended flow:

```text
UI Composable
  -> ViewModel
  -> UseCase if useful, otherwise Repository
  -> Repository
  -> Room DAO
```

Rules:

* Keep Composables mostly stateless where practical.
* ViewModels expose immutable UI state via StateFlow.
* DAOs should not be accessed directly from UI.
* Repositories should hide persistence details from ViewModels.
* Use domain models in presentation/domain layers.
* Map Room entities at the repository boundary if complexity warrants it.
* Do not over-engineer.
* For MVP, fewer well-named classes are better than excessive abstraction.
* Keep export formatting separate from ViewModels.
* Keep date/time conversion utilities centralized.

Suggested domain/use case boundaries:

* Creating/updating/deleting diary entries may use use cases if validation grows.
* Calendar month query can be handled by repository or use case.
* Markdown export should have a dedicated exporter class or use case.
* UI state should not expose Room entities directly if domain models already exist.

---

## 13. Room Schema Guidance

### Migration Policy

* Define an explicit Room database version.
* Release builds must not call Room `fallbackToDestructiveMigration` or use any destructive migration path.
* Debug/local pre-release builds may use destructive migration only when explicitly stated for that task and never for user data expected to be retained.
* After release, schema changes must include Room migrations.
* Treat diary data as personal data; avoid migration shortcuts that risk data loss.

### DAO Query Guidance

* Implement canonical recent/list/search ordering in SQL with timed entries before untimed entries, for example `CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC`, then `entryTimeMinute DESC`, `updatedAt DESC`, and `id DESC`.
* Implement canonical export ordering in SQL with timed entries before untimed entries, for example `CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC`, then `entryTimeMinute ASC`, `createdAt ASC`, and `id ASC`.
* Search DAO methods must use escaped query parameters and `LIKE ... ESCAPE '\'`; do not concatenate user input into SQL.

### DiaryEntryEntity

Recommended fields:

```text
id: Long, primary key, autoGenerate
title: String?
content: String
entryDateEpochDay: Long
entryTimeMinute: Int?
moodCode: String?
createdAt: Long
updatedAt: Long
```

Recommended constraints:

* `content` must not be empty at validation layer.
* Store content as entered after validation; do not trim or normalize line breaks before persistence.
* `entryTimeMinute` should be null or between 0 and 1439.
* `createdAt` and `updatedAt` are set in repository/use case layer.
* Index `entryDateEpochDay`.
* Index `updatedAt`.
* Index `moodCode` only if mood filtering is added.

### MoodEntity

Recommended fields:

```text
code: String, primary key
sortOrder: Int
isActive: Boolean
```

Seed moods during database creation or migration.

Rules:

* Mood seed data must be stable.
* Do not delete or rename existing mood codes casually once released.
* Korean display names belong in string resources, not the database.

---

## 14. Error Handling

User-facing errors must be Korean.

Common error cases:

* Save failed
* Delete failed
* Entry not found
* Export failed
* File creation canceled
* Search failed

Rules:

* Do not show raw exception messages to the user.
* Do not expose stack traces.
* Log only non-sensitive technical details in debug builds if necessary.
* Never log diary title/content/search text.

Suggested Korean messages:

```text
일기를 저장하지 못했습니다.
일기를 삭제하지 못했습니다.
일기를 찾을 수 없습니다.
내보내기에 실패했습니다.
파일 생성이 취소되었습니다.
검색 중 문제가 발생했습니다.
```

---

## 15. Testing and Verification

Before marking a task complete, run the most relevant checks available in the repo.

Preferred commands:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

If the project uses different variants or commands, discover and use the existing commands.

For database or repository changes, add or update unit tests where feasible.

For export changes, test Markdown formatting and sorting.

For date/calendar changes, test:

* Today date
* Month start
* Month end
* Entry grouping by local date
* Multiple entries on the same date
* Empty month

For UI-only changes, at minimum run build/lint. Add Compose UI tests only when the test infrastructure already exists or the user asks for it.

When checks fail:

1. Read the error.
2. Fix the root cause.
3. Re-run the relevant check.
4. Report any remaining failure clearly.

Do not claim success unless the relevant command passed or you explicitly state why it could not be run.

---

## 16. `/goal` Workflow Guidance

The user intends to use Codex's `/goal` workflow. Treat each `/goal` as a durable implementation milestone, not as a vague chat request.

For every `/goal` task:

1. Restate the goal in implementation terms.
2. Identify the relevant files or modules.
3. List constraints from this AGENTS.md that apply.
4. Define "Done when" before coding.
5. Implement only the requested goal.
6. Run relevant verification commands.
7. Summarize changed files, behavior, and test results.

Do not mix unrelated goals in one implementation pass.

If a goal is broad, split it into smaller milestones and ask the user to approve the next goal, unless the user explicitly asks you to proceed through the full sequence.

---

## 17. Recommended MVP Goal Backlog

Use these as suggested `/goal` units.

### Goal 0 — Project Scaffold

Build or verify the base Android project structure.

Done when:

* The app builds successfully.
* MainActivity launches a Compose app shell.
* Material 3 theme supports system light/dark mode.
* Basic navigation placeholders exist for Home, Entries, Calendar, Search, Editor, Detail, and Settings.
* All visible placeholder strings are Korean and stored in the default `res/values/strings.xml`.
* Build passes.

### Goal 1 — Room Database and Seed Moods

Implement local persistence foundation.

Done when:

* Room database is configured.
* Room database has an explicit version and follows the migration policy in this document.
* DiaryEntry and Mood entities exist.
* DAOs support insert, update, delete, get by id, recent entries, entries by date, entries by month range, and basic search.
* Default moods are seeded.
* Database/repository tests are added where feasible.
* Build and relevant tests pass.

### Goal 2 — Create Diary Entry

Implement manual diary writing.

Done when:

* User can create a diary entry.
* Content is required.
* Content validation uses trimmed content, but persisted content preserves the original user input and line breaks.
* Title, date, time, and mood are optional or defaulted correctly.
* Save is disabled when content is blank.
* Saved entry is persisted and emitted by repository queries.
* No full calendar implementation is required in this goal.
* Build and relevant tests pass.

### Goal 3 — Entry List and Detail View

Implement diary browsing by list.

Done when:

* User can see diary entries ordered by the canonical recent/list ordering.
* List items show date, optional time, title or content preview, and optional mood.
* Empty state is shown when no entries exist.
* User can open an entry detail screen.
* Entry detail shows full content and metadata.
* Build and relevant tests pass.

### Goal 4 — Edit and Delete Diary Entry

Implement entry modification.

Done when:

* User can edit an existing diary entry.
* User can update title, content, date, time, and mood.
* `updatedAt` changes on update.
* User can delete after confirmation.
* List/detail data reacts to changes.
* Unsaved changes confirmation exists when leaving editor with modifications.
* Build and relevant tests pass.

### Goal 5 — Calendar Month View

Implement date-based diary browsing.

Done when:

* Calendar shows the current month by default.
* User can navigate previous/next month.
* Calendar weeks start on Sunday.
* Dates with diary entries are indicated.
* Selecting a date shows entries for that date.
* Empty selected dates show a proper empty state.
* User can create an entry for the selected date.
* Calendar queries use local date epoch-day range.
* Build and relevant tests pass.

### Goal 6 — Search

Implement local diary search.

Done when:

* User can search diary title and content.
* Search query is trimmed; `\`, `%`, and `_` are escaped; Room queries use parameters and `LIKE ... ESCAPE '\'`.
* Results are shown using the canonical recent/list ordering.
* Blank search state is handled clearly.
* No diary content or search query is logged.
* Search errors are surfaced in Korean.
* Build and relevant tests pass.

### Goal 7 — Markdown Export and Settings

Implement local data export.

Done when:

* Settings has Markdown export entry point.
* User can export all diary entries.
* Export uses Android Storage Access Framework with `ACTION_CREATE_DOCUMENT` or equivalent safe file creation flow.
* Export uses `.md` extension and `text/markdown`, falling back to `text/plain` if needed.
* Markdown includes date, optional time, optional mood, optional title, and content.
* Exported entries are sorted using the canonical export ordering.
* Export preserves line breaks.
* Export errors are surfaced to the user in Korean.
* Build and relevant tests pass.

### Goal 8 — MVP Polish and Verification

Prepare MVP for personal use.

Done when:

* All MVP flows work together.
* UI strings are centralized in string resources.
* No diary title/content/search query is logged.
* Light/dark mode is acceptable.
* Empty states are clear.
* README includes setup and run instructions.
* Lint/build/tests pass or failures are explicitly documented.

---

## 18. Post-MVP Goal Backlog

Use these only after MVP is complete or when explicitly requested.

### Post-MVP Goal A — App Lock

Add local app protection.

Possible scope:

* PIN lock
* Biometric unlock
* Auto-lock after inactivity
* Korean lock screen text

Do not implement before MVP unless explicitly requested.

### Post-MVP Goal B — JSON Backup and Restore

Add structured backup/restore.

Possible scope:

* Export all diary entries as JSON
* Restore from JSON
* Validate backup schema
* Handle duplicate entries
* Show restore summary

Do not add cloud backup as part of this goal.

### Post-MVP Goal C — Tags

Add flexible organization.

Possible scope:

* Create tags
* Assign tags to entries
* Filter by tag
* Rename/delete tags
* Tag search

Do not add AI auto-tagging unless explicitly requested.

### Post-MVP Goal D — Favorites

Add entry bookmarking.

Possible scope:

* Mark entry as favorite
* Favorites list
* Favorite filter

### Post-MVP Goal E — Rich Text or Markdown Editor

Add improved writing experience.

Possible scope:

* Markdown shortcuts
* Preview mode
* Basic formatting toolbar

Do not add heavy editor dependencies without justification.

### Post-MVP Goal F — Attachments

Add media support.

Possible scope:

* Image attachments
* Local-only storage
* Attachment deletion
* Export behavior

Do not add camera/microphone permissions unless the exact attachment feature requires them.

### Post-MVP Goal G — Database Encryption

Add stronger local privacy.

Possible scope:

* SQLCipher or equivalent
* Migration from unencrypted DB
* Recovery guidance
* Lock integration

Do not implement casually because migration and data-loss risk are significant.

---

## 19. Done Definition

A task is done only when all applicable items are true:

* The implemented behavior matches the requested goal.
* MVP scope was not exceeded without permission.
* Code builds.
* Relevant tests/lint were run.
* New code follows the architecture rules above.
* Diary data remains local.
* Diary content is not logged.
* Search queries are not logged.
* UI text is in string resources.
* User-facing strings are Korean.
* The final response lists changed files and verification results.

---

## 20. Commit and PR Expectations

When asked to commit or prepare a PR:

* Keep each commit focused on one goal.
* Use concise commit messages.
* Mention the user-visible behavior change.
* Mention test/build commands run.
* Do not include generated build artifacts.
* Do not commit local secrets, keystores, API keys, or machine-specific files.
* Do not commit exported diary files.
* Do not commit sample diary content containing real personal information.

Suggested commit style:

```text
feat: add diary entry database
feat: implement diary editor
feat: add calendar entry indicators
feat: export diary entries as markdown
fix: preserve line breaks in markdown export
refactor: separate diary repository
```

---

## 21. Assumption Policy

If something is ambiguous:

1. Prefer the simplest MVP-compatible choice.
2. Do not implement post-MVP functionality by accident.
3. Record meaningful assumptions in the final response.
4. Ask the user only when the decision materially affects product behavior, schema, or dependency choices.

Default assumptions:

* App name is Diary Lite unless renamed later.
* The app is for Korean personal use.
* User-facing UI language is Korean.
* Local timezone is the device timezone.
* Multiple diary entries per date are allowed.
* Diary content is plain text for MVP.
* Mood selection is optional.
* Mood list is fixed for MVP.
* Hard delete is acceptable for MVP.
* Markdown export is enough for MVP.
* Cloud sync is not needed.
* App lock is post-MVP unless explicitly requested.

---

## 22. Do Not Do

* Do not add cloud sync.
* Do not add Google Drive backup until requested.
* Do not add authentication.
* Do not add server APIs.
* Do not add social features.
* Do not add public sharing.
* Do not add AI writing features.
* Do not add AI analysis.
* Do not add analytics, ads, or tracking.
* Do not log diary content.
* Do not log diary titles.
* Do not log search queries.
* Do not request unnecessary Android permissions.
* Do not add image/audio/video attachments until requested.
* Do not add rich text editor dependencies until requested.
* Do not rewrite the whole architecture if a small change is enough.
* Do not combine this app with MoneyFlow Lite unless explicitly requested.

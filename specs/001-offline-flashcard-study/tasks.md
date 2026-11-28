# Tasks — Offline flashcard study and scheduled review

**Release baseline**: v3.0 · 39 tasks, including 16 in Phases 3–6.
Follow the [specification](spec.md), [plan](plan.md), [data model](data-model.md) and
[verification guide](quickstart.md). Paths are relative to the repository root.

## Phase 1 — Project setup and shared configuration

- [X] T001 Add the research-selected Navigation, Lifecycle Compose, Room runtime/compiler and schema plugin, KSP2, Preferences DataStore, coroutines Android/test and desugaring aliases in `gradle/libs.versions.toml`. Each addition must map to the library and version in [research.md](research.md#minimal-catalog-additions-for-implementation).
- [X] T002 Apply the aliases in `build.gradle.kts` and `app/build.gradle.kts`, configure Room schema export to `app/schemas/`, Compose test hosting and java.time desugaring. Preserve the existing Compose BOM, single module, minSdk 24, compile/target 37, Java 11 bytecode and Gradle daemon JDK 25.
- [X] T003 [P] Configure `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/backup_rules.xml` and `app/src/main/res/xml/data_extraction_rules.xml` to keep Room and DataStore data out of legacy backups, cloud backup and device transfer. Inspect the merged release manifest for unnecessary network/storage permissions (NFR-01, NFR-07).
- [X] T004 [P] Apply the approved dark Material 3 palette in `app/src/main/java/com/example/flashcardapp/ui/theme/Color.kt` and `app/src/main/java/com/example/flashcardapp/ui/theme/Theme.kt`. Check the NFR-04 color pairs in the theme, including both grading actions. Measure the rendered controls in T035 (CHK016).
- [X] T005 Use `gradlew` and `specs/001-offline-flashcard-study/quickstart.md` to inspect resolved debug, test and KSP dependencies, build the debug APK, and install and launch the starter on API 24. Record resolved toolchain, API and build result in the quickstart. Verify actual schema generation when v1 exists in T013 (NFR-06, NFR-09).

**Checkpoint passed on 2026-11-22.** The debug starter, manifest, dependencies, tests and
API 24 launch were verified. [Phase 1 evidence](quickstart.md#phase-1-verification)

## Phase 2 — Database, repositories and shared foundations

The 12 reopened foundation tasks were verified against v3.0 and approved. The
[previous checkpoint](quickstart.md#phase-2-foundation-checkpoint) records the earlier baseline.

- [X] T006 Keep profile, set and card validation in `app/src/main/java/com/example/flashcardapp/domain/LibraryValidation.kt` and its JVM boundary tests. Cover `Profile.displayName`, `StudySet.name`, `StudySet.description`, `Flashcard.front` and `Flashcard.back` as defined in [data-model.md §Validation and mutations](data-model.md#validation-and-mutations). Verify blank, trimming, maximum and maximum+1 cases where a length limit applies (BR-01, BR-03, BR-05, SC-02).
- [X] T007 Write pure Kotlin functions for eligibility, stage transitions and recall rate in `app/src/main/java/com/example/flashcardapp/domain/ReviewRules.kt`. In `app/src/test/java/com/example/flashcardapp/domain/ReviewRulesTest.kt` cover all six Remembered intervals, cap, Forgot from every stage, overdue dates, leap day, month/year boundaries and zero/rounded summaries. Supply the local date explicitly (FR-09, FR-11, BR-10, BR-12, BR-13, NFR-06).
- [X] T008 Define the Room profile in `app/src/main/java/com/example/flashcardapp/data/local/Profile.kt` using [data-model.md §Room schema v1](data-model.md#room-schema-v1) for `Profile.id`, `Profile.displayName`, `Profile.createdAt`. Preserve identity across rename.
- [X] T009 Update `app/src/main/java/com/example/flashcardapp/data/local/StudySet.kt` to the four-field entity in [data-model.md §Room schema v1](data-model.md#room-schema-v1), covering `StudySet.id`, `StudySet.ownerId`, `StudySet.name` and `StudySet.description`. Keep the owner index and profile delete cascade, and remove the obsolete entity (FR-02, FR-04, SC-05).
- [X] T010 Define card and review entities and indexes in `app/src/main/java/com/example/flashcardapp/data/local/Flashcard.kt` and `app/src/main/java/com/example/flashcardapp/data/local/ReviewLog.kt`. Map [data-model.md §Room schema v1](data-model.md#room-schema-v1) fields `Flashcard.id`, `Flashcard.setId`, `Flashcard.front`, `Flashcard.back`, `Flashcard.createdAt`, `Flashcard.stage`, `Flashcard.dueDay`, `ReviewLog.sessionId`, `ReviewLog.cardId`, `ReviewLog.grade`, `ReviewLog.reviewedAt`. Preserve the composite session/card primary key and card-to-log cascade.
- [X] T011 Align `ProfileDao.kt` and `LibraryDao.kt` in `app/src/main/java/com/example/flashcardapp/data/local/` with the four-table model. Keep profile metadata and CRUD, and expose only owned set/card CRUD and Set List/Detail projections with total and eligible counts. Verify parent checks, empty counts, card dates and affected rows (FR-01, FR-02, FR-04, FR-05, FR-10).
- [X] T012 Update `app/src/main/java/com/example/flashcardapp/data/local/ReviewDao.kt` to require `profileId: Long` and `setId: Long` on all study reads and grade writes, including `getSessionCounts`. Filter the joined owner and set. Match the library eligibility predicate and queue order in the [data model](data-model.md#set-scoped-study-queries) (FR-08–FR-11, BR-10, BR-11).
- [X] T013 Register the four v1 entities in `app/src/main/java/com/example/flashcardapp/data/local/FlashcardDatabase.kt`. Regenerate and inspect `app/schemas/com.example.flashcardapp.data.local.FlashcardDatabase/1.json` with KSP. Use the approved clean development reset and verify the [four-table schema](data-model.md#room-schema-v1) without a migration from the prior pre-release baseline (NFR-02).
- [X] T014 Implement profile list, create, rename and delete operations in `app/src/main/java/com/example/flashcardapp/data/ProfileRepository.kt` with ProfileDao constructor injection. Use stable IDs and T006 validation. Return the created ID so selection can be retried separately (FR-01, BR-01).
- [X] T015 In `app/src/main/java/com/example/flashcardapp/data/ProfileRepository.kt`, expose the selected profileId using one Preferences DataStore and validate the ID with ProfileDao. Persist explicit selection and clear a deleted ID only if it is still selected. Restore a valid ID. With an absent or deleted ID show the selector for any survivors or creation for zero profiles. Offer retry on read failure. A failed selection can retry the existing ID (FR-01, FR-02, CHK005).
- [X] T016 Align `app/src/main/java/com/example/flashcardapp/data/LibraryRepository.kt` with the owned set/card CRUD and projections. Inject LibraryDao directly, validate and normalize inputs, and check affected rows using the captured profile and relevant parent IDs. Keep schedules and history on edits (FR-02, FR-04, FR-05, FR-10, BR-06, BR-07).
- [X] T017 Align study reads in `app/src/main/java/com/example/flashcardapp/data/ReviewRepository.kt` with a required `setId: Long`. Pass the original profile, set and local day for counts, ordered snapshot and eligible-card lookup. Verify empty results, due-before-new order and skipping unavailable items (FR-08, FR-10, BR-10).
- [X] T018 Update grade submission and `getSessionCounts` in `app/src/main/java/com/example/flashcardapp/data/ReviewRepository.kt` and the DAO transaction to require the original profile and `setId: Long`. Check both scopes, retain an existing session/card grade, and for a new eligible grade insert its log and update exactly one schedule or roll back. Read summary counts with the original profile, set and session (FR-09, FR-11, FR-12, BR-11–BR-13, CHK012).
- [X] T019 Wire one database, one Preferences DataStore and three repositories through their constructors in `app/src/main/java/com/example/flashcardapp/AppContainer.kt`. Register `app/src/main/java/com/example/flashcardapp/FlashcardApplication.kt` in `app/src/main/AndroidManifest.xml` and use a standard ViewModel factory with constructor-injected repositories in `app/src/main/java/com/example/flashcardapp/AppViewModelFactory.kt`. Keep collaborators replaceable by test fixtures. Register each story’s ViewModel constructors when its screens arrive (NFR-06).
- [X] T020 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/LibraryIntegrityTest.kt` to the four-table baseline. Use two profiles with identical set names to verify stale-ID read/write protection, trimmed or cleared set descriptions, edit preservation, and profile/set/card cascades with a file reopen (SC-01, SC-02, SC-05).
- [X] T021 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/ProfileSelectionTest.kt` fixtures to sets and cards. Retain valid and stale selection, one-survivor and empty recovery, read/write failure and retry, and an A set/card write that finishes only for A after selecting B (FR-01, FR-02, FR-12, CHK005).
- [X] T022 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/ReviewQueueTest.kt` to the required set scope. Use multiple sets and two profiles to check counts, lookups, stable date/ID order, empty and future cases, forgotten cards and the next local day or zone change with unchanged saved dates (FR-08, FR-10, SC-03).
- [X] T023 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/GradeCommitTest.kt` to required profile/set scope. Check that a wrong set or owner contributes no summary counts and cannot grade the card. Keep rollback, competing submission, retry and file-reopen checks for the matching set (NFR-02, NFR-08, SC-04).

**Checkpoint passed and approved on 2026-11-28.** Phase 2 is complete. Await explicit user
approval before starting T024 in Phase 3. [Phase 2 v3.0 checkpoint](quickstart.md#phase-2-v30-checkpoint)

## Phase 3 — Local Profiles & Navigation

**Goal and check.** Approve Profile UI, then use the Set List placeholder to verify first
selection, restored selection, recovery and Back isolation.

- [ ] T024 [US3] Build only the stateless Compose content in `app/src/main/java/com/example/flashcardapp/ui/profile/ProfileScreen.kt` with display data and callbacks. Show loading, empty creation, the profile list with an active marker, retry and write feedback. Keep input drafts and named delete confirmation in Compose with `rememberSaveable`. Use inline create and rename controls and explain what profile deletion removes. Add labels to `app/src/main/res/values/strings.xml`, use `secondary` or a foreground with at least 4.5:1 contrast on dark backgrounds and add Compose `@Preview` under `FlashcardAppTheme` for the main states. Stop for UI approval (FR-01, FR-02, FR-12, NFR-04, UI-01).
- [ ] T025 [US3] After T024 approval, implement `app/src/main/java/com/example/flashcardapp/ui/profile/ProfileViewModel.kt` with small sealed Loading, Error and Ready states, simple `Flow` reads and direct `viewModelScope.launch` writes. Keep creation and selection separate and keep dialog state in Compose. Register it in `AppViewModelFactory.kt` and connect `MainActivity.kt` and `ui/navigation/FlashcardNavHost.kt`. Collect state with lifecycle awareness, gate owned routes on validated selection and clear the previous stack, drafts, session and observations on switch. Provide a Set List placeholder under the validated profile with a profile-management entry. T028 supplies the live content (FR-01, FR-02, FR-12, UI-01, UI-03).
- [ ] T026 [US3] Write and run Phase 3 ViewModel tests in `app/src/test/java/com/example/flashcardapp/ui/profile/` and Compose tests in `app/src/androidTest/java/com/example/flashcardapp/ui/profile/`. Cover loading, read recovery, explicit selection including one-survivor recovery, first-run creation or switching, and Back isolation using the validated Set List placeholder. Record the Phase 3 checkpoint before requesting Phase 4 approval (SC-01, UI-01).

## Phase 4 — Study Sets & Flashcards

**Goal and check.** Approve and connect Set List, Set Detail and the shared editor in that
order. Verify owned routes, field validation, counts and saved dates.

- [ ] T027 [US2] Build only stateless `app/src/main/java/com/example/flashcardapp/ui/library/SetListScreen.kt`. Show the active profile, owned sets in stable order, total and eligible counts, Create set and the profile control. Render loading, empty, error and populated states from inputs and expose callbacks. Update `app/src/main/res/values/strings.xml` and add Compose `@Preview` under `FlashcardAppTheme`. Stop for UI approval (FR-02, FR-04, FR-10, UI-03).
- [ ] T028 [US2] After T027 approval, implement `app/src/main/java/com/example/flashcardapp/ui/library/SetListViewModel.kt` using owned set projections. Register it in `AppViewModelFactory.kt` and pass `createSavedStateHandle()` when the route supplies a profile ID, following Inventory. Replace the placeholder with the live Set List route in `ui/navigation/FlashcardNavHost.kt`. Validate its owner against the active profile, fall back safely for stale IDs and refresh counts after local day changes on foreground return. Leave Set Detail and Create set integration to T030 and T032 (FR-02, FR-04, FR-10, UI-03).
- [ ] T029 [US2] Build only stateless `app/src/main/java/com/example/flashcardapp/ui/library/SetDetailScreen.kt`. Show metadata, counts, card rows, saved next dates and callbacks for add, edit, delete and Start study. Include loading, empty, error and no-eligible-card states, and keep named delete confirmation in Compose. Update `app/src/main/res/values/strings.xml` and add Compose `@Preview` under `FlashcardAppTheme`. Stop for UI approval (FR-04, FR-05, FR-09, FR-10, UI-06).
- [ ] T030 [US2] After T029 approval, implement `app/src/main/java/com/example/flashcardapp/ui/library/SetDetailViewModel.kt`, register it in `AppViewModelFactory.kt` with `createSavedStateHandle()` for route IDs, and connect Set List to Set Detail in `ui/navigation/FlashcardNavHost.kt`. Read only the active profile's set and cards. Fall back to the validated profile's Set List or the profile selector for stale IDs. Refresh eligibility on data changes and foreground return and handle confirmed set deletion. Keep editor and study entry callbacks for T032 and T036 (FR-02, FR-04, FR-05, FR-09, FR-10, UI-06).
- [ ] T031 [US2] Build only the shared stateless Compose content in `app/src/main/java/com/example/flashcardapp/ui/library/EditorScreen.kt` for set and card forms. Keep drafts and named discard or deletion confirmations in Compose with `rememberSaveable`. Show field errors and saving feedback, retain draft input after a failed save, and expose save, cancel and confirm callbacks. Update `app/src/main/res/values/strings.xml` and add Compose `@Preview` under `FlashcardAppTheme` for both forms and their key states. Stop for UI approval (FR-04, FR-05, FR-12, UI-05).
- [ ] T032 [US2] After T031 approval, implement `app/src/main/java/com/example/flashcardapp/ui/library/EditorViewModel.kt` for scoped reads, shared validation and set/card writes, using the canonical field rules in [data-model.md](data-model.md#validation-and-mutations) for `StudySet.name`, `StudySet.description`, `Flashcard.front` and `Flashcard.back`. Register it in `AppViewModelFactory.kt` with `createSavedStateHandle()` for route IDs. Connect set and card create/edit routes in `ui/navigation/FlashcardNavHost.kt`. Return to the valid parent after save or discard, and fall back to the validated Set List or profile selector when the target or parent is stale or outside the active profile (FR-02, FR-04, FR-05, FR-12, UI-05).
- [ ] T033 [US2] Write and run Phase 4 Compose tests in `app/src/androidTest/java/com/example/flashcardapp/ui/library/`. Exercise the live scoped Set List, stale routes, one invalid editor input, save/return, draft preservation and count refresh on a new local day without a Room write. Use Phase 2 storage tests for field boundaries and cascades. Record the checkpoint before requesting Phase 5 approval (SC-01, SC-02, SC-03, SC-05).

## Phase 5 — Graded Study & Summary

**Goal and check.** Approve Graded Study before implementing its ViewModel. Approve Review
Summary before connecting the flow. Verify reveal, durable grading, retry and return to the
originating set.

- [ ] T034 [US1] Build only stateless `app/src/main/java/com/example/flashcardapp/ui/study/GradedStudyScreen.kt`. Show the face, original queue position, committed count, Flip, reveal-gated ratings and saving/retry feedback. Expose actions as callbacks and keep exit confirmation visibility in Compose. Update `app/src/main/res/values/strings.xml` and add Compose `@Preview` under `FlashcardAppTheme` for the main states. Stop for UI approval (FR-08, FR-09, FR-12, NFR-04, UI-09).
- [ ] T035 [US1] After T034 approval, implement `app/src/main/java/com/example/flashcardapp/ui/study/GradedStudyViewModel.kt` with small sealed screen states. Capture one set's frozen queue, session and profile IDs. Preserve the original positions, face and reveal state across rotation. Handle empty start, unavailable cards, explicit reveal, atomic grade submission, retry and blocked Exit/Back while saving. Keep exit confirmation state in Compose. Expose committed counts queried with the originating profile, set and session. T036 registers this ViewModel and connects its route (FR-08–FR-12, UI-09).
- [ ] T036 [US1] First build only stateless `app/src/main/java/com/example/flashcardapp/ui/study/ReviewSummaryScreen.kt`. Show committed counts, recall percentage when defined and a Done callback. Update `app/src/main/res/values/strings.xml` and add Compose `@Preview` under `FlashcardAppTheme` for committed and zero-grade summaries. Stop for explicit UI approval without marking T036 complete. After UI approval, register `GradedStudyViewModel` in `AppViewModelFactory.kt` and use `createSavedStateHandle()` for route arguments. Connect Set Detail → Graded Study → Review Summary → originating Set Detail in `ui/navigation/FlashcardNavHost.kt`. Keep an empty start on Set Detail, block Back and Exit during grading, and use the originating study state for summary results. Verify the full wiring before submitting T036 for task approval (FR-08–FR-12, UI-09, UI-10).
- [ ] T037 [US1] Write and run Phase 5 ViewModel tests in `app/src/test/java/com/example/flashcardapp/ui/study/` and Compose tests in `app/src/androidTest/java/com/example/flashcardapp/ui/study/`. Cover frozen queue slots, reveal gating, no grade triggered by recomposition, saving and blocked Back, rollback and retry, empty entry, and return for both committed and zero-grade summaries. Record the checkpoint before requesting Phase 6 approval (SC-03, SC-04, UI-09, UI-10).

## Phase 6 — Final Verification & Offline Demo

- [ ] T038 Run the debug build and all JVM and Android test suites from [quickstart.md](quickstart.md#prepare-and-build). Check the Room export in `app/schemas/`, merged release manifest and a short API 24 study/calendar smoke. Record results, devices and outstanding issues in `quickstart.md` (NFR-02, NFR-06, NFR-07, NFR-09).
- [ ] T039 Run the [offline walkthrough](quickstart.md#offline-course-demo) on a clean installation and record SC-01–SC-06 results in `quickstart.md`. Verify profile isolation, editing, grading, summary, restart, deletion, rotation and reachable controls at 100% and 130% font scale in portrait and landscape. Measure rendered contrast and touch targets and record approval of the final checkpoint (NFR-01, NFR-03–NFR-05, NFR-09).

## Dependencies and approvals

Complete phases and tasks in ID order. Before code, explain the approach and the applicable
course samples. Verify and stop after each task for approval. For each screen, the user must
approve the UI and its themed previews before ViewModel or route wiring begins. T036 has an
explicit UI stop and integration approval under one task ID. Wait for user approval of the
UI before resuming T036 for wiring. Mark a task complete only after verification and approval.

Use `AppViewModelFactory.kt` for screen ViewModels, `createSavedStateHandle()` for route
arguments, and `ui/navigation/FlashcardNavHost.kt` for route wiring. Every screen UI task
includes strings in `app/src/main/res/values/strings.xml` and Compose `@Preview` in
`FlashcardAppTheme`. Follow [AGENTS.md](../../AGENTS.md). At each phase checkpoint, record
evidence in [quickstart.md](quickstart.md) and update [current.md](current.md) and this list
before requesting explicit approval of the next phase.

The completed T004 entry retains its historical reference to T035. Under this plan, T039
owns rendered-control measurements.

## Acceptance and scope index

| Scope | Requirements and business rules | Tasks |
| --- | --- | --- |
| US3 · Local Profiles · SC-01, SC-05 | FR-01, FR-02, FR-12 · BR-01, BR-02, BR-06 · UI-01, UI-03 | T006, T008, T011, T014–T015, T020–T021, T024–T026, T028, T033, T039 |
| US2 · Sets & Cards · SC-01, SC-02, SC-05 | FR-04, FR-05, FR-10, FR-12 · BR-03, BR-05, BR-06, BR-07 · UI-03, UI-05, UI-06 | T006, T009–T011, T013, T016, T020, T027–T033, T039 |
| US1 · Graded Study · SC-03, SC-04 | FR-08, FR-09, FR-10, FR-11, FR-12 · BR-10, BR-11, BR-12, BR-13 · UI-06, UI-09, UI-10 | T007, T010, T012, T017–T018, T022–T023, T029–T030, T034–T037, T039 |
| Release and offline · SC-01 | NFR-01, NFR-02, NFR-07, NFR-09 | T003, T005, T013, T019–T023, T038–T039 |
| Screen usability and accessibility · SC-06 | NFR-03, NFR-04, NFR-05 | T004, T024, T027, T029, T031, T034, T036, T039 |
| Maintainability and reliable writes | NFR-06, NFR-08 | T001–T002, T006–T007, T013, T018–T019, T023, T025, T028, T030, T032, T035–T038 |

See [spec.md](spec.md#requirements-and-traceability) for reserved and deferred requirement IDs.

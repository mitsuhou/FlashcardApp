# Tasks — Offline flashcard study and scheduled review

**Release baseline**: v3.0 · 35 tasks, including 12 in Phases 3–6.
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

Reopen only the existing tasks marked for v3.0 revalidation. For changes with dependent
schema, DAO or repository work still pending, verify the affected definitions or behavior at
the task review and record the integrated build and suites at the Phase 2 checkpoint. The
[previous checkpoint](quickstart.md#phase-2-foundation-checkpoint) remains historical evidence.

- [ ] T006 Keep profile, set and card validation in `app/src/main/java/com/example/flashcardapp/domain/LibraryValidation.kt` and its JVM boundary tests. Cover `Profile.displayName`, `StudySet.name`, `StudySet.description`, `Flashcard.front` and `Flashcard.back` as defined in [data-model.md §Validation and mutations](data-model.md#validation-and-mutations). Verify blank, trimming, maximum and maximum+1 cases where a length limit applies (BR-01, BR-03, BR-05, SC-02). **v3.0 revalidation required.**
- [X] T007 Write pure Kotlin functions for eligibility, stage transitions and recall rate in `app/src/main/java/com/example/flashcardapp/domain/ReviewRules.kt`. In `app/src/test/java/com/example/flashcardapp/domain/ReviewRulesTest.kt` cover all six Remembered intervals, cap, Forgot from every stage, overdue dates, leap day, month/year boundaries and zero/rounded summaries. Supply the local date explicitly (FR-09, FR-11, BR-10, BR-12, BR-13, NFR-06).
- [X] T008 Define the Room profile in `app/src/main/java/com/example/flashcardapp/data/local/Profile.kt` using [data-model.md §Room schema v1](data-model.md#room-schema-v1) for `Profile.id`, `Profile.displayName`, `Profile.createdAt`. Preserve identity across rename.
- [ ] T009 Update `app/src/main/java/com/example/flashcardapp/data/local/StudySet.kt` to the four-field entity in [data-model.md §Room schema v1](data-model.md#room-schema-v1), covering `StudySet.id`, `StudySet.ownerId`, `StudySet.name` and `StudySet.description`. Keep the owner index and profile delete cascade, and remove the obsolete entity (FR-02, FR-04, SC-05). **v3.0 revalidation required.**
- [X] T010 Define card and review entities and indexes in `app/src/main/java/com/example/flashcardapp/data/local/Flashcard.kt` and `app/src/main/java/com/example/flashcardapp/data/local/ReviewLog.kt`. Map [data-model.md §Room schema v1](data-model.md#room-schema-v1) fields `Flashcard.id`, `Flashcard.setId`, `Flashcard.front`, `Flashcard.back`, `Flashcard.createdAt`, `Flashcard.stage`, `Flashcard.dueDay`, `ReviewLog.sessionId`, `ReviewLog.cardId`, `ReviewLog.grade`, `ReviewLog.reviewedAt`. Preserve the composite session/card primary key and card-to-log cascade.
- [ ] T011 Align `ProfileDao.kt` and `LibraryDao.kt` in `app/src/main/java/com/example/flashcardapp/data/local/` with the four-table model. Keep profile metadata and CRUD, and expose only owned set/card CRUD and Set List/Detail projections with total and eligible counts. Verify parent checks, empty counts, card dates and affected rows (FR-01, FR-02, FR-04, FR-05, FR-10). **v3.0 revalidation required.**
- [ ] T012 Update `app/src/main/java/com/example/flashcardapp/data/local/ReviewDao.kt` to require `profileId: Long` and `setId: Long` on all study reads and grade writes, including `getSessionCounts`. Filter the joined owner and set. Match the library eligibility predicate and queue order in the [data model](data-model.md#set-scoped-study-queries) (FR-08–FR-11, BR-10, BR-11). **v3.0 revalidation required.**
- [ ] T013 Register the four v1 entities in `app/src/main/java/com/example/flashcardapp/data/local/FlashcardDatabase.kt`. Regenerate and inspect `app/schemas/com.example.flashcardapp.data.local.FlashcardDatabase/1.json` with KSP. Use the approved clean development reset and verify the [four-table schema](data-model.md#room-schema-v1) without a migration from the prior pre-release baseline (NFR-02). **v3.0 revalidation required.**
- [X] T014 Implement profile list, create, rename and delete operations in `app/src/main/java/com/example/flashcardapp/data/ProfileRepository.kt` with ProfileDao constructor injection. Use stable IDs and T006 validation. Return the created ID so selection can be retried separately (FR-01, BR-01).
- [X] T015 In `app/src/main/java/com/example/flashcardapp/data/ProfileRepository.kt`, expose the selected profileId using one Preferences DataStore and validate the ID with ProfileDao. Persist explicit selection and clear a deleted ID only if it is still selected. Restore a valid ID. With an absent or deleted ID show the selector for any survivors or creation for zero profiles. Offer retry on read failure. A failed selection can retry the existing ID (FR-01, FR-02, CHK005).
- [ ] T016 Align `app/src/main/java/com/example/flashcardapp/data/LibraryRepository.kt` with the owned set/card CRUD and projections. Inject LibraryDao directly, validate and normalize inputs, and check affected rows using the captured profile and relevant parent IDs. Keep schedules and history on edits (FR-02, FR-04, FR-05, FR-10, BR-06, BR-07). **v3.0 revalidation required.**
- [ ] T017 Align study reads in `app/src/main/java/com/example/flashcardapp/data/ReviewRepository.kt` with a required `setId: Long`. Pass the original profile, set and local day for counts, ordered snapshot and eligible-card lookup. Verify empty results, due-before-new order and skipping unavailable items (FR-08, FR-10, BR-10). **v3.0 revalidation required.**
- [ ] T018 Update grade submission and `getSessionCounts` in `app/src/main/java/com/example/flashcardapp/data/ReviewRepository.kt` and the DAO transaction to require the original profile and `setId: Long`. Check both scopes, retain an existing session/card grade, and for a new eligible grade insert its log and update exactly one schedule or roll back. Read summary counts with the original profile, set and session (FR-09, FR-11, FR-12, BR-11–BR-13, CHK012). **v3.0 revalidation required.**
- [X] T019 Wire one database, one Preferences DataStore and three repositories through their constructors in `app/src/main/java/com/example/flashcardapp/AppContainer.kt`. Register `app/src/main/java/com/example/flashcardapp/FlashcardApplication.kt` in `app/src/main/AndroidManifest.xml` and use a standard ViewModel factory with constructor-injected repositories in `app/src/main/java/com/example/flashcardapp/AppViewModelFactory.kt`. Keep collaborators replaceable by test fixtures. Register each story’s ViewModel constructors when its screens arrive (NFR-06).
- [ ] T020 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/LibraryIntegrityTest.kt` to the four-table baseline. Use two profiles with identical set names to verify stale-ID read/write protection, trimmed or cleared set descriptions, edit preservation, and profile/set/card cascades with a file reopen (SC-01, SC-02, SC-05). **v3.0 revalidation required.**
- [ ] T021 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/ProfileSelectionTest.kt` fixtures to sets and cards. Retain valid and stale selection, one-survivor and empty recovery, read/write failure and retry, and an A set/card write that finishes only for A after selecting B (FR-01, FR-02, FR-12, CHK005). **v3.0 revalidation required.**
- [ ] T022 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/ReviewQueueTest.kt` to the required set scope. Use multiple sets and two profiles to check counts, lookups, stable date/ID order, empty and future cases, forgotten cards and the next local day or zone change with unchanged saved dates (FR-08, FR-10, SC-03). **v3.0 revalidation required.**
- [ ] T023 Adapt `app/src/androidTest/java/com/example/flashcardapp/data/GradeCommitTest.kt` to required profile/set scope. Check that a wrong set or owner contributes no summary counts and cannot grade the card. Keep rollback, competing submission, retry and file-reopen checks for the matching set (NFR-02, NFR-08, SC-04). **v3.0 revalidation required.**

**Checkpoint.** Inspect the four-table v1 export and rerun the JVM and device foundation
suites. Verify owned selection and CRUD, queues and summary counts scoped to a set, grade
rollback/retry and file reopen. Record actual results before requesting Phase 3 approval.

## Phase 3 — Local Profiles & Navigation

- [ ] T024 [US3] Implement `ProfileViewModel.kt` and `ProfileScreen.kt` in `app/src/main/java/com/example/flashcardapp/ui/profile/`. Expose creation, selector, active and retry states, create and select separately, and support rename and confirmed deletion with retained input (FR-01, FR-02, FR-12, UI-01).
- [ ] T025 [US3] Wire `MainActivity.kt`, `AppViewModelFactory.kt` and `ui/navigation/FlashcardNavHost.kt` under `app/src/main/java/com/example/flashcardapp/`. Gate owned destinations on validated selection, navigate directly to the selected profile's Set List route, and clear the previous stack, drafts, session and observations on switch. Connect live Set List content in T027 (FR-01, FR-02, UI-01, UI-03).
- [ ] T026 [US3] Add representative profile ViewModel and Compose checks in the existing test roots under `ui/profile/`. Exercise first-run creation or switching, validated routing into Set List and Back isolation. Cover loading, recovery and one-survivor behavior at the ViewModel/data boundary (SC-01, UI-01).

## Phase 4 — Study Sets & Flashcards

- [ ] T027 [US2] Implement and connect `SetListViewModel.kt` and `SetListScreen.kt` under `app/src/main/java/com/example/flashcardapp/ui/library/`. Show the active profile, owned sets in stable order, total and eligible counts, Create set and the profile control. Refresh counts on local day changes when returning to the foreground (FR-02, FR-04, FR-10, UI-03).
- [ ] T028 [US2] Implement and connect `SetDetailViewModel.kt` and `SetDetailScreen.kt` in `ui/library/`. Show metadata, counts, editable card rows and saved next dates. Offer add/edit/deletion actions and a Start study entry for T031–T032. Refresh eligibility after data changes and foreground return (FR-04, FR-05, FR-09, FR-10, UI-06).
- [ ] T029 [US2] Implement and connect shared `EditorViewModel.kt` and `EditorScreen.kt` in `ui/library/`. Wire set and card create/edit, labeled errors, save and return to parent, draft retention on failure/rotation and named discard/deletion confirmations (FR-04, FR-05, FR-12, UI-05).
- [ ] T030 [US2] Add representative library/editor Compose tests in `app/src/androidTest/java/com/example/flashcardapp/ui/library/`. Check scoped Set List and stale routes, invalid input, save/return and count refresh after a local day changes without a Room write. Use the Phase 2 tests for field boundaries and deletion cascades (SC-01, SC-02, SC-03, SC-05).

## Phase 5 — Graded Study & Summary

- [ ] T031 [US1] Implement `GradedStudyViewModel.kt` and `GradedStudyScreen.kt` in `app/src/main/java/com/example/flashcardapp/ui/study/`. Keep an immutable one-set snapshot with original positions and rotation-safe state. Handle empty start and unavailable cards, explicit reveal and ratings, atomic save/retry, blocked Exit/Back while saving and early-exit confirmation (FR-08–FR-10, FR-12, UI-09).
- [ ] T032 [US1] Implement `ReviewSummaryScreen.kt` in `ui/study/` and connect Set Detail → Graded Study → Review Summary → originating Set Detail in `FlashcardNavHost.kt`. Register the study ViewModel in the existing factory. Query committed counts with the captured profile, set and session. Show no percentage at zero reviews (FR-09, FR-11, FR-12, UI-10).
- [ ] T033 [US1] Add focused study ViewModel and Compose tests under `ui/study/` in the existing test roots. Cover queue slots and reveal gating, no grade command on recomposition, saving/blocked Back, rollback and retry, empty entry and return after a committed or zero-grade summary (SC-03, SC-04, UI-09, UI-10).

## Phase 6 — Final Verification & Offline Demo

- [ ] T034 Run debug build, JVM and device suites from the [quickstart](quickstart.md#prepare-and-build). Check the schema export, merged release manifest and a short API 24 study/calendar smoke. Record results, devices and outstanding issues (NFR-02, NFR-06, NFR-07, NFR-09).
- [ ] T035 Run the [airplane-mode walkthrough](quickstart.md#offline-course-demo) on a clean installation. Verify profile isolation, set and card editing, grading, summary, restart, deletion, rotation and reachable controls at both orientations and font scales. Check rendered contrast and touch target sizes and record SC-01–SC-06 evidence (NFR-01, NFR-03–NFR-05, NFR-09).

## Dependencies and approvals

Complete phases and tasks in ID order. Explain the approach and applicable course sample
before implementation, run relevant checks, and stop after each task for approval. Mark a
task complete only after verification and approval. At each phase checkpoint update
[current.md](current.md), this list and [quickstart.md](quickstart.md), then wait for explicit
approval before the next phase. Follow [AGENTS.md](../../AGENTS.md).

## Acceptance and scope index

| Scope | Requirements and business rules | Tasks |
| --- | --- | --- |
| US3 · Local Profiles · SC-01, SC-05 | FR-01, FR-02, FR-12 · BR-01, BR-02, BR-06 · UI-01, UI-03 | T006, T008, T011, T014–T015, T020–T021, T024–T027, T030, T035 |
| US2 · Sets & Cards · SC-01, SC-02, SC-05 | FR-04, FR-05, FR-10, FR-12 · BR-03, BR-05, BR-06, BR-07 · UI-03, UI-05, UI-06 | T006, T009–T011, T013, T016, T020, T027–T030, T035 |
| US1 · Graded Study · SC-03, SC-04 | FR-08, FR-09, FR-10, FR-11, FR-12 · BR-10, BR-11, BR-12, BR-13 · UI-06, UI-09, UI-10 | T007, T010, T012, T017–T018, T022–T023, T028, T031–T035 |
| Release and offline · SC-01 | NFR-01, NFR-02, NFR-07, NFR-09 | T003, T005, T013, T019–T023, T034–T035 |
| Screen usability and accessibility · SC-06 | NFR-03, NFR-04, NFR-05 | T004, T024–T033, T035 |
| Maintainability and reliable writes | NFR-06, NFR-08 | T001–T002, T006–T007, T013, T018–T019, T023, T029, T031, T033–T034 |

See [spec.md](spec.md#requirements-and-traceability) for reserved and deferred requirement IDs.

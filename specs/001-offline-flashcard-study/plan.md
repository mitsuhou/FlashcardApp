# Implementation Plan: Offline flashcard study and scheduled review

**Feature**: `001-offline-flashcard-study` · **Git branch inspected**: `main` · **Date**: 2026-11-21  
**Authority**: [Clarified MVP specification](spec.md) and [project constitution v1.0.0](../../.specify/memory/constitution.md)

## Summary

Deliver a native Android workspace in which several local learners can build independent text-card libraries, browse a set without affecting its schedule, and grade new or due cards offline. All four stories are P1. Build the profile and library foundations first so both study modes operate on the same owned records. The demo must pass SC-01–SC-06, including a restart in airplane mode.

## Technical Context

| Area | Implementation choice |
| --- | --- |
| Platform and build | Single `:app`, package `com.example.flashcardapp`, one existing MainActivity. Keep minSdk 24, compile/target 37, AGP 9.3.3, Kotlin Compose plugin 2.2.10, Gradle 9.5.0, daemon JDK 25 and Java 11 bytecode. Desugar java.time for API 24–25. |
| UI | Jetpack Compose, Material 3, Navigation Compose, one ViewModel per screen or closely related flow. Immutable screen state via StateFlow, lifecycle-aware collection and explicit event handlers. |
| Data and logic | Room/SQLite stores the library and review history. One Preferences DataStore stores the active profile ID. Application-level AppContainer wires three repositories, clock and ViewModel factories by constructor. Kotlin Coroutines and Flow carry asynchronous work and observations. |
| Verification | JVM rule and ViewModel tests, instrumented Room and Compose tests, and manual airplane-mode, restart, rotation and accessibility walkthroughs. Keep tests in the existing test source sets. |
| Scale and performance | One installation, multiple local workspaces. Use indexed, profile-scoped queries, aggregate counts in SQLite, lazy lists and a review snapshot of IDs. Read the current card on demand. No agreed numeric latency, library-size or throughput target. |
| Compatibility evidence | The existing project is an Android Studio greeting template with example tests. The connected WayDroid runtime is API 33. Use an API 24 emulator for the Phase 1 starter launch and the Phase 7 calendar smoke test. The course presentation device has yet to be identified. |

Exact catalog additions, repository evidence and deviations from the original design notes are in [research.md](research.md).

## Constitution Check

**Pre-research gate: pass.** Each principle below has a design decision and a verification method. **Post-design gate: pass.** The schema, navigation boundaries and test plan implement those decisions without an exception. These results evaluate the design; the implementation will supply test evidence.

| Principle | Design decision | Evidence required at release |
| --- | --- | --- |
| I. Offline operation | Every data path ends in private local storage. | First launch through restart with radios offline, SC-01–SC-04. |
| II. Maintainable Kotlin and Compose | Stateless reusable content, screen-owned drafts and study progress, labeled and scalable controls. | Recreate the activity and exercise primary actions in both orientations, SC-06. |
| III. MVVM, UDF and repositories | Events enter a ViewModel. Read flows and command results produce immutable state. Screens cannot access DAOs. | Code review and a Compose test with zero grade commands before the tap, one through recompositions during save, and one advancement after commit. |
| IV. Profile isolation | Enforce selection lifetime and ownership on reads, writes and routes. Discard old navigation and observations at a switch. | Two-profile stale ID, deferred grade and delayed emission tests, SC-01. |
| V. Durable local writes | Room constraints, group detachment in one transaction, cascading owned deletions, atomic review log/schedule writes and durable duplicate prevention. | Real Room rollback, retry, deletion and file reopen cases, SC-04–SC-05. |
| VI. Independently testable business rules | Pure eligibility, schedule and summary rules with controlled dates and grades. | JVM tests for every transition, boundaries and empty summary, SC-02–SC-04. |
| VII. Minimal dependencies and architecture | One app, one activity, manual injection and only feature-required catalog additions. | Review resolved dependencies against the research record and verify the build. |
| VIII. Traceable, approved scope | Keep US1–US4 at P1. Map seven implementation phases and each screen to acceptance evidence. | Acceptance matrix in [quickstart.md](quickstart.md) covers all six success criteria. |

## Project Structure

### Documentation for this feature

| Artifact | Use |
| --- | --- |
| [spec.md](spec.md) | Product behavior, priorities, clarifications and seven visual references |
| [research.md](research.md) | Repository inventory, selected dependencies, alternatives and source conflicts |
| [data-model.md](data-model.md) | Room entities, indexes, ownership checks, DAO/repository responsibilities, ordering and commit protocol |
| [quickstart.md](quickstart.md) | Build and device prerequisites, tests and course-demo walkthrough |

The feature exposes only local screens. Their interaction contract is captured below and in UI-01–UI-11. There is no external API, file interchange format or independently released client that needs a separate contract.

### Source code in the repository

| Path | Existing or planned use |
| --- | --- |
| `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts` | Existing module and catalog. Add only the libraries and build processors listed in research. |
| `app/src/main/java/com/example/flashcardapp/` | Existing MainActivity. Add the Application and its AppContainer here. |
| `ui/` beneath that package | Extend existing `ui/theme/`. Group navigation, shared components, profile, library/editor, and study screens by actual workflow. Keep ViewModels beside their screens. |
| `data/` beneath that package | Planned Room database, entities, projections and DAOs in `data/local/`; repositories and preference-backed selection in the data package. |
| `domain/` beneath that package | Planned pure Kotlin study rules and validation shared by editors and repositories. |
| `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/` | Register the Application, keep one launcher activity, and configure local storage backup exclusions. |
| `app/src/test/java/com/example/flashcardapp/`, `app/src/androidTest/java/com/example/flashcardapp/` | Existing JVM and device test roots. Add feature tests here. Export Room schema versions under planned `app/schemas/` for migration checks. |

## Runtime design

**Selection and navigation.** Start behind a neutral loading screen. Read the saved selection from DataStore and resolve the ID against Room. A valid selection opens Home. An invalid or deleted selection opens the selector when any profiles remain, including one, or profile creation when none remain. A failed read offers retry. While the selector awaits explicit selection, no profile is active and no library is visible.

**Ownership across a switch.** Bind the navigation graph, screen state and in-flight actions to the current selection. Recreate the graph on a switch and clear all saved tab back stacks, editor drafts and study ViewModels from the old workspace. Pass only IDs in routes. A repository revalidates their owner and the original selection on access. A route that cannot resolve returns safely to the current library or selector. The [data model](data-model.md#repositories-selection-and-access) defines serialization of writes with switches and recovery across Room and DataStore.

**Unidirectional data flow.** Route composables collect ViewModel state with `collectAsStateWithLifecycle` and pass values plus callbacks into screen content. ViewModels collect owner-scoped repository flows, run commands in their coroutine scope and expose loading, content, saving and retry states. A save or grade runs only in response to an explicit event. Keep draft fields, card index, visible face and reveal state across activity recreation. Keep persisted selection and confirmed records in storage. On process recreation revalidate selection before restoring any route and discard an interrupted review snapshot.

**Time and study.** LibraryRepository supplies read-only cards to preview and Free Study. ReviewRepository combines ownership, a stable session/card identity and a Room transaction for Graded Review. If the current save is pending, settle its outcome before finalizing an exit summary. Sample the injected clock once per grade and use the current device zone to derive its calendar date. Refresh the local day for Home, Library set rows and Set Detail on foreground return, including when the database has had no writes. Counts, eligibility and queue construction share BR-10. Summary totals come from committed ratings. [Queues, scheduling and recovery](data-model.md#queues-and-dates)

## Screens and navigation

Use a single Navigation Compose host for the active workspace. Home, Library and Profile are the three top-level browsing destinations. Library opens on Study Sets. Push Group Detail and Set Detail from their parent lists. Open editors with the selected parent/item ID and pop on successful save or confirmed discard. Study screens hide browsing navigation. Graded Review captures its origin so Done returns to the initiating Home or Set Detail.

| Screen and design basis | Implementation and observable behavior |
| --- | --- |
| UI-01, UI-02, UI-11 · onboarding, Home, Profile | List local profile names, active marker and create/rename/select/confirmed delete actions. Home names the active profile, shows separate new/due counts, Review now and Library. An empty workload offers Library. |
| UI-03 · REF-01, REF-02 | Library identifies the active profile by display name. Dark top bar with one add action; Study Sets and Groups tabs with lavender selection indicator. Lazy lists show rounded outlined set rows with total/eligible counts and group rows with set counts. Empty lists offer creation. |
| UI-04 · REF-03 | Large group title, description, set count, Add study set and owned set rows. Edit/delete overflow names the affected group and explains that sets move to Ungrouped. |
| UI-05, UI-07 · forms | Labeled scrollable fields, inline errors, Save/Cancel and discard confirmation. Card forms require both faces. Set forms choose an owned group or Ungrouped. Retain input on failure. |
| UI-06 · REF-04, REF-05, REF-06 | Large horizontally browsable flashcard, position indicator, set metadata, Free Study and Grade Mode, Add card and editable card rows. Reviewed rows show their saved next date. Use a dark action sheet with Edit, Move to group, Delete and Cancel. |
| UI-08 · Free Study | Show all cards in one owned set. Share the card face and flip controls with preview. Previous/Next stop at the ends. Card changes reset to Front. No grading operation is available on this path. |
| UI-09 · REF-07 | Front-first review, reveal before self-rating, original one-based position out of the frozen queue size, and committed-review count. A skipped card retains its slot and contributes zero ratings. Keep the reveal flag when flipping back. In landscape put ✗ Forgot, position and ✓ Remembered below the card. In portrait keep both actions reachable under it. Disable both during commit. After rollback retain the card for retry. After a lost acknowledgement, a retry returns the original committed grade and schedule without another write. |
| UI-10 · summary | Display reviewed, remembered, forgot and rounded recall percentage. Omit the percentage when reviewed is zero. Confirm early exit while unreviewed cards remain. Cancel resumes the same session. |

Reuse a card-face composable with explicit Flip and accessible browse actions, a set row, form-field/error presentation and named confirmation dialogs. Use navy backgrounds, blue-gray card surfaces, light text and indigo primary actions from the dark Material 3 palette. Use native insets, touch targets at least 48 × 48 dp, labeled icons and text-plus-symbol recall controls. Long faces scroll within the card. Provide a static flip state when animations are disabled. Validate reachable controls and text contrast (4.5:1 normal, 3:1 large) in portrait and landscape at both default (100%) and 130% font scale. The [UI description and seven supplied references](../../docs/6-ui-design/ui-description.md) guide presentation.

## Implementation sequence

The phases below are future delivery work. Dependency order differs from story numbering. Each story retains P1 priority.

| Phase | Depends on | Deliverable and exit evidence |
| --- | --- | --- |
| **1. Setup and shared configuration** · Week 1 | Existing starter | Configure catalog dependencies, KSP, the Room schema export directory, desugaring, test host, theme and local-only backup rules. Inspect the manifest and resolved dependencies, build the debug starter and launch it on API 24. NFR-01, NFR-06, NFR-07, NFR-09. |
| **2. Database, repositories and shared foundations** · Week 1 | 1 | Implement the five tables, indexes, scoped DAOs, three repositories, active selection boundary, shared validation, pure scheduler and clock. Generate and inspect the v1 Room schema. Wire the concrete Application, AppContainer and constructor-based ViewModel factory. Prove scoped CRUD, cross-owner rejection, atomic grade rollback/retry and file reopen before wiring screens. FR-01–FR-05, FR-08–FR-12, BR-01–BR-07, BR-10–BR-13, NFR-02, NFR-08. |
| **3. US4 – Local Profiles (P1)** · Week 1 | 2 | Connect onboarding, profile management, Home shell and selection-aware navigation. Use fresh storage for onboarding and repository-seeded A/B fixtures for switching. Verify valid restoration, zero profiles, explicit one-survivor selection, distinct Home counts and Back isolation on Home/Profile. Exercise delayed profile results across A → B → A. This supplies partial SC-01 evidence. FR-01, FR-02, FR-10, FR-12, UI-01, UI-02, UI-11. |
| **4. US3 – Study Library (P1)** · Week 1 into early Week 2 | 2, 3 | Connect Library tabs, group/set detail, editors, preview and action sheet. Test field boundaries, cancel/save failures, preservation after edits/moves, group detachment and deletion cascades. FR-03–FR-05, FR-10, FR-12, UI-03–UI-07, SC-01, SC-05. |
| **5. US2 – Free Study (P1)** · Week 2 | 4 | Use the owned set projection and shared card UI for repeatable flips and bounded navigation through every card. Verify unchanged learning state for preview and practice with new, due and future cards. FR-07, BR-09, UI-06, UI-08, SC-02. |
| **6. US1 – Graded Review (P1)** · Week 2 | 2–5 | Connect both review entry points to frozen eligible queues, reveal gating, atomic grading, retry, dates on card rows, summaries and return navigation. Exercise the ladder, duplicates, zero grades, process loss, date rollover and empty queue. FR-08–FR-12, UI-02, UI-06, UI-09, UI-10, SC-03, SC-04. |
| **7. Integration testing and final validation** · Week 3 | All four stories | Run JVM, Room and Compose suites, check the actual release manifest and perform the offline demo. Run the API 24 calendar smoke test. Exercise real stale Set Detail and review routes across profile switches, restart, saved grades and confirmed deletion. Complete the entire SC-06 journey in each orientation at each font scale. Record device/API and pass results for NFR-01–NFR-09 and SC-01–SC-06. Reserve time for defects and the course demo/report. |

## Test strategy and release boundary

Use deterministic unit cases for dates, summary arithmetic and field limits. Drive ViewModels with fake repositories for failed saves, duplicate events and delayed results. Use real Room on Android for constraint, transaction and persistence claims, including an injected failure between the two grading writes and a replay after reopen. In Phase 4, check Library’s visible profile name and use a controlled clock to verify Library and Set Detail counts on resume across midnight without a write. In Phase 6, count grade commands across forced recompositions, assert rendered skipped-slot and reviewed counts, and hold a grade pending while requesting exit. Confirm that a summary uses only settled, committed results. Finish with a manual fresh install and an offline restart on device. The [validation matrix and commands](quickstart.md) map tests to FR, NFR, BR, UI and SC identifiers.

Release requires all four stories, a passing debug build and the constitution's verification evidence. Profiles are logical partitions on one installation. FR-06/BR-08 (images), FR-13/BR-14 (coverage), FR-14/BR-15 (timer), FR-15 (appearance choices) and NFR-10 (enhanced tablet/foldable layouts) are deferred. P1 keeps ordinary resizing, landscape and font scaling. The approved screen actions exclude screenshot-only controls such as group-wide review, hints, shuffle and undo.

Supporting documents: [Project Overview](../../docs/1-Overview.md), [Functional Requirements](../../docs/2-Functional%20Requirements.md), [Tech Stacks](../../docs/3-Tech%20stacks.md), [Business Rules](../../docs/4-Business-Rules.md), [App Description](../../docs/5-App-Description.md) and [UI Description](../../docs/6-ui-design/ui-description.md).

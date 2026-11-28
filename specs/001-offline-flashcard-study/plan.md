# Implementation Plan — Offline flashcard study and scheduled review

**Feature**: `001-offline-flashcard-study` · **Baseline**: v3.0 · **Updated**: 2026-11-28

The approved [specification](spec.md) delivers three P1 stories through six screens. Profile
selection or restoration opens Set List. A learner can maintain sets and cards, start
Graded Study from Set Detail, and return there from Review Summary. The whole journey works
offline. The [constitution](../../.specify/memory/constitution.md) governs the plan.

## Technical context

| Area | Choice |
| --- | --- |
| Platform | One `:app` module and MainActivity, package `com.example.flashcardapp`. Keep minSdk 24, compile/target 37, AGP 9.3.3, Compose plugin 2.2.10, Gradle 9.5.0, daemon JDK 25 and Java 11 bytecode. Desugar java.time for API 24–25. |
| UI | Kotlin, Compose Material 3 and one Navigation Compose host. Review stateless screen content and themed previews first. After approval, bind immutable ViewModel StateFlow with lifecycle-aware collection. A shared editor handles sets and cards. |
| Data | Four Room tables, one Preferences DataStore for selected profile, three repositories with direct DAO injection and one Application-owned AppContainer. Use the standard ViewModel factory. |
| Scope and dates | Repository calls capture profileId. Study, grading and session counts require setId as well. The current local calendar day drives eligibility and schedule calculations. |
| Verification | Pure JVM rules, Room integration, representative Compose tests, a debug build and a short offline walkthrough. Use the current development device and a brief API 24 compatibility check. |

The [research record](research.md) explains reference choices and the [data model](data-model.md)
defines table constraints, repository boundaries and the development reset.

## Constitution check

The revised design covers each principle. Runtime evidence for open tasks is pending.

| Principle | Design and evidence |
| --- | --- |
| I. Offline operation | Room and DataStore serve the full flow on-device. Run the airplane-mode walkthrough and inspect the merged release manifest (SC-01, NFR-01, NFR-07). |
| II. Maintainable Kotlin and Compose | Review stateless content and themed previews before wiring. Save UI-only drafts and prompts in Compose, and keep rotation-safe study session state. Verify reachability at both font scales and orientations (SC-06). |
| III. MVVM, UDF and repositories | After UI approval, Compose sends explicit events to ViewModels, which expose state and call repositories. Test that recomposition alone triggers no grade command. |
| IV. Profile isolation | Gate destinations on validated selection. Scope study to the original profile and set. Check stale IDs, Back after switching and a started A write completing while B is selected (SC-01). |
| V. Durable local writes | Use Room foreign keys, a scoped grade transaction, unique session/card key and file reopen tests. Apply the approved pre-release development reset (SC-04, SC-05). |
| VI. Independently testable business rules | Test the schedule, eligibility and committed summaries with explicit local dates (SC-03, SC-04). |
| VII. Minimal dependencies and architecture | Use Inventory, Mars Photos, Cupcake and Dessert Release patterns in one module with manual DI. Retain the toolchain and dependency baseline. |
| VIII. Traceable, approved scope | Map all active requirements and success criteria to tasks and evidence in the [task index](tasks.md#acceptance-and-scope-index) and [validation matrix](quickstart.md#automated-acceptance-matrix). |

## Runtime design

### UI review and integration

Each screen starts with an isolated Jetpack Compose task. Pass display data and callbacks,
use `app/src/main/res/values/strings.xml`, and show representative states with Compose
`@Preview` in `FlashcardAppTheme`. Use the static previews for UI approval. Save transient
input and confirmation state in Compose with `rememberSaveable`. Prefer inline editing when
it satisfies the screen behavior. Use readable labels and normal text contrast of at least
4.5:1 on dark surfaces per NFR-04.

After explicit UI approval, implement the screen ViewModel where needed and register it in
`AppViewModelFactory.kt`. Use `createSavedStateHandle()` for route arguments following
Inventory and connect the destination in `ui/navigation/FlashcardNavHost.kt`. Use compact
sealed state where loading, error and ready are mutually exclusive. Observe data with
`Flow` and `stateIn` as needed, and write directly from `viewModelScope.launch`.

### Profile selection and routes

Resolve the stored selection before displaying any library route. A valid selection opens
Set List. With no profiles, show creation. With existing profiles and no valid selection,
require a choice even for one survivor. A read failure offers Retry. The Set List profile
control opens profile selection and management.

Use a single NavHost with simple scalar record IDs. A switch clears the departing profile's
stack, drafts, session and screen observations before showing the new Set List. Owned routes
use the validated profile ID, check parent records and safely return to the current Set List
or selector when a stale item is unavailable. An already-started write may finish only against
its captured profile ID. T025 provides a validated Set List placeholder for Phase 3 navigation
checks. T028 replaces it with the approved Set List UI and real state.

### Library and study state

LibraryRepository supplies set list rows, detail rows, set/card writes and scoped counts.
Refresh counts on data changes and after a local day change on foreground return. Editors use
the same validation as repositories and retain failed or unfinished drafts.

Set Detail starts a session only when ReviewRepository returns eligible IDs for the current day
and chosen set. Keep the snapshot, session ID, profile ID, set ID, original slot and reveal state
in the study ViewModel. Screen ratings enter through explicit events. The saving state blocks
ratings, Exit and Back until the atomic transaction finishes. Use the captured profile and set
to read summary totals with `getSessionCounts`. Process restart can build a fresh queue.

| ID | Screen | UI review | ViewModel and integration after approval |
| --- | --- | --- | --- |
| UI-01 | Profile Selection/Management | T024 · Create, rename, select, delete confirmation and recovery states. | T025 · Profile ViewModel, MainActivity and NavHost with a Set List placeholder. |
| UI-03 | Set List | T027 · Profile control, owned set rows, counts and Create set. | T028 · Set List ViewModel and live Set List route. |
| UI-06 | Set Detail | T029 · Metadata, counts, editable card rows, saved dates and study entry action. | T030 · Set Detail ViewModel and owned route with stale-ID fallback. |
| UI-05 | Set/Card Editor | T031 · Shared form with validation, draft and deletion states. | T032 · Editor ViewModel and set/card create and edit routes. |
| UI-09 | Graded Study | T034 · Flip, reveal-gated ratings, frozen position, saving/retry and exit confirmation states. | T035 · Study ViewModel. T036 connects the study route after Review Summary UI approval. |
| UI-10 | Review Summary | T036, UI-only portion · Committed totals, defined recall percentage and Done. | T036, after its UI approval · Register the study ViewModel and wire the complete study and summary flow. |

Follow the [UI description](../../docs/6-ui-design/ui-description.md) for dark styling,
adaptive controls and current reference usage. T036 is one task ID with a mandatory
stop for UI approval before its integration portion starts.

## Implementation phases

| Phase | Tasks | Depends on | Exit evidence |
| --- | --- | --- | --- |
| 1 · Project setup and shared configuration | T001–T005 | Starter project | Approved debug starter, tests, manifest, dependencies and API 24 launch. |
| 2 · Database, repositories and shared foundations | T006–T023 | 1 | Approved four-table v1 export, JVM/Room suites, owned CRUD and selection, set-scoped queues and summaries, atomic grades and file reopen. |
| 3 · Local Profiles & Navigation (US3) | T024–T026 | 2 | Approve Profile UI, then wire and test validated selection, recovery and Back isolation with the Set List placeholder. |
| 4 · Study Sets & Flashcards (US2) | T027–T033 | 3 | Approve each of Set List, Set Detail and the shared editor before its wiring. Verify CRUD/validation, saved dates, count refresh and stale routes. |
| 5 · Graded Study & Summary (US1) | T034–T037 | 4 | Approve Graded Study UI before its ViewModel, and Review Summary UI before wiring. Verify one-set grading, retry, committed results and return to the originating set. |
| 6 · Final Verification & Offline Demo | T038–T039 | 5 | Debug build and full test suites, schema export, merged release manifest and API 24 smoke, then the offline demo, restart and accessibility evidence. |

Phases 1 and 2 are complete under the approved v3.0 baseline. The four-table v1 export is
already verified. All three stories must pass before the demo. Per-task, UI and phase
approvals follow [AGENTS.md](../../AGENTS.md).

## Source layout

Use existing `data/local/`, `data/` and `domain/` packages. Place screen state and content
under `ui/profile/`, `ui/library/`, `ui/study/` and `ui/navigation/`. Keep test fixtures
in existing test source sets and export schemas under `app/schemas/`. The
[task list](tasks.md) names the affected files. No external client contracts are required.

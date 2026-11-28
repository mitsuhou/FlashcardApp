<!--
Sync Impact Report
2.0.0 → 3.0.0 on 2026-11-28 (MAJOR). The v1 release is reduced to profile selection,
set and card management, and graded study of one set with a review summary.
Modified principles: I (offline flow), IV (set-scoped ownership), V (pre-release
schema reset and deletion effects), VI (single study mode), VIII (scope and traceability).
Modified sections: Three-Week Delivery Scope, Verification, Governance.
Added sections: none. Removed sections: none.
Updated in Step 1: the six product, technical and UI documents in docs/.
Follow-up: reconcile specs/ and the implementation plan with v3.0.0, then update
the schema, source and tests in a later approved step. Phase 2 evidence describes
the previous baseline.
-->

# FlashcardApp Constitution

## Core Principles

### I. Offline operation

A learner MUST be able to launch the installed Android app for the first time in airplane mode,
create and select a local profile, manage sets and cards, complete Graded Study in one set and
reopen saved work offline. Every approved feature MUST operate on device. Build-time dependency
downloads are permitted.

### II. Maintainable Kotlin and Compose

The team MUST write native Android features in idiomatic Kotlin and build screens with Jetpack
Compose and Material 3. Reusable composables MUST take state and event callbacks. Each mutable
value MUST have a clear owner. Screen state MUST survive rotation where losing it would discard
draft input or interrupt study. Core actions MUST remain labeled and usable in portrait,
landscape and at increased font sizes. Grading actions MUST be distinguishable without color.

### III. MVVM, unidirectional data flow and repositories

Screens MUST send user actions to a screen ViewModel and collect its immutable UI state
with lifecycle-aware collection. ViewModels MUST coordinate work through repositories and
expose observable state through StateFlow. Repositories own reads, writes and transactions.
Room DAOs MUST stay within the data layer. A recomposition MUST never trigger a grade submission.

### IV. Profile isolation

Each owned read or write MUST use the profile ID selected when the operation starts.
DAOs and repositories MUST verify ownership of the target and its parents, including for
counts, study queues and grades. Study actions MUST also verify membership in the session's
set. An operation started for A MAY finish for A after a switch. It MUST never write to B or
deliver A's content to B's screen. Switching profiles MUST clear the old navigation stack,
drafts, session and displayed data and cancel old UI observations. New actions in B MUST use
B's profile ID, including when opening an old route.

### V. Durable local writes

Room MUST be the source of truth for profiles, library content, schedules and review history.
Preferences DataStore MUST store the active profile ID, which MUST resolve to an existing
profile before use. Confirmed writes MUST survive process restart. Destructive user actions
MUST explain their effects and require confirmation. Deleting a profile, set or card MUST
remove its owned descendants and review logs while preserving unrelated records.

FlashcardApp is unreleased and has no real user data. The pre-release Room v1 baseline MAY be
reset on development installations to four tables: Profile, StudySet, Flashcard and ReviewLog.
That reset requires no migration from the former schema. Schema changes affecting released user
data MUST provide a migration that preserves it.

A submitted grade MUST update the schedule and append the review log in one transaction.
The data layer MUST enforce at most one grade per session and card across retries and concurrent
taps. The screen MUST advance only after commit and keep the current card available for retry
on failure. While saving, the UI MUST disable both ratings, Exit and system Back.
Navigation MUST be available again after success or failure. Ending a session MUST preserve committed grades and leave ungraded cards eligible.

### VI. Independently testable business rules

Scheduling, eligibility and session-summary calculations MUST be testable as ordinary Kotlin
without Android, Compose or Room. Pass the local review date explicitly so tests can
reproduce date boundaries. Graded Study MUST select only new or due cards from the chosen set
and accept a learner's Remembered or Forgot rating only after answer reveal. An empty eligible
queue MUST leave the learner on Set Detail. Summaries MUST count committed ratings only. After
process restart a fresh queue is sufficient.

### VII. Minimal dependencies and architecture

The default architecture is one app module, one activity and manual constructor injection.
Use the Android Basics with Compose course as the primary reference. Prefer direct DAO
constructor injection, Flow for observations, suspend functions for writes and standard
ViewModel factories. Use the existing Android toolchain and the smallest design that satisfies the acceptance
criteria. A proposed library, module or additional layer MUST identify the current requirement
it serves and explain why the existing stack is insufficient. Future extensions alone cannot
justify that cost within a three-week project.

### VIII. Traceable, approved scope

Each feature specification MUST preserve or map the FR, NFR and BR identifiers in the project
documents. Plans, tasks and acceptance checks MUST trace back to the applicable requirements.
Retired identifiers MUST remain reserved. The screen descriptions and screenshots guide
presentation. A visible control needs an approved behavior before it becomes an implementation
task. When the baseline changes, the developer MUST reconcile the feature specification, plan,
tasks and acceptance checks before implementing the affected work.

## Three-Week Delivery Scope

The solo developer MUST deliver one offline flow for v1. Profile Selection/Management opens the
selected profile's Set List. From Set Detail the learner can manage sets and two-sided text cards
using a shared Set/Card Editor or start Graded Study of that set. Review Summary returns to the
originating Set Detail. These are the six screen types in scope.

- **Week 1.** Establish the revised storage baseline and connect profiles and set/card management.
- **Week 2.** Complete set-scoped Graded Study and Review Summary.
- **Week 3.** Verify the offline journey, fix defects and prepare the course demo and report.

P1 acceptance takes priority throughout. Groups and set moves, Free Study and flippable previews,
a separate Home screen and profile-wide review are outside v1. Documented P2/P3 extensions are
deferred beyond v1. Online accounts, authentication, cloud sync, backend APIs, public sharing,
collaboration, AI generation, push reminders and CSV import/export remain outside v1.

## Verification

A feature review MUST identify the acceptance criteria and record its evidence. A successful
debug build and the following four checks are required for the course demo.

- **JVM rules.** Cover all schedule transitions, Forgot, the stage cap, overdue eligibility,
  local date boundaries and summaries, including zero reviews.
- **Room.** Exercise two-profile isolation and stale IDs, deletion cascades, file reopen and
  atomic grading through failure, retry and duplicate submission.
- **Compose.** Cover profile selection or switching, one invalid editor input, rating after
  answer reveal and Summary → Set Detail with representative UI tests.
- **Offline walkthrough.** Start the app in airplane mode, create a profile, set and cards,
  grade a set, view the summary, restart and verify saved cards and next dates. Check core
  controls in portrait and landscape at default and 130% font sizes, and verify that rotation
  preserves an editor draft and the current study card.

## Governance

This constitution governs project decisions throughout the course. The developer MUST check the
applicable principles during planning and before accepting a change. A conflict requires
either a compliant design or an approved amendment before the affected work proceeds.

To amend the constitution, the developer and Supervisor MUST review the proposed wording, its
rationale, the requirements and existing work affected, and any transition needed. On approval, record
the amendment date and version and bring affected specifications, plans, tasks and acceptance
checks into agreement. Use a MAJOR increment for incompatible changes to principles, MINOR for
new principles or materially expanded guidance, and PATCH for wording-only corrections.

**Version**: 3.0.0 | **Ratified**: 2026-11-21 | **Last Amended**: 2026-11-28

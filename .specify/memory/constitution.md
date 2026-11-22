<!--
Sync Impact Report
Version change: unratified template → 1.0.0 (initial adoption)
New principles: I. Offline operation; II. Maintainable Kotlin and Compose;
III. MVVM, unidirectional data flow and repositories; IV. Profile isolation;
V. Durable local writes; VI. Independently testable business rules;
VII. Minimal dependencies and architecture; VIII. Traceable, approved scope.
Added sections: Core Principles, Three-Week Delivery Scope, Verification, Governance.
Removed sections: none.
Deferred constitution items: none.
-->

# FlashcardApp Constitution

## Core Principles

### I. Offline operation

A learner MUST be able to launch the installed Android app for the first time in airplane mode,
create a profile and library, study, grade cards and reopen saved work while still offline.
Every approved feature MUST operate on device. Build-time dependency downloads are permitted.

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

Each local profile owns a separate library and review history. Every repository operation on
owned data MUST enforce active-profile ownership, including lookups by ID, counts, moves and
grade submissions. Parent and child records MUST resolve to the same owner. Switching profiles
MUST clear the previous navigation stack, session and displayed data. A stale route or delayed
action from profile A MUST never reveal or modify A's data while profile B is active.

### V. Durable local writes

Room MUST be the source of truth for profiles, library content, schedules and review history.
A saved active-profile preference MUST resolve to an existing profile before use. Confirmed
writes MUST survive process restart. Schema changes affecting existing user data MUST provide
a migration that preserves it. Destructive user actions MUST explain their effects and require
confirmation. Deleting a group MUST preserve its sets in Ungrouped, including cards and history.

A submitted grade MUST update the schedule and append the review log in one transaction.
The data layer MUST enforce at most one grade per session and card across retries and concurrent
taps. The screen MUST advance only after commit and keep the current card available for retry
on failure. Ending a session MUST preserve committed grades and leave ungraded cards eligible.

### VI. Independently testable business rules

Scheduling, eligibility and session-summary calculations MUST be testable as ordinary Kotlin
without Android, Compose or Room. Pass the local review date explicitly or inject a clock so
tests can reproduce date boundaries. Free Study and previews MUST leave learning data unchanged.
Graded Review MUST select only new or due cards and accept a learner's Remembered or Forgot
rating only after the answer has been revealed. Summaries MUST count committed ratings only.

### VII. Minimal dependencies and architecture

The default architecture is one app module, one activity and manual constructor injection.
Use the existing Android toolchain and the smallest design that satisfies the acceptance
criteria. A proposed library, module or additional layer MUST identify the current requirement
it serves and explain why the existing stack is insufficient. Future extensions alone cannot
justify that cost within a three-week project.

### VIII. Traceable, approved scope

Each feature specification MUST preserve or map the FR, NFR and BR identifiers in the project
documents. Plans, tasks and acceptance checks MUST trace back to the applicable requirements.
The screen descriptions and screenshots guide presentation. A visible control, such as
group-wide study, requires an approved behavior before it becomes an implementation task.
Where documents disagree or label a choice as proposed, the team MUST resolve the affected
decision and update the baseline and feature specification before implementing it.

## Three-Week Delivery Scope

- **Week 1.** Confirm the MVP and establish profile-scoped storage and library editing for
  groups, sets and two-sided text cards.
- **Week 2.** Complete Free Study, Graded Review, scheduling, summaries and business-rule tests.
- **Week 3.** Verify isolation, restart behavior and UI flows, then prepare the demo and report.

P1 acceptance takes priority throughout. The team MAY select documented P2 or P3 work, such as
local card images, coverage or a countdown, only after the MVP is stable and time remains for
verification. The v1 scope excludes online accounts, authentication, cloud sync, backend APIs,
public sharing, collaboration, AI generation, push reminders and CSV import/export.
Schedule pressure MUST be addressed by deferring optional work before reducing MVP guarantees.

## Verification

A feature review MUST identify its acceptance criteria and show how the change meets them.
Before the course demo, the team MUST produce a successful debug build and verify the following:

- Pure Kotlin tests cover the approved schedule transitions, forgetting, the stage cap,
  overdue cards, date boundaries and the zero-review summary.
- Repository and Room tests use two profiles to exercise stale-ID reads and writes, deletion
  effects and atomic grading under failure, retry and duplicate submission.
- Representative Compose tests cover profile switching, input validation, answer reveal,
  rating controls and summary navigation.
- An airplane-mode walkthrough demonstrates both study modes, interruption and restart.
  The saved library and grades survive, Free Study leaves schedules intact, and switching
  profiles exposes only the selected library. Rotation and resizing preserve usable controls.

## Governance

This constitution governs project decisions throughout the course. The team MUST check the
applicable principles during planning and before accepting a change. A conflict requires
either a compliant design or an approved amendment before the affected work proceeds.

To amend the constitution, the project team MUST review the proposed wording, its rationale,
the requirements and existing work affected, and any transition needed. On approval, record
the amendment date and version and bring affected specifications, plans, tasks and acceptance
checks into agreement. Use a MAJOR increment for incompatible changes to principles, MINOR for
new principles or materially expanded guidance, and PATCH for wording-only corrections.

**Version**: 1.0.0 | **Ratified**: 2026-11-21 | **Last Amended**: 2026-11-21

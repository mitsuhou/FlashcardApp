# Functional & Non-functional Requirements

Version 1.1 — planning baseline. The P1 acceptance conditions incorporate the [specification clarifications of 2026-11-21](../specs/001-offline-flashcard-study/spec.md#session-2026-11-21). Preserve these IDs in feature plans and tasks.

## 1. Priority and actors

P1 = required MVP, P2 = desirable after stable core, P3 = optional. The original flashcard-first priority is preserved in product value, but profile/storage foundations must be developed before the study features that depend on them. The only actor is a local learner. No server-side account or admin is required.

## 2. Functional requirements

| ID | Priority | Requirement | Acceptance condition |
| --- | --- | --- | --- |
| FR-01 | P1 | Create, rename, select and delete local profiles. | First launch allows creation; selection persists; deleting a profile requires confirmation. After deletion of the active profile or invalid selection, show creation with zero profiles and require explicit selection with any survivors, including one. |
| FR-02 | P1 | Isolate libraries and review data by active profile. | Profile B cannot list, open, modify or review Profile A's cards, even from a stale navigation ID. Switching profiles resets library/session navigation. |
| FR-03 | P1 | Create, rename, edit description of and delete groups. | Group name is required. Group/set field limits and description normalization follow BR-03. Deleting a group moves its sets to Ungrouped; no cards or schedules are lost. |
| FR-04 | P1 | Create, view, edit, delete and move flashcard sets within the current profile. | Set name is required, description optional. Sets can belong to zero or one group. Deletion requires confirmation and removes its cards/history. |
| FR-05 | P1 | CRUD two-sided flashcards inside a set. | Front and back text are both required and non-blank. A saved card is visible in the same set after restart. |
| FR-06 | P2 | Add, replace or remove an optional image on either side. | Card still needs valid front/back text; selected images remain readable offline after restart. Cancel/failure does not erase existing content. |
| FR-07 | P1 | Non-Grade / Free Study mode. | All cards in a selected set are navigable; users can flip either side and move previous/next. No ratings, scores, review history or schedules are changed. |
| FR-08 | P1 | Grade / Scheduled Review mode. | Only new or due cards are queued. A card starts front-side up; after reveal, Remembered (✓) or Forgot (✗) becomes available. Each card is scored at most once per session. |
| FR-09 | P1 | Assign a next review date based on self-assessed recall. | A committed rating updates card schedule and review log atomically; due selection, late review and failure behavior follow BR-10–BR-12. The approved ladder uses the local review day. Each reviewed card’s Set Detail row shows its saved next date. |
| FR-10 | P1 | Display review availability. | Home shows due/new count for active profile; sets show their own eligible counts; empty queues show an actionable empty state. |
| FR-11 | P1 | Show graded session summary. | Show number reviewed, remembered, forgot, and recall rate for committed ratings; unfinished cards remain eligible. A zero-review session has no percentage. |
| FR-12 | P1 | Preserve committed data and handle interruptions. | Completed CRUD and grades survive restart. An interrupted session may restart with a fresh queue. Committed grades survive and unfinished cards receive no rating. |
| FR-13 | P2 | Display profile learning coverage. | Coverage = currently existing cards with at least one committed review / all existing cards; zero cards display no data. Show as a fraction and percentage; do not call it mastery. |
| FR-14 | P3 | Optional recall countdown. | If enabled later, expiry reveals a prompt or answer but never records Forgot automatically; a user rating is still required. |
| FR-15 | P3 | Extended personalization. | Optional appearance choices such as theme or accent only after MVP; exact choices require approval. |

## 3. Non-functional requirements

| ID | Priority | Requirement and verification |
| --- | --- | --- |
| NFR-01 | P1 | Offline use: create profiles, edit cards, study and retrieve schedules in airplane mode. No required network service. |
| NFR-02 | P1 | Persistence and integrity: confirmed writes are retained across process restart; relationships enforce ownership; failed writes must not partially update rating/schedule. |
| NFR-03 | P1 | Usability: visible primary actions, meaningful labels, confirmation for destructive actions, error/empty/loading states, clear mode and active profile. |
| NFR-04 | P1 | Accessibility: labeled icons, non-color-only correctness indicators, touch targets at least 48 × 48 dp and text contrast at least 4.5:1 for normal text or 3:1 for large text. Validate at default (100%) and 130% font scale in both portrait and landscape. |
| NFR-05 | P1 | Basic adaptive UI: core screens work in portrait/landscape and available screen width without clipped controls or losing committed data; enhanced large-screen layout is P2. |
| NFR-06 | P1 | Maintainability: UI must not query DAOs directly; scheduler is pure and unit-testable; dependencies remain small and documented. |
| NFR-07 | P1 | Privacy: local profiles are logical separation, not encryption or identity authentication; image access uses user-selected files and no unnecessary storage/network permissions. |
| NFR-08 | P1 | Reliability: validation blocks invalid inputs; concurrent/double-tap grade submission cannot create two ratings for the same session card; failure shows retry and retains current card. |
| NFR-09 | P1 | Verification: successful debug build, scheduler unit tests, repository/Room isolation tests and representative Compose navigation/UI tests. |
| NFR-10 | P2 | Enhanced adaptive experience: tablet/foldable screens may use two-pane layouts with the same accessible actions. |

## 4. Acceptance demo

1. Create two profiles; create a group, set and three cards in profile A.
2. Switch to B; verify A data is inaccessible; switch back.
3. Use Free Study, flip cards and navigate; confirm no schedule or history change.
4. Use Grade Mode; grade a card Remembered and a card Forgot; confirm independent dates per business rules and a correct summary.
5. Exit before grading the last card; verify committed ratings remain and the ungraded card is available.
6. Restart with airplane mode, verify data and repeat due-card checks using a controlled test clock.
7. Delete a group and confirm sets survive ungrouped; confirm set deletion removes associated cards/history.

## 5. Boundaries

No account sign-in, remote sync, automatic machine grading, timers that change grades, image-only cards or cross-profile sharing in MVP. When clarification changes a requirement, update this baseline and the formal spec rather than silently implementing extra behavior.

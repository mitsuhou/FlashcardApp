# Feature Specification: Offline flashcard study and scheduled review

**Feature directory**: `specs/001-offline-flashcard-study`  
**Created**: 2026-11-21  
**Status**: Approved MVP behavior, revised 2026-11-23

**Input**: One three-week Android course MVP for a local learner to organize two-sided text cards,
practice recall and review due material on a shared device. The entire journey works in airplane mode.

## Clarifications

### Session 2026-11-21

- Q: What opens if the active profile is deleted or its saved selection is invalid? → A: Show the profile selector even if only one profile remains. Open Home only after explicit selection. With zero profiles, show profile creation.
- Q: Which intervals govern Graded Review? → A: Remembered advances through 1, 3, 7, 14, 30 and 60 calendar days from the review date, capped at 60. Forgot resets to stage 0 and schedules tomorrow.
- Q: Which field lengths are valid? → A: Group and set names accept 1–80 characters, their optional descriptions up to 500, and each card face 1–2,000 after trimming.
- Q: What survives deletion or interruption? → A: Deleting a group preserves its sets in Ungrouped. Deleting a profile, set or card removes the content and reviews it owns. An interrupted review may restart with a fresh queue. Saved grades survive and interruption never grades an outstanding card.
- Q: Where can the learner find a card’s next review date? → A: In that card’s row in the Set Detail card list.

### Session 2026-11-23

- A started operation keeps its original profile ID. A switch clears the old screens and observations. The new profile sees only its own data and starts operations with its own ID.
- Starting review refreshes counts and eligibility using the current local day. An empty queue stays on the entry screen.
- While a grade is saving, Exit and system Back are unavailable. Navigation resumes on success or failure. After process restart, committed grades persist and the next review starts from current eligibility.

## User Scenarios & Testing

All four stories are P1 and required for release. Their order reflects the product priority of
scheduled recall, followed by ungraded practice and the library and profile workflows that
support both. Each independent test specifies its own starting data.

### User Story 1 – Review what is due today (P1)

A learner opens Home to see what needs attention, reveals each answer and rates their own recall.
The next review date and session summary let them see what they accomplished and when to return.

**Independent test.** Start with a local profile containing two sets, known review dates and new
cards. Exercise both review entry points at a fixed local date. Covers FR-08 through FR-12.

1. **Given** an overdue card, a card due today, a new card and a future card across two sets,
   **when** the learner selects Review now on Home, **then** the first three appear once in
   overdue, due, new order. Starting from one set includes only its eligible cards.
2. **Given** the first card is front-side up, **when** the learner reveals the back and flips
   to the front again, **then** Remembered and Forgot become available after that first reveal
   and remain available for this card. Advancing starts the next card front-side up and unrated.
3. **Given** a stage-1 card due on November 22, **when** Remembered is saved on November 24,
   **then** it advances to stage 2 and is due November 27. **Given** that card on November 27,
   **when** Forgot is saved, **then** it resets to stage 0, is due November 28 and cannot recur
   in the current session.
4. **Given** a revealed card, **when** the grading transaction rolls back, **then** all learning
   data remains unchanged, the card stays visible and Retry is available. **When** a retry succeeds
   despite a repeated tap, **then** the session counts one rating and advances once.
5. **Given** three eligible cards and one saved rating of each kind, **when** the learner
   confirms early exit, **then** the summary shows 2 reviewed, 1 remembered, 1 forgot and
   50% recall. Done returns to the starting scope. Restarting offline preserves both grades
   and leaves the third card eligible.
6. **Given** one reviewed card and one new card in a set, **when** the learner opens Set Detail
   after restarting offline, **then** the reviewed card’s row shows its saved next review date
   and the new card has no scheduled date.
7. **Given** a frozen queue of three cards with the first rated, **when** the second is
   skipped, **then** the third shows “3 of 3” with one committed rating. Grading the third
   ends the session with two reviewed.
8. **Given** a grade is saving, **when** the learner presses Exit or system Back, **then**
   the card stays visible and no exit confirmation opens. After success, navigation is
   available on the next card or summary. After rollback, the revealed card offers Retry
   and the learner can request exit.
9. **Given** Home or Set Detail with no eligible cards at review start, **when** the learner
   requests review, **then** the entry screen shows that nothing is due and offers Library
   or Free Study as appropriate. No session or summary opens.

### User Story 2 – Practice a whole set freely (P1)

A learner can revisit material before an exam regardless of its due date, without changing the
daily review workload.

**Independent test.** Open a prepared set of three cards with known history and a mix of new,
due and future dates. Compare schedules, counts and history before and after practice (FR-07).

1. **Given** Set Detail, **when** the learner flips the preview and browses to a different
   card, **then** the new preview starts on Front and the position indicator updates.
2. **Given** that set, **when** the learner starts Free Study, **then** all three cards appear
   in creation order, with a position, Front/Back label, Flip, Previous/Next and exit. Both
   sides can be revisited. Navigation stops at the first and last cards.
3. **Given** the learner has browsed every answer, **when** they exit, **then** no score or
   grading controls have appeared, and review history, stages, due dates and counts match
   their starting values.

### User Story 3 – Build and maintain a study library (P1)

A learner groups sets by subject, edits prompts and answers, and can reorganize material
without losing prior practice.

**Independent test.** Start with one profile, one reviewed card and its known schedule. Create
another group, set and card, then edit, move and delete them offline (FR-03, FR-04, FR-05, FR-12).

1. **Given** an active profile, **when** the learner creates a group, a set in it and a card
   with both text faces, **then** the set opens from Library and Group Detail, and both faces
   remain readable after restart. Renaming the group or set updates the corresponding lists.
2. **Given** a card editor with a blank or whitespace-only back, **when** the learner edits the
   form, **then** an inline field error identifies the missing answer and Save is unavailable.
   A failed valid save retains the draft. Leaving a changed draft requires confirmation.
3. **Given** a reviewed card, **when** the learner edits its text, moves its set to another
   owned group, then confirms deletion of that group, **then** the edited card remains in the
   same set in Ungrouped with its prior history and next date.
4. **Given** a populated set, **when** deletion is requested and canceled, **then** its cards
   and history remain. **When** deletion is confirmed, **then** the set, its cards and their
   reviews disappear while other sets remain. Confirming deletion of one card affects only
   that card and its reviews.

### User Story 4 – Share a device with independent local profiles (P1)

Each learner can return to their own workspace without seeing or changing another profile's
library. This is local workspace selection without a password.

**Independent test.** Use a fresh installation for onboarding and two populated profiles for
switching. Give both profiles identically named sets so ownership can be checked by their
different contents and due counts (FR-01, FR-02, FR-10, FR-12).

1. **Given** first launch offline with zero profiles, **when** the learner creates and selects
   a named profile, **then** Home shows that active profile and an empty review workload.
   Renaming it and restarting restores the same selected workspace under its new name.
2. **Given** a library operation has started for A, **when** the learner switches to B,
   **then** B opens its own Home and Back cannot return to A. The old screen stops observing A.
   The operation may finish against A. Its result never appears in B and its write never affects
   B. A new action in B using A's item ID cannot read or change that item. On returning to A,
   the learner sees A's saved records, including any write that finished for A.

3. **Given** A is active and B is the only other profile, **when** A's deletion is confirmed,
   **then** A's groups, sets, cards and reviews disappear, B's survive, and the selector shows B.
   B's Home opens only when the learner selects B.
4. **Given** a saved selection that no longer identifies a profile, **when** the app opens,
   **then** it shows the selector if any profiles exist. No library opens until a profile is
   explicitly selected. With zero profiles, including immediately after the last deletion,
   the app shows profile creation.

### Edge Cases

- With no sets, groups or cards, the relevant empty screen offers creation. A single card can
  flip in Free Study with both navigation directions disabled. An empty set offers Add card.
  With cards but none eligible, show that no reviews are due and a route to Library or Free Study.
- Freeze the review queue when the session starts. Skip deleted or no-longer-owned cards. Keep each card’s original one-based
  position and the original queue size. Skips contribute zero ratings.
  Newly created cards wait for a later session. Recheck ownership and eligibility on submission.
- Returning after local midnight refreshes due counts. A device date or time-zone change can
  change eligibility but never rewrites a stored calendar date. A forgotten stage-0 card has
  a due date and history, so it cannot enter the queue as a new card.
- An early exit with zero ratings yields zero counts and no percentage. Canceling early exit
  continues the session. Rotation preserves the queue, position, face, reveal state and draft
  input. A process restart may rebuild the queue from saved data.
- Duplicate group/set names and duplicate cards are allowed. Operations affect the selected
  item even when another item has identical text. Reject a move into another profile's group.

## Requirements

### Functional Requirements

Rows below are the P1 acceptance contract. The BR identifiers retain their source meanings.
Deferred identifiers are mapped separately below.

| ID | Required behavior | Business rules | Story |
| --- | --- | --- | --- |
| FR-01 | Create, rename, select and explicitly confirm deletion of local profiles. Trim display names and require a nonblank value. Restore a valid selection on launch. After active-profile deletion or an invalid selection, show the selector with no active profile until the learner explicitly selects one, even with one remaining profile. Open Home after that selection. Show profile creation when none remain. | BR-01, BR-06 | 4 |
| FR-02 | Check every owned operation against its originating profile ID. Switching clears the old stack, screen data, session and UI observations. Already-started work may finish for its original owner. New actions use the newly selected profile. | BR-02 | 4 |
| FR-03 | Create, open, rename, describe and delete flat groups. Show the set count. Confirming removal moves every contained set to Ungrouped with cards and learning records intact. | BR-03, BR-04 | 3 |
| FR-04 | Create, open, edit, move and delete owned sets. Each belongs to zero or one owned group. Confirmed set deletion removes its cards and reviews. Moving preserves them. | BR-03, BR-04, BR-06, BR-07 | 3 |
| FR-05 | Create, view, edit and delete two-sided text cards within one set. Require both faces. Preserve schedule and history on edits. Confirm a card deletion and remove its reviews. | BR-05, BR-06, BR-07 | 3 |
| FR-07 | Free Study browses every card in one set in ascending creation order, breaking ties by ID. Preview and Free Study are read-only for all learning data. | BR-09 | 2 |
| FR-08 | Start Graded Review from Home or Set Detail. Take a snapshot with reviewed cards due today or earlier first (earliest date, then ID), then new cards (creation time, then ID). Require answer reveal and explicit self-rating. | BR-10, BR-11 | 1 |
| FR-09 | A successful rating records exactly one review per session/card and updates its stage and next date together. Rollback leaves all learning data unchanged. Repeated or concurrent submissions for the same session/card leave one log and one schedule update. Disable grading, Exit and system Back while saving. Restore navigation on success or failure. Advance only on success. Display the saved next review date in the card’s row on Set Detail. | BR-11, BR-12 | 1 |
| FR-10 | Home shows active-profile counts of new and due reviewed cards and Review now. Show total and eligible counts for each set. Refresh Home, Library and Set Detail counts on foreground return when the local date changes, even without a database write, and refresh after data changes. At review start, recompute counts and eligibility for the current local day. An empty queue leaves the learner on the entry screen. | BR-02, BR-10 | 1, 4 |
| FR-11 | At completion or confirmed early exit, show reviewed, remembered and forgot counts. Recall rate is remembered divided by reviewed, rounded to a whole percentage. Omit the percentage at zero reviews. Done returns to Home or the originating set. | BR-13 | 1 |
| FR-12 | Confirmed edits, deletions, active selection and grades survive restart. After interruption, a fresh queue may be built from saved grades and current eligibility. Unfinished cards gain no rating from interruption. Warn before ending a review with unreviewed cards. Preserve committed data on save failure. | BR-01, BR-11, BR-13 | 1, 3, 4 |

**Field limits (BR-03, BR-05).** After trimming, group and set names accept 1–80
characters and each card face accepts 1–2,000. Trim leading and trailing whitespace from optional group/set descriptions before validation and saving.
A blank result is absent. Accept up to 500 characters in the trimmed value. At the stated limit Save is available for an otherwise valid form.
Exceeding it identifies the field and prevents saving. The source gives no numeric profile-name
limit.

**Review schedule (BR-12).** A new card starts at stage 0 with no due date and is eligible
immediately. Use the device's local calendar date when the rating is saved, including late
reviews. Keep the actual review timestamp separately. The offsets below are calendar days.

| Self-rating | Stage before | Stage after | Next review |
| --- | --- | --- | --- |
| Remembered | 0 | 1 | Today + 1 day |
| Remembered | 1 | 2 | Today + 3 days |
| Remembered | 2 | 3 | Today + 7 days |
| Remembered | 3 | 4 | Today + 14 days |
| Remembered | 4 | 5 | Today + 30 days |
| Remembered | 5 or 6 | 6 | Today + 60 days |
| Forgot | Any | 0 | Tomorrow |

### Non-functional requirements

| ID | P1 acceptance condition |
| --- | --- |
| NFR-01 | From first launch in airplane mode, complete profile selection, editing, both study modes, review-date lookup and restart without connecting. |
| NFR-02 | Confirmed records and ownership survive process restart. Both the grade and schedule persist together or neither changes. |
| NFR-03 | Identify the active profile on Home/Library and the mode during study. Show labeled primary actions, loading only while pending, actionable empty/error states and saving feedback. Confirm destructive actions by naming the target and consequences. |
| NFR-04 | Require touch targets at least 48 × 48 dp and meaningful accessibility labels. Text-to-background contrast must be at least 4.5:1 for normal text and 3:1 for large text (18 sp or larger, or bold at 14 sp or larger). Provide alternatives to gestures and animation. Use words and symbols for grades. Long text wraps or scrolls at enlarged text sizes. |
| NFR-05 | Complete core tasks in portrait, landscape and resized windows with reachable controls. Rotation preserves current work. |
| NFR-06 | Scheduling must be independently testable with known dates. Keep storage access outside screens and dependencies minimal and documented, as required by the constitution. |
| NFR-07 | Keep learner data local. Profile isolation applies to selected workspaces. The MVP requires neither identity credentials nor network or shared-storage permissions. |
| NFR-08 | Reject invalid input, retain a failed draft or grade for retry, and prevent duplicate ratings even for simultaneous submissions. |
| NFR-09 | Course acceptance requires a successful debug build, independent scheduler tests, storage-level ownership and integrity tests, and representative navigation and screen tests. Tooling belongs in the plan. |

### Screens and visual acceptance

Use UI Description v1.2 and REF-01–07 for appearance, with dark navy backgrounds, outlined
rounded library rows, blue-gray flashcard faces, lavender selected tabs and indigo primary
actions. Use native Android system bars and readable labels. Home, Library and Profile are
the browsing destinations. Hide that navigation during focused study.

| Screen | What the learner sees and can do | Visual basis |
| --- | --- | --- |
| UI-01, UI-02, UI-11 | Restore a valid selection. If the active profile was deleted or the saved selection is invalid, show profile creation when empty or the selector otherwise. Open Home after explicit selection in this recovery flow. Home shows the review workload, Review now and a Library shortcut. Profile shows the active learner, switching, renaming and confirmed deletion. | UI Description §§3, 12 |
| UI-03 | Library shows the active profile’s display name and initially opens Study Sets. Show all owned sets, grouped and ungrouped, in stable order. Groups tab shows owned groups and counts. A prominent add action opens creation. | [REF-01](../../docs/6-ui-design/references/startup_ui.png), [REF-02](../../docs/6-ui-design/references/outside_group_ui.png) |
| UI-04 | Show group title, count, optional description and its sets. The primary action adds a set. Overflow provides group editing and confirmed deletion. | [REF-03](../../docs/6-ui-design/references/inside_group_ui.png) |
| UI-05, UI-07 | Editors show labeled fields, Save/Cancel, inline errors and scrolling. Successful save returns to the parent screen. Warn before discarding a changed draft. | UI Description §9 |
| UI-06 | Browse and flip the large preview, with a position indicator. Show set details, total and eligible counts, Free Study, Grade Mode, the card list and Add card. Each reviewed card’s row shows its next review date. The dark bottom sheet offers Edit, Move to group, Delete and dismissal. | [REF-04](../../docs/6-ui-design/references/flashcard_front.png), [REF-05](../../docs/6-ui-design/references/flashcard_end.png), [REF-06](../../docs/6-ui-design/references/edit_a_set.png) |
| UI-08 | Focus on a labeled card with position, flip, navigation and exit. Browsing to another card starts it on Front. | REF-04/05 card faces and UI Description §10 |
| UI-09 | While saving, disable both rating actions, Exit and system Back. Restore navigation when the save finishes. Show one large card, Flip, Exit, the original position out of the frozen queue size, and a separate committed-review count. Beneath the landscape card place ✗ Forgot, position and ✓ Remembered. Keep both labeled actions reachable in portrait. | [REF-07](../../docs/6-ui-design/references/grade_mode.png) |
| UI-10 | Show the committed session totals, defined recall rate and Done. | UI Description §12 |

### Key Entities

- **Profile** owns one learner's library and reviews. Selection determines the active workspace.
- **Group** is a named, flat collection within a profile. Ungrouped denotes sets with no group.
- **Study set** belongs to one profile and holds zero or more cards, with a name, optional
  description and optional group.
- **Flashcard** belongs to one set and has a prompt, answer, creation order, stage and optional
  next review date. Ownership follows its set.
- **Graded session** has a starting profile or set scope, an ordered snapshot, a current position
  and a summary derived from its saved ratings.
- **Review record** links one card and session to a self-rating, actual time, previous/new stage
  and next review date. It belongs to the same profile as the card.

## Success Criteria

### Measurable Outcomes

- **SC-01.** In airplane mode from first launch, a learner creates two profiles, one group, one
  set and three cards in A. After restart, all three cards and both text faces remain in A.
  B exposes zero of A's records through browsing or stale navigation. A write already started in A can affect only A, and its completion leaves B’s screen and data intact.
- **SC-02.** In Free Study the learner can visit both faces of all three cards, including a
  future-due card. Comparing learning data before and after yields zero changes.
- **SC-03.** At known dates, every row of the scheduling table produces the specified stage
  and date, including a repeat at the cap and a late review. Set Detail shows that date in the
  reviewed card’s row. A mixed queue contains 100% of
  eligible cards in the documented order and zero future or foreign cards. If no cards are
  eligible at either entry point, the learner stays on Home or Set Detail.
- **SC-04.** In a three-card review with one rolled-back grade, one retry and a duplicate tap,
  saving one Remembered and one Forgot before exit produces exactly two records, 50% recall
  and one outstanding card after restart. During a save, Exit and system Back keep the card
  on screen. Navigation works again after both commit and rollback. Exiting before any grade
  in a started session shows no percentage.
- **SC-05.** After group deletion, all former sets, cards and schedules survive in Ungrouped.
  After confirmed set deletion, zero of its cards or reviews remain. Other sets retain theirs.
- **SC-06.** In portrait and landscape at both default (100%) and 130% font scale, a learner can create a
  card, flip it, identify both grade actions without color, submit a grade and return from the
  summary. In all four combinations, primary actions are reachable without clipping or a gesture-only
  control, and touch targets and contrast meet NFR-04.

## Assumptions

### Scope and source decisions

- The complete P1 scope is one feature for three weeks. Deferred P2 items are per-side images
  (FR-06, BR-08), profile coverage and aggregate statistics (FR-13, BR-14), and enhanced
  tablet/foldable layouts (NFR-10). Deferred P3 items are the countdown (FR-14, BR-15) and
  appearance personalization (FR-15). None is needed for P1 acceptance.
- v1 excludes online accounts, authentication, cloud synchronization, backend APIs, public
  sharing, collaborative editing, AI generation, push reminders and CSV import/export.
- The overview's optional Week 2 image work and the pictured illustrated answer do not expand
  P1. The startup screenshot depicts Library after onboarding. The group screenshot's “Study
  this folder” becomes Add study set under UI Description §6. Classes, ads, search/filter,
  sharing, downloads, hints, audio, shuffle and undo have no P1 acceptance criteria.

The minimum supported Android version is API 24. The starter launch on API 24 is recorded in
[Phase 1 verification](quickstart.md#phase-1-verification). Record the presentation device at the final checkpoint. Offline operation has no external service dependency.

### Source documents

Product scope, priorities and course constraints follow the [Project Overview](../../docs/1-Overview.md),
[Functional Requirements](../../docs/2-Functional%20Requirements.md) and
[Tech Stacks](../../docs/3-Tech%20stacks.md). Workflow and screen behavior follow the
[Business Rules](../../docs/4-Business-Rules.md), [App Description](../../docs/5-App-Description.md)
and [UI Description v1.2](../../docs/6-ui-design/ui-description.md). The seven references above
link to the supplied files in the repository's `6-ui-design` directory.

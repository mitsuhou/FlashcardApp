# Feature Specification — Offline flashcard study and scheduled review

**Feature**: `001-offline-flashcard-study` · **Baseline**: v3.0 · **Updated**: 2026-11-28

A local learner selects a profile, manages study sets and two-sided text cards, then studies
eligible cards within one set and sees the results. The full flow runs in airplane mode.
The [constitution](../../.specify/memory/constitution.md) and the approved v3.0
[product requirements](../../docs/2-Functional%20Requirements.md) govern this specification.

## User stories and acceptance scenarios

All three stories are P1. Story numbering expresses product priority. Implementation follows
profiles, set and card management, then study, so each flow builds on its prerequisites.

### US1 — Graded Study of a Set

A learner opens a set, recalls and rates cards that are ready to study, and sees the result
of that session. **Independent test.** Seed one profile with two sets and a mix of new, overdue,
due and future cards. Start from the first set's Set Detail at a known local date (FR-08–FR-12).

1. **Given** eligible and future cards in the first set and an eligible card in the second,
   **when** study starts, **then** the first set's overdue and due cards precede its new cards.
   No future cards or cards from the second set enter the queue. An empty queue leaves the
   learner on Set Detail with an explanation and Add card if the set is empty.
2. **Given** a card is front-side up, **when** the learner first reveals the answer, **then**
   Remembered and Forgot become available and stay available when flipped back. A new card
   starts on Front with both ratings disabled. A skipped item retains its original queue slot
   and adds no grade. New arrivals wait for a later session.
3. **Given** a rating is saving, **when** the learner taps again or presses Exit or Back,
   **then** the screen stays on the card. A commit advances once and restores navigation.
   A rollback changes neither record, retains the revealed card, offers Retry and restores
   navigation. Competing submissions for the same session and card save one grade.
4. **Given** a stage-1 card due November 22, **when** Remembered is saved November 24, **then**
   its stage becomes 2 and its next date is November 27. Forgot at any stage resets to
   stage 0 and schedules tomorrow. Set Detail shows the saved date on the reviewed card's row.
5. **Given** three eligible cards and one committed rating of each kind, **when** early exit
   is confirmed, **then** Review Summary shows 2 reviewed, 1 remembered, 1 forgot and 50%.
   Done returns to the originating Set Detail. With zero committed ratings the summary has
   no percentage. Restarting offline retains grades and leaves the ungraded card eligible.

### US2 — Study Sets & Cards

A learner creates and maintains study sets and their two-sided cards. **Independent test.**
Start in one profile with a set containing a previously reviewed card. Create a second set,
add cards, edit a reviewed card and exercise deletion and restart (FR-04, FR-05, FR-10, FR-12).

1. **Given** a selected profile, **when** a valid set and card are saved, **then** the set
   appears in Set List and the card in Set Detail. Both faces can be read in the card
   editor after restart. A set may be empty and offers Add card.
2. **Given** a blank or over-limit required field or an over-limit description, **when** the
   learner edits the form, **then** the field is identified and Save is unavailable. Valid
   values are trimmed. A blank optional description clears it. A failed save retains the
   draft, rotation retains input, and leaving a changed draft requires confirmation.
3. **Given** a reviewed card, **when** the learner edits its text or the set metadata, **then**
   its history and schedule remain. Set List and Set Detail show total and eligible counts.
   On foreground return after a local date change, eligibility refreshes without a data write.
4. **Given** a deletion dialog, **when** it is canceled, **then** the data remains. Confirming
   deletion of a card or set removes its review records and any child cards. Unrelated data stays.

### US3 — Local Profiles

Learners use independent local workspaces on the same device. **Independent test.** Start
with empty storage, then seed two profiles with identically named sets and different cards
to verify selection, restoration and isolation (FR-01, FR-02, FR-12).

1. **Given** first launch offline with zero profiles, **when** a learner creates and selects
   one, **then** its Set List opens. Rename and restart to verify the same ID and new name.
2. **Given** A has an in-flight operation, **when** the learner switches to B, **then** B's
   Set List opens and Back cannot reopen A's routes. B cannot access A via stale IDs.
   A's started write may finish only for A and its result cannot populate B's screen.
3. **Given** A is active and B is the sole survivor after A is deleted, **when** deletion
   completes, **then** A's records are removed, B's remain, and the selector awaits an explicit
   choice. Zero profiles opens creation. An absent or stale saved ID with existing profiles
   also opens the selector. A failed selection read offers retry.

## Requirements and traceability

| ID | Required behavior | Business rules | Stories |
| --- | --- | --- | --- |
| FR-01 | Create, rename, select and confirm deletion of local profiles. Restore a valid saved selection or require an explicit choice when profiles exist. | BR-01, BR-06 | US3 |
| FR-02 | Scope every owned operation to its originating profile. Clear the old back stack, data, drafts, session and observations when switching. Guard stale IDs. | BR-02 | US1, US2, US3 |
| FR-04 | Create, view, edit and confirm deletion of owned study sets. Preserve schedules and history on edits. | BR-03, BR-06, BR-07 | US2 |
| FR-05 | Create, view, edit and confirm deletion of two-sided text cards owned through a set. Preserve schedules and history on edits. | BR-05, BR-06, BR-07 | US2 |
| FR-08 | Start a frozen queue for a single set from Set Detail. Include only cards with `dueDay == null || dueDay <= today`. Require answer reveal and explicit self-rating. | BR-10, BR-11 | US1 |
| FR-09 | Save one ReviewLog per (sessionId, cardId) and update the schedule in the same Room transaction. Block both ratings, Exit and Back during save. Restore navigation on either outcome, advance on commit and offer retry on failure. Show the saved next date on Set Detail. | BR-11, BR-12 | US1, US2 |
| FR-10 | Show total and eligible counts on Set List and Set Detail. Refresh on data changes, local day change on resume and study start. Keep an empty-queue attempt on Set Detail. | BR-10 | US1, US2 |
| FR-11 | Show committed reviewed, remembered and forgot counts and rounded recall percentage at completion or confirmed early exit. Omit the percentage at zero grades. Done returns to the originating Set Detail. | BR-13 | US1 |
| FR-12 | Retain selection, committed edits, deletions and grades after restart. Support a fresh eligible queue after process loss. Warn before exiting a session with ungraded cards. | BR-01, BR-06, BR-11, BR-13 | US1, US2, US3 |

**Validation.** Trim profile names and require a nonblank value. Trim set names and require
1–80 characters. Trim optional descriptions, save whitespace-only as null and accept up to
500 characters. Trim both card faces and require 1–2,000 characters each. Duplicate names and
card text are allowed. IDs identify records (BR-01, BR-03, BR-05).

**Scheduling.** Remembered advances through 1, 3, 7, 14, 30 and 60 calendar days and caps at
stage 6. Forgot resets to stage 0 and schedules tomorrow. Use the local day of the actual
rating, including overdue reviews. Preserve saved dates when the device day or zone changes.
A forgotten card with a due date is reviewed, even at stage 0 (BR-12).

**Queue and summary.** Order previously reviewed eligible cards by due day then ID, followed
by new cards by creation then ID. Recheck profile, set and eligibility at presentation and
commit. After a skipped card, show the next card's original position out of the frozen total.
Summary uses committed ratings only. Reviewed equals Remembered plus Forgot, with recall
rounded from Remembered divided by reviewed (BR-02, BR-10, BR-11, BR-13).

FR-03, FR-07, BR-04 and BR-09 are reserved as **Removed / Out of scope in v3.0**.
FR-06/BR-08, FR-13/BR-14, FR-14/BR-15, FR-15 and NFR-10 are deferred beyond v1.

## Non-functional requirements

| ID | P1 acceptance condition |
| --- | --- |
| NFR-01 | Complete the full learner flow from first launch through restart in airplane mode. |
| NFR-02 | Reopen committed data and enforce ownership and grade atomicity at the storage boundary. |
| NFR-03 | Show the active profile on Set List, clear loading, empty, saving and error states, labeled actions and named destructive confirmations. |
| NFR-04 | Label controls and identify ratings by text and symbols. Touch targets are at least 48 × 48 dp. Contrast is at least 4.5:1 for normal text and 3:1 for large text. Check core controls at 100% and 130% font scale in portrait and landscape. |
| NFR-05 | Keep core actions reachable as orientation or window size changes. Rotation preserves editor drafts, queue position, card face and reveal state. |
| NFR-06 | Keep DAOs in the data layer. Use independently testable Kotlin rules with explicit dates, one app module, manual DI and course-level MVVM and UDF. |
| NFR-07 | Keep learner data on-device with logical profile isolation and no unnecessary network or shared-storage permissions. |
| NFR-08 | Reject invalid inputs, retain failed drafts and grades for retry, and persist at most one grade per session and card. |
| NFR-09 | Produce a debug build, focused JVM, Room and representative Compose results, and an offline walkthrough record. |

## Screens

| ID | Screen | Behavior |
| --- | --- | --- |
| UI-01 | Profile Selection/Management | Select, create, rename and confirm deletion. Restore a valid saved selection, show creation when empty and offer retry after a read failure. |
| UI-03 | Set List | Open directly after profile selection or restoration. Show active profile, its sets in stable order, total and eligible counts, profile control and Create set. |
| UI-06 | Set Detail | Show metadata, counts, editable card rows and saved next dates for reviewed cards. Offer Add card, Start study, Edit set and confirmed deletion. |
| UI-05 | Set/Card Editor | Reuse one screen for set name and description or both card text faces. Show validation, Save, Cancel and confirmed deletion for existing cards. |
| UI-09 | Graded Study | Show card face, Flip, original queue position, committed count, reveal-gated ratings, saving, retry and early exit confirmation. |
| UI-10 | Review Summary | Show committed results. Done returns to the Set Detail that started the session. |

Use the dark Material 3 design and current visual references in the
[UI description](../../docs/6-ui-design/ui-description.md), and the matching screen identifiers
in the [app description](../../docs/5-App-Description.md).

## Success criteria

| ID | Measure |
| --- | --- |
| SC-01 | From first launch offline, profile A can create a set with three cards and reopen it after restart. B exposes zero of A's records or results, including through stale navigation. |
| SC-02 | A card and set at each valid text boundary can be saved. Blank and over-limit required inputs are rejected, blank descriptions clear, and edits retain existing dates and history. |
| SC-03 | Known local dates yield the six intervals, stage cap and next-day Forgot reset. A mixed queue contains only the set's eligible cards in order. Set Detail shows the saved next date and handles empty queues on entry. |
| SC-04 | A forced rollback plus retry and competing submission leave one committed grade per card. In the three-card early-exit example, 2 grades, 50% recall and 1 ungraded eligible card survive restart. A zero-review summary omits recall. |
| SC-05 | Confirmed profile, set and card deletion remove exactly the affected records and review history. Canceled deletion preserves them. |
| SC-06 | Core actions are readable and reachable in portrait and landscape at 100% and 130% font scale. A learner can create a card, reveal, distinguish both ratings, grade and return from the summary. Rotation preserves drafts and study state. |

The Android floor is API 24. The [plan](plan.md) and [quickstart](quickstart.md) map these
criteria to implementation and verification.

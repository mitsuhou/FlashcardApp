# Functional & Non-functional Requirements

Version 3.0. P1 requirements define the v1 release for a local learner. P2 and P3 requirements
are deferred beyond v1. Retired identifiers remain reserved for traceability.

## 1. Functional requirements

| ID | Priority or status | Requirement | Acceptance condition |
| --- | --- | --- | --- |
| FR-01 | P1 | Create, rename, select and delete local profiles. | Persist the selected profile and open its Set List. On launch, restore a valid selection. With no profiles, show creation. With profiles but no valid selection, require an explicit choice even if only one profile remains. Confirm deletion. |
| FR-02 | P1 | Isolate libraries and review data by profile ID. | A foreign or stale ID cannot expose or modify another profile's records. Switching clears the old back stack, drafts, session and UI observations. In-flight work may finish only for its original profile and its result cannot appear in the new profile. |
| FR-03 | Removed / Out of scope in v3.0 | Former Group/Ungrouped management. | — |
| FR-04 | P1 | Create, view, edit and delete study sets in the selected profile. | A trimmed name has 1–80 characters. The optional trimmed description has at most 500 characters, with blank saved as absent. Confirm deletion and remove its cards and review logs. |
| FR-05 | P1 | Create, view, edit and delete two-sided text cards in a set. | Each trimmed face has 1–2,000 characters. Cards remain in the same set after restart. Edits preserve schedule and history. Confirm deletion and remove only that card and its logs. |
| FR-06 | P2 · Deferred | Optional local card images. | Outside v1 acceptance. |
| FR-07 | Removed / Out of scope in v3.0 | Former Free Study and flippable Set Detail preview. | — |
| FR-08 | P1 | Run Graded Study for one set, starting from Set Detail. | A queue snapshot admits only cards in the selected set whose due day is absent or no later than today. Each starts on the front and the learner must reveal the answer before rating Remembered or Forgot. |
| FR-09 | P1 | Persist a grade and next review date. | Use BR-11 and BR-12 to save one ReviewLog and one schedule update in one Room transaction, unique per (sessionId, cardId). Disable ratings, Exit and system Back during a save. Advance only after commit. On failure retain the revealed card and offer retry. Set Detail shows the saved next date on each reviewed card's row. |
| FR-10 | P1 | Show review availability for each set. | Set List and Set Detail show total and eligible counts. Refresh on data changes and when the app resumes on a new local day. Recheck eligibility on study start. An empty queue leaves the learner on Set Detail with an explanation and Add card when the set is empty. |
| FR-11 | P1 | Show Review Summary at completion or confirmed early exit. | Show reviewed, remembered and forgot counts and rounded recall percentage from committed ratings. Omit the percentage at zero reviews. Done returns to the originating Set Detail. |
| FR-12 | P1 | Preserve committed data and handle interruptions. | Saved profile selection, edits, deletions and grades survive restart. A new session may take a fresh queue. An interrupted card receives no grade. Ungraded cards remain eligible if their saved schedule permits. |
| FR-13 | P2 · Deferred | Aggregate review coverage. | Outside v1 acceptance. |
| FR-14 | P3 · Deferred | Optional recall countdown. | Outside v1 acceptance. |
| FR-15 | P3 · Deferred | Appearance personalization. | Outside v1 acceptance. |

## 2. Non-functional requirements

| ID | Priority | Requirement and verification |
| --- | --- | --- |
| NFR-01 | P1 | Complete the profile, editing and study flows in airplane mode from first launch through restart. |
| NFR-02 | P1 | Persist committed writes across process restart, enforce ownership and keep grade logs and schedule updates atomic. |
| NFR-03 | P1 | Show the active profile on Set List, label primary actions, explain empty and error states, and give clear saving feedback. Name the affected item and consequence in destructive confirmations. |
| NFR-04 | P1 | Provide meaningful labels, text and symbols for ratings, touch targets of at least 48 × 48 dp and contrast of at least 4.5:1 for normal text or 3:1 for large text. Check core controls at 100% and 130% font scale in portrait and landscape. |
| NFR-05 | P1 | Keep core actions reachable in portrait, landscape and available window sizes. Preserve editor drafts and the current study state on rotation. |
| NFR-06 | P1 | Keep DAOs in the data layer. Use testable Kotlin scheduling rules with explicit dates, one app module, manual DI and the course's MVVM and unidirectional data flow patterns. |
| NFR-07 | P1 | Keep learner data on the device. Local profiles provide logical isolation. The app needs no credentials and no unnecessary network or shared-storage permissions. |
| NFR-08 | P1 | Reject invalid input, retain a failed draft or grade for retry, and prevent duplicate commits for the same session card even when submissions compete. |
| NFR-09 | P1 | Produce a debug build and focused JVM rules, Room and Compose results, plus a recorded offline walkthrough. |
| NFR-10 | P2 | Enhanced tablet/foldable layouts are deferred beyond v1. |

## 3. Acceptance demo

1. Start in airplane mode. Create and select profile A, then create a set and three text cards.
2. Select B and verify that A's sets are inaccessible. Reselect A and open its set.
3. Start Graded Study from Set Detail. Reveal and rate one card Remembered and one Forgot,
   then confirm early exit. Check two committed ratings, 50% recall and return to Set Detail.
4. Check the saved dates, restart offline and verify the cards, ratings and remaining eligible
   card. Confirm that deleting a set removes its cards and logs while other sets remain.

The [business rules](4-Business-Rules.md) define queue order, dates, duplicates and recovery.
The [screen inventory](5-App-Description.md) defines the six v1 screen types.

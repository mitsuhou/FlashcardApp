# Data model for offline flashcard study

Room owns the library and learning history. Preferences DataStore stores the selected profile ID.
One device, one app process and one database serve all local profiles. Ownership is enforced at
the repository boundary and in the relationships below. Behavioral authority is the
[clarified specification](spec.md).

## Room schema v1

Use generated, non-reused integer primary keys for profiles, groups, sets and cards. Names and
card text may repeat. IDs and creation timestamps are immutable. All fields are required except
those explicitly marked optional. Instants use Unix epoch milliseconds. Review dates use integer
epoch days, converted to and from Kotlin-facing `LocalDate` values.

| Entity | Fields | Foreign keys and indexes |
| --- | --- | --- |
| Profile | id (PK), displayName, createdAt | Primary key identifies a workspace even across rename. The selector reads only profile metadata. |
| StudyGroup | id (PK), ownerId, name, optional description | ownerId → Profile.id, delete cascade. Unique index (ownerId, id) serves as a composite parent key and supports profile listing. |
| StudySet | id (PK), ownerId, optional groupId, name, optional description | ownerId → Profile.id, delete cascade. (ownerId, groupId) → StudyGroup(ownerId, id), delete NO ACTION. Index (ownerId, groupId) serves both ownership and membership lookups. |
| Flashcard | id (PK), setId, front, back, createdAt, stage (initially 0), optional dueDay (initially null) | setId → StudySet.id, delete cascade. Indexes (setId, createdAt, id) and (setId, dueDay, id) support ordered browsing and eligible-card queries. |
| ReviewLog | sessionId (UUID text), cardId, grade (Remembered or Forgot), reviewedAt, previousStage, newStage, nextDueDay | Primary key (sessionId, cardId) enforces one rating per card in a session and supports session totals. cardId → Flashcard.id, delete cascade. Index cardId for history and deletion. |

A card has exactly one owning profile through its set, and a review through its card. Using
those joins for access checks avoids independent owner fields that could disagree. A set
assigned to a group must share that group's owner. A null groupId represents Ungrouped. Keep
ownerId required even when moving a set to Ungrouped. SQLite requires a matching unique
parent key for a composite reference and allows the reference when groupId is null.
[Room foreign keys](https://developer.android.com/reference/androidx/room/ForeignKey) ·
[SQLite composite foreign keys](https://www.sqlite.org/foreignkeys.html#fk_composite)

### Validation and mutations

- Trim profile names, group/set names and card faces before validation and saving. Require a
  nonblank profile name, 1–80 characters per group/set name and 1–2,000 per face.
  Trim leading and trailing whitespace from `StudyGroup.description` and `StudySet.description` before
  validation and persistence. Store blank as null and allow up to 500 characters in the trimmed value.
  Use the same validation for UI feedback and repository writes. Invalid input leaves the saved record intact (BR-01, BR-03, BR-05).
- Create a card with stage 0 and no date. Only grading can change the stage (0 through 6) or
  schedule. An edit updates only editable columns. Moving a set updates only groupId after a
  same-owner lookup. No operation reassigns a card or set to another profile (BR-02, BR-07).
- Group deletion detaches owned sets by clearing **only groupId**, then deletes the owned group
  within the same Room transaction. The composite foreign key uses NO ACTION because a
  composite SET NULL would also clear ownerId. The learner keeps the same sets, cards, reviews
  and dates. Confirmed profile, set or card deletion cascades through its owned descendants.
  A failure rolls back all changes (FR-01, FR-03 through FR-05, BR-04, BR-06).
- Export the v1 Room schema with the app. A later schema version requires a data-preserving
  migration and a migration test against the exported prior version. Opening an existing
  library must never erase it to recover from a schema mismatch (NFR-02).

## Repositories, selection and access

| Boundary | Responsibilities |
| --- | --- |
| ProfileRepository / ProfileDao | List selector metadata, create profiles, validate and persist explicit selection, rename or delete the selected profile. Own the application-scoped selection state and coordinate profile transitions with library mutations. |
| LibraryRepository / LibraryDao | Owner-scoped group/set/card lists, detail and count projections, validation, create, edit, move, detach and delete. Group detail counts include empty groups. Set lists include empty and ungrouped sets. Free Study and Set Detail preview receive read-only card projections from this repository. |
| ReviewRepository / ReviewDao | Observe new/due counts, capture eligible IDs, load a current card within its original scope, commit a grade and aggregate committed results for that profile/session. Share eligibility predicates with set counts. |

**Selection lifecycle (US4, FR-01, FR-02).** On launch, resolve the saved ID against Room
before exposing a library. If it exists, restore it. If absent or invalid, show the selector
whenever profiles remain, even for one profile. Keep the active profile unset while awaiting explicit selection. With zero profiles, show profile creation.
A storage read failure yields a retryable error and cannot be interpreted as an empty library.
Profile creation commits its row first. If explicit selection then fails to persist, retain the
new profile and retry selection without creating another row.

In the single AppContainer, serialize selection changes and owned writes with one coroutine
mutex. A selected workspace carries its ID and an in-memory generation that changes on each
selection transition. Bind routes, study sessions, drafts and commands to this selection.
A write acquires the mutex, verifies its original selection, runs its complete Room transaction
and then releases the mutex. A profile switch taking the lock first invalidates the old
command. If the write commits first, it belongs to the previous workspace before the switch.
This also rejects an old A command after switching A → B → A.

Persist a validated new selection before exposing its Home. During a switch discard old screen
state, cancel old observations and show a neutral transition. Recreate the navigation scope for
the new generation. Use owner-filtered queries for every read and tag results with the originating
selection so a delayed emission cannot populate a new screen. A route or item ID alone grants
no access. For cards and logs, filter through the owning set in the query itself. For commands,
require the parent and target IDs to match that scope and check the affected row count. Treat
unavailable and foreign IDs alike.

Room deletion and DataStore edits are separate commits. On active-profile deletion, serialize
the operation, commit the Room deletion, invalidate the selection, then clear its preference.
If interrupted between those writes, the old ID fails validation on the next launch. A failed
preference cleanup must not restore the deleted workspace. A new explicit selection supersedes
the stale ID. Use a single DataStore instance and never hold a Room transaction across a
DataStore edit. [DataStore guidance](https://developer.android.com/topic/libraries/architecture/datastore)

## Queues and dates

| Consumer | Selection and ordering |
| --- | --- |
| Set Detail and Free Study (FR-07, BR-09) | All cards in the selected owned set, ordered by createdAt then id ascending. Read both faces and the optional saved date. Flip and browse affect only screen state. |
| Home and Set Detail (FR-10, BR-10) | New = dueDay is null. Due reviewed = dueDay is non-null and no later than today's local epoch day. Home shows both counts for its owner. Set rows show total and eligible within each owned set. Count card IDs when using an outer join so empty sets count as zero. |
| Graded Review (FR-08, BR-10) | Take one ordered snapshot of eligible IDs and bind it to the selected profile and optional set. Sort reviewed cards first, by dueDay then id ascending. Sort new cards after them by createdAt then id ascending. Include each ID once. New arrivals wait for the next session. |

Keep a session UUID, origin, immutable queue, position, visible face, has-revealed flag and pending
submission in the screen ViewModel across rotation. Cards start on Front. After a reveal the
learner may flip back and still rate the current card. A different card resets the reveal flag.
At presentation and commit recheck existence, ownership and eligibility. Skip cards that have
become unavailable without removing their slots. Display the current slot’s original one-based
position and the original snapshot size. A skip adds no ReviewLog row or summary count.
A profile switch discards the entire old session. A local I/O failure keeps
the current card for retry. After process loss, starting a fresh session from current eligibility
is sufficient (BR-10, BR-11, BR-13).

The pure scheduler takes the current stage, grade and local review date. Its result is a stage
and next date. Inside the grading transaction sample the injected clock once and resolve the
current device zone for that instant. Store the instant as reviewedAt and derive today in that
zone. Remembered increases the stage by one up to 6, with offsets of 1, 3, 7, 14, 30 and 60
calendar days for resulting stages 1–6. Forgot from any stage resets to 0 and schedules tomorrow.
Add days to the actual review date even when overdue. A forgotten card has a saved dueDay, so
stage 0 alone never identifies a new card. Recompute today on launch, session start and return
to the foreground. Use the refreshed local day for Home, Library rows and the open Set Detail count even when Room has emitted no changes. Date/zone changes affect comparisons only. See the exact transition table
in [FR-09 and BR-12](spec.md#functional-requirements) and the
[LocalDate reference](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalDate.html).

## Commit and recovery

1. Bind a grade action to the session UUID and current card ID at the tap. Accept it only after
   reveal. While saving, disable both grade actions and keep the same pending key across retries.
2. Under the checked selection and one Room transaction, locate the card in the original owned
   scope. Look for a review for that session/card **before** testing whether the card is still
   due. If a previous attempt committed, return the stored outcome without changing the card.
3. For a first submission, recheck current eligibility, compute the new schedule, insert the
   log with an aborting conflict policy, and update exactly one owned card's stage and dueDay.
   Propagate any failure out of the transaction to roll back both changes. If a concurrent
   insert won the unique key, reread the owned result before acknowledging it. Preserve the
   first recorded grade even when a delayed tap carries the other button's value.
4. After commit, advance only if the completion matches the still-current selection, session
   and card. This prevents two callbacks from skipping a card. If interrupted before commit,
   the card remains eligible. If interrupted after commit, the saved date and history survive.
   Reopening on the same local day excludes that card from a fresh queue.
5. Compute summary totals from owned ReviewLog rows for that session. Reviewed equals Remembered
   plus Forgot. Round 100 × Remembered / reviewed to the nearest whole percent and omit it at
   zero. Exiting with unreviewed cards requires confirmation. A canceled exit leaves the current
   session intact (FR-09, FR-11, FR-12, BR-11 through BR-13, NFR-08).

Use one database instance and a Room transaction spanning all relevant DAO calls. The unique
key remains effective after closing and reopening the database.
[Room transactions](https://developer.android.com/reference/androidx/room/Transaction)

## Verification

| Exercise | Expected storage result | Trace |
| --- | --- | --- |
| Seed two profiles with identical labels. Try stale detail, moves, deletes, and grade actions after switching. Delay a query and switch back to A. | No foreign rows, counts, mutations or stale emissions. A direct cross-owner set/group link fails the foreign key. | US4, FR-02, BR-02, NFR-07, SC-01 |
| Delete a group with reviewed cards, then a set, one card and one profile in independent cases. Reopen the file database. | Ungrouping preserves IDs/dates/history. Cascades remove only the intended descendants. Stale selected IDs lead to explicit selection or creation. | US3, US4, FR-01, FR-03 through FR-05, BR-04, BR-06, SC-05 |
| Compare data before/after preview, Free Study, text edits and moves. | No extra reviews or changed stages/dates. Invalid writes leave the old record intact. | US2, US3, FR-05, FR-07, BR-05, BR-07, BR-09, SC-02 |
| Exercise each ladder transition, stage cap, overdue and forgotten cards, equal sort keys, midnight, DST and device-zone change. | Exact local dates and order match the specification. New and forgotten stage-0 cards are distinguished. | US1, FR-08 through FR-10, BR-10, BR-12, SC-03 |
| Abort after log insertion but before schedule update, retry twice concurrently, then simulate a lost acknowledgment and reopen the file. | Rollback leaves neither change. Successful retry leaves exactly one record and one stage transition. An identical retry returns the committed outcome. | US1, FR-09, FR-11, FR-12, BR-11, BR-13, NFR-02, NFR-08, SC-04 |

Run repository tests through their normal boundaries, plus direct constraint tests against
Room on Android. Use both in-memory failure/race cases and a file-backed close/reopen case.

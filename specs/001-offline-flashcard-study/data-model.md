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

The Application's AppContainer constructs each repository with the DAOs it uses.
Expose Room observations as ordinary Flow values and one-time queries and writes as
suspend functions. The ViewModel supplies its screen's profileId. In the tables
above, ownerId is the stored key corresponding to that profileId.

| Repository | Responsibilities |
| --- | --- |
| ProfileRepository | Inject ProfileDao and one Preferences DataStore. List profile metadata, create, rename and delete by ID, and store the selected ID. |
| LibraryRepository | Inject LibraryDao. Read and edit groups, sets and cards by profileId. Include empty groups and sets in counts. Provide the read-only cards for preview and Free Study. |
| ReviewRepository | Inject ReviewDao. Read counts, ordered eligible IDs and owned cards by profileId and optional setId. Submit grades through the DAO transaction and read committed session totals. |

On launch, check the saved ID against Room. Restore a valid profile. With an absent or invalid ID,
show creation if no profiles remain, or the selector even with one remaining profile. Expose
no library before explicit selection. A storage read failure offers Retry. If profile creation
succeeds but selection fails, retry selecting the existing ID. Delete the profile in Room
before clearing its preference. Clear it only if it still contains the deleted ID, so a later
selection survives. After interruption the same launch check handles a deleted ID.

Use the profileId captured at the start throughout the operation. Filter groups and sets by
ownerId and join cards and logs through the owning set. Check the parent on creation and on
moves. An update or delete with a foreign ID affects zero rows. In particular, editing a
card's text updates only its front and back. Group deletion uses the atomic detachment above.

On switch, clear the old navigation stack and its screen state and cancel its UI observations.
The new screen observes only the selected profile. An in-flight write may finish for its
original owner. It cannot write into the newly selected library or update that screen.

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

Pass the local review date to the pure scheduling function. Remembered advances one stage up
to 6 with offsets of 1, 3, 7, 14, 30 and 60 calendar days. Forgot resets to 0 and is due
tomorrow. Base the next date on the actual review day, including for overdue cards.
A forgotten card has a dueDay and counts as reviewed. See the [schedule table](spec.md#functional-requirements).

Read the current device-local date when Home, Library or Set Detail resumes and when review
begins. Refresh the relevant count queries with that day even if Room has had no writes.
Build the queue for the same day. If empty, stay at the entry screen and show the empty state.
On grade, derive reviewedAt and the local review day from the same instant using the device’s
current time zone. Date and zone changes affect comparisons and leave stored due dates intact.

## Grade transaction

1. The ViewModel accepts an explicit tap on a revealed card. Set the saving flag before
   launching the write. Disable both ratings, Exit and system Back while it is pending.
2. ReviewRepository calls one Room DAO transaction with the originating profileId, optional
   setId, sessionId, cardId and grade. Check the card belongs to that scope. If the session/card
   already has a log, leave both tables untouched. Otherwise check eligibility for the local
   review day, calculate the schedule, insert the log and update exactly one owned card.
   A failed insert, a failed update or an update affecting zero rows must abort the transaction. The composite primary key also
   prevents two concurrent submissions from recording a second grade.
3. On commit, advance once and use committed session totals for the summary. On failure,
   keep the revealed card for Retry. Restore navigation in either case. Confirm an early exit
   when unreviewed cards remain. At zero reviews omit the percentage, otherwise round
   100 × Remembered / reviewed to the nearest whole percent.
4. After process restart, read the library and schedules from Room. The next review takes a
   fresh eligible snapshot. Only committed grades affect that queue.

[Room DAO transactions](https://developer.android.com/reference/androidx/room/Transaction)

## Verification

| Exercise | Expected storage result | Trace |
| --- | --- | --- |
| Seed A and B with identical names. Start a write for A, select B, then let it finish. Try A’s item IDs from B. | Only A can receive the started write. B sees and changes only its own records. A direct cross-owner set/group link fails the foreign key. | US4, FR-02, BR-02, NFR-07, SC-01 |
| Delete a group with reviewed cards, then a set, one card and one profile in independent cases. Reopen the file database. | Ungrouping preserves IDs/dates/history. Cascades remove only the intended descendants. Stale selected IDs lead to explicit selection or creation. | US3, US4, FR-01, FR-03 through FR-05, BR-04, BR-06, SC-05 |
| Compare data before/after preview, Free Study, text edits and moves. | No extra reviews or changed stages/dates. Invalid writes leave the old record intact. | US2, US3, FR-05, FR-07, BR-05, BR-07, BR-09, SC-02 |
| Exercise each ladder transition, stage cap, overdue and forgotten cards, equal sort keys, midnight, DST and device-zone change. | Exact local dates and order match the specification. New and forgotten stage-0 cards are distinguished. | US1, FR-08 through FR-10, BR-10, BR-12, SC-03 |
| Fail between the log insert and schedule update. Retry, then submit competing grades for one session/card. Close and reopen the database. | Rollback persists neither change. Successful and duplicate submissions together leave one log and one schedule change. A fresh queue respects the saved date. | US1, FR-09, FR-11, FR-12, BR-11, BR-13, NFR-02, NFR-08, SC-04 |

Run repository tests through their normal boundaries, plus direct constraint tests against
Room on Android. Use both in-memory failure/race cases and a file-backed close/reopen case.

# FlashcardApp MVP status

**2026-11-23 · Phase 1 complete. Phase 2 is in progress.** The tracker shows **14 of 51 tasks complete** (T001–T014). T015–T051 await approval or implementation. [Implementation tasks](tasks.md)

| Phase | Pending tasks |
| --- | --- |
| 1 · Project setup and shared configuration | None |
| 2 · Database, repositories and shared foundations | T015–T023 |
| 3 · US4 Local Profiles | T024–T029 |
| 4 · US3 Study Library | T030–T037 |
| 5 · US2 Free Study | T038–T041 |
| 6 · US1 Graded Review | T042–T047 |
| 7 · Integration testing and final validation | T048–T051 |

**Completed SDD stages.** Constitution ratification, feature specification and clarification, planning and research, data model and validation guide, task generation, checklist review, consistency analysis and remediation. The earlier baseline is recorded at `869ff1d`. The approved documentation revision of 2026-11-23 updates profile ownership, grading, calendar handling and task review.

**Phase 1 verification.** The debug and test APKs built. One starter JVM test and one device test passed. The final starter launched on Android 7.0/API 24 and remained resumed. Dependency, merged-release backup/permission and theme contrast checks passed. [Phase 1 verification](quickstart.md#phase-1-verification)

**Phase 2 verification.** T006 field validation was approved after all eight debug JVM tests passed, including seven validation tests. T007 review rules were approved after all 15 debug JVM tests passed, including seven review-rule tests. T008 was approved after the profile entity compiled, all 15 JVM tests passed and a rename check preserved its ID and creation timestamp.

**T009 approved.** Both Room entities compiled and all 15 debug JVM tests passed. The compiled foreign keys and indexes match the design. An in-memory SQLite check of the equivalent constraints accepted an ungrouped set, rejected a cross-profile link and verified group detachment and profile cascades.

**T010 approved.** The card holds its current stage and due day. Each log records the session ID, card ID, grade and review timestamp. All 15 debug JVM tests passed. The compiled log fields and Room key annotations were checked. Equivalent SQLite constraints rejected a duplicate session/card, allowed the same card in another session and cascaded deletion only to that card’s logs.

**T011 approved.** Group and set list projections omit descriptions. Detail queries still return them. All 15 debug JVM tests passed. Room compiled both DAOs in an isolated fixture. SQLite checks on its generated schema covered ownership, empty counts, card order and preservation of schedules on text edits. After review, group and set creation use Room-generated inserts. The revised empty-group delete returned zero for nonempty, foreign and missing groups, and one for an owned empty group. Detaching a set preserved its owner.

**T012 approved.** All 15 debug JVM tests passed. Room compiled the DAO in an isolated fixture. SQLite checks on its generated schema verified that Home counts agree with the set projections and queue, tie ordering is stable, empty scopes return zero counts, foreign writes are rejected, and session totals count only the owning profile’s grades.

**T013 approved.** KSP compiled the database and three DAOs, the v1 schema was exported and checked, the debug APK built, and all 15 JVM tests passed. [Database verification](quickstart.md#t013-database-verification)

**T014 approved.** All 15 debug JVM tests passed. A separate JVM check with a test double for ProfileDao verified the list flow, trimmed names, blank-name rejection, distinct generated IDs for matching names, and rename and delete by ID. Both operations reported a missing ID.

**Current task.** T015 (persist and validate the selected profile ID) is implemented and awaiting review. All 15 debug JVM tests passed. A separate JVM check with DAO and DataStore test doubles verified restoration, retry after read and write failures, and an absent selection with zero or one remaining profile. Deleting A preserved a newer selection of B. When preference cleanup failed, observers and a new repository both treated the deleted ID as unselected. File-backed integration tests are scheduled for T021.

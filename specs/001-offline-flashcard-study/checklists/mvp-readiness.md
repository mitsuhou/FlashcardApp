# Requirements Quality Checklist: FlashcardApp P1 MVP

**Purpose** · Peer review of the written requirements and design before task generation for the three-week MVP.  
**Created** · 2026-11-21  
**Feature** · [Offline flashcard study and scheduled review](../spec.md)

The reviewer owns the checkboxes. Mark an item `[x]` when the cited documents give a clear, consistent answer. Each mark records a judgment about requirements quality.

## Scope, governance and dependencies

- [x] CHK001 Do the spec and release boundary identify US1–US4 as individually testable P1 stories and keep deferred features outside course acceptance? [Completeness · Spec, User Scenarios & Testing and Scope and source decisions · Plan, Test strategy and release boundary]
- [x] CHK002 Does the plan map every constitutional principle to a design decision and the evidence needed for release, including offline first launch, maintainable UI, repository boundaries and independently testable rules? [Traceability · Constitution I–VIII · Plan, Constitution Check · NFR-01, NFR-06, NFR-09]
- [x] CHK003 Are prerequisites and exit evidence explicit for setup → data foundations → US4 Profiles → US3 Library → US2 Free Study → US1 Graded Review → integration, with time reserved for final validation? [Consistency · Plan, Implementation sequence · Constitution, Three-Week Delivery Scope]
- [x] CHK004 Do research and quickstart distinguish selected dependency versions from verified compatibility, and identify the evidence needed for the build toolchain, API 24 floor and course device? [Feasibility · Research, Toolchain and runtime · Plan, Phase 1 · Quickstart, Prepare and build]

## Ownership and persistence

- [x] CHK005 Is the active-profile rule consistent for a valid selection, an invalid selection with survivors, and an empty installation, including the interval before explicit reselection? [Consistency · Spec FR-01, US4 · Business Rules, Glossary and BR-01 · Plan, Runtime design]
- [x] CHK006 Are the ownership rules explicit for groups, sets, cards, reviews and counts, including stale reads or writes after an A → B → A switch? [Coverage · FR-02, BR-02, SC-01 · Data model, Repositories, selection and access]
- [x] CHK007 Do the spec and data model agree on what survives an edit, move, group deletion or confirmed profile/set/card deletion, including another profile’s records? [Consistency · FR-01–FR-05, BR-04, BR-06, BR-07, SC-05]
- [x] CHK008 Do the requirements distinguish rejected input, a failed save and intentional discard, including field limits, draft retention and destructive-action confirmation? [Clarity · US3, FR-03–FR-05, BR-03, BR-05, NFR-03, NFR-08]

## Study rules, errors and recovery

- [x] CHK009 Is Free Study defined as read-only for learning data, with an ordered set that includes future cards and clear empty, single-card and navigation-boundary cases? [Coverage · FR-07, BR-09, SC-02 · UI-06, UI-08]
- [x] CHK010 Is the graded queue defined for both entry points, including new versus previously forgotten cards, date ordering, ties, and what happens to position and total when a queued card is skipped? [Clarity · FR-08, FR-10, BR-10, UI-09, SC-03]
- [x] CHK011 Does the scheduling policy specify local review day, all six intervals, the cap, Forgot reset, overdue reviews and date or zone changes with determinable next dates? [Completeness · FR-09, BR-12, SC-03 · Data model, Queues and dates]
- [x] CHK012 Do the spec and model distinguish a rejected grade from a saved grade whose acknowledgement was lost, with an unambiguous outcome for retry, duplicate taps and advancement? [Clarity · US1 scenario 4, FR-09, NFR-02, NFR-08 · Data model, Commit and recovery]
- [x] CHK013 Do the recovery and summary criteria distinguish rotation, restart, canceled exit and confirmed early exit, including a pending grade and zero committed reviews? [Coverage · FR-11, FR-12, BR-13, SC-04 · Plan, Runtime design]

## UI and acceptance evidence

- [x] CHK014 Are UI-01–UI-11 and REF-01–REF-07 mapped to approved interactions, including preview flips, reveal-gated ratings, date placement on Set Detail and mode-specific controls? [Traceability · Spec, Screens and visual acceptance · Plan, Screens and navigation · FR-07–FR-10]
- [x] CHK015 Are empty, loading and retry states distinguishable in the written criteria, particularly an empty library versus a storage read failure during profile selection? [Coverage · NFR-03, UI-01–UI-03 · Data model, Repositories, selection and access]
- [x] CHK016 Are contrast, enlarged text, touch targets and reachable actions in both orientations described precisely enough for two reviewers to assess SC-06 on comparable settings? [Measurability · NFR-04, NFR-05 · UI Description §13 · Quickstart, Offline course demo]
- [x] CHK017 Does the acceptance matrix map each SC-01–SC-06 outcome to evidence at the appropriate level, including pure scheduling rules, persistent storage failures, navigation and an offline restart? [Coverage · Constitution, Verification · NFR-09 · Quickstart, Automated acceptance matrix and Offline course demo]

## Notes

**Requirements review · 17/17 satisfied**

| Item | Written evidence | Source |
| --- | --- | --- |
| CHK001 | Each story names its own starting data and independent test. The release boundary requires all four at P1 and explicitly defers the optional IDs. | [Stories](../spec.md#user-scenarios--testing) · [Release boundary](../plan.md#test-strategy-and-release-boundary) |
| CHK002 | The eight-principle table pairs each design decision with release evidence, including offline first launch, ownership, atomic writes and isolated rule tests. | [Constitution Check](../plan.md#constitution-check) |
| CHK003 | The seven phase rows give prerequisites and exit evidence. Setup and storage precede US4 → US3 → US2 → US1, with Week 3 reserved for integration, fixes and the demo. | [Implementation sequence](../plan.md#implementation-sequence) |
| CHK004 | Research labels dependency versions as planned pins. The guide assigns the API 24 starter build/launch to Phase 1, schema generation and concrete wiring to Phase 2, and API 24 calendar verification and presentation-runtime evidence to Phase 7. The API 33 development device is recorded separately. | [Toolchain](../research.md#toolchain-and-runtime) · [Build prerequisites](../quickstart.md#prepare-and-build) |
| CHK005 | BR-01 and FR-01 restore a valid selection, show creation with zero profiles, and keep the active profile unset while awaiting explicit selection, even with one survivor. | [Glossary and BR-01](../../../docs/4-Business-Rules.md#1-glossary) · [FR-01](../spec.md#functional-requirements) |
| CHK006 | Sets and groups have an owner. Cards and reviews inherit ownership through their parents. Scoped queries cover counts and details, and the selection generation invalidates delayed A actions after A → B → A. | [Schema](../data-model.md#room-schema-v1) · [Access rules](../data-model.md#repositories-selection-and-access) |
| CHK007 | Text edits and same-owner moves preserve learning records. Group deletion detaches sets. Profile, set and card deletion cascade only through owned descendants. SC-05 specifies surviving and removed records. | [Mutation rules](../data-model.md#validation-and-mutations) · [SC-05](../spec.md#measurable-outcomes) |
| CHK008 | BR-03/05 and US3 specify trimmed bounds, inline errors, disabled invalid saves, retained drafts on write failure, and confirmation before discard or deletion. | [US3 scenarios](../spec.md#user-story-3--build-and-maintain-a-study-library-p1) · [Field limits](../spec.md#functional-requirements) |
| CHK009 | FR-07 defines all cards in creation/ID order. US2 and the edge cases cover empty sets, single cards and list boundaries. SC-02 compares dates, counts and history before and after practice. | [US2](../spec.md#user-story-2--practice-a-whole-set-freely-p1) · [Edge cases](../spec.md#edge-cases) |
| CHK010 | BR-10 defines both entry scopes, eligibility and ordering. Stage 0 with a due date denotes a previously forgotten card. US1 scenario 7 gives the frozen-position example: “3 of 3,” one reviewed before the third grade, two after. | [Queue rules](../data-model.md#queues-and-dates) · [US1 scenarios](../spec.md#user-story-1--review-what-is-due-today-p1) |
| CHK011 | BR-12 specifies the local review day, all six offsets, the stage-6 cap and next-day reset. The matrix includes late reviews, calendar boundaries and zone changes with stored dates preserved. | [Schedule table](../spec.md#functional-requirements) · [Acceptance matrix](../quickstart.md#automated-acceptance-matrix) |
| CHK012 | US1 scenarios 4 and 8 distinguish rollback from lost acknowledgement. The commit protocol looks up the saved session/card outcome before checking whether it is due again. Replay returns that result with one rating, one schedule update and one advance. | [US1 scenarios](../spec.md#user-story-1--review-what-is-due-today-p1) · [Commit and recovery](../data-model.md#commit-and-recovery) |
| CHK013 | Rotation retains study state. Restart may rebuild the queue. Canceled exit continues it, confirmed exit summarizes saved grades, and a pending grade settles before the summary. Zero reviewed has no percentage. | [Edge cases](../spec.md#edge-cases) · [Runtime design](../plan.md#runtime-design) |
| CHK014 | The screen inventory maps all eleven screens, links all seven visual references and names the local actions for each, including date placement, read-only preview and reveal-gated ratings. | [Screen inventory](../spec.md#screens-and-visual-acceptance) · [Navigation](../plan.md#screens-and-navigation) |
| CHK015 | The launch flow has a neutral loading state. A storage read failure offers retry. A successful read yielding zero profiles leads to creation, while an empty library or queue has its own next action. | [Selection lifecycle](../data-model.md#repositories-selection-and-access) · [Edge cases](../spec.md#edge-cases) |
| CHK016 | NFR-04 specifies both 48 dp dimensions, contrast minima of 4.5:1 and 3:1, and the Android size/weight convention for large text. SC-06 and the guide verify creation, flip, both identifiable grading actions, grade submission and Done in each of the four orientation and font-scale combinations. | [NFR-04](../spec.md#non-functional-requirements) · [SC-06 walkthrough](../quickstart.md#offline-course-demo) · [Android text visibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps#increase-text-visibility) |
| CHK017 | The matrix assigns SC-01 isolation/restart, SC-02 read-only study, SC-03 scheduling, SC-04 commit/retry, SC-05 deletion and SC-06 accessible navigation to named automated checks and offline demo steps. | [Acceptance matrix](../quickstart.md#automated-acceptance-matrix) · [Offline demo](../quickstart.md#offline-course-demo) |

**Feasibility evidence.** The plan retains API 24 as the minimum. Research records an API 33 development runtime. Dependency resolution, the API 24 smoke check and the presentation device remain implementation-stage evidence. [Research](../research.md#toolchain-and-runtime) · [Quickstart](../quickstart.md#prepare-and-build)

**Review sources.** [Constitution](../../../.specify/memory/constitution.md) · [Specification](../spec.md) · [Plan](../plan.md) · [Research](../research.md) · [Data model](../data-model.md) · [Quickstart](../quickstart.md)

`$speckit-implement` reads reviewer-owned markers without changing them.

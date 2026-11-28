# MVP Readiness Checklist — FlashcardApp v3.0

This checklist evaluates the written scope and verification plan.
Runtime evidence belongs in the [quickstart](../quickstart.md#v30-verification-record).

| ID | Checked | Documentation criterion | Evidence |
| --- | --- | --- | --- |
| CHK001 | [x] | All three P1 stories and six screens are in scope, with deferred requirements outside v1. | [Spec](../spec.md) |
| CHK002 | [x] | Offline behavior, architecture and quality gates satisfy all constitutional principles. | [Constitution check](../plan.md#constitution-check) |
| CHK003 | [x] | Existing foundation IDs remain in order with only impacted tasks reopened. Story and final verification tasks total 12. | [Tasks](../tasks.md), [Tracker](../current.md) |
| CHK004 | [x] | Historical results and pending v3.0 runtime checks have clear dates, devices and evidence targets. | [Quickstart](../quickstart.md) |
| CHK005 | [x] | Valid selection opens Set List. Invalid selection, including with one survivor, requires a choice. Zero profiles opens creation. | [Profile and library rules](../../../docs/4-Business-Rules.md#2-profile-and-library), [Profile routes](../plan.md#profile-selection-and-routes) |
| CHK006 | [x] | Stale IDs and in-flight writes preserve profile isolation. Every study operation requires the originating set. | [Set-scoped queries](../data-model.md#set-scoped-study-queries) |
| CHK007 | [x] | Four tables, the reset policy, scoped cascades and history preservation agree with the approved model. | [Schema](../data-model.md#room-schema-v1), [Mutations](../data-model.md#validation-and-mutations) |
| CHK008 | [x] | Field limits, normalization, named confirmations and draft recovery are measurable. | [Requirements and traceability](../spec.md#requirements-and-traceability) |
| CHK009 | [x] | Set Detail has one study entry. Empty and stale states have clear routes and feedback. | [Screens](../spec.md#screens), [US1](../spec.md#us1--graded-study-of-a-set) |
| CHK010 | [x] | The queue rules cover set ownership, empty sets, ties, original positions and unavailable cards. | [Queue and grade transaction](../data-model.md#queue-and-grade-transaction) |
| CHK011 | [x] | The schedule, local date policy, stage cap and Forgot reset can be tested with known dates. | [Scheduling](../../../docs/4-Business-Rules.md#br-12--date-scheduling-fr-09) |
| CHK012 | [x] | Rollback, retry, uniqueness, scoped summary counts and reopened storage have automated checks. | [Matrix](../quickstart.md#automated-acceptance-matrix) |
| CHK013 | [x] | Rotation, blocked navigation during save, restart and zero-review summary are specified. | [Spec](../spec.md), [Matrix](../quickstart.md#automated-acceptance-matrix) |
| CHK014 | [x] | Active screen IDs and reference usage agree with the approved design. | [Screens](../spec.md#screens), [UI description](../../../docs/6-ui-design/ui-description.md) |
| CHK015 | [x] | The UI distinguishes loading, empty results, saving and actionable failures. | [Screens](../spec.md#screens), [Runtime design](../plan.md#runtime-design) |
| CHK016 | [x] | Labels, 48 dp touch targets, contrast, two font scales and orientations have concrete checks. | [Offline walkthrough](../quickstart.md#offline-course-demo) |
| CHK017 | [x] | Focused JVM, Room, representative Compose and one offline walkthrough cover SC-01–SC-06. | [Acceptance index](../tasks.md#acceptance-and-scope-index), [Matrix](../quickstart.md#automated-acceptance-matrix) |

- [ ] Verify and approve the updated Phase 2 checkpoint.
- [ ] Verify and approve the three story checkpoints.
- [ ] Record the build, test and offline demo results for v3.0.

# Quickstart — FlashcardApp v3.0 validation

The three dated records below preserve the original Phase 1 and Phase 2 results verbatim.
Their counts, schema and task references describe what was verified at the time.
Current tasks and pending checks begin at [Prepare and build](#prepare-and-build).

## Phase 1 verification

**Passed on 2026-11-22 (T001–T005).** The debug starter and instrumentation APK built with Gradle 9.5.0, daemon JDK 25.0.2, AGP 9.3.3, the Kotlin Compose plugin 2.2.10 and Java 11 bytecode. The app uses minSdk 24 and compile/target SDK 37.

| Check | Observed result |
| --- | --- |
| Dependencies | Inspected debug, JVM-test, instrumentation-test, KSP processor and desugaring classpaths with `:app:dependencies`. Navigation Compose 2.10.2, Lifecycle Compose 2.11.0, Room runtime/compiler 2.8.5, KSP 2.3.14, DataStore Preferences 1.3.0, coroutines Android/test 1.10.2 and desugaring 2.1.5 resolved. The final report had zero failed entries. |
| Compose and Room setup | Compose BOM 2026.02.01 applies to the app, debug tooling and instrumentation. The debug test host and Compose UI test library resolved to 1.11.2. KSP ran, and Room’s schema copy reported `NO-SOURCE`. T013 verifies the first generated schema when the database exists. |
| Release privacy | The merged release manifest sets `allowBackup=false` and references both rule files. Legacy backup, cloud backup and device transfer each exclude the database domain and the `files/datastore` directory. Its sole requested permission was the app-private dynamic-receiver permission. |
| Dark theme | Foreground/background contrast measured 5.87:1 for white on indigo, 17.34:1 for primary text on navy, and 10.41:1, 6.65:1 and 4.52:1 for primary, secondary and muted text on the card surface. Remembered and Forgot on the grade-action surface measured 5.60:1 and 6.78:1. All exceed 4.5:1. T048 measures the rendered controls. |
| Build and tests | `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:testDebugUnitTest` and `:app:processReleaseMainManifest` succeeded. One starter JVM test passed. The debug and test APKs installed on an AOSP x86_64 Android 7.0 emulator (API 24, image revision 8), and the starter instrumentation test passed (1/1). |
| Starter launch | ADB reported API 24. Launching `com.example.flashcardapp/.MainActivity` with `am start -W` returned `Status: ok`. A subsequent `dumpsys activity activities` check showed MainActivity resumed. |

After verification, the temporary API 24 emulator shut down. Its virtual device directory, extracted system image and downloaded archive were removed.

## T013 database verification

On 2026-11-23, `./gradlew --offline :app:assembleDebug :app:testDebugUnitTest` built the debug APK and all 15 JVM tests passed. KSP generated the database and all three DAO implementations. The [exported v1 schema](../../app/schemas/com.example.flashcardapp.data.local.FlashcardDatabase/1.json) matches the approved five tables, keys, indexes, nullability and defaults.

A host SQLite check using the exported SQL rejected a cross-profile group link and a duplicate session/card grade. Reopening the test file preserved its card and review. The database builder uses Room's default migration policy. This first schema establishes the baseline for future migration tests.

## Phase 2 foundation checkpoint

On 2026-11-24, the debug app and instrumentation APKs built offline. All 15 JVM tests and all 22 instrumentation tests passed on the checkpoint run. The device suites ran on WayDroid x86_64, Android 13 (API 33), with zero skipped tests.

- KSP generated the database and three DAO implementations. The exported v1 schema contains five tables and uses `(sessionId, cardId)` as the review log primary key.
- The merged debug manifest names `FlashcardApplication`. The activity wiring test resolved its container through the default ViewModel creation extras.
- Seven library tests, seven profile-selection tests and four queue tests passed. They cover ownership, cascades, restored selection, queue order and local-date eligibility.
- Both grade transaction tests passed. A forced failure during the schedule update rolled back the new log and preserved the previous grade. After retry, a conflicting repeat and file reopen, the session held one Remembered and one Forgot, with one of three cards still eligible that day. Concurrent conflicting submissions for one new card produced one log and one matching schedule update.

The checkpoint passed. T023 was approved, completing Phase 2 (T006–T023).

## Prepare and build

Use SDK Platform 37, Build Tools 36.0.0, Platform Tools and the repository's Gradle 9.5.0
wrapper with JDK 25. The app targets Java 11 bytecode and supports API 24. Use a disposable
phone emulator or development device for the pre-release schema reset and deletion checks.
Before running v3.0, use a clean development installation so it starts with the new
four-table Room v1 schema.

```sh
adb devices -l
adb shell getprop ro.build.version.sdk
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:processReleaseMainManifest
./gradlew :app:connectedDebugAndroidTest
```

Use a connected target reported as `device`. Check actual test cases and counts in
`app/build/reports/tests/` and `app/build/reports/androidTests/connected/`.
Inspect the four-table v1 JSON and merged manifest for release backup exclusions and
unnecessary network or shared-storage permissions. For T038, run a short API 24 smoke of the
implemented calendar path. With a stage-1 card rated Remembered on November 24 at a fixed
local date, expect stage 2 and a November 27 due date after reopen. Record the device/API.
The course device may be the same device or a separate one.

## Phase checkpoints

| Phase | Evidence to record |
| --- | --- |
| 1 · T001–T005 | Approved historical starter build, tests, manifest and API 24 launch above. |
| 2 · T006–T023 | Four-table Room export, application/container wiring and focused JVM/Room suites. Check profile isolation, cascades, persisted selection, required set scope and atomic grade durability. |
| 3 · T024–T026 | Profile UI, validated selection into the Set List route, recovery and Back after switching. |
| 4 · T027–T033 | Owned Set List and Set Detail, the shared editor, validation and count refresh on a new local day. |
| 5 · T034–T037 | Single-set study, answer reveal, save/retry, summary totals and return to Set Detail. |
| 6 · T038–T039 | Debug build and suite results, merged manifest and API 24 smoke, plus the offline demo and accessibility observations. |

Record the relevant checks at each checkpoint and obtain approval before starting the next phase.

## Automated acceptance matrix

Use fixed local dates in tests and two profiles when checking isolation. Cover detailed
edge cases at the rules and storage boundaries and keep Compose checks representative.

| Level | Representative evidence | Tasks and coverage |
| --- | --- | --- |
| JVM rules and validation | Check all Remembered intervals and cap, Forgot, overdue and calendar boundaries, and zero/rounded summaries. Check blank, exact and over-limit set names and card faces, and trim or clear the set description. | T006–T007 · SC-02–SC-04 · NFR-06, NFR-08 |
| Room and DataStore | With separate profiles and identically named sets, try stale IDs and complete a started write after switching. Reopen files and verify selection recovery and exactly scoped profile/set/card cascades. Verify saved descriptions and schedule/history preservation on edits. | T020–T021 · SC-01, SC-02, SC-05 · NFR-02 |
| Room study | Seed multiple sets and profiles and check target-set counts, empty queue, date/ID ordering, future and forgotten cards, and eligibility on a new local day. Verify wrong-set and wrong-profile reads/writes, including zero summary contribution. Force a rollback, retry and submit competing grades. Reopen and verify the committed result. | T022–T023 · SC-03, SC-04 · NFR-02, NFR-08 |
| Profile and library UI | Exercise profile creation or switching and Back isolation, one invalid editor input, save/return and a count refresh without a database write. Check actual scope when live Set List is connected. | T026, T033 · SC-01–SC-03 · NFR-03, NFR-08 |
| Study ViewModel and Compose | Check answer reveal, frozen positions and skip, no grade triggered by recomposition, Exit/Back blocked during save, failure/retry, empty entry and Summary → Set Detail. Use a committed summary and a zero-grade case. | T037 · SC-03, SC-04 · NFR-03, NFR-08 |

## Offline course demo

Install the debug app on a clean disposable device. Enable airplane mode and turn off Wi-Fi
before first launch. Keep the app offline for the walkthrough and restart.

1. **Profiles and editing.** Create and select A. Its Set List opens directly. Create a
   set and three text cards. Edit one of them. Check a blank required field in the editor.
   Create and select B. Check that its Set List and Back do not expose A's data. Reselect A.
2. **Study and summary.** Open A's set. Reveal and rate one card Remembered and one Forgot.
   Confirm exit before rating the third. Verify 2 reviewed, 1 remembered, 1 forgot and 50%.
   Select Done. The same Set Detail opens with both saved next review dates and one eligible
   card. If both new cards were rated on the same local day, both are due tomorrow.
3. **Interruption and restart.** Start the remaining card and reveal it. Stop and relaunch
   the process before rating. Verify A restores to Set List, its set still has three cards
   and two saved next dates, and the third card is still eligible. Start a fresh queue and
   confirm exit before grading. Check that the summary has zero reviews and no percentage.
4. **Deletion.** Create a second disposable set with a card. Cancel deletion once and
   verify the set remains. Confirm deletion of that set and verify the original set remains.
   The Room suite verifies removal of the deleted cards and their logs.
5. **Accessibility and rotation.** Spot-check the editor, study and summary controls in
   portrait and landscape at both 100% and 130% font scale. Verify labels, both text-and-symbol
   ratings, scrolling, no clipping, 48 × 48 dp touch targets and rendered contrast of at
   least 4.5:1 for normal text and 3:1 for large text. Rotate with a draft and after answer
   reveal, then verify draft, card position, visible face and available controls. Complete
   the create → reveal → grade → Done path on the demo device.

Use the test suite for fixed dates, failure injection, duplicate commits and storage-level
cascades. The demo uses the actual device day. See the [UI acceptance](../../docs/6-ui-design/ui-description.md#9-accessibility-and-verification).

## v3.0 verification record

### Phase 2 v3.0 checkpoint

**Passed and approved on 2026-11-28.** Revalidation covered T006, T009, T011–T013,
T016–T018 and T020–T023. Phase 2 (T006–T023) is complete.

| Check | Result |
| --- | --- |
| Room v1 export | Four tables: `profiles`, `study_sets`, `flashcards` and `review_logs`. The exported schema matches the data model, with `identityHash = f4090c945947e676cea4597750c8defe`. |
| Debug build | `./gradlew :app:assembleDebug :app:testDebugUnitTest` succeeded and KSP regenerated the schema. |
| JVM unit tests | 15/15 passed, 0 skipped. |
| Instrumentation tests | `./gradlew :app:connectedDebugAndroidTest` succeeded on WayDroid x86_64, Android 13 (API 33). 23/23 passed, 0 skipped. |

Record later phase checkpoints here as they are verified. Final acceptance includes the
offline demo results for SC-01–SC-06 with orientation, font scale, measurements and any defects.

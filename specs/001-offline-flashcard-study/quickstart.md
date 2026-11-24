# Quickstart — FlashcardApp P1 validation

Use this guide as each feature becomes available and run the full sequence before the demo.

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

1. Open the repository root (the directory containing `settings.gradle.kts`, `gradlew` and
   `app/`). Install SDK Platform 37.0 (`platforms;android-37.0`), Build Tools 36.0.0 and Platform Tools through SDK Manager. Configure
   the SDK location in Android Studio or with `ANDROID_HOME`.
2. Use the checked-in Gradle 9.5.0 wrapper and provide the JDK 25 requested by
   `gradle/gradle-daemon-jvm.properties`, locally or via configured toolchain provisioning.
   The app module's Java 11 compile target is separate from the Gradle daemon JVM.
   Allow network access on the build machine for first-time downloads.
3. Connect one USB-debuggable course device or start one phone emulator. Use a disposable
   emulator for first-launch and deletion scenarios. Check the supported floor on an API 24
   emulator and the current UI on the course device or an API 37 emulator. Log the actual
   Android version used for the demo.

Record checkpoint evidence after each phase. The Phase 1 record above is complete.

| Phase | Required evidence |
| --- | --- |
| 1 · Setup (T001–T005) | Configure dependencies and test host, inspect classpaths, build and install the debug starter, confirm the device reports API 24 and launch MainActivity. Record the build and launch result. |
| 2 · Foundation (T013, T019–T023) | Generate and inspect the v1 Room schema with KSP. Verify the concrete Application, AppContainer and ViewModel factory wiring and run the foundation suites. Story-specific ViewModel registrations follow their screens. |
| 3 · Profiles (T024–T029) | Verify selection, Home counts, cancellation of old UI observations and Back after switching. |
| 4 · Library (T030–T037) | Run the editor and library suites, including resume with a new local date. |
| 5 · Free Study (T038–T041) | Verify browsing and compare learning records before and after practice. |
| 6 · Graded Review (T042–T047) | Exercise both entry points, empty queues, saving, retry, Exit/Back and committed summaries. |
| 7 · Integration (T048–T051) | On API 24, exercise the implemented calendar path at a known local date. For a late stage-1 Remembered on November 24, verify stage 2 and November 27 after reopen. Record the device/API and result separately from the presentation-runtime checks. |

Run from the repository root. On Windows use `gradlew.bat` in place of `./gradlew`.

```sh
adb devices -l
adb shell getprop ro.build.version.sdk
./gradlew --version
./gradlew :app:assembleDebug :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

Expect a target listed as `device`, a debug APK, and passing feature test cases. Inspect
`app/build/reports/tests/` for local results and `app/build/reports/androidTests/connected/`
for instrumentation results. Check the report's executed cases against the matrix below.
A passing template example only verifies the starter project. [Android test commands](https://developer.android.com/studio/test/command-line)

## Automated acceptance matrix

Use fixed local dates and supplied test profiles. Prefer JVM tests for rules, Android Room
tests with private test databases for storage guarantees, and Compose instrumentation for
visible behavior. Use an on-disk database for close/reopen persistence cases. Test fixtures
and injected failures belong in tests. [Room database testing](https://developer.android.com/training/data-storage/room/testing-db)

| Level and story | Exercise and expected result | Acceptance references |
| --- | --- | --- |
| JVM · US1 | Exercise Remembered at stages 0–6 and Forgot at every stage. Check all six intervals, the 60-day cap, tomorrow after Forgot, month/year rollover, leap day and a DST transition. A late stage-1 review saved on Nov 24 is due Nov 27. Changing zone changes the comparison date and leaves stored dates intact. Expect no percentage with zero reviews, 67% for two of three remembered and 50% for one of two. | FR-08, FR-09, FR-11 · NFR-06 · BR-11–BR-13 · SC-03, SC-04 |
| JVM + editor UI · US3, US4 | Trim required fields. Check blank and whitespace-only values, 1/80/81 for group/set names, and 1/2,000/2,001 per card face. For both group and set descriptions, save and reopen padded text with a trimmed length of 500, reject 501 after trimming, and clear an existing description with whitespace only. Expect trimmed text or null in storage. Accept duplicate card content and group/set names. Test Save/Cancel, named confirmation, discard confirmation and draft retention on failure. | FR-01, FR-03–FR-05 · NFR-03, NFR-08 · BR-01, BR-03, BR-05 · UI-01, UI-05, UI-07 · SC-01, SC-06 |
| Room + selection persistence · US4 | Create A/B with identically named items. Start an A write, select B and let it finish for A. A new B lookup or write using A’s item ID must find no matching record. Verify B’s records are intact and A alone can contain the started write. Reopen the database and preferences. Test valid selection, missing/deleted ID with one survivor, and zero profiles. While awaiting explicit selection, expect no active profile or visible library. Confirm profile deletion removes only owned content and reviews. | FR-01, FR-02, FR-10, FR-12 · NFR-02, NFR-07 · BR-01, BR-02, BR-06 · UI-01, UI-02, UI-11 · SC-01 |
| Compose · Phase 3 US4 (T024) | Use empty storage for onboarding. For switching, seed A and B through Phase 2 repositories with identically named sets and different new/due counts. Select each profile, check its Home name and counts, then use Back in B and verify A’s Home/Profile cannot reopen. This provides partial SC-01 evidence. Phase 7 T049 exercises real stale Set Detail and review routes and isolation of a write already started for A. | FR-01, FR-02, FR-10, FR-12 · UI-01, UI-02, UI-11 · SC-01 |
| Compose · Phase 4 US3 (T030) | Switch between two profiles with identically named sets and check the active display name on both Library tabs. For Library and Set Detail separately, seed a single reviewed card due tomorrow. Background the screen, supply the next local day on resume without writing to Room. Expect eligible count 0 → 1 and the original saved due date. | FR-02, FR-10 · NFR-03 · UI-03, UI-06 |
| Room · US3 | Create, read, rename/edit, move and delete items. Reject mixed-owner relationships. Moving a set or editing a card preserves schedules and reviews. Group deletion leaves sets in Ungrouped. Set/card deletion removes exactly the corresponding descendants and reviews. Check both profiles and reopen the file. | FR-02–FR-05, FR-12 · NFR-02 · BR-03–BR-07 · UI-03–UI-07 · SC-01, SC-05 |
| JVM + Room · US1 | Seed due, overdue, new, future and foreign cards in two sets with tied dates/creation times. Compare profile and set counts and queues. Reviewed eligible cards precede new cards, with ID tie-breaks. Snapshot excludes cards added later, skips deleted cards, excludes a just-forgotten card and respects date changes on foreground return. | FR-02, FR-08–FR-10 · BR-02, BR-10–BR-12 · UI-02, UI-06, UI-09 · SC-03 |
| Room + ViewModel · US1 | Fail a real transaction after its first write using a test-only fault, then inspect both tables. Neither change may persist. Retry successfully and submit competing taps for the same session/card. Expect exactly one log and one schedule update for the same session/card even if the taps conflict. Reopen the file and start from current eligibility. The committed grade and date persist. Verify a save error keeps the current card, and a successful retry advances once. | FR-08, FR-09, FR-11, FR-12 · NFR-02, NFR-08 · BR-11–BR-13 · UI-09, UI-10 · SC-04 |
| Compose + Room · US2 | Browse both sides of new, due and future cards in preview and Free Study. Verify default Front, reset on card change, position, repeat flips, stable order and previous/next boundaries for one and many cards. Compare history, stages, due dates and eligible counts before/after. | FR-07, FR-10 · BR-09 · UI-06, UI-08 · SC-02 |
| Compose · Phase 6 US1 (T043) | Force review recomposition before a grade tap and during its pending save. Observe zero repository grade commands before the tap, exactly one through the pending recompositions, and exactly one advancement on commit. Try Exit and system Back during the held save. Neither navigates or opens a dialog. After commit navigation works on the next card or summary. After rollback the revealed card offers Retry and early exit works. With an empty queue at either entry point, stay at the origin and show that nothing is due. | FR-09, FR-11, FR-12 · NFR-08 · UI-09, UI-10 · SC-04 |
| Compose navigation · all four stories | Exercise onboarding, Home, Library tabs, groups, set sheets, editors, mode entry and Back/Done. Grade becomes available after first reveal and stays available if flipped back. Check empty, loading, saving, error/retry and cancel/confirm exit states. Rotate with a draft and during review. While the first card of a three-card snapshot is open, delete the second through the fixture. Rate the first and verify the second is skipped. Expect “3 of 3” and one reviewed on the third, then two reviewed after its grade. Check reveal state, summary, Set Detail date and navigation reset after profile switch. | FR-01–FR-05, FR-07–FR-12 · NFR-03–NFR-05, NFR-08 · BR-01–BR-07, BR-09–BR-13 · UI-01–UI-11 · SC-01–SC-06 |

For NFR-06, also review the changed dependencies and confirm screen code has no direct DAO
access. For NFR-07, inspect the merged manifest and backup settings for local-only handling of
learner data and unnecessary network/storage permissions. NFR-09 requires build, JVM, Room
and representative Compose results together. P2 and P3 identifiers remain deferred in the
[clarified specification](spec.md).

## Offline course demo

Use a fresh disposable test installation. Install the debug app, enable airplane mode and
ensure Wi-Fi is off **before first launch**. USB or emulator debugging stays available for
the restart check. Keep the device offline through the walkthrough.

```sh
./gradlew :app:installDebug
adb shell am start -n com.example.flashcardapp/.MainActivity
```

1. **US4 and US3 · SC-01.** Create and select profile A. Create a group, a set in it and
   three cards with distinguishable fronts/backs. Rename a set, open each face and return
   to Library. Create and select B. Its Home and Library show empty counts and Back cannot
   reopen A. Reselect A and check the three saved cards.
2. **US1 · SC-03 and SC-04.** From Home, start Graded Review. Reveal card one, select
   Remembered and wait for the next front. Reveal card two, select Forgot, then confirm
   early exit before rating card three. Expect 2 reviewed, 1 remembered, 1 forgot and 50%.
   Done returns to Home. In Set Detail each rated row shows tomorrow's local date (both
   cards were new), and card three has no scheduled date. One card is eligible.
3. **US2 · SC-02.** In preview and Free Study, flip all three cards, including the two due
   tomorrow. Previous/Next stop at the ends. Exit and compare counts and dates with step 2.
   Keep the device date fixed during this comparison.
4. **Interruption and restart · SC-01, SC-03, SC-04.** Start a set-scoped review of the
   remaining card and reveal it. Before rating, stop and relaunch the process with the
   commands below. Expect A restored, the first two committed grades intact and the third
   card still eligible. Start a fresh session, exit before grading and check zero reviewed
   with no percentage. Done returns to its originating scope.

   ```sh
   adb shell am force-stop com.example.flashcardapp
   adb shell am start -n com.example.flashcardapp/.MainActivity
   ```

5. **Adaptive UI · SC-06.** Run four independent passes in a separate disposable fixture, starting with an empty set each time. Use portrait at 100%, portrait at 130%, landscape at 100% and landscape at 130%. Keep each setting for the entire pass.
   - Create and save a two-sided card with long text. Start Graded Review from that set, flip the card and identify both ✗ Forgot and ✓ Remembered by label and symbol. Submit one grade, verify one committed review on the summary and tap Done. Verify return to the originating set and its empty review workload.
   - Record the full journey for each setting. Check accessible names, gesture alternatives, scrolling, clipping and reachability. Measure touch targets in both dimensions (minimum 48 × 48 dp) and text contrast on actual surfaces (4.5:1 normal, 3:1 large as defined in NFR-04).
   - Separately, return to profile A from step 4. Rotate while editing its remaining card and verify the draft survives. Rotate after reveal during review and resize where supported. Verify position, face and enabled actions survive, submit the last grade and check the empty review state. Compare Library, group, preview, sheet and landscape review against REF-01–REF-07.
   [UI description and reference index](../../docs/6-ui-design/ui-description.md)
6. **Deletion · SC-05 and SC-01.** Cancel group deletion once, then confirm it and verify
   the set appears in Ungrouped with all three cards and dates intact. Confirm set deletion
   and verify its cards disappear and counts update. Delete A. With only B left, the profile
   selector appears and B's Home requires an explicit selection. Delete B on this disposable
   installation and verify profile creation opens.

For the demo record the device/API, debug build result, automated pass/fail counts and any
observed deviation from SC-01–SC-06. The scheduler and transaction tests supply the evidence
for future dates, duplicate submissions and failed writes without changing the demo phone's
clock or adding controls to the app. [Scheduling and persistence design](data-model.md)

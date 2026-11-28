# Research decisions — Offline flashcard study

**Baseline**: v3.0. This record explains the selected course patterns and tooling. The
[specification](spec.md) defines behavior, and the [quickstart](quickstart.md) records
historical verification and pending release evidence.

## Toolchain and runtime

Keep the single Kotlin DSL `:app` module, AGP 9.3.3 built-in Kotlin, Compose plugin 2.2.10,
Gradle wrapper 9.5.0 and daemon JDK 25. Android bytecode targets Java 11. The app supports
API 24 and compiles and targets SDK 37. The Compose BOM is 2026.02.01. Exact pins remain
in the [version catalog](../../gradle/libs.versions.toml) and
[app build configuration](../../app/build.gradle.kts).

The starter launched on API 24, and the Phase 2 tests ran on WayDroid Android 13/API 33.
Record the actual demo device and run the final minimum-API calendar smoke with the
implemented study path.

### Minimal catalog additions for implementation

| Existing addition | Use in v3.0 |
| --- | --- |
| Navigation Compose 2.10.2 | One NavHost with simple internal route IDs. Cupcake demonstrates navigation callbacks and an explicit back stack. |
| Lifecycle ViewModel/Runtime Compose 2.11.0 | Screen-owned StateFlow, lifecycle collection and an AppContainer-backed standard factory. |
| Room 2.8.5 and KSP2 2.3.14 | Compile the DAOs and export the four-table v1 schema. Inventory demonstrates Room and direct DAO injection. |
| Preferences DataStore 1.3.0 | Keep the selected profile ID. Dessert Release demonstrates a repository backed by one Preferences DataStore. Validate against Room and offer retry on read failure. |
| Android/test coroutines 1.10.2, JUnit and Compose test host | Model Flow observations and suspend writes and cover focused rule, storage and screen behavior. |
| Core library desugaring 2.1.5 | Run explicit local calendar calculations on API 24–25 using java.time. |

These additions were resolved in Phase 1. Keep the existing AndroidX test setup and omit
new libraries for routing, dependency injection and state management.

## Design decisions

| Decision | Basis |
| --- | --- |
| Open Set List directly after selecting or restoring a profile. | The approved flow starts with the learner's sets. Cupcake's one NavHost and screen callbacks fit the six-screen flow. Scope destinations to the validated profile and clear the old stack on switch. |
| Keep an Application-owned AppContainer and three simple repositories. | Mars Photos demonstrates manual application wiring. Inventory demonstrates direct DAO-backed repositories and a standard ViewModel factory. Retain these patterns and keep Room out of composables. |
| Require a set ID from queue creation through summary. | The same profile and set pair identifies card lookup, grade writes and committed totals. The repository and DAO require both arguments, including in `getSessionCounts`, so the study path cannot widen its scope. |
| Replace the pre-release v1 schema export and use a clean development database. | Four tables cover the approved product behavior. The constitution authorizes resetting the unreleased development baseline without migration. A future schema change affecting released data requires a preserving migration. |
| Keep the transaction, key and date policy. | The session/card primary key protects against repeated grades. One transaction protects log and schedule together. A pure date rule uses the local day of the actual rating, and the study ViewModel retains only the active session. |
| Use representative tests and one offline walkthrough. | Keep rule and storage cases at their data boundaries. Use Compose for profile navigation, invalid input, reveal and return from summary. Check orientation and font size on the device. |

Use the [Android Basics with Compose course](https://developer.android.com/courses/android-basics-compose/course)
as the primary reference. Keep local copies of Inventory, Mars Photos, Cupcake and Dessert
Release read-only. Use the [approved UI design](../../docs/6-ui-design/ui-description.md)
for the current visual references.

## Implementation status

The previous development build and test evidence remains in
[Phase 1 verification](quickstart.md#phase-1-verification) and the
[Phase 2 foundation checkpoint](quickstart.md#phase-2-foundation-checkpoint). The
[tracker](current.md) records the tasks that require updated code and rerun evidence.

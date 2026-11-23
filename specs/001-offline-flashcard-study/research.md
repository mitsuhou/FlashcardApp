# Research decisions — Offline flashcard study

The [clarified specification](spec.md) settles product behavior. The [constitution](../../.specify/memory/constitution.md) governs the design. This record distinguishes repository evidence, selected additions and checks that require the future implementation.

## Toolchain and runtime

| Repository evidence | Decision and rationale |
| --- | --- |
| [Settings](../../settings.gradle.kts), [root build](../../build.gradle.kts) and [app build](../../app/build.gradle.kts) | Keep the existing single `:app` module, Kotlin DSL, built-in Kotlin in AGP 9.3.3, Compose plugin 2.2.10 and Java 11 compile target. Adding the legacy Kotlin Android plugin would compete with AGP's Kotlin support. |
| [Gradle wrapper](../../gradle/wrapper/gradle-wrapper.properties) and [daemon criteria](../../gradle/gradle-daemon-jvm.properties) | Keep Gradle 9.5.0 and the configured JDK 25 daemon. The host has JDK 25 available even though its shell defaults to JDK 21. Keep Google Maven, Maven Central and the existing plugin repositories. |
| App SDK configuration and available runtime | Keep minSdk 24, compileSdk 37 and targetSdk 37. The local SDK has Platform 37.0 and the connected WayDroid device reports API 33. The course handset is unspecified. The verified API 24 floor preserves the template's installation range. |
| [Version catalog](../../gradle/libs.versions.toml) and existing test dependencies | Retain Compose BOM 2026.02.01, BOM-managed UI, tooling and Material 3, Activity Compose 1.13.0, Core KTX 1.19.1 and Lifecycle Runtime KTX 2.11.0. Keep JUnit 4.13.2, AndroidX Test Ext JUnit 1.3.0, Espresso 3.7.0 and BOM-managed Compose JUnit4 tests. |

AGP 9.3's Gradle and Build Tools requirements are compatible with the wrapper and installed Build Tools 36.0.0. Keep the build JVM and Android bytecode settings separate. [AGP compatibility](https://developer.android.com/build/releases/agp-9-3-0-release-notes) · [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)

### Minimal catalog additions for implementation

Phase 1 resolved these dependencies, built the starter and launched it on API 24.
See [Phase 1 verification](quickstart.md#phase-1-verification) for the observed classpaths.
T013 checks the exported schema when the database is implemented.

| Addition | Planned pin and use | Alternative considered |
| --- | --- | --- |
| Navigation Compose | `androidx.navigation:navigation-compose:2.10.2`. An internal graph with simple route names and scalar arguments covers Back and scope navigation. [Release](https://developer.android.com/jetpack/androidx/releases/navigation) | Typed route serialization adds a plugin for a handful of internal IDs. |
| Screen state | `androidx.lifecycle:lifecycle-viewmodel-compose` and `lifecycle-runtime-compose` at the catalog's 2.11.0. Use an AppContainer-backed factory, ViewModel scope and lifecycle-aware collection. [Lifecycle releases](https://developer.android.com/jetpack/androidx/releases/lifecycle) | A DI framework and another state library duplicate course-level facilities. |
| Persistence | `androidx.room:room-runtime` and `room-compiler` 2.8.5 via `com.google.devtools.ksp` 2.3.14 (KSP2). Use platform SQLite, exported schemas in `app/schemas/` and the same Room version for its schema Gradle plugin. [Room](https://developer.android.com/jetpack/androidx/releases/room) · [KSP](https://github.com/google/ksp/releases/tag/2.3.14) | Raw SQLite adds manual mapping and query checking. A separate database per profile adds file lifecycle and migration complexity. |
| Selection preference | `androidx.datastore:datastore-preferences:1.3.0` for one active ID. Validate it against Room. [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore) | A second profile catalog in preferences could disagree with Room. |
| Asynchronous work and tests | `org.jetbrains.kotlinx:kotlinx-coroutines-android` and test-scoped `kotlinx-coroutines-test`, planned at 1.10.2. Use the same resolved version for runtime and tests. [Coroutines release](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.10.2) | Additional reactive stacks have no P1 consumer. |
| Calendar arithmetic at API 24–25 | Enable core library desugaring with `com.android.tools:desugar_jdk_libs:2.1.5`. Use the desugared java.time APIs on device and the same date policy on the JVM. [Android desugaring](https://developer.android.com/studio/write/java8-support) | Raising the minimum to 26 would discard existing device coverage. Millisecond offsets would mis-handle local day boundaries. |

## Product decisions with architectural consequences

| Decision | Rationale | Alternative considered |
| --- | --- | --- |
| One AppContainer, three repositories and pure Kotlin functions | Screen ViewModels coordinate events and expose immutable state. Pass DAOs directly to repositories. Use ordinary Flow observations, suspend operations and one standard ViewModel factory. | A use-case class per CRUD action or a domain module would add indirection to simple repository calls. |
| Ownership by profileId | DAO queries use the originating profileId and verify parent ownership. Switching clears the old stack and cancels its UI observations. | A started write can finish for its owner. Filtering a loaded list in Compose would leave detail and write operations unprotected. |
| Relational ownership and one grade transaction | A composite owner/group relationship prevents cross-profile moves. The session/card primary key and one Room transaction protect both writes across concurrent submissions and restart. Details and failure cases are in the [data model](data-model.md). | A disabled rating button alone cannot enforce uniqueness in storage. |
| Freeze only the review queue | A session UUID, snapshot and current face live in a ViewModel. Saved logs and dates are durable. Rotation retains progress, while process death permits a fresh queue. | Persisting an entire resumable session would expand FR-12. |
| Use the confirmed binary ladder | Use a pure stage/grade/date function with an explicit local date. Read the current day on resume and review start and at grading time. A late grade starts from its actual local day. | SM-2 and FSRS require inputs or policy beyond BR-12. |
| Render the approved Android adaptation of all seven references | Retain the card hierarchy, dark palette and two recall controls, and use the confirmed Set Detail list for saved dates. Native semantics and explicit controls support touch and assistive input. | Copying the reference toolbars would add unapproved functions. |
| Keep learner records on device | Phase 1 disabled app backup and explicitly excluded the database domain and the DataStore files from applicable legacy backup and modern cloud/device-transfer rules. The Phase 1 verification records the merged release policy. [Android backup rules](https://developer.android.com/identity/data/autobackup) | Relying on the absence of an Internet permission would leave OS-managed backup eligible. |

## Reference discrepancies and disposition

- UI Description spells the folder `06-ui-design`. The actual directory is [docs/6-ui-design](../../docs/6-ui-design/). All seven filenames in the reference index exist there.
- The [2026-11-21 clarifications](spec.md#session-2026-11-21) approve the profile fallback, intervals, field limits, deletion and interruption behavior, and Set Detail date placement. The business rules and technical baseline reflect those decisions.
- The overview's optional Week 2 images and controls pictured in the commercial app do not enter this plan. The scope is all four P1 stories. Basic rotation and resizing are mandatory. Enhanced large-screen layouts remain deferred.
- Phase 1 applied the documented dark palette and verified its contrast. The existing greeting and example tests are scaffolding.
- Spec Kit reports `001-offline-flashcard-study` as the feature identifier from the active feature configuration. Git reports the checkout branch as `main`.

The [Android Basics with Compose course](https://developer.android.com/courses/android-basics-compose/course)
is the primary implementation reference. The local Inventory sample constructs
OfflineItemsRepository with ItemDao and provides ViewModels through a standard factory.
Use the [Inventory Room codelab](https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room)
for storage and the [Mars Photos manual DI codelab](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository)
for Application and AppContainer wiring. Cupcake illustrates a single NavHost and navigation
callbacks. Dessert Release illustrates using a single Preferences DataStore. FlashcardApp
treats profile-selection read failures as retryable and preserves existing Room data on schema changes.

Phase 1 verified the build and starter on API 24. Phase 2 owns schema export and manual wiring.
Phase 7 exercises calendar arithmetic on API 24.

Confirm the actual presentation device before the demo.

# Research decisions — Offline flashcard study

The [clarified specification](spec.md) settles product behavior. The [constitution](../../.specify/memory/constitution.md) governs the design. This record distinguishes repository evidence, selected additions and checks that require the future implementation.

## Toolchain and runtime

| Repository evidence | Decision and rationale |
| --- | --- |
| [Settings](../../settings.gradle.kts), [root build](../../build.gradle.kts) and [app build](../../app/build.gradle.kts) | Keep the existing single `:app` module, Kotlin DSL, built-in Kotlin in AGP 9.3.3, Compose plugin 2.2.10 and Java 11 compile target. Adding the legacy Kotlin Android plugin would compete with AGP's Kotlin support. |
| [Gradle wrapper](../../gradle/wrapper/gradle-wrapper.properties) and [daemon criteria](../../gradle/gradle-daemon-jvm.properties) | Keep Gradle 9.5.0 and the configured JDK 25 daemon. The host has JDK 25 available even though its shell defaults to JDK 21. Keep Google Maven, Maven Central and the existing plugin repositories. |
| App SDK configuration and available runtime | Keep minSdk 24, compileSdk 37 and targetSdk 37. The local SDK has Platform 37.0 and the connected WayDroid device reports API 33. The course handset is unspecified. API 26 in Tech Stacks is a suggestion. Retaining API 24 supports the available device and preserves the template's installation range. |
| [Version catalog](../../gradle/libs.versions.toml) and existing test dependencies | Retain Compose BOM 2026.02.01, BOM-managed UI, tooling and Material 3, Activity Compose 1.13.0, Core KTX 1.19.1 and Lifecycle Runtime KTX 2.11.0. Keep JUnit 4.13.2, AndroidX Test Ext JUnit 1.3.0, Espresso 3.7.0 and BOM-managed Compose JUnit4 tests. |

AGP 9.3's Gradle and Build Tools requirements are compatible with the wrapper and installed Build Tools 36.0.0. Keep the build JVM and Android bytecode settings separate. [AGP compatibility](https://developer.android.com/build/releases/agp-9-3-0-release-notes) · [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)

### Minimal catalog additions for implementation

These are selected coordinates for Phase 1. Preserve existing catalog aliases. Verify the resolved classpaths, compiler processing, schema export and starter build before layering features on top. A full build of these future additions has not been run.

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
| One AppContainer, three repositories and a small pure scheduling package | Screen ViewModels coordinate events and expose immutable state. The same ownership checks apply to navigation IDs, counts and background completions. | A use-case class per CRUD action or a domain module would add indirection to simple repository calls. |
| Profile selection has a lifetime | Scope an active workspace to a profile ID and selection generation. Serialize selection changes and writes, validate the originating scope, and discard stale emissions. This protects A → B → A as well as A → B. | Filtering already-loaded content in the UI leaves stale writes possible. |
| Relational ownership and one grade transaction | A composite owner/group relationship prevents cross-profile moves. A durable session/card key works across retries. Persist grade and schedule together, then acknowledge. Details and failure cases are in the [data model](data-model.md). | A disabled button alone cannot handle a commit whose acknowledgement is lost. |
| Freeze only the review queue | A session UUID, snapshot and current face live in a ViewModel. Saved logs and dates are durable. Rotation retains progress, while process death permits a fresh queue. | Persisting an entire resumable session would expand FR-12. |
| Use the confirmed binary ladder | Use a pure stage/grade/date function and an injectable clock with the current device zone at the repository boundary. A late grade starts from its actual local day. | SM-2 and FSRS require inputs or policy beyond BR-12. |
| Render the approved Android adaptation of all seven references | Retain the card hierarchy, dark palette and two recall controls, and use the confirmed Set Detail list for saved dates. Native semantics and explicit controls support touch and assistive input. | Copying the reference toolbars would add unapproved functions. |
| Keep learner records on device | The starter enables Auto Backup and supplies sample-only backup rules. In Phase 1 disable app backup and explicitly exclude the database domain and the DataStore files from applicable legacy backup and modern cloud/device-transfer rules. Verify merged release policy. [Android backup rules](https://developer.android.com/identity/data/autobackup) | Relying on the absence of an Internet permission would leave OS-managed backup eligible. |

## Reference discrepancies and disposition

- UI Description spells the folder `06-ui-design`. The actual directory is [docs/6-ui-design](../../docs/6-ui-design/). All seven filenames in the reference index exist there.
- Earlier documents label the ladder and deletion rules as proposed. The five decisions in the specification's dated Clarifications section are approved. That section also settles the invalid-selection fallback and next-date placement. The original checklist notes describe the earlier draft.
- The overview's optional Week 2 images and controls pictured in the commercial app do not enter this plan. The scope is all four P1 stories. Basic rotation and resizing are mandatory. Enhanced large-screen layouts remain deferred.
- The starter's theme follows system appearance and enables dynamic color. Apply the documented dark palette by default during the UI setup. The existing greeting and example tests are scaffolding.
- Spec Kit reports `001-offline-flashcard-study` as the feature identifier from the active feature configuration. Git reports the checkout branch as `main`.

The minimum runtime and technical choices are settled for planning. Phase 1 will verify resolved dependencies and the minimum-runtime smoke test. The API 33 WayDroid target can support development tests. Confirm the actual presentation device before the demo.

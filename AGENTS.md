# FlashcardApp — Agent Instructions

## Project Context

This is a small educational Android project using
Spec-Driven Development with GitHub Spec Kit.

## Learning Reference

Follow the entire Android Basics with Compose course:

https://developer.android.com/courses/android-basics-compose/course

Use the relevant pathways, codelabs and sample apps.

## Reference-Based Implementation

- Use the entire Android Basics with Compose course as
  the primary learning reference.
- Before each task, inspect relevant sample projects in
  `/home/mitsuhou/AndroidStudioProjects/`.
- Prefer Inventory for Room and Repository patterns.
- Prefer Mars Photos for MVVM and manual dependency injection.
- Use Cupcake and other course projects where relevant.
- Follow the actual coding style of the reference projects.
- Keep all reference projects read-only.
- Explain the selected reference files before coding.
- Obtain approval before using advanced techniques not
  covered by the course.
- Implement one task at a time and wait for approval.
- Keep all implementation reports and explanations in English.

## Coding Standards

- Write simple, beginner-friendly Kotlin.
- The coding style should be stable between files.
- Prefer explicit `if` / `else`, `when`, and clear local variable names. Avoid
  overusing `takeIf`, `takeUnless`, `let`, `run`, `apply`, `also`, `with`, and
  infix `to` for a `Pair`.
- Keep Compose screen content stateless with data and callback parameters. Keep
  UI-only state such as text drafts, dialog visibility, and delete confirmation
  in the composable with `rememberSaveable`, following Inventory's
  `ItemDetailsScreen.kt`. Use inline forms and actions when they meet the
  requirement.
- Keep ViewModel UI state small. Use a sealed interface for mutually exclusive
  screen states such as Loading, Error, and Ready, following Mars Photos.
- Use course-level coroutines. Observe data with `Flow` and use `stateIn` when
  useful. Launch writes directly with `viewModelScope.launch`. Avoid manually
  managed `Job`, `suspend () -> Boolean` wrappers, `flatMapLatest`,
  `MutableSharedFlow`, and `channelFlow` unless necessary.
- Give every screen Compose `@Preview` functions for its main states using
  `FlashcardAppTheme`. For text, labels, and `TextButton` on dark surfaces,
  use `MaterialTheme.colorScheme.secondary` or a text color that reaches at
  least 4.5:1 under NFR-04. Reserve primary Indigo for uses such as a filled
  button background and use an approved foreground on Navy and CardSurface.
- Follow the architecture and patterns taught in the course.
- Use simple MVVM, Room, Repository and manual DI.
- Prefer explicit functions and direct DAO injection.
- Avoid overengineering and unnecessary abstractions.
- Do not introduce techniques outside the course
  without my approval.
- Do not move complexity into additional helper classes.
- If requirements conflict with simplicity,
  discuss alternatives before implementation.
- Never silently change approved requirements.

## Implementation Workflow

- Read current.md and the relevant SDD documents first.
- Implement exactly one task at a time.
- For each new screen, first do one UI-only task with stateless Jetpack Compose
  content, `app/src/main/res/values/strings.xml`, and themed previews for its
  main states. Stop and get explicit user approval of the UI before starting
  the ViewModel and navigation task.
- After UI approval, implement the screen's ViewModel when needed, register it
  in `AppViewModelFactory.kt`, pass `createSavedStateHandle()` for route
  arguments following Inventory, and connect its route in
  `ui/navigation/FlashcardNavHost.kt`.
- T036 reserves both Review Summary UI and study navigation under one ID. Stop
  after its UI-only work and wait for explicit user approval before wiring.
  Complete T036 only after its wiring is verified and approved.
- Follow task order and dependencies.
- Explain the intended approach before writing code.
- Run relevant tests.
- Explain changed files and code in Vietnamese.
- Stop after each task and wait for my approval.
- Mark tasks complete only after verification and approval.
- Update current.md and tasks.md accordingly.
- Do not implement future tasks in advance.
- Do not commit or push without my approval.

## Phase Completion and Transition

- Determine the current phase from current.md and tasks.md.
- Implement only tasks belonging to the current phase.
- A phase is complete only when all its tasks are verified
  and approved by the user.
- Run the required phase checkpoint tests.
- Report completed tasks, changed files, test results
  and remaining issues.
- Update current.md, tasks.md and relevant evidence
  in quickstart.md.
- Stop when the current phase is complete.
- Never start the next phase without explicit approval.

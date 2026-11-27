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
- Add Compose `@Preview` functions for each new UI screen and reusable
  UI component. Show representative states in the app theme using sample
  data and no-op event callbacks. Keep previews independent of real
  ViewModels, Room, DataStore and Navigation.
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

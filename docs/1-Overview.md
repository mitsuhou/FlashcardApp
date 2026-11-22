# Project Overview — Flashcards: Spaced Repetition Learning

| Item | Description |
| --- | --- |
| Version | 1.1 — planning baseline for review |
| Updated | 2026-11-21 |
| Course | Introduction to Mobile Programming |
| Platform | Native Android |
| Duration | 3 weeks |

## 1. Product concept

Flashcards is a local-first Android application that helps students organize study material, recall answers actively, and revisit cards at scheduled intervals. It combines the simplicity of two-sided physical flashcards with self-assessed recall and spaced repetition. It is inspired by Quizlet and Anki, not a clone.

## 2. Problem and objectives

- Learners need a convenient way to create reusable question–answer material, organize it by subject, and review it without an Internet connection.
- The application should minimize distractions, make daily review easy to start, and retain learning data between launches.
- Users should be able to personalize their workspace with independent local profiles and organized sets; visual customization is deliberately limited to avoid unnecessary complexity.
- The development team should demonstrate maintainable Kotlin code, clean separation of concerns, lifecycle-aware state management, persistent storage, and independently testable business rules from Android Basics with Compose.

## 3. Target users

Students, self-directed learners and people studying vocabulary, definitions, facts or other short question–answer material. Multiple people may use the same installation through separate local profiles. Profiles are data partitions, not password-protected accounts.

## 4. Scope

### MVP (required)

- Local profiles and per-profile library isolation.
- Groups containing flashcard sets, with a default ungrouped library.
- Create, view, edit and delete sets and two-sided text flashcards.
- Free study (ungraded) and graded review with Remembered / Forgot ratings.
- Local spaced-repetition scheduling, due-card selection and session summaries.
- Data persistence, input validation, empty states, deletion confirmation and phone-friendly UI.

### Extended scope (only after MVP stability)

- Optional images on either side of a card (P2), subject to validation and safe local storage.
- Profile-level review coverage bar and basic aggregate statistics.
- Enhanced layouts for tablets/foldables. Basic resizing and orientation correctness are MVP.
- Optional recall countdown, visual workspace personalization beyond names and organization.

### Explicitly out of scope for v1.0

Online accounts, authentication, cloud synchronization, sharing marketplace, backend APIs, collaborative editing, AI card generation, push reminders and CSV import/export. Installation/build may require Internet for dependencies; normal app use must not.

## 5. Deliverables and milestones

| Week | Target |
| --- | --- |
| 1 | Confirm scope, generate/review Spec Kit files, establish local storage and profile/set/card CRUD. |
| 2 | Complete both study modes, scheduling, session summaries and unit tests; add card images if core flow is stable. |
| 3 | Verify isolation, persistence and UI; implement selected extensions if time permits; prepare demo and report. |

## 6. Success indicators

The demo can create two profiles with separate libraries, create at least one group/set and cards, study without grades, grade due cards, see next review dates, restart the app, and confirm that prior data survives in airplane mode. Build and relevant tests pass. Detailed requirements and acceptance criteria are in [requirements](2-Functional%20Requirements.md).

## 7. References and document status

- [Quizlet](https://quizlet.com/) and [Anki](https://apps.ankiweb.net/): product inspiration.
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course): course material and learning foundation.
- This file records product intent. The [specification clarifications of 2026-11-21](../specs/001-offline-flashcard-study/spec.md#session-2026-11-21) record the approved profile, field, deletion, scheduling and interruption decisions for the MVP. Do not treat this document as generated code instructions.

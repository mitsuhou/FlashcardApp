# Project Overview — Flashcards: Spaced Repetition Learning

| Item | Value |
| --- | --- |
| Scope baseline | 3.0 |
| Updated | 2026-11-28 |
| Course | Introduction to Mobile Programming |
| Platform | Native Android |
| Delivery | One developer, three weeks |

## 1. Product concept

FlashcardApp helps learners create two-sided text cards, test their recall and revisit cards
at scheduled intervals. All profiles, sets, cards and review records stay on the device.
Several learners can keep independent libraries on the same installation using local profiles.

## 2. MVP scope

The release follows one path: **Profile Selection/Management → Set List → Set Detail →
Graded Study → Review Summary**. Set and card management uses one shared editor screen.
A valid saved profile opens its Set List on launch. Done on Review Summary returns to the
set that started the session.

- Create, rename, select and confirm deletion of local profiles. Scope all library and study
  operations to the selected profile.
- Create, view, edit and delete study sets and their two-sided text cards. Validate input
  and confirm destructive actions.
- Study new or due cards from one set. Reveal the answer before rating Remembered or Forgot.
  Save each rating and its next review date together. Show committed counts and recall rate
  in Review Summary.
- Complete the flow offline from first launch through restart. Use Kotlin, Jetpack Compose,
  Room, Preferences DataStore, MVVM, unidirectional data flow and manual dependency injection.

## 3. Release boundaries

v1 excludes Group/Ungrouped organization and set moves, Free Study and flippable previews,
a separate Home screen and profile-wide review. Card images, aggregate coverage, countdown,
appearance personalization and enhanced tablet/foldable layouts are deferred beyond v1.
Online accounts, authentication, cloud sync, backend APIs, public sharing, collaboration,
AI generation, push reminders and CSV import/export are outside the release.

## 4. Deliverables and milestones

| Week | Target |
| --- | --- |
| 1 | Align the specifications with the solo MVP, establish the four-table development baseline and connect profile and set/card management. |
| 2 | Complete set-scoped Graded Study, scheduling and Review Summary. |
| 3 | Verify isolation, persistence and the core UI flow, then prepare the offline demo and report. |

## 5. Success indicators

In airplane mode, select a profile, create a set and text cards, study that set, see the
session summary and restart to verify cards and next review dates. A second profile shows only
its own sets. A debug build and focused JVM, Room and Compose checks provide release evidence.

## 6. References and document status

- [Functional requirements](2-Functional%20Requirements.md)
- [Business rules](4-Business-Rules.md)
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
- [Constitution v3.0.0](../.specify/memory/constitution.md)

The v3.0 product baseline supersedes the earlier scope. The feature specifications and Phase 2
implementation record still describe the previous baseline and will be reconciled in a later step.

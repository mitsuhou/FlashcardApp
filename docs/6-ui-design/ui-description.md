# UI Description — Flashcards: Spaced Repetition Learning

**Version:** 1.2 (screenshot-based design baseline)  
**Platform:** Native Android (Kotlin, Jetpack Compose, Material 3)  
**Design direction:** Quizlet-inspired, dark, minimal and focused on offline study.

## 1. Purpose and design boundaries

This document describes visual appearance, screen layout and interactions for the Flashcards application. It is an input for Spec Kit, not a pixel-perfect implementation contract. The supplied screenshots are the primary visual references and supersede earlier light-theme wireframes.

Preserve the companion functional requirements and business rules. Visual inspiration does not imply approval to implement every visible feature. Do not copy Quizlet branding, content, or iOS system bars; adapt the layout to native Android components and insets.

Place the original images under `docs/06-ui-design/references/` with these filenames:

| ID | Reference image | Design reference |
| --- | --- | --- |
| REF-01 | `startup_ui.png` | Library — Study Sets |
| REF-02 | `outside_group_ui.png` | Library — Groups/Folders |
| REF-03 | `inside_group_ui.png` | Group details and contained sets |
| REF-04 | `flashcard_front.png` | Set details — card front preview |
| REF-05 | `flashcard_end.png` | Set details — card back preview |
| REF-06 | `edit_a_set.png` | Set overflow action sheet |
| REF-07 | `grade_mode.png` | Landscape graded-review interface |

Names, images, avatars, dates, terms and advertisements in the references are illustrative, not required assets or seed data.

## 2. Global visual design

### 2.1 Theme and colors

Dark theme by default. These approximate colors are inspired by the reference screenshots; use semantic Material 3 color roles and verify contrast in implementation.

| Token | Value | Usage |
| --- | --- | --- |
| Background | #0D0D2F | Screen and navigation background |
| Card surface | #303A54 | Large flashcard face |
| Outline | #343E57 | Outlined containers and dividers |
| Primary | #454BF5 | Main action button |
| Selected accent | #A5ADFF | Active tab underline, focus indicator |
| On primary | #FFFFFF | Primary action labels |
| Primary text | #F5F5FC | Headings and flashcard content |
| Secondary text | #C3C6D6 | Counts and supporting labels |
| Muted text | #9BA4B9 | Inactive controls and metadata |
| Remembered | #20B88A | Check icon/accent |
| Forgot | #FF9A60 | Warm cross icon/accent |

Grade controls must have accessible labels and visible symbols, not depend on color alone. Check color contrast on the actual backgrounds.

### 2.2 Typography and components

- Native sans-serif typeface using Material 3 type scale; bold screen headings, semibold set titles, comfortably readable content. Suggested 24–32 sp screen headings, 16–20 sp set titles, 14–16 sp metadata, 24–32 sp short study-card text.
- Consistent 8 dp spacing grid and approximately 16–20 dp horizontal margins on compact screens.
- Library cards feature subtle outlines, dark background and round corners (12–16 dp). Flashcards feature a solid blue-gray surface with round corners (16–24 dp).
- Indigo filled primary buttons; outlined or tonal secondary buttons. Simple, accessible icons. Preserve Android system bars/insets and avoid recreating iOS chrome.

## 3. Application shell and navigation

Adapt the bottom navigation in the references to the local application:

| Destination | Purpose |
| --- | --- |
| Home | Daily review dashboard, new/due counts and Review now |
| Library | Study Sets and Groups |
| Profile | Local profile selection and management |

Library has a prominent + action opening New study set / New group. A centered + button inspired by the screenshots may be used instead of the top-right add button, but is an action rather than a separate destination; avoid unnecessary duplication.

On first launch without profiles, open profile creation/selection. After selecting a profile, Home is the review dashboard. Opening Library shows Study Sets by default (REF-01); the screenshot does not imply bypassing profile onboarding. The navigation bar remains available while browsing; hide it during focused study and editing where appropriate.

The active profile scopes all content. Switching profiles clears the old navigation stack and session state and cancels the old screen’s observations. Omit Solutions, Classes and other online-oriented navigation.

## 4. Library — Study Sets (REF-01)

**Reference:** `startup_ui.png`  
**Requirements:** FR-02, FR-04, FR-10.

### Layout

- Centered Library title and + action at top right.
- Horizontal tabs: **Study Sets** and **Groups** (Folders in the reference). Selected tab has a lavender underline; inactive labels use muted text.
- Scrollable list of rounded, outlined study-set cards. Each card shows bold set name, total card count and eligible/due count where useful. Optional local profile name or initials may appear as metadata. Do not display unrelated online authors.
- Bottom navigation: Home, Library, Profile.

### Behavior

- Tap a set to open Set Detail. The list includes the current profile's sets, whether grouped or ungrouped. Keep a stable list order and indicate group membership when relevant.
- + opens the create flow. Show “No study sets yet” and Create study set when empty.
- The reference's All dropdown, Filter sets field and month-based headings are optional inspirations; do not introduce filtering, search or date grouping as mandatory requirements without approval.
- No advertising or public set discovery.

## 5. Library — Groups (REF-02)

**Reference:** `outside_group_ui.png`  
**Requirements:** FR-02, FR-03, FR-04.

- Retain the same Library top bar, tabs and bottom navigation.
- A rounded outlined group card contains a folder icon, prominent group name and number of sets. Empty groups display zero sets.
- Tap a group to open Group Detail; + creates a group.
- Show an actionable empty state if no groups exist.
- Use “Groups” consistently throughout the application, including the move-set dialog.

## 6. Group detail (REF-03)

**Reference:** `inside_group_ui.png`  
**Requirements:** FR-03, FR-04, FR-10.

- Top bar: Back on left, Add (+) and overflow menu on right.
- Large group name, set count and optional description.
- Prominent primary action below the header, then a vertical list of outlined set cards.
- Set cards show name, total card count and optionally due count. If optional image support is approved, a thumbnail may use an existing card image; otherwise omit it or use a neutral icon.
- Add opens a new-set or appropriate move-existing-set flow for this group.
- Overflow includes Edit group and Delete group. Deletion must be confirmed and moves contained sets to Ungrouped without losing cards or schedules.

The screenshot's “Study this folder” button suggests group-wide study. The current MVP supports profile-wide graded review and individual-set study. Until group-wide study is explicitly approved, use a primary Create/Add study set action and navigate to each set for study.

## 7. Set detail — flashcard preview (REF-04 and REF-05)

**References:** `flashcard_front.png`, `flashcard_end.png`  
**Requirements:** FR-04, FR-05, FR-06, FR-07, FR-08, FR-10.

### Layout

1. Top bar with Back and overflow menu.
2. Large horizontally browsable, blue-gray flashcard preview. A portion of the next card may be visible to signal swiping.
3. Page indicator beneath the preview (dots for short lists or bounded/numeric indicator for longer lists).
4. Large set name, total cards, eligible/due count and optional description.
5. Distinct Free Study and Grade Mode buttons.
6. Card list and Add card action, with access to edit individual cards.

### Card faces and interaction

- Front: show prompt/question text, centered for short text.
- Back: display corresponding answer. Long answers wrap and may scroll within the card. If P2 image support is approved, either face may contain text and a local image; use side-by-side layout on wide screens and vertically stacked content on narrower screens, maintaining image aspect ratio.
- Tap to flip repeatedly; swipe horizontally to browse. Changing cards resets the visible face to Front. Provide accessible alternatives to gestures. Use a short optional flip animation.
- Preview activity is ungraded and must not change review dates, statistics or history.
- The expand icon can enter focused Free Study if implemented, with predictable Back behavior.
- An empty set shows Add card and disables unavailable study actions.
- Omit Share and Download icons: all cards already reside locally; remote sharing and downloads are out of scope.

## 8. Set action sheet (REF-06)

**Reference:** `edit_a_set.png`  
**Requirements:** FR-04, FR-05.

Tapping the set overflow icon opens a dark Material 3 modal bottom sheet, with full-width rows, leading icons, labels, subtle dividers and Cancel/dismiss behavior.

| Action | Behavior |
| --- | --- |
| Edit set | Opens editor for existing name and description |
| Move to group | Selects an owned group or Ungrouped |
| Set info | Optional local metadata display; may remain on Set Detail |
| Delete set | Opens confirmation explaining permanent loss of set, cards and review history |
| Cancel | Dismisses without changes |

Do not reproduce Add to class, Save and edit (duplicate), or Share; these are not approved requirements.

## 9. Create/edit forms

The reference shows an action sheet rather than a complete editor. Derive forms from the existing requirements using the same dark style:

| Editor | Fields |
| --- | --- |
| Profile | Display name (required) |
| Group | Name (required), description (optional) |
| Set | Name (required), description (optional), group or Ungrouped |
| Card | Front and back text (required); optional per-side image selection if P2 is approved |

Use Save/Cancel, labeled fields, inline errors and scrolling. Disable saving invalid inputs, retain draft on failure, confirm abandoning changed unsaved fields. Editing an existing card supports confirmed deletion. Content edits preserve existing schedules/history according to business rules.

## 10. Focused Free Study — Non-Grade Mode

**Design basis:** REF-04/05 card faces and REF-07 focused layout.  
**Requirement:** FR-07.

- Distraction-free dark screen with Back/Exit, mode label and current position (e.g. 2 / 21).
- Large central rounded flashcard, showing front initially; tap or explicit Flip control reveals/hides answer.
- Previous/Next buttons or equivalent swipe actions navigate all cards in the selected set; disable directions at list boundaries.
- No grade controls or score. Never update recall statistics, history or schedules.
- Handle empty and single-card sets correctly, retaining readable card content.

## 11. Focused Graded Review — Grade Mode (REF-07)

**Reference:** `grade_mode.png`  
**Requirements:** FR-08, FR-09, FR-10, FR-11, FR-12.

### Landscape layout

- One large blue-gray card occupies most of the screen, with comfortable margins and dark background. Center short prompt/answer content; wrap/scroll longer text and preserve optional image aspect ratio.
- Beneath the card place **✗ Forgot**, a session position indicator (e.g. 2 / 21), and **✓ Remembered**.
- Use large rounded tonal buttons, a warm cross icon and a green/teal check icon, consistent with the reference. Include explicit accessible recall labels.
- Provide clear Exit/Back and Flip actions. Avoid unnecessary system immersive mode.

### Portrait layout

- Top bar with Exit/Back, review heading and progress.
- Large central flashcard with distinguishable Front/Back states.
- Two equally important grade actions beneath the card; place progress above or between them. Keep controls accessible at larger font sizes.
- Avoid crowding all reference toolbar icons onto compact screens.

### Interaction rules

1. At review start refresh counts for the current local date. Start a session only for a nonempty queue of eligible new/due cards. Otherwise show the empty state at the entry screen.
2. Present each card front-side up. Allow repeated flipping.
3. Enable Remembered/Forgot only after the answer has been revealed at least once.
4. Rating is a self-assessment, not automated semantic grading.
5. Disable both ratings, Exit and system Back while saving. After commit, advance once and enable navigation. On failure, enable navigation and retain the revealed card for retry.
6. Reset the next card to its front and update session progress. Prevent duplicate ratings.
7. Show Review Summary at session end. Confirm early exit; preserve committed ratings.

The screenshot shows Get a hint, audio, sorting, undo, shuffle, settings and fullscreen. These are visual inspiration, not approved MVP features. Omit inactive or nonfunctional placeholders. Do not shuffle the approved due queue or add undo of committed ratings without an explicit rule protecting persisted schedule and history.

## 12. Supporting screens not pictured

### Home / daily review

- Active profile, new/due counts and prominent indigo Review now button.
- Review now starts profile-wide scheduled review. Library shortcut and owned sets.
- Show “No reviews due” when queue is empty; do not start empty sessions.
- Optional P2 profile coverage measures reviewed-card coverage, not mastery.

### Review summary

- Completion heading, reviewed count, Remembered and Forgot counts, recall percentage when defined.
- No percentage when zero cards were graded. Done returns to originating scope.

### Profile / settings

- Display current local profile, initials or avatar placeholder, switching, creation, renaming and confirmed deletion.
- Profiles are independent local libraries, not authenticated online accounts.
- No public account, synchronization or social controls.

## 13. UI states, accessibility and adaptive layout

- Support loading, empty, populated, validation/error, saving, retry and confirmation states as appropriate.
- Display accurate card counts after updates; avoid leaking old profile content during switching.
- Use accessible semantics, touch targets at least 48 × 48 dp, meaningful icon labels, readable contrast and text scaling. Do not rely only on colors or gestures.
- Compact portrait: single-column navigation and vertically arranged content. Landscape and tablets: wider card layouts, accessible controls and optional expanded presentation. Do not lose committed data or unnecessarily reset the current card on rotation.
- Allow long content to wrap/scroll, rather than shrinking text until unreadable. Preserve state and provide static alternatives to animation.

## 14. UI acceptance criteria

- Library follows REF-01/02 with dark background, relevant tabs and rounded outlined cards.
- Group and set navigation expose only active-profile data.
- Set detail follows REF-04/05 with flippable preview and separate Free Study / Grade Mode actions.
- Set overflow follows REF-06 with only local valid operations and confirmation before deletion.
- Free Study never changes persistent learning state.
- Grade Mode follows REF-07 with a large card, two distinct accessible recall buttons, correct reveal gating, and one persisted rating per successful submission.
- Editors, Home, summary and empty states share a consistent theme. Core actions remain usable offline in portrait and landscape at both default (100%) and 130% font scale. In each configuration, touch targets measure at least 48 × 48 dp and text meets the NFR-04 contrast minima of 4.5:1 for normal text and 3:1 for large text.

## 15. Spec Kit handoff

Use this file and supplied screenshots together with the product overview, functional requirements, business rules and app description during `speckit-specify` and `speckit-plan`. Maintain traceability to FR/NFR/BR identifiers. Raise screenshot-derived interactions that conflict with the feature scope (such as group-wide study, shuffle or undo) during `speckit-clarify` before adding implementation tasks. Do not implement unapproved features merely to reproduce a screenshot.

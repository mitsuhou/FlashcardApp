# UI Description — FlashcardApp

**Version 3.0** · Native Android with Kotlin, Jetpack Compose and Material 3

## 1. Visual references

The screenshots guide card proportions, spacing and the dark palette. Apply those visual
ideas using native Android components and system insets. The
[screen inventory](../5-App-Description.md) defines the six screen types and their behaviors.

| ID | Reference | Use in v1 |
| --- | --- | --- |
| REF-01 | [startup_ui.png](references/startup_ui.png) | Set List row proportions and add action. |
| REF-02 | [outside_group_ui.png](references/outside_group_ui.png) | Retired with the Groups tab. |
| REF-03 | [inside_group_ui.png](references/inside_group_ui.png) | Retired with Group Detail. |
| REF-04 | [flashcard_front.png](references/flashcard_front.png) | Graded Study front-face styling. |
| REF-05 | [flashcard_end.png](references/flashcard_end.png) | Graded Study back-face styling for text. |
| REF-06 | [edit_a_set.png](references/edit_a_set.png) | Style of labeled Edit and Delete actions on Set Detail. |
| REF-07 | [grade_mode.png](references/grade_mode.png) | Large study card and landscape rating layout. |

## 2. Visual system

Use the existing dark Material 3 theme. Keep the screen background navy, flashcard faces
blue-gray, and primary actions indigo. Use the semantic foreground colors from the theme with
their corresponding surfaces. Use the palette in both orientations.

| Role | Design reference |
| --- | --- |
| Background | Deep navy |
| Flashcard surface | Blue-gray |
| List rows and dividers | Subtle outline |
| Primary action | Indigo fill with light text |
| Remembered | Check symbol with green/teal accent |
| Forgot | Cross symbol with warm accent |

Use Material 3 typography, bold screen headings, readable supporting labels, an 8 dp spacing
grid, and approximately 16–20 dp horizontal margins on compact screens. Use rounded outlines
for set rows and a rounded filled surface for the study card.

## 3. Profile Selection/Management (UI-01)

List local profiles with an active marker and explicit selection. Provide Create profile,
Rename and Delete actions with labels and confirm deletion with its effects. Show creation
when the list is empty. Use an inline error or retry action if storage is unavailable.
After selection, open Set List directly.

## 4. Set List (UI-03)

Use a top bar with the active profile control and a prominent Create set action. Display
owned sets in a single scrollable list. Each row shows the set name and total and eligible
card counts. Open Set Detail when a row is tapped. An empty list has a Create set action.

## 5. Set Detail (UI-06)

Show Back, the set name, optional description and total and eligible counts. Use a simple
list of cards with front-text labels and saved next review dates for reviewed cards.
Open the card editor from a row. Offer Add card and the single Start study action.
Keep the learner on Set Detail with a clear message if the queue is empty. Offer Add card
when the set itself is empty.

The set overflow offers Edit set and Delete set, with dismissal. Confirm deletion and explain
that the set, its cards and their review history will be removed.

## 6. Set/Card Editor (UI-05)

Use the same scrollable editor layout for a set's name and optional description or a card's
required front and back text. Use labeled inputs, field errors, Save and Cancel. Keep Save
disabled for invalid input. Preserve drafts on rotation or failure, and confirm discarding
changes. For an existing card, provide Delete with confirmation.

## 7. Graded Study (UI-09)

- Show one large card, a Front or Back label, an explicit Flip action, the original position
  in the queue and a committed-review count. Wrap and scroll long content inside the card.
- Keep Remembered and Forgot visible with text and a check or cross. Enable them after the
  answer has been revealed for the current card. Each next card starts on Front.
- In portrait keep the rating controls under the card. In landscape place Forgot, the
  position indicator and Remembered beneath the card, with enough space for text scaling.
- Show saving feedback and disable both ratings, Exit and system Back during the write.
  On success advance once. On failure keep the revealed card and offer retry. Restore
  navigation after either outcome. Confirm early exit when ungraded cards remain.

## 8. Review Summary (UI-10)

Show reviewed, remembered and forgot counts and the recall percentage when at least one
rating was committed. Done returns to the originating Set Detail.

## 9. Accessibility and verification

Use meaningful accessible labels, explicit controls and touch targets of at least 48 × 48 dp.
Identify both ratings by words and symbols. Keep actions reachable in portrait, landscape and
when the available window size changes. Let long text wrap or scroll. If a flip is animated,
provide a static alternative.

Check core controls at 100% and 130% font scale in both orientations. Verify text contrast on
the actual surfaces at 4.5:1 for normal text and 3:1 for large text, using at least 18 sp or
bold at 14 sp or larger for the large-text criterion. Check that rotation preserves an
unfinished editor draft and the current study card. Verify the offline path from profile
selection to summary and back to Set Detail.

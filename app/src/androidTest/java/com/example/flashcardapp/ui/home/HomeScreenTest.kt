package com.example.flashcardapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ReviewCounts
import com.example.flashcardapp.ui.theme.FlashcardAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyHomeOffersLibraryAndPopulatedHomeEnablesReview() {
        val state = mutableStateOf(HomeUiState(Profile(1, "Bao", 0), ReviewCounts(0, 0)))
        var libraryClicks = 0
        var reviewClicks = 0
        compose.setContent {
            FlashcardAppTheme {
                HomeBody(state.value, { libraryClicks++ }, { reviewClicks++ }, {})
            }
        }
        compose.onNodeWithText("Bao").assertIsDisplayed()
        compose.onNodeWithText("No reviews due").assertIsDisplayed()
        compose.onNodeWithText("Review now").assertIsNotEnabled()
        compose.onNodeWithText("Library").performClick()
        compose.runOnIdle {
            assertEquals(1, libraryClicks)
            state.value = state.value.copy(counts = ReviewCounts(3, 2))
        }
        compose.onNodeWithText("New: 3").assertIsDisplayed()
        compose.onNodeWithText("Due: 2").assertIsDisplayed()
        compose.onNodeWithText("Review now").performClick()
        compose.runOnIdle { assertEquals(1, reviewClicks) }
    }
}

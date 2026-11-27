package com.example.flashcardapp.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.flashcardapp.data.local.Profile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun renameDraftSurvivesRestorationAndUsesTheActiveId() {
        val alice = Profile(1, "Alice", 0)
        var renamed: Pair<Long, String>? = null
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            ProfileScreen(ProfileUiState.Active(alice, listOf(alice)),
                { if (it.isBlank()) "Enter a profile name." else null },
                {}, {}, { id, name -> renamed = id to name }, {}, {})
        }
        compose.onNodeWithText("Rename").performClick()
        val field = compose.onNode(hasSetTextAction())
        field.performTextReplacement(" ")
        compose.onNodeWithText("Enter a profile name.").assertIsDisplayed()
        compose.onNodeWithText("Save").assertIsNotEnabled()
        field.performTextReplacement("Alice Nguyen")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Alice Nguyen").assertIsDisplayed()
        compose.onNodeWithText("Save").performClick()
        compose.runOnIdle { assertEquals(alice.id to "Alice Nguyen", renamed) }
    }

    @Test
    fun switchAndDeleteRequireConfirmation() {
        val alice = Profile(1, "Alice", 0)
        val bao = Profile(2, "Bao", 0)
        val actions = mutableListOf<ProfileChange>()
        compose.setContent {
            ProfileScreen(ProfileUiState.Active(alice, listOf(alice, bao)), { null }, {},
                { actions.add(ProfileChange.Select(it)) }, { _, _ -> },
                { actions.add(ProfileChange.Delete(it)) }, {})
        }
        compose.onNodeWithText("Bao").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Delete").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertEquals(emptyList<ProfileChange>(), actions) }

        compose.onNodeWithText("Bao").performClick()
        compose.onNode(hasText("Switch") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("Delete").performClick()
        compose.onNode(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
        compose.runOnIdle {
            assertEquals(listOf(ProfileChange.Select(bao.id), ProfileChange.Delete(alice.id)), actions)
        }
    }
}

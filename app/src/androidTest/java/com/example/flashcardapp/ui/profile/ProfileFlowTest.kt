package com.example.flashcardapp.ui.profile

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.FlashcardApplication
import com.example.flashcardapp.MainActivity
import com.example.flashcardapp.domain.ReviewGrade
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileFlowTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private val container = ApplicationProvider.getApplicationContext<FlashcardApplication>().container
    private val profiles = container.profileRepository
    private var startedEmpty = false

    @Before
    fun requireEmptyTestInstall() = runBlocking {
        assertTrue("Use an empty test installation", profiles.observeProfiles().first().isEmpty())
        startedEmpty = true
    }

    @After
    fun removeFixtureProfiles() = runBlocking {
        if (startedEmpty) {
            for (profile in profiles.observeProfiles().first()) profiles.deleteProfile(profile.id)
        }
    }

    // Room and DataStore may deliver their results after Compose has become idle.
    private fun displayed(matcher: SemanticsMatcher) = compose.onNode(matcher).also { node ->
        compose.waitUntil(conditionDescription = matcher.description, timeoutMillis = 5_000) {
            compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
        node.assertIsDisplayed()
    }

    private fun tap(label: String) = displayed(hasText(label)).performClick()

    private fun assertHome(name: String, newCards: Int, dueCards: Int) {
        displayed(hasText("Review now"))
        displayed(hasText(name))
        displayed(hasText("New: $newCards"))
        displayed(hasText("Due: $dueCards"))
    }

    private fun assertLibraryHidden() {
        compose.onNodeWithText("Review now").assertDoesNotExist()
        compose.onNodeWithText("Library").assertDoesNotExist()
        compose.onNodeWithText("Vocabulary").assertDoesNotExist()
    }

    private fun seedProfiles() = runBlocking {
        val a = profiles.createProfile("Alice")
        val b = profiles.createProfile("Bao")
        val library = container.libraryRepository
        val setA = library.createSet(a, "Vocabulary", "", null)
        val setB = library.createSet(b, "Vocabulary", "", null)
        library.createCard(a, setA, "Hello", "Xin chào")
        library.createCard(a, setA, "Thanks", "Cảm ơn")
        val card = library.createCard(b, setB, "Goodbye", "Tạm biệt")
        assertTrue(container.reviewRepository.submitGrade(
            b, "fixture", card, ReviewGrade.FORGOT, setB, ZonedDateTime.now().minusDays(2)
        ))
    }

    @Test
    fun createRenameAndRelaunchRestoresTheSameProfile() {
        val createdId: Long
        ActivityScenario.launch(MainActivity::class.java).use {
            displayed(hasSetTextAction()).performTextReplacement("Alice")
            assertLibraryHidden()
            tap("Create profile")
            assertHome("Alice", 0, 0)
            createdId = runBlocking { requireNotNull(profiles.selectedProfileId.first()) }
            tap("Profile")
            tap("Rename")
            displayed(hasSetTextAction()).performTextReplacement("Alice Nguyen")
            tap("Save")
            displayed(hasText("Alice Nguyen"))
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            assertHome("Alice Nguyen", 0, 0)
            val stored = runBlocking { profiles.observeProfiles().first().single() }
            assertEquals(createdId, stored.id)
            assertEquals("Alice Nguyen", stored.displayName)
        }
    }

    @Test
    fun recreationRestoresProfileDraftAndRenameKeepsTheSameRoute() {
        seedProfiles()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            tap("Alice")
            assertHome("Alice", 2, 0)
            tap("Profile")
            tap("Rename")
            displayed(hasSetTextAction()).performTextReplacement("Alice Nguyen")

            scenario.recreate()
            displayed(hasSetTextAction() and hasText("Alice Nguyen"))
            tap("Save")
            displayed(hasText("Rename"))
            displayed(hasText("Alice Nguyen"))
            tap("Home")
            assertHome("Alice Nguyen", 2, 0)
        }
    }

    @Test
    fun switchingOpensTheSelectedHomeAndBackCannotReopenTheOldProfile() {
        seedProfiles()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            displayed(hasText("Alice"))
            displayed(hasText("Bao"))
            assertLibraryHidden()
            tap("Alice")
            assertHome("Alice", 2, 0)
            tap("Profile")
            tap("Bao")
            tap("Switch")
            assertHome("Bao", 0, 1)
            compose.onNodeWithText("Alice").assertDoesNotExist()

            pressBackUnconditionally()
            // Back from the new root can also finish or background the activity.
            if (scenario.state == Lifecycle.State.RESUMED) {
                assertHome("Bao", 0, 1)
                compose.onNodeWithText("Alice").assertDoesNotExist()
            }
        }
    }

    @Test
    fun deletingRequiresConfirmationAndSurvivorMustBeSelectedExplicitly() {
        seedProfiles()
        ActivityScenario.launch(MainActivity::class.java).use {
            tap("Alice")
            assertHome("Alice", 2, 0)
            tap("Profile")
            tap("Delete")
            displayed(hasText("Alice", substring = true) and hasAnyAncestor(isDialog()))
            tap("Cancel")
            assertEquals(2, runBlocking { profiles.observeProfiles().first().size })

            tap("Delete")
            displayed(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodes(hasText("Alice")).fetchSemanticsNodes().isEmpty()
            }
            displayed(hasText("Bao"))
            compose.onNodeWithText("Alice").assertDoesNotExist()
            assertLibraryHidden()
        }
        ActivityScenario.launch(MainActivity::class.java).use {
            displayed(hasText("Bao"))
            assertLibraryHidden()
            tap("Bao")
            assertHome("Bao", 0, 1)
            tap("Profile")
            tap("Delete")
            displayed(hasText("Bao", substring = true) and hasAnyAncestor(isDialog()))
            displayed(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
            displayed(hasSetTextAction())
            displayed(hasText("Create profile"))
            assertLibraryHidden()
            assertTrue(runBlocking { profiles.observeProfiles().first().isEmpty() })
        }
    }
}

package com.example.flashcardapp.ui.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.ProfileRepository
import com.example.flashcardapp.data.ReviewRepository
import com.example.flashcardapp.data.local.Flashcard
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ProfileDao
import com.example.flashcardapp.data.local.ReviewCounts
import com.example.flashcardapp.data.local.ReviewDao
import com.example.flashcardapp.data.local.SessionCounts
import com.example.flashcardapp.domain.ReviewGrade
import com.example.flashcardapp.ui.home.HomeViewModel
import java.time.LocalDate
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val alice = Profile(1, "Alice", 0)
    private val bao = Profile(2, "Bao", 0)
    private val dao = MemoryProfileDao(listOf(alice, bao))
    private val store = MemorySelectionStore()
    private val profiles = ProfileRepository(dao, store)
    private val models = mutableListOf<ViewModel>()

    @Before
    fun setMain() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun resetMain() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    @Test
    fun savedSelectionReadShowsLoadingThenErrorAndRetryRestoresAlice() = runTest {
        profiles.selectProfile(alice.id)
        store.failNextRead = true
        val model = ProfileViewModel(profiles)
        models.add(model)
        backgroundScope.launch { model.uiState.collect() }
        runCurrent()
        assertEquals(ProfileUiState.Loading, model.uiState.value)

        store.allowRead.complete(Unit)
        runCurrent()
        assertTrue(model.uiState.value is ProfileUiState.Error)
        model.retry()
        runCurrent()
        assertEquals(alice, (model.uiState.value as ProfileUiState.Active).profile)
    }

    @Test
    fun failedSelectionAfterCreationRetriesWithoutCreatingAnotherProfile() = runTest {
        dao.rows.value = emptyList()
        store.allowRead.complete(Unit)
        val model = ProfileViewModel(profiles)
        models.add(model)
        backgroundScope.launch { model.uiState.collect() }
        runCurrent()
        assertTrue(model.uiState.value is ProfileUiState.Create)

        store.failNextWrite = true
        model.createProfile("Lan")
        runCurrent()
        val created = dao.rows.value.single()
        assertEquals("Lan", created.displayName)
        assertTrue(model.uiState.value is ProfileUiState.Error)
        model.retry()
        runCurrent()
        assertEquals(listOf(created), dao.rows.value)
        assertEquals(created, (model.uiState.value as ProfileUiState.Active).profile)
    }

    @Test
    fun switchingCancelsAliceCountsAndKeepsHomeClearUntilBaoCountsArrive() = runTest {
        store.allowRead.complete(Unit)
        profiles.selectProfile(alice.id)
        val counts = HomeCountsDao(alice.id, bao.id)
        val profileModel = ProfileViewModel(profiles)
        val home = HomeViewModel(profiles, ReviewRepository(counts))
        models.add(profileModel)
        models.add(home)
        backgroundScope.launch { profileModel.uiState.collect() }
        backgroundScope.launch { home.uiState.collect() }
        runCurrent()
        assertEquals(1, counts.alice.subscriptionCount.value)
        counts.alice.emit(ReviewCounts(3, 0))
        runCurrent()
        assertEquals(ReviewCounts(3, 0), home.uiState.value.counts)

        profileModel.selectProfile(bao.id)
        runCurrent()
        assertEquals(bao, (profileModel.uiState.value as ProfileUiState.Active).profile)
        assertEquals(0, counts.alice.subscriptionCount.value)
        assertEquals(1, counts.bao.subscriptionCount.value)
        assertNull(home.uiState.value.counts)
        counts.alice.emit(ReviewCounts(99, 99))
        runCurrent()
        assertNull(home.uiState.value.counts)

        counts.bao.emit(ReviewCounts(0, 2))
        runCurrent()
        assertEquals(ReviewCounts(0, 2), home.uiState.value.counts)
        counts.alice.emit(ReviewCounts(98, 98))
        runCurrent()
        assertEquals(ReviewCounts(0, 2), home.uiState.value.counts)
    }

    @Test
    fun newLocalDayRefreshesHomeCountsWithoutAProfileOrCardWrite() = runTest {
        store.allowRead.complete(Unit)
        profiles.selectProfile(alice.id)
        val counts = HomeCountsDao(alice.id, bao.id)
        val home = HomeViewModel(profiles, ReviewRepository(counts))
        models.add(home)
        val day = LocalDate.of(2026, 11, 27)
        home.refreshDate(day)
        runCurrent()
        counts.alice.emit(ReviewCounts(0, 0))
        runCurrent()
        assertEquals(ReviewCounts(0, 0), home.uiState.value.counts)
        home.refreshDate(day.plusDays(1))
        runCurrent()
        assertEquals(day.plusDays(1).toEpochDay(), counts.observedDay)
        assertNull(home.uiState.value.counts)
        counts.alice.emit(ReviewCounts(0, 1))
        runCurrent()
        assertEquals(ReviewCounts(0, 1), home.uiState.value.counts)
    }

    private class MemorySelectionStore : DataStore<Preferences> {
        private val saved = MutableStateFlow(emptyPreferences())
        val allowRead = CompletableDeferred<Unit>()
        var failNextRead = false
        var failNextWrite = false

        override val data: Flow<Preferences> = flow {
            allowRead.await()
            if (failNextRead) {
                failNextRead = false
                throw IOException("Selection read failed")
            }
            emitAll(saved)
        }

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            if (failNextWrite) {
                failNextWrite = false
                throw IOException("Selection write failed")
            }
            val updated = transform(saved.value)
            saved.value = updated
            return updated
        }
    }

    private class MemoryProfileDao(initial: List<Profile>) : ProfileDao {
        val rows = MutableStateFlow(initial)
        override fun observeProfiles(): Flow<List<Profile>> = rows
        override suspend fun getProfile(profileId: Long) = rows.value.find { it.id == profileId }
        override suspend fun insertProfile(profile: Profile): Long {
            val id = (rows.value.maxOfOrNull { it.id } ?: 0L) + 1
            rows.value += profile.copy(id = id)
            return id
        }
        override suspend fun renameProfile(profileId: Long, displayName: String): Int = error("Unused")
        override suspend fun deleteProfile(profileId: Long): Int = error("Unused")
    }

    private class HomeCountsDao(private val aliceId: Long, private val baoId: Long) : ReviewDao {
        val alice = MutableSharedFlow<ReviewCounts>()
        val bao = MutableSharedFlow<ReviewCounts>()
        var observedDay: Long? = null
        override fun observeReviewCounts(profileId: Long, today: Long, setId: Long?): Flow<ReviewCounts> {
            observedDay = today
            return when (profileId) {
                aliceId -> alice
                baoId -> bao
                else -> error("Unexpected profile")
            }
        }

        override suspend fun getEligibleCardIds(profileId: Long, today: Long, setId: Long?): List<Long> = error("Unused")
        override suspend fun getCard(profileId: Long, cardId: Long, setId: Long?): Flashcard? = error("Unused")
        override suspend fun getSessionCounts(profileId: Long, sessionId: String): SessionCounts = error("Unused")
        override suspend fun hasGrade(profileId: Long, sessionId: String, cardId: Long): Boolean = error("Unused")
        override suspend fun insertGrade(profileId: Long, setId: Long?, sessionId: String, cardId: Long,
            grade: ReviewGrade, reviewedAt: Long): Unit = error("Unused")
        override suspend fun updateSchedule(profileId: Long, setId: Long?, cardId: Long, stage: Int,
            dueDay: Long): Int = error("Unused")
    }
}

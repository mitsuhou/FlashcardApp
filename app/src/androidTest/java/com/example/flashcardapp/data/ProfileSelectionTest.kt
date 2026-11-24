package com.example.flashcardapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.data.local.FlashcardDatabase
import com.example.flashcardapp.data.local.LibraryDao
import com.example.flashcardapp.data.local.ProfileDao
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileSelectionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "selection-test-${UUID.randomUUID()}"
    private val preferencesFile = File(context.cacheDir, "$name.preferences_pb")
    private lateinit var db: FlashcardDatabase
    private lateinit var storeJob: Job
    private lateinit var store: DataStore<Preferences>
    private lateinit var profiles: ProfileRepository

    @Before
    fun openStorage() {
        db = Room.databaseBuilder(context, FlashcardDatabase::class.java, name).build()
        storeJob = Job()
        store = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + storeJob),
            produceFile = { preferencesFile }
        )
        profiles = ProfileRepository(db.profileDao(), store)
    }

    private suspend fun closeStorage() {
        storeJob.cancelAndJoin()
        db.close()
    }

    private suspend fun reopenStorage() {
        closeStorage()
        openStorage()
    }

    @After
    fun tearDown() {
        runBlocking { closeStorage() }
        context.deleteDatabase(name)
        preferencesFile.delete()
    }

    // Throw inside the real DataStore update so the attempted edit cannot commit.
    private fun repositoryWithOneFailedWrite(): ProfileRepository {
        val failingStore = object : DataStore<Preferences> by store {
            var failNext = true
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                store.updateData { previous ->
                    val updated = transform(previous)
                    if (failNext) {
                        failNext = false
                        throw IOException("Test write failure")
                    }
                    updated
                }
        }
        return ProfileRepository(db.profileDao(), failingStore)
    }

    @Test
    fun explicitSelectionRestoresAfterReopeningBothFiles() = runBlocking {
        profiles.createProfile("A")
        val b = profiles.createProfile("B")
        profiles.selectProfile(b)
        reopenStorage()
        assertEquals(b, profiles.selectedProfileId.first())
    }

    @Test
    fun deletionRequiresExplicitSelectionEvenWithOneSurvivor() = runBlocking {
        val emptyPreferences = store.data.first()
        assertNull(profiles.selectedProfileId.first())
        assertTrue(profiles.observeProfiles().first().isEmpty())
        val a = profiles.createProfile("A")
        assertNull(profiles.selectedProfileId.first())
        val b = profiles.createProfile("B")
        profiles.selectProfile(a)

        assertTrue(profiles.deleteProfile(a))
        reopenStorage()
        assertEquals(emptyPreferences, store.data.first())
        assertNull(profiles.selectedProfileId.first())
        assertEquals(listOf(b), profiles.observeProfiles().first().map { it.id })
        profiles.selectProfile(b)
        assertEquals(b, profiles.selectedProfileId.first())

        assertTrue(profiles.deleteProfile(b))
        reopenStorage()
        assertNull(profiles.selectedProfileId.first())
        assertTrue(profiles.observeProfiles().first().isEmpty())
    }

    @Test
    fun interruptedCleanupLeavesAStaleIdThatCannotBeRestored() = runBlocking {
        val a = profiles.createProfile("A")
        val b = profiles.createProfile("B")
        profiles.selectProfile(a)
        val savedSelection = store.data.first()
        val failing = repositoryWithOneFailedWrite()
        val error = runCatching { failing.deleteProfile(a) }.exceptionOrNull()
        assertTrue(error is IOException)

        reopenStorage()
        assertEquals(savedSelection, store.data.first())
        assertNull(profiles.selectedProfileId.first())
        assertEquals(listOf(b), profiles.observeProfiles().first().map { it.id })
        profiles.selectProfile(b)
        assertEquals(b, profiles.selectedProfileId.first())
    }

    @Test
    fun deletionCleanupPreservesANewerSelection() = runBlocking {
        val a = profiles.createProfile("A")
        val b = profiles.createProfile("B")
        profiles.selectProfile(a)
        val dao = db.profileDao()
        val switchAfterDelete = object : ProfileDao by dao {
            override suspend fun deleteProfile(profileId: Long): Int {
                val deleted = dao.deleteProfile(profileId)
                profiles.selectProfile(b)
                return deleted
            }
        }
        assertTrue(ProfileRepository(switchAfterDelete, store).deleteProfile(a))
        reopenStorage()
        assertEquals(b, profiles.selectedProfileId.first())
    }

    @Test
    fun readFailureReachesCallerAndRecollectionRestoresSelection() = runBlocking {
        val a = profiles.createProfile("A")
        profiles.selectProfile(a)
        val failingStore = object : DataStore<Preferences> by store {
            var failNext = true
            override val data: Flow<Preferences> = flow {
                if (failNext) {
                    failNext = false
                    throw IOException("Test read failure")
                }
                emitAll(store.data)
            }
        }
        val selection = ProfileRepository(db.profileDao(), failingStore).selectedProfileId
        val error = runCatching { selection.first() }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals(a, selection.first())
    }

    @Test
    fun failedSelectionRetriesTheCreatedIdWithoutDuplicatingTheProfile() = runBlocking {
        val previous = profiles.createProfile("A")
        profiles.selectProfile(previous)
        val failing = repositoryWithOneFailedWrite()
        val created = failing.createProfile("B")
        val error = runCatching { failing.selectProfile(created) }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals(previous, failing.selectedProfileId.first())

        failing.selectProfile(created)
        reopenStorage()
        assertEquals(created, profiles.selectedProfileId.first())
        assertEquals(listOf(previous, created), profiles.observeProfiles().first().map { it.id })
    }

    @Test
    fun inFlightWriteKeepsItsOriginalOwnerAfterSwitch() = runBlocking {
        val a = profiles.createProfile("A")
        val b = profiles.createProfile("B")
        profiles.selectProfile(a)
        val dao = db.libraryDao()
        val library = LibraryRepository(dao)
        val groupA = library.createGroup(a, "Shared", "")
        val groupB = library.createGroup(b, "Shared", "")
        val started = CompletableDeferred<Unit>()
        val resume = CompletableDeferred<Unit>()
        val pausedDao = object : LibraryDao by dao {
            override suspend fun updateGroup(profileId: Long, groupId: Long, name: String, description: String?): Int {
                started.complete(Unit)
                resume.await()
                return dao.updateGroup(profileId, groupId, name, description)
            }
        }
        val origin = requireNotNull(profiles.selectedProfileId.first())
        val write = async { LibraryRepository(pausedDao).updateGroup(origin, groupA, "Edited by A", "") }
        withTimeout(10_000) { started.await() }
        profiles.selectProfile(b)
        val active = requireNotNull(profiles.selectedProfileId.first())
        assertEquals(b, active)
        assertNull(library.observeGroup(active, groupA).first())
        assertFalse(library.updateGroup(active, groupA, "Edited by B", ""))
        resume.complete(Unit)
        assertTrue(write.await())

        assertEquals("Edited by A", library.observeGroup(a, groupA).first()?.name)
        assertEquals("Shared", library.observeGroup(b, groupB).first()?.name)
        assertEquals(listOf(groupB), library.observeGroups(b).first().map { it.id })
    }
}

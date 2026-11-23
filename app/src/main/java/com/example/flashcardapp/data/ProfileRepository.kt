package com.example.flashcardapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ProfileDao
import com.example.flashcardapp.domain.isValidProfileName
import com.example.flashcardapp.domain.trimField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class ProfileRepository(
    private val profileDao: ProfileDao,
    //Data store governs the current active profile, even when the app is closed.
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SELECTED_PROFILE_ID = longPreferencesKey("selected_profile_id")
    }

    val selectedProfileId: Flow<Long?> =
        combine(dataStore.data, profileDao.observeProfiles()) { preferences, profiles ->
            val savedId = preferences[SELECTED_PROFILE_ID]
            profiles.find { it.id == savedId }?.id
        }.distinctUntilChanged()

    fun observeProfiles(): Flow<List<Profile>> = profileDao.observeProfiles()

    suspend fun selectProfile(profileId: Long) {
        dataStore.edit { preferences ->
            requireNotNull(profileDao.getProfile(profileId)) { "Profile does not exist." }
            preferences[SELECTED_PROFILE_ID] = profileId
        }
    }

    suspend fun createProfile(displayName: String): Long {
        val profile = Profile(
            displayName = validatedName(displayName),
            createdAt = System.currentTimeMillis()
        )
        return profileDao.insertProfile(profile)
    }

    suspend fun renameProfile(profileId: Long, displayName: String): Boolean =
        profileDao.renameProfile(profileId, validatedName(displayName)) == 1

    suspend fun deleteProfile(profileId: Long): Boolean {
        val deleted = profileDao.deleteProfile(profileId) == 1
        dataStore.edit { preferences ->
            if (preferences[SELECTED_PROFILE_ID] == profileId) {
                preferences.remove(SELECTED_PROFILE_ID)
            }
        }
        return deleted
    }

    private fun validatedName(displayName: String): String {
        require(isValidProfileName(displayName)) { "Profile name must not be blank." }
        return trimField(displayName)
    }
}

package com.example.flashcardapp.data

import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ProfileDao
import com.example.flashcardapp.domain.isValidProfileName
import com.example.flashcardapp.domain.trimField
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {
    fun observeProfiles(): Flow<List<Profile>> = profileDao.observeProfiles()

    suspend fun createProfile(displayName: String): Long {
        val profile = Profile(
            displayName = validatedName(displayName),
            createdAt = System.currentTimeMillis()
        )
        return profileDao.insertProfile(profile)
    }

    suspend fun renameProfile(profileId: Long, displayName: String): Boolean =
        profileDao.renameProfile(profileId, validatedName(displayName)) == 1

    suspend fun deleteProfile(profileId: Long): Boolean =
        profileDao.deleteProfile(profileId) == 1

    private fun validatedName(displayName: String): String {
        require(isValidProfileName(displayName)) { "Profile name must not be blank." }
        return trimField(displayName)
    }
}

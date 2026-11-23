package com.example.flashcardapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY displayName, id")
    fun observeProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfile(profileId: Long): Profile?

    @Insert
    suspend fun insertProfile(profile: Profile): Long

    @Query("UPDATE profiles SET displayName = :displayName WHERE id = :profileId")
    suspend fun renameProfile(profileId: Long, displayName: String): Int

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long): Int
}

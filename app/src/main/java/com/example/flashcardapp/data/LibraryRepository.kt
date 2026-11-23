package com.example.flashcardapp.data

import com.example.flashcardapp.data.local.GroupWithCount
import com.example.flashcardapp.data.local.LibraryCard
import com.example.flashcardapp.data.local.LibraryDao
import com.example.flashcardapp.data.local.SetWithCounts
import com.example.flashcardapp.data.local.StudyGroup
import com.example.flashcardapp.data.local.StudySet
import com.example.flashcardapp.domain.isValidCardFace
import com.example.flashcardapp.domain.isValidDescription
import com.example.flashcardapp.domain.isValidGroupOrSetName
import com.example.flashcardapp.domain.normalizeDescription
import com.example.flashcardapp.domain.trimField
import kotlinx.coroutines.flow.Flow

class LibraryRepository(private val libraryDao: LibraryDao) {
    fun observeGroups(profileId: Long): Flow<List<GroupWithCount>> =
        libraryDao.observeGroups(profileId)

    fun observeGroup(profileId: Long, groupId: Long): Flow<StudyGroup?> =
        libraryDao.observeGroup(profileId, groupId)

    suspend fun createGroup(profileId: Long, name: String, description: String): Long {
        val group = StudyGroup(
            ownerId = profileId,
            name = validatedName(name),
            description = validatedDescription(description)
        )
        return libraryDao.insertGroup(group)
    }

    suspend fun updateGroup(
        profileId: Long,
        groupId: Long,
        name: String,
        description: String
    ): Boolean = libraryDao.updateGroup(
        profileId, groupId, validatedName(name), validatedDescription(description)
    ) == 1

    suspend fun deleteGroup(profileId: Long, groupId: Long): Boolean =
        libraryDao.deleteGroup(profileId, groupId)

    fun observeSets(profileId: Long, today: Long, groupId: Long? = null): Flow<List<SetWithCounts>> =
        libraryDao.observeSets(profileId, today, groupId)

    fun observeUngroupedSets(profileId: Long, today: Long): Flow<List<SetWithCounts>> =
        libraryDao.observeSets(profileId, today, groupId = null, allGroups = false)

    fun observeSet(profileId: Long, setId: Long): Flow<StudySet?> =
        libraryDao.observeSet(profileId, setId)

    suspend fun createSet(profileId: Long, name: String, description: String, groupId: Long?): Long {
        val set = StudySet(
            ownerId = profileId,
            name = validatedName(name),
            description = validatedDescription(description),
            groupId = groupId
        )
        return libraryDao.insertSet(set)
    }

    suspend fun updateSet(
        profileId: Long,
        setId: Long,
        name: String,
        description: String,
        groupId: Long?
    ): Boolean = libraryDao.updateSet(
        profileId, setId, validatedName(name), validatedDescription(description), groupId
    ) == 1

    suspend fun moveSet(profileId: Long, setId: Long, groupId: Long?): Boolean =
        libraryDao.moveSet(profileId, setId, groupId) == 1

    suspend fun deleteSet(profileId: Long, setId: Long): Boolean =
        libraryDao.deleteSet(profileId, setId) == 1

    fun observeCards(profileId: Long, setId: Long): Flow<List<LibraryCard>> =
        libraryDao.observeCards(profileId, setId)

    fun observeCard(profileId: Long, setId: Long, cardId: Long): Flow<LibraryCard?> =
        libraryDao.observeCard(profileId, setId, cardId)

    suspend fun createCard(profileId: Long, setId: Long, front: String, back: String): Long =
        libraryDao.insertCard(
            profileId, setId, validatedFace(front), validatedFace(back), System.currentTimeMillis()
        )

    suspend fun updateCardText(
        profileId: Long,
        setId: Long,
        cardId: Long,
        front: String,
        back: String
    ): Boolean = libraryDao.updateCardText(
        profileId, setId, cardId, validatedFace(front), validatedFace(back)
    ) == 1

    suspend fun deleteCard(profileId: Long, setId: Long, cardId: Long): Boolean =
        libraryDao.deleteCard(profileId, setId, cardId) == 1

    private fun validatedName(name: String): String {
        require(isValidGroupOrSetName(name)) { "Invalid group or set name." }
        return trimField(name)
    }

    private fun validatedDescription(description: String): String? {
        require(isValidDescription(description)) { "Invalid description." }
        return normalizeDescription(description)
    }

    private fun validatedFace(face: String): String {
        require(isValidCardFace(face)) { "Invalid card face." }
        return trimField(face)
    }
}

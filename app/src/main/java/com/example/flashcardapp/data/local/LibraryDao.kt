package com.example.flashcardapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class GroupWithCount(val id: Long, val name: String, val description: String?, val setCount: Int)

data class SetWithCounts(
    val id: Long,
    val name: String,
    val description: String?,
    val groupId: Long?,
    val cardCount: Int,
    val eligibleCount: Int
)

@Dao
interface LibraryDao {
    @Query("""
        SELECT g.id, g.name, g.description, COUNT(s.id) AS setCount
        FROM study_groups AS g
        LEFT JOIN study_sets AS s ON s.groupId = g.id AND s.ownerId = g.ownerId
        WHERE g.ownerId = :profileId
        GROUP BY g.id
        ORDER BY g.name, g.id
    """)
    fun observeGroups(profileId: Long): Flow<List<GroupWithCount>>

    @Query("SELECT * FROM study_groups WHERE ownerId = :profileId AND id = :groupId")
    fun observeGroup(profileId: Long, groupId: Long): Flow<StudyGroup?>

    @Insert
    suspend fun insertGroup(group: StudyGroup): Long

    @Query("""
        UPDATE study_groups SET name = :name, description = :description
        WHERE ownerId = :profileId AND id = :groupId
    """)
    suspend fun updateGroup(profileId: Long, groupId: Long, name: String, description: String?): Int

    @Query("UPDATE study_sets SET groupId = NULL WHERE ownerId = :profileId AND groupId = :groupId")
    suspend fun detachSetsFromGroup(profileId: Long, groupId: Long): Int

    @Query("""
        DELETE FROM study_groups
        WHERE ownerId = :profileId AND id = :groupId
          AND NOT EXISTS (
              SELECT 1 FROM study_sets WHERE ownerId = :profileId AND groupId = :groupId
          )
    """)
    suspend fun deleteEmptyGroup(profileId: Long, groupId: Long): Int

    // A null group filter shows the entire library, including ungrouped sets.
    @Query("""
        SELECT s.id, s.name, s.description, s.groupId, COUNT(c.id) AS cardCount,
               COUNT(CASE WHEN c.dueDay IS NULL OR c.dueDay <= :today THEN c.id END) AS eligibleCount
        FROM study_sets AS s
        LEFT JOIN flashcards AS c ON c.setId = s.id
        WHERE s.ownerId = :profileId AND (:groupId IS NULL OR s.groupId = :groupId)
        GROUP BY s.id
        ORDER BY s.name, s.id
    """)
    fun observeSets(profileId: Long, today: Long, groupId: Long? = null): Flow<List<SetWithCounts>>

    @Query("SELECT * FROM study_sets WHERE ownerId = :profileId AND id = :setId")
    fun observeSet(profileId: Long, setId: Long): Flow<StudySet?>

    // The composite foreign key checks that a non-null group belongs to this profile.
    @Insert
    suspend fun insertSet(set: StudySet): Long

    @Query("""
        UPDATE study_sets SET name = :name, description = :description, groupId = :groupId
        WHERE ownerId = :profileId AND id = :setId
          AND (:groupId IS NULL OR EXISTS (SELECT 1 FROM study_groups WHERE ownerId = :profileId AND id = :groupId))
    """)
    suspend fun updateSet(profileId: Long, setId: Long, name: String, description: String?, groupId: Long?): Int

    @Query("""
        UPDATE study_sets SET groupId = :groupId
        WHERE ownerId = :profileId AND id = :setId
          AND (:groupId IS NULL OR EXISTS (SELECT 1 FROM study_groups WHERE ownerId = :profileId AND id = :groupId))
    """)
    suspend fun moveSet(profileId: Long, setId: Long, groupId: Long?): Int

    @Query("DELETE FROM study_sets WHERE ownerId = :profileId AND id = :setId")
    suspend fun deleteSet(profileId: Long, setId: Long): Int

    @Query("""
        SELECT c.* FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId
        ORDER BY c.createdAt, c.id
    """)
    fun observeCards(profileId: Long, setId: Long): Flow<List<Flashcard>>

    @Query("""
        SELECT c.* FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId AND c.id = :cardId
    """)
    fun observeCard(profileId: Long, setId: Long, cardId: Long): Flow<Flashcard?>

    // setId is required. If the lookup finds no owned set, SQLite rejects the insert.
    @Query("""
        INSERT INTO flashcards (setId, front, back, createdAt)
        VALUES ((SELECT id FROM study_sets WHERE ownerId = :profileId AND id = :setId), :front, :back, :createdAt)
    """)
    suspend fun insertCard(profileId: Long, setId: Long, front: String, back: String, createdAt: Long): Long

    @Query("""
        UPDATE flashcards SET front = :front, back = :back
        WHERE id = :cardId AND setId = (SELECT id FROM study_sets WHERE ownerId = :profileId AND id = :setId)
    """)
    suspend fun updateCardText(profileId: Long, setId: Long, cardId: Long, front: String, back: String): Int

    @Query("""
        DELETE FROM flashcards
        WHERE id = :cardId AND setId = (SELECT id FROM study_sets WHERE ownerId = :profileId AND id = :setId)
    """)
    suspend fun deleteCard(profileId: Long, setId: Long, cardId: Long): Int
}

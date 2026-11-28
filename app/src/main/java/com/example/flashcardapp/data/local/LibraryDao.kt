package com.example.flashcardapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class SetWithCounts(
    val id: Long,
    val name: String,
    val cardCount: Int,
    val eligibleCount: Int
)

data class LibraryCard(val id: Long, val front: String, val back: String, val dueDay: Long?)

@Dao
interface LibraryDao {
    @Query("""
        SELECT s.id, s.name, COUNT(c.id) AS cardCount,
               COUNT(CASE WHEN c.dueDay IS NULL OR c.dueDay <= :today THEN c.id END) AS eligibleCount
        FROM study_sets AS s
        LEFT JOIN flashcards AS c ON c.setId = s.id
        WHERE s.ownerId = :profileId
        GROUP BY s.id
        ORDER BY s.name, s.id
    """)
    fun observeSets(profileId: Long, today: Long): Flow<List<SetWithCounts>>

    @Query("""
        SELECT s.id, s.name, COUNT(c.id) AS cardCount,
               COUNT(CASE WHEN c.dueDay IS NULL OR c.dueDay <= :today THEN c.id END) AS eligibleCount
        FROM study_sets AS s
        LEFT JOIN flashcards AS c ON c.setId = s.id
        WHERE s.ownerId = :profileId AND s.id = :setId
        GROUP BY s.id
    """)
    fun observeSetCounts(profileId: Long, setId: Long, today: Long): Flow<SetWithCounts?>

    @Query("SELECT * FROM study_sets WHERE ownerId = :profileId AND id = :setId")
    fun observeSet(profileId: Long, setId: Long): Flow<StudySet?>

    @Insert
    suspend fun insertSet(set: StudySet): Long

    @Query("""
        UPDATE study_sets SET name = :name, description = :description
        WHERE ownerId = :profileId AND id = :setId
    """)
    suspend fun updateSet(profileId: Long, setId: Long, name: String, description: String?): Int

    @Query("DELETE FROM study_sets WHERE ownerId = :profileId AND id = :setId")
    suspend fun deleteSet(profileId: Long, setId: Long): Int

    @Query("""
        SELECT c.id, c.front, c.back, c.dueDay FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId
        ORDER BY c.createdAt, c.id
    """)
    fun observeCards(profileId: Long, setId: Long): Flow<List<LibraryCard>>

    @Query("""
        SELECT c.id, c.front, c.back, c.dueDay FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId AND c.id = :cardId
    """)
    fun observeCard(profileId: Long, setId: Long, cardId: Long): Flow<LibraryCard?>

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

package com.example.flashcardapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = StudySet::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["setId", "createdAt", "id"]),
        Index(value = ["setId", "dueDay", "id"])
    ]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setId: Long,
    val front: String,
    val back: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0")
    val stage: Int = 0,
    val dueDay: Long? = null
)

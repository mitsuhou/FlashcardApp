package com.example.flashcardapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.flashcardapp.domain.ReviewGrade

@Entity(
    tableName = "review_logs",
    primaryKeys = ["sessionId", "cardId"],
    foreignKeys = [
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class ReviewLog(
    val sessionId: String,
    val cardId: Long,
    val grade: ReviewGrade,
    val reviewedAt: Long
)

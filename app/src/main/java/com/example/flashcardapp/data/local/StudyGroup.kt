package com.example.flashcardapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_groups",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ownerId", "id"], unique = true)]
)
data class StudyGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val description: String? = null
)

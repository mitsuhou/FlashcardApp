package com.example.flashcardapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sets",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudyGroup::class,
            parentColumns = ["ownerId", "id"],
            childColumns = ["ownerId", "groupId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["ownerId", "groupId"])]
)
data class StudySet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val groupId: Long? = null,
    val description: String? = null
)

package com.example.flashcardapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Profile::class, StudySet::class, Flashcard::class, ReviewLog::class],
    version = 1,
    exportSchema = true
)
abstract class FlashcardDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun libraryDao(): LibraryDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var instance: FlashcardDatabase? = null

        fun getDatabase(context: Context): FlashcardDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlashcardDatabase::class.java,
                    "flashcard_database"
                ).build().also { instance = it }
            }
        }
    }
}

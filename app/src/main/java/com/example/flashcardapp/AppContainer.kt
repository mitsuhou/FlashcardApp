package com.example.flashcardapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.flashcardapp.data.LibraryRepository
import com.example.flashcardapp.data.ProfileRepository
import com.example.flashcardapp.data.ReviewRepository
import com.example.flashcardapp.data.local.FlashcardDatabase

class AppContainer(database: FlashcardDatabase, dataStore: DataStore<Preferences>) {
    val profileRepository = ProfileRepository(database.profileDao(), dataStore)
    val libraryRepository = LibraryRepository(database.libraryDao())
    val reviewRepository = ReviewRepository(database.reviewDao())
}

package com.example.flashcardapp

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.flashcardapp.data.local.FlashcardDatabase

private val Context.profileDataStore by preferencesDataStore(name = "profile_preferences")

class FlashcardApplication : Application() {
    val container by lazy {
        AppContainer(FlashcardDatabase.getDatabase(this), profileDataStore)
    }
}

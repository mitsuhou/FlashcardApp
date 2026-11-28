package com.example.flashcardapp

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flashcardapp.ui.home.HomeViewModel
import com.example.flashcardapp.ui.profile.ProfileViewModel

object AppViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProfileViewModel(flashcardApplication().container.profileRepository)
        }
        initializer {
            val container = flashcardApplication().container
            HomeViewModel(container.profileRepository, container.reviewRepository)
        }
    }
}

internal fun CreationExtras.flashcardApplication(): FlashcardApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as FlashcardApplication

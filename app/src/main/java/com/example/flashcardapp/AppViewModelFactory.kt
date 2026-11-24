package com.example.flashcardapp

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory

object AppViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        // Screen initializers get their repositories from flashcardApplication().container.
    }
}

internal fun CreationExtras.flashcardApplication(): FlashcardApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as FlashcardApplication
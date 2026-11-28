package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.flashcardapp.ui.navigation.FlashcardNavHost
import com.example.flashcardapp.ui.theme.FlashcardAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FlashcardNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

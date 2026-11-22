package com.example.flashcardapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlashcardColors = darkColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = CardSurface,
    onPrimaryContainer = PrimaryText,
    secondary = Lavender,
    onSecondary = Navy,
    secondaryContainer = GradeActionSurface,
    onSecondaryContainer = Lavender,
    background = Navy,
    onBackground = PrimaryText,
    surface = Navy,
    onSurface = PrimaryText,
    surfaceVariant = CardSurface,
    onSurfaceVariant = SecondaryText,
    surfaceContainer = CardSurface,
    outline = MutedText,
    outlineVariant = Outline,
)

@Composable
fun FlashcardAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlashcardColors,
        typography = Typography,
        content = content,
    )
}

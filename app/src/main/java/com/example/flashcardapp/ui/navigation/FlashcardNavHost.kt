package com.example.flashcardapp.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flashcardapp.AppViewModelFactory
import com.example.flashcardapp.R
import com.example.flashcardapp.ui.home.HomeScreen
import com.example.flashcardapp.ui.profile.ProfileScreen
import com.example.flashcardapp.ui.profile.ProfileUiState
import com.example.flashcardapp.ui.profile.ProfileViewModel

enum class FlashcardScreen {
    Home,
    Profile
}

@Composable
fun FlashcardNavHost(
    modifier: Modifier = Modifier,
    profiles: ProfileViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val state by profiles.uiState.collectAsStateWithLifecycle()
    val activeId = (state as? ProfileUiState.Active)?.profile?.id
    val nav = rememberNavController()
    var stackOwner by rememberSaveable { mutableStateOf<Long?>(null) }
    val showWorkspace = activeId != null && activeId == stackOwner

    LaunchedEffect(state) {
        // Loading, saving and retry screens do not establish a new owner.
        if (activeId == null && state !is ProfileUiState.Selector && state !is ProfileUiState.Create) {
            return@LaunchedEffect
        }
        if (stackOwner != activeId) {
            nav.navigate(FlashcardScreen.Home.name) {
                popUpTo(FlashcardScreen.Home.name) { inclusive = true }
            }
            stackOwner = activeId
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (activeId == null) {
            ProfileRoute(state, profiles)
        }
        NavHost(
            navController = nav,
            startDestination = FlashcardScreen.Home.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(FlashcardScreen.Home.name) {
                if (showWorkspace) {
                    Column {
                        TextButton(onClick = { nav.navigate(FlashcardScreen.Profile.name) { launchSingleTop = true } }) {
                            Text(stringResource(R.string.profile))
                        }
                        HomeScreen(
                            viewModel = viewModel(factory = AppViewModelFactory.Factory),
                            onLibrary = null,
                            onReviewNow = null
                        )
                    }
                }
            }
            composable(FlashcardScreen.Profile.name) {
                if (showWorkspace) {
                    Column {
                        TextButton(onClick = { nav.popBackStack() }) { Text(stringResource(R.string.home)) }
                        ProfileRoute(state, profiles)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRoute(state: ProfileUiState, profiles: ProfileViewModel) {
    ProfileScreen(
        uiState = state,
        nameError = profiles::nameError,
        onCreate = profiles::createProfile,
        onSelect = profiles::selectProfile,
        onRename = profiles::renameProfile,
        onDelete = profiles::deleteProfile,
        onRetry = profiles::retry
    )
}

package com.example.flashcardapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.ProfileRepository
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.domain.isValidProfileName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    object Create : ProfileUiState
    data class Selector(val profiles: List<Profile>) : ProfileUiState
    data class Active(val profile: Profile, val profiles: List<Profile>) : ProfileUiState
    object Saving : ProfileUiState
    // A read failure has no pending write. Retry reads the saved selection again.
    data class Error(val failedChange: ProfileChange? = null) : ProfileUiState
}

sealed interface ProfileChange {
    data class Create(val displayName: String) : ProfileChange
    data class Select(val profileId: Long) : ProfileChange
    data class Rename(val profileId: Long, val displayName: String) : ProfileChange
    data class Delete(val profileId: Long) : ProfileChange
}

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var observation: Job? = null

    init {
        observeSelection()
    }

    private fun observeSelection() {
        _uiState.value = ProfileUiState.Loading
        observation = viewModelScope.launch {
            combine(repository.selectedProfileId, repository.observeProfiles()) { id, profiles ->
                val active = profiles.find { it.id == id }
                when {
                    active != null -> ProfileUiState.Active(active, profiles)
                    profiles.isEmpty() -> ProfileUiState.Create
                    else -> ProfileUiState.Selector(profiles)
                }
            }.catch { emit(ProfileUiState.Error()) }
                .collect { _uiState.value = it }
        }
    }

    fun nameError(displayName: String): String? =
        if (isValidProfileName(displayName)) null else "Enter a profile name."

    fun createProfile(displayName: String) {
        if (nameError(displayName) == null) save(ProfileChange.Create(displayName))
    }

    fun selectProfile(profileId: Long) {
        save(ProfileChange.Select(profileId))
    }

    fun renameProfile(profileId: Long, displayName: String) {
        if (nameError(displayName) == null && isActive(profileId)) {
            save(ProfileChange.Rename(profileId, displayName))
        }
    }

    fun deleteProfile(profileId: Long) {
        if (isActive(profileId)) save(ProfileChange.Delete(profileId))
    }

    private fun isActive(profileId: Long): Boolean =
        (uiState.value as? ProfileUiState.Active)?.profile?.id == profileId

    fun retry() {
        val error = uiState.value as? ProfileUiState.Error ?: return
        val change = error.failedChange
        if (change == null) observeSelection() else save(change)
    }

    private fun save(change: ProfileChange) {
        when (val state = uiState.value) {
            ProfileUiState.Loading, ProfileUiState.Saving -> return
            is ProfileUiState.Error -> if (state.failedChange != change) return
            else -> Unit
        }
        observation?.cancel()
        _uiState.value = ProfileUiState.Saving
        viewModelScope.launch {
            var retryChange = change
            try {
                val saved = when (change) {
                    is ProfileChange.Create -> {
                        val id = repository.createProfile(change.displayName)
                        retryChange = ProfileChange.Select(id)
                        repository.selectProfile(id)
                        true
                    }
                    is ProfileChange.Select -> {
                        repository.selectProfile(change.profileId)
                        true
                    }
                    is ProfileChange.Rename -> repository.renameProfile(change.profileId, change.displayName)
                    is ProfileChange.Delete -> repository.deleteProfile(change.profileId)
                }
                // If the row is missing, Retry reloads the list.
                if (saved) observeSelection() else _uiState.value = ProfileUiState.Error()
            } catch (e: Exception) {
                when {
                    e is CancellationException -> throw e
                    e is IllegalArgumentException && retryChange is ProfileChange.Select -> observeSelection()
                    else -> _uiState.value = ProfileUiState.Error(retryChange)
                }
            }
        }
    }
}

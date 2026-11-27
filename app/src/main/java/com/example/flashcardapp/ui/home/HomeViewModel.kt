package com.example.flashcardapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.ProfileRepository
import com.example.flashcardapp.data.ReviewRepository
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ReviewCounts
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class HomeUiState(
    val profile: Profile? = null,
    val counts: ReviewCounts? = null,
    val hasError: Boolean = false
)

class HomeViewModel(
    private val profiles: ProfileRepository,
    private val reviews: ReviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var today = LocalDate.now()
    private var observation: Job? = null

    init {
        observeHome()
    }

    fun refreshDate(localDate: LocalDate = LocalDate.now()) {
        if (today != localDate) {
            today = localDate
            observeHome()
        }
    }

    fun retry() = observeHome()

    private fun observeHome() {
        observation?.cancel()
        _uiState.value = HomeUiState()
        observation = viewModelScope.launch {
            try {
                combine(profiles.selectedProfileId, profiles.observeProfiles()) { id, rows ->
                    rows.find { it.id == id }
                }.distinctUntilChanged().collectLatest { profile ->
                    // Clear the previous workload before waiting for this profile's first count.
                    _uiState.value = HomeUiState(profile)
                    if (profile != null) {
                        reviews.observeReviewCounts(profile.id, today).collect { counts ->
                            _uiState.value = HomeUiState(profile, counts)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HomeUiState(hasError = true)
            }
        }
    }
}

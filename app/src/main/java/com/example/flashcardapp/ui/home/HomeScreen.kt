package com.example.flashcardapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardapp.R
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ReviewCounts
import com.example.flashcardapp.ui.theme.FlashcardAppTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLibrary: (() -> Unit)?,
    onReviewNow: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDate() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeBody(uiState, onLibrary, onReviewNow, viewModel::retry, modifier)
}

@Composable
internal fun HomeBody(
    uiState: HomeUiState,
    onLibrary: (() -> Unit)?,
    onReviewNow: (() -> Unit)?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val profile = uiState.profile
        val counts = uiState.counts
        if (profile != null) {
            Text(profile.displayName, style = MaterialTheme.typography.headlineSmall)
        }
        when {
            uiState.hasError -> {
                Text(stringResource(R.string.workload_error))
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            profile == null || counts == null -> Text(stringResource(R.string.loading_workload))
            else -> {
                Text(stringResource(R.string.new_card_count, counts.newCount))
                Text(stringResource(R.string.due_card_count, counts.dueCount))
                val hasReviews = counts.newCount > 0 || counts.dueCount > 0
                if (!hasReviews) Text(stringResource(R.string.no_reviews_due))
                Button(onClick = { onReviewNow?.invoke() }, enabled = hasReviews && onReviewNow != null) {
                    Text(stringResource(R.string.review_now))
                }
                OutlinedButton(onClick = { onLibrary?.invoke() }, enabled = onLibrary != null) { Text(stringResource(R.string.library)) }
            }
        }
    }
}

@Composable
private fun HomePreview(counts: ReviewCounts) {
    FlashcardAppTheme {
        Surface {
            HomeBody(HomeUiState(Profile(1, "Alice", 0), counts), {}, {}, {})
        }
    }
}

@Preview(name = "Empty workload")
@Composable
private fun EmptyHomePreview() {
    HomePreview(ReviewCounts(0, 0))
}

@Preview(name = "Populated workload")
@Composable
private fun PopulatedHomePreview() {
    HomePreview(ReviewCounts(3, 2))
}

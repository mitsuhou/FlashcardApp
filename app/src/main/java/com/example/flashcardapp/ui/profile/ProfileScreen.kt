package com.example.flashcardapp.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flashcardapp.R
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.ui.theme.FlashcardAppTheme

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    nameError: (String) -> String?,
    onCreate: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (uiState) {
            ProfileUiState.Loading -> Text(stringResource(R.string.loading_profiles))
            ProfileUiState.Saving -> Text(stringResource(R.string.saving_profile))
            is ProfileUiState.Error -> {
                val message = when (val change = uiState.failedChange) {
                    is ProfileChange.Create -> stringResource(R.string.create_failed, change.displayName)
                    is ProfileChange.Rename -> stringResource(R.string.rename_failed, change.displayName)
                    is ProfileChange.Select -> stringResource(R.string.select_failed)
                    is ProfileChange.Delete -> stringResource(R.string.delete_failed)
                    null -> stringResource(R.string.reload_profiles)
                }
                Text(message)
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            ProfileUiState.Create -> ProfileNameEditor("", nameError, onCreate)
            is ProfileUiState.Selector -> ProfileList(
                uiState.profiles, null, nameError, onCreate, onSelect, onRename, onDelete
            )
            is ProfileUiState.Active -> ProfileList(
                uiState.profiles, uiState.profile, nameError, onCreate, onSelect, onRename, onDelete
            )
        }
    }
}

@Composable
private fun ProfileList(
    profiles: List<Profile>,
    active: Profile?,
    nameError: (String) -> String?,
    onCreate: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var creating by rememberSaveable(active?.id) { mutableStateOf(false) }
    var renaming by rememberSaveable(active?.id) { mutableStateOf(false) }
    var confirmId by rememberSaveable(active?.id) { mutableStateOf<Long?>(null) }

    val cancelEditor = {
        creating = false
        renaming = false
    }
    BackHandler(enabled = creating || renaming, onBack = cancelEditor)

    if (creating || renaming) {
        if (renaming && active != null) {
            ProfileNameEditor(active.displayName, nameError) { onRename(active.id, it) }
        } else {
            ProfileNameEditor("", nameError, onCreate)
        }
        TextButton(onClick = cancelEditor) { Text(stringResource(R.string.cancel)) }
    } else {
        Text(stringResource(R.string.profiles), style = MaterialTheme.typography.headlineSmall)
        if (active != null) {
            Text(stringResource(R.string.active_profile), style = MaterialTheme.typography.labelLarge)
            Text(active.displayName, style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { renaming = true }) { Text(stringResource(R.string.rename)) }
            OutlinedButton(onClick = { confirmId = active.id }) { Text(stringResource(R.string.delete_profile)) }
        }
        profiles.filter { it.id != active?.id }.forEach { profile ->
            OutlinedButton(
                onClick = { if (active == null) onSelect(profile.id) else confirmId = profile.id },
                modifier = Modifier.fillMaxWidth()
            ) { Text(profile.displayName) }
        }
        Button(onClick = { creating = true }) { Text(stringResource(R.string.create_profile)) }

        val target = profiles.find { it.id == confirmId }
        if (target != null) {
            val deleting = target.id == active?.id
            ProfileConfirmationDialog(
                profileName = target.displayName,
                deleting = deleting,
                onConfirm = {
                    confirmId = null
                    if (deleting) onDelete(target.id) else onSelect(target.id)
                },
                onDismiss = { confirmId = null }
            )
        }
    }
}

@Composable
private fun ProfileConfirmationDialog(
    profileName: String,
    deleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val action = if (deleting) R.string.delete_profile else R.string.switch_profile
    val message = if (deleting) R.string.confirm_delete_profile else R.string.confirm_switch_profile
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_profile_change)) },
        text = { Text(stringResource(message, profileName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(action)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun ProfileNameEditor(initialName: String, nameError: (String) -> String?, onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var edited by rememberSaveable { mutableStateOf(false) }
    val error = nameError(name)
    Text(stringResource(R.string.profile), style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(
        value = name,
        onValueChange = {
            name = it
            edited = true
        },
        label = { Text(stringResource(R.string.profile_name)) },
        singleLine = true,
        isError = edited && error != null,
        supportingText = if (edited && error != null) ({ Text(error) }) else null,
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { onSave(name) }, enabled = error == null) {
        Text(stringResource(if (initialName.isEmpty()) R.string.create_profile else R.string.save))
    }
}

@Composable
private fun ProfilePreviewContent(state: ProfileUiState) {
    FlashcardAppTheme {
        Surface {
            ProfileScreen(
                uiState = state,
                nameError = { if (it.isBlank()) "Enter a profile name." else null },
                onCreate = {},
                onSelect = {},
                onRename = { _, _ -> },
                onDelete = {},
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Create")
@Composable
private fun ProfileCreatePreview() {
    ProfilePreviewContent(ProfileUiState.Create)
}

@Preview(name = "Selector")
@Composable
private fun ProfileSelectorPreview() {
    val bao = Profile(id = 2, displayName = "Bao", createdAt = 0)
    ProfilePreviewContent(ProfileUiState.Selector(listOf(bao)))
}

@Preview(name = "Active")
@Composable
private fun ProfileActivePreview() {
    val alice = Profile(id = 1, displayName = "Alice", createdAt = 0)
    val bao = Profile(id = 2, displayName = "Bao", createdAt = 0)
    ProfilePreviewContent(ProfileUiState.Active(alice, listOf(alice, bao)))
}

@Preview(name = "Loading")
@Composable
private fun ProfileLoadingPreview() {
    ProfilePreviewContent(ProfileUiState.Loading)
}

@Preview(name = "Saving")
@Composable
private fun ProfileSavingPreview() {
    ProfilePreviewContent(ProfileUiState.Saving)
}

@Preview(name = "Error")
@Composable
private fun ProfileErrorPreview() {
    ProfilePreviewContent(ProfileUiState.Error(ProfileChange.Select(2)))
}

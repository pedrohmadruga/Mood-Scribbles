package com.example.moodscribbles.ui.journal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.Mood
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent: (JournalUiEvent) -> Unit = viewModel::onEvent
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.journal_screen_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) {
                        Text(text = stringResource(R.string.journal_navigate_up))
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    DateRow(
                        formattedDate = uiState.date.format(dateFormatter),
                        onPreviousDay = {
                            onEvent(JournalUiEvent.DateSelected(uiState.date.minusDays(1)))
                        },
                        onNextDay = {
                            onEvent(JournalUiEvent.DateSelected(uiState.date.plusDays(1)))
                        },
                    )
                    MoodRow(
                        selected = uiState.mood,
                        onMoodSelected = { onEvent(JournalUiEvent.MoodSelected(it)) },
                    )
                    Text(
                        text = stringResource(R.string.journal_energy_label, uiState.energyLevel),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = uiState.energyLevel.toFloat(),
                        onValueChange = { value ->
                            onEvent(
                                JournalUiEvent.EnergyChanged(
                                    value.roundToInt().coerceIn(JournalEntry.MIN_ENERGY, JournalEntry.MAX_ENERGY),
                                ),
                            )
                        },
                        valueRange = JournalEntry.MIN_ENERGY.toFloat()..JournalEntry.MAX_ENERGY.toFloat(),
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { onEvent(JournalUiEvent.TitleChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.journal_title_label)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { onEvent(JournalUiEvent.DescriptionChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.journal_description_label)) },
                        minLines = 3,
                    )
                    uiState.saveError?.let { error ->
                        JournalSaveErrorBlock(error = error)
                        TextButton(onClick = { onEvent(JournalUiEvent.ClearSaveErrorClicked) }) {
                            Text(text = stringResource(R.string.journal_dismiss_error))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onEvent(JournalUiEvent.SaveClicked) },
                            enabled = !uiState.isSaving,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = LocalContentColor.current,
                                    )
                                }
                                Text(
                                    text = if (uiState.isExistingEntry) {
                                        stringResource(R.string.journal_save_update)
                                    } else {
                                        stringResource(R.string.journal_save_create)
                                    },
                                )
                            }
                        }
                        TextButton(
                            onClick = { onEvent(JournalUiEvent.ReloadFromRepositoryClicked) },
                            enabled = !uiState.isSaving,
                        ) {
                            Text(text = stringResource(R.string.journal_reload_from_repository))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DateRow(
    formattedDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPreviousDay) {
            Text(text = stringResource(R.string.journal_date_previous))
        }
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onNextDay) {
            Text(text = stringResource(R.string.journal_date_next))
        }
    }
}

@Composable
private fun MoodRow(
    selected: Mood,
    onMoodSelected: (Mood) -> Unit,
) {
    Text(
        text = stringResource(R.string.journal_mood_label),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Mood.entries.forEach { mood ->
            FilterChip(
                selected = mood == selected,
                onClick = { onMoodSelected(mood) },
                label = { Text(text = moodLabel(mood)) },
            )
        }
    }
}

@Composable
private fun moodLabel(mood: Mood): String = when (mood) {
    Mood.VERY_SAD -> stringResource(R.string.mood_very_sad)
    Mood.SAD -> stringResource(R.string.mood_sad)
    Mood.NEUTRAL -> stringResource(R.string.mood_neutral)
    Mood.HAPPY -> stringResource(R.string.mood_happy)
    Mood.VERY_HAPPY -> stringResource(R.string.mood_very_happy)
}

@Composable
private fun JournalSaveErrorBlock(error: JournalSaveError) {
    val errorColor = MaterialTheme.colorScheme.error
    val style = MaterialTheme.typography.bodyMedium
    when (error) {
        is JournalSaveError.ValidationFailed -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                error.violations.forEach { violation ->
                    Text(
                        text = violationLabel(violation),
                        color = errorColor,
                        style = style,
                    )
                }
            }
        }
        JournalSaveError.AlreadyExistsForDate -> {
            Text(
                text = stringResource(R.string.journal_error_already_exists),
                color = errorColor,
                style = style,
            )
        }
        JournalSaveError.NotFoundForDate -> {
            Text(
                text = stringResource(R.string.journal_error_not_found),
                color = errorColor,
                style = style,
            )
        }
    }
}

@Composable
private fun violationLabel(v: JournalEntryRuleViolation): String = when (v) {
    is JournalEntryRuleViolation.EnergyOutOfRange -> stringResource(R.string.journal_error_energy, v.value)
    JournalEntryRuleViolation.UpdatedBeforeCreated -> stringResource(R.string.journal_error_updated_before_created)
    JournalEntryRuleViolation.AlreadyExistsForDate -> stringResource(R.string.journal_error_already_exists)
    JournalEntryRuleViolation.EntryNotFoundForDate -> stringResource(R.string.journal_error_not_found)
    is JournalEntryRuleViolation.EntryIdDoesNotMatchDate -> stringResource(
        R.string.journal_error_id_mismatch,
        v.entryId,
        v.existingId,
    )
}

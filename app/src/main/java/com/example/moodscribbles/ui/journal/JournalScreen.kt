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
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateUp: () -> Unit,
    initialDate: LocalDate? = null,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent: (JournalUiEvent) -> Unit = viewModel::onEvent
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val today = LocalDate.now()

    LaunchedEffect(initialDate) {
        val requestedDate = initialDate ?: return@LaunchedEffect
        if (requestedDate != uiState.date) {
            onEvent(JournalUiEvent.DateSelected(requestedDate))
        }
    }

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
                        date = uiState.date,
                        formattedDate = uiState.date.format(dateFormatter),
                        today = today,
                        onPreviousDay = {
                            onEvent(JournalUiEvent.DateSelected(uiState.date.minusDays(1)))
                        },
                        onNextDay = {
                            onEvent(JournalUiEvent.DateSelected(uiState.date.plusDays(1)))
                        },
                        onDatePicked = { pickedDate ->
                            onEvent(JournalUiEvent.DateSelected(pickedDate))
                        },
                        onTodayClick = {
                            onEvent(JournalUiEvent.DateSelected(today))
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
                    EmotionRow(
                        selectedEmotionName = uiState.emotion.name,
                        options = uiState.emotionOptions,
                        onEmotionSelected = { onEvent(JournalUiEvent.EmotionSelected(it)) },
                    )
                    TagsSection(
                        suggestions = uiState.tagSuggestions,
                        selectedTags = uiState.tags,
                        newTagInput = uiState.newTagInput,
                        onNewTagInputChanged = { onEvent(JournalUiEvent.NewTagInputChanged(it)) },
                        onSuggestedTagToggled = { onEvent(JournalUiEvent.SuggestedTagToggled(it)) },
                        onSelectedTagRemoved = { onEvent(JournalUiEvent.SelectedTagRemoved(it)) },
                        onAddTypedTagClicked = { onEvent(JournalUiEvent.AddTypedTagClicked) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRow(
    date: LocalDate,
    formattedDate: String,
    today: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onTodayClick: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val pickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPreviousDay) {
            Text(text = stringResource(R.string.journal_date_previous))
        }
        TextButton(onClick = { showDatePicker = true }) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        TextButton(onClick = onNextDay) {
            Text(text = stringResource(R.string.journal_date_next))
        }
    }
    if (date != today) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = onTodayClick) {
                Text(text = stringResource(R.string.journal_today))
            }
        }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis
                            ?.let { millis ->
                                val picked = Instant
                                    .ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                onDatePicked(picked)
                            }
                        showDatePicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.journal_date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.journal_date_picker_cancel))
                }
            },
        ) {
            androidx.compose.material3.DatePicker(state = pickerState)
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
private fun EmotionRow(
    selectedEmotionName: String,
    options: List<Emotion>,
    onEmotionSelected: (Emotion) -> Unit,
) {
    Text(
        text = stringResource(R.string.journal_emotion_label),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { emotion ->
            val isSelected = emotion.name.equals(selectedEmotionName, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onEmotionSelected(emotion) },
                label = { Text(text = localizedEmotionLabel(emotion.name)) },
            )
        }
    }
}

@Composable
private fun TagsSection(
    suggestions: List<Tag>,
    selectedTags: List<Tag>,
    newTagInput: String,
    onNewTagInputChanged: (String) -> Unit,
    onSuggestedTagToggled: (Tag) -> Unit,
    onSelectedTagRemoved: (Tag) -> Unit,
    onAddTypedTagClicked: () -> Unit,
) {
    Text(
        text = stringResource(R.string.journal_tags_label),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suggestions.forEach { suggestion ->
            val isSelected = selectedTags.any { it.name.equals(suggestion.name, ignoreCase = true) }
            FilterChip(
                selected = isSelected,
                onClick = { onSuggestedTagToggled(suggestion) },
                label = { Text(text = localizedSuggestedTagLabel(suggestion.name)) },
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = newTagInput,
            onValueChange = onNewTagInputChanged,
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.journal_new_tag_label)) },
            singleLine = true,
        )
        Button(onClick = onAddTypedTagClicked) {
            Text(text = stringResource(R.string.journal_add_tag))
        }
    }
    if (selectedTags.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selectedTags.forEach { tag ->
                FilterChip(
                    selected = true,
                    onClick = { onSelectedTagRemoved(tag) },
                    label = { Text(text = localizedSuggestedTagLabel(tag.name)) },
                )
            }
        }
    }
}

@Composable
private fun localizedEmotionLabel(rawName: String): String = when (rawName.trim().lowercase()) {
    "happy" -> stringResource(R.string.journal_emotion_happy)
    "content" -> stringResource(R.string.journal_emotion_content)
    "neutral" -> stringResource(R.string.journal_emotion_neutral)
    "anxious" -> stringResource(R.string.journal_emotion_anxious)
    "sad" -> stringResource(R.string.journal_emotion_sad)
    "frustrated" -> stringResource(R.string.journal_emotion_frustrated)
    else -> rawName
}

@Composable
private fun localizedSuggestedTagLabel(rawName: String): String = when (rawName.trim().lowercase()) {
    "work" -> stringResource(R.string.journal_tag_work)
    "family" -> stringResource(R.string.journal_tag_family)
    "health" -> stringResource(R.string.journal_tag_health)
    "friends" -> stringResource(R.string.journal_tag_friends)
    "study" -> stringResource(R.string.journal_tag_study)
    "exercise" -> stringResource(R.string.journal_tag_exercise)
    else -> rawName
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

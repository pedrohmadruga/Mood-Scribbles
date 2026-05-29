package com.example.moodscribbles.ui.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import com.example.moodscribbles.ui.localizedEmotionLabel
import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import com.example.moodscribbles.ui.calendar.calendarEmoji
import com.example.moodscribbles.ui.calendar.calendarTint
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
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(initialDate) {
        val requestedDate = initialDate ?: return@LaunchedEffect
        if (requestedDate != uiState.date) {
            onEvent(JournalUiEvent.DateSelected(requestedDate))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collectLatest { onNavigateUp() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.journal_screen_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) {
                        Text(text = "<", color = colorScheme.primary)
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onNavigateUp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant,
                            contentColor = colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Text(text = "× ${stringResource(R.string.journal_date_picker_cancel)}")
                    }
                    Button(
                        onClick = { onEvent(JournalUiEvent.SaveClicked) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current,
                            )
                        } else {
                            Text(
                                text = "✓ ${if (uiState.isExistingEntry) stringResource(R.string.journal_save_update) else stringResource(R.string.journal_save_create)}",
                            )
                        }
                    }
                }
            }
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
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Spacer(Modifier.height(4.dp))

                    DateRow(
                        date = uiState.date,
                        formattedDate = uiState.date.format(dateFormatter),
                        today = today,
                        onPreviousDay = { onEvent(JournalUiEvent.DateSelected(uiState.date.minusDays(1))) },
                        onNextDay = { onEvent(JournalUiEvent.DateSelected(uiState.date.plusDays(1))) },
                        onDatePicked = { onEvent(JournalUiEvent.DateSelected(it)) },
                        onTodayClick = { onEvent(JournalUiEvent.DateSelected(today)) },
                    )

                    MoodSection(
                        selected = uiState.mood,
                        onMoodSelected = { onEvent(JournalUiEvent.MoodSelected(it)) },
                    )

                    EnergySection(
                        energyLevel = uiState.energyLevel,
                        onEnergyChanged = { onEvent(JournalUiEvent.EnergyChanged(it)) },
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionLabel(emoji = "Aa", label = stringResource(R.string.journal_title_label))
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { onEvent(JournalUiEvent.TitleChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.journal_title_label)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionLabel(emoji = "📝", label = stringResource(R.string.journal_description_label))
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { onEvent(JournalUiEvent.DescriptionChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.journal_description_label)) },
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    Text(
                                        text = stringResource(R.string.journal_notes_char_count, uiState.description.length),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                        )
                    }

                    EmotionSection(
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

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(emoji: String, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    val colorScheme = MaterialTheme.colorScheme
    val pickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPreviousDay) {
            Text(text = "< ${stringResource(R.string.journal_date_previous)}")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = { showDatePicker = true }) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (date == today) {
                Text(
                    text = stringResource(R.string.journal_today).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
        TextButton(onClick = onNextDay) {
            Text(text = "${stringResource(R.string.journal_date_next)} >")
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
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
private fun MoodSection(
    selected: Mood,
    onMoodSelected: (Mood) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(emoji = "😊", label = stringResource(R.string.journal_mood_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Mood.entries.forEach { mood ->
                val isSelected = mood == selected
                Surface(
                    onClick = { onMoodSelected(mood) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) mood.calendarTint().copy(alpha = 0.25f) else colorScheme.surfaceVariant,
                    border = if (isSelected) BorderStroke(1.5.dp, mood.calendarTint()) else null,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(text = mood.calendarEmoji(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = moodLabel(mood),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnergySection(
    energyLevel: Int,
    onEnergyChanged: (Int) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val amber = Color(0xFFFFB300)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        amber.copy(alpha = 0.25f),
                        Color(0xFFFF6F00).copy(alpha = 0.10f),
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "⚡ ${stringResource(R.string.day_detail_energy_section).uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = amber.copy(alpha = 0.20f),
                ) {
                    Text(
                        text = "$energyLevel",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }
            }
            Slider(
                value = energyLevel.toFloat(),
                onValueChange = { onEnergyChanged(it.roundToInt().coerceIn(JournalEntry.MIN_ENERGY, JournalEntry.MAX_ENERGY)) },
                valueRange = JournalEntry.MIN_ENERGY.toFloat()..JournalEntry.MAX_ENERGY.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = amber,
                    activeTrackColor = amber,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_gauge_low_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.dashboard_gauge_high_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmotionSection(
    selectedEmotionName: String,
    options: List<Emotion>,
    onEmotionSelected: (Emotion) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(emoji = "♥", label = stringResource(R.string.journal_emotion_label))
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
    val colorScheme = MaterialTheme.colorScheme
    val unselectedSuggestions = suggestions.filter { s ->
        selectedTags.none { it.name.equals(s.name, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF26C6DA).copy(alpha = 0.22f),
                        Color(0xFF00897B).copy(alpha = 0.10f),
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(emoji = "🏷", label = stringResource(R.string.journal_tags_label))
                if (selectedTags.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colorScheme.primary.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = stringResource(R.string.journal_tags_selected, selectedTags.size),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary,
                        )
                    }
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
                            label = { Text(text = "#${localizedSuggestedTagLabel(tag.name)}") },
                            trailingIcon = {
                                Text(text = "×", style = MaterialTheme.typography.labelLarge)
                            },
                        )
                    }
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
                    placeholder = { Text(stringResource(R.string.journal_new_tag_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Button(
                    onClick = onAddTypedTagClicked,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Light,
                    )
                }
            }

            if (unselectedSuggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.journal_tags_suggested).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        unselectedSuggestions.forEach { suggestion ->
                            FilterChip(
                                selected = false,
                                onClick = { onSuggestedTagToggled(suggestion) },
                                label = { Text(text = "+ ${localizedSuggestedTagLabel(suggestion.name)}") },
                            )
                        }
                    }
                }
            }
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
                    Text(text = violationLabel(violation), color = errorColor, style = style)
                }
            }
        }
        JournalSaveError.AlreadyExistsForDate -> {
            Text(text = stringResource(R.string.journal_error_already_exists), color = errorColor, style = style)
        }
        JournalSaveError.NotFoundForDate -> {
            Text(text = stringResource(R.string.journal_error_not_found), color = errorColor, style = style)
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

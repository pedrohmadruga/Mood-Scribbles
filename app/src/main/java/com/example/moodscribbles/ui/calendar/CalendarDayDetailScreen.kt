package com.example.moodscribbles.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDayDetailScreen(
    date: LocalDate,
    onNavigateUp: () -> Unit,
    onOpenJournalForDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarDayDetailViewModel = koinViewModel { parametersOf(date) },
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val fullDateFormatter = rememberFullDateFormatter()
    val formattedDate = date.format(fullDateFormatter)
    val navigateUpLabel = stringResource(R.string.calendar_navigate_up_cd)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.calendar_day_detail_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.semantics { contentDescription = navigateUpLabel },
                    ) {
                        Text("<", color = colorScheme.primary)
                    }
                },
            )
        },
    ) { innerPadding ->
        CalendarDayDetailBody(
            formattedDate = formattedDate,
            entry = entry,
            onOpenJournal = { onOpenJournalForDate(date) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarDayDetailBody(
    formattedDate: String,
    entry: JournalEntry?,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onBackground,
        )
        if (entry != null) {
            val mood = entry.mood
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = mood.calendarTint().copy(alpha = 0.35f),
                ) {
                    Text(
                        text = mood.calendarEmoji(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Text(
                    text = stringResource(
                        R.string.calendar_sheet_saved_mood_line,
                        moodDisplayLabel(mood),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.journal_energy_label, entry.energyLevel),
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.journal_emotion_label),
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
            val emotionName = entry.emotion.name.trim()
            Text(
                text = if (emotionName.isEmpty()) {
                    stringResource(R.string.calendar_day_detail_not_set)
                } else {
                    localizedEmotionLabel(emotionName)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.journal_tags_label),
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
            if (entry.tags.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_day_detail_no_tags),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    entry.tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
            val titleText = entry.title?.trim().orEmpty()
            val descriptionText = entry.description?.trim().orEmpty()
            Text(
                text = stringResource(R.string.calendar_day_detail_journal_heading),
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
            if (titleText.isEmpty() && descriptionText.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_day_detail_no_notes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (titleText.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.journal_title_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onBackground,
                            )
                        }
                    }
                    if (descriptionText.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.journal_description_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = descriptionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = stringResource(R.string.calendar_sheet_no_entry),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onOpenJournal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.calendar_edit_entry_for_day))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun moodDisplayLabel(mood: Mood): String = when (mood) {
    Mood.VERY_SAD -> stringResource(R.string.mood_very_sad)
    Mood.SAD -> stringResource(R.string.mood_sad)
    Mood.NEUTRAL -> stringResource(R.string.mood_neutral)
    Mood.HAPPY -> stringResource(R.string.mood_happy)
    Mood.VERY_HAPPY -> stringResource(R.string.mood_very_happy)
}

@Composable
private fun TagChip(tag: Tag) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceVariant,
    ) {
        Text(
            text = localizedTagLabel(tag.name),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
        )
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
private fun localizedTagLabel(rawName: String): String = when (rawName.trim().lowercase()) {
    "work" -> stringResource(R.string.journal_tag_work)
    "family" -> stringResource(R.string.journal_tag_family)
    "health" -> stringResource(R.string.journal_tag_health)
    "friends" -> stringResource(R.string.journal_tag_friends)
    "study" -> stringResource(R.string.journal_tag_study)
    "exercise" -> stringResource(R.string.journal_tag_exercise)
    else -> rawName
}

@Composable
private fun rememberFullDateFormatter(): DateTimeFormatter {
    return remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
    }
}

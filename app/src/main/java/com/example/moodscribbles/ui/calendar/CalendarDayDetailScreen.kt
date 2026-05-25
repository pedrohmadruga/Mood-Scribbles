package com.example.moodscribbles.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.util.Locale

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
    val fullDateFormatter = rememberFullDateFormatter()
    val formattedDate = date.format(fullDateFormatter)
    val navigateUpLabel = stringResource(R.string.calendar_navigate_up_cd)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
                        Text(
                            text = "<",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        CalendarDayDetailBody(
            date = date,
            formattedDate = formattedDate,
            entry = entry,
            onOpenJournal = { onOpenJournalForDate(date) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun CalendarDayDetailBody(
    date: LocalDate,
    formattedDate: String,
    entry: JournalEntry?,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val weekdayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(50),
                color = colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "📅  $weekdayName",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            if (entry != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.day_detail_saved_badge),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
        )

        if (entry != null) {
            DayDetailWithEntry(entry = entry, onOpenJournal = onOpenJournal)
        } else {
            DayDetailEmpty(onOpenJournal = onOpenJournal)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DayDetailWithEntry(
    entry: JournalEntry,
    onOpenJournal: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val mood = entry.mood

    // Mood card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = mood.calendarTint().copy(alpha = 0.18f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = mood.calendarTint().copy(alpha = 0.35f),
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 1.5.dp,
                        color = mood.calendarTint().copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                    ),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = mood.calendarEmoji(), style = MaterialTheme.typography.headlineMedium)
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.journal_mood_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = moodDisplayLabel(mood),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
            }
        }
    }

    // Energy + Emotion row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Energy — amber/yellow
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFB300).copy(alpha = 0.30f),
                            Color(0xFFFF6F00).copy(alpha = 0.12f),
                        )
                    )
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "⚡ ${stringResource(R.string.day_detail_energy_section).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.day_detail_energy_of_100, entry.energyLevel),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                LinearProgressIndicator(
                    progress = { entry.energyLevel / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Color(0xFFFFB300),
                    trackColor = colorScheme.outline.copy(alpha = 0.3f),
                )
            }
        }

        // Emotion — pink/rose
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEC407A).copy(alpha = 0.30f),
                            Color(0xFF9C27B0).copy(alpha = 0.12f),
                        )
                    )
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "♥ ${stringResource(R.string.journal_emotion_label).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                val emotionName = entry.emotion.name.trim()
                Text(
                    text = if (emotionName.isEmpty()) stringResource(R.string.calendar_day_detail_not_set)
                    else localizedEmotionLabel(emotionName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.day_detail_emotion_primary),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // Tags — teal/cyan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF26C6DA).copy(alpha = 0.25f),
                        Color(0xFF00897B).copy(alpha = 0.12f),
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "🏷 ${stringResource(R.string.journal_tags_label).uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
            )
            if (entry.tags.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_day_detail_no_tags),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    entry.tags.forEach { tag -> TagChip(tag = tag) }
                }
            }
        }
    }

    // Diary — primary (blue/indigo do tema)
    val titleText = entry.title?.trim().orEmpty()
    val descriptionText = entry.description?.trim().orEmpty()
    val hasDiaryContent = titleText.isNotEmpty() || descriptionText.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.28f),
                        colorScheme.primaryContainer.copy(alpha = 0.15f),
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "📋 ${stringResource(R.string.calendar_day_detail_journal_heading).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                if (!hasDiaryContent) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colorScheme.outline.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = stringResource(R.string.day_detail_diary_draft),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (!hasDiaryContent) {
                Text(
                    text = stringResource(R.string.day_detail_diary_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            } else {
                if (titleText.isNotEmpty()) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                    )
                }
                if (descriptionText.isNotEmpty()) {
                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    Button(
        onClick = onOpenJournal,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.calendar_edit_entry_for_day))
    }
}

@Composable
private fun DayDetailEmpty(onOpenJournal: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.40f),
                        colorScheme.primaryContainer.copy(alpha = 0.55f),
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.primary.copy(alpha = 0.25f),
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "✨", style = MaterialTheme.typography.headlineMedium)
                }
            }
            Text(
                text = stringResource(R.string.day_detail_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.day_detail_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }

    Text(
        text = "✨  ${stringResource(R.string.day_detail_you_can_track).uppercase()}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.50f),
                        colorScheme.primaryContainer.copy(alpha = 0.25f),
                    )
                )
            ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            TrackableItem(
                emoji = "😊",
                label = stringResource(R.string.journal_mood_label),
                desc = stringResource(R.string.day_detail_track_mood_desc),
            )
            TrackableItem(
                emoji = "⚡",
                label = stringResource(R.string.day_detail_energy_section),
                desc = stringResource(R.string.day_detail_track_energy_desc),
            )
            TrackableItem(
                emoji = "♥",
                label = stringResource(R.string.journal_emotion_label),
                desc = stringResource(R.string.day_detail_track_emotion_desc),
            )
            TrackableItem(
                emoji = "🏷",
                label = stringResource(R.string.journal_tags_label),
                desc = stringResource(R.string.day_detail_track_tags_desc),
            )
            TrackableItem(
                emoji = "📋",
                label = stringResource(R.string.calendar_day_detail_journal_heading),
                desc = stringResource(R.string.day_detail_track_notes_desc),
                isLast = true,
            )
        }
    }

    Button(
        onClick = onOpenJournal,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.calendar_open_journal_for_day))
    }
}

@Composable
private fun TrackableItem(
    emoji: String,
    label: String,
    desc: String,
    isLast: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = colorScheme.surface,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 60.dp),
            color = colorScheme.outline.copy(alpha = 0.15f),
        )
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
        color = colorScheme.surface,
    ) {
        Text(
            text = "#${localizedTagLabel(tag.name)}",
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
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    }
}

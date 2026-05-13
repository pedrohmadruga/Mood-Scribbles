package com.example.moodscribbles.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.ui.calendar.calendarEmoji
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalHistoryScreen(
    onOpenDayDetail: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val monthTitleFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }
    val rowDateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    }
    val prevMonthCd = stringResource(R.string.calendar_prev_month_cd)
    val nextMonthCd = stringResource(R.string.calendar_next_month_cd)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.history_screen_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.filterMode == HistoryFilterMode.BY_MONTH,
                    onClick = { viewModel.setFilterMode(HistoryFilterMode.BY_MONTH) },
                    label = { Text(stringResource(R.string.history_filter_by_month)) },
                )
                FilterChip(
                    selected = uiState.filterMode == HistoryFilterMode.LAST_30_DAYS,
                    onClick = { viewModel.setFilterMode(HistoryFilterMode.LAST_30_DAYS) },
                    label = { Text(stringResource(R.string.history_filter_last_30_days)) },
                )
            }
            if (uiState.filterMode == HistoryFilterMode.BY_MONTH) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { viewModel.onPreviousMonth() },
                        modifier = Modifier.semantics { contentDescription = prevMonthCd },
                    ) {
                        Text("<", color = colorScheme.primary)
                    }
                    Text(
                        text = uiState.visibleMonth.format(monthTitleFormatter)
                            .replaceFirstChar { ch ->
                                if (ch.isLowerCase()) {
                                    ch.titlecase(Locale.getDefault())
                                } else {
                                    ch.toString()
                                }
                            },
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onBackground,
                    )
                    TextButton(
                        onClick = { viewModel.onNextMonth() },
                        modifier = Modifier.semantics { contentDescription = nextMonthCd },
                    ) {
                        Text(">", color = colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_empty_state),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    items(
                        items = uiState.entries,
                        key = { it.id },
                    ) { entry ->
                        HistoryEntryRow(
                            entry = entry,
                            rowDateFormatter = rowDateFormatter,
                            onClick = { onOpenDayDetail(entry.date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryRow(
    entry: JournalEntry,
    rowDateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val openDetail = stringResource(R.string.history_open_day_detail_cd)
    val dateLine = entry.date.format(rowDateFormatter)
    val titlePreview = entry.title?.trim().orEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$dateLine, $openDetail" }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = entry.mood.calendarEmoji(),
                style = MaterialTheme.typography.headlineSmall,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateLine,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = moodDisplayLabel(entry.mood),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                if (titlePreview.isNotEmpty()) {
                    Text(
                        text = titlePreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
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

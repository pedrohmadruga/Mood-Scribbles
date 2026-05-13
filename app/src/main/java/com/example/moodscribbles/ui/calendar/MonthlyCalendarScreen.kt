package com.example.moodscribbles.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

private const val GRID_CELLS = 42

private fun calendarGridCells(yearMonth: YearMonth): List<LocalDate?> {
    val weekFields = WeekFields.of(Locale.getDefault())
    val firstDayOfWeek: DayOfWeek = weekFields.firstDayOfWeek
    val firstOfMonth = yearMonth.atDay(1)
    val gridStart = firstOfMonth.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    return (0 until GRID_CELLS).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        if (YearMonth.from(date) == yearMonth) date else null
    }
}

@Composable
fun MonthlyCalendarScreen(
    onOpenDayDetail: (LocalDate) -> Unit,
    onOpenJournalForDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val visibleMonth = uiState.visibleMonth

    val fabLabel = stringResource(R.string.calendar_fab_today_content_description)
    val prevMonthCd = stringResource(R.string.calendar_prev_month_cd)
    val nextMonthCd = stringResource(R.string.calendar_next_month_cd)
    val monthTitleFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }
    val weekFields = remember { WeekFields.of(Locale.getDefault()) }
    val weekdayLabels = remember(weekFields) {
        generateSequence(weekFields.firstDayOfWeek) { it.plus(1) }
            .take(7)
            .map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
            .toList()
    }
    val gridCells = remember(visibleMonth) { calendarGridCells(visibleMonth) }
    val today = LocalDate.now()
    val colorScheme = MaterialTheme.colorScheme

    fun onFabClick() {
        if (YearMonth.from(today) == visibleMonth) {
            onOpenDayDetail(today)
        } else {
            onOpenJournalForDate(today)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFabClick() },
                modifier = Modifier.semantics { contentDescription = fabLabel },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.calendar_screen_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.calendar_data_banner),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                    text = visibleMonth.format(monthTitleFormatter)
                        .replaceFirstChar { ch ->
                            if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                weekdayLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(
                    count = gridCells.size,
                    key = { index -> gridCells[index]?.toString() ?: "e$index" },
                ) { index ->
                    val cellDate = gridCells[index]
                    if (cellDate == null) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    } else {
                        val entry = uiState.entriesByDate[cellDate]
                        val isToday = cellDate == today
                        val cellBackground = if (entry != null) {
                            lerp(
                                colorScheme.surfaceVariant,
                                entry.mood.calendarTint(),
                                0.58f,
                            )
                        } else {
                            colorScheme.surfaceVariant
                        }
                        val borderModifier = if (entry != null) {
                            Modifier.border(
                                1.5.dp,
                                colorScheme.outline,
                                RoundedCornerShape(12.dp),
                            )
                        } else {
                            Modifier
                        }
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cellBackground)
                                .then(borderModifier)
                                .clickable { onOpenDayDetail(cellDate) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = cellDate.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isToday) colorScheme.primary else colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                )
                                Text(
                                    text = if (entry != null) {
                                        entry.mood.calendarEmoji()
                                    } else {
                                        stringResource(R.string.calendar_empty_day_symbol)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

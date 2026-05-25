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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

    var showMonthPicker by rememberSaveable { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            current = visibleMonth,
            onConfirm = { selected ->
                viewModel.onMonthSelected(selected)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (YearMonth.from(today) == visibleMonth) {
                        onOpenDayDetail(today)
                    } else {
                        onOpenJournalForDate(today)
                    }
                },
                modifier = Modifier.semantics { contentDescription = fabLabel },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
            ) {
                Text(text = "+", style = MaterialTheme.typography.headlineMedium)
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
                TextButton(onClick = { showMonthPicker = true }) {
                    Text(
                        text = visibleMonth.month
                            .getDisplayName(TextStyle.FULL, Locale.getDefault())
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } +
                            " ${visibleMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onBackground,
                    )
                }
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
                        Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)))
                    } else {
                        val entry = uiState.entriesByDate[cellDate]
                        val isToday = cellDate == today
                        val cellBackground = if (entry != null) {
                            lerp(colorScheme.surfaceVariant, entry.mood.calendarTint(), 0.58f)
                        } else {
                            colorScheme.surfaceVariant
                        }
                        val borderModifier = if (entry != null) {
                            Modifier.border(1.5.dp, colorScheme.outline, RoundedCornerShape(12.dp))
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
                                )
                                Text(
                                    text = if (entry != null) entry.mood.calendarEmoji()
                                    else stringResource(R.string.calendar_empty_day_symbol),
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

@Composable
private fun MonthYearPickerDialog(
    current: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedYear by rememberSaveable { mutableStateOf(current.year) }
    var selectedMonth by rememberSaveable { mutableStateOf(current.monthValue) }
    val colorScheme = MaterialTheme.colorScheme
    val months = remember {
        (1..12).map { m ->
            YearMonth.of(2000, m).month
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.calendar_month_picker_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { selectedYear-- }) {
                        Text("<", color = colorScheme.primary)
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onBackground,
                    )
                    TextButton(onClick = { selectedYear++ }) {
                        Text(">", color = colorScheme.primary)
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(12) { index ->
                        val monthNum = index + 1
                        val isSelected = monthNum == selectedMonth
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedMonth = monthNum },
                        ) {
                            Text(
                                text = months[index],
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) }) {
                Text(stringResource(R.string.journal_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.journal_date_picker_cancel))
            }
        },
    )
}

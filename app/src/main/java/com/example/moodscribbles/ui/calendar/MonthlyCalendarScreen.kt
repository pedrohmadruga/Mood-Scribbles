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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.ui.prototype.PrototypeColors
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyCalendarScreen(
    onOpenJournalForDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDaySheet by remember { mutableStateOf(false) }

    val visibleMonth = uiState.visibleMonth

    LaunchedEffect(visibleMonth) {
        showDaySheet = false
        selectedDate = null
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fabLabel = stringResource(R.string.calendar_fab_today_content_description)
    val prevMonthCd = stringResource(R.string.calendar_prev_month_cd)
    val nextMonthCd = stringResource(R.string.calendar_next_month_cd)
    val monthTitleFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }
    val fullDateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
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

    fun openSheetForDate(date: LocalDate) {
        selectedDate = date
        showDaySheet = true
    }

    fun dismissSheet() {
        showDaySheet = false
        selectedDate = null
    }

    fun onFabClick() {
        if (YearMonth.from(today) == visibleMonth) {
            openSheetForDate(today)
        } else {
            onOpenJournalForDate(today)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PrototypeColors.background,
        contentColor = PrototypeColors.onBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFabClick() },
                modifier = Modifier.semantics { contentDescription = fabLabel },
                containerColor = PrototypeColors.accent,
                contentColor = PrototypeColors.background,
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
                color = PrototypeColors.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.calendar_data_banner),
                style = MaterialTheme.typography.bodySmall,
                color = PrototypeColors.onSurfaceMuted,
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
                    Text("<", color = PrototypeColors.accent)
                }
                Text(
                    text = visibleMonth.format(monthTitleFormatter)
                        .replaceFirstChar { ch ->
                            if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color = PrototypeColors.onBackground,
                )
                TextButton(
                    onClick = { viewModel.onNextMonth() },
                    modifier = Modifier.semantics { contentDescription = nextMonthCd },
                ) {
                    Text(">", color = PrototypeColors.accent)
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
                        color = PrototypeColors.onSurfaceMuted,
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
                        val isSelected = showDaySheet && cellDate == selectedDate
                        val cellBackground = if (entry != null) {
                            lerp(
                                PrototypeColors.surfaceCard,
                                entry.mood.calendarTint(),
                                0.58f,
                            )
                        } else {
                            PrototypeColors.surfaceCard
                        }
                        val borderModifier = when {
                            isSelected -> Modifier.border(
                                2.dp,
                                PrototypeColors.selectedBorder,
                                RoundedCornerShape(12.dp),
                            )
                            entry != null -> Modifier.border(
                                1.5.dp,
                                PrototypeColors.accent,
                                RoundedCornerShape(12.dp),
                            )
                            else -> Modifier
                        }
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cellBackground)
                                .then(borderModifier)
                                .clickable { openSheetForDate(cellDate) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = cellDate.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isToday) PrototypeColors.accent else PrototypeColors.onBackground,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                )
                                Text(
                                    text = if (entry != null) {
                                        entry.mood.calendarEmoji()
                                    } else {
                                        stringResource(R.string.calendar_empty_day_symbol)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDaySheet && selectedDate != null) {
        val date = selectedDate!!
        val entryForDay = uiState.entriesByDate[date]
        ModalBottomSheet(
            onDismissRequest = { dismissSheet() },
            sheetState = sheetState,
            containerColor = PrototypeColors.surface,
            contentColor = PrototypeColors.onBackground,
        ) {
            CalendarDaySheetContent(
                formattedDate = date.format(fullDateFormatter),
                entry = entryForDay,
                onOpenJournal = {
                    dismissSheet()
                    onOpenJournalForDate(date)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            )
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

@Composable
private fun CalendarDaySheetContent(
    formattedDate: String,
    entry: JournalEntry?,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.titleLarge,
            color = PrototypeColors.onBackground,
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Text(
                    text = stringResource(
                        R.string.calendar_sheet_saved_mood_line,
                        moodDisplayLabel(mood),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrototypeColors.onSurfaceMuted,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.calendar_sheet_no_entry),
                style = MaterialTheme.typography.bodyMedium,
                color = PrototypeColors.onSurfaceMuted,
            )
        }
        Button(onClick = onOpenJournal) {
            Text(text = stringResource(R.string.calendar_open_journal_for_day))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

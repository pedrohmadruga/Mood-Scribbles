package com.example.moodscribbles.ui.prototype

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R
import java.time.LocalDate

/**
 * Skeleton: calendar grid; day details open in a slide-up [ModalBottomSheet] after tapping a day.
 * Center FAB opens the sheet for **today** (day-of-month clamped to the stub grid 1–30).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHomePrototype(
    onOpenMoodEntry: () -> Unit,
    onOpenJournalStep: () -> Unit,
    onOpenFunctionalJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var monthWeek by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var showDaySheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fabLabel = stringResource(R.string.prototype_fab_add_today)

    fun openSheetForDay(day: Int) {
        selectedDay = day
        showDaySheet = true
    }

    fun dismissSheet() {
        showDaySheet = false
        selectedDay = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PrototypeColors.background,
        contentColor = PrototypeColors.onBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val todayDom = LocalDate.now().dayOfMonth.coerceIn(1, 30)
                    openSheetForDay(todayDom)
                },
                modifier = Modifier.semantics {
                    contentDescription = fabLabel
                },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.prototype_app_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrototypeColors.onBackground,
                )
                IconButton(onClick = { /* biometric stub */ }) {
                    Text(text = "◉", color = PrototypeColors.onBackground)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { /* prev month stub */ }) { Text("<", color = PrototypeColors.accent) }
                Text(
                    text = stringResource(R.string.prototype_calendar_month),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = PrototypeColors.onBackground,
                )
                TextButton(onClick = { /* next month stub */ }) { Text(">", color = PrototypeColors.accent) }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                FilterChip(
                    selected = monthWeek == 0,
                    onClick = { monthWeek = 0 },
                    label = { Text(stringResource(R.string.prototype_view_month)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrototypeColors.accent.copy(alpha = 0.35f),
                        labelColor = PrototypeColors.onBackground,
                    ),
                )
                Spacer(modifier = Modifier.padding(8.dp))
                FilterChip(
                    selected = monthWeek == 1,
                    onClick = { monthWeek = 1 },
                    label = { Text(stringResource(R.string.prototype_view_week)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrototypeColors.accent.copy(alpha = 0.35f),
                        labelColor = PrototypeColors.onBackground,
                    ),
                )
            }
            Text(
                text = stringResource(R.string.prototype_weekday_row),
                style = MaterialTheme.typography.labelSmall,
                color = PrototypeColors.onSurfaceMuted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(35) { index ->
                    val day = index - 2
                    val isPlaceholder = day < 1 || day > 30
                    val isSelected = !isPlaceholder && showDaySheet && day == selectedDay
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrototypeColors.surfaceCard)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, PrototypeColors.selectedBorder, RoundedCornerShape(12.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable(enabled = !isPlaceholder) {
                                if (!isPlaceholder) openSheetForDay(day)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!isPlaceholder) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = PrototypeColors.onBackground,
                                modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                            )
                            Text(
                                text = if (day % 5 == 0) "😊" else "·",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDaySheet && selectedDay != null) {
        val day = selectedDay!!
        ModalBottomSheet(
            onDismissRequest = { dismissSheet() },
            sheetState = sheetState,
            containerColor = PrototypeColors.surface,
            contentColor = PrototypeColors.onBackground,
        ) {
            CalendarDayDetailSheetContent(
                day = day,
                onOpenMoodEntry = {
                    dismissSheet()
                    onOpenMoodEntry()
                },
                onOpenJournalStep = {
                    dismissSheet()
                    onOpenJournalStep()
                },
                onOpenFunctionalJournal = {
                    dismissSheet()
                    onOpenFunctionalJournal()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun CalendarDayDetailSheetContent(
    day: Int,
    onOpenMoodEntry: () -> Unit,
    onOpenJournalStep: () -> Unit,
    onOpenFunctionalJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.prototype_day_sheet_title, day),
            style = MaterialTheme.typography.titleLarge,
            color = PrototypeColors.onBackground,
        )
        Text(
            text = stringResource(R.string.prototype_day_detail_mood_line),
            style = MaterialTheme.typography.bodyMedium,
            color = PrototypeColors.onSurfaceMuted,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.prototype_notes_placeholder),
            style = MaterialTheme.typography.labelLarge,
            color = PrototypeColors.onSurfaceMuted,
        )
        repeat(2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = PrototypeColors.surfaceCard,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.prototype_note_title_stub),
                            style = MaterialTheme.typography.titleSmall,
                            color = PrototypeColors.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.prototype_note_body_stub),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrototypeColors.onSurfaceMuted,
                        )
                    }
                    Text("›", color = PrototypeColors.accent)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onOpenMoodEntry) {
            Text(stringResource(R.string.prototype_nav_mood_entry), color = PrototypeColors.accent)
        }
        TextButton(onClick = onOpenJournalStep) {
            Text(stringResource(R.string.prototype_nav_journal_step), color = PrototypeColors.accent)
        }
        TextButton(onClick = onOpenFunctionalJournal) {
            Text(stringResource(R.string.prototype_nav_functional_journal), color = PrototypeColors.accent)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

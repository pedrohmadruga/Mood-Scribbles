package com.example.moodscribbles.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.metrics.DashboardMetrics
import com.example.moodscribbles.ui.history.HistoryEntryRow
import com.example.moodscribbles.ui.history.HistoryFilterMode
import com.example.moodscribbles.ui.history.JournalHistoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsTabScreen(
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onBackground,
                )
            }
            item {
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
            }
            if (uiState.filterMode == HistoryFilterMode.BY_MONTH) {
                item {
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
            }
            item {
                InsightsPeriodSubtitle(metrics = uiState.dashboardMetrics)
            }
            item {
                DashboardSection(
                    metrics = uiState.dashboardMetrics,
                    showPageHeader = false,
                )
            }
            item {
                HorizontalDivider(color = colorScheme.outlineVariant)
            }
            item {
                Text(
                    text = stringResource(R.string.history_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground,
                )
            }
            item {
                HorizontalDivider(color = colorScheme.outlineVariant)
            }
            if (uiState.entries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    )
                }
            } else {
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

@Composable
private fun InsightsPeriodSubtitle(metrics: DashboardMetrics) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    }
    val text = remember(metrics.periodStart, metrics.periodEnd) {
        val start = metrics.periodStart
        val end = metrics.periodEnd
        if (start != null && end != null) {
            "${start.format(formatter)} – ${end.format(formatter)}"
        } else {
            ""
        }
    }
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

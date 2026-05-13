package com.example.moodscribbles.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.metrics.DashboardMetrics
import com.example.moodscribbles.domain.metrics.JournalMetricsCalculator
import com.example.moodscribbles.domain.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

enum class HistoryFilterMode {
    BY_MONTH,
    LAST_30_DAYS,
}

data class JournalHistoryUiState(
    val filterMode: HistoryFilterMode = HistoryFilterMode.BY_MONTH,
    val visibleMonth: YearMonth = YearMonth.now(),
    val entries: List<JournalEntry> = emptyList(),
    val dashboardMetrics: DashboardMetrics = DashboardMetrics.empty(),
)

class JournalHistoryViewModel(
    private val journalRepository: JournalRepository,
    private val calculator: JournalMetricsCalculator,
) : ViewModel() {

    private val filterMode = MutableStateFlow(HistoryFilterMode.BY_MONTH)
    private val visibleMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<JournalHistoryUiState> = combine(
        filterMode,
        visibleMonth,
    ) { mode, month ->
        mode to month
    }
        .flatMapLatest { (mode, month) ->
            val (start, end) = when (mode) {
                HistoryFilterMode.BY_MONTH ->
                    month.atDay(1) to month.atEndOfMonth()
                HistoryFilterMode.LAST_30_DAYS -> {
                    val today = LocalDate.now()
                    today.minusDays(29) to today
                }
            }
            journalRepository.observeEntriesByDateRange(start, end).map { entries ->
                JournalHistoryUiState(
                    filterMode = mode,
                    visibleMonth = month,
                    entries = entries.sortedByDescending { it.date },
                    dashboardMetrics = calculator.compute(entries, start, end),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = JournalHistoryUiState(),
        )

    fun setFilterMode(mode: HistoryFilterMode) {
        filterMode.value = mode
    }

    fun onPreviousMonth() {
        if (filterMode.value != HistoryFilterMode.BY_MONTH) return
        visibleMonth.update { it.minusMonths(1) }
    }

    fun onNextMonth() {
        if (filterMode.value != HistoryFilterMode.BY_MONTH) return
        visibleMonth.update { it.plusMonths(1) }
    }
}

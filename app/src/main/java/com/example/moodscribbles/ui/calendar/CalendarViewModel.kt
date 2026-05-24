package com.example.moodscribbles.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val entriesByDate: Map<LocalDate, JournalEntry> = emptyMap(),
)

class CalendarViewModel(
    private val journalRepository: JournalRepository,
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<CalendarUiState> = visibleMonth
        .flatMapLatest { ym ->
            journalRepository.observeEntriesByDateRange(ym.atDay(1), ym.atEndOfMonth())
                .map { entries ->
                    CalendarUiState(
                        visibleMonth = ym,
                        entriesByDate = entries.associateBy { it.date },
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState(),
        )

    fun onPreviousMonth() {
        visibleMonth.update { it.minusMonths(1) }
    }

    fun onNextMonth() {
        visibleMonth.update { it.plusMonths(1) }
    }

    fun onMonthSelected(yearMonth: YearMonth) {
        visibleMonth.update { yearMonth }
    }
}

package com.example.moodscribbles.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.repository.JournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class CalendarDayDetailViewModel(
    journalRepository: JournalRepository,
    date: LocalDate,
) : ViewModel() {

    val entry: StateFlow<JournalEntry?> = journalRepository
        .observeEntriesByDateRange(date, date)
        .map { list -> list.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}

package com.example.moodscribbles.domain.usecase

import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.repository.JournalRepository
import java.time.LocalDate

class GetJournalEntryByDateUseCase(
    private val journalRepository: JournalRepository,
) {

    suspend operator fun invoke(date: LocalDate): JournalEntry? {
        return journalRepository.getEntryByDate(date)
    }
}

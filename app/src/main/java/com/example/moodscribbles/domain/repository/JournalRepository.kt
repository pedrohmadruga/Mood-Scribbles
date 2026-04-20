package com.example.moodscribbles.domain.repository

import com.example.moodscribbles.domain.JournalEntry
import java.time.LocalDate

interface JournalRepository {
    suspend fun getEntries(): List<JournalEntry>
    suspend fun getEntryByDate(date: LocalDate): JournalEntry?
    suspend fun getEntriesByDateRange(
        startDateInclusive: LocalDate,
        endDateInclusive: LocalDate,
    ): List<JournalEntry>

    suspend fun insertEntry(entry: JournalEntry): Long
    suspend fun updateEntry(entry: JournalEntry)
    suspend fun deleteEntry(entryId: Long)
}

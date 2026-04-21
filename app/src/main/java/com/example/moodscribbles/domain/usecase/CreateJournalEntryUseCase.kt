package com.example.moodscribbles.domain.usecase

import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRules
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.repository.JournalRepository

class CreateJournalEntryUseCase(
    private val journalRepository: JournalRepository,
) {

    suspend operator fun invoke(entry: JournalEntry): Result {
        val violations = JournalEntryRules.validate(entry)
        if (violations.isNotEmpty()) {
            return Result.ValidationError(violations)
        }

        val existingEntry = journalRepository.getEntryByDate(entry.date)
        if (existingEntry != null) {
            return Result.AlreadyExistsForDate
        }

        val createdEntryId = journalRepository.insertEntry(entry)
        return Result.Success(createdEntryId)
    }

    // represents the outcome of the use case that must be handled by the caller
    sealed interface Result {
        data class Success(val entryId: Long) : Result
        data class ValidationError(
            val violations: List<JournalEntryRuleViolation>,
        ) : Result

        data object AlreadyExistsForDate : Result
    }
}

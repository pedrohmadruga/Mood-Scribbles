package com.example.moodscribbles.domain.usecase

import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRules
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.repository.JournalRepository

class CreateJournalEntryUseCase(
    private val journalRepository: JournalRepository,
) {

    suspend operator fun invoke(entry: JournalEntry): Result {
        val existingForDate = journalRepository.getEntryByDate(entry.date)
        val violations = JournalEntryRules.validateForCreate(entry, existingForDate)
        if (violations.isNotEmpty()) {
            if (JournalEntryRules.isOnlyDuplicateForDate(violations)) { // if the entry already exists for the date, return an AlreadyExistsForDate error
                return Result.AlreadyExistsForDate
            }
            return Result.ValidationError(violations)
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

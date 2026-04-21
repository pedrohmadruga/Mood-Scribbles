package com.example.moodscribbles.domain.usecase

import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRules
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.repository.JournalRepository
import java.time.Instant

class UpdateJournalEntryUseCase(
    private val journalRepository: JournalRepository,
) {

    suspend operator fun invoke(entry: JournalEntry): Result {
        val violations = JournalEntryRules.validate(entry)
        if (violations.isNotEmpty()) {
            return Result.ValidationError(violations)
        }

        val existingEntry = journalRepository.getEntryByDate(entry.date)?: return Result.NotFoundForDate // if the entry is not found, return a NotFoundForDate error

        val entryToUpdate = entry.copy(
            id = existingEntry.id,
            createdAt = existingEntry.createdAt,
            updatedAt = Instant.now(),
        )

        journalRepository.updateEntry(entryToUpdate)
        return Result.Success
    }

    sealed interface Result {
        data object Success : Result
        data class ValidationError(
            val violations: List<JournalEntryRuleViolation>,
        ) : Result

        data object NotFoundForDate : Result
    }
}

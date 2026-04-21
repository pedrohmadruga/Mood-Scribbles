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
        val existingForDate = journalRepository.getEntryByDate(entry.date)
        val violations = JournalEntryRules.validateForUpdate(entry, existingForDate)
        if (violations.isNotEmpty()) {
            if (JournalEntryRules.isOnlyNotFoundForDate(violations)) {
                return Result.NotFoundForDate
            }
            return Result.ValidationError(violations)
        }

        val existingEntry = existingForDate!! // if the existingForDate is null, throw a KotlinNullPointerException (Kotlin's way of handling nulls)

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

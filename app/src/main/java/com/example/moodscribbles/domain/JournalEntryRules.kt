package com.example.moodscribbles.domain

// Single place for journal entry business rules (pure checks + rules that need a fetched row).
object JournalEntryRules {

    // validates the entry and returns a list of JournalEntryRuleViolations
    fun validate(entry: JournalEntry): List<JournalEntryRuleViolation> = buildList {
        if (entry.energyLevel !in JournalEntry.MIN_ENERGY..JournalEntry.MAX_ENERGY) {
            add(JournalEntryRuleViolation.EnergyOutOfRange(entry.energyLevel))
        }
        if (entry.updatedAt.isBefore(entry.createdAt)) {
            add(JournalEntryRuleViolation.UpdatedBeforeCreated)
        }
    }

    /**
     * Create flow: base checks + at most one entry per calendar day.
     * [existingForDate] is whatever is already stored for [entry.date], or null.
     */
    fun validateForCreate(
        entry: JournalEntry,
        existingForDate: JournalEntry?,
    ): List<JournalEntryRuleViolation> = buildList {
        addAll(validate(entry))
        if (existingForDate != null) {
            add(JournalEntryRuleViolation.AlreadyExistsForDate)
        }
    }

    /**
     * Update flow: base checks + row must exist for that day.
     * [existingForDate] is the stored entry for [entry.date], or null if none.
     */
    fun validateForUpdate(
        entry: JournalEntry,
        existingForDate: JournalEntry?,
    ): List<JournalEntryRuleViolation> = buildList {
        addAll(validate(entry))
        if (existingForDate == null) {
            add(JournalEntryRuleViolation.EntryNotFoundForDate)
        } else if (entry.id != JournalEntry.UNSAVED_ID && entry.id != existingForDate.id) {
            add(JournalEntryRuleViolation.EntryIdDoesNotMatchDate(entry.id, existingForDate.id))
        }
    }

    fun isValid(entry: JournalEntry): Boolean = validate(entry).isEmpty()

    fun isOnlyDuplicateForDate(violations: List<JournalEntryRuleViolation>): Boolean =
        violations.singleOrNull() is JournalEntryRuleViolation.AlreadyExistsForDate

    fun isOnlyNotFoundForDate(violations: List<JournalEntryRuleViolation>): Boolean =
        violations.singleOrNull() is JournalEntryRuleViolation.EntryNotFoundForDate
}

sealed interface JournalEntryRuleViolation {
    data class EnergyOutOfRange(val value: Int) : JournalEntryRuleViolation
    data object UpdatedBeforeCreated : JournalEntryRuleViolation
    data object AlreadyExistsForDate : JournalEntryRuleViolation
    data object EntryNotFoundForDate : JournalEntryRuleViolation
    data class EntryIdDoesNotMatchDate(val entryId: Long, val existingId: Long) : JournalEntryRuleViolation
}

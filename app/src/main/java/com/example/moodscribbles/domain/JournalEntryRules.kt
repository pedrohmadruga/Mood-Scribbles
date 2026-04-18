package com.example.moodscribbles.domain

import java.time.Instant


object JournalEntryRules {

    // takes one JournalEntry and returns a list of JournalEntryRuleViolations
    fun validate(entry: JournalEntry): List<JournalEntryRuleViolation> = buildList { // creates a MutableList and returns an immutable List
        if (entry.energyLevel !in JournalEntry.MIN_ENERGY..JournalEntry.MAX_ENERGY) {
            add(JournalEntryRuleViolation.EnergyOutOfRange(entry.energyLevel))
        }
        if (entry.updatedAt.isBefore(entry.createdAt)) {
            add(JournalEntryRuleViolation.UpdatedBeforeCreated)
        }
    }

    fun isValid(entry: JournalEntry): Boolean = validate(entry).isEmpty()
}

sealed interface JournalEntryRuleViolation {
    data class EnergyOutOfRange(val value: Int) : JournalEntryRuleViolation
    data object UpdatedBeforeCreated : JournalEntryRuleViolation
}

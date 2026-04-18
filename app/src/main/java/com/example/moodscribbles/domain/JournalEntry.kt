package com.example.moodscribbles.domain

import java.time.LocalDate

/**
 * One emotional journal record for a calendar day.
 *
 * Business rule: at most one primary entry per [date] is enforced at repository/DB level.
 *
 * @param id Local row id; use [UNSAVED_ID] for a new entry before persistence.
 * @param energyLevel Subjective energy on a 0–100 scale (UI slider).
 * @param title Optional short note title.
 * @param description Optional longer note body.
 */
data class JournalEntry(
    val id: Long,
    val date: LocalDate,
    val mood: Mood,
    val energyLevel: Int,
    val title: String?,
    val description: String?,
    val emotions: List<Emotion>,
    val tags: List<Tag>,
) {
    companion object {
        const val UNSAVED_ID = 0L

        const val MIN_ENERGY = 0
        const val MAX_ENERGY = 100
    }
}

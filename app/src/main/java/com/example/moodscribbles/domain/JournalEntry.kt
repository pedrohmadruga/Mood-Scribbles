package com.example.moodscribbles.domain

import java.time.Instant
import java.time.LocalDate

/**
 * One emotional journal record for a calendar day.
 *
 * **Single entry per day:** At most one primary row per [date] — see [JournalEntryRules].
 *
 * **Editing:** Allowed at any time; [updatedAt] reflects the last successful save.
 *
 * @param id Local row id; use [UNSAVED_ID] for a new entry before persistence.
 * @param energyLevel Subjective energy on a 0–100 scale (UI slider).
 * @param title Optional short note title.
 * @param description Optional longer note body.
 * @param createdAt Instant when the row was first persisted (set by data layer on insert).
 * @param updatedAt Instant when the row was last persisted (set by data layer on insert/update).
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
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        const val UNSAVED_ID = 0L

        const val MIN_ENERGY = 0
        const val MAX_ENERGY = 100

         // creates an entry in memory, not yet inserted into the database
        fun newDraft(
            date: LocalDate,
            mood: Mood,
            energyLevel: Int,
            title: String?,
            description: String?,
            emotions: List<Emotion>,
            tags: List<Tag>,
            now: Instant = Instant.now(),
        ): JournalEntry = JournalEntry(
            id = UNSAVED_ID,
            date = date,
            mood = mood,
            energyLevel = energyLevel,
            title = title,
            description = description,
            emotions = emotions,
            tags = tags,
            createdAt = now,
            updatedAt = now,
        )
    }
}

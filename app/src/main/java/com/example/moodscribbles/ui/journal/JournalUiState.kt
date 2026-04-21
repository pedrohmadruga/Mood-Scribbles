package com.example.moodscribbles.ui.journal

import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.JournalEntryRuleViolation
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import java.time.Instant
import java.time.LocalDate

// represents the state of the journal form
data class JournalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val mood: Mood = Mood.NEUTRAL,
    val energyLevel: Int = 50,
    val title: String = "",
    val description: String = "",
    val emotion: Emotion = Emotion(Emotion.UNSAVED_ID, ""),
    val tags: List<Tag> = emptyList(),
    val entryId: Long = JournalEntry.UNSAVED_ID,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val saveError: JournalSaveError? = null,
) {
    val isExistingEntry: Boolean get() = entryId != JournalEntry.UNSAVED_ID
}

// represents the error that can occur when saving the journal entry
sealed interface JournalSaveError {
    data object AlreadyExistsForDate : JournalSaveError
    data object NotFoundForDate : JournalSaveError
    data class ValidationFailed(
        val violations: List<JournalEntryRuleViolation>,
    ) : JournalSaveError
}

// extension function to convert the JournalUiState to a JournalEntry
internal fun JournalUiState.toJournalEntry(now: Instant = Instant.now()): JournalEntry = JournalEntry(
    id = entryId,
    date = date,
    mood = mood,
    energyLevel = energyLevel,
    title = title.trim().takeIf { it.isNotEmpty() },
    description = description.trim().takeIf { it.isNotEmpty() },
    emotion = emotion,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// extension function to convert a JournalEntry to a JournalUiState
internal fun JournalEntry.toUiState(isLoading: Boolean = false, isSaving: Boolean = false): JournalUiState =
    JournalUiState(
        isLoading = isLoading,
        isSaving = isSaving,
        date = date,
        mood = mood,
        energyLevel = energyLevel,
        title = title.orEmpty(),
        description = description.orEmpty(),
        emotion = emotion,
        tags = tags,
        entryId = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        saveError = null,
    )

// initializes a fresh journal entry with default values and returns a JournalUiState
// Will only be used when the entry is not found for the given date
internal fun emptyDraftUiState(date: LocalDate, now: Instant = Instant.now()): JournalUiState =
    JournalEntry.newDraft(
        date = date,
        mood = Mood.NEUTRAL,
        energyLevel = 50,
        title = null,
        description = null,
        emotion = Emotion(Emotion.UNSAVED_ID, ""),
        tags = emptyList(),
        now = now,
    ).toUiState(isLoading = false)

package com.example.moodscribbles.ui.journal

import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import java.time.LocalDate

/**
 * All user-driven actions on the journal screen flow through [JournalViewModel.onEvent].
 */
sealed interface JournalUiEvent {
    data class DateSelected(val date: LocalDate) : JournalUiEvent
    data class MoodSelected(val mood: Mood) : JournalUiEvent
    data class EnergyChanged(val level: Int) : JournalUiEvent
    data class TitleChanged(val value: String) : JournalUiEvent
    data class DescriptionChanged(val value: String) : JournalUiEvent
    data class EmotionSelected(val emotion: Emotion) : JournalUiEvent
    data class NewTagInputChanged(val value: String) : JournalUiEvent
    data class SuggestedTagToggled(val tag: Tag) : JournalUiEvent
    data class SelectedTagRemoved(val tag: Tag) : JournalUiEvent
    data object AddTypedTagClicked : JournalUiEvent
    data object SaveClicked : JournalUiEvent
    /** Reloads the row for the current [JournalUiState.date] from storage (discards unsaved field edits). */
    data object ReloadFromRepositoryClicked : JournalUiEvent
    data object ClearSaveErrorClicked : JournalUiEvent
}

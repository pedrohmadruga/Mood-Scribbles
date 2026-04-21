package com.example.moodscribbles.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import com.example.moodscribbles.domain.usecase.CreateJournalEntryUseCase
import com.example.moodscribbles.domain.usecase.GetJournalEntryByDateUseCase
import com.example.moodscribbles.domain.usecase.UpdateJournalEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class JournalViewModel(
    private val getJournalEntryByDate: GetJournalEntryByDateUseCase,
    private val createJournalEntry: CreateJournalEntryUseCase,
    private val updateJournalEntry: UpdateJournalEntryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState()) // internal state (mutable) to store the UI state
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow() // public state (immutable) to observe the UI state

    init {
        loadForDate(LocalDate.now()) // loads the journal entry for the current date
    }

    fun loadForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, date = date, saveError = null)
            }
            val entry = getJournalEntryByDate(date)
            val now = Instant.now()
            _uiState.update { current ->
                when (entry) {
                    null -> emptyDraftUiState(date, now).copy(isSaving = current.isSaving)
                    else -> entry.toUiState(isLoading = false, isSaving = current.isSaving)
                }
            }
        }
    }

    fun setMood(mood: Mood) {
        _uiState.update { it.copy(mood = mood, saveError = null) }
    }

    fun setEnergyLevel(level: Int) {
        _uiState.update { it.copy(energyLevel = level, saveError = null) }
    }

    fun setTitle(value: String) {
        _uiState.update { it.copy(title = value, saveError = null) }
    }

    fun setDescription(value: String) {
        _uiState.update { it.copy(description = value, saveError = null) }
    }

    fun setEmotion(emotion: Emotion) {
        _uiState.update { it.copy(emotion = emotion, saveError = null) }
    }

    fun setTags(tags: List<Tag>) {
        _uiState.update { it.copy(tags = tags, saveError = null) }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    /*
    1. Gets the current UI state
    2. Validates the entry
    3. Creates or updates the entry in the database
    4. Treats the result of the save operation
    5. Updates the UI state with the result of the save operation
    */
    fun save() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            if (snapshot.isLoading || snapshot.isSaving) return@launch // If it's loading or saving, do nothing

            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val entry = snapshot.toJournalEntry() // converts UI to domain object

            // If the entry is existing, update it in the database. Otherwise, create it.
            if (snapshot.isExistingEntry) {
                when (val result = updateJournalEntry(entry)) {
                    UpdateJournalEntryUseCase.Result.Success -> applyPersistedSnapshot(snapshot.date)
                    is UpdateJournalEntryUseCase.Result.ValidationError -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveError = JournalSaveError.ValidationFailed(result.violations),
                            )
                        }
                    }
                    UpdateJournalEntryUseCase.Result.NotFoundForDate -> {
                        _uiState.update {
                            it.copy(isSaving = false, saveError = JournalSaveError.NotFoundForDate)
                        }
                    }
                }
            } else {
                when (val result = createJournalEntry(entry)) {
                    is CreateJournalEntryUseCase.Result.Success -> applyPersistedSnapshot(snapshot.date)
                    is CreateJournalEntryUseCase.Result.ValidationError -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveError = JournalSaveError.ValidationFailed(result.violations),
                            )
                        }
                    }
                    CreateJournalEntryUseCase.Result.AlreadyExistsForDate -> {
                        _uiState.update {
                            it.copy(isSaving = false, saveError = JournalSaveError.AlreadyExistsForDate)
                        }
                    }
                }
            }
        }
    }

    // reloads the journal entry for the given date from the database. Used to update the UI with the real data.
    private suspend fun applyPersistedSnapshot(date: LocalDate) {
        val refreshed = getJournalEntryByDate(date)
        _uiState.update { current ->
            when (refreshed) {
                null -> current.copy(isSaving = false, saveError = JournalSaveError.NotFoundForDate)
                else -> refreshed.toUiState(isLoading = false, isSaving = false)
            }
        }
    }
}

package com.example.moodscribbles.domain

/**
 * A selectable emotion label (may come from a fixed catalog or user-defined list in persistence).
 *
 * @param id Stable row id from local storage; use [UNSAVED_ID] before insert.
 * @param name Display name (user-facing string; localization lives in UI/resources).
 */
data class Emotion(
    val id: Long,
    val name: String,
) {
    companion object {
        const val UNSAVED_ID = 0L
    }
}

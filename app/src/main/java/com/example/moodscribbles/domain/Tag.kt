package com.example.moodscribbles.domain

/**
 * User-defined tag for filtering and context.
 *
 * @param id Stable row id from local storage; use [UNSAVED_ID] before insert.
 * @param name Display name (user-facing string; localization lives in UI/resources).
 */
data class Tag(
    val id: Long,
    val name: String,
) {
    companion object {
        const val UNSAVED_ID = 0L
    }
}

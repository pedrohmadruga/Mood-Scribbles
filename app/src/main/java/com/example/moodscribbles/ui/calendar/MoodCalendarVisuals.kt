package com.example.moodscribbles.ui.calendar

import androidx.compose.ui.graphics.Color
import com.example.moodscribbles.domain.Mood

/**
 * Visual mapping for calendar cells (emoji + tint). Used by the monthly calendar UI;
 * Day 15 will replace demo assignment with persisted [Mood] from each journal entry.
 */
fun Mood.calendarEmoji(): String = when (this) {
    Mood.VERY_SAD -> "😢"
    Mood.SAD -> "😕"
    Mood.NEUTRAL -> "😐"
    Mood.HAPPY -> "🙂"
    Mood.VERY_HAPPY -> "😄"
}

fun Mood.calendarTint(): Color = when (this) {
    Mood.VERY_SAD -> Color(0xFF5C6BC0)
    Mood.SAD -> Color(0xFF78909C)
    Mood.NEUTRAL -> Color(0xFFB0BEC5)
    Mood.HAPPY -> Color(0xFFFFB74D)
    Mood.VERY_HAPPY -> Color(0xFF81C784)
}

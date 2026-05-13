package com.example.moodscribbles.domain.metrics

import com.example.moodscribbles.domain.Mood

/**
 * Maps the ordinal mood scale to a 1–5 numeric score for averaging and charting.
 * Convention: VERY_SAD = 1 … VERY_HAPPY = 5 (linear spacing).
 */
fun Mood.toScore(): Float = when (this) {
    Mood.VERY_SAD -> 1f
    Mood.SAD -> 2f
    Mood.NEUTRAL -> 3f
    Mood.HAPPY -> 4f
    Mood.VERY_HAPPY -> 5f
}

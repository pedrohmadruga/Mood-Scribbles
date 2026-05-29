package com.example.moodscribbles.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.moodscribbles.R
import java.util.Locale

/**
 * Maps persisted emotion keys (English catalog names) to localized UI labels.
 * Custom user-entered names are shown as-is with light capitalization.
 */
@Composable
fun localizedEmotionLabel(rawName: String): String = when (rawName.trim().lowercase(Locale.ROOT)) {
    "happy" -> stringResource(R.string.journal_emotion_happy)
    "content" -> stringResource(R.string.journal_emotion_content)
    "neutral" -> stringResource(R.string.journal_emotion_neutral)
    "anxious" -> stringResource(R.string.journal_emotion_anxious)
    "sad" -> stringResource(R.string.journal_emotion_sad)
    "frustrated" -> stringResource(R.string.journal_emotion_frustrated)
    else -> rawName.trim().replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
    }
}

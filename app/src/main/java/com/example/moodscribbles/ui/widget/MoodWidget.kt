package com.example.moodscribbles.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.repository.JournalRepository
import com.example.moodscribbles.ui.MainActivity
import com.example.moodscribbles.ui.calendar.calendarEmoji
import org.koin.core.context.GlobalContext
import java.time.LocalDate

private val bgColor = ColorProvider(Color(0xFFF6F0FF))
private val textPrimary = ColorProvider(Color(0xFF1C1B1F))
private val textSecondary = ColorProvider(Color(0xFF6B6572))
private val accentColor = ColorProvider(Color(0xFF6750A4))

class MoodWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = try {
            GlobalContext.get().get<JournalRepository>().getEntryByDate(LocalDate.now())
        } catch (_: Exception) {
            null
        }
        provideContent {
            WidgetRoot(context = context, entry = entry)
        }
    }
}

@Composable
private fun WidgetRoot(context: Context, entry: JournalEntry?) {
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java))
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(bgColor)
            .clickable(openApp)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (entry != null) {
            EntryContent(context = context, entry = entry)
        } else {
            EmptyContent(context = context)
        }
    }
}

@Composable
private fun EntryContent(context: Context, entry: JournalEntry) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = context.getString(R.string.journal_today).uppercase(),
            style = TextStyle(color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = entry.mood.calendarEmoji(),
            style = TextStyle(fontSize = 38.sp),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = moodLabel(context, entry.mood),
            style = TextStyle(color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "⚡ ${entry.energyLevel}",
            style = TextStyle(color = textSecondary, fontSize = 12.sp),
        )
    }
}

@Composable
private fun EmptyContent(context: Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "😶", style = TextStyle(fontSize = 36.sp))
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = context.getString(R.string.widget_no_entry),
            style = TextStyle(color = textPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp),
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = context.getString(R.string.widget_tap_to_add),
            style = TextStyle(color = accentColor, fontSize = 11.sp),
        )
    }
}

private fun moodLabel(context: Context, mood: Mood): String = when (mood) {
    Mood.VERY_SAD -> context.getString(R.string.mood_very_sad)
    Mood.SAD -> context.getString(R.string.mood_sad)
    Mood.NEUTRAL -> context.getString(R.string.mood_neutral)
    Mood.HAPPY -> context.getString(R.string.mood_happy)
    Mood.VERY_HAPPY -> context.getString(R.string.mood_very_happy)
}

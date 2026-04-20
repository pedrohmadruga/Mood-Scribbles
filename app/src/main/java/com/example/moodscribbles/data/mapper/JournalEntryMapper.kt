package com.example.moodscribbles.data.mapper

import com.example.moodscribbles.data.local.entity.JournalEntryEntity
import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Mood
import com.example.moodscribbles.domain.Tag
import java.time.Instant
import java.time.LocalDate

object JournalEntryMapper {

    fun toDomain(
        entity: JournalEntryEntity,
        emotion: Emotion,
        tags: List<Tag>,
    ): JournalEntry = JournalEntry(
        id = entity.id,
        date = LocalDate.parse(entity.date),
        mood = moodFromStoredName(entity.mood),
        energyLevel = entity.energyLevel,
        title = entity.title,
        description = entity.description,
        emotion = emotion,
        tags = tags,
        createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
    )

    fun toEntity(
        domain: JournalEntry,
        emotionId: Long,
        updatedAt: Instant,
    ): JournalEntryEntity = JournalEntryEntity(
        id = domain.id,
        date = domain.date.toString(),
        mood = domain.mood.name,
        emotionId = emotionId,
        energyLevel = domain.energyLevel,
        title = domain.title,
        description = domain.description,
        createdAtEpochMillis = domain.createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )

    /** Keeps the app stable if older rows have an unexpected mood string. */
    private fun moodFromStoredName(name: String): Mood =
        Mood.entries.firstOrNull { it.name == name } ?: Mood.NEUTRAL
}

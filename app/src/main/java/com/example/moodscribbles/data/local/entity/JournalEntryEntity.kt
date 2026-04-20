package com.example.moodscribbles.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local persistence model for the primary daily journal record.
 * - [date] uses ISO-8601 (yyyy-MM-dd).
 * - [mood] uses enum name (e.g. "HAPPY").
 */
@Entity(
    tableName = "journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = EmotionEntity::class,
            parentColumns = ["id"],
            childColumns = ["emotion_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["entry_date"], unique = true),
        Index(value = ["emotion_id"]),
    ],
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "entry_date")
    val date: String,
    @ColumnInfo(name = "mood")
    val mood: String,
    @ColumnInfo(name = "emotion_id")
    val emotionId: Long,
    @ColumnInfo(name = "energy_level")
    val energyLevel: Int,
    val title: String?,
    val description: String?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)

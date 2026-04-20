package com.example.moodscribbles.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emotions",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class EmotionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
)

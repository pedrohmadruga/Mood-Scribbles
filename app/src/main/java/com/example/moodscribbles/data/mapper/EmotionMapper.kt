package com.example.moodscribbles.data.mapper

import com.example.moodscribbles.data.local.entity.EmotionEntity
import com.example.moodscribbles.domain.Emotion

object EmotionMapper {

    fun toDomain(entity: EmotionEntity): Emotion = Emotion(
        id = entity.id,
        name = entity.name,
    )
}

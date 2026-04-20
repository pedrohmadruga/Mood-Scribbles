package com.example.moodscribbles.data.mapper

import com.example.moodscribbles.data.local.entity.TagEntity
import com.example.moodscribbles.domain.Tag

object TagMapper {

    fun toDomain(entity: TagEntity): Tag = Tag(
        id = entity.id,
        name = entity.name,
    )
}

package com.example.moodscribbles.data.repository

import androidx.room.withTransaction
import com.example.moodscribbles.data.local.AppDatabase
import com.example.moodscribbles.data.local.dao.EmotionDao
import com.example.moodscribbles.data.local.dao.EntryTagCrossRefDao
import com.example.moodscribbles.data.local.dao.JournalDao
import com.example.moodscribbles.data.local.dao.TagDao
import com.example.moodscribbles.data.local.entity.EmotionEntity
import com.example.moodscribbles.data.local.entity.EntryTagCrossRef
import com.example.moodscribbles.data.local.entity.JournalEntryEntity
import com.example.moodscribbles.data.local.entity.TagEntity
import com.example.moodscribbles.data.mapper.EmotionMapper
import com.example.moodscribbles.data.mapper.JournalEntryMapper
import com.example.moodscribbles.data.mapper.TagMapper
import com.example.moodscribbles.domain.Emotion
import com.example.moodscribbles.domain.JournalEntry
import com.example.moodscribbles.domain.Tag
import com.example.moodscribbles.domain.repository.JournalRepository
import java.time.Instant
import java.time.LocalDate

class JournalRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val journalDao: JournalDao,
    private val emotionDao: EmotionDao,
    private val tagDao: TagDao,
    private val entryTagCrossRefDao: EntryTagCrossRefDao,
) : JournalRepository {

    override suspend fun getEntries(): List<JournalEntry> = appDatabase.withTransaction {
        journalDao.getEntries().map { toDomain(it) }
    }

    override suspend fun getEntryByDate(date: LocalDate): JournalEntry? = appDatabase.withTransaction {
        journalDao.getEntryByDate(date.toString())?.let { toDomain(it) }
    }

    override suspend fun getEntriesByDateRange(
        startDateInclusive: LocalDate,
        endDateInclusive: LocalDate,
    ): List<JournalEntry> = appDatabase.withTransaction {
        journalDao.getEntriesByDateRange(
            startDateInclusive = startDateInclusive.toString(),
            endDateInclusive = endDateInclusive.toString(),
        ).map { toDomain(it) }
    }

    override suspend fun insertEntry(entry: JournalEntry): Long = appDatabase.withTransaction {
        val emotionId = resolveEmotionId(entry.emotion)
        val entityId = journalDao.insert(
            JournalEntryMapper.toEntity(
                domain = entry,
                emotionId = emotionId,
                updatedAt = entry.updatedAt,
            ),
        )

        replaceTagsForEntry(entityId = entityId, tags = entry.tags)
        entityId
    }

    override suspend fun updateEntry(entry: JournalEntry) = appDatabase.withTransaction {
        val emotionId = resolveEmotionId(entry.emotion)
        journalDao.update(
            JournalEntryMapper.toEntity(
                domain = entry,
                emotionId = emotionId,
                updatedAt = Instant.now(),
            ),
        )
        replaceTagsForEntry(entityId = entry.id, tags = entry.tags)
    }

    override suspend fun deleteEntry(entryId: Long) = appDatabase.withTransaction {
        journalDao.deleteById(entryId)
    }

    // changes the relation between an entry and its tags for a new list of tags
    // better than deleting and inserting all tags again or comparing all tags in the new list with the old list
    private suspend fun replaceTagsForEntry(entityId: Long, tags: List<Tag>) {
        entryTagCrossRefDao.deleteAllByEntryId(entityId)
        val tagIds = resolveTagIds(tags)
        val crossRefs = tagIds.map { tagId ->
            EntryTagCrossRef(entryId = entityId, tagId = tagId)
        }
        entryTagCrossRefDao.insertAll(crossRefs)
    }

    // Reuse by id/name when possible; create only if truly missing.
    private suspend fun resolveEmotionId(emotion: Emotion): Long {
        if (emotion.id != Emotion.UNSAVED_ID) {
            val existing = emotionDao.getById(emotion.id)
            if (existing != null) return existing.id
        }

        val normalizedName = emotion.name.trim()
        emotionDao.getByName(normalizedName)?.let { return it.id }

        val insertedId = emotionDao.insert(EmotionEntity(name = normalizedName))
        if (insertedId > 0) return insertedId

        return requireNotNull(emotionDao.getByName(normalizedName)) {
            "Failed to resolve emotion id for name=$normalizedName"
        }.id
    }

    // Same idea as emotion: deduplicate and return stable IDs for cross-refs.
    private suspend fun resolveTagIds(tags: List<Tag>): List<Long> {
        val resolvedIds = mutableSetOf<Long>()
        tags.forEach { tag ->
            if (tag.id != Tag.UNSAVED_ID) {
                val existing = tagDao.getById(tag.id)
                if (existing != null) {
                    resolvedIds += existing.id
                    return@forEach
                }
            }

            val normalizedName = tag.name.trim()
            val tagId = tagDao.getByName(normalizedName)?.id ?: run {
                val insertedId = tagDao.insert(TagEntity(name = normalizedName))
                if (insertedId > 0) {
                    insertedId
                } else {
                    requireNotNull(tagDao.getByName(normalizedName)) {
                        "Failed to resolve tag id for name=$normalizedName"
                    }.id
                }
            }
            resolvedIds += tagId
        }
        return resolvedIds.toList()
    }

    private suspend fun toDomain(entity: JournalEntryEntity): JournalEntry {
        val emotionEntity = requireNotNull(emotionDao.getById(entity.emotionId)) {
            "Emotion not found for id=${entity.emotionId}."
        }
        val emotion = EmotionMapper.toDomain(emotionEntity)

        val tagIds = entryTagCrossRefDao.getTagIdsByEntryId(entity.id)
        val tagsById = tagDao.getByIds(tagIds).associateBy { it.id }
        val tags = tagIds.mapNotNull { tagsById[it] }.map { TagMapper.toDomain(it) }

        return JournalEntryMapper.toDomain(
            entity = entity,
            emotion = emotion,
            tags = tags,
        )
    }
}

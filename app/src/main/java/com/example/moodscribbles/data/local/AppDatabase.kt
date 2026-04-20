package com.example.moodscribbles.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moodscribbles.data.local.dao.EmotionDao
import com.example.moodscribbles.data.local.dao.EntryTagCrossRefDao
import com.example.moodscribbles.data.local.dao.JournalDao
import com.example.moodscribbles.data.local.dao.TagDao
import com.example.moodscribbles.data.local.entity.EmotionEntity
import com.example.moodscribbles.data.local.entity.EntryTagCrossRef
import com.example.moodscribbles.data.local.entity.JournalEntryEntity
import com.example.moodscribbles.data.local.entity.TagEntity

@Database(
    entities = [
        JournalEntryEntity::class,
        EmotionEntity::class,
        TagEntity::class,
        EntryTagCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun emotionDao(): EmotionDao
    abstract fun tagDao(): TagDao
    abstract fun entryTagCrossRefDao(): EntryTagCrossRefDao

    companion object {
        const val NAME: String = "moodscribbles.db"
    }
}

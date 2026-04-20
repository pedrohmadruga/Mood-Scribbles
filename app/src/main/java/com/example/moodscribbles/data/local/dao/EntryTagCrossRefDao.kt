package com.example.moodscribbles.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodscribbles.data.local.entity.EntryTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryTagCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: EntryTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<EntryTagCrossRef>)

    @Query("SELECT tag_id FROM entry_tag_cross_ref WHERE entry_id = :entryId")
    fun observeTagIdsByEntryId(entryId: Long): Flow<List<Long>>

    @Query("DELETE FROM entry_tag_cross_ref WHERE entry_id = :entryId")
    suspend fun deleteAllByEntryId(entryId: Long)
}

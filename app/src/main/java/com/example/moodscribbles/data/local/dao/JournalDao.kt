package com.example.moodscribbles.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moodscribbles.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries ORDER BY entry_date DESC")
    fun observeEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :entryId LIMIT 1")
    suspend fun getEntryById(entryId: Long): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE entry_date = :date LIMIT 1")
    fun observeEntryByDate(date: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE entry_date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): JournalEntryEntity?

    @Query(
        """
        SELECT * FROM journal_entries
        WHERE entry_date BETWEEN :startDateInclusive AND :endDateInclusive
        ORDER BY entry_date ASC
        """,
    )
    fun observeEntriesByDateRange(
        startDateInclusive: String,
        endDateInclusive: String,
    ): Flow<List<JournalEntryEntity>>

    @Query(
        """
        SELECT * FROM journal_entries
        WHERE entry_date BETWEEN :startDateInclusive AND :endDateInclusive
        ORDER BY entry_date ASC
        """,
    )
    suspend fun getEntriesByDateRange(
        startDateInclusive: String,
        endDateInclusive: String,
    ): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}

package com.example.moodscribbles.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moodscribbles.data.local.entity.EmotionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionDao {

    @Query("SELECT * FROM emotions ORDER BY name ASC")
    fun observeAll(): Flow<List<EmotionEntity>>

    @Query("SELECT * FROM emotions WHERE id = :emotionId LIMIT 1")
    suspend fun getById(emotionId: Long): EmotionEntity?

    @Query("SELECT * FROM emotions WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): EmotionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(emotion: EmotionEntity): Long

    @Update
    suspend fun update(emotion: EmotionEntity)

    @Delete
    suspend fun delete(emotion: EmotionEntity)
}

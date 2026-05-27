package com.diarylite.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diarylite.app.data.local.entity.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(moods: List<MoodEntity>)

    @Query("SELECT * FROM moods WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun observeActive(): Flow<List<MoodEntity>>
}

package com.diarylite.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diarylite.app.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Insert
    suspend fun insert(entry: DiaryEntryEntity): Long

    @Update
    suspend fun update(entry: DiaryEntryEntity): Int

    @Delete
    suspend fun delete(entry: DiaryEntryEntity): Int

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun observeById(id: Long): Flow<DiaryEntryEntity?>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getById(id: Long): DiaryEntryEntity?

    @Query(
        """
        SELECT * FROM diary_entries
        ORDER BY entryDateEpochDay DESC,
            CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC,
            entryTimeMinute DESC,
            updatedAt DESC,
            id DESC
        """,
    )
    fun observeAll(): Flow<List<DiaryEntryEntity>>

    @Query(
        """
        SELECT * FROM diary_entries
        ORDER BY entryDateEpochDay DESC,
            CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC,
            entryTimeMinute DESC,
            updatedAt DESC,
            id DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<DiaryEntryEntity>>

    @Query(
        """
        SELECT * FROM diary_entries
        WHERE entryDateEpochDay = :entryDateEpochDay
        ORDER BY entryDateEpochDay DESC,
            CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC,
            entryTimeMinute DESC,
            updatedAt DESC,
            id DESC
        """,
    )
    fun observeByDate(entryDateEpochDay: Long): Flow<List<DiaryEntryEntity>>

    @Query(
        """
        SELECT entryDateEpochDay, COUNT(*) AS count
        FROM diary_entries
        WHERE entryDateEpochDay >= :startEpochDay AND entryDateEpochDay < :endEpochDay
        GROUP BY entryDateEpochDay
        """,
    )
    fun observeDateCountsInRange(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<EntryDateCount>>

    @Query(
        """
        SELECT * FROM diary_entries
        WHERE (title IS NOT NULL AND title LIKE :escapedQuery ESCAPE '\')
            OR content LIKE :escapedQuery ESCAPE '\'
        ORDER BY entryDateEpochDay DESC,
            CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC,
            entryTimeMinute DESC,
            updatedAt DESC,
            id DESC
        """,
    )
    fun search(escapedQuery: String): Flow<List<DiaryEntryEntity>>

    @Query(
        """
        SELECT * FROM diary_entries
        ORDER BY entryDateEpochDay ASC,
            CASE WHEN entryTimeMinute IS NULL THEN 1 ELSE 0 END ASC,
            entryTimeMinute ASC,
            createdAt ASC,
            id ASC
        """,
    )
    suspend fun getAllForExport(): List<DiaryEntryEntity>
}

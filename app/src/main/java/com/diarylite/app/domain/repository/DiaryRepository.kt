package com.diarylite.app.domain.repository

import com.diarylite.app.data.local.dao.EntryDateCount
import com.diarylite.app.domain.model.DiaryEntry
import com.diarylite.app.domain.model.Mood
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun observeAllEntries(): Flow<List<DiaryEntry>>
    fun observeRecentEntries(limit: Int): Flow<List<DiaryEntry>>
    fun observeEntry(id: Long): Flow<DiaryEntry?>
    fun observeEntriesByDate(entryDateEpochDay: Long): Flow<List<DiaryEntry>>
    fun observeEntryCountsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<EntryDateCount>>
    fun searchEntries(trimmedEscapedLikeQuery: String): Flow<List<DiaryEntry>>
    fun observeActiveMoods(): Flow<List<Mood>>

    suspend fun getEntry(id: Long): DiaryEntry?
    suspend fun getEntriesForExport(): List<DiaryEntry>
    suspend fun createEntry(
        title: String?,
        content: String,
        entryDateEpochDay: Long,
        entryTimeMinute: Int?,
        moodCode: String?,
    ): Long

    suspend fun updateEntry(entry: DiaryEntry): Boolean
    suspend fun deleteEntry(id: Long): Boolean
    suspend fun seedMoods()
}

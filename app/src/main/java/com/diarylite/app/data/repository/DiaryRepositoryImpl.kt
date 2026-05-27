package com.diarylite.app.data.repository

import com.diarylite.app.data.local.dao.DiaryEntryDao
import com.diarylite.app.data.local.dao.EntryDateCount
import com.diarylite.app.data.local.dao.MoodDao
import com.diarylite.app.data.local.defaultMoodEntities
import com.diarylite.app.data.local.entity.DiaryEntryEntity
import com.diarylite.app.data.local.entity.toDomain
import com.diarylite.app.data.local.entity.toEntity
import com.diarylite.app.domain.model.DiaryEntry
import com.diarylite.app.domain.model.Mood
import com.diarylite.app.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiaryRepositoryImpl(
    private val diaryEntryDao: DiaryEntryDao,
    private val moodDao: MoodDao,
    private val clockMillis: () -> Long = { System.currentTimeMillis() },
) : DiaryRepository {
    override fun observeAllEntries(): Flow<List<DiaryEntry>> =
        diaryEntryDao.observeAll().map { entries -> entries.map { it.toDomain() } }

    override fun observeRecentEntries(limit: Int): Flow<List<DiaryEntry>> =
        diaryEntryDao.observeRecent(limit).map { entries -> entries.map { it.toDomain() } }

    override fun observeEntry(id: Long): Flow<DiaryEntry?> =
        diaryEntryDao.observeById(id).map { it?.toDomain() }

    override fun observeEntriesByDate(entryDateEpochDay: Long): Flow<List<DiaryEntry>> =
        diaryEntryDao.observeByDate(entryDateEpochDay).map { entries -> entries.map { it.toDomain() } }

    override fun observeEntryCountsInRange(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<EntryDateCount>> = diaryEntryDao.observeDateCountsInRange(startEpochDay, endEpochDay)

    override fun searchEntries(trimmedEscapedLikeQuery: String): Flow<List<DiaryEntry>> =
        diaryEntryDao.search(trimmedEscapedLikeQuery).map { entries -> entries.map { it.toDomain() } }

    override fun observeActiveMoods(): Flow<List<Mood>> =
        moodDao.observeActive().map { moods -> moods.map { it.toDomain() } }

    override suspend fun getEntry(id: Long): DiaryEntry? = diaryEntryDao.getById(id)?.toDomain()

    override suspend fun getEntriesForExport(): List<DiaryEntry> =
        diaryEntryDao.getAllForExport().map { it.toDomain() }

    override suspend fun createEntry(
        title: String?,
        content: String,
        entryDateEpochDay: Long,
        entryTimeMinute: Int?,
        moodCode: String?,
    ): Long {
        validateContent(content)
        validateTime(entryTimeMinute)
        val now = clockMillis()
        return diaryEntryDao.insert(
            DiaryEntryEntity(
                title = title?.takeIf { it.isNotBlank() },
                content = content,
                entryDateEpochDay = entryDateEpochDay,
                entryTimeMinute = entryTimeMinute,
                moodCode = moodCode,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateEntry(entry: DiaryEntry): Boolean {
        validateContent(entry.content)
        validateTime(entry.entryTimeMinute)
        val existing = diaryEntryDao.getById(entry.id) ?: return false
        val updated = entry.copy(
            title = entry.title?.takeIf { it.isNotBlank() },
            createdAt = existing.createdAt,
            updatedAt = clockMillis(),
        )
        return diaryEntryDao.update(updated.toEntity()) > 0
    }

    override suspend fun deleteEntry(id: Long): Boolean {
        val existing = diaryEntryDao.getById(id) ?: return false
        return diaryEntryDao.delete(existing) > 0
    }

    override suspend fun seedMoods() {
        moodDao.upsertAll(defaultMoodEntities())
    }

    private fun validateContent(content: String) {
        require(content.trim().isNotEmpty()) { "Content must not be blank." }
    }

    private fun validateTime(entryTimeMinute: Int?) {
        require(entryTimeMinute == null || entryTimeMinute in 0..1439) {
            "Entry time must be null or between 0 and 1439."
        }
    }
}

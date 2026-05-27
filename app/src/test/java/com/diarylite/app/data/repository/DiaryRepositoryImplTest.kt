package com.diarylite.app.data.repository

import com.diarylite.app.data.local.dao.DiaryEntryDao
import com.diarylite.app.data.local.dao.EntryDateCount
import com.diarylite.app.data.local.dao.MoodDao
import com.diarylite.app.data.local.entity.DiaryEntryEntity
import com.diarylite.app.data.local.entity.MoodEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DiaryRepositoryImplTest {
    @Test
    fun createEntryPreservesContentAndNormalizesBlankTitle() = runBlocking {
        val diaryDao = FakeDiaryEntryDao()
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = diaryDao,
            moodDao = FakeMoodDao(),
            clockMillis = { 1_000L },
        )

        val id = repository.createEntry(
            title = "   ",
            content = "  first line\nsecond line  ",
            entryDateEpochDay = 20_000L,
            entryTimeMinute = 60,
            moodCode = "happy",
        )

        val stored = diaryDao.stored(id)
        assertNull(stored.title)
        assertEquals("  first line\nsecond line  ", stored.content)
        assertEquals(20_000L, stored.entryDateEpochDay)
        assertEquals(60, stored.entryTimeMinute)
        assertEquals("happy", stored.moodCode)
        assertEquals(1_000L, stored.createdAt)
        assertEquals(1_000L, stored.updatedAt)
    }

    @Test
    fun createEntryRejectsBlankContentAndInvalidTime() = runBlocking {
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = FakeDiaryEntryDao(),
            moodDao = FakeMoodDao(),
        )

        assertFailsWithIllegalArgument {
            repository.createEntry(
                title = null,
                content = " \n\t ",
                entryDateEpochDay = 20_000L,
                entryTimeMinute = null,
                moodCode = null,
            )
        }

        assertFailsWithIllegalArgument {
            repository.createEntry(
                title = null,
                content = "content",
                entryDateEpochDay = 20_000L,
                entryTimeMinute = 1_440,
                moodCode = null,
            )
        }
    }

    @Test
    fun createEntryNormalizesBlankMoodAndRejectsUnknownMood() = runBlocking {
        val diaryDao = FakeDiaryEntryDao()
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = diaryDao,
            moodDao = FakeMoodDao(),
        )

        val id = repository.createEntry(
            title = null,
            content = "content",
            entryDateEpochDay = 20_000L,
            entryTimeMinute = null,
            moodCode = "  ",
        )

        assertNull(diaryDao.stored(id).moodCode)
        assertFailsWithIllegalArgument {
            repository.createEntry(
                title = null,
                content = "content",
                entryDateEpochDay = 20_000L,
                entryTimeMinute = null,
                moodCode = "unknown",
            )
        }
    }

    @Test
    fun updateEntryKeepsCreatedAtAndRefreshesUpdatedAt() = runBlocking {
        val diaryDao = FakeDiaryEntryDao()
        var now = 1_000L
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = diaryDao,
            moodDao = FakeMoodDao(),
            clockMillis = { now },
        )
        val id = repository.createEntry(
            title = "Original",
            content = "content",
            entryDateEpochDay = 20_000L,
            entryTimeMinute = 90,
            moodCode = null,
        )

        now = 2_500L
        val existing = repository.getEntry(id) ?: error("Expected inserted entry.")
        val updated = repository.updateEntry(
            existing.copy(
                title = "",
                content = "updated\ncontent",
                entryDateEpochDay = 20_001L,
                entryTimeMinute = null,
                moodCode = "calm",
            ),
        )

        assertTrue(updated)
        val stored = diaryDao.stored(id)
        assertNull(stored.title)
        assertEquals("updated\ncontent", stored.content)
        assertEquals(20_001L, stored.entryDateEpochDay)
        assertNull(stored.entryTimeMinute)
        assertEquals("calm", stored.moodCode)
        assertEquals(1_000L, stored.createdAt)
        assertEquals(2_500L, stored.updatedAt)
    }

    @Test
    fun updateEntryNormalizesBlankMoodAndRejectsUnknownMood() = runBlocking {
        val diaryDao = FakeDiaryEntryDao()
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = diaryDao,
            moodDao = FakeMoodDao(),
        )
        val id = repository.createEntry(
            title = null,
            content = "content",
            entryDateEpochDay = 20_000L,
            entryTimeMinute = null,
            moodCode = "happy",
        )
        val existing = repository.getEntry(id) ?: error("Expected inserted entry.")

        assertTrue(repository.updateEntry(existing.copy(moodCode = " ")))
        assertNull(diaryDao.stored(id).moodCode)
        assertFailsWithIllegalArgument {
            repository.updateEntry(existing.copy(moodCode = "unknown"))
        }
    }

    @Test
    fun updateAndDeleteReturnFalseWhenEntryDoesNotExist() = runBlocking {
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = FakeDiaryEntryDao(),
            moodDao = FakeMoodDao(),
        )

        assertFalse(
            repository.updateEntry(
                com.diarylite.app.domain.model.DiaryEntry(
                    id = 42L,
                    title = null,
                    content = "content",
                    entryDateEpochDay = 20_000L,
                    entryTimeMinute = null,
                    moodCode = null,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
            ),
        )
        assertFalse(repository.deleteEntry(42L))
    }

    @Test
    fun observeEntryCountsMapsDaoResultToDomainModel() = runBlocking {
        val diaryDao = FakeDiaryEntryDao()
        val repository = DiaryRepositoryImpl(
            diaryEntryDao = diaryDao,
            moodDao = FakeMoodDao(),
        )
        repository.createEntry(
            title = null,
            content = "first",
            entryDateEpochDay = 20_000L,
            entryTimeMinute = null,
            moodCode = null,
        )
        repository.createEntry(
            title = null,
            content = "second",
            entryDateEpochDay = 20_000L,
            entryTimeMinute = null,
            moodCode = null,
        )
        repository.createEntry(
            title = null,
            content = "outside",
            entryDateEpochDay = 20_010L,
            entryTimeMinute = null,
            moodCode = null,
        )

        val counts = repository.observeEntryCountsInRange(
            startEpochDay = 20_000L,
            endEpochDay = 20_001L,
        ).first()

        assertEquals(1, counts.size)
        assertEquals(20_000L, counts.single().entryDateEpochDay)
        assertEquals(2, counts.single().count)
    }

    private suspend fun assertFailsWithIllegalArgument(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException.")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    private class FakeDiaryEntryDao : DiaryEntryDao {
        private val entries = linkedMapOf<Long, DiaryEntryEntity>()
        private var nextId = 1L

        override suspend fun insert(entry: DiaryEntryEntity): Long {
            val id = nextId++
            entries[id] = entry.copy(id = id)
            return id
        }

        override suspend fun update(entry: DiaryEntryEntity): Int {
            if (!entries.containsKey(entry.id)) return 0
            entries[entry.id] = entry
            return 1
        }

        override suspend fun delete(entry: DiaryEntryEntity): Int {
            return if (entries.remove(entry.id) != null) 1 else 0
        }

        override fun observeById(id: Long): Flow<DiaryEntryEntity?> = flowOf(entries[id])

        override suspend fun getById(id: Long): DiaryEntryEntity? = entries[id]

        override fun observeAll(): Flow<List<DiaryEntryEntity>> = flowOf(entries.values.toList())

        override fun observeRecent(limit: Int): Flow<List<DiaryEntryEntity>> =
            flowOf(entries.values.take(limit).toList())

        override fun observeByDate(entryDateEpochDay: Long): Flow<List<DiaryEntryEntity>> =
            flowOf(entries.values.filter { it.entryDateEpochDay == entryDateEpochDay })

        override fun observeDateCountsInRange(
            startEpochDay: Long,
            endEpochDay: Long,
        ): Flow<List<EntryDateCount>> {
            val counts = entries.values
                .filter { it.entryDateEpochDay >= startEpochDay && it.entryDateEpochDay < endEpochDay }
                .groupingBy { it.entryDateEpochDay }
                .eachCount()
                .map { (date, count) -> EntryDateCount(date, count) }
            return flowOf(counts)
        }

        override fun search(escapedQuery: String): Flow<List<DiaryEntryEntity>> = flowOf(emptyList())

        override suspend fun getAllForExport(): List<DiaryEntryEntity> = entries.values.toList()

        fun stored(id: Long): DiaryEntryEntity = entries.getValue(id)
    }

    private class FakeMoodDao : MoodDao {
        private var moods: List<MoodEntity> = emptyList()

        override suspend fun upsertAll(moods: List<MoodEntity>) {
            this.moods = moods
        }

        override fun observeActive(): Flow<List<MoodEntity>> = flowOf(moods.filter { it.isActive })
    }
}

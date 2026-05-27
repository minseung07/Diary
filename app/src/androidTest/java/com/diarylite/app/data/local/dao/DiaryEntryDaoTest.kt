package com.diarylite.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diarylite.app.data.local.DiaryDatabase
import com.diarylite.app.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiaryEntryDaoTest {
    private lateinit var database: DiaryDatabase
    private lateinit var dao: DiaryEntryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DiaryDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        dao = database.diaryEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeAllUsesCanonicalRecentOrdering() = runBlocking {
        insertEntry(id = 1, entryDateEpochDay = 10, entryTimeMinute = null, updatedAt = 100)
        insertEntry(id = 2, entryDateEpochDay = 10, entryTimeMinute = 600, updatedAt = 100)
        insertEntry(id = 3, entryDateEpochDay = 10, entryTimeMinute = 700, updatedAt = 50)
        insertEntry(id = 4, entryDateEpochDay = 11, entryTimeMinute = null, updatedAt = 10)
        insertEntry(id = 5, entryDateEpochDay = 10, entryTimeMinute = 600, updatedAt = 200)
        insertEntry(id = 6, entryDateEpochDay = 10, entryTimeMinute = 600, updatedAt = 200)

        val ids = dao.observeAll().first().map { it.id }

        assertEquals(listOf(4L, 3L, 6L, 5L, 2L, 1L), ids)
    }

    @Test
    fun getAllForExportUsesCanonicalExportOrdering() = runBlocking {
        insertEntry(id = 10, entryDateEpochDay = 9, entryTimeMinute = null, createdAt = 5)
        insertEntry(id = 11, entryDateEpochDay = 8, entryTimeMinute = 600, createdAt = 2)
        insertEntry(id = 12, entryDateEpochDay = 8, entryTimeMinute = 500, createdAt = 9)
        insertEntry(id = 13, entryDateEpochDay = 8, entryTimeMinute = null, createdAt = 1)
        insertEntry(id = 14, entryDateEpochDay = 8, entryTimeMinute = 500, createdAt = 3)

        val ids = dao.getAllForExport().map { it.id }

        assertEquals(listOf(14L, 12L, 11L, 13L, 10L), ids)
    }

    @Test
    fun searchTreatsEscapedLikeWildcardsLiterally() = runBlocking {
        insertEntry(id = 20, title = "100% real", content = "plain")
        insertEntry(id = 21, title = "1000 real", content = "plain")
        insertEntry(id = 22, title = "under_score", content = "plain")
        insertEntry(id = 23, title = "underXscore", content = "plain")

        val percentIds = dao.search("%100\\%%").first().map { it.id }
        val underscoreIds = dao.search("%under\\_score%").first().map { it.id }

        assertEquals(listOf(20L), percentIds)
        assertEquals(listOf(22L), underscoreIds)
    }

    @Test
    fun observeDateCountsInRangeUsesHalfOpenRange() = runBlocking {
        insertEntry(id = 30, entryDateEpochDay = 100)
        insertEntry(id = 31, entryDateEpochDay = 100)
        insertEntry(id = 32, entryDateEpochDay = 101)

        val counts = dao.observeDateCountsInRange(
            startEpochDay = 100,
            endEpochDay = 101,
        ).first()

        assertEquals(listOf(EntryDateCount(entryDateEpochDay = 100, count = 2)), counts)
    }

    private suspend fun insertEntry(
        id: Long,
        title: String? = null,
        content: String = "content-$id",
        entryDateEpochDay: Long = 10,
        entryTimeMinute: Int? = null,
        moodCode: String? = null,
        createdAt: Long = id,
        updatedAt: Long = id,
    ) {
        dao.insert(
            DiaryEntryEntity(
                id = id,
                title = title,
                content = content,
                entryDateEpochDay = entryDateEpochDay,
                entryTimeMinute = entryTimeMinute,
                moodCode = moodCode,
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
        )
    }
}

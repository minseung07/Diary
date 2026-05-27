package com.diarylite.app.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class DiaryEntryEntityTest {
    @Test
    fun acceptsNullAndBoundaryEntryTimes() {
        assertNull(entity(entryTimeMinute = null).entryTimeMinute)
        assertEquals(0, entity(entryTimeMinute = 0).entryTimeMinute)
        assertEquals(1_439, entity(entryTimeMinute = 1_439).entryTimeMinute)
    }

    @Test
    fun rejectsOutOfRangeEntryTimes() {
        assertInvalidEntryTime(-1)
        assertInvalidEntryTime(1_440)
    }

    private fun assertInvalidEntryTime(entryTimeMinute: Int) {
        try {
            entity(entryTimeMinute = entryTimeMinute)
            fail("Expected IllegalArgumentException.")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    private fun entity(entryTimeMinute: Int?): DiaryEntryEntity = DiaryEntryEntity(
        id = 1L,
        title = null,
        content = "content",
        entryDateEpochDay = 20_000L,
        entryTimeMinute = entryTimeMinute,
        moodCode = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )
}

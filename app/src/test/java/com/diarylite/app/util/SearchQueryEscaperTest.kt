package com.diarylite.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchQueryEscaperTest {
    @Test
    fun blankQueryReturnsNull() {
        assertNull("   \n\t  ".toEscapedLikeQueryOrNull())
    }

    @Test
    fun queryIsTrimmedAndWrappedForLike() {
        assertEquals("%오늘 일기%", "  오늘 일기  ".toEscapedLikeQueryOrNull())
    }

    @Test
    fun likeWildcardsAndEscapeCharacterAreEscaped() {
        assertEquals("%a\\\\b\\%c\\_d%", "a\\b%c_d".toEscapedLikeQueryOrNull())
    }
}

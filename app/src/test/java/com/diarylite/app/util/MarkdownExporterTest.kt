package com.diarylite.app.util

import com.diarylite.app.domain.model.DiaryEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MarkdownExporterTest {
    private val labels = MarkdownExportLabels(
        title = "Diary Lite 내보내기",
        exportedAt = "내보낸 시각",
        untitled = "제목 없음",
        time = "시간",
        mood = "기분",
    )

    @Test
    fun exportUsesLocalizedLabelsAndPreservesLineBreaks() {
        val zoneId = ZoneId.of("Asia/Seoul")
        val exportedAtMillis = LocalDateTime.of(2026, 5, 24, 22, 30)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        val markdown = MarkdownExporter.export(
            entries = listOf(
                DiaryEntry(
                    id = 1L,
                    title = null,
                    content = "첫 줄\n둘째 줄",
                    entryDateEpochDay = LocalDate.of(2026, 5, 23).toEpochDay(),
                    entryTimeMinute = null,
                    moodCode = null,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
            labels = labels,
            moodLabel = { error("Mood label should not be requested for entries without mood.") },
            exportedAtMillis = exportedAtMillis,
            zoneId = zoneId,
        )

        val expected = """
            # Diary Lite 내보내기

            내보낸 시각: 2026-05-24 22:30

            ---

            ## 2026-05-23

            ### 제목 없음

            첫 줄
            둘째 줄

            ---

        """.trimIndent()

        assertEquals(expected, markdown)
    }

    @Test
    fun exportIncludesOptionalTimeAndMoodWhenPresent() {
        val zoneId = ZoneId.of("Asia/Seoul")
        val exportedAtMillis = LocalDateTime.of(2026, 5, 24, 22, 30)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        val markdown = MarkdownExporter.export(
            entries = listOf(
                DiaryEntry(
                    id = 2L,
                    title = "하루",
                    content = "좋은 하루였다.",
                    entryDateEpochDay = LocalDate.of(2026, 5, 24).toEpochDay(),
                    entryTimeMinute = 9 * 60 + 5,
                    moodCode = "happy",
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
            labels = labels,
            moodLabel = { code -> if (code == "happy") "기쁨" else "선택 안 함" },
            exportedAtMillis = exportedAtMillis,
            zoneId = zoneId,
        )

        val expected = """
            # Diary Lite 내보내기

            내보낸 시각: 2026-05-24 22:30

            ---

            ## 2026-05-24

            ### 하루

            - 시간: 09:05
            - 기분: 기쁨

            좋은 하루였다.

            ---

        """.trimIndent()

        assertEquals(expected, markdown)
    }
}

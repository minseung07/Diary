package com.diarylite.app.util

import com.diarylite.app.domain.model.DiaryEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MarkdownExportLabels(
    val title: String,
    val exportedAt: String,
    val untitled: String,
    val time: String,
    val mood: String,
)

object MarkdownExporter {
    private val exportedAtFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREAN)

    fun export(
        entries: List<DiaryEntry>,
        labels: MarkdownExportLabels,
        moodLabel: (String) -> String,
        exportedAtMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String {
        val exportedAt = Instant.ofEpochMilli(exportedAtMillis)
            .atZone(zoneId)
            .toLocalDateTime()
            .format(exportedAtFormatter)

        return buildString {
            appendLine("# ${labels.title}")
            appendLine()
            appendLine("${labels.exportedAt}: $exportedAt")
            appendLine()
            appendLine("---")
            entries.forEach { entry ->
                appendLine()
                appendLine("## ${java.time.LocalDate.ofEpochDay(entry.entryDateEpochDay).formatExportDate()}")
                appendLine()
                appendLine("### ${entry.title?.takeIf { it.isNotBlank() } ?: labels.untitled}")
                appendLine()
                entry.entryTimeMinute?.let { minute ->
                    appendLine("- ${labels.time}: ${minute.toLocalTime().formatInputTime()}")
                }
                entry.moodCode?.let { code ->
                    appendLine("- ${labels.mood}: ${moodLabel(code)}")
                }
                if (entry.entryTimeMinute != null || entry.moodCode != null) {
                    appendLine()
                }
                appendLine(entry.content)
                appendLine()
                appendLine("---")
            }
        }
    }
}

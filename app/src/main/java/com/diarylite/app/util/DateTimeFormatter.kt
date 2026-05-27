package com.diarylite.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val koreanLocale: Locale = Locale.KOREAN
private val dateInputFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val timeInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", koreanLocale)
private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", koreanLocale)
private val exportDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun LocalDate.formatShortDate(): String = format(shortDateFormatter)

fun LocalDate.formatInputDate(): String = format(dateInputFormatter)

fun LocalDate.formatExportDate(): String = format(exportDateFormatter)

fun LocalTime.formatInputTime(): String = format(timeInputFormatter)

fun parseInputDate(value: String): LocalDate? = try {
    LocalDate.parse(value.trim(), dateInputFormatter)
} catch (_: DateTimeParseException) {
    null
}

fun parseInputTime(value: String): LocalTime? = try {
    LocalTime.parse(value.trim(), timeInputFormatter)
} catch (_: DateTimeParseException) {
    null
}

fun Int.toLocalTime(): LocalTime = LocalTime.of(this / 60, this % 60)

fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

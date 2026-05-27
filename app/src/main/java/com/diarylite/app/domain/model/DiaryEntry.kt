package com.diarylite.app.domain.model

data class DiaryEntry(
    val id: Long = 0,
    val title: String?,
    val content: String,
    val entryDateEpochDay: Long,
    val entryTimeMinute: Int?,
    val moodCode: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

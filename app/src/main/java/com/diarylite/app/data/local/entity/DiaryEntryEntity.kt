package com.diarylite.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diarylite.app.domain.model.DiaryEntry

@Entity(
    tableName = "diary_entries",
    indices = [
        Index(value = ["entryDateEpochDay"]),
        Index(value = ["updatedAt"]),
    ],
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String?,
    val content: String,
    val entryDateEpochDay: Long,
    val entryTimeMinute: Int?,
    val moodCode: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun DiaryEntryEntity.toDomain(): DiaryEntry = DiaryEntry(
    id = id,
    title = title,
    content = content,
    entryDateEpochDay = entryDateEpochDay,
    entryTimeMinute = entryTimeMinute,
    moodCode = moodCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DiaryEntry.toEntity(): DiaryEntryEntity = DiaryEntryEntity(
    id = id,
    title = title,
    content = content,
    entryDateEpochDay = entryDateEpochDay,
    entryTimeMinute = entryTimeMinute,
    moodCode = moodCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

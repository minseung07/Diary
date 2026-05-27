package com.diarylite.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diarylite.app.domain.model.Mood

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey
    val code: String,
    val sortOrder: Int,
    val isActive: Boolean,
)

fun MoodEntity.toDomain(): Mood = Mood(
    code = code,
    sortOrder = sortOrder,
    isActive = isActive,
)

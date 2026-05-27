package com.diarylite.app.data.local

import com.diarylite.app.data.local.entity.MoodEntity

fun defaultMoodEntities(): List<MoodEntity> = listOf(
    MoodEntity(code = "happy", sortOrder = 10, isActive = true),
    MoodEntity(code = "calm", sortOrder = 20, isActive = true),
    MoodEntity(code = "normal", sortOrder = 30, isActive = true),
    MoodEntity(code = "tired", sortOrder = 40, isActive = true),
    MoodEntity(code = "sad", sortOrder = 50, isActive = true),
    MoodEntity(code = "angry", sortOrder = 60, isActive = true),
    MoodEntity(code = "anxious", sortOrder = 70, isActive = true),
    MoodEntity(code = "excited", sortOrder = 80, isActive = true),
    MoodEntity(code = "grateful", sortOrder = 90, isActive = true),
)

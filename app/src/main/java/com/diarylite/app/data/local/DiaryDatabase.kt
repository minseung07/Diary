package com.diarylite.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diarylite.app.data.local.dao.DiaryEntryDao
import com.diarylite.app.data.local.dao.MoodDao
import com.diarylite.app.data.local.entity.DiaryEntryEntity
import com.diarylite.app.data.local.entity.MoodEntity

@Database(
    entities = [
        DiaryEntryEntity::class,
        MoodEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun moodDao(): MoodDao
}

package com.diarylite.app

import android.app.Application
import androidx.room.Room
import com.diarylite.app.data.local.DiaryDatabase
import com.diarylite.app.data.repository.DiaryRepositoryImpl
import com.diarylite.app.domain.repository.DiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DiaryLiteApplication : Application() {
    lateinit var container: DiaryAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DiaryAppContainer(this)
    }
}

class DiaryAppContainer(application: Application) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: DiaryDatabase = Room.databaseBuilder(
        application,
        DiaryDatabase::class.java,
        "diary_lite.db",
    ).build()

    val repository: DiaryRepository = DiaryRepositoryImpl(
        diaryEntryDao = database.diaryEntryDao(),
        moodDao = database.moodDao(),
    )

    init {
        applicationScope.launch {
            repository.seedMoods()
        }
    }
}

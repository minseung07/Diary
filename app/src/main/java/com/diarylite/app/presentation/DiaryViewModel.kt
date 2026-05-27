@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.diarylite.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diarylite.app.data.local.dao.EntryDateCount
import com.diarylite.app.domain.model.DiaryEntry
import com.diarylite.app.domain.model.Mood
import com.diarylite.app.domain.repository.DiaryRepository
import com.diarylite.app.util.MarkdownExportLabels
import com.diarylite.app.util.MarkdownExporter
import com.diarylite.app.util.toEscapedLikeQueryOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class EditorResult(
    val success: Boolean,
    val entryId: Long? = null,
)

class DiaryViewModel(
    private val repository: DiaryRepository,
) : ViewModel() {
    val recentEntries: StateFlow<List<DiaryEntry>> = repository.observeRecentEntries(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allEntries: StateFlow<List<DiaryEntry>> = repository.observeAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val moods: StateFlow<List<Mood>> = repository.observeActiveMoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchErrorEvents = MutableSharedFlow<Unit>()
    val searchErrorEvents: SharedFlow<Unit> = _searchErrorEvents.asSharedFlow()

    val searchResults: StateFlow<List<DiaryEntry>> = _searchQuery
        .map { it.toEscapedLikeQueryOrNull() }
        .flatMapLatest { escapedQuery ->
            if (escapedQuery == null) {
                flowOf(emptyList())
            } else {
                repository.searchEntries(escapedQuery)
                    .catch {
                        _searchErrorEvents.emit(Unit)
                        emit(emptyList())
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _visibleMonth = MutableStateFlow(YearMonth.now())
    val visibleMonth: StateFlow<YearMonth> = _visibleMonth

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    val monthEntryCounts: StateFlow<List<EntryDateCount>> = _visibleMonth
        .flatMapLatest { month ->
            repository.observeEntryCountsInRange(
                startEpochDay = month.atDay(1).toEpochDay(),
                endEpochDay = month.plusMonths(1).atDay(1).toEpochDay(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedDateEntries: StateFlow<List<DiaryEntry>> = _selectedDate
        .flatMapLatest { date -> repository.observeEntriesByDate(date.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeEntry(id: Long): Flow<DiaryEntry?> = repository.observeEntry(id)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun previousMonth() {
        moveVisibleMonth(_visibleMonth.value.minusMonths(1))
    }

    fun nextMonth() {
        moveVisibleMonth(_visibleMonth.value.plusMonths(1))
    }

    fun returnToToday() {
        val today = LocalDate.now()
        _visibleMonth.value = YearMonth.from(today)
        _selectedDate.value = today
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _visibleMonth.value = YearMonth.from(date)
    }

    private fun moveVisibleMonth(month: YearMonth) {
        _visibleMonth.value = month
        if (YearMonth.from(_selectedDate.value) != month) {
            val day = minOf(_selectedDate.value.dayOfMonth, month.lengthOfMonth())
            _selectedDate.value = month.atDay(day)
        }
    }

    suspend fun saveNewEntry(
        title: String?,
        content: String,
        date: LocalDate,
        time: LocalTime?,
        moodCode: String?,
    ): EditorResult = runCatching {
        val id = repository.createEntry(
            title = title,
            content = content,
            entryDateEpochDay = date.toEpochDay(),
            entryTimeMinute = time?.let { it.hour * 60 + it.minute },
            moodCode = moodCode,
        )
        EditorResult(success = true, entryId = id)
    }.getOrElse {
        EditorResult(success = false)
    }

    suspend fun updateExistingEntry(
        entryId: Long,
        title: String?,
        content: String,
        date: LocalDate,
        time: LocalTime?,
        moodCode: String?,
    ): EditorResult {
        val existing = repository.getEntry(entryId) ?: return EditorResult(success = false)
        return runCatching {
            val updated = existing.copy(
                title = title,
                content = content,
                entryDateEpochDay = date.toEpochDay(),
                entryTimeMinute = time?.let { it.hour * 60 + it.minute },
                moodCode = moodCode,
            )
            EditorResult(success = repository.updateEntry(updated), entryId = entryId)
        }.getOrElse {
            EditorResult(success = false)
        }
    }

    suspend fun deleteEntry(entryId: Long): Boolean = runCatching {
        repository.deleteEntry(entryId)
    }.getOrDefault(false)

    suspend fun buildMarkdownExport(
        labels: MarkdownExportLabels,
        moodLabel: (String) -> String,
    ): String {
        val entries = repository.getEntriesForExport()
        return MarkdownExporter.export(
            entries = entries,
            labels = labels,
            moodLabel = moodLabel,
        )
    }
}

class DiaryViewModelFactory(
    private val repository: DiaryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            return DiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class.")
    }
}

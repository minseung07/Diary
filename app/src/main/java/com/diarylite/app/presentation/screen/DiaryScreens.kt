@file:OptIn(ExperimentalMaterial3Api::class)

package com.diarylite.app.presentation.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diarylite.app.R
import com.diarylite.app.domain.model.DiaryEntry
import com.diarylite.app.domain.model.Mood
import com.diarylite.app.presentation.DiaryViewModel
import com.diarylite.app.util.MarkdownExportLabels
import com.diarylite.app.util.formatFullKoreanDate
import com.diarylite.app.util.formatInputDate
import com.diarylite.app.util.formatInputTime
import com.diarylite.app.util.formatKoreanMonth
import com.diarylite.app.util.formatShortDate
import com.diarylite.app.util.parseInputDate
import com.diarylite.app.util.parseInputTime
import com.diarylite.app.util.toLocalTime
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: DiaryViewModel,
    onWriteToday: () -> Unit,
    onOpenEntries: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.content_description_settings),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = LocalDate.now().formatFullKoreanDate(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onWriteToday,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.today_diary))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onOpenEntries,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.all_entries))
                    }
                    Button(
                        onClick = onOpenCalendar,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.calendar))
                    }
                    Button(
                        onClick = onOpenSearch,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.search))
                    }
                }
            }
            item {
                SectionHeader(
                    title = stringResource(R.string.recent_entries),
                    actionText = stringResource(R.string.view_all),
                    onAction = onOpenEntries,
                )
            }
            if (recentEntries.isEmpty()) {
                item {
                    EmptyMessage(text = stringResource(R.string.empty_home))
                }
            } else {
                items(recentEntries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
fun EntriesScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val entries by viewModel.allEntries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.all_entries)) },
                navigationIcon = { BackIconButton(onBack) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entry))
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyMessage(text = stringResource(R.string.empty_entries))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
fun DetailScreen(
    viewModel: DiaryViewModel,
    entryId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val entry by viewModel.observeEntry(entryId).collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val deleteFailed = stringResource(R.string.delete_failed)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.entry_detail)) },
                navigationIcon = { BackIconButton(onBack) },
                actions = {
                    IconButton(onClick = onEdit, enabled = entry != null) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.content_description_edit),
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }, enabled = entry != null) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.content_description_delete),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val current = entry
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyMessage(text = stringResource(R.string.entry_not_found))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(
                        text = current.title?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.untitled),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    EntryMetaRow(entry = current)
                }
                item {
                    Text(
                        text = current.content,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_dialog_title),
            message = stringResource(R.string.delete_dialog_message),
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    val deleted = viewModel.deleteEntry(entryId)
                    if (deleted) {
                        onDeleted()
                    } else {
                        snackbarHostState.showSnackbar(deleteFailed)
                    }
                }
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
fun EditorScreen(
    viewModel: DiaryViewModel,
    entryId: Long?,
    initialDateEpochDay: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val moods by viewModel.moods.collectAsStateWithLifecycle()
    val existingEntry by if (entryId == null) {
        remember { mutableStateOf<DiaryEntry?>(null) }
    } else {
        viewModel.observeEntry(entryId).collectAsStateWithLifecycle(initialValue = null)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailed = stringResource(R.string.save_failed)
    val invalidDate = stringResource(R.string.invalid_date)
    val invalidTime = stringResource(R.string.invalid_time)

    val defaultDate = remember(initialDateEpochDay) {
        initialDateEpochDay?.let(LocalDate::ofEpochDay) ?: LocalDate.now()
    }
    val defaultTime = remember { LocalTime.now().withSecond(0).withNano(0) }

    var title by rememberSaveable(entryId, initialDateEpochDay) { mutableStateOf("") }
    var content by rememberSaveable(entryId, initialDateEpochDay) { mutableStateOf("") }
    var dateText by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf(defaultDate.formatInputDate())
    }
    var timeText by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf(defaultTime.formatInputTime())
    }
    var selectedMoodCode by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf<String?>(null)
    }
    var originalKey by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf(editorKey(title, content, dateText, timeText, selectedMoodCode))
    }
    var initializedEdit by rememberSaveable(entryId) { mutableStateOf(entryId == null) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(existingEntry?.id) {
        val entry = existingEntry
        if (entryId != null && entry != null && !initializedEdit) {
            title = entry.title.orEmpty()
            content = entry.content
            dateText = LocalDate.ofEpochDay(entry.entryDateEpochDay).formatInputDate()
            timeText = entry.entryTimeMinute?.toLocalTime()?.formatInputTime().orEmpty()
            selectedMoodCode = entry.moodCode
            originalKey = editorKey(title, content, dateText, timeText, selectedMoodCode)
            initializedEdit = true
        }
    }

    val currentKey = editorKey(title, content, dateText, timeText, selectedMoodCode)
    val hasUnsavedChanges = currentKey != originalKey
    val parsedDate = parseInputDate(dateText)
    val parsedTime = if (timeText.isBlank()) null else parseInputTime(timeText)
    val hasValidTime = timeText.isBlank() || parsedTime != null
    val canSave = content.trim().isNotEmpty() && parsedDate != null && hasValidTime

    fun requestBack() {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = true) {
        requestBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (entryId == null) {
                            stringResource(R.string.add_entry)
                        } else {
                            stringResource(R.string.edit_entry)
                        },
                    )
                },
                navigationIcon = { BackIconButton(::requestBack) },
                actions = {
                    IconButton(
                        enabled = canSave,
                        onClick = {
                            val safeDate = parsedDate
                            if (safeDate == null) {
                                scope.launch { snackbarHostState.showSnackbar(invalidDate) }
                                return@IconButton
                            }
                            if (!hasValidTime) {
                                scope.launch { snackbarHostState.showSnackbar(invalidTime) }
                                return@IconButton
                            }
                            scope.launch {
                                val result = if (entryId == null) {
                                    viewModel.saveNewEntry(
                                        title = title,
                                        content = content,
                                        date = safeDate,
                                        time = parsedTime,
                                        moodCode = selectedMoodCode,
                                    )
                                } else {
                                    viewModel.updateExistingEntry(
                                        entryId = entryId,
                                        title = title,
                                        content = content,
                                        date = safeDate,
                                        time = parsedTime,
                                        moodCode = selectedMoodCode,
                                    )
                                }
                                val savedId = result.entryId
                                if (result.success && savedId != null) {
                                    originalKey = currentKey
                                    onSaved(savedId)
                                } else {
                                    snackbarHostState.showSnackbar(saveFailed)
                                }
                            }
                        },
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_label)) },
                    placeholder = { Text(stringResource(R.string.title_optional_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(R.string.content_label)) },
                    placeholder = { Text(stringResource(R.string.content_hint)) },
                    minLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (content.trim().isEmpty()) {
                            Text(stringResource(R.string.content_required))
                        }
                    },
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text(stringResource(R.string.date_label)) },
                        singleLine = true,
                        isError = parsedDate == null,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text(stringResource(R.string.time_label)) },
                        placeholder = { Text(stringResource(R.string.time_hint)) },
                        singleLine = true,
                        isError = !hasValidTime,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.mood_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(8.dp))
                MoodSelector(
                    moods = moods,
                    selectedMoodCode = selectedMoodCode,
                    onSelected = { selectedMoodCode = it },
                )
            }
            item {
                TextButton(onClick = { timeText = "" }) {
                    Text(stringResource(R.string.clear_time))
                }
            }
        }
    }

    if (showUnsavedDialog) {
        ConfirmDialog(
            title = stringResource(R.string.unsaved_dialog_title),
            message = stringResource(R.string.unsaved_dialog_message),
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                showUnsavedDialog = false
                onBack()
            },
            onDismiss = { showUnsavedDialog = false },
        )
    }
}

@Composable
fun CalendarScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit,
    onAddForDate: (Long) -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val visibleMonth by viewModel.visibleMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dateCounts by viewModel.monthEntryCounts.collectAsStateWithLifecycle()
    val selectedEntries by viewModel.selectedDateEntries.collectAsStateWithLifecycle()
    val countsByDate = remember(dateCounts) { dateCounts.associate { it.entryDateEpochDay to it.count } }
    val weekdays = stringArrayResource(R.array.calendar_weekdays).toList()
    val calendarDates = remember(visibleMonth) { visibleMonth.calendarGridDates() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
                navigationIcon = { BackIconButton(onBack) },
                actions = {
                    TextButton(onClick = viewModel::returnToToday) {
                        Text(stringResource(R.string.go_today))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MonthHeader(
                    visibleMonth = visibleMonth,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                )
            }
            item {
                Row(Modifier.fillMaxWidth()) {
                    weekdays.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp),
                    userScrollEnabled = false,
                ) {
                    items(calendarDates) { date ->
                        CalendarDayCell(
                            date = date,
                            visibleMonth = visibleMonth,
                            selectedDate = selectedDate,
                            entryCount = countsByDate[date.toEpochDay()] ?: 0,
                            onClick = { viewModel.selectDate(date) },
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { onAddForDate(selectedDate.toEpochDay()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.write_selected_date))
                }
            }
            item {
                Text(
                    text = selectedDate.formatFullKoreanDate(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (selectedEntries.isEmpty()) {
                item { EmptyMessage(text = stringResource(R.string.empty_date_entries)) }
            } else {
                items(selectedEntries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val searchFailed = stringResource(R.string.search_failed)

    LaunchedEffect(viewModel) {
        viewModel.searchErrorEvents.collect {
            snackbarHostState.showSnackbar(searchFailed)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.search)) },
                navigationIcon = { BackIconButton(onBack) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setSearchQuery,
                    label = { Text(stringResource(R.string.search)) },
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.trim().isEmpty()) {
                item { EmptyMessage(text = stringResource(R.string.empty_search)) }
            } else if (results.isEmpty()) {
                item { EmptyMessage(text = stringResource(R.string.empty_search_results)) }
            } else {
                items(results, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val exportFailed = stringResource(R.string.export_failed)
    val exportSuccess = stringResource(R.string.export_success)
    val exportCanceled = stringResource(R.string.file_creation_canceled)
    val labels = MarkdownExportLabels(
        title = stringResource(R.string.export_title),
        exportedAt = stringResource(R.string.exported_at_label),
        untitled = stringResource(R.string.untitled),
        time = stringResource(R.string.export_time_label),
        mood = stringResource(R.string.export_mood_label),
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode != Activity.RESULT_OK || uri == null) {
            scope.launch { snackbarHostState.showSnackbar(exportCanceled) }
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val exported = runCatching {
                val markdown = viewModel.buildMarkdownExport(
                    labels = labels,
                    moodLabel = { code -> context.moodLabel(code) },
                )
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(markdown.toByteArray(StandardCharsets.UTF_8))
                } ?: error("Output stream is null.")
            }.isSuccess

            snackbarHostState.showSnackbar(if (exported) exportSuccess else exportFailed)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { BackIconButton(onBack) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.markdown_export),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.export_description),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(
                            onClick = { launcher.launch(createExportDocumentIntent(suggestedExportFilename())) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.export_all_entries))
                        }
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(stringResource(R.string.privacy_line_one))
                        Text(stringResource(R.string.privacy_line_two))
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.app_info_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(stringResource(R.string.app_info_body))
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = entry.title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.untitled),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
            EntryMetaRow(entry = entry)
            Text(
                text = entry.content.previewText(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EntryMetaRow(entry: DiaryEntry) {
    val date = LocalDate.ofEpochDay(entry.entryDateEpochDay)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = {},
            label = { Text(date.formatShortDate()) },
        )
        entry.entryTimeMinute?.let { minute ->
            AssistChip(
                onClick = {},
                label = { Text(minute.toLocalTime().formatInputTime()) },
            )
        }
        entry.moodCode?.let { code ->
            AssistChip(
                onClick = {},
                label = { Text(moodLabel(code)) },
            )
        }
    }
}

@Composable
private fun MoodSelector(
    moods: List<Mood>,
    selectedMoodCode: String?,
    onSelected: (String?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            AssistChip(
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.mood_none)) },
                leadingIcon = {
                    if (selectedMoodCode == null) SelectedDot()
                },
            )
        }
        items(moods, key = { it.code }) { mood ->
            AssistChip(
                onClick = { onSelected(mood.code) },
                label = { Text(moodLabel(mood.code)) },
                leadingIcon = {
                    if (selectedMoodCode == mood.code) SelectedDot()
                },
            )
        }
    }
}

@Composable
private fun SelectedDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}

@Composable
private fun moodLabel(code: String): String = when (code) {
    "happy" -> stringResource(R.string.mood_happy)
    "calm" -> stringResource(R.string.mood_calm)
    "normal" -> stringResource(R.string.mood_normal)
    "tired" -> stringResource(R.string.mood_tired)
    "sad" -> stringResource(R.string.mood_sad)
    "angry" -> stringResource(R.string.mood_angry)
    "anxious" -> stringResource(R.string.mood_anxious)
    "excited" -> stringResource(R.string.mood_excited)
    "grateful" -> stringResource(R.string.mood_grateful)
    else -> stringResource(R.string.mood_none)
}

private fun Context.moodLabel(code: String): String = when (code) {
    "happy" -> getString(R.string.mood_happy)
    "calm" -> getString(R.string.mood_calm)
    "normal" -> getString(R.string.mood_normal)
    "tired" -> getString(R.string.mood_tired)
    "sad" -> getString(R.string.mood_sad)
    "angry" -> getString(R.string.mood_angry)
    "anxious" -> getString(R.string.mood_anxious)
    "excited" -> getString(R.string.mood_excited)
    "grateful" -> getString(R.string.mood_grateful)
    else -> getString(R.string.mood_none)
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onAction) {
            Text(actionText)
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackIconButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}

@Composable
private fun MonthHeader(
    visibleMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.previous_month),
            )
        }
        Text(
            text = visibleMonth.formatKoreanMonth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.next_month),
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entryCount: Int,
    onClick: () -> Unit,
) {
    val inMonth = YearMonth.from(date) == visibleMonth
    val isSelected = date == selectedDate
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (entryCount > 0) {
            Text(
                text = entryCount.toString(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun YearMonth.calendarGridDates(): List<LocalDate> {
    val firstDay = atDay(1)
    val sundayStartOffset = firstDay.dayOfWeek.value % 7
    val gridStart = firstDay.minusDays(sundayStartOffset.toLong())
    return List(42) { index -> gridStart.plusDays(index.toLong()) }
}

private fun String.previewText(): String {
    val compact = lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .ifBlank { this }
    return if (compact.length <= 90) compact else compact.take(90) + "..."
}

private fun editorKey(
    title: String,
    content: String,
    dateText: String,
    timeText: String,
    moodCode: String?,
): String = listOf(title, content, dateText, timeText, moodCode.orEmpty()).joinToString(separator = "\u0001")

private fun suggestedExportFilename(): String {
    val timestamp = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.KOREAN),
    )
    return "diary_export_$timestamp.md"
}

private fun createExportDocumentIntent(filename: String): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "text/markdown"
    putExtra(Intent.EXTRA_TITLE, filename)
    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/markdown", "text/plain"))
}

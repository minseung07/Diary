@file:OptIn(ExperimentalMaterial3Api::class)

package com.diarylite.app.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diarylite.app.R
import com.diarylite.app.domain.model.Mood
import com.diarylite.app.presentation.DiaryViewModel
import com.diarylite.app.util.formatInputDate
import com.diarylite.app.util.formatInputTime
import com.diarylite.app.util.parseInputDate
import com.diarylite.app.util.parseInputTime
import com.diarylite.app.util.toLocalTime
import com.diarylite.app.util.toMinuteOfDay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Composable
fun DetailScreen(
    viewModel: DiaryViewModel,
    entryId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val entryState by produceState<EntryLookupState>(
        initialValue = EntryLookupState.Loading,
        key1 = viewModel,
        key2 = entryId,
    ) {
        viewModel.observeEntry(entryId).collect { entry ->
            value = if (entry == null) {
                EntryLookupState.Missing
            } else {
                EntryLookupState.Found(entry)
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val deleteFailed = stringResource(R.string.delete_failed)
    val currentEntry = (entryState as? EntryLookupState.Found)?.entry

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DiaryTopBar(
                title = stringResource(R.string.entry_detail),
                navigationIcon = { BackIconButton(onBack) },
                actions = {
                    IconButton(onClick = onEdit, enabled = currentEntry != null) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.content_description_edit),
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }, enabled = currentEntry != null) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.content_description_delete),
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val current = entryState) {
            EntryLookupState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyMessage(text = stringResource(R.string.loading_entry))
                }
            }
            EntryLookupState.Missing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyMessage(text = stringResource(R.string.entry_not_found))
                }
            }
            is EntryLookupState.Found -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = current.entry.title?.takeIf { it.isNotBlank() }
                                    ?: stringResource(R.string.untitled),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            EntryMetaRow(entry = current.entry)
                        }
                    }
                    item {
                        SectionHeader(title = stringResource(R.string.detail_content_section))
                    }
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                text = current.entry.content,
                                modifier = Modifier.padding(18.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailed = stringResource(R.string.save_failed)
    val invalidDate = stringResource(R.string.invalid_date)
    val invalidTime = stringResource(R.string.invalid_time)
    val moods by viewModel.activeMoods.collectAsStateWithLifecycle()

    val defaultDate = remember(initialDateEpochDay) {
        initialDateEpochDay?.let(LocalDate::ofEpochDay) ?: LocalDate.now()
    }
    val defaultTime = remember(entryId, initialDateEpochDay) {
        LocalTime.now().truncatedTo(ChronoUnit.MINUTES).formatInputTime()
    }

    var title by rememberSaveable(entryId, initialDateEpochDay) { mutableStateOf("") }
    var content by rememberSaveable(entryId, initialDateEpochDay) { mutableStateOf("") }
    var dateText by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf(defaultDate.formatInputDate())
    }
    var timeText by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf(defaultTime)
    }
    var selectedMoodCode by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf<String?>(null)
    }
    var originalKey by rememberSaveable(entryId, initialDateEpochDay) {
        mutableStateOf(editorKey(title, content, dateText, timeText, selectedMoodCode.orEmpty()))
    }
    var editLoadCompleted by rememberSaveable(entryId) { mutableStateOf(entryId == null) }
    var editEntryExists by rememberSaveable(entryId) { mutableStateOf(entryId == null) }
    var saveInProgress by rememberSaveable(entryId, initialDateEpochDay) { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(entryId, editLoadCompleted) {
        if (entryId != null && !editLoadCompleted) {
            val entry = viewModel.getEntryForEditor(entryId)
            if (entry == null) {
                editEntryExists = false
            } else {
                title = entry.title.orEmpty()
                content = entry.content
                dateText = LocalDate.ofEpochDay(entry.entryDateEpochDay).formatInputDate()
                timeText = entry.entryTimeMinute?.toLocalTime()?.formatInputTime().orEmpty()
                selectedMoodCode = entry.moodCode
                originalKey = editorKey(title, content, dateText, timeText, selectedMoodCode.orEmpty())
                editEntryExists = true
            }
            editLoadCompleted = true
        }
    }

    val currentKey = editorKey(title, content, dateText, timeText, selectedMoodCode.orEmpty())
    val hasUnsavedChanges = currentKey != originalKey
    val parsedDate = parseInputDate(dateText)
    val parsedTime = timeText.trim().takeIf { it.isNotEmpty() }?.let(::parseInputTime)
    val isTimeValid = timeText.trim().isEmpty() || parsedTime != null
    val displayedDateText = if (parsedDate == null) invalidDate else fullKoreanDateText(parsedDate)
    val canEdit = entryId == null || (editLoadCompleted && editEntryExists)
    val canSave = canEdit &&
        !saveInProgress &&
        content.trim().isNotEmpty() &&
        parsedDate != null &&
        isTimeValid

    fun requestBack() {
        if (saveInProgress) {
            return
        } else if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    fun saveEntry() {
        if (saveInProgress) {
            return
        }
        val safeDate = parsedDate
        if (safeDate == null) {
            scope.launch { snackbarHostState.showSnackbar(invalidDate) }
            return
        }
        if (!isTimeValid) {
            scope.launch { snackbarHostState.showSnackbar(invalidTime) }
            return
        }
        val safeTimeMinute = parsedTime?.toMinuteOfDay()
        saveInProgress = true
        scope.launch {
            try {
                val editingId = entryId
                val result = if (editingId == null) {
                    viewModel.saveNewEntry(
                        title = title,
                        content = content,
                        date = safeDate,
                        entryTimeMinute = safeTimeMinute,
                        moodCode = selectedMoodCode,
                    )
                } else {
                    viewModel.updateExistingEntry(
                        entryId = editingId,
                        title = title,
                        content = content,
                        date = safeDate,
                        entryTimeMinute = safeTimeMinute,
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
            } finally {
                saveInProgress = false
            }
        }
    }

    BackHandler(enabled = true) {
        requestBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DiaryTopBar(
                title = if (entryId == null) {
                    stringResource(R.string.add_entry)
                } else {
                    stringResource(R.string.edit_entry)
                },
                navigationIcon = { BackIconButton(::requestBack) },
                actions = {
                    IconButton(
                        enabled = canSave,
                        onClick = ::saveEntry,
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            entryId != null && !editLoadCompleted -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyMessage(text = stringResource(R.string.loading_entry))
                }
            }
            entryId != null && !editEntryExists -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyMessage(text = stringResource(R.string.entry_not_found))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        EditorWritingPanel(
                            title = title,
                            content = content,
                            onTitleChange = { title = it },
                            onContentChange = { content = it },
                        )
                    }
                    item {
                        EditorDateTimePanel(
                            displayedDateText = displayedDateText,
                            dateText = dateText,
                            timeText = timeText,
                            parsedDate = parsedDate,
                            isTimeValid = isTimeValid,
                            onDateChange = { dateText = it },
                            onTimeChange = { timeText = it },
                            onOpenDatePicker = { showDatePicker = true },
                            onClearTime = { timeText = "" },
                        )
                    }
                    item {
                        EditorMoodPanel(
                            moods = moods,
                            selectedMoodCode = selectedMoodCode,
                            onMoodSelected = { selectedMoodCode = it },
                        )
                    }
                    item {
                        Button(
                            onClick = ::saveEntry,
                            enabled = canSave,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(
                                    if (saveInProgress) {
                                        R.string.save_in_progress
                                    } else {
                                        R.string.save
                                    },
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (parsedDate ?: defaultDate).toDatePickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                            dateText = selectedDateMillis.toDatePickerLocalDate().formatInputDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
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
private fun EditorWritingPanel(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.editor_writing_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.title_label)) },
                placeholder = { Text(stringResource(R.string.title_optional_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text(stringResource(R.string.content_label)) },
                placeholder = { Text(stringResource(R.string.content_hint)) },
                minLines = 16,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (content.trim().isEmpty()) {
                        Text(stringResource(R.string.content_required))
                    }
                },
            )
        }
    }
}

@Composable
private fun EditorDateTimePanel(
    displayedDateText: String,
    dateText: String,
    timeText: String,
    parsedDate: LocalDate?,
    isTimeValid: Boolean,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onClearTime: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.editor_metadata_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = displayedDateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onOpenDatePicker) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.select_date),
                    )
                }
            }
            OutlinedTextField(
                value = dateText,
                onValueChange = onDateChange,
                label = { Text(stringResource(R.string.date_label)) },
                placeholder = { Text(stringResource(R.string.date_format_hint)) },
                supportingText = {
                    Text(
                        if (parsedDate == null) {
                            stringResource(R.string.invalid_date)
                        } else {
                            stringResource(R.string.date_format_hint)
                        },
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.select_date),
                        )
                    }
                },
                singleLine = true,
                isError = parsedDate == null,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = timeText,
                onValueChange = onTimeChange,
                label = { Text(stringResource(R.string.time_label)) },
                placeholder = { Text(stringResource(R.string.time_hint)) },
                supportingText = {
                    Text(
                        if (!isTimeValid) {
                            stringResource(R.string.invalid_time)
                        } else {
                            stringResource(R.string.time_format_hint)
                        },
                    )
                },
                trailingIcon = {
                    if (timeText.isNotBlank()) {
                        IconButton(onClick = onClearTime) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_time),
                            )
                        }
                    }
                },
                singleLine = true,
                isError = !isTimeValid,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EditorMoodPanel(
    moods: List<Mood>,
    selectedMoodCode: String?,
    onMoodSelected: (String?) -> Unit,
) {
    val context = LocalContext.current
    val options = remember(moods) { listOf<String?>(null) + moods.map { it.code } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.editor_mood_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            options.chunked(3).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowOptions.forEach { moodCode ->
                        FilterChip(
                            selected = selectedMoodCode == moodCode,
                            onClick = { onMoodSelected(moodCode) },
                            label = {
                                Text(
                                    text = moodCode?.let(context::moodLabel)
                                        ?: stringResource(R.string.mood_none),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun LocalDate.toDatePickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toDatePickerLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

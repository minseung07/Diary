package com.diarylite.app.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diarylite.app.R
import com.diarylite.app.presentation.DiaryViewModel
import kotlinx.coroutines.launch

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
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailed = stringResource(R.string.save_failed)

    var title by rememberSaveable(entryId) { mutableStateOf("") }
    var content by rememberSaveable(entryId) { mutableStateOf("") }
    var originalKey by rememberSaveable(entryId) {
        mutableStateOf(editorKey(title, content))
    }
    var editLoadCompleted by rememberSaveable(entryId) { mutableStateOf(entryId == null) }
    var editEntryExists by rememberSaveable(entryId) { mutableStateOf(entryId == null) }
    var saveInProgress by rememberSaveable(entryId) { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(entryId, editLoadCompleted) {
        if (entryId != null && !editLoadCompleted) {
            val entry = viewModel.getEntryForEditor(entryId)
            if (entry == null) {
                editEntryExists = false
            } else {
                title = entry.title.orEmpty()
                content = entry.content
                originalKey = editorKey(title, content)
                editEntryExists = true
            }
            editLoadCompleted = true
        }
    }

    val currentKey = editorKey(title, content)
    val hasUnsavedChanges = currentKey != originalKey
    val canEdit = entryId == null || (editLoadCompleted && editEntryExists)
    val canSave = canEdit &&
        !saveInProgress &&
        content.trim().isNotEmpty()

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
        saveInProgress = true
        scope.launch {
            try {
                val editingId = entryId
                val result = if (editingId == null) {
                    viewModel.saveNewEntry(
                        title = title,
                        content = content,
                    )
                } else {
                    viewModel.updateExistingEntry(
                        entryId = editingId,
                        title = title,
                        content = content,
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

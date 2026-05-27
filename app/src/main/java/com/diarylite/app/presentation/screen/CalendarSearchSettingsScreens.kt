@file:OptIn(ExperimentalMaterial3Api::class)

package com.diarylite.app.presentation.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diarylite.app.R
import com.diarylite.app.presentation.DiaryViewModel
import com.diarylite.app.presentation.MarkdownExportResult
import com.diarylite.app.util.MarkdownExportLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

@Composable
fun CalendarScreen(
    viewModel: DiaryViewModel,
    onAddForDate: (Long) -> Unit,
    onOpenEntry: (Long) -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    val visibleMonth by viewModel.visibleMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dateCounts by viewModel.monthEntryCounts.collectAsStateWithLifecycle()
    val selectedEntries by viewModel.selectedDateEntries.collectAsStateWithLifecycle()
    val datesWithEntries = remember(dateCounts) { dateCounts.map { it.entryDateEpochDay }.toSet() }
    val weekdays = stringArrayResource(R.array.calendar_weekdays).toList()
    val calendarDates = remember(visibleMonth) { visibleMonth.calendarGridDates() }

    Scaffold(
        topBar = {
            DiaryTopBar(
                title = stringResource(R.string.calendar),
                actions = {
                    TextButton(onClick = viewModel::returnToToday) {
                        Icon(Icons.Default.Today, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text(stringResource(R.string.go_today))
                    }
                },
            )
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MonthHeader(
                            visibleMonth = visibleMonth,
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth,
                        )
                        Row(Modifier.fillMaxWidth()) {
                            weekdays.forEach { label ->
                                Text(
                                    text = label,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            calendarDates.chunked(7).forEach { weekDates ->
                                Row(Modifier.fillMaxWidth()) {
                                    weekDates.forEach { date ->
                                        CalendarDayCell(
                                            date = date,
                                            visibleMonth = visibleMonth,
                                            selectedDate = selectedDate,
                                            hasEntry = date.toEpochDay() in datesWithEntries,
                                            onClick = { viewModel.selectDate(date) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
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
                SectionHeader(
                    title = fullKoreanDateText(selectedDate),
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
    onOpenEntry: (Long) -> Unit,
    bottomBar: @Composable () -> Unit = {},
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
            DiaryTopBar(title = stringResource(R.string.search))
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setSearchQuery,
                    label = { Text(stringResource(R.string.search)) },
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.content_description_clear_search),
                                )
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.trim().isEmpty()) {
                item { EmptyMessage(text = stringResource(R.string.empty_search), icon = Icons.Default.Search) }
            } else if (results.isEmpty()) {
                item { EmptyMessage(text = stringResource(R.string.empty_search_results), icon = Icons.Default.Search) }
            } else {
                item {
                    SectionHeader(
                        title = stringResource(R.string.search_results_count, results.size),
                    )
                }
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
    bottomBar: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val exportContext = remember(context) { context.applicationContext }
    val exportUiState by viewModel.exportUiState.collectAsStateWithLifecycle()
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
            val exportResult = viewModel.exportMarkdown(
                labels = labels,
                moodLabel = { code -> exportContext.moodLabel(code) },
            ) { markdown ->
                withContext(Dispatchers.IO) {
                    exportContext.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(markdown.toByteArray(StandardCharsets.UTF_8))
                    } ?: error("Output stream is null.")
                }
            }

            snackbarHostState.showSnackbar(
                if (exportResult == MarkdownExportResult.Success) {
                    exportSuccess
                } else {
                    exportFailed
                },
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DiaryTopBar(title = stringResource(R.string.settings))
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SettingPanel(
                    icon = Icons.Default.Save,
                    title = stringResource(R.string.markdown_export),
                    body = stringResource(R.string.export_description),
                ) {
                    Button(
                        onClick = { launcher.launch(createExportDocumentIntent(suggestedExportFilename())) },
                        enabled = !exportUiState.inProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(
                                if (exportUiState.inProgress) {
                                    R.string.export_in_progress
                                } else {
                                    R.string.export_all_entries
                                },
                            ),
                        )
                    }
                }
            }
            item {
                SettingPanel(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.privacy_title),
                    body = stringResource(R.string.privacy_line_one) + "\n" + stringResource(R.string.privacy_line_two),
                )
            }
            item {
                SettingPanel(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.app_info_title),
                    body = stringResource(R.string.app_info_body),
                )
            }
        }
    }
}

@Composable
private fun SettingPanel(
    icon: ImageVector,
    title: String,
    body: String,
    action: @Composable (() -> Unit)? = null,
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            action?.invoke()
        }
    }
}

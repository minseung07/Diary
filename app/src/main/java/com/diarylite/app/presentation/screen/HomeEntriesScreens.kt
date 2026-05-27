@file:OptIn(ExperimentalMaterial3Api::class)

package com.diarylite.app.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diarylite.app.R
import com.diarylite.app.presentation.DiaryViewModel

@Composable
fun HomeScreen(
    viewModel: DiaryViewModel,
    onWriteToday: () -> Unit,
    onOpenEntries: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            DiaryTopBar(
                title = stringResource(R.string.home_title),
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                },
            )
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.recent_entries),
                        actionText = stringResource(R.string.view_all),
                        onAction = onOpenEntries,
                    )
                }
                if (recentEntries.isEmpty()) {
                    item {
                        EmptyMessage(
                            text = stringResource(R.string.empty_home),
                            icon = Icons.Default.EditNote,
                        )
                    }
                } else {
                    items(recentEntries, key = { it.id }) { entry ->
                        EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                    }
                }
            }
            WriteTodayFab(
                onClick = onWriteToday,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
            )
        }
    }
}

@Composable
fun EntriesScreen(
    viewModel: DiaryViewModel,
    onAdd: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    val entries by viewModel.allEntries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            DiaryTopBar(title = stringResource(R.string.diary_records))
        },
        bottomBar = bottomBar,
        floatingActionButton = { WriteTodayFab(onClick = onAdd) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyMessage(
                    text = stringResource(R.string.empty_entries),
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.entry_count_format, entries.size),
                    )
                }
                items(entries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}

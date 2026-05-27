@file:OptIn(ExperimentalMaterial3Api::class)

package com.diarylite.app.presentation.screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diarylite.app.R
import com.diarylite.app.presentation.DiaryViewModel
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: DiaryViewModel,
    onWriteToday: () -> Unit,
    onOpenEntries: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            DiaryTopBar(title = stringResource(R.string.home_title))
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                TodayWritePanel(
                    dateText = fullKoreanDateText(LocalDate.now()),
                    onWriteToday = onWriteToday,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuickActionCard(
                        label = stringResource(R.string.all_entries),
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = onOpenEntries,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        label = stringResource(R.string.calendar),
                        icon = Icons.Default.CalendarMonth,
                        onClick = onOpenCalendar,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        label = stringResource(R.string.search),
                        icon = Icons.Default.Search,
                        onClick = onOpenSearch,
                        modifier = Modifier.weight(1f),
                    )
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
    }
}

@Composable
private fun TodayWritePanel(
    dateText: String,
    onWriteToday: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = dateText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(R.string.home_today_card_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.home_today_card_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                )
            }
            Button(
                onClick = onWriteToday,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.today_diary))
            }
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
            DiaryTopBar(title = stringResource(R.string.all_entries))
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entry))
            }
        },
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.diarylite.app.presentation.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.diarylite.app.R
import com.diarylite.app.domain.model.DiaryEntry
import com.diarylite.app.util.formatInputTime
import com.diarylite.app.util.formatShortDate
import com.diarylite.app.util.toLocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun DiaryTopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
internal fun EntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = LocalDate.ofEpochDay(entry.entryDateEpochDay)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            EntryDateBadge(date = date)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = entry.title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.untitled),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
                EntryMetaRow(entry = entry, includeDate = false)
                Text(
                    text = entry.content.previewText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EntryDateBadge(date: LocalDate) {
    Surface(
        modifier = Modifier.width(58.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.date_month_short_format, date.monthValue),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun EntryMetaRow(
    entry: DiaryEntry,
    includeDate: Boolean = true,
) {
    val context = LocalContext.current
    val date = LocalDate.ofEpochDay(entry.entryDateEpochDay)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (includeDate) {
            MetaPill(
                text = date.formatShortDate(),
                icon = Icons.Default.CalendarMonth,
            )
        }
        entry.entryTimeMinute?.let { minute ->
            MetaPill(
                text = minute.toLocalTime().formatInputTime(),
                icon = Icons.Default.AccessTime,
            )
        }
        entry.moodCode?.let { moodCode ->
            MetaPill(
                text = context.moodLabel(moodCode),
                icon = Icons.Default.Mood,
            )
        }
    }
}

@Composable
private fun MetaPill(
    text: String,
    icon: ImageVector,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun Context.moodLabel(code: String): String = when (code) {
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
internal fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
internal fun EmptyMessage(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.NoteAlt,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun WriteTodayFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
            )
        },
        text = { Text(stringResource(R.string.write_diary_short)) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )
}

@Composable
internal fun BackIconButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

@Composable
internal fun ConfirmDialog(
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
internal fun MonthHeader(
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
            text = koreanMonthText(visibleMonth),
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
internal fun CalendarDayCell(
    date: LocalDate,
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    hasEntry: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val inMonth = YearMonth.from(date) == visibleMonth
    val isSelected = date == selectedDate
    val isToday = date == today
    val fullDateText = fullKoreanDateText(date)
    val dayDescription = if (hasEntry) {
        stringResource(R.string.calendar_day_has_entry_description, fullDateText)
    } else {
        stringResource(R.string.calendar_day_no_entry_description, fullDateText)
    }
    val dayBackground = when {
        isSelected -> MaterialTheme.colorScheme.primary
        hasEntry && inMonth -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val dayContentColor = when {
        !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        hasEntry && inMonth -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val entryDotColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .heightIn(min = 48.dp)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.medium)
            .semantics { contentDescription = dayDescription }
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = dayBackground,
            contentColor = dayContentColor,
            border = if (isToday && !isSelected) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            } else {
                null
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected || isToday || hasEntry) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
        Box(
            modifier = Modifier
                .height(8.dp)
                .padding(top = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (hasEntry && inMonth) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(entryDotColor),
                )
            }
        }
    }
}

internal fun YearMonth.calendarGridDates(): List<LocalDate> {
    val firstDay = atDay(1)
    val sundayStartOffset = firstDay.dayOfWeek.value % 7
    val gridStart = firstDay.minusDays(sundayStartOffset.toLong())
    return List(42) { index -> gridStart.plusDays(index.toLong()) }
}

@Composable
internal fun fullKoreanDateText(date: LocalDate): String {
    val weekdays = stringArrayResource(R.array.calendar_weekdays_full)
    val weekdayIndex = date.dayOfWeek.value % 7
    return stringResource(
        R.string.date_full_format,
        date.year,
        date.monthValue,
        date.dayOfMonth,
        weekdays[weekdayIndex],
    )
}

@Composable
internal fun koreanMonthText(month: YearMonth): String =
    stringResource(R.string.month_year_format, month.year, month.monthValue)

private fun String.previewText(): String {
    val compact = lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .ifBlank { this }
    return if (compact.length <= 90) compact else compact.take(90) + "..."
}

internal fun editorKey(vararg parts: String): String = buildString {
    parts.forEach(::appendEditorPart)
}

private fun StringBuilder.appendEditorPart(value: String) {
    append(value.length)
    append(':')
    append(value)
}

internal fun suggestedExportFilename(): String {
    val timestamp = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.KOREAN),
    )
    return "diary_export_$timestamp.md"
}

internal fun createExportDocumentIntent(filename: String): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "text/markdown"
    putExtra(Intent.EXTRA_TITLE, filename)
    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/markdown", "text/plain"))
}

internal sealed interface EntryLookupState {
    data object Loading : EntryLookupState
    data object Missing : EntryLookupState
    data class Found(val entry: DiaryEntry) : EntryLookupState
}

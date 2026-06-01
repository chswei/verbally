package com.verbally.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.history.HistoryRetentionMode

@Composable
internal fun HistoryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.historyRepository.list()) }
    val historyRetentionMode = container.settingsRepository.load().historyRetentionMode
    val context = LocalContext.current
    val historyClearedMessage = stringResource(R.string.history_cleared)
    val copiedMessage = stringResource(R.string.copied)
    HistoryScreenContent(
        query = query,
        entries = entries,
        historyRetentionMode = historyRetentionMode,
        onQueryChange = {
            query = it
            entries = container.historyRepository.search(it)
        },
        onClearHistory = {
            container.historyRepository.clear()
            entries = container.historyRepository.search(query)
            Toast.makeText(context, historyClearedMessage, Toast.LENGTH_SHORT).show()
        },
        onCopy = { entry ->
            copyText(context, entry.cleanedText)
            Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.historyRepository.delete(entry.id)
            entries = container.historyRepository.search(query)
        },
        modifier = modifier,
    )
}

@Composable
fun HistoryScreenContent(
    query: String,
    entries: List<DictationHistoryEntry>,
    onQueryChange: (String) -> Unit,
    onClearHistory: () -> Unit,
    onCopy: (DictationHistoryEntry) -> Unit,
    onDelete: (DictationHistoryEntry) -> Unit,
    modifier: Modifier = Modifier,
    historyRetentionMode: HistoryRetentionMode = HistoryRetentionMode.LATEST_100,
) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showHistoryMenu by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<DictationHistoryEntry?>(null) }
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.history_clear_confirm_title)) },
            text = { Text(stringResource(R.string.history_clear_confirm_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        onClearHistory()
                    },
                ) {
                    Text(stringResource(R.string.history_clear_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    pendingDeleteEntry?.let { entry ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.history_entry_delete_confirm_title),
            description = stringResource(R.string.history_entry_delete_confirm_description),
            onDismiss = { pendingDeleteEntry = null },
            onConfirm = {
                pendingDeleteEntry = null
                onDelete(entry)
            },
        )
    }
    Column(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HistoryScreenHeader(
            historyRetentionMode = historyRetentionMode,
            showOverflow = entries.isNotEmpty(),
            overflowExpanded = showHistoryMenu,
            onOverflowClick = { showHistoryMenu = true },
            onOverflowDismiss = { showHistoryMenu = false },
            onClearHistoryClick = {
                showHistoryMenu = false
                showClearConfirmation = true
            },
        )
        SearchField(
            value = query,
            onChange = onQueryChange,
            placeholder = stringResource(R.string.history_search_placeholder),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStateBlock(
                            title = if (historyRetentionMode == HistoryRetentionMode.NONE) {
                                stringResource(R.string.history_disabled_title)
                            } else {
                                stringResource(R.string.history_empty_title)
                            },
                            description = if (historyRetentionMode == HistoryRetentionMode.NONE) {
                                stringResource(R.string.history_disabled_description)
                            } else {
                                stringResource(R.string.history_empty_description)
                            },
                        )
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        onCopy = { onCopy(entry) },
                        onDelete = { pendingDeleteEntry = entry },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryScreenHeader(
    historyRetentionMode: HistoryRetentionMode,
    showOverflow: Boolean,
    overflowExpanded: Boolean,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onClearHistoryClick: () -> Unit,
) {
    val historyMenuContentDescription = stringResource(R.string.history_menu_content_description)
    Box(modifier = Modifier.fillMaxWidth()) {
        ScreenHeader(
            title = stringResource(R.string.history_title),
            subtitle = historySubtitle(historyRetentionMode),
        )
        if (showOverflow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-6).dp),
            ) {
                IconButton(
                    onClick = onOverflowClick,
                    modifier = Modifier.semantics {
                        contentDescription = historyMenuContentDescription
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_more_vert_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = onOverflowDismiss,
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.history_clear_menu_item),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = onClearHistoryClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun historySubtitle(historyRetentionMode: HistoryRetentionMode): String =
    when (historyRetentionMode) {
        HistoryRetentionMode.LATEST_100 -> stringResource(R.string.history_subtitle)
        HistoryRetentionMode.AUTO_DELETE_24_HOURS -> stringResource(R.string.history_subtitle_auto_delete_24h)
        HistoryRetentionMode.NONE -> stringResource(R.string.history_subtitle_none)
    }

@Composable
private fun HistoryItem(
    entry: DictationHistoryEntry,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    val deleteContentDescription = stringResource(R.string.delete_item_content_description, entry.cleanedText)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            Text(entry.cleanedText, style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) { Text(stringResource(R.string.copy)) }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics {
                        contentDescription = deleteContentDescription
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
}

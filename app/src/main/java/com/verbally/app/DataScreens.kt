package com.verbally.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.snippets.SnippetEntry

@Composable
internal fun DictionaryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.dictionaryRepository.list()) }
    val context = LocalContext.current
    val savedMessage = stringResource(R.string.dictionary_saved)
    val deletedMessage = stringResource(R.string.dictionary_deleted)
    fun refresh(nextQuery: String = query) {
        entries = container.dictionaryRepository.search(nextQuery)
    }
    DictionaryScreenContent(
        query = query,
        entries = entries,
        onQueryChange = {
            query = it
            refresh(it)
        },
        onSave = { entry ->
            container.dictionaryRepository.save(entry)
            refresh()
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.dictionaryRepository.delete(entry.id)
            refresh()
            Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
        },
        modifier = modifier,
    )
}

@Composable
fun DictionaryScreenContent(
    query: String,
    entries: List<DictionaryEntry>,
    onQueryChange: (String) -> Unit,
    onSave: (DictionaryEntry) -> Unit,
    onDelete: (DictionaryEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val addContentDescription = stringResource(R.string.dictionary_add_content_description)
    var editingEntry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    if (showEditor) {
        DictionaryEntryDialog(
            entry = editingEntry,
            onDismiss = {
                showEditor = false
                editingEntry = null
            },
            onSave = { entry ->
                onSave(entry)
                showEditor = false
                editingEntry = null
            },
        )
    }
    Box(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ScreenHeader(
                title = stringResource(R.string.dictionary_title),
                subtitle = stringResource(R.string.dictionary_subtitle),
            )
            SearchField(
                value = query,
                onChange = onQueryChange,
                placeholder = stringResource(R.string.dictionary_search_placeholder),
                contentDescription = stringResource(R.string.dictionary_search_content_description),
            )
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateBlock(
                        title = if (query.isBlank()) {
                            stringResource(R.string.dictionary_empty_title)
                        } else {
                            stringResource(R.string.dictionary_empty_search_title)
                        },
                        description = if (query.isBlank()) {
                            stringResource(R.string.dictionary_empty_description)
                        } else {
                            stringResource(R.string.dictionary_empty_search_description)
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        DictionaryEntryCard(
                            entry = entry,
                            onEdit = {
                                editingEntry = entry
                                showEditor = true
                            },
                            onDelete = { onDelete(entry) },
                        )
                    }
                }
            }
        }
        AddEntryFloatingActionButton(
            contentDescription = addContentDescription,
            onClick = {
                editingEntry = null
                showEditor = true
            },
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun DictionaryEntryCard(
    entry: DictionaryEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val editContentDescription = stringResource(R.string.edit_item_content_description, entry.term)
    val deleteContentDescription = stringResource(R.string.delete_item_content_description, entry.term)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.term,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            EntryActionRow(
                editContentDescription = editContentDescription,
                deleteContentDescription = deleteContentDescription,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun DictionaryEntryDialog(
    entry: DictionaryEntry?,
    onDismiss: () -> Unit,
    onSave: (DictionaryEntry) -> Unit,
) {
    val termInputContentDescription = stringResource(R.string.dictionary_term_input_content_description)
    val noteInputContentDescription = stringResource(R.string.dictionary_note_input_content_description)
    var term by remember(entry?.id) { mutableStateOf(entry?.term.orEmpty()) }
    var note by remember(entry?.id) { mutableStateOf(entry?.note.orEmpty()) }
    val title = if (entry == null) {
        stringResource(R.string.dictionary_dialog_add_title)
    } else {
        stringResource(R.string.dictionary_dialog_edit_title)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text(stringResource(R.string.dictionary_term_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = termInputContentDescription },
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dictionary_note_label)) },
                    placeholder = { Text(stringResource(R.string.dictionary_note_placeholder)) },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = noteInputContentDescription },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        DictionaryEntry(
                            id = entry?.id ?: System.currentTimeMillis(),
                            term = term,
                            note = note,
                        ),
                    )
                },
                enabled = term.trim().isNotEmpty(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
internal fun SnippetsScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.snippetRepository.list()) }
    val context = LocalContext.current
    val savedMessage = stringResource(R.string.snippets_saved)
    val deletedMessage = stringResource(R.string.snippets_deleted)
    fun refresh(nextQuery: String = query) {
        entries = container.snippetRepository.search(nextQuery)
    }
    SnippetsScreenContent(
        query = query,
        entries = entries,
        onQueryChange = {
            query = it
            refresh(it)
        },
        onSave = { entry ->
            container.snippetRepository.save(entry)
            refresh()
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.snippetRepository.delete(entry.id)
            refresh()
            Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
        },
        modifier = modifier,
    )
}

@Composable
fun SnippetsScreenContent(
    query: String,
    entries: List<SnippetEntry>,
    onQueryChange: (String) -> Unit,
    onSave: (SnippetEntry) -> Unit,
    onDelete: (SnippetEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val addContentDescription = stringResource(R.string.snippets_add_content_description)
    var editingEntry by remember { mutableStateOf<SnippetEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    if (showEditor) {
        SnippetEntryDialog(
            entry = editingEntry,
            onDismiss = {
                showEditor = false
                editingEntry = null
            },
            onSave = { entry ->
                onSave(entry)
                showEditor = false
                editingEntry = null
            },
        )
    }
    Box(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ScreenHeader(
                title = stringResource(R.string.snippets_title),
                subtitle = stringResource(R.string.snippets_subtitle),
            )
            SearchField(
                value = query,
                onChange = onQueryChange,
                placeholder = stringResource(R.string.snippets_search_placeholder),
                contentDescription = stringResource(R.string.snippets_search_content_description),
            )
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateBlock(
                        title = if (query.isBlank()) {
                            stringResource(R.string.snippets_empty_title)
                        } else {
                            stringResource(R.string.snippets_empty_search_title)
                        },
                        description = if (query.isBlank()) {
                            stringResource(R.string.snippets_empty_description)
                        } else {
                            stringResource(R.string.snippets_empty_search_description)
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        SnippetEntryCard(
                            entry = entry,
                            onEdit = {
                                editingEntry = entry
                                showEditor = true
                            },
                            onDelete = { onDelete(entry) },
                        )
                    }
                }
            }
        }
        AddEntryFloatingActionButton(
            contentDescription = addContentDescription,
            onClick = {
                editingEntry = null
                showEditor = true
            },
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun SnippetEntryCard(
    entry: SnippetEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val editContentDescription = stringResource(R.string.edit_item_content_description, entry.trigger)
    val deleteContentDescription = stringResource(R.string.delete_item_content_description, entry.trigger)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.trigger,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = entry.expansion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            EntryActionRow(
                editContentDescription = editContentDescription,
                deleteContentDescription = deleteContentDescription,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun AddEntryFloatingActionButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .padding(bottom = 24.dp)
            .semantics { this.contentDescription = contentDescription },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text("+", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun EntryActionRow(
    editContentDescription: String,
    deleteContentDescription: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onEdit,
            modifier = Modifier.semantics {
                contentDescription = editContentDescription
            },
        ) {
            Text(stringResource(R.string.edit))
        }
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

@Composable
private fun SnippetEntryDialog(
    entry: SnippetEntry?,
    onDismiss: () -> Unit,
    onSave: (SnippetEntry) -> Unit,
) {
    val triggerInputContentDescription = stringResource(R.string.snippets_trigger_input_content_description)
    val expansionInputContentDescription = stringResource(R.string.snippets_expansion_input_content_description)
    var trigger by remember(entry?.id) { mutableStateOf(entry?.trigger.orEmpty()) }
    var expansion by remember(entry?.id) { mutableStateOf(entry?.expansion.orEmpty()) }
    val title = if (entry == null) {
        stringResource(R.string.snippets_dialog_add_title)
    } else {
        stringResource(R.string.snippets_dialog_edit_title)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text(stringResource(R.string.snippets_trigger_label)) },
                    placeholder = { Text(stringResource(R.string.snippets_trigger_placeholder)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = triggerInputContentDescription },
                )
                OutlinedTextField(
                    value = expansion,
                    onValueChange = { expansion = it },
                    label = { Text(stringResource(R.string.snippets_expansion_label)) },
                    placeholder = { Text(stringResource(R.string.snippets_expansion_placeholder)) },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = expansionInputContentDescription },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SnippetEntry(
                            id = entry?.id ?: System.currentTimeMillis(),
                            trigger = trigger,
                            expansion = expansion,
                        ),
                    )
                },
                enabled = trigger.trim().isNotEmpty() && expansion.trim().isNotEmpty(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun EmptyStateBlock(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = description,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun HistoryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.historyRepository.list()) }
    val context = LocalContext.current
    val historyClearedMessage = stringResource(R.string.history_cleared)
    val copiedMessage = stringResource(R.string.copied)
    HistoryScreenContent(
        query = query,
        entries = entries,
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
                            title = stringResource(R.string.history_empty_title),
                            description = stringResource(R.string.history_empty_description),
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
            subtitle = stringResource(R.string.history_subtitle),
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

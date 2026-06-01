package com.verbally.app

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.verbally.app.snippets.SnippetEntry

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
            val validationResult = LocalEntryConflictValidator.validateSnippet(
                candidate = entry,
                snippets = container.snippetRepository.list(),
                dictionaries = container.dictionaryRepository.list(),
            )
            val saveResult = if (validationResult == LocalEntrySaveResult.Saved) {
                container.snippetRepository.save(entry)
            } else {
                validationResult
            }
            if (saveResult == LocalEntrySaveResult.Saved) {
                refresh()
                Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            }
            saveResult
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
    onSave: (SnippetEntry) -> LocalEntrySaveResult,
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
                val result = onSave(entry)
                if (result == LocalEntrySaveResult.Saved) {
                    showEditor = false
                    editingEntry = null
                }
                result
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
private fun SnippetEntryDialog(
    entry: SnippetEntry?,
    onDismiss: () -> Unit,
    onSave: (SnippetEntry) -> LocalEntrySaveResult,
) {
    val triggerInputContentDescription = stringResource(R.string.snippets_trigger_input_content_description)
    val expansionInputContentDescription = stringResource(R.string.snippets_expansion_input_content_description)
    var trigger by remember(entry?.id) { mutableStateOf(entry?.trigger.orEmpty()) }
    var expansion by remember(entry?.id) { mutableStateOf(entry?.expansion.orEmpty()) }
    var validationResult by remember(entry?.id) { mutableStateOf<LocalEntrySaveResult?>(null) }
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
                validationResult
                    ?.validationMessageRes()
                    ?.let { messageRes ->
                        Text(
                            text = stringResource(messageRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = onSave(
                        SnippetEntry(
                            id = entry?.id ?: System.currentTimeMillis(),
                            trigger = trigger,
                            expansion = expansion,
                        ),
                    )
                    validationResult = result.takeIf { it != LocalEntrySaveResult.Saved }
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

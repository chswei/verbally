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
import com.verbally.app.dictionary.DictionaryEntry

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

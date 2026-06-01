package com.verbally.app

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.snippets.SnippetEntry
import java.util.Locale

enum class LocalEntrySaveResult {
    Saved,
    Invalid,
    Duplicate,
    Conflict,
}

object LocalEntryConflictValidator {
    fun validateDictionary(
        candidate: DictionaryEntry,
        dictionaries: List<DictionaryEntry>,
        snippets: List<SnippetEntry>,
    ): LocalEntrySaveResult {
        val key = normalizedLocalEntryKey(candidate.term) ?: return LocalEntrySaveResult.Invalid
        if (dictionaries.any { it.id != candidate.id && normalizedLocalEntryKey(it.term) == key }) {
            return LocalEntrySaveResult.Duplicate
        }
        if (snippets.any { normalizedLocalEntryKey(it.trigger) == key }) {
            return LocalEntrySaveResult.Conflict
        }
        return LocalEntrySaveResult.Saved
    }

    fun validateSnippet(
        candidate: SnippetEntry,
        snippets: List<SnippetEntry>,
        dictionaries: List<DictionaryEntry>,
    ): LocalEntrySaveResult {
        val key = normalizedLocalEntryKey(candidate.trigger) ?: return LocalEntrySaveResult.Invalid
        if (candidate.expansion.trim().isEmpty()) return LocalEntrySaveResult.Invalid
        if (snippets.any { it.id != candidate.id && normalizedLocalEntryKey(it.trigger) == key }) {
            return LocalEntrySaveResult.Duplicate
        }
        if (dictionaries.any { normalizedLocalEntryKey(it.term) == key }) {
            return LocalEntrySaveResult.Conflict
        }
        return LocalEntrySaveResult.Saved
    }
}

fun normalizedLocalEntryKey(value: String): String? =
    value.trim().lowercase(Locale.ROOT).ifBlank { null }

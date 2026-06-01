package com.verbally.app

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.snippets.SnippetEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalEntryConflictValidatorTest {
    @Test
    fun rejectsSnippetTriggerMatchingDictionaryTerm() {
        val result = LocalEntryConflictValidator.validateSnippet(
            candidate = SnippetEntry(trigger = "  openai  ", expansion = "OpenAI, Inc.", id = 2L),
            snippets = emptyList(),
            dictionaries = listOf(DictionaryEntry(term = "OpenAI", note = null, id = 1L)),
        )

        assertEquals(LocalEntrySaveResult.Conflict, result)
    }

    @Test
    fun rejectsDictionaryTermMatchingSnippetTrigger() {
        val result = LocalEntryConflictValidator.validateDictionary(
            candidate = DictionaryEntry(term = " 我的地址 ", note = null, id = 2L),
            dictionaries = emptyList(),
            snippets = listOf(SnippetEntry(trigger = "我的地址", expansion = "台北市", id = 1L)),
        )

        assertEquals(LocalEntrySaveResult.Conflict, result)
    }

    @Test
    fun ignoresCurrentEntryWhenCheckingConflicts() {
        val result = LocalEntryConflictValidator.validateSnippet(
            candidate = SnippetEntry(trigger = " address ", expansion = "new", id = 1L),
            snippets = listOf(SnippetEntry(trigger = "Address", expansion = "old", id = 1L)),
            dictionaries = emptyList(),
        )

        assertEquals(LocalEntrySaveResult.Saved, result)
    }
}

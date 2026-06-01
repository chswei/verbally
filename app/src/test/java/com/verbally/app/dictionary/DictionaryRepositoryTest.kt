package com.verbally.app.dictionary

import com.verbally.app.LocalEntrySaveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryRepositoryTest {
    @Test
    fun savesTrimmedEntriesAndListsNewestFirst() {
        val repository = InMemoryDictionaryRepository(limit = 10)

        repository.save(DictionaryEntry(term = "  OpenAI  ", note = "  O 大寫，AI 大寫  ", id = 1L))
        repository.save(DictionaryEntry(term = "林昌緯", note = "", id = 2L))

        val entries = repository.list()
        assertEquals(listOf("林昌緯", "OpenAI"), entries.map { it.term })
        assertEquals(null, entries.first().note)
        assertEquals("O 大寫，AI 大寫", entries.last().note)
    }

    @Test
    fun updatesExistingEntryByIdWithoutDuplicatingIt() {
        val repository = InMemoryDictionaryRepository(limit = 10)

        repository.save(DictionaryEntry(term = "Open AI", note = null, id = 1L))
        val result = repository.save(DictionaryEntry(term = "OpenAI", note = "不要加空白", id = 1L))

        assertEquals(LocalEntrySaveResult.Saved, result)
        val entries = repository.list()
        assertEquals(1, entries.size)
        assertEquals("OpenAI", entries.single().term)
        assertEquals("不要加空白", entries.single().note)
    }

    @Test
    fun updatesExistingEntryWhenOnlyCaseOrWhitespaceChanges() {
        val repository = InMemoryDictionaryRepository(limit = 10)

        repository.save(DictionaryEntry(term = "OpenAI", note = "old", id = 1L))
        val result = repository.save(DictionaryEntry(term = "  openai  ", note = "new", id = 1L))

        assertEquals(LocalEntrySaveResult.Saved, result)
        assertEquals(listOf("openai"), repository.list().map { it.term })
        assertEquals("new", repository.list().single().note)
    }

    @Test
    fun rejectsRenamingEntryToExistingNormalizedTerm() {
        val repository = InMemoryDictionaryRepository(limit = 10)
        repository.save(DictionaryEntry(term = "Gemini", note = null, id = 1L))
        repository.save(DictionaryEntry(term = "OpenAI", note = null, id = 2L))

        val result = repository.save(DictionaryEntry(term = "  gemini  ", note = "duplicate", id = 2L))

        assertEquals(LocalEntrySaveResult.Duplicate, result)
        assertEquals(listOf("OpenAI", "Gemini"), repository.list().map { it.term })
    }

    @Test
    fun loadedDuplicateDictionaryDataIsDeduplicated() {
        val repository = InMemoryDictionaryRepository(
            limit = 10,
            initialEntries = listOf(
                DictionaryEntry(term = "OpenAI", note = "old", id = 1L),
                DictionaryEntry(term = " openai ", note = "new", id = 2L),
            ),
        )

        val entries = repository.list()

        assertEquals(1, entries.size)
        assertEquals(2L, entries.single().id)
        assertEquals("openai", entries.single().term)
        assertEquals("new", entries.single().note)
    }

    @Test
    fun searchesTermAndNoteIgnoringCase() {
        val repository = InMemoryDictionaryRepository(limit = 10)
        repository.save(DictionaryEntry(term = "Gemini", note = "Google cleanup provider", id = 1L))
        repository.save(DictionaryEntry(term = "林昌緯", note = "使用者姓名", id = 2L))

        assertEquals(listOf("Gemini"), repository.search("provider").map { it.term })
        assertEquals(listOf("林昌緯"), repository.search("林").map { it.term })
        assertEquals(emptyList<DictionaryEntry>(), repository.search("不存在"))
    }

    @Test
    fun blankSearchReturnsAllEntriesNewestFirst() {
        val repository = InMemoryDictionaryRepository(limit = 10)
        repository.save(DictionaryEntry(term = "Gemini", note = null, id = 1L))
        repository.save(DictionaryEntry(term = "OpenAI", note = null, id = 2L))

        assertEquals(listOf("OpenAI", "Gemini"), repository.search("   ").map { it.term })
    }

    @Test
    fun deletesEntryById() {
        val repository = InMemoryDictionaryRepository(limit = 10)
        repository.save(DictionaryEntry(term = "Gemini", note = null, id = 1L))
        repository.save(DictionaryEntry(term = "OpenAI", note = null, id = 2L))

        repository.delete(1L)

        assertEquals(listOf("OpenAI"), repository.list().map { it.term })
    }

    @Test
    fun ignoresBlankTermsAndKeepsOnlyLatestEntriesWithinLimit() {
        val repository = InMemoryDictionaryRepository(limit = 2)

        repository.save(DictionaryEntry(term = "   ", note = "空白不該保存", id = 1L))
        repository.save(DictionaryEntry(term = "A", note = null, id = 2L))
        repository.save(DictionaryEntry(term = "B", note = null, id = 3L))
        repository.save(DictionaryEntry(term = "C", note = null, id = 4L))

        assertEquals(listOf("C", "B"), repository.list().map { it.term })
    }

    @Test
    fun blankTermSaveReturnsInvalid() {
        val repository = InMemoryDictionaryRepository(limit = 10)

        val result = repository.save(DictionaryEntry(term = "   ", note = "空白不該保存", id = 1L))

        assertEquals(LocalEntrySaveResult.Invalid, result)
        assertTrue(repository.list().isEmpty())
    }
}

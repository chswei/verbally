package com.verbally.app.snippets

import com.verbally.app.LocalEntrySaveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnippetRepositoryTest {
    @Test
    fun saveNormalizesEntriesAndIgnoresBlankRequiredFields() {
        val repository = InMemorySnippetRepository()

        repository.save(SnippetEntry(trigger = "  我的地址  ", expansion = "  台北市信義區一號  ", id = 1L))
        repository.save(SnippetEntry(trigger = "   ", expansion = "empty trigger", id = 2L))
        repository.save(SnippetEntry(trigger = "empty expansion", expansion = "   ", id = 3L))

        assertEquals(
            listOf(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 1L)),
            repository.list(),
        )
    }

    @Test
    fun saveReplacesExistingTriggerCaseInsensitively() {
        val repository = InMemorySnippetRepository()

        repository.save(SnippetEntry(trigger = "My Address", expansion = "old", id = 1L))
        val result = repository.save(SnippetEntry(trigger = "my address", expansion = "new", id = 1L))

        assertEquals(LocalEntrySaveResult.Saved, result)
        assertEquals(
            listOf(SnippetEntry(trigger = "my address", expansion = "new", id = 1L)),
            repository.list(),
        )
    }

    @Test
    fun rejectsRenamingSnippetToExistingNormalizedTrigger() {
        val repository = InMemorySnippetRepository()
        repository.save(SnippetEntry(trigger = "address", expansion = "one", id = 1L))
        repository.save(SnippetEntry(trigger = "report", expansion = "two", id = 2L))

        val result = repository.save(SnippetEntry(trigger = " ADDRESS ", expansion = "duplicate", id = 2L))

        assertEquals(LocalEntrySaveResult.Duplicate, result)
        assertEquals(listOf("report", "address"), repository.list().map { it.trigger })
    }

    @Test
    fun loadedDuplicateSnippetDataIsDeduplicated() {
        val repository = InMemorySnippetRepository(
            initialEntries = listOf(
                SnippetEntry(trigger = "address", expansion = "old", id = 1L),
                SnippetEntry(trigger = " ADDRESS ", expansion = "new", id = 2L),
            ),
        )

        val entries = repository.list()

        assertEquals(1, entries.size)
        assertEquals(2L, entries.single().id)
        assertEquals("ADDRESS", entries.single().trigger)
        assertEquals("new", entries.single().expansion)
    }

    @Test
    fun searchMatchesTriggerOrExpansion() {
        val repository = InMemorySnippetRepository()
        repository.save(SnippetEntry(trigger = "放射科報告模板", expansion = "Findings:\nImpression:", id = 1L))
        repository.save(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 2L))

        assertEquals(listOf("我的地址"), repository.search("信義").map { it.trigger })
        assertEquals(listOf("放射科報告模板"), repository.search("報告").map { it.trigger })
        assertTrue(repository.search("不存在").isEmpty())
    }

    @Test
    fun blankSearchReturnsAllEntriesNewestFirst() {
        val repository = InMemorySnippetRepository()
        repository.save(SnippetEntry(trigger = "放射科報告模板", expansion = "Findings:\nImpression:", id = 1L))
        repository.save(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 2L))

        assertEquals(listOf("我的地址", "放射科報告模板"), repository.search("   ").map { it.trigger })
    }

    @Test
    fun deleteRemovesEntryById() {
        val repository = InMemorySnippetRepository()
        repository.save(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 1L))

        repository.delete(1L)

        assertTrue(repository.list().isEmpty())
    }

    @Test
    fun saveKeepsNewestEntriesWithinLimit() {
        val repository = InMemorySnippetRepository(limit = 2)

        repository.save(SnippetEntry(trigger = "one", expansion = "1", id = 1L))
        repository.save(SnippetEntry(trigger = "two", expansion = "2", id = 2L))
        repository.save(SnippetEntry(trigger = "three", expansion = "3", id = 3L))

        assertEquals(listOf("three", "two"), repository.list().map { it.trigger })
    }
}

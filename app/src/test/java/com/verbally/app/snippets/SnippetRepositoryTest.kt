package com.verbally.app.snippets

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
        repository.save(SnippetEntry(trigger = "my address", expansion = "new", id = 2L))

        assertEquals(
            listOf(SnippetEntry(trigger = "my address", expansion = "new", id = 2L)),
            repository.list(),
        )
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

package com.verbally.app.history

import org.junit.Assert.assertEquals
import org.junit.Test

class DictationHistoryRepositoryTest {
    @Test
    fun keepsOnlyLatestOneHundredEntries() {
        val repository = InMemoryDictationHistoryRepository(limit = 100)

        repeat(101) { index ->
            repository.save(
                DictationHistoryEntry(
                    rawTranscript = "raw-$index",
                    cleanedText = "clean-$index",
                    createdAtMillis = index.toLong(),
                    provider = "openai",
                    transcriptionModel = "gpt-4o-transcribe",
                    cleanupModel = "gpt-5.4-mini",
                    appLabel = "Test",
                ),
            )
        }

        val entries = repository.list()
        assertEquals(100, entries.size)
        assertEquals("raw-100", entries.first().rawTranscript)
        assertEquals("raw-1", entries.last().rawTranscript)
    }

    @Test
    fun searchesRawAndCleanedText() {
        val repository = InMemoryDictationHistoryRepository(limit = 100)
        repository.save(
            DictationHistoryEntry(
                rawTranscript = "今天要寄 email",
                cleanedText = "今天要寄 email。",
                createdAtMillis = 1L,
                provider = "openai",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupModel = "gemini-3.5-flash",
                appLabel = null,
            ),
        )

        assertEquals(1, repository.search("email").size)
        assertEquals(0, repository.search("不存在").size)
    }
}

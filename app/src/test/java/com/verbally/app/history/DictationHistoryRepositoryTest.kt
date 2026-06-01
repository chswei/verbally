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
                    transcriptionProvider = "openai",
                    transcriptionModel = "gpt-4o-transcribe",
                    cleanupProvider = "openai",
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
    fun autoDeleteRetentionRemovesEntriesOlderThanTwentyFourHours() {
        var now = 48L * 60L * 60L * 1_000L
        val repository = InMemoryDictationHistoryRepository(
            limit = 100,
            retentionModeProvider = { HistoryRetentionMode.AUTO_DELETE_24_HOURS },
            currentTimeMillis = { now },
        )

        repository.save(entry("old", createdAtMillis = now - 25L * 60L * 60L * 1_000L))
        repository.save(entry("fresh", createdAtMillis = now - 23L * 60L * 60L * 1_000L))

        assertEquals(listOf("fresh"), repository.list().map { it.rawTranscript })

        now += 2L * 60L * 60L * 1_000L

        assertEquals(emptyList<DictationHistoryEntry>(), repository.list())
    }

    @Test
    fun noHistoryRetentionClearsAndSkipsFutureSaves() {
        var mode = HistoryRetentionMode.LATEST_100
        val repository = InMemoryDictationHistoryRepository(
            limit = 100,
            retentionModeProvider = { mode },
        )
        repository.save(entry("saved", createdAtMillis = 1L))

        mode = HistoryRetentionMode.NONE
        repository.clear()
        repository.save(entry("ignored", createdAtMillis = 2L))

        assertEquals(emptyList<DictationHistoryEntry>(), repository.list())
    }

    @Test
    fun latestRetentionStillKeepsOnlyLatestEntries() {
        val repository = InMemoryDictationHistoryRepository(
            limit = 2,
            retentionModeProvider = { HistoryRetentionMode.LATEST_100 },
        )

        repository.save(entry("one", createdAtMillis = 1L))
        repository.save(entry("two", createdAtMillis = 2L))
        repository.save(entry("three", createdAtMillis = 3L))

        assertEquals(listOf("three", "two"), repository.list().map { it.rawTranscript })
    }

    @Test
    fun searchesRawAndCleanedText() {
        val repository = InMemoryDictationHistoryRepository(limit = 100)
        repository.save(
            DictationHistoryEntry(
                rawTranscript = "今天要寄 email",
                cleanedText = "今天要寄 email。",
                createdAtMillis = 1L,
                transcriptionProvider = "openai",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "gemini",
                cleanupModel = "gemini-3.5-flash",
                appLabel = null,
            ),
        )

        assertEquals(1, repository.search("email").size)
        assertEquals(0, repository.search("不存在").size)
    }

    @Test
    fun blankSearchReturnsAllEntriesNewestFirst() {
        val repository = InMemoryDictationHistoryRepository(limit = 100)
        repository.save(
            DictationHistoryEntry(
                rawTranscript = "first raw",
                cleanedText = "first clean",
                createdAtMillis = 1L,
                transcriptionProvider = "openai",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "openai",
                cleanupModel = "gpt-5.4-mini",
                appLabel = null,
            ),
        )
        repository.save(
            DictationHistoryEntry(
                rawTranscript = "second raw",
                cleanedText = "second clean",
                createdAtMillis = 2L,
                transcriptionProvider = "openai",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "openai",
                cleanupModel = "gpt-5.4-mini",
                appLabel = null,
            ),
        )

        assertEquals(listOf("second raw", "first raw"), repository.search("   ").map { it.rawTranscript })
    }

    private fun entry(raw: String, createdAtMillis: Long): DictationHistoryEntry =
        DictationHistoryEntry(
            rawTranscript = raw,
            cleanedText = "$raw clean",
            createdAtMillis = createdAtMillis,
            transcriptionProvider = "openai",
            transcriptionModel = "gpt-4o-transcribe",
            cleanupProvider = "openai",
            cleanupModel = "gpt-5.4-mini",
            appLabel = null,
        )
}

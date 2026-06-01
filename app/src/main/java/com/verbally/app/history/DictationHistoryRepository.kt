package com.verbally.app.history

data class DictationHistoryEntry(
    val rawTranscript: String,
    val cleanedText: String,
    val createdAtMillis: Long,
    val transcriptionProvider: String,
    val transcriptionModel: String,
    val cleanupProvider: String,
    val cleanupModel: String,
    val appLabel: String?,
    val id: Long = createdAtMillis,
)

enum class HistoryRetentionMode {
    LATEST_100,
    AUTO_DELETE_24_HOURS,
    NONE,
}

interface DictationHistoryRepository {
    fun save(entry: DictationHistoryEntry)
    fun list(): List<DictationHistoryEntry>
    fun search(query: String): List<DictationHistoryEntry>
    fun delete(id: Long)
    fun clear()
}

class InMemoryDictationHistoryRepository(
    private val limit: Int = 100,
    private val retentionModeProvider: () -> HistoryRetentionMode = { HistoryRetentionMode.LATEST_100 },
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
) : DictationHistoryRepository {
    private val entries = mutableListOf<DictationHistoryEntry>()

    override fun save(entry: DictationHistoryEntry) {
        if (retentionModeProvider() == HistoryRetentionMode.NONE) {
            entries.clear()
            return
        }
        entries.add(0, entry)
        enforceRetention()
    }

    override fun list(): List<DictationHistoryEntry> {
        enforceRetention()
        return entries.toList()
    }

    override fun search(query: String): List<DictationHistoryEntry> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return list()
        return list().filter {
            it.rawTranscript.contains(normalized, ignoreCase = true) ||
                it.cleanedText.contains(normalized, ignoreCase = true)
        }
    }

    override fun delete(id: Long) {
        entries.removeAll { it.id == id }
    }

    override fun clear() {
        entries.clear()
    }

    private fun enforceRetention() {
        if (retentionModeProvider() == HistoryRetentionMode.NONE) {
            entries.clear()
            return
        }
        if (retentionModeProvider() == HistoryRetentionMode.AUTO_DELETE_24_HOURS) {
            val cutoff = currentTimeMillis() - ONE_DAY_MILLIS
            entries.removeAll { it.createdAtMillis < cutoff }
        }
        while (entries.size > limit) {
            entries.removeAt(entries.lastIndex)
        }
    }

    private companion object {
        const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1_000L
    }
}

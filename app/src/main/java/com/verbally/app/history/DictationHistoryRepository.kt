package com.verbally.app.history

data class DictationHistoryEntry(
    val rawTranscript: String,
    val cleanedText: String,
    val createdAtMillis: Long,
    val provider: String,
    val transcriptionModel: String,
    val cleanupModel: String,
    val appLabel: String?,
    val id: Long = createdAtMillis,
)

interface DictationHistoryRepository {
    fun save(entry: DictationHistoryEntry)
    fun list(): List<DictationHistoryEntry>
    fun search(query: String): List<DictationHistoryEntry>
    fun delete(id: Long)
    fun clear()
}

class InMemoryDictationHistoryRepository(
    private val limit: Int = 100,
) : DictationHistoryRepository {
    private val entries = mutableListOf<DictationHistoryEntry>()

    override fun save(entry: DictationHistoryEntry) {
        entries.add(0, entry)
        while (entries.size > limit) {
            entries.removeAt(entries.lastIndex)
        }
    }

    override fun list(): List<DictationHistoryEntry> = entries.toList()

    override fun search(query: String): List<DictationHistoryEntry> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return list()
        return entries.filter {
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
}

package com.verbally.app.dictionary

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.verbally.app.LocalEntrySaveResult
import com.verbally.app.normalizedLocalEntryKey
import org.json.JSONArray
import org.json.JSONObject

data class DictionaryEntry(
    val term: String,
    val note: String?,
    val id: Long = System.currentTimeMillis(),
)

interface DictionaryRepository {
    fun save(entry: DictionaryEntry): LocalEntrySaveResult
    fun list(): List<DictionaryEntry>
    fun search(query: String): List<DictionaryEntry>
    fun delete(id: Long)
}

class InMemoryDictionaryRepository(
    private val limit: Int = DEFAULT_LIMIT,
    initialEntries: List<DictionaryEntry> = emptyList(),
) : DictionaryRepository {
    private val entries = dedupeEntries(initialEntries).toMutableList()

    override fun save(entry: DictionaryEntry): LocalEntrySaveResult {
        val normalized = entry.normalized() ?: return LocalEntrySaveResult.Invalid
        val normalizedKey = normalizedLocalEntryKey(normalized.term) ?: return LocalEntrySaveResult.Invalid
        if (entries.any { it.id != normalized.id && normalizedLocalEntryKey(it.term) == normalizedKey }) {
            return LocalEntrySaveResult.Duplicate
        }
        entries.removeAll { it.id == normalized.id }
        entries.add(0, normalized)
        trimToLimit()
        return LocalEntrySaveResult.Saved
    }

    override fun list(): List<DictionaryEntry> = entries.toList()

    override fun search(query: String): List<DictionaryEntry> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return list()
        return entries.filter { entry ->
            entry.term.contains(normalized, ignoreCase = true) ||
                entry.note.orEmpty().contains(normalized, ignoreCase = true)
        }
    }

    override fun delete(id: Long) {
        entries.removeAll { it.id == id }
    }

    private fun trimToLimit() {
        while (entries.size > limit) {
            entries.removeAt(entries.lastIndex)
        }
    }
}

class SharedPreferencesDictionaryRepository(
    context: Context,
    private val limit: Int = DEFAULT_LIMIT,
) : DictionaryRepository {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("verbally_dictionary", Context.MODE_PRIVATE)

    override fun save(entry: DictionaryEntry): LocalEntrySaveResult {
        val normalized = entry.normalized() ?: return LocalEntrySaveResult.Invalid
        val normalizedKey = normalizedLocalEntryKey(normalized.term) ?: return LocalEntrySaveResult.Invalid
        val current = list()
        if (current.any { it.id != normalized.id && normalizedLocalEntryKey(it.term) == normalizedKey }) {
            return LocalEntrySaveResult.Duplicate
        }
        val updated = listOf(normalized) + current.filterNot { it.id == normalized.id }
        persist(updated.take(limit))
        return LocalEntrySaveResult.Saved
    }

    override fun list(): List<DictionaryEntry> {
        val raw = prefs.getString(KEY_ENTRIES, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val entry = DictionaryEntry(
                        id = item.getLong("id"),
                        term = item.getString("term"),
                        note = item.optString("note").ifBlank { null },
                    ).normalized()
                    if (entry != null) add(entry)
                }
            }
        }.getOrDefault(emptyList())
            .let(::dedupeEntries)
            .take(limit)
    }

    override fun search(query: String): List<DictionaryEntry> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return list()
        return list().filter { entry ->
            entry.term.contains(normalized, ignoreCase = true) ||
                entry.note.orEmpty().contains(normalized, ignoreCase = true)
        }
    }

    override fun delete(id: Long) {
        persist(list().filterNot { it.id == id })
    }

    private fun persist(entries: List<DictionaryEntry>) {
        val array = JSONArray()
        entries.take(limit).forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("term", entry.term)
                    .put("note", entry.note.orEmpty()),
            )
        }
        prefs.edit {
            putString(KEY_ENTRIES, array.toString())
        }
    }

    private companion object {
        const val KEY_ENTRIES = "entries"
    }
}

private fun DictionaryEntry.normalized(): DictionaryEntry? {
    val normalizedTerm = term.trim()
    if (normalizedTerm.isEmpty()) return null
    return copy(
        term = normalizedTerm,
        note = note?.trim()?.ifBlank { null },
    )
}

private fun dedupeEntries(entries: List<DictionaryEntry>): List<DictionaryEntry> =
    entries
        .mapNotNull { it.normalized() }
        .sortedByDescending { it.id }
        .distinctBy { normalizedLocalEntryKey(it.term) }

private const val DEFAULT_LIMIT = 200

package com.verbally.app.snippets

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SnippetEntry(
    val trigger: String,
    val expansion: String,
    val id: Long = System.currentTimeMillis(),
)

interface SnippetRepository {
    fun save(entry: SnippetEntry)
    fun list(): List<SnippetEntry>
    fun search(query: String): List<SnippetEntry>
    fun delete(id: Long)
}

class InMemorySnippetRepository(
    private val limit: Int = DEFAULT_LIMIT,
) : SnippetRepository {
    private val entries = mutableListOf<SnippetEntry>()

    override fun save(entry: SnippetEntry) {
        val normalized = entry.normalized() ?: return
        entries.removeAll { it.id == normalized.id || it.trigger.equals(normalized.trigger, ignoreCase = true) }
        entries.add(0, normalized)
        trimToLimit()
    }

    override fun list(): List<SnippetEntry> = entries.toList()

    override fun search(query: String): List<SnippetEntry> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return list()
        return entries.filter { entry ->
            entry.trigger.contains(normalized, ignoreCase = true) ||
                entry.expansion.contains(normalized, ignoreCase = true)
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

class SharedPreferencesSnippetRepository(
    context: Context,
    private val limit: Int = DEFAULT_LIMIT,
) : SnippetRepository {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("verbally_snippets", Context.MODE_PRIVATE)

    override fun save(entry: SnippetEntry) {
        val normalized = entry.normalized() ?: return
        val updated = listOf(normalized) + list().filterNot {
            it.id == normalized.id || it.trigger.equals(normalized.trigger, ignoreCase = true)
        }
        persist(updated.take(limit))
    }

    override fun list(): List<SnippetEntry> {
        val raw = prefs.getString(KEY_ENTRIES, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val entry = SnippetEntry(
                        id = item.getLong("id"),
                        trigger = item.getString("trigger"),
                        expansion = item.getString("expansion"),
                    ).normalized()
                    if (entry != null) add(entry)
                }
            }
        }.getOrDefault(emptyList()).take(limit)
    }

    override fun search(query: String): List<SnippetEntry> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return list()
        return list().filter { entry ->
            entry.trigger.contains(normalized, ignoreCase = true) ||
                entry.expansion.contains(normalized, ignoreCase = true)
        }
    }

    override fun delete(id: Long) {
        persist(list().filterNot { it.id == id })
    }

    private fun persist(entries: List<SnippetEntry>) {
        val array = JSONArray()
        entries.take(limit).forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("trigger", entry.trigger)
                    .put("expansion", entry.expansion),
            )
        }
        prefs.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    private companion object {
        const val KEY_ENTRIES = "entries"
    }
}

private fun SnippetEntry.normalized(): SnippetEntry? {
    val normalizedTrigger = trigger.trim()
    val normalizedExpansion = expansion.trim()
    if (normalizedTrigger.isEmpty() || normalizedExpansion.isEmpty()) return null
    return copy(
        trigger = normalizedTrigger,
        expansion = normalizedExpansion,
    )
}

private const val DEFAULT_LIMIT = 200

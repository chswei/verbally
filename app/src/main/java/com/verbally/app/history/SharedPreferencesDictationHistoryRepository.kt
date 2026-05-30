package com.verbally.app.history

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesDictationHistoryRepository(
    context: Context,
    private val limit: Int = 100,
) : DictationHistoryRepository {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("verbally_history", Context.MODE_PRIVATE)

    override fun save(entry: DictationHistoryEntry) {
        val updated = listOf(entry) + list().filterNot { it.id == entry.id }
        persist(updated.take(limit))
    }

    override fun list(): List<DictationHistoryEntry> {
        val raw = prefs.getString(KEY_ENTRIES, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val legacyProvider = item.optString("provider").ifBlank { "openai" }
                    add(
                        DictationHistoryEntry(
                            id = item.getLong("id"),
                            rawTranscript = item.getString("rawTranscript"),
                            cleanedText = item.getString("cleanedText"),
                            createdAtMillis = item.getLong("createdAtMillis"),
                            transcriptionProvider = item.optString("transcriptionProvider").ifBlank { "openai" },
                            transcriptionModel = item.getString("transcriptionModel"),
                            cleanupProvider = item.optString("cleanupProvider").ifBlank { legacyProvider },
                            cleanupModel = item.getString("cleanupModel"),
                            appLabel = item.optString("appLabel").ifBlank { null },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
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
        persist(list().filterNot { it.id == id })
    }

    override fun clear() {
        persist(emptyList())
    }

    private fun persist(entries: List<DictationHistoryEntry>) {
        val array = JSONArray()
        entries.take(limit).forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("rawTranscript", entry.rawTranscript)
                    .put("cleanedText", entry.cleanedText)
                    .put("createdAtMillis", entry.createdAtMillis)
                    .put("provider", entry.cleanupProvider)
                    .put("transcriptionProvider", entry.transcriptionProvider)
                    .put("transcriptionModel", entry.transcriptionModel)
                    .put("cleanupProvider", entry.cleanupProvider)
                    .put("cleanupModel", entry.cleanupModel)
                    .put("appLabel", entry.appLabel.orEmpty()),
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

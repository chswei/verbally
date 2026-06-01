package com.verbally.app.history

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesDictationHistoryRepository(
    context: Context,
    private val limit: Int = 100,
    private val retentionModeProvider: () -> HistoryRetentionMode = { HistoryRetentionMode.LATEST_100 },
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
) : DictationHistoryRepository {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("verbally_history", Context.MODE_PRIVATE)

    override fun save(entry: DictationHistoryEntry) {
        if (retentionModeProvider() == HistoryRetentionMode.NONE) {
            clear()
            return
        }
        val updated = listOf(entry) + list().filterNot { it.id == entry.id }
        persist(retained(updated))
    }

    override fun list(): List<DictationHistoryEntry> {
        val persistedEntries = readEntries()
        val entries = retained(persistedEntries)
        if (entries != persistedEntries) {
            persist(entries)
        }
        return entries
    }

    private fun readEntries(): List<DictationHistoryEntry> {
        val raw = prefs.getString(KEY_ENTRIES, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        DictationHistoryEntry(
                            id = item.getLong("id"),
                            rawTranscript = item.getString("rawTranscript"),
                            cleanedText = item.getString("cleanedText"),
                            createdAtMillis = item.getLong("createdAtMillis"),
                            transcriptionProvider = item.getString("transcriptionProvider"),
                            transcriptionModel = item.getString("transcriptionModel"),
                            cleanupProvider = item.getString("cleanupProvider"),
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
        retained(entries).forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("rawTranscript", entry.rawTranscript)
                    .put("cleanedText", entry.cleanedText)
                    .put("createdAtMillis", entry.createdAtMillis)
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

    private fun retained(entries: List<DictationHistoryEntry>): List<DictationHistoryEntry> {
        if (retentionModeProvider() == HistoryRetentionMode.NONE) return emptyList()
        val ageFiltered = if (retentionModeProvider() == HistoryRetentionMode.AUTO_DELETE_24_HOURS) {
            val cutoff = currentTimeMillis() - ONE_DAY_MILLIS
            entries.filter { it.createdAtMillis >= cutoff }
        } else {
            entries
        }
        return ageFiltered.take(limit)
    }

    private companion object {
        const val KEY_ENTRIES = "entries"
        const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1_000L
    }
}

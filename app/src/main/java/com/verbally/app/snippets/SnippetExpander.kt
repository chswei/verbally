package com.verbally.app.snippets

object SnippetExpander {
    fun expand(text: String, snippets: List<SnippetEntry>): String {
        val usableSnippets = snippets
            .mapNotNull { entry ->
                val trigger = entry.trigger.trim()
                val expansion = entry.expansion.trim()
                if (trigger.isEmpty() || expansion.isEmpty()) null else entry.copy(trigger = trigger, expansion = expansion)
            }
            .sortedByDescending { it.trigger.length }
        if (text.isBlank() || usableSnippets.isEmpty()) return text

        val wholeTrigger = text.trim().trimEnd(*TRAILING_PUNCTUATION)
        usableSnippets.firstOrNull { it.trigger.equals(wholeTrigger, ignoreCase = true) }?.let {
            return it.expansion
        }

        val pattern = usableSnippets.joinToString("|") { Regex.escape(it.trigger) }
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        return regex.replace(text) { match ->
            usableSnippets.first { it.trigger.equals(match.value, ignoreCase = true) }.expansion
        }
    }

    private val TRAILING_PUNCTUATION = charArrayOf('。', '.', '！', '!', '？', '?')
}

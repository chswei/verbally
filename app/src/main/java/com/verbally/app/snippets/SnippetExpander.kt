package com.verbally.app.snippets

object SnippetExpander {
    fun expand(text: String, snippets: List<SnippetEntry>): String {
        val usableSnippets = snippets
            .mapNotNull { entry ->
                val trigger = entry.trigger.trim()
                if (trigger.isEmpty() || entry.expansion.trim().isEmpty()) {
                    null
                } else {
                    entry.copy(trigger = trigger)
                }
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
            if (isInsideLatinToken(text, match.range)) {
                match.value
            } else {
                usableSnippets.first { it.trigger.equals(match.value, ignoreCase = true) }.expansion
            }
        }
    }

    private fun isInsideLatinToken(text: String, range: IntRange): Boolean {
        val previous = range.first - 1
        val next = range.last + 1
        return (previous >= 0 && text[previous].isLatinTokenChar()) ||
            (next < text.length && text[next].isLatinTokenChar())
    }

    private fun Char.isLatinTokenChar(): Boolean =
        this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'

    private val TRAILING_PUNCTUATION = charArrayOf('。', '.', '！', '!', '？', '?')
}

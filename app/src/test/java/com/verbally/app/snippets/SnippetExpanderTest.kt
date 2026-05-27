package com.verbally.app.snippets

import org.junit.Assert.assertEquals
import org.junit.Test

class SnippetExpanderTest {
    @Test
    fun expandsTriggerInsideLongerText() {
        val snippets = listOf(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 1L))

        val expanded = SnippetExpander.expand("請寄到我的地址。", snippets)

        assertEquals("請寄到台北市信義區一號。", expanded)
    }

    @Test
    fun expandsWholeTriggerWithTrailingPunctuationToExactExpansion() {
        val snippets = listOf(SnippetEntry(trigger = "放射科報告模板", expansion = "Findings:\n\nImpression:", id = 1L))

        val expanded = SnippetExpander.expand(" 放射科報告模板。 ", snippets)

        assertEquals("Findings:\n\nImpression:", expanded)
    }

    @Test
    fun prefersLongerTriggersWhenTriggersOverlap() {
        val snippets = listOf(
            SnippetEntry(trigger = "我的地址", expansion = "住家地址", id = 1L),
            SnippetEntry(trigger = "我的公司地址", expansion = "公司地址", id = 2L),
        )

        val expanded = SnippetExpander.expand("請寄到我的公司地址。", snippets)

        assertEquals("請寄到公司地址。", expanded)
    }

    @Test
    fun leavesTextUnchangedWhenNoTriggerAppears() {
        val snippets = listOf(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 1L))

        val expanded = SnippetExpander.expand("請問明天地址在哪裡？", snippets)

        assertEquals("請問明天地址在哪裡？", expanded)
    }
}

package com.verbally.app.style

import org.junit.Assert.assertEquals
import org.junit.Test

class AppStyleProfileRepositoryTest {
    @Test
    fun defaultsUseCasualForChatAndFormalForWorkAndOther() {
        val repository = InMemoryAppStyleProfileRepository()

        assertEquals(OutputStyle.CASUAL, repository.styleFor(AppCategory.CHAT))
        assertEquals(OutputStyle.FORMAL, repository.styleFor(AppCategory.WORK))
        assertEquals(OutputStyle.FORMAL, repository.styleFor(AppCategory.OTHER))
    }

    @Test
    fun savesCategoryStyleUpdates() {
        val repository = InMemoryAppStyleProfileRepository()

        repository.save(AppStyleProfile(category = AppCategory.OTHER, style = OutputStyle.CASUAL))

        assertEquals(OutputStyle.CASUAL, repository.styleFor(AppCategory.OTHER))
        assertEquals(
            listOf(AppCategory.CHAT, AppCategory.WORK, AppCategory.OTHER),
            repository.list().map { it.category },
        )
    }

    @Test
    fun classifiesKnownAppsAndFallsBackToOther() {
        assertEquals(AppCategory.CHAT, AppCategoryClassifier.classify("jp.naver.line.android"))
        assertEquals(AppCategory.CHAT, AppCategoryClassifier.classify("com.whatsapp"))
        assertEquals(AppCategory.WORK, AppCategoryClassifier.classify("com.google.android.gm"))
        assertEquals(AppCategory.WORK, AppCategoryClassifier.classify("com.microsoft.teams"))
        assertEquals(AppCategory.OTHER, AppCategoryClassifier.classify("com.example.unclassified"))
        assertEquals(AppCategory.OTHER, AppCategoryClassifier.classify(null))
    }
}

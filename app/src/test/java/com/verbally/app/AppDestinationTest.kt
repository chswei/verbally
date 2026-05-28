package com.verbally.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun bottomNavigationKeepsHistoryRightmost() {
        assertEquals(
            listOf("首頁", "字典", "片段", "語氣", "歷史"),
            AppDestination.entries.map { it.label },
        )
    }
}

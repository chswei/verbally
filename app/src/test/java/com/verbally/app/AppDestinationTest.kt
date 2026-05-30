package com.verbally.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun bottomNavigationKeepsHistoryRightmost() {
        assertEquals(
            listOf(
                R.string.nav_home,
                R.string.nav_dictionary,
                R.string.nav_snippets,
                R.string.nav_style,
                R.string.nav_history,
            ),
            AppDestination.entries.map { it.labelRes },
        )
    }
}

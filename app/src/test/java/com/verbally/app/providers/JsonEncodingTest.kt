package com.verbally.app.providers

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonEncodingTest {
    @Test
    fun jsonStringEscapesAllControlCharacters() {
        val value = "quote \" slash \\ newline\nbackspace\b formfeed\u000C"

        assertEquals(
            """"quote \" slash \\ newline\nbackspace\b formfeed\f"""",
            jsonString(value),
        )
    }
}

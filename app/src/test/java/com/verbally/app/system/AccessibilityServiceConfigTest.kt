package com.verbally.app.system

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityServiceConfigTest {
    @Test
    fun serviceIsNotDeclaredAsAnAccessibilityTool() {
        val document = DocumentBuilderFactory
            .newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(resourceFile("xml/accessibility_service.xml"))
        val service = document.documentElement

        assertEquals(
            "false",
            service.getAttributeNS(ANDROID_NAMESPACE, "isAccessibilityTool"),
        )
    }

    private fun resourceFile(resourcePath: String): File =
        listOf(
            File("src/main/res/$resourcePath"),
            File("app/src/main/res/$resourcePath"),
        ).firstOrNull { it.isFile } ?: error("Missing resource file $resourcePath")

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}

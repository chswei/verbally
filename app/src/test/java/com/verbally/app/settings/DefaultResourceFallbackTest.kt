package com.verbally.app.settings

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultResourceFallbackTest {
    @Test
    fun defaultAndroidResourcesFallBackToEnglish() {
        assertEquals("Settings", stringValue("values/strings.xml", "settings_title"))
        assertEquals("Follow system", stringValue("values/strings.xml", "settings_language_system"))
    }

    @Test
    fun traditionalChineseResourcesAreExplicitlyQualified() {
        assertEquals("設定", stringValue("values-zh-rTW/strings.xml", "settings_title"))
        assertEquals("跟隨系統", stringValue("values-zh-rTW/strings.xml", "settings_language_system"))
    }

    private fun stringValue(resourcePath: String, name: String): String {
        val file = resourceFile(resourcePath)
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val strings = document.getElementsByTagName("string")
        for (index in 0 until strings.length) {
            val node = strings.item(index)
            if (node.attributes.getNamedItem("name")?.nodeValue == name) {
                return node.textContent
            }
        }
        error("Missing string resource $name in ${file.path}")
    }

    private fun resourceFile(resourcePath: String): File =
        listOf(
            File("src/main/res/$resourcePath"),
            File("app/src/main/res/$resourcePath"),
        ).firstOrNull { it.isFile } ?: error("Missing resource file $resourcePath")
}

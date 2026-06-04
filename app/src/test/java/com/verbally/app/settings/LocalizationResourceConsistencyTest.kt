package com.verbally.app.settings

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationResourceConsistencyTest {
    @Test
    fun everyLocalizedStringsFileHasTheSameKeysAsDefaultResources() {
        val defaultKeys = stringsFor("values/strings.xml").keys

        localizedStringFiles().forEach { resourcePath ->
            assertEquals(
                "String keys should match default resources for $resourcePath",
                defaultKeys,
                stringsFor(resourcePath).keys,
            )
        }
    }

    @Test
    fun localizedStringPlaceholdersMatchDefaultResources() {
        val defaultStrings = stringsFor("values/strings.xml")
        val defaultPlaceholders = defaultStrings.mapValues { (_, value) -> value.placeholderSignature() }

        localizedStringFiles().forEach { resourcePath ->
            val localizedStrings = stringsFor(resourcePath)
            localizedStrings.forEach { (name, value) ->
                assertEquals(
                    "Placeholder signature for $name should match default resources in $resourcePath",
                    defaultPlaceholders.getValue(name),
                    value.placeholderSignature(),
                )
            }
        }
    }

    @Test
    fun localeConfigMatchesSupportedManualLanguages() {
        val configuredLocales = xmlAttributeValues(
            resourceFile("xml/locales_config.xml"),
            tagName = "locale",
            namespace = ANDROID_NAMESPACE,
            attributeName = "name",
        )

        assertEquals(
            AppLanguage.entries
                .filterNot { it == AppLanguage.SYSTEM }
                .map { it.languageTag },
            configuredLocales,
        )
    }

    @Test
    fun supportedManualLanguagesHaveResourceDirectories() {
        val resourceDirectories = resourceRoot()
            .listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values") }
            .map { it.name }
            .toSet()

        AppLanguage.entries.filterNot { it == AppLanguage.SYSTEM }.forEach { language ->
            assertTrue(
                "Missing resource directory for ${language.languageTag}",
                resourceDirectories.contains(language.valuesDirectoryName()),
            )
        }
    }

    private fun localizedStringFiles(): List<String> =
        resourceRoot()
            .listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .map { "${it.name}/strings.xml" }
            .sorted()

    private fun stringsFor(resourcePath: String): Map<String, String> {
        val document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(resourceFile(resourcePath))
        val strings = document.getElementsByTagName("string")
        return buildMap {
            for (index in 0 until strings.length) {
                val node = strings.item(index)
                val name = node.attributes.getNamedItem("name")?.nodeValue ?: continue
                check(!containsKey(name)) { "Duplicate string resource $name in $resourcePath" }
                put(name, node.textContent)
            }
        }
    }

    private fun String.placeholderSignature(): List<String> =
        placeholderRegex.findAll(this)
            .map { match -> match.value }
            .filterNot { it == "%%" }
            .toList()

    private fun xmlAttributeValues(
        file: File,
        tagName: String,
        namespace: String,
        attributeName: String,
    ): List<String> {
        val document = DocumentBuilderFactory
            .newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(file)
        val nodes = document.getElementsByTagName(tagName)
        return buildList {
            for (index in 0 until nodes.length) {
                add(nodes.item(index).attributes.getNamedItemNS(namespace, attributeName).nodeValue)
            }
        }
    }

    private fun AppLanguage.valuesDirectoryName(): String =
        when (this) {
            AppLanguage.SYSTEM -> "values"
            AppLanguage.TRADITIONAL_CHINESE -> "values-zh-rTW"
            AppLanguage.ENGLISH -> "values-en"
            AppLanguage.SPANISH -> "values-es"
            AppLanguage.FRENCH -> "values-fr"
            AppLanguage.GERMAN -> "values-de"
            AppLanguage.ITALIAN -> "values-it"
            AppLanguage.PORTUGUESE_BRAZIL -> "values-pt-rBR"
            AppLanguage.JAPANESE -> "values-ja"
            AppLanguage.KOREAN -> "values-ko"
            AppLanguage.SIMPLIFIED_CHINESE -> "values-zh-rCN"
        }

    private fun resourceFile(resourcePath: String): File =
        listOf(
            File("src/main/res/$resourcePath"),
            File("app/src/main/res/$resourcePath"),
        ).firstOrNull { it.isFile } ?: error("Missing resource file $resourcePath")

    private fun resourceRoot(): File =
        listOf(
            File("src/main/res"),
            File("app/src/main/res"),
        ).firstOrNull { it.isDirectory } ?: error("Missing resource root")

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        val placeholderRegex = Regex("%(?:\\d+\\$)?[%bcdeEfgGosxX]")
    }
}

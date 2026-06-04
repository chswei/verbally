package com.verbally.app.system

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestReleaseMetadataTest {
    @Test
    fun microphoneFeatureIsExplicitlyDeclaredForStoreFiltering() {
        val document = DocumentBuilderFactory
            .newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(projectFile("app/src/main/AndroidManifest.xml"))

        val features = document.getElementsByTagName("uses-feature")
        val microphoneFeature = (0 until features.length)
            .map { features.item(it) }
            .firstOrNull {
                it.attributes.getNamedItemNS(ANDROID_NAMESPACE, "name")?.nodeValue ==
                    "android.hardware.microphone"
            }

        assertTrue("Expected manifest to declare android.hardware.microphone", microphoneFeature != null)
        assertEquals(
            "true",
            microphoneFeature!!.attributes.getNamedItemNS(ANDROID_NAMESPACE, "required").nodeValue,
        )
    }

    private fun projectFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.isFile } ?: error("Missing project file $path")

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}

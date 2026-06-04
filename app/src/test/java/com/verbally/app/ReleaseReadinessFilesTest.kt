package com.verbally.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessFilesTest {
    private val releasePreparationChangeName = "prepare-store-open-source-release"

    @Test
    fun rootOpenSourceAndPolicyFilesExist() {
        assertProjectFileContains("LICENSE", "Apache License")
        assertProjectFileContains("NOTICE", "Verbally")
        assertProjectFileContains("PRIVACY.md", "https://chswei.github.io/verbally/privacy/")
        assertProjectFileContains("README.md", "F-Droid")
        assertProjectFileContains("README.zh-TW.md", "F-Droid")
        assertProjectFileContains("CONTRIBUTING.md", "./gradlew testDebugUnitTest")
        assertProjectFileContains("CODE_OF_CONDUCT.md", "Contributor Covenant")
        assertProjectFileContains("SECURITY.md", "security")
        assertProjectFileContains("SUPPORT.md", "support")
        assertProjectFileContains("CHANGELOG.md", "0.1.0")
    }

    @Test
    fun githubCommunityFilesExist() {
        assertProjectFileContains(".github/ISSUE_TEMPLATE/bug_report.yml", "name: Bug report")
        assertProjectFileContains(".github/ISSUE_TEMPLATE/feature_request.yml", "name: Feature request")
        assertProjectFileContains(".github/ISSUE_TEMPLATE/config.yml", "blank_issues_enabled: false")
        assertProjectFileContains(".github/pull_request_template.md", "Privacy")
        assertProjectFileContains(".github/workflows/android.yml", "./gradlew testDebugUnitTest")
    }

    @Test
    fun storeAndReleaseDocsExist() {
        assertProjectFileContains("docs/store/google-play.md", "Google Play")
        assertProjectFileContains("docs/store/data-safety.md", "Data safety")
        assertProjectFileContains("docs/store/accessibility-declaration.md", "AccessibilityService API")
        assertProjectFileContains("docs/store/f-droid.md", "NonFreeNet")
        assertProjectFileContains("docs/release.md", "bundleRelease")
        assertProjectFileContains("docs/index.md", "Privacy Policy")
        assertProjectFileContains("docs/privacy.md", "https://chswei.github.io/verbally/privacy/")
    }

    @Test
    fun fastlaneMetadataExistsForEnglishAndTraditionalChinese() {
        assertProjectFileContains(
            "fastlane/metadata/android/en-US/title.txt",
            "Verbally",
        )
        assertMaxLength("fastlane/metadata/android/en-US/short_description.txt", 80)
        assertProjectFileContains(
            "fastlane/metadata/android/en-US/full_description.txt",
            "AccessibilityService",
        )
        assertProjectFileContains(
            "fastlane/metadata/android/en-US/changelogs/1.txt",
            "Initial",
        )
        assertProjectFileContains(
            "fastlane/metadata/android/zh-TW/title.txt",
            "Verbally",
        )
        assertMaxLength("fastlane/metadata/android/zh-TW/short_description.txt", 80)
        assertProjectFileContains(
            "fastlane/metadata/android/zh-TW/full_description.txt",
            "輔助使用",
        )
        assertProjectFileExists("fastlane/metadata/android/en-US/images/icon.png")
        assertProjectFileExists("fastlane/metadata/android/en-US/images/featureGraphic.png")
        assertProjectFileExists("fastlane/metadata/android/en-US/images/phoneScreenshots/1.png")
        assertProjectFileExists("fastlane/metadata/android/en-US/images/phoneScreenshots/4.png")
    }

    @Test
    fun openspecReleasePreparationChangeIsArchivedAndSynced() {
        val archivedChangePath = archivedOpenSpecChangePath(releasePreparationChangeName)

        assertProjectFileContains(
            "$archivedChangePath/proposal.md",
            "Play Store",
        )
        assertProjectFileContains(
            "$archivedChangePath/tasks.md",
            "F-Droid",
        )
        assertProjectFileContains(
            "$archivedChangePath/specs/floating-dictation-overlay/spec.md",
            "Accessibility disclosure",
        )
        assertProjectFileContains(
            "openspec/specs/floating-dictation-overlay/spec.md",
            "Accessibility disclosure",
        )
        assertProjectFileContains(
            "openspec/specs/local-history-and-settings/spec.md",
            "Public privacy and store metadata",
        )
    }

    @Test
    fun releaseReadinessDocsDoNotPinActiveOpenSpecChangePaths() {
        val scannedFiles = listOf(
            "README.md",
            "README.zh-TW.md",
            "app/src/test/java/com/verbally/app/ReleaseReadinessFilesTest.kt",
        ).map(::projectFile) + projectFilesUnder("docs")
        val activeChangePathPattern = Regex("""openspec/changes/(?!archive(?:/|\b|$))[A-Za-z0-9._-]+/?""")
        val violations = scannedFiles.flatMap { file ->
            activeChangePathPattern.findAll(file.readText())
                .map { "${file.toProjectPath()}: ${it.value}" }
                .toList()
        }

        assertTrue(
            "Release readiness docs/tests must use main specs or archived OpenSpec paths, " +
                "not active change paths: $violations",
            violations.isEmpty(),
        )
    }

    private fun assertProjectFileExists(path: String) {
        assertTrue("Expected $path to exist", projectFile(path).isFile)
    }

    private fun assertProjectFileContains(path: String, expected: String) {
        val file = projectFile(path)
        assertTrue("Expected $path to exist", file.isFile)
        assertTrue(
            "Expected $path to contain $expected",
            file.readText().contains(expected),
        )
    }

    private fun assertMaxLength(path: String, maxLength: Int) {
        val text = projectFile(path).readText().trim()
        assertTrue("Expected $path to be at most $maxLength chars", text.length <= maxLength)
    }

    private fun archivedOpenSpecChangePath(changeName: String): String {
        val archivePath = "openspec/changes/archive"
        val archiveRoot = projectFile(archivePath)
        assertTrue("Expected $archivePath to exist", archiveRoot.isDirectory)

        val matches = archiveRoot.listFiles()
            ?.filter { it.isDirectory && it.name.endsWith("-$changeName") }
            ?.sortedBy { it.name }
            .orEmpty()

        assertTrue(
            "Expected exactly one archived OpenSpec change ending with -$changeName, " +
                "found ${matches.map { it.name }}",
            matches.size == 1,
        )

        return "$archivePath/${matches.single().name}"
    }

    private fun projectFilesUnder(path: String): List<File> {
        val root = projectFile(path)
        assertTrue("Expected $path to exist", root.exists())
        return root.walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.toProjectPath() }
            .toList()
    }

    private fun File.toProjectPath(): String =
        path.removePrefix("../")

    private fun projectFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.exists() } ?: File(path)
}

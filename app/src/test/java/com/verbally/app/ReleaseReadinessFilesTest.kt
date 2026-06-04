package com.verbally.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessFilesTest {
    @Test
    fun rootOpenSourceAndPolicyFilesExist() {
        assertProjectFileContains("LICENSE", "Apache License")
        assertProjectFileContains("NOTICE", "Verbally")
        assertProjectFileContains("PRIVACY.md", "https://github.com/chswei/verbally/blob/main/PRIVACY.md")
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
    fun openspecReleasePreparationChangeExists() {
        assertProjectFileContains(
            "openspec/changes/prepare-store-open-source-release/proposal.md",
            "Play Store",
        )
        assertProjectFileContains(
            "openspec/changes/prepare-store-open-source-release/tasks.md",
            "F-Droid",
        )
        assertProjectFileContains(
            "openspec/changes/prepare-store-open-source-release/specs/floating-dictation-overlay/spec.md",
            "Accessibility disclosure",
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

    private fun projectFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.exists() } ?: File(path)
}

package com.verbally.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DictationContentGuardTest {
    @Test
    fun rawTranscriptOnlySuppressesBlankOrExplicitNoContentSentinel() {
        assertTrue(DictationContentGuard.rawTranscriptHasNoContent(""))
        assertTrue(DictationContentGuard.rawTranscriptHasNoContent("  "))
        assertTrue(DictationContentGuard.rawTranscriptHasNoContent("<NO_DICTATION_CONTENT>"))

        assertFalse(DictationContentGuard.rawTranscriptHasNoContent("鳥叫聲"))
        assertFalse(DictationContentGuard.rawTranscriptHasNoContent("請輸入內容"))
        assertFalse(DictationContentGuard.rawTranscriptHasNoContent("沒有偵測到語音"))
        assertFalse(DictationContentGuard.rawTranscriptHasNoContent("No speech detected."))
        assertFalse(DictationContentGuard.rawTranscriptHasNoContent("Thank you for watching."))
    }

    @Test
    fun cleanedTextSuppressesNoContentSystemMessagesAndKnownEmptyHallucinations() {
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("目前沒有內容，請輸入內容。"))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("沒有偵測到語音。"))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("No speech detected."))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("Thank you for watching."))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("Subtitles by Amara.org"))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("You."))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("The."))
        assertTrue(DictationContentGuard.cleanedTextHasNoContent("I'm going to go to the next video."))
    }

    @Test
    fun cleanedTextDoesNotSuppressLegitimateShortDictation() {
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("請輸入內容"))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("請在欄位輸入內容"))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("音樂"))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("今天背景噪音很大"))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("Thank you."))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("Who will write the subtitles by Friday?"))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("Could you send this?"))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("The meeting starts now."))
        assertFalse(DictationContentGuard.cleanedTextHasNoContent("I'm going to go to the next video with Sarah."))
    }
}

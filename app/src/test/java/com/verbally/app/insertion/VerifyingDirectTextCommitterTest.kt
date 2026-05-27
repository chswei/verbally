package com.verbally.app.insertion

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifyingDirectTextCommitterTest {
    @Test
    fun reportsSuccessOnlyAfterCommittedTextAppearsInSurroundingText() = runBlocking {
        val connection = FakeDirectTextConnection(
            commitOutcomes = ArrayDeque(listOf(CommitOutcome.VISIBLE)),
        )

        val inserted = VerifyingDirectTextCommitter(
            connectionProvider = { connection },
            afterCommitDelay = {},
        ).commitText("整理後文字")

        assertTrue(inserted)
        assertEquals(listOf("整理後文字"), connection.commits)
        assertEquals(listOf(500 to 500), connection.surroundingTextRequests)
    }

    @Test
    fun retriesWhenCommitTextDoesNotAppearInSurroundingText() = runBlocking {
        val connection = FakeDirectTextConnection(
            commitOutcomes = ArrayDeque(
                listOf(
                    CommitOutcome.SILENT_FAILURE,
                    CommitOutcome.VISIBLE,
                ),
            ),
        )

        val inserted = VerifyingDirectTextCommitter(
            connectionProvider = { connection },
            afterCommitDelay = {},
        ).commitText("整理後文字")

        assertTrue(inserted)
        assertEquals(listOf("整理後文字", "整理後文字"), connection.commits)
    }

    @Test
    fun failsAfterThreeUnverifiedCommitAttempts() = runBlocking {
        val connection = FakeDirectTextConnection(
            commitOutcomes = ArrayDeque(
                listOf(
                    CommitOutcome.SILENT_FAILURE,
                    CommitOutcome.THROWS,
                    CommitOutcome.SILENT_FAILURE,
                ),
            ),
        )

        val inserted = VerifyingDirectTextCommitter(
            connectionProvider = { connection },
            afterCommitDelay = {},
        ).commitText("整理後文字")

        assertFalse(inserted)
        assertEquals(listOf("整理後文字", "整理後文字", "整理後文字"), connection.commits)
    }

    private enum class CommitOutcome {
        VISIBLE,
        SILENT_FAILURE,
        THROWS,
    }

    private class FakeDirectTextConnection(
        private val commitOutcomes: ArrayDeque<CommitOutcome>,
    ) : DirectTextConnection {
        val commits = mutableListOf<String>()
        val surroundingTextRequests = mutableListOf<Pair<Int, Int>>()
        private var surroundingText = "原本文字"

        override val editorPackageName: String = "test.app"

        override fun commitText(text: String) {
            commits += text
            when (commitOutcomes.removeFirstOrNull() ?: CommitOutcome.SILENT_FAILURE) {
                CommitOutcome.VISIBLE -> surroundingText += text
                CommitOutcome.SILENT_FAILURE -> Unit
                CommitOutcome.THROWS -> error("commit failed")
            }
        }

        override fun surroundingText(beforeLength: Int, afterLength: Int): CharSequence? {
            surroundingTextRequests += beforeLength to afterLength
            return surroundingText
        }
    }
}

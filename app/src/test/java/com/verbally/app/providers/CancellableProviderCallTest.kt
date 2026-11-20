package com.verbally.app.providers

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test

class CancellableProviderCallTest {
    @Test fun cancellationStopsPendingHeaders() = verifyCancellation(false)
    @Test fun cancellationStopsPendingBody() = verifyCancellation(true)

    private fun verifyCancellation(duringBody: Boolean) = runBlocking {
        MockWebServer().use { server ->
            val response = MockResponse.Builder().body("hello")
            if (duringBody) response.bodyDelay(30, TimeUnit.SECONDS)
            else response.headersDelay(30, TimeUnit.SECONDS)
            server.enqueue(response.build())
            server.start()
            val call = OkHttpClient().newCall(Request.Builder().url(server.url("/")).build())
            val readingBody = CountDownLatch(1)
            val job = async(Dispatchers.IO) {
                call.awaitResponse {
                    readingBody.countDown()
                    it.body.string()
                }
            }
            assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            if (duringBody) assertTrue(readingBody.await(5, TimeUnit.SECONDS))
            withTimeout(2_000) { job.cancelAndJoin() }
            assertTrue(call.isCanceled())
            assertTrue(job.isCancelled)
        }
    }

    @Test fun successfulResponseIsConsumedAndReturned() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse.Builder().body("hello").build())
            server.start()
            val call = OkHttpClient().newCall(Request.Builder().url(server.url("/")).build())
            assertEquals("hello", call.awaitResponse { it.body.string() })
        }
    }
}

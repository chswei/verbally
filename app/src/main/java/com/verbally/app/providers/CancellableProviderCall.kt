package com.verbally.app.providers

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

/** Consume the response on OkHttp's worker, keeping cancellation active through body reads. */
internal suspend fun <T> Call.awaitResponse(consume: (Response) -> T): T =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val result = response.use {
                        if (!continuation.isActive) return
                        consume(it)
                    }
                    continuation.resume(result)
                } catch (error: Exception) {
                    continuation.resumeWithException(error)
                }
            }
        })
    }

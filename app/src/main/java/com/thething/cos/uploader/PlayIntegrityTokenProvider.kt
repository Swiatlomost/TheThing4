package com.thething.cos.uploader

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Helper object that fetches Play Integrity tokens for uploads.
 * Uses a blocking Tasks.await because uploads run on a debug workflow only.
 */
object PlayIntegrityTokenProvider {
    private const val TAG = "PlayIntegrityToken"
    private const val TIMEOUT_SECONDS = 30L

    suspend fun obtain(context: Context, nonce: String): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val manager = IntegrityManagerFactory.create(context)
                val request = IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .build()
                val response =
                    Tasks.await(manager.requestIntegrityToken(request), TIMEOUT_SECONDS, TimeUnit.SECONDS)
                response.token()
            }.onFailure { error ->
                Log.w(TAG, "Play Integrity token request failed", error)
            }.getOrNull()
        }
    }
}

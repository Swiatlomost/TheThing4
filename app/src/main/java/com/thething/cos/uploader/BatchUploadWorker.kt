package com.thething.cos.uploader

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.io.IOException

class BatchUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val payload = BatchUploadPayload.fromWorkData(inputData) ?: return Result.failure()
        Log.i(
            TAG,
            "Work attempt=$runAttemptCount hash=${payload.hashBase64.take(8)} device=${payload.deviceId}"
        )
        return try {
            BatchUploader.submitSingle(
                context = applicationContext,
                merkleRootBase64 = payload.merkleRootBase64,
                hashBase64 = payload.hashBase64,
                signatureBase64 = payload.signatureBase64,
                publicKeyBase64 = payload.publicKeyBase64,
                timestampMs = payload.timestampMs,
                deviceId = payload.deviceId
            )
            Result.success()
        } catch (error: Throwable) {
            if (shouldRetry(error) && runAttemptCount < MAX_ATTEMPTS) {
                Log.w(TAG, "Retry scheduled for batch upload", error)
                Result.retry()
            } else {
                Log.e(TAG, "Giving up on batch upload", error)
                Result.failure()
            }
        }
    }

    private fun shouldRetry(error: Throwable): Boolean {
        return when (error) {
            is UploadSkippedException -> true
            is StatusRuntimeException -> when (error.status.code) {
                Status.Code.UNAVAILABLE,
                Status.Code.DEADLINE_EXCEEDED,
                Status.Code.RESOURCE_EXHAUSTED,
                Status.Code.INTERNAL,
                Status.Code.ABORTED -> true
                else -> false
            }
            is IOException -> true
            else -> false
        }
    }

    companion object {
        private const val TAG = "BatchUploadWorker"
        private const val MAX_ATTEMPTS = 5
    }
}

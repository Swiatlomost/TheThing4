package com.thething.cos.uploader

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BatchUploadScheduler {
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    private val backoffDurationSeconds = TimeUnit.MINUTES.toSeconds(1)

    fun enqueue(context: Context, payload: BatchUploadPayload, expedite: Boolean) {
        val builder = OneTimeWorkRequestBuilder<BatchUploadWorker>()
            .setInputData(payload.toWorkData())
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                backoffDurationSeconds,
                TimeUnit.SECONDS
            )
        if (expedite) {
            builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        val workRequest = builder.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            payload.uniqueWorkName(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}

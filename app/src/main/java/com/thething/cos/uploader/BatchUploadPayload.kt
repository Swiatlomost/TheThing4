package com.thething.cos.uploader

import androidx.work.Data
import androidx.work.workDataOf
import java.nio.charset.StandardCharsets
import java.util.UUID

data class BatchUploadPayload(
    val merkleRootBase64: String,
    val hashBase64: String,
    val signatureBase64: String,
    val publicKeyBase64: String,
    val timestampMs: Long,
    val deviceId: String
) {
    fun toWorkData(): Data = workDataOf(
        KEY_MERKLE_ROOT to merkleRootBase64,
        KEY_HASH to hashBase64,
        KEY_SIGNATURE to signatureBase64,
        KEY_PUBLIC_KEY to publicKeyBase64,
        KEY_TIMESTAMP to timestampMs,
        KEY_DEVICE_ID to deviceId
    )

    fun uniqueWorkName(): String {
        val bytes =
            "$merkleRootBase64|$hashBase64|$timestampMs|$deviceId".toByteArray(StandardCharsets.UTF_8)
        return "$WORK_NAME_PREFIX-${UUID.nameUUIDFromBytes(bytes)}"
    }

    companion object {
        private const val KEY_MERKLE_ROOT = "batch_merkle_root_b64"
        private const val KEY_HASH = "batch_hash_b64"
        private const val KEY_SIGNATURE = "batch_signature_b64"
        private const val KEY_PUBLIC_KEY = "batch_public_key_b64"
        private const val KEY_TIMESTAMP = "batch_timestamp_ms"
        private const val KEY_DEVICE_ID = "batch_device_id"
        const val WORK_NAME_PREFIX = "poi_batch_upload"

        fun fromWorkData(data: Data): BatchUploadPayload? {
            val merkle = data.getString(KEY_MERKLE_ROOT) ?: return null
            val hash = data.getString(KEY_HASH) ?: return null
            val signature = data.getString(KEY_SIGNATURE) ?: return null
            val publicKey = data.getString(KEY_PUBLIC_KEY) ?: return null
            val timestamp = data.getLong(KEY_TIMESTAMP, -1L)
            val deviceId = data.getString(KEY_DEVICE_ID) ?: "UNKNOWN"
            if (timestamp <= 0) return null
            return BatchUploadPayload(
                merkleRootBase64 = merkle,
                hashBase64 = hash,
                signatureBase64 = signature,
                publicKeyBase64 = publicKey,
                timestampMs = timestamp,
                deviceId = deviceId
            )
        }
    }
}

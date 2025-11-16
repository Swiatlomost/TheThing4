package com.thething.cos.uploader

import android.content.Context
import android.util.Log
import com.google.protobuf.ByteString
import com.thething.cos.BuildConfig
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import io.thething.poi.validator.AttestationPayload
import io.thething.poi.validator.BatchEntryMetrics
import io.thething.poi.validator.BatchProofRequest
import io.thething.poi.validator.PoIValidatorGrpc
import java.security.MessageDigest
import java.util.Base64
import kotlin.text.Charsets

/**
 * Minimal gRPC uploader for dev: sends a single-entry batch to 10.0.2.2:50051.
 * In prod, switch to TLS + WorkManager + retries.
 */
object BatchUploader {
    private const val TAG = "BatchUploader"

    suspend fun submitSingle(
        context: Context,
        merkleRootBase64: String,
        hashBase64: String,
        signatureBase64: String,
        publicKeyBase64: String,
        timestampMs: Long,
        deviceId: String = "EMU-PIXEL5"
    ) {
        val channel: ManagedChannel = OkHttpChannelBuilder
            .forAddress(BuildConfig.VALIDATOR_HOST, BuildConfig.VALIDATOR_PORT)
            .usePlaintext()
            .build()
        try {
            val stub = PoIValidatorGrpc.newBlockingStub(channel)

            val nonce = generateNonce(deviceId, merkleRootBase64, timestampMs)
            val integrityToken = PlayIntegrityTokenProvider.obtain(context, nonce)
            if (integrityToken.isNullOrBlank()) {
                Log.w(TAG, "Play Integrity token unavailable; upload skipped")
                return
            }
            Log.i(TAG, "Integrity token obtained (nonce=$nonce)")

            val metrics = BatchEntryMetrics.newBuilder()
                .setLedgerIndex(0)
                .setEntropy(0.0)
                .setTrustScore(0.0)
                .setFar(0.0)
                .setFrr(0.0)
                .setHash(decodeBase64(hashBase64))
                .setTimestampMs(timestampMs)
                .build()

            val request = BatchProofRequest.newBuilder()
                .setDeviceId(deviceId)
                .setBatchStart(timestampMs)
                .setBatchEnd(timestampMs)
                .setMerkleRoot(decodeBase64(merkleRootBase64))
                .addMetrics(metrics)
                .setSignature(decodeBase64(signatureBase64))
                .setPublicKey(decodeBase64(publicKeyBase64))
                .setAttestation(
                    AttestationPayload.newBuilder()
                        .setProvider("play_integrity")
                        .setToken(ByteString.copyFromUtf8(integrityToken))
                        .setNonce(nonce)
                        .build()
                )
                .build()

            val response = stub.submitBatch(request)
            Log.i(
                TAG,
                "Upload result: status=${response.status} reason=${response.reason} batch=${response.batchId}"
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Upload failed", t)
        } finally {
            channel.shutdownNow()
        }
    }
}

private fun decodeBase64(value: String): ByteString {
    val decoded = Base64.getDecoder().decode(value)
    return ByteString.copyFrom(decoded)
}

private fun generateNonce(deviceId: String, merkleRootBase64: String, timestampMs: Long): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val input = "$deviceId|$merkleRootBase64|$timestampMs"
    val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
}

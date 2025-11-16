package com.thething.cos.uploader

import android.content.Context
import android.util.Log
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import io.thething.poi.validator.BatchProofRequest
import io.thething.poi.validator.BatchEntryMetrics
import io.thething.poi.validator.AttestationPayload
import io.thething.poi.validator.PoIValidatorGrpc
import java.util.Base64

/**
 * Minimal gRPC uploader for dev: sends a single-entry batch to 10.0.2.2:50051.
 * In prod, switch to TLS + WorkManager + retries.
 */
object BatchUploader {
    private const val TAG = "BatchUploader"

    fun submitSingle(
        context: Context,
        merkleRootBase64: String,
        hashBase64: String,
        signatureBase64: String,
        publicKeyBase64: String,
        timestampMs: Long,
        deviceId: String = "EMU-PIXEL5"
    ) {
        val channel: ManagedChannel = OkHttpChannelBuilder
            .forAddress("10.0.2.2", 50051)
            .usePlaintext()
            .build()
        try {
            val stub = PoIValidatorGrpc.newBlockingStub(channel)

            val metrics = BatchEntryMetrics.newBuilder()
                .setLedgerIndex(0)
                .setEntropy(0.0)
                .setTrustScore(0.0)
                .setFar(0.0)
                .setFrr(0.0)
                .setHash(Base64.getDecoder().decode(hashBase64).let { com.google.protobuf.ByteString.copyFrom(it) })
                .setTimestampMs(timestampMs)
                .build()

            val request = BatchProofRequest.newBuilder()
                .setDeviceId(deviceId)
                .setBatchStart(timestampMs)
                .setBatchEnd(timestampMs)
                .setMerkleRoot(Base64.getDecoder().decode(merkleRootBase64).let { com.google.protobuf.ByteString.copyFrom(it) })
                .addMetrics(metrics)
                .setSignature(Base64.getDecoder().decode(signatureBase64).let { com.google.protobuf.ByteString.copyFrom(it) })
                .setPublicKey(Base64.getDecoder().decode(publicKeyBase64).let { com.google.protobuf.ByteString.copyFrom(it) })
                .setAttestation(
                    AttestationPayload.newBuilder()
                        .setProvider("play_integrity")
                        .setToken(com.google.protobuf.ByteString.copyFromUtf8("dummy"))
                        .setNonce("dev-emulator")
                        .build()
                )
                .build()

            val response = stub.submitBatch(request)
            Log.i(TAG, "Upload result: status=${response.status} reason=${response.reason} batch=${response.batchId}")
        } catch (t: Throwable) {
            Log.e(TAG, "Upload failed", t)
        } finally {
            channel.shutdownNow()
        }
    }
}

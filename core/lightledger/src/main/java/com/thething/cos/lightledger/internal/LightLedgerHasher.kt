package com.thething.cos.lightledger.internal

import com.thething.cos.lightledger.LightLedgerNative
import com.thething.cos.lightledger.model.SessionFingerprint
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Base64

object LightLedgerHasher {
    private const val HASH_ALGORITHM = "SHA-256"
    private const val MIN_NATIVE_BATCH = 2_000
    private const val DEFAULT_WARMUP_ROUNDS = 1

    fun hashFingerprint(fingerprint: SessionFingerprint): ByteArray =
        hashFingerprintNative(fingerprint) ?: hashFingerprintFallback(fingerprint)

    fun hashFingerprintBase64(fingerprint: SessionFingerprint): String =
        Base64.getEncoder().encodeToString(hashFingerprint(fingerprint))

    fun hashFingerprintNative(fingerprint: SessionFingerprint): ByteArray? =
        hashFingerprintsNative(listOf(fingerprint))?.first()

    fun hashFingerprintFallback(fingerprint: SessionFingerprint): ByteArray =
        sha256(flattenFingerprint(fingerprint))

    fun hashFingerprintsNative(
        fingerprints: List<SessionFingerprint>,
        warmupRounds: Int = DEFAULT_WARMUP_ROUNDS,
        force: Boolean = false
    ): List<ByteArray>? {
        if (!LightLedgerNative.nativeReady()) return null
        if (!force && fingerprints.size < MIN_NATIVE_BATCH) return null
        if (fingerprints.isEmpty()) return emptyList()
        val payloads = fingerprints.map(::flattenFingerprint)
        repeat(warmupRounds.coerceAtLeast(0)) {
            for (payload in payloads) {
                if (runCatching { LightLedgerNative.hashFingerprintPayload(payload) }.getOrNull() == null) {
                    return null
                }
            }
        }
        val results = mutableListOf<ByteArray>()
        for (payload in payloads) {
            val digest = runCatching { LightLedgerNative.hashFingerprintPayload(payload) }.getOrNull()
                ?: return null
            results += digest
        }
        return results
    }

    private fun flattenFingerprint(fingerprint: SessionFingerprint): ByteArray {
        val sessionBytes = fingerprint.sessionId.toByteArray()
        val motionBytes = floatArrayToBytes(fingerprint.motionVector)
        val touchBytes = fingerprint.touchSignature.toByteArray()
        val entropyBytes = doubleToBytes(fingerprint.envEntropy)
        val soundBytes = doubleToBytes(fingerprint.soundVariance)
        val batteryBytes = fingerprint.batteryCurve.toByteArray()
        val trustBytes = intToBytes(fingerprint.trustScore)
        val timestampBytes = longToBytes(fingerprint.timestampSeconds)

        val totalSize = sessionBytes.size + motionBytes.size + touchBytes.size +
            entropyBytes.size + soundBytes.size + batteryBytes.size + trustBytes.size + timestampBytes.size
        val buffer = ByteBuffer.allocate(totalSize)
        buffer.put(sessionBytes)
        buffer.put(motionBytes)
        buffer.put(touchBytes)
        buffer.put(entropyBytes)
        buffer.put(soundBytes)
        buffer.put(batteryBytes)
        buffer.put(trustBytes)
        buffer.put(timestampBytes)
        return buffer.array()
    }

    private fun sha256(payload: ByteArray): ByteArray = MessageDigest.getInstance(HASH_ALGORITHM).digest(payload)

    private fun floatArrayToBytes(values: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(values.size * 4)
        values.forEach(buffer::putFloat)
        return buffer.array()
    }

    private fun doubleToBytes(value: Double): ByteArray = ByteBuffer.allocate(8).putDouble(value).array()

    private fun longToBytes(value: Long): ByteArray = ByteBuffer.allocate(8).putLong(value).array()

    private fun intToBytes(value: Int): ByteArray = ByteBuffer.allocate(4).putInt(value).array()
}

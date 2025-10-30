package com.example.cos.lightledger.internal

import com.example.cos.lightledger.LightLedgerNative
import com.example.cos.lightledger.model.SessionFingerprint
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Base64

object LightLedgerHasher {
    private const val HASH_ALGORITHM = "SHA-256"

    fun hashFingerprint(fingerprint: SessionFingerprint): ByteArray {
        val payload = flattenFingerprint(fingerprint)
        val nativeDigest = if (LightLedgerNative.nativeReady()) {
            runCatching { LightLedgerNative.hashFingerprintPayload(payload) }.getOrNull()
        } else {
            null
        }
        return nativeDigest ?: sha256(payload)
    }

    fun hashFingerprintBase64(fingerprint: SessionFingerprint): String =
        Base64.getEncoder().encodeToString(hashFingerprint(fingerprint))

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

package com.thething.cos.lightledger

import android.content.Context
import android.util.Log
import com.thething.cos.BuildConfig
import com.thething.cos.lightledger.internal.LightLedgerBenchmarkRunner
import com.thething.cos.lightledger.internal.LightLedgerRepository
import com.thething.cos.lightledger.internal.LightLedgerSigner
import com.thething.cos.lightledger.model.SessionFingerprint
import java.util.Base64

/**
 * Executes runtime smoke checks proving native hashing, Merkle root generation
 * and hardware-backed signatures are all wired correctly.
 */
object LightLedgerRuntimeSmoke {
    private const val TAG = "LightLedgerRuntimeSmoke"
    private val samplePayload = byteArrayOf(0x01, 0x23, 0x45, 0x67)

    fun verify(context: Context) {
        if (!LightLedgerNative.nativeReady()) {
            Log.w(TAG, "Native ledger unavailable; Kotlin fallback stays active.")
            return
        }

        val digest = runCatching { LightLedgerNative.hashFingerprintPayload(samplePayload) }
            .onFailure { error ->
                Log.e(TAG, "Native ledger call failed; Kotlin fallback will be used.", error)
            }
            .getOrNull()

        if (digest == null || digest.isEmpty()) {
            Log.w(TAG, "Native ledger returned empty digest; Kotlin fallback will be used.")
            return
        }

        Log.i(TAG, "Native ledger ready; produced ${digest.size}-byte digest.")
        if (BuildConfig.DEBUG) {
            LightLedgerBenchmarkRunner.logOnce(enabled = true)
            demoMerkleProof(context)
        }
    }

    private fun demoMerkleProof(context: Context) {
        val repository = LightLedgerRepository(context)
        repository.clear() // start clean, leave snapshot after run

        val fingerprint = SessionFingerprint(
            sessionId = "smoke-${System.currentTimeMillis()}",
            motionVector = floatArrayOf(0.12f, -0.91f, 0.33f, 0.54f, -0.11f, 0.87f, 0.05f, -0.42f, 0.66f),
            touchSignature = "runtime-smoke-fingerprint",
            envEntropy = 0.82,
            soundVariance = 0.18,
            batteryCurve = "100-98-96-95-93-91",
            trustScore = 92,
            timestampSeconds = System.currentTimeMillis() / 1000L
        )

        val snapshot = repository.appendFingerprint(fingerprint)
        val hashBase64 = snapshot.latestEntry.hashBase64
        val signatureBase64 = snapshot.latestEntry.signatureBase64
        val publicKeyBase64 = snapshot.latestEntry.signerPublicKeyBase64

        val hashBytes = Base64.getDecoder().decode(hashBase64)
        val signatureBytes = Base64.getDecoder().decode(signatureBase64)
        val signatureValid = LightLedgerSigner.verify(hashBytes, signatureBytes)

        Log.i(
            TAG,
            "Merkle root demo: leaves=${snapshot.totalLeaves}, root=${snapshot.merkleRootBase64}"
        )
        Log.i(TAG, "Ledger signature valid=$signatureValid")
        Log.i(TAG, "Ledger hash (Base64 SHA-256): $hashBase64")
        Log.i(TAG, "Ledger signature (Base64 DER): $signatureBase64")
        Log.i(TAG, "Ledger signer public key (Base64 X.509): $publicKeyBase64")

        // Dev-only: upload to local validator on host
        if (BuildConfig.DEBUG) {
            try {
                com.thething.cos.uploader.BatchUploader.submitSingle(
                    context = context,
                    merkleRootBase64 = snapshot.merkleRootBase64,
                    hashBase64 = hashBase64,
                    signatureBase64 = signatureBase64,
                    publicKeyBase64 = publicKeyBase64,
                    timestampMs = System.currentTimeMillis()
                )
            } catch (t: Throwable) {
                Log.w(TAG, "Dev upload skipped", t)
            }
        }
    }
}


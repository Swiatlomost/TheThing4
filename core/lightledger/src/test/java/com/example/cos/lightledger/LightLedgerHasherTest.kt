package com.example.cos.lightledger

import com.example.cos.lightledger.internal.LightLedgerHasher
import com.example.cos.lightledger.model.SessionFingerprint
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Base64

class LightLedgerHasherTest {
    @Test
    fun `hash fingerprint is deterministic`() {
        val fingerprint = SessionFingerprint(
            sessionId = "test-session",
            motionVector = floatArrayOf(0.94f, 1.02f, 0.11f),
            touchSignature = "ab14dfe3",
            envEntropy = 0.78,
            soundVariance = 0.56,
            batteryCurve = "3.95V4.12V/15min",
            trustScore = 91,
            timestampSeconds = 1730119200
        )

        val hash = LightLedgerHasher.hashFingerprint(fingerprint)
        val encoded = Base64.getEncoder().encodeToString(hash)

        assertEquals("RgfD15yu9PIyx2qd3J7175VFCrXhigLdBS4oCkpisTE=", encoded)
    }
}

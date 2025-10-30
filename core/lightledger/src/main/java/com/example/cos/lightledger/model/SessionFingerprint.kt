package com.example.cos.lightledger.model

data class SessionFingerprint(
    val sessionId: String,
    val motionVector: FloatArray,
    val touchSignature: String,
    val envEntropy: Double,
    val soundVariance: Double,
    val batteryCurve: String,
    val trustScore: Int,
    val timestampSeconds: Long
)


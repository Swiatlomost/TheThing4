package com.example.cos.sensorharness.model

data class SensorCaptureState(
    val isCapturing: Boolean = false,
    val sampleCount: Int = 0,
    val motionMagnitude: Double = 0.0,
    val gyroMagnitude: Double = 0.0,
    val entropyEstimate: Double = 0.0,
    val trustEstimate: Double = 0.0,
    val fingerprintBase64: String? = null
)

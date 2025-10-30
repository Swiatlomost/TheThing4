package com.example.cos.sensorharness.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.cos.lightledger.internal.LightLedgerHasher
import com.example.cos.lightledger.model.SessionFingerprint
import com.example.cos.sensorharness.domain.RunningStats
import com.example.cos.sensorharness.model.SensorCaptureState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SensorCaptureController @Inject constructor(
    @ApplicationContext context: Context,
    private val clock: Clock
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _state = MutableStateFlow(SensorCaptureState())
    val state: StateFlow<SensorCaptureState> = _state.asStateFlow()

    private var capturing = false
    private var motionStats = RunningStats()
    private var gyroStats = RunningStats()

    fun startCapture() {
        if (capturing) return
        motionStats = RunningStats()
        gyroStats = RunningStats()
        capturing = true
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        _state.update { it.copy(isCapturing = true, sampleCount = 0, fingerprintBase64 = null) }
    }

    fun stopCapture() {
        if (!capturing) return
        capturing = false
        sensorManager.unregisterListener(this)
        _state.update { it.copy(isCapturing = false) }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!capturing) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val magnitude = magnitude(event.values)
                motionStats = motionStats.add(magnitude)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val magnitude = magnitude(event.values)
                gyroStats = gyroStats.add(magnitude)
            }
        }
        val count = motionStats.count.coerceAtLeast(gyroStats.count)
        val entropy = (motionStats.stdDev() + gyroStats.stdDev()) / 2.0
        val trust = (entropy / TARGET_ENTROPY).coerceIn(0.0, 1.0) * 100.0
        _state.update {
            it.copy(
                sampleCount = count,
                motionMagnitude = motionStats.mean,
                gyroMagnitude = gyroStats.mean,
                entropyEstimate = entropy,
                trustEstimate = trust,
                fingerprintBase64 = null
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun generateFingerprint(): String {
        val current = _state.value
        val fingerprint = SessionFingerprint(
            sessionId = UUID.randomUUID().toString(),
            motionVector = floatArrayOf(current.motionMagnitude.toFloat(), gyroStats.mean.toFloat(), current.entropyEstimate.toFloat()),
            touchSignature = "sensor-harness",
            envEntropy = current.entropyEstimate,
            soundVariance = 0.0,
            batteryCurve = "N/A",
            trustScore = current.trustEstimate.toInt(),
            timestampSeconds = clock.instant().epochSecond
        )
        val base64 = LightLedgerHasher.hashFingerprintBase64(fingerprint)
        _state.update { it.copy(fingerprintBase64 = base64) }
        return base64
    }

    private fun magnitude(values: FloatArray): Double {
        val x = values.getOrNull(0)?.toDouble() ?: 0.0
        val y = values.getOrNull(1)?.toDouble() ?: 0.0
        val z = values.getOrNull(2)?.toDouble() ?: 0.0
        return sqrt(x * x + y * y + z * z)
    }

    companion object {
        private const val TARGET_ENTROPY = 5.0
    }
}

package com.thething.cos.lightledger.internal

import android.os.Build
import android.util.Log
import com.thething.cos.lightledger.model.SessionFingerprint
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import kotlin.system.measureNanoTime

/**
 * Simple benchmark harness comparing JNI hashing vs. Kotlin fallback.
 * Intended for debug builds to provide quick telemetry for Kai.
 */
object LightLedgerBenchmarkRunner {
    private const val TAG = "LightLedgerBenchmark"
    private val loggedOnce = AtomicBoolean(false)

    data class Timing(
        val minMs: Double,
        val maxMs: Double,
        val averageMs: Double,
        val medianMs: Double
    ) {
        fun perItem(batchSize: Int): Double = averageMs / batchSize

        companion object {
            fun from(samples: List<Double>): Timing {
                val sorted = samples.sorted()
                val min = sorted.first()
                val max = sorted.last()
                val avg = sorted.sum() / sorted.size
                val median = if (sorted.size % 2 == 0) {
                    (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
                } else {
                    sorted[sorted.size / 2]
                }
                return Timing(min, max, avg, median)
            }
        }
    }

    data class BatchResult(
        val batchSize: Int,
        val nativeTiming: Timing?,
        val fallbackTiming: Timing
    )

    data class BenchmarkResult(
        val deviceModel: String,
        val apiLevel: Int,
        val batches: List<BatchResult>
    ) {
        fun log(tag: String = TAG) {
            Log.i(tag, "Benchmark on $deviceModel (API $apiLevel)")
            batches.forEach { batch ->
                val fallback = batch.fallbackTiming
                Log.i(
                    tag,
                    "Batch ${batch.batchSize}: fallback avg=${fallback.averageMs.format()} ms " +
                        "(${fallback.perItem(batch.batchSize).format()} ms/hash)"
                )
                val native = batch.nativeTiming
                if (native != null) {
                    Log.i(
                        tag,
                        "Batch ${batch.batchSize}: native  avg=${native.averageMs.format()} ms " +
                            "(${native.perItem(batch.batchSize).format()} ms/hash)"
                    )
                } else {
                    Log.w(tag, "Batch ${batch.batchSize}: native hash unavailable, fallback only.")
                }
            }
        }
    }

    fun run(
        batchSizes: List<Int> = listOf(1_000, 5_000, 10_000),
        iterations: Int = 3,
        seed: Long = 42L
    ): BenchmarkResult {
        require(iterations > 0) { "iterations must be > 0" }
        val random = Random(seed)
        val batches = batchSizes.map { size ->
            val fingerprints = List(size) { index -> randomFingerprint(random, index) }
            BatchResult(
                batchSize = size,
                nativeTiming = sampleNative(fingerprints, iterations),
                fallbackTiming = sampleFallback(fingerprints, iterations)
            )
        }
        return BenchmarkResult(
            deviceModel = Build.MODEL ?: "unknown",
            apiLevel = Build.VERSION.SDK_INT,
            batches = batches
        )
    }

    fun logOnce(
        enabled: Boolean,
        batchSizes: List<Int> = listOf(1_000, 5_000, 10_000),
        iterations: Int = 3
    ) {
        if (!enabled) return
        if (!loggedOnce.compareAndSet(false, true)) return
        run(batchSizes, iterations).log()
    }

    private fun sampleNative(
        fingerprints: List<SessionFingerprint>,
        iterations: Int
    ): Timing? {
        val warmupNeeded = LightLedgerHasher.hashFingerprintsNative(
            listOf(fingerprints.first()),
            force = true
        ) != null
        if (!warmupNeeded) return null
        val samples = mutableListOf<Double>()
        repeat(iterations) {
            val nanos = measureNanoTime {
                val digests = LightLedgerHasher.hashFingerprintsNative(
                    fingerprints,
                    warmupRounds = 1,
                    force = true
                )
                if (digests == null || digests.size != fingerprints.size) {
                    return null
                }
            }
            samples += nanos.toMillis()
        }
        return Timing.from(samples)
    }

    private fun sampleFallback(
        fingerprints: List<SessionFingerprint>,
        iterations: Int
    ): Timing {
        val samples = mutableListOf<Double>()
        repeat(iterations) {
            val nanos = measureNanoTime {
                for (fingerprint in fingerprints) {
                    LightLedgerHasher.hashFingerprintFallback(fingerprint)
                }
            }
            samples += nanos.toMillis()
        }
        return Timing.from(samples)
    }

    private fun randomFingerprint(random: Random, index: Int): SessionFingerprint {
        val motion = FloatArray(9) { random.nextFloat() * 2f - 1f }
        val touch = buildString(32) {
            repeat(32) { append('a' + random.nextInt(26)) }
        }
        val battery = buildString(24) {
            repeat(6) {
                append((random.nextInt(900) + 100).toString())
                if (it < 5) append('-')
            }
        }
        return SessionFingerprint(
            sessionId = "session-${index}-${random.nextLong().absolute()}",
            motionVector = motion,
            touchSignature = touch,
            envEntropy = random.nextDouble(0.5, 1.0),
            soundVariance = random.nextDouble(0.0, 1.0),
            batteryCurve = battery,
            trustScore = random.nextInt(40, 100),
            timestampSeconds = System.currentTimeMillis() / 1000L - random.nextLong(0, 3_600)
        )
    }

    private fun Double.format(): String = String.format("%.2f", this)

    private fun Long.toMillis(): Double = this / 1_000_000.0

    private fun Long.absolute(): Long = if (this < 0) -this else this
}

package com.thething.cos.sensorharness.domain

data class RunningStats(
    val count: Int = 0,
    val mean: Double = 0.0,
    val m2: Double = 0.0
) {
    fun add(value: Double): RunningStats {
        val newCount = count + 1
        val delta = value - mean
        val newMean = mean + delta / newCount
        val newM2 = m2 + delta * (value - newMean)
        return RunningStats(newCount, newMean, newM2)
    }

    fun variance(): Double = if (count > 1) m2 / (count - 1) else 0.0
    fun stdDev(): Double = kotlin.math.sqrt(variance())
}

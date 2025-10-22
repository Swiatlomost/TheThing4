package com.example.thething4.core.cell

import com.example.thething4.core.time.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

class CellLifecycleStateMachine(
    private val timeProvider: TimeProvider
) {

    fun evaluate(
        lifecycle: CellLifecycle,
        reference: Duration = timeProvider.now()
    ): CellStage {
        val age = (reference - lifecycle.createdAt).coerceAtLeast(ZERO)
        val warmup = lifecycle.warmupDuration
        val saturation = lifecycle.saturationDuration

        return when {
            age < warmup -> CellStage.Seed(fraction(age, warmup))
            age < warmup + saturation -> {
                val budProgress = fraction(age - warmup, saturation)
                CellStage.Bud(budProgress)
            }
            else -> CellStage.Mature()
        }
    }

    private fun fraction(elapsed: Duration, total: Duration): Float {
        if (total <= ZERO) return 1f
        val ratio = (elapsed / total).toFloat()
        return ratio.coerceIn(0f, 1f)
    }

    private fun Duration.coerceAtLeast(minDuration: Duration): Duration =
        if (this < minDuration) minDuration else this
}

package com.example.thething4.core.time

import android.os.SystemClock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Uses [SystemClock.elapsedRealtime] to ensure we count time even when the screen is off.
 */
class MonotonicTimeProvider : TimeProvider {
    override fun now(): Duration = SystemClock.elapsedRealtime().milliseconds
}

package com.example.thething4.core.time

import kotlin.time.Duration

/**
 * Abstraction over monotonic time used to compute the age of Coś cells.
 */
fun interface TimeProvider {
    fun now(): Duration
}

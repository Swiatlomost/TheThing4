package com.example.cos.sensorharness.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RunningStatsTest {
    @Test
    fun `adds values and computes variance`() {
        var stats = RunningStats()
        listOf(1.0, 2.0, 3.0, 4.0).forEach { stats = stats.add(it) }
        assertEquals(4, stats.count)
        assertEquals(2.5, stats.mean, 1e-6)
        assertEquals(1.667, stats.variance(), 1e-3)
    }
}

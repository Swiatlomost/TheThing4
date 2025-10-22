package com.example.thething4.core.cell

import com.example.thething4.core.time.TimeProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTimeProvider(var current: Duration) : TimeProvider {
    override fun now(): Duration = current
}

class CellLifecycleStateMachineTest {
    private val fakeTime = FakeTimeProvider(Duration.ZERO)
    private val stateMachine = CellLifecycleStateMachine(fakeTime)
    private val lifecycle = CellLifecycle.default(Duration.ZERO)

    @Test
    fun `seed progress grows until warmup threshold`() {
        fakeTime.current = 5.seconds
        val stage = stateMachine.evaluate(lifecycle)
        assertTrue(stage is CellStage.Seed)
        assertEquals(0.5f, stage.progress, 0.01f)
    }

    @Test
    fun `enters bud stage after warmup`() {
        fakeTime.current = lifecycle.warmupDuration + 3.seconds
        val stage = stateMachine.evaluate(lifecycle)
        assertTrue(stage is CellStage.Bud)
        assertEquals(0.2f, stage.progress, 0.01f)
    }

    @Test
    fun `reaches mature stage after saturation completes`() {
        fakeTime.current = lifecycle.warmupDuration + lifecycle.saturationDuration + 1.seconds
        val stage = stateMachine.evaluate(lifecycle)
        assertTrue(stage is CellStage.Mature)
        assertEquals(1f, stage.progress, 0.0f)
    }
}

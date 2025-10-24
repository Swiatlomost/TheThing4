
package com.example.cos.lifecycle

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CosLifecycleEngineTest {

    private lateinit var engine: DefaultCosLifecycleEngine

    @Before
    fun setup() {
        engine = DefaultCosLifecycleEngine(object : TimeProvider {
            override fun tickMillis(): Long = 1_000L
        })
        engine.pause()
    }

    @Test
    fun `initial state contains single seed cell`() = runBlocking {
        val state = engine.state.first()
        assertEquals(1, state.cells.size)
        val stage = state.cells.first().stage
        assertTrue(stage is CellStage.Seed)
        assertEquals(0f, (stage as CellStage.Seed).progress, 0.001f)
    }
}


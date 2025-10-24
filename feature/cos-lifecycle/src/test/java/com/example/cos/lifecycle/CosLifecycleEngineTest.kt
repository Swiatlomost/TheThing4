
package com.example.cos.lifecycle

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CosLifecycleEngineTest {

    private lateinit var engine: CosLifecycleEngine

    @Before
    fun setup() {
        engine = DefaultCosLifecycleEngine(DefaultTimeProvider())
    }

    @Test
    fun `initial state contains single seed cell`() = runBlocking {
        val state = engine.state.first()
        assertEquals(1, state.cells.size)
        assertEquals(CellPhase.Seed, state.cells.first().phase)
    }
}

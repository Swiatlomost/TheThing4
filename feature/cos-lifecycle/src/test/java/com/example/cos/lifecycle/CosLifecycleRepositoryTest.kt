package com.example.cos.lifecycle

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CosLifecycleRepositoryTest {

    private lateinit var repository: CosLifecycleRepository

    @Before
    fun setup() {
        repository = DefaultCosLifecycleRepository()
    }

    @Test
    fun `initial state contains single seed cell`() {
        val state = repository.initialState()
        assertEquals(1, state.cells.size)
        assertEquals(CellPhase.Seed, state.cells.first().phase)
    }
}

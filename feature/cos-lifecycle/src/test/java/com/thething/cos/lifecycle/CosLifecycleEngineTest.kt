package com.thething.cos.lifecycle

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.hypot

class CosLifecycleEngineTest {

    private lateinit var engine: DefaultCosLifecycleEngine

    @Before
    fun setup() {
        engine = DefaultCosLifecycleEngine()
    }

    @Test
    fun `initial state contains single seed cell`() = runBlocking {
        val state = engine.state.first()
        assertEquals(1, state.cells.size)
        assertTrue(state.cells.first().stage is CellStage.Seed)
    }

    @Test
    fun `set stage bud transitions last cell`() = runBlocking {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        val stage = engine.state.first().cells.last().stage
        assertTrue(stage is CellStage.Bud)
    }

    @Test
    fun `set stage mature requires bud first`() = runBlocking {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        val stage = engine.state.first().cells.last().stage
        assertTrue(stage is CellStage.Mature)
    }

    @Test
    fun `reset restores single seed`() = runBlocking {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        engine.apply(LifecycleAction.CreateChild)
        engine.apply(LifecycleAction.Reset)
        val state = engine.state.first()
        assertEquals(1, state.cells.size)
        assertTrue(state.cells.first().stage is CellStage.Seed)
    }

    @Test
    fun `create child adds new seed and marks parent spawned`() = runBlocking {
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        val before = engine.state.first()

        engine.apply(LifecycleAction.CreateChild)

        val after = engine.state.first()
        assertEquals(before.cells.size + 1, after.cells.size)
        val parent = after.cells[before.cells.lastIndex]
        val child = after.cells.last()
        assertTrue(parent.stage is CellStage.Spawned)
        assertTrue(child.stage is CellStage.Seed)
        assertEquals(parent.id, child.parentId)
        assertNotEquals(parent.center, child.center)
    }

    @Test
    fun `create child ignored when last cell not mature`() = runBlocking {
        val before = engine.state.first()
        engine.apply(LifecycleAction.CreateChild)
        val after = engine.state.first()
        assertEquals(before, after)
    }

    @Test
    fun `first ring cells share the same radial distance`() {
        engine.growToCells(7)
        val state = engine.state.value
        val cells = state.cells
        assertEquals(7, cells.size)
        val baseRadius = state.cellRadius
        assertTrue(baseRadius > 0f)

        val distances = cells.drop(1).map { it.center.magnitude() }
        val reference = distances.first()
        distances.forEach { distance ->
            assertEquals(reference.toDouble(), distance.toDouble(), 1e-3)
        }
        assertEquals(2f * baseRadius.toDouble(), reference.toDouble(), 1e-3)
    }

    @Test
    fun `partial outer ring advances along hex perimeter`() {
        engine.growToCells(8)
        val state = engine.state.value
        val cells = state.cells
        assertEquals(8, cells.size)

        val innerRingDistance = cells[1].center.magnitude()
        val outerCandidate = cells.last().center

        assertTrue(outerCandidate.magnitude() > innerRingDistance + 1e-3)
        assertEquals(0f, outerCandidate.y, 1e-3f)
        assertTrue(outerCandidate.x > 0f)
        assertEquals(2f * state.cellRadius.toDouble(), innerRingDistance.toDouble(), 1e-3)
    }
}

private fun DefaultCosLifecycleEngine.growToCells(target: Int) {
    while (state.value.cells.size < target) {
        apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        apply(LifecycleAction.CreateChild)
    }
}

private fun Offset.magnitude(): Float = hypot(x.toDouble(), y.toDouble()).toFloat()

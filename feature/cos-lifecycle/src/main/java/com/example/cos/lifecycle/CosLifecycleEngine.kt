package com.example.cos.lifecycle

import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class CellStage(val label: String) {
    object Seed : CellStage("Seed")
    object Bud : CellStage("Bud")
    object Mature : CellStage("Mature")
    object Spawned : CellStage("Spawned")
}

data class CellSnapshot(
    val id: String,
    val stage: CellStage,
    val center: Offset = Offset.Zero,
    val radius: Float = 0f,
    val parentId: String? = null
)

data class CosLifecycleState(
    val cells: List<CellSnapshot>,
    val cellRadius: Float
)

enum class LifecycleStageCommand {
    SEED,
    BUD,
    MATURE
}

sealed interface LifecycleAction {
    object Reset : LifecycleAction
    data class SetStage(val stage: LifecycleStageCommand) : LifecycleAction
    object CreateChild : LifecycleAction
}

interface CosLifecycleEngine {
    val state: StateFlow<CosLifecycleState>
    fun apply(action: LifecycleAction)
}

private data class HexCoord(val q: Int, val r: Int)

@Singleton
class DefaultCosLifecycleEngine @Inject constructor() : CosLifecycleEngine {

    private val _state = MutableStateFlow(initialState())
    override val state: StateFlow<CosLifecycleState> = _state.asStateFlow()

    override fun apply(action: LifecycleAction) {
        _state.update { current ->
            when (action) {
                LifecycleAction.Reset -> initialState()
                is LifecycleAction.SetStage -> setStage(current, action.stage)
                LifecycleAction.CreateChild -> createChild(current)
            }
        }
    }

    private fun setStage(
        state: CosLifecycleState,
        stage: LifecycleStageCommand
    ): CosLifecycleState {
        val cells = state.cells
        if (cells.isEmpty()) return state

        val lastIndex = cells.lastIndex
        val lastCell = cells[lastIndex]
        val updatedCell = when (stage) {
            LifecycleStageCommand.SEED -> return initialState()
            LifecycleStageCommand.BUD -> {
                if (lastCell.stage != CellStage.Seed) return state
                lastCell.copy(stage = CellStage.Bud)
            }
            LifecycleStageCommand.MATURE -> {
                if (lastCell.stage != CellStage.Bud) return state
                lastCell.copy(stage = CellStage.Mature)
            }
        }

        val updated = cells.toMutableList().apply { this[lastIndex] = updatedCell }
        log("SetStage() -> ")
        return layoutState(updated)
    }

    private fun createChild(state: CosLifecycleState): CosLifecycleState {
        val cells = state.cells
        if (cells.isEmpty()) return state

        val parentIndex = cells.lastIndex
        val parent = cells[parentIndex]
        if (parent.stage != CellStage.Mature) {
            log("CreateChild ignored (parent stage=)")
            return state
        }

        val newParent = parent.copy(stage = CellStage.Spawned)
        val child = CellSnapshot(
            id = UUID.randomUUID().toString(),
            stage = CellStage.Seed,
            parentId = parent.id
        )

        val updated = cells.toMutableList().apply {
            this[parentIndex] = newParent
            add(child)
        }
        log("CreateChild parent= child=")
        return layoutState(updated)
    }

    private fun initialState(): CosLifecycleState = layoutState(
        listOf(
            CellSnapshot(
                id = ROOT_CELL_ID,
                stage = CellStage.Seed,
                parentId = null
            )
        )
    )

    private fun layoutState(cells: List<CellSnapshot>): CosLifecycleState {
        val count = cells.size
        if (count == 0) return CosLifecycleState(emptyList(), 0f)
        val (radius, centers) = packCircles(count, ORGANISM_RADIUS_UNITS)
        val arranged = cells.mapIndexed { index, cell ->
            val center = centers.getOrNull(index) ?: Offset.Zero
            cell.copy(center = center, radius = radius)
        }
        return CosLifecycleState(
            cells = arranged,
            cellRadius = radius
        )
    }

    private fun packCircles(count: Int, outerRadius: Float): Pair<Float, List<Offset>> {
        require(count >= 0)
        if (count == 0) return 0f to emptyList()

        var ring = 0
        while (1 + 3 * ring * (ring + 1) < count) {
            ring++
        }

        val radius = outerRadius / (2 * ring + 1)
        if (count == 1) return radius to listOf(Offset.Zero)

        val centers = ArrayList<Offset>(count)
        centers += Offset.Zero

        var remaining = count - 1
        var currentRing = 1
        while (remaining > 0) {
            val ringCenters = generateRing(currentRing, radius)
            val take = minOf(remaining, ringCenters.size)
            for (index in 0 until take) {
                centers += ringCenters[index]
            }
            remaining -= take
            currentRing++
        }

        return radius to centers
    }

    private fun generateRing(ringIndex: Int, radius: Float): List<Offset> {
        if (ringIndex <= 0) return listOf(Offset.Zero)

        val axialPoints = ArrayList<HexCoord>(ringIndex * 6)
        var q = ringIndex
        var r = 0
        for (direction in HEX_DIRECTIONS) {
            repeat(ringIndex) {
                axialPoints += HexCoord(q, r)
                q += direction.q
                r += direction.r
            }
        }

        return axialPoints.map { coord ->
            val x = (2f * radius * coord.q) + (radius * coord.r)
            val y = SQRT_THREE * radius * coord.r
            Offset(x, y)
        }
    }

    private fun log(message: String) {
        try {
            Log.i(TAG, message)
        } catch (_: RuntimeException) {
            // ignore during local tests
        }
    }

    companion object {
        private val HEX_DIRECTIONS = arrayOf(
            HexCoord(-1, 1),
            HexCoord(-1, 0),
            HexCoord(0, -1),
            HexCoord(1, -1),
            HexCoord(1, 0),
            HexCoord(0, 1)
        )
        private const val SQRT_THREE = 1.7320508f
        private const val ROOT_CELL_ID = "root"
        private const val ORGANISM_RADIUS_UNITS = 4f
        private const val TAG = "CosLifecycleEngine"
    }
}

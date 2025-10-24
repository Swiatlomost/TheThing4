package com.example.cos.lifecycle

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

sealed class CellStage(val progress: Float) {
    class Seed(progress: Float) : CellStage(progress)
    class Bud(progress: Float) : CellStage(progress)
    object Mature : CellStage(1f)
}

data class CellSnapshot(
    val id: String,
    val stage: CellStage,
    val elapsedMillis: Long,
    val center: Offset,
    val parentId: String? = null
)

data class CosLifecycleState(val cells: List<CellSnapshot>)

interface CosLifecycleEngine {
    val state: StateFlow<CosLifecycleState>
    fun pause()
    fun resume()
}

@Singleton
class DefaultCosLifecycleEngine @Inject constructor(
    private val timeProvider: TimeProvider
) : CosLifecycleEngine {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow(initialState())
    override val state: StateFlow<CosLifecycleState> = _state

    private var tickJob: Job? = null

    init { resume() }

    override fun pause() {
        tickJob?.cancel()
        tickJob = null
    }

    override fun resume() {
        if (tickJob?.isActive == true) return
        tickJob = scope.launch {
            while (true) {
                delay(timeProvider.tickMillis())
                _state.value = advanceState(_state.value)
            }
        }
    }

    private fun initialState(): CosLifecycleState = CosLifecycleState(
        cells = listOf(
            CellSnapshot(
                id = ROOT_CELL_ID,
                stage = CellStage.Seed(progress = 0f),
                elapsedMillis = 0L,
                center = Offset.Zero,
                parentId = null
            )
        )
    )

    private fun advanceState(state: CosLifecycleState): CosLifecycleState {
        val evolved = state.cells.map { cell ->
            val nextElapsed = cell.elapsedMillis + TICK_MS
            val nextStage = when (cell.stage) {
                is CellStage.Seed -> {
                    val progress = (nextElapsed / SEED_DURATION).coerceIn(0f, 1f)
                    if (progress >= 1f) CellStage.Bud(0f) else CellStage.Seed(progress)
                }
                is CellStage.Bud -> {
                    val progress = ((nextElapsed - SEED_DURATION) / BUD_DURATION).coerceIn(0f, 1f)
                    if (progress >= 1f) CellStage.Mature else CellStage.Bud(progress)
                }
                CellStage.Mature -> CellStage.Mature
            }
            cell.copy(stage = nextStage, elapsedMillis = nextElapsed)
        }.toMutableList()

        val matureCells = evolved.filter { it.stage is CellStage.Mature }
        if (matureCells.size < MAX_CELLS && matureCells.isNotEmpty()) {
            val parent = matureCells.first()
            val hasChild = evolved.any { it.parentId == parent.id }
            if (!hasChild) {
                val existingChildren = state.cells.count { it.parentId == parent.id }
                val direction = CONTACT_DIRECTIONS[existingChildren % CONTACT_DIRECTIONS.size]
                val newCenter = Offset(
                    parent.center.x + direction.x * CELL_OFFSET,
                    parent.center.y + direction.y * CELL_OFFSET
                )
                evolved.add(
                    CellSnapshot(
                        id = UUID.randomUUID().toString(),
                        stage = CellStage.Seed(0f),
                        elapsedMillis = 0L,
                        center = newCenter,
                        parentId = parent.id
                    )
                )
            }
        }
        return CosLifecycleState(evolved)
    }

    companion object {
        private const val ROOT_CELL_ID = "root"
        private const val TICK_MS = 1_000L
        private const val SEED_DURATION = 10_000f
        private const val BUD_DURATION = 10_000f
        private const val CELL_OFFSET = 2f
        private const val MAX_CELLS = 8

        private val CONTACT_DIRECTIONS = List(8) { index ->
            val angle = (index / 8f) * 2f * PI.toFloat()
            Offset(cos(angle), sin(angle))
        }
    }
}

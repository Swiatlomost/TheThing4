package com.example.cos.lifecycle

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

enum class CellPhase { Seed, Bud, Mature }

data class CellSnapshot(
    val id: String,
    val phase: CellPhase,
    val contacts: Set<String>,
    val elapsedMillis: Long
)

data class CosLifecycleState(
    val cells: List<CellSnapshot>
)

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

    init {
        resume()
    }

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
                phase = CellPhase.Seed,
                contacts = emptySet(),
                elapsedMillis = 0L
            )
        )
    )

    private fun advanceState(state: CosLifecycleState): CosLifecycleState {
        val evolved = state.cells.map { cell ->
            val nextElapsed = cell.elapsedMillis + TICK_MS
            val nextPhase = when {
                cell.phase == CellPhase.Seed && nextElapsed >= SEED_DURATION -> CellPhase.Bud
                cell.phase == CellPhase.Bud && nextElapsed >= BUD_DURATION -> CellPhase.Mature
                else -> cell.phase
            }
            cell.copy(phase = nextPhase, elapsedMillis = nextElapsed)
        }.toMutableList()

        val matureCells = evolved.filter { it.phase == CellPhase.Mature }
        val hasDetached = evolved.any { snapshot ->
            snapshot.contacts.isNotEmpty() && snapshot.contacts.none { contactId ->
                evolved.any { it.id == contactId }
            }
        }
        if (hasDetached) {
            // remove detached cells to preserve contact rule
            evolved.removeIf { snapshot ->
                snapshot.contacts.isNotEmpty() && snapshot.contacts.none { contactId ->
                    evolved.any { it.id == contactId }
                }
            }
        }
        if (matureCells.isNotEmpty()) {
            val parent = matureCells.random()
            val alreadySpawned = evolved.any { parent.id in it.contacts }
            if (!alreadySpawned) {
                evolved.add(
                    CellSnapshot(
                        id = UUID.randomUUID().toString(),
                        phase = CellPhase.Seed,
                        contacts = setOf(parent.id),
                        elapsedMillis = 0L
                    )
                )
            }
        }
        return CosLifecycleState(evolved)
    }

    companion object {
        private const val ROOT_CELL_ID = "root"
        private const val TICK_MS = 1_000L
        private const val SEED_DURATION = 10_000L
        private const val BUD_DURATION = 20_000L
    }
}

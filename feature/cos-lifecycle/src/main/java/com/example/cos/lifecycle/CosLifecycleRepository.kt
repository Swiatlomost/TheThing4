package com.example.cos.lifecycle

import kotlin.random.Random

enum class CellPhase { Seed, Bud, Mature }

data class CellSnapshot(
    val id: String,
    val phase: CellPhase,
    val contacts: Set<String>,
    val elapsedMillis: Long
)

data class CosLifecycleState(val cells: List<CellSnapshot>)

interface CosLifecycleRepository {
    fun initialState(): CosLifecycleState
    fun advanceState(state: CosLifecycleState): CosLifecycleState
}

class DefaultCosLifecycleRepository @javax.inject.Inject constructor() : CosLifecycleRepository {

    override fun initialState(): CosLifecycleState = CosLifecycleState(
        cells = listOf(CellSnapshot("root", CellPhase.Seed, emptySet(), 0L))
    )

    override fun advanceState(state: CosLifecycleState): CosLifecycleState {
        val updated = state.cells.map { cell ->
            val nextElapsed = cell.elapsedMillis + TICK_MS
            val nextPhase = when {
                cell.phase == CellPhase.Seed && nextElapsed >= SEED_DURATION -> CellPhase.Bud
                cell.phase == CellPhase.Bud && nextElapsed >= BUD_DURATION -> CellPhase.Mature
                else -> cell.phase
            }
            cell.copy(phase = nextPhase, elapsedMillis = nextElapsed)
        }.toMutableList()

        val matureCells = updated.filter { it.phase == CellPhase.Mature }
        if (matureCells.isNotEmpty()) {
            val parent = matureCells.random()
            if (updated.none { parent.id in it.contacts }) {
                val newId = "cell_${Random.nextInt(1_000)}"
                updated.add(
                    CellSnapshot(
                        id = newId,
                        phase = CellPhase.Seed,
                        contacts = setOf(parent.id),
                        elapsedMillis = 0L
                    )
                )
            }
        }
        return CosLifecycleState(updated)
    }

    companion object {
        private const val TICK_MS = 1_000L
        private const val SEED_DURATION = 10_000L
        private const val BUD_DURATION = 20_000L
    }
}

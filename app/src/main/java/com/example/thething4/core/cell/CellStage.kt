package com.example.thething4.core.cell

sealed interface CellStage {
    val progress: Float

    data class Seed(override val progress: Float) : CellStage
    data class Bud(override val progress: Float) : CellStage
    data class Mature(override val progress: Float = 1f) : CellStage
}

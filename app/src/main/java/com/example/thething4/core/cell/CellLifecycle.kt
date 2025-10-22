package com.example.thething4.core.cell

import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@JvmInline
value class CellId(val value: String) {
    companion object {
        val Root: CellId = CellId("root")
        fun random(): CellId = CellId(UUID.randomUUID().toString())
    }
}

data class CellLifecycle(
    val id: CellId,
    val createdAt: Duration,
    val warmupDuration: Duration = 10.seconds,
    val saturationDuration: Duration = 15.seconds
) {
    companion object {
        fun default(createdAt: Duration): CellLifecycle =
            CellLifecycle(
                id = CellId.Root,
                createdAt = createdAt
            )
    }
}

data class CellSnapshot(
    val lifecycle: CellLifecycle,
    val stage: CellStage
)

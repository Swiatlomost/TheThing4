package com.example.thething4.observation

import androidx.compose.ui.geometry.Offset
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.cell.CellStage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

data class ObservationUiState(
    val organism: OrganismSnapshot = OrganismSnapshot(),
    val readiness: GatingReadiness = GatingReadiness(),
    val timeline: SaturationTimeline = SaturationTimeline(),
    val hints: ObservationHints = ObservationHints()
)

data class OrganismSnapshot(
    val cells: List<OrganismCellSnapshot> = emptyList(),
    val transform: OrganismTransform = OrganismTransform.Identity
)

data class OrganismCellSnapshot(
    val snapshot: CellSnapshot,
    /** Normalised coordinate relative to organism centre (-1..1 on both axes). */
    val layoutPosition: Offset
)

data class OrganismTransform(
    val position: Offset = Offset.Zero,
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f
) {
    fun translate(delta: Offset): OrganismTransform =
        copy(position = position + delta)

    companion object {
        val Identity: OrganismTransform = OrganismTransform()
    }
}

data class GatingReadiness(
    val status: GatingStatus = GatingStatus.NotReady,
    val matureCount: Int = 0,
    val totalCount: Int = 0,
    val message: String? = null
) {
    val isReady: Boolean get() = status == GatingStatus.Ready
}

enum class GatingStatus {
    Ready,
    NotReady,
    Cooldown
}

data class SaturationTimeline(
    val entries: List<TimelineEntry> = emptyList()
) {
    companion object {
        val Empty = SaturationTimeline()
    }
}

data class TimelineEntry(
    val timestamp: Duration,
    val stage: CellStage,
    val label: String
)

data class ObservationHints(
    val showDragHint: Boolean = true,
    val showBuddingHint: Boolean = true
)

typealias DragDelta = Offset

data class ObservationGestures(
    val onOrganismDrag: (DragDelta) -> Unit,
    val onGestureEnd: () -> Unit,
    val onTapCell: (cellId: String) -> Unit = {},
    val onPinchScale: (Float) -> Unit = {}
)

fun timelineForSnapshot(now: Duration, snapshots: List<CellSnapshot>): SaturationTimeline {
    if (snapshots.isEmpty()) return SaturationTimeline.Empty

    val entries = snapshots.map { snapshot ->
        TimelineEntry(
            timestamp = now - snapshot.lifecycle.createdAt,
            stage = snapshot.stage,
            label = when (val stage = snapshot.stage) {
                is CellStage.Seed -> "Seed ${(stage.progress * 100).toInt()}%"
                is CellStage.Bud -> "Bud ${(stage.progress * 100).toInt()}%"
                is CellStage.Mature -> "Mature"
            }
        )
    }
    return SaturationTimeline(entries)
}

fun computeHints(readiness: GatingReadiness): ObservationHints =
    ObservationHints(
        showDragHint = true,
        showBuddingHint = !readiness.isReady
    )

fun Duration?.elapsedOrZero(reference: Duration): Duration =
    if (this == null) ZERO else (this - reference).coerceAtLeast(ZERO)

fun radialLayout(count: Int): List<Offset> {
    if (count <= 1) return listOf(Offset.Zero)

    val ringRadius = 0.65f
    val step = (2 * PI / count)
    return buildList {
        for (index in 0 until count) {
            val angle = index * step
            add(
                Offset(
                    x = (cos(angle) * ringRadius).toFloat(),
                    y = (sin(angle) * ringRadius).toFloat()
                )
            )
        }
    }
}

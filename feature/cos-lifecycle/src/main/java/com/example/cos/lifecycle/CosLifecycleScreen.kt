package com.example.cos.lifecycle

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.lerp
import kotlin.math.max
import kotlin.math.min

@Composable
fun CosLifecycleScreen(
    state: CosLifecycleState,
    onToggleOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    CosLifecycleCanvas(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onToggleOverlay) {
                detectTapGestures(onDoubleTap = { onToggleOverlay() })
            }
    )
}

@Composable
fun CosLifecycleCanvas(
    state: CosLifecycleState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        drawCells(
            cells = state.cells,
            pulseScale = pulseScale,
            primaryColor = primaryColor
        )
    }
}

private fun DrawScope.drawCells(
    cells: List<CellSnapshot>,
    pulseScale: Float,
    primaryColor: Color
) {
    if (cells.isEmpty()) return

    val paddingUnits = 1.25f
    val minX = cells.minOf { it.center.x } - (MATURITY_RADIUS_UNITS + paddingUnits)
    val maxX = cells.maxOf { it.center.x } + (MATURITY_RADIUS_UNITS + paddingUnits)
    val minY = cells.minOf { it.center.y } - (MATURITY_RADIUS_UNITS + paddingUnits)
    val maxY = cells.maxOf { it.center.y } + (MATURITY_RADIUS_UNITS + paddingUnits)

    val unitWidth = max(maxX - minX, MIN_BOUND_UNITS)
    val unitHeight = max(maxY - minY, MIN_BOUND_UNITS)

    val scale = min(size.width / unitWidth, size.height / unitHeight)
    val offsetX = (size.width - unitWidth * scale) / 2f
    val offsetY = (size.height - unitHeight * scale) / 2f

    val renderedCells = cells.map { cell ->
        val stage = cell.stage
        val stageProgress = when (stage) {
            is CellStage.Seed -> stage.progress.coerceIn(0f, 1f)
            is CellStage.Bud -> stage.progress.coerceIn(0f, 1f)
            CellStage.Mature -> 1f
        }
        val pulse = if (stage is CellStage.Seed) 1f else pulseScale
        val baseCenter = Offset(
            x = offsetX + (cell.center.x - minX) * scale,
            y = offsetY + (cell.center.y - minY) * scale
        )
        val outerRadiusPx = when (stage) {
            is CellStage.Seed -> seedRadiusPx(scale, stageProgress)
            is CellStage.Bud -> matureRadiusPx(scale, pulse)
            CellStage.Mature -> matureRadiusPx(scale, pulse)
        }
        val contactRadiusPx = when (stage) {
            is CellStage.Seed -> outerRadiusPx
            is CellStage.Bud -> matureRadiusPx(scale, pulse = 1f)
            CellStage.Mature -> matureRadiusPx(scale, pulse = 1f)
        }
        RenderedCell(
            cell = cell,
            stage = stage,
            stageProgress = stageProgress,
            center = baseCenter,
            outerRadiusPx = outerRadiusPx,
            contactRadiusPx = contactRadiusPx
        )
    }

    val renderedById = renderedCells.associateBy { it.cell.id }
    val adjustedCells = renderedCells.map { renderedCell ->
        if (renderedCell.stage is CellStage.Seed && renderedCell.cell.parentId != null) {
            val parent = renderedById[renderedCell.cell.parentId]
            if (parent != null) {
                val direction = renderedCell.center - parent.center
                val distance = direction.getDistance()
                val desiredDistance = parent.contactRadiusPx + renderedCell.contactRadiusPx
                if (distance > desiredDistance + CONTACT_EPSILON_PX && distance > 0f) {
                    val shift = distance - desiredDistance
                    val normalized = direction / distance
                    val adjustedCenter = renderedCell.center - normalized * shift
                    renderedCell.copy(center = adjustedCenter)
                } else if (distance + CONTACT_EPSILON_PX < desiredDistance && distance > 0f) {
                    val shortfall = desiredDistance - distance
                    val normalized = direction / distance
                    val adjustedCenter = renderedCell.center + normalized * shortfall
                    renderedCell.copy(center = adjustedCenter)
                } else {
                    renderedCell
                }
            } else {
                renderedCell
            }
        } else {
            renderedCell
        }
    }

    adjustedCells.forEach { renderedCell ->
        drawCell(
            renderedCell = renderedCell,
            primaryColor = primaryColor,
            scale = scale
        )
    }
}

private fun DrawScope.drawCell(
    renderedCell: RenderedCell,
    primaryColor: Color,
    scale: Float
) {
    val stage = renderedCell.stage
    val center = renderedCell.center
    val stageProgress = renderedCell.stageProgress
    when (stage) {
        is CellStage.Seed -> {
            drawCircle(
                color = primaryColor,
                radius = renderedCell.outerRadiusPx,
                center = center
            )
        }
        is CellStage.Bud -> {
            val outlineRadius = renderedCell.outerRadiusPx
            val strokeWidth = scale * 0.18f
            val fillRadius = lerp(
                start = outlineRadius * 0.25f,
                stop = outlineRadius * 0.95f,
                fraction = stageProgress
            )
            val fillAlpha = lerp(0.18f, 0.85f, stageProgress)

            drawCircle(
                color = primaryColor.copy(alpha = fillAlpha),
                radius = fillRadius,
                center = center
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.65f),
                radius = outlineRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }
        CellStage.Mature -> {
            drawCircle(
                color = primaryColor,
                radius = renderedCell.outerRadiusPx,
                center = center
            )
        }
    }
}

private fun seedRadiusPx(scale: Float, stageProgress: Float): Float {
    val clamped = stageProgress.coerceIn(0f, 1f)
    val matureRadius = matureRadiusPx(scale, pulse = 1f)
    return lerp(
        start = matureRadius * 0.18f,
        stop = matureRadius * 0.45f,
        fraction = clamped
    )
}

private fun matureRadiusPx(scale: Float, pulse: Float): Float =
    scale * MATURITY_RADIUS_UNITS * pulse

private data class RenderedCell(
    val cell: CellSnapshot,
    val stage: CellStage,
    val stageProgress: Float,
    val center: Offset,
    val outerRadiusPx: Float,
    val contactRadiusPx: Float
)

private const val MATURITY_RADIUS_UNITS = 1f
private const val MIN_BOUND_UNITS = 4f
private const val CONTACT_EPSILON_PX = 0.5f

package com.example.cos.lifecycle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.min
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CosLifecycleScreen(
    state: CosLifecycleState,
    onToggleOverlay: () -> Unit,
    onReset: () -> Unit,
    onSetStage: (LifecycleStageCommand) -> Unit,
    onCreateChild: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastStage = state.cells.lastOrNull()?.stage
    val canNarodziny = lastStage is CellStage.Seed
    val canDojrzewanie = lastStage is CellStage.Bud
    val canSpawn = lastStage is CellStage.Mature

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(onToggleOverlay) {
                    detectTapGestures(onDoubleTap = { onToggleOverlay() })
                }
        ) {
            CosLifecycleCanvas(
                state = state,
                modifier = Modifier.fillMaxSize()
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onReset) {
                Text(text = "Reset")
            }
            Button(
                onClick = { onSetStage(LifecycleStageCommand.BUD) },
                enabled = canNarodziny
            ) {
                Text(text = "Narodziny")
            }
            Button(
                onClick = { onSetStage(LifecycleStageCommand.MATURE) },
                enabled = canDojrzewanie
            ) {
                Text(text = "Dojrzewanie")
            }
            Button(
                onClick = onCreateChild,
                enabled = canSpawn
            ) {
                Text(text = "Nowa komórka")
            }
        }
    }
}

@Composable
fun CosLifecycleCanvas(
    state: CosLifecycleState,
    modifier: Modifier = Modifier
) {
    val baseRadiusUnits = state.cellRadius.takeIf { it > 0f }
        ?: computeBaseRadiusUnits(state.cells.size.coerceAtLeast(1))
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.onSurface
    val animatedCells = state.cells.map { cell ->
        val stageValue by animateFloatAsState(
            targetValue = when (cell.stage) {
                CellStage.Seed -> 0f
                CellStage.Bud -> 1f
                CellStage.Mature -> 2f
                CellStage.Spawned -> 3f
            },
            animationSpec = tween(durationMillis = 400),
            label = "stage-${cell.id}"
        )
        AnimatedCell(
            snapshot = cell,
            stageValue = stageValue
        )
    }

    Canvas(modifier = modifier) {
        drawOrganism(
            cells = animatedCells,
            baseRadiusUnits = baseRadiusUnits,
            containerRadiusUnits = ORGANISM_RADIUS_UNITS,
            primaryColor = primaryColor,
            accentColor = accentColor
        )
    }
}

private fun DrawScope.drawOrganism(
    cells: List<AnimatedCell>,
    baseRadiusUnits: Float,
    containerRadiusUnits: Float,
    primaryColor: Color,
    accentColor: Color
) {
    if (cells.isEmpty()) return

    val scale = (min(size.width, size.height) / 2f) / containerRadiusUnits
    val origin = Offset(size.width / 2f, size.height / 2f)

    cells.forEach { animated ->
        val centerPx = origin + animated.snapshot.center * scale
        val outerRadiusUnits = baseRadiusUnits * outerRadiusMultiplier(animated.stageValue)
        val fillRadiusUnits = baseRadiusUnits * fillRadiusMultiplier(animated.stageValue)
        val outlineAlpha = outlineAlpha(animated.stageValue)
        val fillAlpha = fillAlpha(animated.stageValue)

        val outerRadiusPx = outerRadiusUnits * scale
        val fillRadiusPx = fillRadiusUnits * scale

        if (fillRadiusPx > 0f) {
            drawCircle(
                color = primaryColor.copy(alpha = fillAlpha),
                radius = fillRadiusPx,
                center = centerPx
            )
        }

        val outlineWidth = (baseRadiusUnits * 0.2f) * scale
        if (outlineAlpha > 0f && outerRadiusPx > 0f) {
            drawCircle(
                color = accentColor.copy(alpha = outlineAlpha),
                radius = outerRadiusPx,
                center = centerPx,
                style = Stroke(width = outlineWidth)
            )
        }
    }
}

private fun computeBaseRadiusUnits(count: Int): Float {
    val c = count.coerceAtLeast(1)
    val shrink = sqrt((c - 1).coerceAtLeast(0).toFloat())
    return ORGANISM_RADIUS_UNITS / (1f + shrink)
}

private fun outerRadiusMultiplier(stageValue: Float): Float = when {
    stageValue <= 1f -> lerp(0.45f, 0.75f, stageValue.coerceIn(0f, 1f))
    stageValue <= 2f -> lerp(0.75f, 1f, (stageValue - 1f).coerceIn(0f, 1f))
    else -> 1f
}

private fun fillRadiusMultiplier(stageValue: Float): Float = when {
    stageValue <= 0f -> 0.3f
    stageValue <= 1f -> lerp(0.3f, 0.6f, stageValue.coerceIn(0f, 1f))
    stageValue <= 2f -> lerp(0.6f, 1f, (stageValue - 1f).coerceIn(0f, 1f))
    else -> 0.85f
}

private fun fillAlpha(stageValue: Float): Float = when {
    stageValue <= 1f -> 1f
    stageValue <= 2f -> lerp(0.85f, 1f, (stageValue - 1f).coerceIn(0f, 1f))
    else -> 0.6f
}

private fun outlineAlpha(stageValue: Float): Float = when {
    stageValue <= 0f -> 0f
    stageValue <= 1f -> lerp(0.6f, 0.85f, stageValue.coerceIn(0f, 1f))
    stageValue <= 2f -> lerp(0.85f, 0.45f, (stageValue - 1f).coerceIn(0f, 1f))
    else -> 0.3f
}

private data class AnimatedCell(
    val snapshot: CellSnapshot,
    val stageValue: Float
)

private const val ORGANISM_RADIUS_UNITS = 4f

package com.example.cos.lifecycle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun CosLifecycleScreen(
    state: CosLifecycleState,
    onToggleOverlay: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onToggleOverlay() })
            }
    ) {
        drawCells(state)
    }
}

@Composable
fun CosLifecycleCanvas(
    state: CosLifecycleState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCells(state)
    }
}

private fun DrawScope.drawCells(state: CosLifecycleState) {
    val cells = state.cells
    if (cells.isEmpty()) return
    val maxRadius = size.minDimension / (cells.size + 2)
    val centerY = center.y
    cells.forEachIndexed { index, cell ->
        val circleCenter = Offset(maxRadius * (index + 1), centerY)
        when (cell.phase) {
            CellPhase.Seed -> drawCircle(color = Color.Magenta, radius = maxRadius / 4f, center = circleCenter)
            CellPhase.Bud -> drawCircle(
                color = Color.Cyan,
                radius = maxRadius / 2f,
                center = circleCenter,
                style = Stroke(width = (maxRadius / 8f).coerceAtLeast(2f))
            )
            CellPhase.Mature -> drawCircle(color = Color.Green, radius = maxRadius / 2f, center = circleCenter)
        }
    }
}

package com.example.cos.lifecycle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun CosLifecycleScreen(state: State<CosLifecycleState>) {
    val cells = state.value.cells
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (cells.isEmpty()) return@Canvas
        val radius = size.minDimension / (cells.size + 2)
        cells.forEachIndexed { index, cell ->
            val color = when (cell.phase) {
                CellPhase.Seed -> Color.Magenta
                CellPhase.Bud -> Color.Cyan
                CellPhase.Mature -> Color.Green
            }
            drawCircle(
                color = color,
                radius = radius / 2f,
                center = Offset(radius * (index + 1), center.y)
            )
        }
    }
}

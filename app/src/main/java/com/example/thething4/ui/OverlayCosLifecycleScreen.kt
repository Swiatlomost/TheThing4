package com.example.thething4.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.thething4.core.cell.CellStage
import kotlinx.coroutines.flow.StateFlow

@Composable
fun OverlayCosLifecycleScreen(
    stageFlow: StateFlow<CellStage?>,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stage by stageFlow.collectAsState()

    Box(
        modifier = modifier
            .size(140.dp)
            .semantics { contentDescription = "Coś – tryb pływający" }
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() }
                )
            }
    ) {
        LifecycleCanvas(stage = stage, modifier = Modifier.matchParentSize())
    }
}




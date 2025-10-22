package com.example.thething4.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.thething4.core.cell.CellStage

@Composable
fun CosLifecycleScreen(
    uiState: CellUiState,
    modifier: Modifier = Modifier
) {
    val snapshot = uiState.cells.firstOrNull()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val stageLabel = snapshot?.stage?.readableName ?: "Starting..."

        LifecycleCanvas(
            stage = snapshot?.stage,
            modifier = Modifier.weight(1f, fill = true)
        )

        Text(
            text = stageLabel,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 32.dp)
        )
    }
}

@Composable
private fun LifecycleCanvas(stage: CellStage?, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseRadius = size.minDimension * 0.25f * pulseScale
        val primary = colorScheme.primary

        when (stage) {
            is CellStage.Seed -> {
                val seedRadius = baseRadius * 0.18f
                drawCircle(
                    color = primary,
                    radius = seedRadius
                )
            }

            is CellStage.Bud -> {
                // Outline stays constant
                drawCircle(
                    color = primary.copy(alpha = 0.65f),
                    radius = baseRadius,
                    style = Stroke(width = 6.dp.toPx())
                )

                // Fill grows from the center, gaining opacity with time
                val fillRadius = baseRadius * (0.25f + 0.75f * stage.progress.coerceIn(0f, 1f))
                val fillAlpha = 0.15f + 0.75f * stage.progress.coerceIn(0f, 1f)
                drawCircle(
                    color = primary.copy(alpha = fillAlpha),
                    radius = fillRadius
                )
            }

            is CellStage.Mature -> {
                drawCircle(
                    color = primary,
                    radius = baseRadius
                )
            }

            null -> {
                drawCircle(
                    color = primary.copy(alpha = 0.1f),
                    radius = baseRadius * 0.15f
                )
            }
        }
    }
}

private val CellStage.readableName: String
    get() = when (this) {
        is CellStage.Seed -> "Narodziny (${(progress * 100).toInt()}%)"
        is CellStage.Bud -> "Dojrzewanie (${(progress * 100).toInt()}%)"
        is CellStage.Mature -> "Dojrzałość"
    }

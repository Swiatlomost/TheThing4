package com.example.thething4.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thething4.core.cell.CellId
import com.example.thething4.core.cell.CellLifecycle
import com.example.thething4.core.cell.CellSnapshot
import com.example.thething4.core.cell.CellStage
import com.example.thething4.observation.GatingReadiness
import com.example.thething4.observation.GatingStatus
import com.example.thething4.observation.ObservationGestures
import com.example.thething4.observation.ObservationHints
import com.example.thething4.observation.ObservationUiState
import com.example.thething4.observation.OrganismCellSnapshot
import com.example.thething4.observation.OrganismSnapshot
import com.example.thething4.observation.SaturationTimeline
import com.example.thething4.observation.TimelineEntry
import kotlin.math.abs
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationModeScreen(
    uiState: ObservationUiState,
    gestures: ObservationGestures,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onBudding: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Observation Mode") },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    ReadinessPill(
                        status = uiState.readiness.status,
                        message = uiState.readiness.message
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricsStrip(
                matureCount = uiState.readiness.matureCount,
                totalCount = uiState.readiness.totalCount
            )
            ObservationCanvas(
                uiState = uiState,
                gestures = gestures,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            SaturationTimelineRail(uiState)
            if (uiState.hints.showDragHint || uiState.hints.showBuddingHint) {
                ObservationHintsCard(hints = uiState.hints, isReady = uiState.readiness.isReady)
            }
            ActionRow(
                isReady = uiState.readiness.isReady,
                onBudding = onBudding,
                status = uiState.readiness.status
            )
        }
    }
}

@Composable
private fun MetricsStrip(
    matureCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssistChip(onClick = {}, label = { Text("Cells: $totalCount") })
        AssistChip(onClick = {}, label = { Text("Mature: $matureCount") })
        val readiness = if (matureCount > 0) "ACTIVE" else "WARMUP"
        AssistChip(onClick = {}, label = { Text("Readiness: $readiness") })
    }
}

@Composable
private fun ObservationCanvas(
    uiState: ObservationUiState,
    gestures: ObservationGestures,
    modifier: Modifier = Modifier
) {
    val organism = uiState.organism
    val accent = MaterialTheme.colorScheme.primary
    val orbitStroke = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    Canvas(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large)
            .semantics { contentDescription = "Observation canvas with ${organism.cells.size} cells" }
            .pointerInput(organism.cells.size) {
                detectDragGestures(
                    onDragStart = { gestures.onOrganismDrag(Offset.Zero) },
                    onDragEnd = { gestures.onGestureEnd() },
                    onDragCancel = { gestures.onGestureEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        gestures.onOrganismDrag(dragAmount)
                    }
                )
            }
    ) {
        if (organism.cells.isEmpty()) {
            drawObservationPlaceholder(placeholderColor)
            return@Canvas
        }

        val transform = organism.transform
        val center = Offset(size.width / 2f, size.height / 2f)
        val orbitRadius = size.minDimension * 0.35f
        val scale = transform.scale.coerceIn(0.5f, 2.5f)
        val baseRadius = (size.minDimension * 0.085f).coerceAtLeast(36f) * scale

        organism.cells.forEach { cell ->
            val layoutOffset = Offset(
                x = center.x + orbitRadius * cell.layoutPosition.x * scale,
                y = center.y + orbitRadius * cell.layoutPosition.y * scale
            )
            val translated = layoutOffset + transform.position
            drawCellStage(
                stage = cell.snapshot.stage,
                center = translated,
                radius = baseRadius,
                accent = accent
            )
        }

        drawCircle(
            color = orbitStroke,
            center = center + transform.position,
            radius = orbitRadius * scale,
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
        )
    }
}

@Composable
private fun SaturationTimelineRail(uiState: ObservationUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Saturation timeline",
            style = MaterialTheme.typography.titleMedium
        )
        if (uiState.timeline.entries.isEmpty()) {
            Text(
                text = "Awaiting samples...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            uiState.timeline.entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.label, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${entry.timestamp.inWholeSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ObservationHintsCard(hints: ObservationHints, isReady: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AlertDialogDefaults.containerColor.copy(alpha = 0.6f), shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hints.showDragHint) {
            Text(
                text = "Drag anywhere on the canvas to reposition the organism.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (hints.showBuddingHint && !isReady) {
            Text(
                text = "Keep observing until at least one cell is mature to enable budding.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionRow(
    isReady: Boolean,
    onBudding: () -> Unit,
    status: GatingStatus
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onBudding,
            enabled = isReady,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Trigger budding")
        }
        val statusText = when (status) {
            GatingStatus.Ready -> "Budding available"
            GatingStatus.NotReady -> "Awaiting maturity"
            GatingStatus.Cooldown -> "Cooling down"
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReadinessPill(status: GatingStatus, message: String?) {
    val color = when (status) {
        GatingStatus.Ready -> Color(0xFF2E7D32)
        GatingStatus.NotReady -> Color(0xFF6D4C41)
        GatingStatus.Cooldown -> Color(0xFFF9A825)
    }
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(color.copy(alpha = 0.15f), shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(
                text = status.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun DrawScope.drawObservationPlaceholder(color: Color) {
    val radius = size.minDimension * 0.2f
    val center = Offset(x = size.width / 2f, y = size.height / 2f)
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 4.dp.toPx()))
    drawCircle(color = color.copy(alpha = 0.35f), radius = radius * 0.3f, center = center)
}

private fun DrawScope.drawCellStage(
    stage: CellStage,
    center: Offset,
    radius: Float,
    accent: Color
) {
    when (stage) {
        is CellStage.Seed -> {
            drawCircle(color = accent.copy(alpha = 0.8f), radius = radius * 0.45f, center = center)
            drawCircle(
                color = accent.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(width = radius * 0.22f)
            )
        }
        is CellStage.Bud -> {
            val progress = stage.progress.coerceIn(0f, 1f)
            drawCircle(color = accent.copy(alpha = 0.18f), radius = radius * 1.6f, center = center)
            drawCircle(
                color = accent.copy(alpha = 0.55f),
                radius = radius,
                center = center,
                style = Stroke(width = radius * 0.28f)
            )
            drawCircle(
                color = accent.copy(alpha = 0.45f),
                radius = radius * (0.35f + 0.5f * progress),
                center = center
            )
        }
        is CellStage.Mature -> {
            drawCircle(color = accent, radius = radius, center = center)
            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = radius * 0.45f, center = center)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ObservationModePreview() {
    val lifecycle = CellLifecycle(id = CellId("preview"), createdAt = ZERO)
    val cells = listOf(
        OrganismCellSnapshot(CellSnapshot(lifecycle, CellStage.Seed(progress = 0.3f)), Offset.Zero),
        OrganismCellSnapshot(CellSnapshot(lifecycle, CellStage.Bud(progress = 0.6f)), Offset(0.5f, 0.0f)),
        OrganismCellSnapshot(CellSnapshot(lifecycle, CellStage.Mature()), Offset(-0.5f, 0.0f))
    )
    ObservationModeScreen(
        uiState = ObservationUiState(
            organism = OrganismSnapshot(cells = cells),
            readiness = GatingReadiness(status = GatingStatus.Ready, matureCount = 1, totalCount = 3),
            timeline = SaturationTimeline(entries = listOf(TimelineEntry(5.seconds, CellStage.Seed(0.2f), "Seed 20%")))
        ),
        gestures = ObservationGestures(onOrganismDrag = {}, onGestureEnd = {})
    )
}

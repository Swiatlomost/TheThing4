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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.lerp
import com.example.cos.designsystem.components.NeonButton
import com.example.cos.designsystem.tokens.LocalUiTokens
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
    onOpenMorphogenesis: () -> Unit,
    onOpenSkinDemo: () -> Unit,
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
            NeonButton(text = "Reset", onClick = onReset)
            NeonButton(text = "Morfogeneza", onClick = onOpenMorphogenesis)
            NeonButton(text = "Skin", onClick = onOpenSkinDemo)
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
                Text(text = "Nowa komĂłrka")
            }
        }
    }
}

@Composable
fun CosLifecycleCanvas(
    state: CosLifecycleState,
    modifier: Modifier = Modifier
) {
    val tokens = LocalUiTokens.current
    val density = LocalDensity.current
    // TEMP hard-coded neon parameters (locked for SKIN calibration)
    val ringDpConst = 5.1f
    val haloWidthMultConst = 11.3f
    val haloAlpha = 0.40f
    val blurDpConst = 46f
    val ringCrispMult = 0.6f // thinner bright outer ring
    val ringCoreMult = 0.4f // white core slimmer than crisp ring

    val ringStrokePx = with(density) { ringDpConst.dp.toPx() }
    val haloWidthPx = ringStrokePx * haloWidthMultConst
    val blurPx = with(density) { blurDpConst.dp.toPx() }
    val baseRadiusUnits = state.cellRadius.takeIf { it > 0f }
        ?: computeBaseRadiusUnits(state.cells.size.coerceAtLeast(1))
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.primary
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
            accentColor = accentColor,
            ringStrokePx = ringStrokePx,
            haloWidthPx = haloWidthPx,
            haloAlpha = haloAlpha,
            blurPx = blurPx
        )
    }
}

private fun DrawScope.drawOrganism(
    cells: List<AnimatedCell>,
    baseRadiusUnits: Float,
    containerRadiusUnits: Float,
    primaryColor: Color,
    accentColor: Color,
    ringStrokePx: Float,
    haloWidthPx: Float,
    haloAlpha: Float,
    blurPx: Float
) {
    if (cells.isEmpty()) return

    val scale = (min(size.width, size.height) / 2f) / containerRadiusUnits
    val origin = Offset(size.width / 2f, size.height / 2f)

    // Circular mask to keep halo inside organism boundary (avoids square window glow)
    val containerRadiusPx = containerRadiusUnits * scale
    val maskRect = Rect(
        offset = Offset(origin.x - containerRadiusPx, origin.y - containerRadiusPx),
        size = androidx.compose.ui.geometry.Size(containerRadiusPx * 2f, containerRadiusPx * 2f)
    )
    val maskPath = Path().apply { addOval(maskRect) }

    clipPath(maskPath, clipOp = ClipOp.Intersect) {
        cells.forEach { animated ->
        val centerPx = origin + animated.snapshot.center * scale
        val cellRadiusUnits = animated.snapshot.radius.takeIf { it > 0f } ?: baseRadiusUnits
        // Birth segment [0..1]: kropka -> pierścień z progiem
        data class Seg(val outerR: Float, val fillR: Float, val outA: Float, val fillA: Float)
        val seg: Seg =
            if (animated.stageValue <= 1f) {
                val s = animated.stageValue.coerceIn(0f, 1f)
                val th = 0.8f
                if (s < th) {
                    val t = (s / th).coerceIn(0f, 1f)
                    val outer = cellRadiusUnits * lerp(0.55f, 0.75f, t)
                    val fillR = cellRadiusUnits * lerp(0.15f, 0.65f, t)
                    Seg(outer, fillR, lerp(0.4f, 0.7f, t), 1f)
                } else {
                    val t = ((s - th) / (1f - th)).coerceIn(0f, 1f)
                    val outer = cellRadiusUnits * lerp(0.75f, 1f, t)
                    val fillR = cellRadiusUnits * lerp(0.65f, 0.75f, t) // lekko pod ring
                    Seg(outer, fillR, lerp(0.7f, 0.85f, t), lerp(1f, 0.85f, t))
                }
            } else {
                val outer = cellRadiusUnits * outerRadiusMultiplier(animated.stageValue)
                val fillR = cellRadiusUnits * fillRadiusMultiplier(animated.stageValue)
                Seg(outer, fillR, outlineAlpha(animated.stageValue), fillAlpha(animated.stageValue))
            }
        val outerRadiusUnits = seg.outerR
        val fillRadiusUnitsRaw = seg.fillR
        var outlineAlpha = seg.outA
        // Keep neon visible even after new births; do not fade below floor
        outlineAlpha = outlineAlpha.coerceAtLeast(0.5f)
        val fillAlpha = seg.fillA

        val outerRadiusPx = outerRadiusUnits * scale
        val fillRadiusPx = fillRadiusUnitsRaw * scale

        if (fillRadiusPx > 0f) {
            drawCircle(
                color = primaryColor.copy(alpha = fillAlpha),
                radius = fillRadiusPx,
                center = centerPx
            )
        }

        if (outlineAlpha > 0f && outerRadiusPx > 0f) {
            drawIntoCanvas { canvas ->
                val p = androidx.compose.ui.graphics.Paint()
                val fp = p.asFrameworkPaint()
                fp.isAntiAlias = true
                fp.style = android.graphics.Paint.Style.STROKE
                // Blurred halo
                fp.color = accentColor.copy(alpha = outlineAlpha * haloAlpha).toArgb()
                fp.strokeWidth = haloWidthPx
                fp.maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(centerPx, outerRadiusPx, p)
            // White core ring to simulate bloom (thinner)
            fp.maskFilter = null
            fp.color = Color.White.copy(alpha = outlineAlpha * 0.55f).toArgb()
            fp.strokeWidth = ringStrokePx * 0.4f
            canvas.drawCircle(centerPx, outerRadiusPx, p)
            // Accent crisp ring
            fp.color = accentColor.copy(alpha = outlineAlpha.coerceAtMost(0.95f)).toArgb()
            fp.strokeWidth = ringStrokePx * 0.6f
            canvas.drawCircle(centerPx, outerRadiusPx, p)
        }
        }
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


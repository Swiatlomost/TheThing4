package com.thething.cos.lifecycle

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.lerp
import androidx.compose.ui.res.stringResource
import com.thething.cos.designsystem.components.NeonButton
import com.thething.cos.designsystem.tokens.LocalUiTokens
import com.thething.cos.lifecycle.R
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.LinearGradient

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
    onOpenSensorHarness: () -> Unit,
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
            NeonButton(text = stringResource(R.string.lifecycle_action_reset), onClick = onReset)
            NeonButton(text = stringResource(R.string.lifecycle_action_morphogenesis), onClick = onOpenMorphogenesis)
            NeonButton(text = stringResource(R.string.lifecycle_action_skin_demo), onClick = onOpenSkinDemo)
            NeonButton(text = stringResource(R.string.lifecycle_action_sensor_harness), onClick = onOpenSensorHarness)
            Button(
                onClick = { onSetStage(LifecycleStageCommand.BUD) },
                enabled = canNarodziny
            ) {
                Text(text = stringResource(R.string.lifecycle_action_birth))
            }
            Button(
                onClick = { onSetStage(LifecycleStageCommand.MATURE) },
                enabled = canDojrzewanie
            ) {
                Text(text = stringResource(R.string.lifecycle_action_mature))
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
    val tokens = LocalUiTokens.current
    val density = LocalDensity.current
    // Neon parameters sourced from tokens (fallbacks preserve calibrated look)
    val ringStrokeDp = tokens.cell.ringStrokeDp.toFloat()
    val haloWidthMult = (tokens.glow.haloWidthMult ?: 11.3).toFloat()
    val haloAlpha = (tokens.glow.haloAlpha ?: 0.40).toFloat()
    val blurDp = tokens.glow.blurDp.toFloat()

    val ringCrispMult = 0.6f // accent crisp ring width multiplier
    val ringCoreMult = 0.4f  // white core ring width multiplier

    val ringStrokePx = with(density) { ringStrokeDp.dp.toPx() }
    val haloWidthPx = ringStrokePx * haloWidthMult
    val blurPx = with(density) { blurDp.dp.toPx() }
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
        val energy = tokens.energy
        drawOrganism(
            cells = animatedCells,
            baseRadiusUnits = baseRadiusUnits,
            containerRadiusUnits = ORGANISM_RADIUS_UNITS,
            primaryColor = primaryColor,
            accentColor = accentColor,
            ringStrokePx = ringStrokePx,
            haloWidthPx = haloWidthPx,
            haloAlpha = haloAlpha,
            blurPx = blurPx,
            ringCoreMult = ringCoreMult,
            ringCrispMult = ringCrispMult,
            energyWhiten = energy.whiten.toFloat(),
            energyCoreAlpha = energy.coreAlpha.toFloat(),
            energyGlowAlpha = energy.glowAlpha.toFloat(),
            energyCoreStop = energy.coreStop.toFloat(),
            energyGlowStop = energy.glowStop.toFloat(),
            energyRimAlpha = energy.rimAlpha.toFloat(),
            specularEnabled = (energy.specular?.enabled ?: false),
            specularAngleDeg = (energy.specular?.angleDeg ?: 45.0).toFloat(),
            specularBandAlpha = (energy.specular?.bandAlpha ?: 0.22).toFloat(),
            specularBandWidth = (energy.specular?.bandWidth ?: 0.10).toFloat(),
            specularJitterDeg = (energy.specular?.jitterDeg ?: 0.0).toFloat()
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
    blurPx: Float,
    ringCoreMult: Float,
    ringCrispMult: Float,
    energyWhiten: Float,
    energyCoreAlpha: Float,
    energyGlowAlpha: Float,
    energyCoreStop: Float,
    energyGlowStop: Float,
    energyRimAlpha: Float,
    specularEnabled: Boolean,
    specularAngleDeg: Float,
    specularBandAlpha: Float,
    specularBandWidth: Float,
    specularJitterDeg: Float
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
        // Birth segment [0..1]: kropka -> pier�cie� z progiem
        data class Seg(val outerR: Float, val fillR: Float, val outA: Float, val fillA: Float)
        val seg: Seg =
            if (animated.stageValue <= 1f) {
                val s = animated.stageValue.coerceIn(0f, 1f)
                val th = 0.8f
                if (s < th) {
                    val t = (s / th).coerceIn(0f, 1f)
                    // Birth: ring-only (no fill). Grow ring from small to target.
                    val outer = cellRadiusUnits * lerp(0.10f, 1f, t)
                    Seg(outer, 0f, lerp(0.55f, 0.85f, t), 0f)
                } else {
                    val t = ((s - th) / (1f - th)).coerceIn(0f, 1f)
                    val outer = cellRadiusUnits * lerp(0.75f, 1f, t)
                    // Keep no fill at end of Birth; ring finalizes
                    Seg(outer, 0f, lerp(0.75f, 0.9f, t), 0f)
                }
            } else {
                // Dojrzewanie i dalej: ring pozostaje nieruchomy (outer = 1R),
                // nasyca si� jedynie �rodek (fill ro�nie).
                val outer = cellRadiusUnits * 1f
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
            val jitter = if (specularEnabled && specularJitterDeg != 0f) {
                stableAngleJitter(animated.snapshot.id, specularJitterDeg)
            } else 0f
            val specAngleForCell = (specularAngleDeg + jitter)
            drawGaussianEnergyFill(
                center = centerPx,
                radius = fillRadiusPx,
                accent = primaryColor,
                alpha = fillAlpha,
                whiten = energyWhiten,
                coreAlpha = energyCoreAlpha,
                glowAlpha = energyGlowAlpha,
                coreStop = energyCoreStop,
                glowStop = energyGlowStop,
                rimAlpha = energyRimAlpha,
                specularEnabled = specularEnabled,
                specularAngleDeg = specAngleForCell,
                specularBandWidth = specularBandWidth,
                specularBandAlpha = specularBandAlpha
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
                val safeBlurPx = blurPx.coerceAtLeast(0.5f)
                fp.maskFilter = android.graphics.BlurMaskFilter(safeBlurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(centerPx, outerRadiusPx, p)
            // White core ring to simulate bloom (thinner)
            fp.maskFilter = null
            fp.color = Color.White.copy(alpha = outlineAlpha * 0.55f).toArgb()
            fp.strokeWidth = ringStrokePx * ringCoreMult
            canvas.drawCircle(centerPx, outerRadiusPx, p)
            // Accent crisp ring
            fp.color = accentColor.copy(alpha = outlineAlpha.coerceAtMost(0.95f)).toArgb()
            fp.strokeWidth = ringStrokePx * ringCrispMult
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
    else -> 1f
}

private fun fillAlpha(stageValue: Float): Float = when {
    stageValue <= 1f -> 1f
    stageValue <= 2f -> lerp(0.85f, 1f, (stageValue - 1f).coerceIn(0f, 1f))
    else -> 1f
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

private fun stableAngleJitter(id: String, jitterAmplitudeDeg: Float): Float {
    if (jitterAmplitudeDeg == 0f) return 0f
    val h = id.hashCode()
    val mix = ((h ushr 16) xor (h and 0xFFFF)).toFloat()
    val unit = (mix % 65535f) / 65535f // [0,1)
    val signed = (unit * 2f) - 1f // [-1,1)
    return signed * jitterAmplitudeDeg
}

// Simple cached noise + radial blend to simulate energetic fill
private object EnergyShaders {
    private const val NOISE_SIZE = 256
    private val noiseBitmap: Bitmap by lazy { generatePlasma(NOISE_SIZE, NOISE_SIZE) }
    private val noiseShader: BitmapShader by lazy {
        BitmapShader(noiseBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    fun composeSpecularShader(
        cx: Float,
        cy: Float,
        radius: Float,
        accent: Color,
        alpha: Float
    ): Shader {
        // Base spherical glow
        val baseInner = accent.copy(alpha = (alpha * 0.85f).coerceIn(0f, 1f)).toArgb()
        val baseMid = accent.copy(alpha = (alpha * 0.35f).coerceIn(0f, 1f)).toArgb()
        val baseRadial = RadialGradient(
            cx, cy, radius,
            intArrayOf(baseInner, baseMid, android.graphics.Color.TRANSPARENT),
            floatArrayOf(0f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )

        // Diagonal specular streaks (\ and /)
        val s = radius * 1.6f // extend beyond circle for smooth falloff
        val hl = mixToWhite(accent, 0.65f).copy(alpha = (alpha * 0.9f).coerceIn(0f, 1f)).toArgb()
        val tr = android.graphics.Color.TRANSPARENT
        // Narrow band around center
        val band = floatArrayOf(0f, 0.45f, 0.5f, 0.55f, 1f)
        val col = intArrayOf(tr, tr, hl, tr, tr)

        val diag1 = LinearGradient(
            cx - s, cy - s, cx + s, cy + s,
            col, band, Shader.TileMode.CLAMP
        )
        val diag2 = LinearGradient(
            cx - s, cy + s, cx + s, cy - s,
            col, band, Shader.TileMode.CLAMP
        )

        // Compose: base -> diag1 -> diag2 using SCREEN
        val basePlus1 = ComposeShader(baseRadial, diag1, PorterDuff.Mode.SCREEN)
        return ComposeShader(basePlus1, diag2, PorterDuff.Mode.SCREEN)
    }

    fun composeGaussianEnergyShader(
        cx: Float,
        cy: Float,
        radius: Float,
        accent: Color,
        alpha: Float
    ): Shader {
        // Core (szybki spadek ~exp(-r^2*4)) � aproksymacja przez g�ste stopnie
        val coreCol = mixToWhite(accent, 0.9f)
        val core = RadialGradient(
            cx, cy, radius,
            intArrayOf(
                coreCol.copy(alpha = (alpha * 0.95f)).toArgb(),
                coreCol.copy(alpha = (alpha * 0.55f)).toArgb(),
                coreCol.copy(alpha = (alpha * 0.12f)).toArgb(),
                android.graphics.Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.35f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )

        // Glow (wolniejszy spadek ~exp(-r^2*1.5))
        val glow = RadialGradient(
            cx, cy, radius,
            intArrayOf(
                accent.copy(alpha = (alpha * 0.65f)).toArgb(),
                accent.copy(alpha = (alpha * 0.35f)).toArgb(),
                android.graphics.Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.72f, 1f),
            Shader.TileMode.CLAMP
        )

        // Mieszamy SCREEN jak w referencji (sumaryczna �energia�) bez szumu
        return ComposeShader(glow, core, PorterDuff.Mode.SCREEN)
    }

    private fun mixToWhite(c: Color, t: Float): Color {
        val clamped = t.coerceIn(0f, 1f)
        return Color(
            red = lerp(c.red, 1f, clamped),
            green = lerp(c.green, 1f, clamped),
            blue = lerp(c.blue, 1f, clamped),
            alpha = c.alpha
        )
    }

    fun composeEnergyShader(cx: Float, cy: Float, radius: Float, accent: Color, alpha: Float): Shader {
        val inner = accent.copy(alpha = (alpha * 0.9f).coerceIn(0f, 1f)).toArgb()
        val mid = accent.copy(alpha = (alpha * 0.35f).coerceIn(0f, 1f)).toArgb()
        val outer = android.graphics.Color.TRANSPARENT
        val radial = RadialGradient(
            cx, cy, radius,
            intArrayOf(inner, mid, outer),
            floatArrayOf(0f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
        // Compose bitmap noise with radial gradient using SCREEN to boost luminous regions
        return ComposeShader(noiseShader, radial, PorterDuff.Mode.SCREEN)
    }

    private fun generatePlasma(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        var idx = 0
        for (y in 0 until h) {
            val vy = y.toFloat() / h
            for (x in 0 until w) {
                val vx = x.toFloat() / w
                // Multi-sine plasma (cheap, tile-friendly)
                val v = (
                    kotlin.math.sin(10f * vx) +
                    kotlin.math.sin(10f * vy) +
                    kotlin.math.sin(14f * (vx + vy))
                ) / 3f
                val n = ((v * 0.5f + 0.5f).coerceIn(0f, 1f))
                // Gamma for contrast
                val g = n * n
                val c = (g * 255f).toInt().coerceIn(0, 255)
                val argb = (0xFF shl 24) or (c shl 16) or (c shl 8) or c
                pixels[idx++] = argb
            }
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }
}

private fun DrawScope.drawGaussianEnergyFill(
    center: Offset,
    radius: Float,
    accent: Color,
    alpha: Float,
    whiten: Float,
    coreAlpha: Float,
    glowAlpha: Float,
    coreStop: Float,
    glowStop: Float,
    rimAlpha: Float,
    specularEnabled: Boolean,
    specularAngleDeg: Float,
    specularBandWidth: Float,
    specularBandAlpha: Float,
) {
    val rect = Rect(
        offset = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f)
    )
    val path = Path().apply { addOval(rect) }

    fun mixToWhite(c: Color, t: Float): Color {
        val clamped = t.coerceIn(0f, 1f)
        return Color(
            red = lerp(c.red, 1f, clamped),
            green = lerp(c.green, 1f, clamped),
            blue = lerp(c.blue, 1f, clamped),
            alpha = c.alpha
        )
    }

    // Core (whitened accent) � emulate faster Gaussian falloff via stops
    val coreColor = mixToWhite(accent, whiten)
    val coreBrush = Brush.radialGradient(
        colors = listOf(
            coreColor.copy(alpha = (alpha * coreAlpha)),
            coreColor.copy(alpha = (alpha * coreAlpha * 0.58f)),
            coreColor.copy(alpha = (alpha * coreAlpha * 0.14f)),
            coreColor.copy(alpha = 0f)
        ),
        center = center,
        radius = radius
    )
    drawPath(path = path, brush = coreBrush)

    // Glow (accent) � slower falloff
    val glowBrush = Brush.radialGradient(
        colors = listOf(
            accent.copy(alpha = (alpha * glowAlpha)),
            accent.copy(alpha = (alpha * glowAlpha * 0.54f)),
            accent.copy(alpha = 0f)
        ),
        center = center,
        radius = radius
    )
    drawPath(path = path, brush = glowBrush)

    // Specular bands (optional) � two perpendicular streaks
    if (specularEnabled && specularBandAlpha > 0f && specularBandWidth > 0f) {
        val highlightColor = mixToWhite(accent, 1f).copy(alpha = (alpha * specularBandAlpha).coerceIn(0f, 1f))
        drawSpecularBand(path, center, radius, specularAngleDeg, highlightColor, specularBandWidth)
        drawSpecularBand(path, center, radius, (specularAngleDeg + 90f) % 360f, highlightColor.copy(alpha = highlightColor.alpha * 0.85f), specularBandWidth)
    }

    // Optional subtle rim light near the boundary
    if (rimAlpha > 0f) {
        drawIntoCanvas { canvas ->
            val p = androidx.compose.ui.graphics.Paint()
            val fp = p.asFrameworkPaint()
            fp.isAntiAlias = true
            fp.style = android.graphics.Paint.Style.STROKE
            fp.color = accent.copy(alpha = rimAlpha.coerceIn(0f, 1f)).toArgb()
            fp.strokeWidth = (radius * 0.06f).coerceAtLeast(1f)
            fp.maskFilter = null
            canvas.drawCircle(center, radius, p)
        }
    }
}

private fun DrawScope.drawSpecularBand(
    clipPath: Path,
    center: Offset,
    radius: Float,
    angleDeg: Float,
    color: Color,
    width: Float,
) {
    // Compute gradient line along angle across circle
    val a = angleDeg / 180f * PI.toFloat()
    val dir = Offset(cos(a), sin(a))
    val ext = radius * 1.6f
    val start = center - dir * ext
    val end = center + dir * ext
    val w = width.coerceIn(0.02f, 0.5f)
    val mid = 0.5f
    val half = (w * 0.5f).coerceIn(0.01f, 0.4f)

    val brush = Brush.linearGradient(
        0f to color.copy(alpha = 0f),
        (mid - half) to color.copy(alpha = 0f),
        mid to color,
        (mid + half) to color.copy(alpha = 0f),
        1f to color.copy(alpha = 0f),
        start = start,
        end = end
    )
    drawPath(path = clipPath, brush = brush)
}


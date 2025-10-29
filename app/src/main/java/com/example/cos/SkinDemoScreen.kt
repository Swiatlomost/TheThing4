package com.example.cos

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cos.designsystem.components.NeonButton
import com.example.cos.designsystem.tokens.LocalUiTokens
import com.example.cos.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinDemoScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.skin_demo_title)) },
            navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } }
        )

        val tokens = LocalUiTokens.current
        var ringDp by remember { mutableStateOf(tokens.cell.ringStrokeDp.toFloat()) }
        var haloMult by remember { mutableStateOf((tokens.glow.haloWidthMult ?: 8.0).toFloat()) }
        var haloAlpha by remember { mutableStateOf((tokens.glow.haloAlpha ?: 0.35).toFloat()) }
        var blurDp by remember { mutableStateOf(tokens.glow.blurDp.toFloat()) }

        val energy = tokens.energy
        var whiten by remember { mutableStateOf(energy.whiten.toFloat()) }
        var coreAlpha by remember { mutableStateOf(energy.coreAlpha.toFloat()) }
        var glowAlpha by remember { mutableStateOf(energy.glowAlpha.toFloat()) }
        var coreStop by remember { mutableStateOf(energy.coreStop.toFloat()) }
        var glowStop by remember { mutableStateOf(energy.glowStop.toFloat()) }
        var rimAlpha by remember { mutableStateOf(energy.rimAlpha.toFloat()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.skin_demo_cell_preview), style = MaterialTheme.typography.titleMedium)
            CombinedCellPreview(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                ringStrokeDp = ringDp,
                haloWidthMult = haloMult,
                haloAlpha = haloAlpha,
                blurDp = blurDp,
                whiten = whiten,
                coreAlpha = coreAlpha,
                glowAlpha = glowAlpha,
                coreStop = coreStop,
                glowStop = glowStop,
                rimAlpha = rimAlpha
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VerticalSlider(stringResource(R.string.skin_demo_slider_ring), ringDp, 1f..8f) { ringDp = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_halo_multiplier), haloMult, 2f..20f) { haloMult = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_halo_alpha), haloAlpha, 0f..1f) { haloAlpha = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_blur), blurDp, 0f..96f) { blurDp = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_whiten), whiten, 0f..1f) { whiten = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_core), coreAlpha, 0f..1f) { coreAlpha = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_glow), glowAlpha, 0f..1f) { glowAlpha = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_core_stop), coreStop, 0.3f..0.9f) { coreStop = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_glow_stop), glowStop, 0.5f..0.95f) { glowStop = it }
                VerticalSlider(stringResource(R.string.skin_demo_slider_rim), rimAlpha, 0f..1f) { rimAlpha = it }
            }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var status by remember { mutableStateOf("") }
            var busy by remember { mutableStateOf(false) }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonButton(
                    text = stringResource(R.string.skin_demo_save),
                    enabled = !busy,
                    onClick = {
                        if (busy) return@NeonButton
                        busy = true
                        scope.launch {
                            val result = runCatching {
                                writeSkinOverride(
                                    context = context,
                                    ringStroke = ringDp,
                                    haloMult = haloMult,
                                    haloAlpha = haloAlpha,
                                    blurDp = blurDp,
                                    whiten = whiten,
                                    coreAlpha = coreAlpha,
                                    glowAlpha = glowAlpha,
                                    coreStop = coreStop,
                                    glowStop = glowStop,
                                    rimAlpha = rimAlpha
                                )
                            }
                            status = result.fold(
                                onSuccess = {
                                    (context as? Activity)?.recreate()
                                    context.getString(R.string.skin_demo_save_success)
                                },
                                onFailure = { error ->
                                    val message = error.message ?: context.getString(R.string.skin_demo_error_unknown)
                                    context.getString(R.string.skin_demo_save_error, message)
                                }
                            )
                            busy = false
                        }
                    }
                )
                NeonButton(
                    text = stringResource(R.string.skin_demo_remove),
                    enabled = !busy,
                    onClick = {
                        if (busy) return@NeonButton
                        busy = true
                        scope.launch {
                            val result = runCatching { deleteSkinOverride(context) }
                            status = result.fold(
                                onSuccess = { removed ->
                                    (context as? Activity)?.recreate()
                                    if (removed) {
                                        context.getString(R.string.skin_demo_remove_success)
                                    } else {
                                        context.getString(R.string.skin_demo_remove_missing)
                                    }
                                },
                                onFailure = { error ->
                                    val message = error.message ?: context.getString(R.string.skin_demo_error_unknown)
                                    context.getString(R.string.skin_demo_remove_error, message)
                                }
                            )
                            busy = false
                        }
                    }
                )
            }

            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun VerticalSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.skin_demo_status_label, value),
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier
                .height(320.dp)
                .width(64.dp)
                .graphicsLayer(
                    rotationZ = -90f,
                    scaleX = 1.15f,
                    scaleY = 1.15f
                )
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CombinedCellPreview(
    modifier: Modifier = Modifier,
    ringStrokeDp: Float,
    haloWidthMult: Float,
    haloAlpha: Float,
    blurDp: Float,
    whiten: Float,
    coreAlpha: Float,
    glowAlpha: Float,
    coreStop: Float,
    glowStop: Float,
    rimAlpha: Float
) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.padding(8.dp)) {
        val center = center
        val radius = size.minDimension * 0.33f
        val path = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
        }

        fun mixToWhite(amount: Float): Color {
            val clamped = amount.coerceIn(0f, 1f)
            return Color(
                red = androidx.compose.ui.util.lerp(accent.red, 1f, clamped),
                green = androidx.compose.ui.util.lerp(accent.green, 1f, clamped),
                blue = androidx.compose.ui.util.lerp(accent.blue, 1f, clamped),
                alpha = accent.alpha
            )
        }

        val coreColor = mixToWhite(whiten)
        val coreBrush = Brush.radialGradient(
            colors = listOf(
                coreColor.copy(alpha = coreAlpha),
                coreColor.copy(alpha = coreAlpha * 0.55f),
                coreColor.copy(alpha = coreAlpha * 0.12f),
                coreColor.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        )
        drawPath(path = path, brush = coreBrush)

        val glowBrush = Brush.radialGradient(
            colors = listOf(
                accent.copy(alpha = glowAlpha),
                accent.copy(alpha = glowAlpha * 0.5f),
                accent.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        )
        drawPath(path = path, brush = glowBrush)

        if (rimAlpha > 0f) {
            drawIntoCanvas { canvas ->
                val paint = androidx.compose.ui.graphics.Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                frameworkPaint.isAntiAlias = true
                frameworkPaint.style = android.graphics.Paint.Style.STROKE
                frameworkPaint.color = accent.copy(alpha = rimAlpha).toArgb()
                frameworkPaint.strokeWidth = radius * 0.06f
                canvas.drawCircle(center, radius, paint)
            }
        }

        drawIntoCanvas { canvas ->
            val paint = androidx.compose.ui.graphics.Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.isAntiAlias = true
            frameworkPaint.style = android.graphics.Paint.Style.STROKE
            frameworkPaint.color = accent.copy(alpha = haloAlpha).toArgb()
            frameworkPaint.strokeWidth = ringStrokeDp * haloWidthMult
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(blurDp, android.graphics.BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(center, radius, paint)

            frameworkPaint.maskFilter = null
            frameworkPaint.color = Color.White.copy(alpha = 0.55f).toArgb()
            frameworkPaint.strokeWidth = ringStrokeDp * 0.6f
            canvas.drawCircle(center, radius, paint)

            frameworkPaint.color = accent.copy(alpha = 0.95f).toArgb()
            frameworkPaint.strokeWidth = ringStrokeDp
            canvas.drawCircle(center, radius, paint)
        }
    }
}

private suspend fun writeSkinOverride(
    context: Context,
    ringStroke: Float,
    haloMult: Float,
    haloAlpha: Float,
    blurDp: Float,
    whiten: Float,
    coreAlpha: Float,
    glowAlpha: Float,
    coreStop: Float,
    glowStop: Float,
    rimAlpha: Float
) = withContext(Dispatchers.IO) {
    val payload = JSONObject()
        .put(
            "glow",
            JSONObject()
                .put("halo-width-mult", haloMult.toDouble())
                .put("halo-alpha", haloAlpha.toDouble())
                .put("blur-dp", blurDp.toInt())
        )
        .put("cell", JSONObject().put("ring-stroke-dp", ringStroke.toInt()))
        .put(
            "energy",
            JSONObject()
                .put("whiten", whiten.toDouble())
                .put("core-alpha", coreAlpha.toDouble())
                .put("glow-alpha", glowAlpha.toDouble())
                .put("core-stop", coreStop.toDouble())
                .put("glow-stop", glowStop.toDouble())
                .put("rim-alpha", rimAlpha.toDouble())
        )

    context.openFileOutput("ui_tokens_override.json", Context.MODE_PRIVATE).use { stream ->
        stream.write(payload.toString(2).toByteArray())
    }
}

private suspend fun deleteSkinOverride(context: Context): Boolean =
    withContext(Dispatchers.IO) {
        context.deleteFile("ui_tokens_override.json")
    }

package com.example.cos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import com.example.cos.designsystem.components.NeonButton
import com.example.cos.designsystem.tokens.LocalUiTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinDemoScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Skin Demo") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Wróć") } }
        )

        // Defaults from tokens (single unified editor)
        val t = LocalUiTokens.current
        var ringDp by remember { mutableStateOf(t.cell.ringStrokeDp.toFloat()) }
        var haloMult by remember { mutableStateOf(((t.glow.haloWidthMult ?: 8.0).toFloat())) }
        var haloAlpha by remember { mutableStateOf(((t.glow.haloAlpha ?: 0.35).toFloat())) }
        var blurDp by remember { mutableStateOf(t.glow.blurDp.toFloat()) }

        val e = t.energy
        var whiten by remember { mutableStateOf(e.whiten.toFloat()) }
        var coreAlpha by remember { mutableStateOf(e.coreAlpha.toFloat()) }
        var glowAlpha by remember { mutableStateOf(e.glowAlpha.toFloat()) }
        var coreStop by remember { mutableStateOf(e.coreStop.toFloat()) }
        var glowStop by remember { mutableStateOf(e.glowStop.toFloat()) }
        var rimAlpha by remember { mutableStateOf(e.rimAlpha.toFloat()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Cell preview", style = MaterialTheme.typography.titleMedium)
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
                rimAlpha = rimAlpha,
            )

            // Equalizer-style vertical sliders with live preview
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VerticalSlider("ring", ringDp, 1f..8f) { ringDp = it }
                VerticalSlider("halo-m", haloMult, 2f..20f) { haloMult = it }
                VerticalSlider("halo-a", haloAlpha, 0f..1f) { haloAlpha = it }
                VerticalSlider("blur", blurDp, 0f..96f) { blurDp = it }
                VerticalSlider("white", whiten, 0f..1f) { whiten = it }
                VerticalSlider("c-α", coreAlpha, 0f..1f) { coreAlpha = it }
                VerticalSlider("g-α", glowAlpha, 0f..1f) { glowAlpha = it }
                VerticalSlider("c-stop", coreStop, 0.3f..0.9f) { coreStop = it }
                VerticalSlider("g-stop", glowStop, 0.5f..0.95f) { glowStop = it }
                VerticalSlider("rim", rimAlpha, 0f..1f) { rimAlpha = it }
            }

            val ctx = LocalContext.current
            var status by remember { mutableStateOf("") }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonButton(text = "Zapisz (globalnie)", onClick = {
                    val root = JSONObject()
                    val glow = JSONObject()
                        .put("halo-width-mult", haloMult.toDouble())
                        .put("halo-alpha", haloAlpha.toDouble())
                        .put("blur-dp", blurDp.toInt())
                    val cell = JSONObject().put("ring-stroke-dp", ringDp.toInt())
                    val energy = JSONObject()
                        .put("whiten", whiten.toDouble())
                        .put("core-alpha", coreAlpha.toDouble())
                        .put("glow-alpha", glowAlpha.toDouble())
                        .put("core-stop", coreStop.toDouble())
                        .put("glow-stop", glowStop.toDouble())
                        .put("rim-alpha", rimAlpha.toDouble())
                    root.put("glow", glow)
                    root.put("cell", cell)
                    root.put("energy", energy)
                    try {
                        ctx.openFileOutput("ui_tokens_override.json", android.content.Context.MODE_PRIVATE).use {
                            it.write(root.toString(2).toByteArray())
                        }
                        (ctx as? android.app.Activity)?.recreate()
                        status = "Zapisano i odświeżono motyw."
                    } catch (t: Throwable) {
                        status = "Błąd zapisu: ${t.message}"
                    }
                })
                NeonButton(text = "Usuń override", onClick = {
                    try {
                        val ok = ctx.deleteFile("ui_tokens_override.json")
                        (ctx as? android.app.Activity)?.recreate()
                        status = if (ok) "Przywrócono domyślne (odświeżone)." else "Brak pliku override."
                    } catch (t: Throwable) {
                        status = "Błąd usuwania: ${t.message}"
                    }
                })
            }
            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun VerticalSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(String.format("%.2f", value), style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier
                .height(320.dp)    // więcej przestrzeni na ruch
                .width(64.dp)      // większy uchwyt po obróceniu
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
    rimAlpha: Float,
) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.padding(8.dp)) {
        val c = center
        val r = size.minDimension * 0.33f
        val path = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(c.x - r, c.y - r, c.x + r, c.y + r))
        }

        fun mixToWhite(t: Float): Color {
            val cl = t.coerceIn(0f, 1f)
            return Color(
                red = androidx.compose.ui.util.lerp(accent.red, 1f, cl),
                green = androidx.compose.ui.util.lerp(accent.green, 1f, cl),
                blue = androidx.compose.ui.util.lerp(accent.blue, 1f, cl),
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
            center = c,
            radius = r
        )
        drawPath(path = path, brush = coreBrush)

        val glowBrush = Brush.radialGradient(
            colors = listOf(
                accent.copy(alpha = glowAlpha),
                accent.copy(alpha = glowAlpha * 0.5f),
                accent.copy(alpha = 0f)
            ),
            center = c,
            radius = r
        )
        drawPath(path = path, brush = glowBrush)

        // Rim light
        if (rimAlpha > 0f) {
            drawIntoCanvas { canvas ->
                val p = androidx.compose.ui.graphics.Paint()
                val fp = p.asFrameworkPaint()
                fp.isAntiAlias = true
                fp.style = android.graphics.Paint.Style.STROKE
                fp.color = accent.copy(alpha = rimAlpha).toArgb()
                fp.strokeWidth = r * 0.06f
                canvas.drawCircle(c, r, p)
            }
        }

        // Neon ring + halo
        drawIntoCanvas { canvas ->
            val p = androidx.compose.ui.graphics.Paint()
            val fp = p.asFrameworkPaint()
            fp.isAntiAlias = true
            fp.style = android.graphics.Paint.Style.STROKE
            // Halo
            fp.color = accent.copy(alpha = haloAlpha).toArgb()
            fp.strokeWidth = ringStrokeDp * haloWidthMult
            fp.maskFilter = android.graphics.BlurMaskFilter(blurDp, android.graphics.BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(c, r, p)
            // White core
            fp.maskFilter = null
            fp.color = Color.White.copy(alpha = 0.55f).toArgb()
            fp.strokeWidth = ringStrokeDp * 0.6f
            canvas.drawCircle(c, r, p)
            // Accent crisp
            fp.color = accent.copy(alpha = 0.95f).toArgb()
            fp.strokeWidth = ringStrokeDp
            canvas.drawCircle(c, r, p)
        }
    }
}

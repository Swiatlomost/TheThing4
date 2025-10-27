package com.example.cos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import org.json.JSONObject
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Neon ring (algorytm)", style = MaterialTheme.typography.titleMedium)
            // Defaults from tokens
            val t = LocalUiTokens.current
            var ringDp by remember { mutableStateOf(t.cell.ringStrokeDp.toFloat()) }
            var haloMult by remember { mutableStateOf(((t.glow.haloWidthMult ?: 8.0).toFloat())) }
            var haloAlpha by remember { mutableStateOf(((t.glow.haloAlpha ?: 0.35).toFloat())) }
            var blurDp by remember { mutableStateOf(t.glow.blurDp.toFloat()) }

            NeonRingPreview(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                ringStrokeDp = ringDp,
                haloWidthMult = haloMult,
                haloAlpha = haloAlpha,
                blurDp = blurDp
            )

            // Controls
            LabeledSlider("ring-stroke-dp", ringDp, 1f..8f) { ringDp = it }
            LabeledSlider("halo-width-mult", haloMult, 2f..20f) { haloMult = it }
            LabeledSlider("halo-alpha", haloAlpha, 0f..1f) { haloAlpha = it }
            LabeledSlider("blur-dp", blurDp, 0f..96f) { blurDp = it }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonButton(text = "Neon A", onClick = {})
                NeonButton(text = "Neon B", onClick = {})
            }

            // --- Energy Fill Demo ---
            Text("Energy fill (Gaussian)", style = MaterialTheme.typography.titleMedium)
            val e = LocalUiTokens.current.energy
            var whiten by remember { mutableStateOf(e.whiten.toFloat()) }
            var coreAlpha by remember { mutableStateOf(e.coreAlpha.toFloat()) }
            var glowAlpha by remember { mutableStateOf(e.glowAlpha.toFloat()) }
            var coreStop by remember { mutableStateOf(e.coreStop.toFloat()) }
            var glowStop by remember { mutableStateOf(e.glowStop.toFloat()) }
            var rimAlpha by remember { mutableStateOf(e.rimAlpha.toFloat()) }
            var specEnabled by remember { mutableStateOf(e.specular?.enabled ?: false) }
            var specAngle by remember { mutableStateOf(((e.specular?.angleDeg) ?: 45.0).toFloat()) }
            var specWidth by remember { mutableStateOf(((e.specular?.bandWidth) ?: 0.10).toFloat()) }
            var specAlpha by remember { mutableStateOf(((e.specular?.bandAlpha) ?: 0.22).toFloat()) }
            var specJitter by remember { mutableStateOf(((e.specular?.jitterDeg) ?: 0.0).toFloat()) }

            EnergyFillPreview(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                whiten = whiten,
                coreAlpha = coreAlpha,
                glowAlpha = glowAlpha,
                coreStop = coreStop,
                glowStop = glowStop,
                rimAlpha = rimAlpha,
                specEnabled = specEnabled,
                specAngle = specAngle,
                specWidth = specWidth,
                specAlpha = specAlpha
            )

            LabeledSlider("whiten", whiten, 0f..1f) { whiten = it }
            LabeledSlider("core-alpha", coreAlpha, 0f..1f) { coreAlpha = it }
            LabeledSlider("glow-alpha", glowAlpha, 0f..1f) { glowAlpha = it }
            LabeledSlider("core-stop", coreStop, 0.3f..0.9f) { coreStop = it }
            LabeledSlider("glow-stop", glowStop, 0.5f..0.95f) { glowStop = it }
            LabeledSlider("rim-alpha", rimAlpha, 0f..1f) { rimAlpha = it }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("specular", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { specEnabled = !specEnabled }) { Text(if (specEnabled) "ON" else "OFF") }
            }
            if (specEnabled) {
                LabeledSlider("spec-angle", specAngle, 0f..180f) { specAngle = it }
                LabeledSlider("spec-width", specWidth, 0.02f..0.3f) { specWidth = it }
                LabeledSlider("spec-alpha", specAlpha, 0f..1f) { specAlpha = it }
                LabeledSlider("spec-jitter-deg", specJitter, 0f..60f) { specJitter = it }
            }

            val ctx = LocalContext.current
            var status by remember { mutableStateOf("") }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonButton(text = "Zapisz do tokenów", onClick = {
                    val root = JSONObject()
                    val energy = JSONObject()
                        .put("whiten", whiten.toDouble())
                        .put("core-alpha", coreAlpha.toDouble())
                        .put("glow-alpha", glowAlpha.toDouble())
                        .put("core-stop", coreStop.toDouble())
                        .put("glow-stop", glowStop.toDouble())
                        .put("rim-alpha", rimAlpha.toDouble()).apply {
                            val spec = JSONObject()
                                .put("enabled", specEnabled)
                                .put("angle-deg", specAngle.toDouble())
                                .put("band-alpha", specAlpha.toDouble())
                                .put("band-width", specWidth.toDouble())
                                .put("jitter-deg", specJitter.toDouble())
                            put("specular", spec)
                        }
                    root.put("energy", energy)
                    try {
                        ctx.openFileOutput("ui_tokens_override.json", android.content.Context.MODE_PRIVATE).use { it.write(root.toString(2).toByteArray()) }
                        status = "Zapisano override. Uruchom ponownie aplikację, aby zastosować globalnie."
                    } catch (t: Throwable) {
                        status = "Błąd zapisu: ${t.message}"
                    }
                })
                NeonButton(text = "Restart activity", onClick = {
                    val act = (ctx as? android.app.Activity)
                    if (act != null) {
                        act.recreate()
                        status = "Restartuję aktywność..."
                    } else {
                        status = "Brak Activity do restartu"
                    }
                })
                NeonButton(text = "Usuń override", onClick = {
                    try {
                        val ok = ctx.deleteFile("ui_tokens_override.json")
                        status = if (ok) "Usunięto override (restart aby wrócić do domyślnych)." else "Brak pliku override."
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
private fun NeonRingPreview(
    modifier: Modifier = Modifier,
    ringStrokeDp: Float,
    haloWidthMult: Float,
    haloAlpha: Float,
    blurDp: Float,
) {
    val tokens = LocalUiTokens.current
    val accent = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.padding(8.dp)) {
        val center = center
        val radius = size.minDimension * 0.3f
        val haloWidthPx = haloWidthMult * ringStrokeDp
        // Halo
        drawIntoCanvas { canvas ->
            val p = androidx.compose.ui.graphics.Paint()
            val fp = p.asFrameworkPaint()
            fp.isAntiAlias = true
            fp.style = android.graphics.Paint.Style.STROKE
            fp.color = accent.copy(alpha = haloAlpha).toArgb()
            fp.strokeWidth = haloWidthPx
            fp.maskFilter = android.graphics.BlurMaskFilter(blurDp, android.graphics.BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(center, radius, p)
            // White core
            fp.maskFilter = null
            fp.color = Color.White.copy(alpha = 0.55f).toArgb()
            fp.strokeWidth = ringStrokeDp * 0.6f
            canvas.drawCircle(center, radius, p)
            // Accent crisp ring
            fp.color = accent.copy(alpha = 0.95f).toArgb()
            fp.strokeWidth = ringStrokeDp
            canvas.drawCircle(center, radius, p)
        }
    }
}

@Composable
private fun LabeledSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(String.format("%.2f", value), style = MaterialTheme.typography.bodySmall)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun EnergyFillPreview(
    modifier: Modifier = Modifier,
    whiten: Float,
    coreAlpha: Float,
    glowAlpha: Float,
    coreStop: Float,
    glowStop: Float,
    rimAlpha: Float,
    specEnabled: Boolean,
    specAngle: Float,
    specWidth: Float,
    specAlpha: Float,
) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.padding(8.dp)) {
        val c = center
        val r = size.minDimension * 0.3f

        // Circle path for clipping gradients
        val path = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(
                c.x - r, c.y - r, c.x + r, c.y + r
            ))
        }

            fun mixToWhite(t: Float): androidx.compose.ui.graphics.Color {
                val cl = t.coerceIn(0f, 1f)
                return androidx.compose.ui.graphics.Color(
                    red = androidx.compose.ui.util.lerp(accent.red, 1f, cl),
                    green = androidx.compose.ui.util.lerp(accent.green, 1f, cl),
                    blue = androidx.compose.ui.util.lerp(accent.blue, 1f, cl),
                    alpha = accent.alpha
                )
            }

            val coreColor = mixToWhite(whiten)

            // Layered radial gradients (GPU-friendly)
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

            // Rim light (preview only)
            if (rimAlpha > 0f) {
                drawIntoCanvas { canvas ->
                    val p = androidx.compose.ui.graphics.Paint()
                    val fp = p.asFrameworkPaint()
                    fp.isAntiAlias = true
                    fp.style = android.graphics.Paint.Style.STROKE
                    fp.color = accent.copy(alpha = rimAlpha).toArgb()
                    fp.strokeWidth = r * 0.08f
                    fp.maskFilter = null
                    canvas.drawCircle(c, r, p)
                }
            }

            // Specular bands preview (optional)
            if (specEnabled && specAlpha > 0f && specWidth > 0f) {
                fun drawBand(angle: Float, alphaMul: Float) {
                    val a = angle / 180f * Math.PI.toFloat()
                    val dir = androidx.compose.ui.geometry.Offset(kotlin.math.cos(a), kotlin.math.sin(a))
                    val ext = r * 1.6f
                    val start = androidx.compose.ui.geometry.Offset(c.x - dir.x * ext, c.y - dir.y * ext)
                    val end = androidx.compose.ui.geometry.Offset(c.x + dir.x * ext, c.y + dir.y * ext)
                    val mid = 0.5f
                    val half = (specWidth * 0.5f).coerceIn(0.01f, 0.4f)
                    val col = accent.copy(alpha = specAlpha * alphaMul)
                    val brush = Brush.linearGradient(
                        0f to col.copy(alpha = 0f),
                        (mid - half) to col.copy(alpha = 0f),
                        mid to col,
                        (mid + half) to col.copy(alpha = 0f),
                        1f to col.copy(alpha = 0f),
                        start = start,
                        end = end
                    )
                    drawPath(path = path, brush = brush)
                }
                drawBand(specAngle, 1f)
                drawBand((specAngle + 90f) % 360f, 0.85f)
            }
    }
}

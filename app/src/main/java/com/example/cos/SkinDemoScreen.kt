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
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.LinearGradient
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

            EnergyFillPreview(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                whiten = whiten,
                coreAlpha = coreAlpha,
                glowAlpha = glowAlpha,
                coreStop = coreStop,
                glowStop = glowStop,
                rimAlpha = rimAlpha
            )

            LabeledSlider("whiten", whiten, 0f..1f) { whiten = it }
            LabeledSlider("core-alpha", coreAlpha, 0f..1f) { coreAlpha = it }
            LabeledSlider("glow-alpha", glowAlpha, 0f..1f) { glowAlpha = it }
            LabeledSlider("core-stop", coreStop, 0.3f..0.9f) { coreStop = it }
            LabeledSlider("glow-stop", glowStop, 0.5f..0.95f) { glowStop = it }
            LabeledSlider("rim-alpha", rimAlpha, 0f..1f) { rimAlpha = it }

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
                        .put("rim-alpha", rimAlpha.toDouble())
                    root.put("energy", energy)
                    try {
                        ctx.openFileOutput("ui_tokens_override.json", android.content.Context.MODE_PRIVATE).use { it.write(root.toString(2).toByteArray()) }
                        status = "Zapisano override. Uruchom ponownie aplikację, aby zastosować globalnie."
                    } catch (t: Throwable) {
                        status = "Błąd zapisu: ${t.message}"
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
) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.padding(8.dp)) {
        val c = center
        val r = size.minDimension * 0.3f

        drawIntoCanvas { canvas ->
            val p = androidx.compose.ui.graphics.Paint()
            val fp = p.asFrameworkPaint()
            fp.isAntiAlias = true
            fp.style = android.graphics.Paint.Style.FILL

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
            val core = RadialGradient(
                c.x, c.y, r,
                intArrayOf(
                    coreColor.copy(alpha = coreAlpha).toArgb(),
                    coreColor.copy(alpha = coreAlpha * 0.55f).toArgb(),
                    coreColor.copy(alpha = coreAlpha * 0.12f).toArgb(),
                    android.graphics.Color.TRANSPARENT
                ),
                floatArrayOf(0f, coreStop * 0.6f, coreStop, 1f),
                Shader.TileMode.CLAMP
            )

            val glow = RadialGradient(
                c.x, c.y, r,
                intArrayOf(
                    accent.copy(alpha = glowAlpha).toArgb(),
                    accent.copy(alpha = glowAlpha * 0.5f).toArgb(),
                    android.graphics.Color.TRANSPARENT
                ),
                floatArrayOf(0f, glowStop, 1f),
                Shader.TileMode.CLAMP
            )

            val shader = ComposeShader(glow, core, PorterDuff.Mode.SCREEN)
            fp.shader = shader
            canvas.drawCircle(c, r, p)
            fp.shader = null

            // Rim light (preview only)
            if (rimAlpha > 0f) {
                fp.style = android.graphics.Paint.Style.STROKE
                fp.color = accent.copy(alpha = rimAlpha).toArgb()
                fp.strokeWidth = r * 0.08f
                fp.maskFilter = null
                canvas.drawCircle(c, r, p)
            }
        }
    }
}

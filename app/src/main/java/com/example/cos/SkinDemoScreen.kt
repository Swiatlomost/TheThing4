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

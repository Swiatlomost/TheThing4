package com.thething.cos.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun NeonProgress(
    progress: Float, // [0..1]
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(MaterialTheme.colorScheme.surface, shape)
    ) {
        val clamped = progress.coerceIn(0f, 1f)
        val widthPercent = (clamped * 100).toInt()
        val barModifier = Modifier
            .fillMaxWidth(widthPercent / 100f)
            .height(8.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.9f),
                        accent.copy(alpha = 0.6f)
                    )
                ),
                shape
            )
        Box(modifier = barModifier)
        // glow halo
        Box(modifier = barModifier.blur(12.dp).padding(vertical = 2.dp))
    }
}


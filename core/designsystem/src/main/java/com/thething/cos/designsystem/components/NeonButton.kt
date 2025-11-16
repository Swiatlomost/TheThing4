package com.thething.cos.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(24.dp)

    // Simplified neon: blurred halo + solid pill
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(accent.copy(alpha = 0.35f))
                .blur(16.dp)
        ) {}

        androidx.compose.material3.Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = onAccent,
                disabledContainerColor = Color.Transparent.copy(alpha = 0.3f),
                disabledContentColor = onAccent.copy(alpha = 0.5f)
            ),
            contentPadding = contentPadding,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accent),
        ) {
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accent.copy(alpha = 0.25f),
                                accent.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(0.5.dp, accent.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, style = style)
            }
        }
    }
}


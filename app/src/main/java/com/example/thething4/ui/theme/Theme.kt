package com.example.thething4.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF002B36),
    background = Color(0xFF101014),
    onBackground = Color(0xFFE2E8F0)
)

@Composable
fun CosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CosColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

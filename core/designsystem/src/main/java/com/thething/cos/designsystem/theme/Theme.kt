package com.thething.cos.designsystem.theme

import android.graphics.Color.parseColor
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.thething.cos.designsystem.tokens.UiTokenProvider
import com.thething.cos.designsystem.tokens.UiTokens
import com.thething.cos.designsystem.tokens.LocalUiTokens

private fun UiTokens.toColorScheme(): ColorScheme {
    fun c(hex: String) = Color(parseColor(hex))
    val bg = c(palette.bgPrimary)
    val surface = c(palette.bgElevated)
    val primary = c(palette.accentCyan)
    val onPrimary = c(palette.textHigh)
    val onSurface = c(palette.textMedium)
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.15f),
        onPrimaryContainer = onPrimary,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primary.copy(alpha = 0.1f),
        onSecondaryContainer = onPrimary,
        tertiary = primary,
        onTertiary = onPrimary,
        tertiaryContainer = primary.copy(alpha = 0.08f),
        onTertiaryContainer = onPrimary,
        error = Color(0xFFEF5350),
        onError = Color(0xFF000000),
        errorContainer = Color(0x22EF5350),
        onErrorContainer = Color(0xFFEF5350),
        background = bg,
        onBackground = onPrimary,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surface.copy(alpha = 0.7f),
        onSurfaceVariant = onSurface,
        outline = primary.copy(alpha = 0.5f),
        outlineVariant = primary.copy(alpha = 0.25f),
        scrim = Color(0x99000000),
        inverseOnSurface = onPrimary,
        inverseSurface = bg,
        inversePrimary = primary
    )
}

private fun UiTokens.toTypography(): Typography {
    return Typography(
        displayLarge = TextStyle(fontSize = typography.displaySp.sp),
        titleMedium = TextStyle(fontSize = typography.titleSp.sp),
        bodyMedium = TextStyle(fontSize = typography.bodySp.sp),
        labelLarge = TextStyle(fontSize = typography.buttonSp.sp)
    )
}

@Composable
fun CosTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val tokens = remember { UiTokenProvider.load(context) }

    val colors = remember(tokens) { tokens.toColorScheme() }
    val typo = remember(tokens) { tokens.toTypography() }

    CompositionLocalProvider(LocalUiTokens provides tokens) {
        MaterialTheme(
            colorScheme = colors,
            typography = typo,
            content = content
        )
    }
}

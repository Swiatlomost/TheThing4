package com.example.cos.designsystem.tokens

import android.content.Context
import androidx.annotation.RawRes
import com.example.cos.designsystem.R
import org.json.JSONObject
import androidx.compose.runtime.staticCompositionLocalOf

data class Palette(
    val bgPrimary: String,
    val bgElevated: String,
    val textHigh: String,
    val textMedium: String,
    val accentCyan: String,
    val accentCyanWeak: String,
)

data class Glow(
    val radiusDp: Int,
    val intensity: Double,
    val blurDp: Int,
    val strokeDp: Int,
    val haloWidthMult: Double? = null,
    val haloAlpha: Double? = null,
)

data class Cell(
    val ringStrokeDp: Int,
    val ringShadowBlurDp: Int,
    val ringShadowIntensity: Double,
)

data class Typography(
    val displaySp: Int,
    val titleSp: Int,
    val bodySp: Int,
    val buttonSp: Int,
)

data class Anim(
    val birthMs: Int,
    val birthEase: String,
    val matureMs: Int,
    val matureEase: String,
)

data class ProgressBar(
    val heightDp: Int,
    val glowIntensity: Double,
)

data class UiTokens(
    val version: String,
    val palette: Palette,
    val glow: Glow,
    val cell: Cell,
    val typography: Typography,
    val animation: Anim,
    val progress: ProgressBar,
)

val LocalUiTokens = staticCompositionLocalOf<UiTokens> {
    error("UiTokens not provided")
}

object UiTokenProvider {
    fun load(context: Context, @RawRes resId: Int = R.raw.ui_tokens): UiTokens {
        val json = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        fun palette(): Palette {
            val o = root.getJSONObject("palette")
            return Palette(
                bgPrimary = o.getString("bg-primary"),
                bgElevated = o.getString("bg-elevated"),
                textHigh = o.getString("text-high"),
                textMedium = o.getString("text-medium"),
                accentCyan = o.getString("accent-cyan"),
                accentCyanWeak = o.getString("accent-cyan-weak"),
            )
        }

        fun glow(): Glow {
            val o = root.getJSONObject("glow")
            return Glow(
                radiusDp = o.getInt("radius-dp"),
                intensity = o.getDouble("intensity"),
                blurDp = o.getInt("blur-dp"),
                strokeDp = o.getInt("stroke-dp"),
                haloWidthMult = o.optDouble("halo-width-mult", Double.NaN).let { if (it.isNaN()) null else it },
                haloAlpha = o.optDouble("halo-alpha", Double.NaN).let { if (it.isNaN()) null else it },
            )
        }

        fun cell(): Cell {
            val o = root.getJSONObject("cell")
            return Cell(
                ringStrokeDp = o.getInt("ring-stroke-dp"),
                ringShadowBlurDp = o.getInt("ring-shadow-blur-dp"),
                ringShadowIntensity = o.getDouble("ring-shadow-intensity"),
            )
        }

        fun typo(): Typography {
            val o = root.getJSONObject("typography")
            return Typography(
                displaySp = o.getInt("display-sp"),
                titleSp = o.getInt("title-sp"),
                bodySp = o.getInt("body-sp"),
                buttonSp = o.getInt("button-sp"),
            )
        }

        fun anim(): Anim {
            val o = root.getJSONObject("animation")
            return Anim(
                birthMs = o.getInt("birth-ms"),
                birthEase = o.getString("birth-ease"),
                matureMs = o.getInt("mature-ms"),
                matureEase = o.getString("mature-ease"),
            )
        }

        fun progress(): ProgressBar {
            val o = root.getJSONObject("progress")
            return ProgressBar(
                heightDp = o.getInt("height-dp"),
                glowIntensity = o.getDouble("glow-intensity"),
            )
        }

        return UiTokens(
            version = root.getString("version"),
            palette = palette(),
            glow = glow(),
            cell = cell(),
            typography = typo(),
            animation = anim(),
            progress = progress(),
        )
    }
}

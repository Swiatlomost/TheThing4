package com.example.cos.designsystem.tokens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiTokensInstrumentedTest {
    @Test
    fun load_tokens_from_raw_and_validate() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val tokens = UiTokenProvider.load(context)

        // Basic sanity
        assertTrue(tokens.version.startsWith("0.1"))

        // Palette values
        assertEquals("#FF31C7F7", tokens.palette.accentCyan)
        assertEquals(12, tokens.glow.radiusDp)
        assertEquals(400, tokens.animation.birthMs)
        assertEquals(600, tokens.animation.matureMs)

        // Ranges
        assertTrue(tokens.glow.intensity in 0.0..1.0)
        assertTrue(tokens.progress.glowIntensity in 0.0..1.0)
    }
}


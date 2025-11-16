package com.thething.cos.lightledger

/**
 * Placeholder interface for native Light Ledger bridge.
 * Native integration will be provided by the Rust module (POI-201).
 */
object LightLedgerNative {
    private val loaded = try {
        System.loadLibrary("light_ledger")
        true
    } catch (_: UnsatisfiedLinkError) {
        false
    }

    external fun isAvailable(): Boolean
    external fun hashFingerprintPayload(payload: ByteArray): ByteArray

    fun nativeReady(): Boolean = loaded && runCatching { isAvailable() }.getOrElse { false }
}

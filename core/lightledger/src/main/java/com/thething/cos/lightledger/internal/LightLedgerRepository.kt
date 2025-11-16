package com.thething.cos.lightledger.internal

import android.content.Context
import com.thething.cos.lightledger.model.SessionFingerprint
import java.io.File
import java.util.Base64

/**
 * Minimal persistence layer for the Light Ledger prototype.
 *
 * Stores hash chain entries as Base64 lines on disk and exposes the current
 * Merkle root. Not intended for production use.
 */
class LightLedgerRepository(context: Context) {

    private val directory: File = File(context.filesDir, "light-ledger").apply {
        if (!exists()) mkdirs()
    }
    private val chainFile: File = File(directory, "hash-chain.txt")
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()
    private val publicKeyBase64: String

    init {
        LightLedgerSigner.ensureKeyReady(context)
        publicKeyBase64 = LightLedgerSigner.publicKeyBase64()
            ?: throw IllegalStateException("Light Ledger signer public key unavailable.")
    }

    @Synchronized
    fun appendFingerprint(fingerprint: SessionFingerprint): LedgerSnapshot {
        val entries = readEntriesMutable()
        val hashBytes = LightLedgerHasher.hashFingerprint(fingerprint)
        val hashBase64 = encoder.encodeToString(hashBytes)
        val signatureBytes = LightLedgerSigner.sign(hashBytes)
        val signatureBase64 = encoder.encodeToString(signatureBytes)
        val entry = LedgerEntry(
            index = entries.size,
            hashBase64 = hashBase64,
            signatureBase64 = signatureBase64,
            timestampMillis = System.currentTimeMillis(),
            signerPublicKeyBase64 = publicKeyBase64
        )
        chainFile.appendText(
            "${entry.timestampMillis}|${entry.hashBase64}|${entry.signatureBase64}|${entry.signerPublicKeyBase64}\n"
        )
        entries.add(entry)
        val leaves = entries.map { decoder.decode(it.hashBase64) }
        val merkleRoot = MerkleTree.computeRoot(leaves)
        return LedgerSnapshot(
            latestEntry = entry,
            merkleRootBase64 = encoder.encodeToString(merkleRoot),
            totalLeaves = leaves.size
        )
    }

    @Synchronized
    fun entries(): List<LedgerEntry> = readEntriesMutable()

    @Synchronized
    fun merkleRoot(): String? {
        val entries = readEntriesMutable()
        if (entries.isEmpty()) return null
        val leaves = entries.map { decoder.decode(it.hashBase64) }
        return encoder.encodeToString(MerkleTree.computeRoot(leaves))
    }

    @Synchronized
    fun clear() {
        if (chainFile.exists()) {
            chainFile.writeText("")
        }
    }

    private fun readEntriesMutable(): MutableList<LedgerEntry> {
        if (!chainFile.exists()) return mutableListOf()
        return chainFile.readLines()
            .filter { it.isNotBlank() }
            .mapIndexedNotNull { index, line -> line.toEntry(index) }
            .toMutableList()
    }

    private fun String.toEntry(index: Int): LedgerEntry? {
        val parts = split("|")
        if (parts.size < 2) return null
        val timestamp = parts[0].toLongOrNull() ?: return null
        val hash = parts[1]
        val signature = parts.getOrNull(2) ?: ""
        val signerKey = parts.getOrNull(3) ?: publicKeyBase64
        return LedgerEntry(index, hash, signature, timestamp, signerKey)
    }

    data class LedgerEntry(
        val index: Int,
        val hashBase64: String,
        val signatureBase64: String,
        val timestampMillis: Long,
        val signerPublicKeyBase64: String
    )

    data class LedgerSnapshot(
        val latestEntry: LedgerEntry,
        val merkleRootBase64: String,
        val totalLeaves: Int
    )
}

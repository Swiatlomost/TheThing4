package com.thething.cos.lightledger.internal

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64

/**
 * Handles hardware-backed key provisioning and signing for Light Ledger entries.
 *
 * Prefer StrongBox / TitanM when available, with automatic fallback to a standard
 * Android Keystore key if hardware backing is not supported on the device.
 */
object LightLedgerSigner {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "poi_light_ledger_signer"
    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    private const val EC_CURVE = "secp256r1"

    @Synchronized
    fun ensureKeyReady(context: Context) {
        val keyStore = loadKeyStore()
        if (keyStore.containsAlias(KEY_ALIAS)) return
        generateKey(context)
    }

    fun sign(payload: ByteArray): ByteArray {
        val entry = loadPrivateKeyEntry()
            ?: throw IllegalStateException("Light Ledger signing key not provisioned.")
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(entry.privateKey)
        signature.update(payload)
        return signature.sign()
    }

    fun verify(payload: ByteArray, signatureBytes: ByteArray): Boolean {
        val certificate = loadKeyStore().getCertificate(KEY_ALIAS) ?: return false
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initVerify(certificate.publicKey)
        signature.update(payload)
        return runCatching { signature.verify(signatureBytes) }.getOrDefault(false)
    }

    fun publicKeyBase64(): String? {
        val certificate = loadKeyStore().getCertificate(KEY_ALIAS) ?: return null
        return Base64.getEncoder().encodeToString(certificate.publicKey.encoded)
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun loadPrivateKeyEntry(): KeyStore.PrivateKeyEntry? =
        loadKeyStore().getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry

    private fun generateKey(context: Context) {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
        val strongBoxPreferred = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

        if (strongBoxPreferred) {
            runCatching {
                generator.initialize(buildSpec(strongBox = true))
                generator.generateKeyPair()
            }.onSuccess { return }
        }

        generator.initialize(buildSpec(strongBox = false))
        generator.generateKeyPair()
    }

    private fun buildSpec(strongBox: Boolean): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .setUserAuthenticationRequired(false)

        if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }
        return builder.build()
    }
}

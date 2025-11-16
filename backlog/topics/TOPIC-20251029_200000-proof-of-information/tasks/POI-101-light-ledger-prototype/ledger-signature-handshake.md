# Light Ledger Signature Handshake

Reference guide for Kai/Nodus to fetch the Light Ledger signing artefacts and verify signatures end-to-end.

## Data Produced On Device

Each ledger append writes a line to `filesDir/light-ledger/hash-chain.txt` with the following pipe-separated fields:

```
<timestamp_millis>|<hash_base64>|<signature_base64>|<signer_public_key_base64>
```

- `hash_base64` — SHA-256 digest of the session fingerprint payload (Base64).
- `signature_base64` — ECDSA signature in DER format (Base64) calculated with `SHA256withECDSA` over the digest bytes.
- `signer_public_key_base64` — X.509 SubjectPublicKeyInfo (Base64) for the device signing key (StrongBox preferred, Keystore fallback).

## Capturing Evidence (Debug build)

```
./gradlew installDebug
adb shell run-as com.thething.cos cat files/light-ledger/hash-chain.txt
adb logcat -d | grep LightLedgerRuntimeSmoke
```

Sample log output produced by the debug runtime smoke (`LightLedgerRuntimeSmoke.verify`):

```
I LightLedgerRuntimeSmoke: Merkle root demo: leaves=1, root=qYQz...==
I LightLedgerRuntimeSmoke: Ledger signature valid=true
I LightLedgerRuntimeSmoke: Ledger hash (Base64 SHA-256): 0hJAzX9V...
I LightLedgerRuntimeSmoke: Ledger signature (Base64 DER): MEQCIF7P...
I LightLedgerRuntimeSmoke: Ledger signer public key (Base64 X.509): MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...
```

Copy the three Base64 values (`hash`, `signature`, `signer public key`) for downstream verification.

## Validator-side Verification Procedure

The validator should re-run an ECDSA verification using the hash bytes as the message and the provided DER signature:

```kotlin
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import java.util.Base64

fun verifyLedgerSignature(
    hashBase64: String,
    signatureBase64: String,
    signerPublicKeyBase64: String
): Boolean {
    val hashBytes = Base64.getDecoder().decode(hashBase64)
    val signatureBytes = Base64.getDecoder().decode(signatureBase64)
    val publicKeyBytes = Base64.getDecoder().decode(signerPublicKeyBase64)

    val keyFactory = KeyFactory.getInstance("EC")
    val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))

    val signature = Signature.getInstance("SHA256withECDSA")
    signature.initVerify(publicKey)
    signature.update(hashBytes)
    return signature.verify(signatureBytes)
}
```

Notes:

- The validator receives the already hashed payload (32 bytes). The `SHA256withECDSA` engine hashes the provided message internally which matches the on-device signing flow.
- All fields are ASCII-safe and transmitted as Base64 strings.

## Deliverables

- Public key Base64 string surfaced via debug log (`LightLedgerRuntimeSmoke`) and stored alongside each ledger entry.
- Procedure above ready to integrate into the validator service (Nodus) and QA harness (Kai).

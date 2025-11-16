# Light Ledger Module (Prototype)

Minimal Kotlin/Rust bridge for hashing Proof of Information session fingerprints, signing them with Android Keystore (StrongBox when available) and persisting a hash chain on device. Built for the PoI spike - **not production ready**.

## Components

| Path | Description |
| --- | --- |
| `internal/LightLedgerHasher.kt` | Normalises fingerprints, hashes via JNI (cargo-ndk) with Kotlin fallback. |
| `internal/LightLedgerSigner.kt` | Provisions ECDSA key in Android Keystore / StrongBox and signs ledger entries. |
| `internal/MerkleTree.kt` | SHA-256 Merkle root helper for the hash chain. |
| `internal/LightLedgerRepository.kt` | Persists hash-chain entries (`timestamp|hash|signature|publicKey`) and exposes Merkle snapshots. |
| `internal/LightLedgerBenchmarkRunner.kt` | Debug-only benchmarking harness (JNI vs. Kotlin fallback). |

## Usage

```kotlin
val repository = LightLedgerRepository(context)
val fingerprint = SessionFingerprint(
    sessionId = "session-123",
    motionVector = FloatArray(9),
    touchSignature = "finger",
    envEntropy = 0.82,
    soundVariance = 0.14,
    batteryCurve = "100-95-90-86-82-79",
    trustScore = 88,
    timestampSeconds = System.currentTimeMillis() / 1000
)

val snapshot = repository.appendFingerprint(fingerprint)
snapshot.merkleRootBase64           // current Merkle root (Base64)
snapshot.latestEntry.hashBase64     // hash of the fingerprint (Base64)
snapshot.latestEntry.signatureBase64// ECDSA signature produced via Keystore
```

## Hashing & Signing Policy

- Native hashing path requires **>= 2000** entries per batch; smaller batches fall back to Kotlin (`LightLedgerHasher`).
- One warm-up round precedes the first JNI batch to stabilise timings.
- Hardware-backed signing prefers StrongBox / TitanM; automatic fallback to standard Android Keystore if unavailable (`LightLedgerSigner`).

## Runtime Smoke & Merkle Proof

Debug builds execute `LightLedgerRuntimeSmoke.verify()` during `Application.onCreate`:

```bash
./gradlew installDebug
adb logcat -d | grep LightLedgerRuntimeSmoke
```

Example output:

```
I LightLedgerRuntimeSmoke: Native ledger ready; produced 32-byte digest.
I LightLedgerRuntimeSmoke: Merkle root demo: leaves=1, root=qYQz...==
I LightLedgerRuntimeSmoke: Ledger signature valid=true, signerKey=MFkwEwYHKoZIzj0...
```

This satisfies the PoI-101 acceptance proof that the module generates and verifies Merkle roots using a hardware-backed signature.

Detailed steps for sharing the public key and validating signatures live in `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-101-light-ledger-prototype/ledger-signature-handshake.md`.

## Build & Benchmark

```bash
# Build native libs + app (requires cargo-ndk)
./gradlew :core:lightledger:cargoBuildLightLedgerArm64 \
          :core:lightledger:cargoBuildLightLedgerX86 \
          :app:assembleDebug -PenableCargoNdk=true

# Install & run smoke/benchmark (DEBUG build)
./gradlew installDebug
adb logcat -d | grep LightLedgerBenchmark
```

Benchmark results and observations live in `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-101-light-ledger-prototype/benchmark-report.md`.

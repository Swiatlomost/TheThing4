## JNI Bridge & Performance Plan

### JNI / Rust Integration
- Generate Kotlin bindings in `LightLedgerNative.kt` with JNI signatures:
  ```kotlin
  external fun buildMerkleRoot(entries: Array<LedgerEntryNative>): MerkleRootNative
  external fun computeEntropy(values: FloatArray): Float
  ```
- Use `jni::objects::JClass` wrappers in Rust; expose safe APIs via `#[no_mangle]` functions.
- Memory strategy: pass byte arrays (SHA-256 hashes) and reuse direct `ByteBuffer` to avoid copies.
- Error handling: map Rust `anyhow::Error` -> Kotlin `LedgerNativeException`.

### Build System
- Add Gradle `externalNativeBuild` with Cargo via `cargo-ndk`.
- Configure CI job to compile Rust `.so` for arm64-v8a + x86_64.

### Performance Tests
- Benchmark hashing/merkle on Pixel 5 & Pixel 7:
  - Target ≤150 ms for 256 entries.
  - Monitor CPU usage (adb `dumpsys batterystats`).
  - Record FAR/FRR telemetry dispatch latency.
- Stress test coroutine scheduling with WorkManager (background batching).

### Deliverables
1. JNI bridge module skeleton.
2. Benchmark script (`scripts/benchmark_merkle.py` or instrumentation test).
3. Report capturing CPU/Battery metrics appended to task log.

### Next Actions
- Scaffold JNI package structure in app module.
- Draft instrumentation test harness for batching workloads.

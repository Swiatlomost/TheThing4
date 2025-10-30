## Light Ledger Component Sketch

### Kotlin Layer
- `LightLedgerRepository`
  - Persists sessions into `sessions` table and manages hash-chain entries.
  - Interface:
    ```kotlin
    interface LightLedgerRepository {
        suspend fun appendSession(session: SessionFingerprint): LedgerEntry
        suspend fun listPendingEntries(limit: Int = 256): List<LedgerEntry>
        suspend fun markConfirmed(ids: List<Long>)
    }
    ```
- `MerkleBatcher`
  - Aggregates pending ledger entries, builds Merkle tree via Rust FFI.
  - Emits `BatchProof` data class with FAR/FRR metrics.

### Rust (NDK) Module
- `merkle::Builder`
  - Functions:
    ```rust
    pub fn build_root(entries: &[LedgerEntryFFI]) -> Result<MerkleRoot, Error>;
    pub fn compute_entropy(scores: &[f32]) -> f32;
    ```
- Compiled as `.so`, loaded via JNI; includes unit tests for tree correctness.

### Data Flow
1. `SessionCollector` assembles sensor fingerprint → calls `LightLedgerRepository.appendSession`.
2. `MetricsTracker` updates rolling FAR/FRR counters and logs telemetry for Kai.
3. Scheduler triggers `MerkleBatcher` every 15 min or when 256 entries queued.
4. Batch proof serialized to protobuf and handed to `BatchUploader`.

### Interfaces to Validator
- `BatchUploader` uses gRPC stub generated from `validator.proto`.
- Retries with exponential backoff; stores submission status in `ledger_chain`.

### Testing Strategy
- JVM unit tests mocking Rust FFI to verify repo behaviour.
- Instrumentation test measuring CPU/battery on Pixel 5 using WorkManager scheduler.

### Pending Tasks
- Implement JNI bridge data classes (Rust ↔ Kotlin).
- Decide on coroutine context for hashing (Dispatchers.Default).

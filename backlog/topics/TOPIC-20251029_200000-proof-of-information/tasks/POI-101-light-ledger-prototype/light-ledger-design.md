## Light Ledger Prototype – Design Draft

### Storage Model
- **Session table (`sessions`)**
  - `id` (TEXT, primary key) – session UUID.
  - `timestamp` (INTEGER) – epoch seconds for session close.
  - `fingerprint_hash` (BLOB) – SHA-256 of JSON fingerprint payload.
  - `entropy_score` (REAL) – computed entropy 0.0–1.0.
  - `trust_score` (INTEGER) – 0–100 trust metric delivered to validator.
  - `signature` (BLOB) – ECDSA signature using hardware-backed key.
- **Ledger chain table (`ledger_chain`)**
  - `index` (INTEGER, autoincrement) — order of entries.
  - `session_hash` (BLOB) — SHA-256 of session record.
  - `prev_hash` (BLOB) — SHA-256 of previous ledger entry.
  - `created_at` (INTEGER) — epoch seconds.
  - `proof_status` (TEXT) — `pending`, `batched`, `confirmed`.
  - `signature` (BLOB) — ECDSA signature produced via hardware key (StrongBox when available).
  - `signer_public_key` (BLOB) — cached public key corresponding to the signature for validator replay.

### Key Management
- Prefer **Android StrongBox / Titan M** via `KeyGenParameterSpec` with ECDSA P-256.
- Fallback (no StrongBox): software key in Keystore + **Play Integrity attestation** appended to batch.
- Store public key reference + attestation token per device in ledger metadata transmitted to validator.

### Merkle Root Pipeline
1. Collect ledger entries within 15-minute window or when count ≥256.
2. Build Merkle tree using SHA-256; root packaged with:
   ```json
   {
     "device_id": "C0S-00931",
     "batch_start": 1730119200,
     "batch_end": 1730120100,
     "root": "0x...",
     "entropy_avg": 0.82,
     "trust_avg": 93,
     "far": 0.04,
     "frr": 0.09
   }
   ```
3. Sign batch payload with hardware key; include attestation proof.
4. Submit to validator via gRPC (`BatchProof` message).

### Runtime Constraints
- **FAR/FRR Targets**: FAR ≤ 5 %, FRR ≤ 10 % aggregated per batch (alert if exceeded).
- **Entropy Threshold**: discard entries with entropy < 0.75; ring buffer raw data for 15 min for audit.
- **Resource Budget**: CPU < 5 % avg, battery impact < 3 % per hour during batching.

### Cell Lifecycle Link
- Morfogeneza komórek (Birth → Mature → Division) zależy od ilości zebranych i potwierdzonych danych:
  - **Birth unlock**: co najmniej 10 autentycznych fingerprintów w ciągu 30 minut (entropy ≥0.75).
  - **Mature transition**: 100 fingerprintów + średni trust ≥90.
  - **Division**: 1000 fingerprintów w oknie 24h i utrzymany FAR ≤5 %, FRR ≤10 %.
- Progi można kalibrować na podstawie testów (Kai) – wartości startowe do dalszej iteracji.
- Interfejs Morfogenezy powinien otrzymywać agregaty z Light Ledgera, by wizualizować wzrost organizmu proporcjonalnie do danych PoI.

### Data Retention & Pruning
- Upon validator confirmation + Solana inclusion, mark ledger entries as `confirmed`.
- Prune raw sensor buffers after 15 min, keep ledger hashes for 30 days for dispute resolution.
- Maintain optional encrypted archive (device-only) when entropy anomalies detected.

### Open Questions
- Need confirmation on on-device storage pruning policy after blockchain confirmation.
- Evaluate optional use of **Rust NDK** module for hashing if Kotlin hashing insufficient.
- Define Kotlin interface vs. Rust FFI boundary for Merkle builder (class diagram pending).
- Validate battery/performance assumptions on mid-range hardware (Pixel 5 baseline).

### Review Notes (Vireal & Kai)
- Verified schema satisfies guardrails; ensure `proof_status` transitions logged for audit (Kai).
- Add telemetry hook emitting FAR/FRR per batch to validator (Vireal).
- Confirm attestation token cached with batch payload for validator replay protection.

## PoI Architecture Research – Guardrails Draft

### Technology Matrix
| Layer | Options Evaluated | Pros | Cons | Recommendation |
|-------|-------------------|------|------|----------------|
| On-device Trust Anchor | ARM TrustZone + Android Keystore, Intel SGX (select OEMs), TPM-backed keys | TrustZone widely available; strongbox integration; moderate cost | SGX limited to x86, higher power draw | **Use TrustZone/StrongBox where available; fall back to software key with attestation token** |
| Federated Orchestration | TensorFlow Federated (TFF), OpenFL, NVIDIA FLARE | TFF native to Python, strong DP ecosystem; OpenFL has attestation plugins | TFF requires custom scaling; OpenFL heavier infra | **Adopt TFF + secure aggregation (Bonawitz) with optional OpenFL plugins for attestation** |
| Secure Aggregation | Bonawitz protocol (reference impl), Google SecAgg, Falkor | Proven at >1k clients; supports dropout | Requires cryptographic primitives, complexity | **Implement Bonawitz SecAgg via open-source TFF extensions** |
| Validator Pipeline | Rust (Actix/Tonic), Go (gRPC), Node.js (Nest) | Rust: safety, performance; Go: quick dev; Node: ecosystem | Rust ramp-up cost, Go GC jitter | **Prototype in Rust for deterministic crypto + async** |
| Blockchain Layer | Solana, Sui, Polygon zkEVM, Substrate parachain | Solana high TPS + Rust tooling; Sui stable gas + object model; Polygon zkEVM cheaper fees; Substrate customizable | Solana requires Rust BPF + history of outages; Sui needs Move expertise + shared-object latency; Polygon fees fluctuate; Substrate ops overhead | **Primary: Solana devnet → mainnet; Secondary: evaluate Sui spike for object storage/zkLogin; keep Substrate fallback** |

### Guardrails & Decisions
1. **Device Layer**  
   - All PoI fingerprints signed with hardware-backed keys when StrongBox available; log fallback cases.  
   - Require attestation token (Play Integrity / SafetyNet) before accepting Merkle root.

2. **Federated Proof Layer**  
   - Enforce secure aggregation with client dropout tolerance ≥20 %; reject updates missing attestation.  
   - Clip gradients and attach DP accounting metadata (ε, δ) per batch.

3. **Validator Service**  
   - Stateless microservices in Rust, one per availability zone; HSM-backed root key for signing validator attestations.  
   - Batch Merkle roots ≤15 min interval; store raw proofs in append-only PostgreSQL ledger.

4. **Blockchain Commitments**  
- Solana program records `{root, trust, entropy, validator_sig}`; include replay protection nonce.  
- Provide adapter API for future zk-proof verification; keep proofs sha256-compatible.
- Maintain Sui spike branch: prototype Move module storing PoI root object history; evaluate zkLogin for device attestation.

5. **Observability & Resilience**  
   - Emit metrics: attested devices, failed DP budget, Merkle batch latency, chain confirmation time.  
   - Fallback: if blockchain unavailable >30 min, queue batches with signed timestamp and notify Orin/Nodus.

### Dependencies & Follow-up
- **Attestation**: Coordinate with Nodus to provision Play Integrity / SafetyNet keys and fallback offline cache.  
- **HSM/Key Mgmt**: Nodus to select cloud HSM (AWS CloudHSM / GCP CloudKMS) for validator signing.  
- **Blockchain Ops**: Lumen/Nodus to script Solana devnet deployment + gas monitoring.  
- **QA Alignment**: Kai to incorporate guardrails into PoI test-plan (failure modes, metrics).

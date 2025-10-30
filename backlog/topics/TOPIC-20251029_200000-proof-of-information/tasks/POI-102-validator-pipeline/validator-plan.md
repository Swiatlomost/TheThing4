## Validator & Integration Plan

### HSM / Key Management
- **Primary**: AWS CloudHSM cluster (cm-small) – provides dedicated hardware and integrates with Rust via PKCS#11.
- **Fallback**: GCP CloudKMS with EKM connector; slightly higher latency but simpler ops.
- Rotate validator signing key every ≤30 dni; store public key fingerprints in config repo.

### Attestation Flow
1. Device includes Play Integrity verdict + nonce in batch payload.
2. Validator verifies verdict using Google REST API (cached service account credential).
3. If StrongBox unavailable, ensure nonce ties to ledger batch and store audit trail in PostgreSQL.

### gRPC API Draft
```
service PoIValidator {
  rpc SubmitBatch (BatchProofRequest) returns (BatchProofResponse);
  rpc QueryStatus (BatchStatusRequest) returns (BatchStatusResponse);
}
```
- `BatchProofRequest` includes Merkle root, entropy/trust metrics, signature, attestation proof.
- Response returns batch ID, status (`accepted`, `rejected`, `queued`), and expected chain slot.

### Solana Devnet Tasks
- Deploy PoI program using Anchor; record `{root, trust_avg, entropy_avg, validator_sig}`.
- Set batching interval 15 min; ensure compute units < 200k per transaction.
- Configure monitoring: Solana RPC health, transaction confirmation time, fee tracker.

### Integration Checklist
- [ ] Provision CloudHSM cluster and PKCS#11 client in staging.
- [ ] Implement attestation verifier service + caching.
- [ ] Build Rust validator service skeleton (Actix/Tonic).
- [ ] Write Solana Anchor program + tests on devnet.
- [ ] Set up observability dashboard (Grafana) for batch metrics.
- [ ] Document recovery procedure if Solana outage >30 min (queue & re-submit).

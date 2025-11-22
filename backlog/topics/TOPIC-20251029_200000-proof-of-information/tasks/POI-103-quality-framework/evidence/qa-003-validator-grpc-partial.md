# QA-003 Evidence: Validator gRPC Responses (Partial)

**Scenario:** POI-QA-003 - Validator to Solana devnet pipeline
**Metrics:** `batch_interval: 15min`, `confirmation_time: ≤30s`
**Date collected:** 2025-11-22
**Status:** ⚠️ PARTIAL (Solana integration pending POI-205)

---

## Summary

Validator gRPC service operational and accepting batches. Evidence shows successful gRPC request/response cycle from Android client to validator. **Solana transaction submission not yet implemented** (POI-205 pending).

**Samples collected:** 3 successful gRPC responses (STATUS_ACCEPTED)

---

## Evidence Samples

### Sample 1: POI-212 (WorkManager Upload)

**Client request (logcat):**
```
11-16 13:48:30.558 I BatchUploadWorker: Work attempt=0 hash=9MKgPqdd device=23078PND5G
```

**Validator processing (logs):**
```
2025-11-16T12:48:32.135767Z INFO Received batch: device_id=23078PND5G, entries=1, merkle_root=9MKgPqddiELEj1xAf9sl2ncfHSOgBLgNk3IvZgeudmQ=
2025-11-16T12:48:32.137834Z INFO Batch validated: device_id=23078PND5G, merkle_root=9MKgPqddiELEj1xAf9sl2ncfHSOgBLgNk3IvZgeudmQ=
```

**Client response (logcat):**
```
11-16 13:48:32.082 I BatchUploader: Upload result: status=STATUS_ACCEPTED reason=validated batch=1a7acb65-e3d8-4e10-8b8e-66bb89d86275
```

**gRPC Response structure (inferred):**
```json
{
  "status": "STATUS_ACCEPTED",
  "reason": "validated",
  "batch_id": "1a7acb65-e3d8-4e10-8b8e-66bb89d86275",
  "merkle_root": "9MKgPqddiELEj1xAf9sl2ncfHSOgBLgNk3IvZgeudmQ=",
  "solana_tx": null,
  "timestamp": "2025-11-16T13:48:32.082Z"
}
```

**Processing time:** ~1.5s (30.558 → 32.082)

---

### Sample 2: POI-212 (Second Upload)

**Validator processing (logs):**
```
2025-11-16T12:52:10.754144Z INFO Received batch: device_id=23078PND5G, entries=1, merkle_root=VJ3Oy7KoVVrdmLNXN/HXP+38bY2tbGgW1GPdLmhpbSs=
2025-11-16T12:52:10.756865Z INFO Batch validated: device_id=23078PND5G, merkle_root=VJ3Oy7KoVVrdmLNXN/HXP+38bY2tbGgW1GPdLmhpbSs=
```

**gRPC Response (inferred):**
```json
{
  "status": "STATUS_ACCEPTED",
  "reason": "validated",
  "batch_id": "<generated-uuid>",
  "merkle_root": "VJ3Oy7KoVVrdmLNXN/HXP+38bY2tbGgW1GPdLmhpbSs=",
  "solana_tx": null,
  "timestamp": "2025-11-16T12:52:10.756Z"
}
```

**Validator processing time:** ~2ms (754.144 → 756.865)

---

### Sample 3: POI-213 (TLS Validator, Release Build)

**Source:** POI-213 log.jsonl (2025-11-22)

**Evidence:**
> "Deployed to validator.poi-lab.pl - release build verified **STATUS_ACCEPTED**."

**gRPC Response (inferred):**
```json
{
  "status": "STATUS_ACCEPTED",
  "reason": "validated",
  "batch_id": "<generated-uuid>",
  "merkle_root": "<merkle-root-base64>",
  "solana_tx": null,
  "timestamp": "2025-11-22T<timestamp>Z"
}
```

**Context:** TLS-enabled validator, OAuth-authenticated Play Integrity, nonce padding fix applied

---

## gRPC Service Definition

**Proto file:** `validator/proto/validator.proto`

**Service method (inferred):**
```protobuf
service PoIValidator {
  rpc SubmitBatch(BatchRequest) returns (BatchResponse);
}

message BatchRequest {
  string device_id = 1;
  bytes merkle_root = 2;
  repeated bytes entries = 3;
  string attestation_token = 4;  // Play Integrity JWT
  string nonce = 5;
}

message BatchResponse {
  enum Status {
    STATUS_UNSPECIFIED = 0;
    STATUS_ACCEPTED = 1;
    STATUS_REJECTED = 2;
  }

  Status status = 1;
  string reason = 2;  // "validated" or rejection reason
  string batch_id = 3;  // UUID
  string solana_tx = 4;  // ⚠️ null (POI-205 pending)
  int64 timestamp = 5;
}
```

**Note:** Actual proto definition may differ - this is inferred from client/validator logs.

---

## Metrics (Partial)

| Metric | Target (QA-003) | Observed | Status |
|--------|----------------|----------|--------|
| **Batch interval** | 15 minutes | N/A (manual tests) | ⏳ Deferred to POI-204 |
| **Validator response time** | N/A | ~2ms (server-side) | ✅ Fast |
| **gRPC round-trip** | N/A | ~1.5s (client-side, includes Play Integrity) | ✅ Within SLA |
| **Solana confirmation** | ≤30s (p95) | ❌ Not implemented | ⏳ Blocked by POI-205 |

---

## Limitations (Partial Evidence)

**Missing (blocked by POI-205):**
1. ❌ Solana transaction hash in gRPC response
2. ❌ Solana confirmation time measurement
3. ❌ On-chain verification (devnet/mainnet)
4. ❌ Retry logic for Solana outages

**Available:**
1. ✅ Validator gRPC service operational
2. ✅ Batch validation successful (Play Integrity + Merkle root)
3. ✅ gRPC response structure (STATUS_ACCEPTED/REJECTED)
4. ✅ Fast validator processing (~2ms server-side)

---

## Next Steps (POI-205 Required)

**To complete QA-003 evidence:**

1. **Implement Solana integration (POI-205):**
   - Validator submits Merkle root to Solana devnet
   - Return `solana_tx` (transaction signature) in gRPC response
   - Implement retry queue for Solana outages (60min retention)

2. **Update gRPC response:**
   ```json
   {
     "status": "STATUS_ACCEPTED",
     "reason": "validated_and_submitted",
     "batch_id": "1a7acb65-e3d8-4e10-8b8e-66bb89d86275",
     "merkle_root": "9MKgPqddiELEj1xAf9sl2ncfHSOgBLgNk3IvZgeudmQ=",
     "solana_tx": "5J7... (base58 signature)",  // ← NEW
     "solana_slot": 123456789,  // ← NEW
     "timestamp": "2025-11-16T13:48:32.082Z"
   }
   ```

3. **Measure confirmation latency:**
   - Time from gRPC response to Solana `confirmed` commitment
   - Target: p95 ≤ 30s
   - Metric: `poi_solana_confirmation_seconds` (histogram)

4. **Verify on-chain:**
   ```bash
   solana transaction <tx-sig> --url devnet
   # Verify Merkle root appears in transaction data
   ```

5. **Collect 100+ samples:**
   - Batch interval: 15min
   - Measure confirmation time distribution
   - Document any Solana network issues

---

## References

- **Source logs:**
  - [POI-212 validator logs](../../POI-212-android-uploader/evidence/validator-logs-2025-11-16-workmanager.txt)
  - [POI-212 logcat](../../POI-212-android-uploader/evidence/logcat-2025-11-16-workmanager.txt)
  - [POI-213 log.jsonl](../../POI-213-validator-tls/log.jsonl)
- **Test plan:** [test-plan.json](../test-plan.json) (POI-QA-003)
- **SLA:** [sla-definition.md](../sla-definition.md) (Blockchain confirmation section)
- **Blocked by:** POI-205 (Solana Integration - pending)

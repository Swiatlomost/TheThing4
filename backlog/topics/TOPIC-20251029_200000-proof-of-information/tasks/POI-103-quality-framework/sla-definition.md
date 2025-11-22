# PoI System SLA (v1.0 - Baseline)

**Owner:** Kai
**Status:** Draft (requires production validation)
**Last updated:** 2025-11-22

## Overview

This document defines Service Level Agreements for the Proof of Information pipeline, covering device-to-validator uploads, data quality thresholds, and blockchain confirmation targets.

---

## 1. Upload Success Rate (Device → Validator)

**Target:** ≥99%

**Definition:** Percentage of upload attempts that result in `STATUS_ACCEPTED` from the validator.

**Measurement:**
```
success_rate = (STATUS_ACCEPTED_count / total_upload_attempts) × 100
```

**Monitoring:**
- Metric: `poi_upload_success_rate` (gauge, 30-day rolling window)
- Alert: `< 99%` → Page on-call (PagerDuty)

**Source:** Based on Play Integrity `attestation_success_rate ≥0.99` (test-plan.json QA-002)

---

## 2. Upload Latency (LightLedger finalize → STATUS_ACCEPTED)

**Targets:**
- **p50:** ≤3s
- **p95:** ≤5s
- **p99:** ≤10s

**Measurement stages:**
1. LightLedger session finalization (Merkle root ready)
2. Play Integrity token request
3. Play Integrity token received
4. gRPC upload to validator
5. Validator response (STATUS_ACCEPTED/REJECTED)

**Baseline (2025-11-16 evidence):**
- Single sample: 2.17s end-to-end
- Bottleneck: Play Integrity API (1.4s = 65%)
- Network: Local WiFi, non-production

**Monitoring:**
- Metric: `poi_upload_latency_seconds` (histogram, p50/p95/p99)
- Alert: `p95 > 5s` for 5 minutes → Warning (Slack)
- Alert: `p99 > 10s` for 5 minutes → Page on-call

**⚠️ Note:** Conservative estimates based on 1 sample. Requires validation with production traffic (defer to POI-204).

---

## 3. Validator Uptime

**Target:** ≥99.5%

**Definition:** Percentage of time the validator is reachable and accepting gRPC requests.

**Measurement:**
```
uptime = (uptime_seconds / total_seconds_in_period) × 100
```
- Period: 30-day rolling window
- Excludes: Planned maintenance (max 4h/month, announced 48h advance)

**Monitoring:**
- Metric: `poi_validator_up` (gauge, scraped every 15s)
- Alert: `validator_up == 0` for 1 minute → Page on-call

**Downtime budget:** 3.6 hours/month (99.5% of 720h)

---

## 4. Data Quality (Device Sensor Data)

**Thresholds (from test-plan.json QA-001):**

| Metric | Threshold | Description |
|--------|-----------|-------------|
| **FAR** (False Acceptance Rate) | ≤5% | Max rate of accepting spoofed/low-quality data |
| **FRR** (False Rejection Rate) | ≤10% | Max rate of rejecting legitimate data |
| **Entropy** | ≥0.75 | Min entropy per session (normalized Shannon entropy) |

**Measurement:**
- Entropy calculated per-session by Light Ledger
- FAR/FRR validated via synthetic data generator (POI-204)

**Monitoring:**
- Metric: `poi_session_entropy` (histogram)
- Alert: `entropy_avg < 0.75` for 15 minutes → Warning

**Differential Privacy budget:** ε ≤ 2.0 (per test-plan.json)

---

## 5. Blockchain Confirmation (Validator → Solana)

**Targets:**
- **Confirmation time (p95):** ≤30s
- **Batch interval:** 15 minutes
- **Finality:** Solana `confirmed` commitment level

**Measurement:**
- Time from validator batch submission to Solana transaction confirmation
- Excludes: Solana network outages (covered by resilience SLA)

**Monitoring:**
- Metric: `poi_solana_confirmation_seconds` (histogram, p95)
- Alert: `p95 > 30s` for 10 minutes → Warning

**Source:** test-plan.json QA-003

---

## 6. Resilience (Failure Handling)

**Queue retention during Solana outages:**
- **Target:** 60 minutes
- **Behavior:** Validator queues batches in-memory, replays once Solana available
- **Alert:** Queue length > 50 batches → Warning

**Retry policy (gRPC uploads):**
- Exponential backoff: 1s, 2s, 4s, 8s, 16s, 32s, 60s (max)
- Max retries: 5 attempts
- Failure mode: Return `STATUS_REJECTED` to client after exhaustion

**Monitoring:**
- Metric: `poi_validator_queue_length` (gauge)
- Metric: `poi_upload_retries_total` (counter)

**Source:** test-plan.json QA-004

---

## 7. Play Integrity Verdicts

**Accepted verdicts:**
- `MEETS_DEVICE_INTEGRITY` (all Android versions)
- `MEETS_STRONG_INTEGRITY` (Android 13+, stricter)

**Rationale:** Both prove device authenticity. Validator logs verdict type for future trust scoring. Authenticity is priority #1.

**Monitoring:**
- Metric: `poi_play_integrity_verdict` (counter with label `verdict_type`)
- Dashboard: Show distribution of DEVICE vs STRONG verdicts by Android version

---

## Validation & Next Steps

**Current status:**
- ✅ Thresholds defined from test-plan.json (QA-001 to QA-004)
- ✅ Latency baseline captured (1 sample: 2.17s)
- ⚠️ Latency p50/p95/p99 are conservative estimates (not statistically validated)
- 🔜 Requires: Production monitoring implementation (POI-204)

**Production validation plan:**
1. Deploy metrics collection (TimescaleDB + Grafana dashboards)
2. Collect 1000+ samples over 7 days
3. Recalibrate p50/p95/p99 based on real traffic
4. Adjust alerts based on false-positive rate

**Owner for validation:** Kai (POI-204 QA Toolchain implementation)

---

## References

- [test-plan.json](test-plan.json) - QA scenarios and acceptance criteria
- [PDCA.json](PDCA.json) - POI-103 planning and execution tracking
- [latency baseline evidence](../POI-212-android-uploader/evidence/logcat-2025-11-16-workmanager.txt) - 2.17s sample
- [monitoring-strategy.md](monitoring-strategy.md) - Detailed monitoring implementation (Phase 2.1)
- [production-readiness.md](production-readiness.md) - Quality gates and deployment checklist (Phase 2.2)

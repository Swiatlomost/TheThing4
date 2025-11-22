## QA Tooling Notes (Concrete Implementation Plan)

**Owner:** Kai
**Status:** Planning (implementation in POI-204)
**Last updated:** 2025-11-22

---

## 1. Metrics Collection (Prometheus + TimescaleDB)

### Prometheus Metrics (Validator `/metrics` Endpoint)

**Implementation:** Rust `prometheus` crate (v0.13)

**Metrics to expose:**

| Metric Name | Type | Labels | Purpose |
|-------------|------|--------|---------|
| `poi_upload_attempts_total` | Counter | `status`, `rejection_reason` | Success rate calculation |
| `poi_upload_latency_seconds` | Histogram | - | p50/p95/p99 latency tracking |
| `poi_validator_up` | Gauge | - | Uptime monitoring |
| `poi_session_entropy` | Histogram | - | Data quality (entropy ≥0.75) |
| `poi_solana_confirmation_seconds` | Histogram | - | Blockchain confirmation time |
| `poi_validator_queue_length` | Gauge | - | Resilience (Solana outage queue) |
| `poi_upload_retries_total` | Counter | `reason` | Retry rate monitoring |
| `poi_play_integrity_verdict` | Counter | `type`, `android_version` | Verdict distribution analysis |

**Histogram buckets:**
- Latency: `[0.5, 1.0, 2.0, 3.0, 5.0, 10.0]` seconds
- Entropy: `[0.5, 0.6, 0.7, 0.75, 0.8, 0.9, 1.0]`

**Scraping:** Prometheus scrapes `:9090/metrics` every 15 seconds

---

### TimescaleDB (Long-Term Storage)

**Purpose:** 90-day aggregated metrics for trend analysis

**Tables:**
```sql
CREATE TABLE poi_metrics_hourly (
  timestamp TIMESTAMPTZ NOT NULL,
  metric_name TEXT NOT NULL,
  metric_value DOUBLE PRECISION NOT NULL,
  labels JSONB,
  PRIMARY KEY (timestamp, metric_name)
);

SELECT create_hypertable('poi_metrics_hourly', 'timestamp');
SELECT add_retention_policy('poi_metrics_hourly', INTERVAL '90 days');
```

**Ingestion:** Prometheus remote_write to TimescaleDB adapter (hourly aggregation)

---

## 2. Evidence Capture (Automated Scripts)

### Script: `extract_play_integrity_evidence.sh`

**Purpose:** Extract Play Integrity success samples for QA-002

**Implementation:**
```bash
#!/bin/bash
# Extract last 10 successful Play Integrity verifications from validator logs

kubectl logs -n poi poi-validator-xxxxx --tail=10000 \
  | grep "MEETS_DEVICE_INTEGRITY\|MEETS_STRONG_INTEGRITY" \
  | grep "STATUS_ACCEPTED" \
  | tail -10 \
  > evidence/play-integrity-success-$(date +%Y-%m-%d).txt
```

**Output format:**
```
2025-11-22T15:30:45Z INFO Batch validated: device=23078PND5G verdict=MEETS_STRONG_INTEGRITY status=STATUS_ACCEPTED
```

**Trigger:** Run after 7 days of production (collect 1000+ samples)

---

### Script: `extract_validator_grpc_samples.sh`

**Purpose:** Extract validator gRPC responses for QA-003

**Implementation:**
```bash
#!/bin/bash
# Capture gRPC response samples (partial until POI-205 Solana integration done)

kubectl logs -n poi poi-validator-xxxxx --tail=5000 \
  | grep "gRPC response" \
  | jq -r '.status, .batch_id, .merkle_root' \
  | head -20 \
  > evidence/validator-grpc-samples-$(date +%Y-%m-%d).json
```

**Output format (JSON):**
```json
{
  "status": "STATUS_ACCEPTED",
  "batch_id": "1a7acb65-e3d8-4e10-8b8e-66bb89d86275",
  "merkle_root": "9MKgPqddiELEj1xAf9sl2ncfHSOgBLgNk3IvZgeudmQ=",
  "solana_tx": null  // Pending POI-205
}
```

---

### Script: `parse_latency_breakdown.sh`

**Purpose:** Analyze latency breakdown from Android logcat

**Implementation:**
```bash
#!/bin/bash
# Parse logcat to extract stage-by-stage timing

adb logcat -d | grep -E "LightLedger|PlayCore|UploadWorker|STATUS_ACCEPTED" \
  | awk '{print $2, $NF}' \
  | python3 scripts/analyze_latency.py \
  > evidence/latency-breakdown-$(date +%Y-%m-%d).csv
```

**Output (CSV):**
```csv
stage,duration_ms,timestamp
LightLedger_finalize,45,13:48:29.912
Play_Integrity_request,1401,13:48:30.609
gRPC_upload,72,13:48:32.010
Total,2170,13:48:32.082
```

---

## 3. Alerting (Grafana + PagerDuty + Slack)

### Alert Rules (Prometheus AlertManager)

**Critical alerts → PagerDuty:**

| Alert Name | Condition | Duration | Severity |
|------------|-----------|----------|----------|
| `PoI_UploadSuccessRateLow` | `success_rate < 0.99` | 5 minutes | critical |
| `PoI_ValidatorDown` | `poi_validator_up == 0` | 1 minute | critical |
| `PoI_LatencyP99High` | `p99 > 10s` | 5 minutes | critical |

**Warning alerts → Slack (#poi-alerts):**

| Alert Name | Condition | Duration | Severity |
|------------|-----------|----------|----------|
| `PoI_LatencyP95Degraded` | `p95 > 5s` | 5 minutes | warning |
| `PoI_LowEntropyRate` | `avg(entropy) < 0.75` | 15 minutes | warning |
| `PoI_QueueLengthHigh` | `queue_length > 50` | 2 minutes | warning |
| `PoI_HighRetryRate` | `retries/uploads > 0.05` | 10 minutes | warning |

**AlertManager config location:** `monitoring/alertmanager/poi-alerts.yml`

**PagerDuty integration key:** `PAGERDUTY_POI_INTEGRATION_KEY` (env var)

**Slack webhook:** `SLACK_WEBHOOK_POI_ALERTS` (env var)

---

## 4. Dashboards (Grafana)

### Dashboard: "PoI System Overview"

**Panels (8 total):**

1. **Upload Success Rate (30-day)** - Time series, target line at 99%
2. **Upload Latency Percentiles** - Multi-line (p50/p95/p99), targets at 3s/5s/10s
3. **Validator Uptime (30-day)** - Stat panel (big number), target ≥99.5%
4. **Play Integrity Verdict Distribution** - Pie chart (DEVICE vs STRONG)
5. **Session Entropy Histogram** - Heatmap, threshold line at 0.75
6. **Solana Confirmation Time (p95)** - Time series, target at 30s
7. **Queue Depth (Resilience)** - Time series, alert threshold at 50
8. **Top Rejection Reasons (24h)** - Horizontal bar chart (topk 5)

**Grafana dashboard JSON:** `monitoring/grafana/dashboards/poi-system-overview.json`

**Auto-provisioning:** Git-backed, deployed via Grafana provisioning API

---

### Dashboard: "PoI Debugging"

**Panels (4 total):**

1. **Upload Latency Breakdown (Tracing)** - Stacked bar chart by stage (requires OpenTelemetry)
2. **Retry Rate by Reason** - Time series grouped by `reason` label
3. **Android Version Distribution** - Stacked bar chart (Play Integrity verdict × Android version)
4. **Device-level Success Rate** - Table (show devices with success < 95%)

**Grafana dashboard JSON:** `monitoring/grafana/dashboards/poi-debugging.json`

---

## 5. Synthetic Data Generator (POI-204)

### Tool: `qa/generator/`

**Purpose:** Generate labeled sensor sessions for FAR/FRR/entropy testing

**Approaches:**
1. **Replay Capture:** Anonymize real sessions from volunteer devices, replay with labels
2. **GAN-based Spoofing:** PyTorch time-series GAN to synthesize spoofed movement patterns
3. **Noise Injection:** Apply DP noise (ε ∈ {0.5, 1.0, 2.0}) to observe degradation

**CLI usage:**
```bash
python qa/generator/generate.py \
  --mode replay \
  --input real_sessions.json \
  --output synthetic_batch.json \
  --entropy-target 0.8 \
  --count 1000
```

**Output format:** JSON matching Light Ledger `SessionFingerprint` schema

**Validation:** `pytest qa/generator/tests/` (verify metadata, hash compatibility)

---

### Tool: `qa/load_tester/`

**Purpose:** Submit 1000+ synthetic batches to staging validator

**Implementation:**
```bash
python qa/load_tester/run.py \
  --validator https://staging.validator.poi-lab.pl:443 \
  --batches 1000 \
  --duration 24h \
  --report load_test_report.html
```

**Report includes:**
- Success rate (≥99% target)
- Latency percentiles (p50/p95/p99)
- Rejection reasons breakdown
- Memory/CPU usage over time

---

## 6. Play Integrity Verdict Mock (Offline Testing)

### Tool: `qa/mocks/play_integrity_mock.py`

**Purpose:** Mock Play Integrity API responses for unit tests

**Implementation:**
```python
# qa/mocks/play_integrity_mock.py
def mock_play_integrity_response(nonce: str, verdict: str) -> dict:
    """
    Generate mock Play Integrity token response
    verdict: 'MEETS_DEVICE_INTEGRITY' | 'MEETS_STRONG_INTEGRITY'
    """
    return {
        "token": base64_encode(mock_jwt(nonce, verdict)),
        "device_integrity": {
            "device_recognition_verdict": [verdict]
        }
    }
```

**Usage in tests:**
```rust
// validator/tests/play_integrity_test.rs
#[tokio::test]
async fn test_accepts_device_integrity_verdict() {
    let mock_token = mock_play_integrity_response(
        "test_nonce",
        "MEETS_DEVICE_INTEGRITY"
    );
    assert_eq!(verify_play_integrity(mock_token).await, Ok(()));
}
```

---

## 7. Log Aggregation (Loki / CloudWatch)

**Purpose:** 30-day structured log retention for debugging

**Logs to collect:**
- Validator logs: `kubectl logs -n poi poi-validator-xxxxx`
- Android logcat: Captured via `adb logcat` during testing
- Play Integrity API errors: Logged by validator (redacted tokens)

**Retention policy:** 30 days (configurable)

**Query examples (Loki LogQL):**
```logql
# Find all rejected uploads in last 24h
{app="poi-validator"} |= "STATUS_REJECTED" | json | rejection_reason != ""

# Find Play Integrity failures
{app="poi-validator"} |= "attestation_invalid" | logfmt
```

---

## 8. Implementation Timeline (POI-204)

**Week 1:**
- Implement Prometheus `/metrics` endpoint in validator (Rust)
- Deploy Prometheus + Grafana + AlertManager stack
- Create "PoI System Overview" dashboard

**Week 2:**
- Deploy TimescaleDB for 90-day storage
- Write evidence extraction scripts (`extract_play_integrity_evidence.sh`, etc.)
- Integrate PagerDuty + Slack alerting

**Week 3:**
- Build synthetic data generator (`qa/generator/`)
- Run load test (1000 batches over 24h)
- Collect 7 days production data, recalibrate SLA thresholds

**Owner:** Kai

---

## 9. References

- [monitoring-strategy.md](monitoring-strategy.md) - Full metrics/alert specification
- [production-readiness.md](production-readiness.md) - Deployment checklist
- [sla-definition.md](sla-definition.md) - SLA targets
- [test-plan.json](test-plan.json) - QA scenarios (POI-QA-001 to 004)
- [qa-generator-plan.md](qa-generator-plan.md) - Synthetic data generator details
- [telemetry-proto-plan.md](telemetry-proto-plan.md) - Protobuf schema for telemetry

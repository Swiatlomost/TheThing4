# PoI Monitoring Strategy

**Owner:** Kai
**Status:** Planning (implementation in POI-204)
**Last updated:** 2025-11-22

## Overview

This document defines the monitoring infrastructure for the Proof of Information pipeline, covering metrics collection, alerting rules, dashboard requirements, and data retention policies. Implementation deferred to POI-204 (QA Toolchain).

---

## 1. Metrics Collection

### 1.1 Prometheus Metrics (Validator)

**Endpoint:** `http://validator.poi-lab.pl:9090/metrics`
**Scrape interval:** 15s
**Format:** OpenMetrics / Prometheus exposition format

#### Upload Success Rate

```prometheus
# HELP poi_upload_attempts_total Total number of upload attempts
# TYPE poi_upload_attempts_total counter
poi_upload_attempts_total{status="accepted"} 1234
poi_upload_attempts_total{status="rejected"} 56

# Derived metric (PromQL):
# success_rate = rate(poi_upload_attempts_total{status="accepted"}[30d])
#                / rate(poi_upload_attempts_total[30d])
```

**Labels:**
- `status`: `accepted` | `rejected`
- `rejection_reason`: `attestation_invalid`, `nonce_mismatch`, `low_entropy`, etc. (when status=rejected)

---

#### Upload Latency

```prometheus
# HELP poi_upload_latency_seconds Upload latency histogram (device → validator)
# TYPE poi_upload_latency_seconds histogram
poi_upload_latency_seconds_bucket{le="0.5"} 12
poi_upload_latency_seconds_bucket{le="1.0"} 45
poi_upload_latency_seconds_bucket{le="2.0"} 234
poi_upload_latency_seconds_bucket{le="3.0"} 567
poi_upload_latency_seconds_bucket{le="5.0"} 890
poi_upload_latency_seconds_bucket{le="10.0"} 945
poi_upload_latency_seconds_bucket{le="+Inf"} 950
poi_upload_latency_seconds_sum 2345.67
poi_upload_latency_seconds_count 950
```

**Percentiles (PromQL):**
```promql
histogram_quantile(0.50, rate(poi_upload_latency_seconds_bucket[5m]))  # p50
histogram_quantile(0.95, rate(poi_upload_latency_seconds_bucket[5m]))  # p95
histogram_quantile(0.99, rate(poi_upload_latency_seconds_bucket[5m]))  # p99
```

---

#### Validator Uptime

```prometheus
# HELP poi_validator_up Validator health check (1=up, 0=down)
# TYPE poi_validator_up gauge
poi_validator_up 1
```

**Derived metric (PromQL):**
```promql
# Uptime % over 30 days
avg_over_time(poi_validator_up[30d]) * 100
```

---

#### Session Entropy

```prometheus
# HELP poi_session_entropy Session entropy histogram (0.0 to 1.0)
# TYPE poi_session_entropy histogram
poi_session_entropy_bucket{le="0.5"} 5
poi_session_entropy_bucket{le="0.6"} 12
poi_session_entropy_bucket{le="0.7"} 34
poi_session_entropy_bucket{le="0.75"} 56
poi_session_entropy_bucket{le="0.8"} 234
poi_session_entropy_bucket{le="0.9"} 567
poi_session_entropy_bucket{le="1.0"} 890
poi_session_entropy_bucket{le="+Inf"} 890
poi_session_entropy_sum 678.45
poi_session_entropy_count 890
```

**Alert threshold:** `avg(entropy) < 0.75` for 15 minutes

---

#### Solana Confirmation Time

```prometheus
# HELP poi_solana_confirmation_seconds Solana transaction confirmation latency
# TYPE poi_solana_confirmation_seconds histogram
poi_solana_confirmation_seconds_bucket{le="10"} 234
poi_solana_confirmation_seconds_bucket{le="20"} 567
poi_solana_confirmation_seconds_bucket{le="30"} 789
poi_solana_confirmation_seconds_bucket{le="60"} 850
poi_solana_confirmation_seconds_bucket{le="+Inf"} 900
poi_solana_confirmation_seconds_sum 18456.78
poi_solana_confirmation_seconds_count 900
```

---

#### Queue Length (Resilience)

```prometheus
# HELP poi_validator_queue_length Number of batches queued during Solana outage
# TYPE poi_validator_queue_length gauge
poi_validator_queue_length 0
```

**Alert threshold:** `> 50` batches → Warning

---

#### Upload Retries

```prometheus
# HELP poi_upload_retries_total Total number of upload retry attempts
# TYPE poi_upload_retries_total counter
poi_upload_retries_total{reason="network_timeout"} 12
poi_upload_retries_total{reason="solana_unavailable"} 34
```

---

#### Play Integrity Verdicts

```prometheus
# HELP poi_play_integrity_verdict Play Integrity verdict distribution
# TYPE poi_play_integrity_verdict counter
poi_play_integrity_verdict{type="MEETS_DEVICE_INTEGRITY"} 567
poi_play_integrity_verdict{type="MEETS_STRONG_INTEGRITY"} 234
poi_play_integrity_verdict{type="MEETS_BASIC_INTEGRITY"} 0
```

**Labels:**
- `type`: `MEETS_DEVICE_INTEGRITY` | `MEETS_STRONG_INTEGRITY` | `MEETS_BASIC_INTEGRITY`
- `android_version`: `13`, `14`, `15`, etc.

---

### 1.2 TimescaleDB (Long-term Storage)

**Purpose:** Store aggregated metrics for historical analysis (90+ days).

**Schema:**
```sql
CREATE TABLE poi_metrics_hourly (
  timestamp TIMESTAMPTZ NOT NULL,
  metric_name TEXT NOT NULL,
  metric_value DOUBLE PRECISION NOT NULL,
  labels JSONB,
  PRIMARY KEY (timestamp, metric_name)
);

SELECT create_hypertable('poi_metrics_hourly', 'timestamp');
```

**Example row:**
```json
{
  "timestamp": "2025-11-22T15:00:00Z",
  "metric_name": "poi_upload_success_rate",
  "metric_value": 0.9923,
  "labels": {"period": "1h"}
}
```

**Retention:** 90 days (configurable via TimescaleDB retention policy)

---

## 2. Alert Rules

### 2.1 Critical Alerts (PagerDuty)

| Alert | Condition | Duration | Action |
|-------|-----------|----------|--------|
| **Upload Success Rate Low** | `success_rate < 0.99` | 5 minutes | Page on-call |
| **Validator Down** | `poi_validator_up == 0` | 1 minute | Page on-call |
| **Latency p99 Exceeded** | `p99 > 10s` | 5 minutes | Page on-call |

**Prometheus AlertManager config:**
```yaml
groups:
  - name: poi_critical
    interval: 30s
    rules:
      - alert: PoI_UploadSuccessRateLow
        expr: |
          (rate(poi_upload_attempts_total{status="accepted"}[5m])
           / rate(poi_upload_attempts_total[5m])) < 0.99
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "PoI upload success rate below 99%"
          description: "Current rate: {{ $value | humanizePercentage }}"

      - alert: PoI_ValidatorDown
        expr: poi_validator_up == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "PoI validator is down"

      - alert: PoI_LatencyP99High
        expr: |
          histogram_quantile(0.99,
            rate(poi_upload_latency_seconds_bucket[5m])) > 10
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "PoI p99 latency > 10s"
          description: "Current p99: {{ $value }}s"
```

---

### 2.2 Warning Alerts (Slack)

| Alert | Condition | Duration | Action |
|-------|-----------|----------|--------|
| **Latency p95 Degraded** | `p95 > 5s` | 5 minutes | Slack #poi-alerts |
| **Low Entropy Sessions** | `avg(entropy) < 0.75` | 15 minutes | Slack #poi-alerts |
| **Queue Length High** | `queue_length > 50` | 2 minutes | Slack #poi-alerts |
| **High Retry Rate** | `retries/uploads > 0.05` | 10 minutes | Slack #poi-alerts |

**Prometheus AlertManager config:**
```yaml
  - name: poi_warnings
    interval: 60s
    rules:
      - alert: PoI_LatencyP95Degraded
        expr: |
          histogram_quantile(0.95,
            rate(poi_upload_latency_seconds_bucket[5m])) > 5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "PoI p95 latency > 5s"
          description: "Current p95: {{ $value }}s"

      - alert: PoI_LowEntropyRate
        expr: |
          (sum(rate(poi_session_entropy_sum[15m]))
           / sum(rate(poi_session_entropy_count[15m]))) < 0.75
        for: 15m
        labels:
          severity: warning
        annotations:
          summary: "Average session entropy < 0.75"
          description: "Current avg: {{ $value }}"

      - alert: PoI_QueueLengthHigh
        expr: poi_validator_queue_length > 50
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Validator queue length > 50 batches"
          description: "Current length: {{ $value }}"
```

---

## 3. Dashboard Requirements (Grafana)

### 3.1 Dashboard: "PoI System Overview"

**Panels:**

1. **Upload Success Rate (30-day)**
   - Metric: `poi_upload_success_rate`
   - Visualization: Time series (line chart)
   - Target line: 99%
   - Color: Green (above target), Red (below)

2. **Upload Latency Percentiles**
   - Metrics: p50, p95, p99 from `poi_upload_latency_seconds`
   - Visualization: Time series (multi-line)
   - Target lines: p95=5s, p99=10s

3. **Validator Uptime (30-day)**
   - Metric: `avg_over_time(poi_validator_up[30d]) * 100`
   - Visualization: Stat panel (big number)
   - Target: ≥99.5%

4. **Play Integrity Verdict Distribution**
   - Metric: `poi_play_integrity_verdict`
   - Visualization: Pie chart
   - Breakdown: DEVICE_INTEGRITY vs STRONG_INTEGRITY

5. **Session Entropy Histogram**
   - Metric: `poi_session_entropy`
   - Visualization: Heatmap (time vs entropy buckets)
   - Threshold line: 0.75

6. **Solana Confirmation Time (p95)**
   - Metric: `histogram_quantile(0.95, poi_solana_confirmation_seconds_bucket)`
   - Visualization: Time series
   - Target line: 30s

7. **Queue Depth (Resilience)**
   - Metric: `poi_validator_queue_length`
   - Visualization: Time series
   - Alert threshold: 50 batches

8. **Top Rejection Reasons (24h)**
   - Metric: `topk(5, rate(poi_upload_attempts_total{status="rejected"}[24h]))`
   - Visualization: Bar chart (horizontal)
   - Grouped by `rejection_reason` label

---

### 3.2 Dashboard: "PoI Debugging"

**Panels:**

1. **Upload Latency Breakdown (Tracing)**
   - Stages: LightLedger → WorkManager → Play Integrity → gRPC → Validator
   - Requires: OpenTelemetry tracing integration (future work)

2. **Retry Rate by Reason**
   - Metric: `rate(poi_upload_retries_total[1h])`
   - Grouped by `reason` label

3. **Android Version Distribution**
   - Metric: `poi_play_integrity_verdict` grouped by `android_version`
   - Visualization: Stacked bar chart

4. **Device-level Success Rate**
   - Metric: `poi_upload_attempts_total` grouped by `device_id`
   - Filter: Show only devices with `success_rate < 0.95`

---

## 4. Data Retention Policies

| Data Type | Storage | Retention | Rationale |
|-----------|---------|-----------|-----------|
| **Raw Prometheus metrics** | Prometheus TSDB | 7 days | Short-term debugging, alerts |
| **Aggregated metrics (hourly)** | TimescaleDB | 90 days | Trend analysis, SLA reporting |
| **Aggregated metrics (daily)** | TimescaleDB | 365 days | Long-term capacity planning |
| **Validator logs (structured)** | Loki / CloudWatch | 30 days | Debugging rejected uploads |
| **Play Integrity tokens** | None | N/A | Not stored (GDPR, privacy) |
| **Evidence snapshots** | Git repo | Indefinite | QA acceptance criteria (test-plan.json) |

**Prometheus retention config:**
```yaml
# prometheus.yml
storage:
  tsdb:
    retention.time: 7d
    retention.size: 10GB
```

**TimescaleDB retention policy:**
```sql
-- Drop hourly aggregates after 90 days
SELECT add_retention_policy('poi_metrics_hourly', INTERVAL '90 days');

-- Drop daily aggregates after 365 days
SELECT add_retention_policy('poi_metrics_daily', INTERVAL '365 days');
```

---

## 5. Integration & Tooling

### 5.1 Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Metrics collection** | Prometheus | Scrape validator `/metrics` endpoint |
| **Long-term storage** | TimescaleDB | 90-day aggregated metrics |
| **Visualization** | Grafana | Dashboards (system overview, debugging) |
| **Alerting** | Prometheus AlertManager | Route alerts to PagerDuty/Slack |
| **On-call paging** | PagerDuty | Critical alerts (validator down, SLA breach) |
| **Warnings** | Slack (#poi-alerts) | Non-critical degradation |
| **Log aggregation** | Loki (or CloudWatch) | Structured validator logs (30-day retention) |

---

### 5.2 Validator `/metrics` Endpoint

**Implementation notes (for POI-204):**

1. Use Rust `prometheus` crate:
   ```toml
   [dependencies]
   prometheus = "0.13"
   ```

2. Expose metrics endpoint in validator gRPC server:
   ```rust
   // src/metrics.rs
   use prometheus::{Counter, Histogram, Gauge, Registry};

   pub struct Metrics {
       pub upload_attempts: Counter,
       pub upload_latency: Histogram,
       pub validator_up: Gauge,
       // ...
   }

   // Expose via HTTP on :9090/metrics
   ```

3. Histogram buckets for latency:
   ```rust
   Histogram::with_opts(
       HistogramOpts::new("poi_upload_latency_seconds", "Upload latency")
           .buckets(vec![0.5, 1.0, 2.0, 3.0, 5.0, 10.0])
   )
   ```

---

### 5.3 Grafana Provisioning

**Auto-provision dashboards via Git:**
```yaml
# grafana/provisioning/dashboards/poi.yaml
apiVersion: 1
providers:
  - name: 'PoI Dashboards'
    orgId: 1
    folder: 'PoI'
    type: file
    options:
      path: /etc/grafana/dashboards/poi
```

**Dashboard JSON stored in repo:**
- `grafana/dashboards/poi-system-overview.json`
- `grafana/dashboards/poi-debugging.json`

---

## 6. Validation & Calibration

**Post-deployment (POI-204 + 7 days production):**

1. **Collect 1000+ upload samples** over 7 days
2. **Recalibrate SLA thresholds** based on real p50/p95/p99:
   - If p95 < 3s consistently → tighten SLA to p95 ≤ 3s
   - If p95 > 5s frequently → investigate bottlenecks or relax SLA
3. **Tune alert thresholds** to reduce false positives:
   - Target: < 2 false alerts per week
4. **Update [sla-definition.md](sla-definition.md)** with production-validated values

---

## 7. References

- [sla-definition.md](sla-definition.md) - SLA targets and thresholds
- [production-readiness.md](production-readiness.md) - Deployment checklist
- [test-plan.json](test-plan.json) - QA acceptance criteria
- [qa-tooling-notes.md](qa-tooling-notes.md) - Tooling implementation notes

---

## Implementation Timeline (POI-204)

**Week 1:**
- Implement Prometheus `/metrics` endpoint in validator
- Deploy Prometheus + Grafana + AlertManager stack
- Create initial dashboards (system overview)

**Week 2:**
- Deploy TimescaleDB for long-term storage
- Configure retention policies
- Integrate PagerDuty + Slack alerting

**Week 3:**
- Collect 7 days of production data
- Calibrate alert thresholds (reduce false positives)
- Update SLA based on real percentiles

**Owner:** Kai (POI-204)

# PoI Production Readiness Checklist

**Owner:** Kai
**Status:** Planning (deployment gate for POI-209)
**Last updated:** 2025-11-22

## Overview

This document defines the production readiness criteria for the Proof of Information system. All quality gates must pass before deploying to production (POI-209). This checklist ensures the system meets performance baselines, security requirements, and has appropriate failure handling.

---

## 1. Quality Gates (Pre-Deployment)

### 1.1 Testing

| Gate | Requirement | Verification Method | Status |
|------|-------------|---------------------|--------|
| **Unit tests** | 100% passing | `cargo test --all` (validator) | ⏳ Pending |
| **Integration tests** | 100% passing | `./gradlew test` (Android) | ⏳ Pending |
| **E2E tests** | All scenarios pass | test-plan.json (POI-QA-001 to 004) | ⏳ Pending |
| **Load testing** | 1000+ uploads, success ≥99% | Synthetic generator (POI-204) | ⏳ Pending |
| **Play Integrity** | Both verdicts accepted | Evidence: validator logs (QA-002) | ✅ Done (POI-213) |

**Blocking:** Any test failure blocks deployment until resolved.

---

### 1.2 Security

| Gate | Requirement | Verification Method | Status |
|------|-------------|---------------------|--------|
| **TLS validation** | TLS 1.3 on validator | `curl -vI https://validator.poi-lab.pl` | ✅ Done (POI-213) |
| **Credentials** | No secrets in code/logs | Grep for `API_KEY`, `SECRET` in repo | ⏳ Pending |
| **OAuth tokens** | Service account in env var only | `PLAY_INTEGRITY_SERVICE_ACCOUNT_JSON` set | ✅ Done (POI-213) |
| **Debug code removed** | No NONCE_DEBUG in production | Grep for `NONCE_DEBUG` in validator | ⚠️ Pending cleanup |
| **Dependency scan** | No critical CVEs | `cargo audit` (Rust), Snyk (Android) | ⏳ Pending |

**Blocking:** Critical or high-severity vulnerabilities block deployment.

---

### 1.3 Monitoring

| Gate | Requirement | Verification Method | Status |
|------|-------------|---------------------|--------|
| **Metrics endpoint** | `/metrics` returns data | `curl validator.poi-lab.pl:9090/metrics` | ⏳ Pending (POI-204) |
| **Dashboards** | System overview dashboard live | Grafana accessible | ⏳ Pending (POI-204) |
| **Alerts** | Critical alerts routed to PagerDuty | Test alert fires | ⏳ Pending (POI-204) |
| **Uptime check** | External monitor pings validator | UptimeRobot / Pingdom configured | ⏳ Pending |

**Blocking:** Monitoring must be operational before launch (blind deployment not allowed).

---

## 2. Performance Baselines

### 2.1 SLA Targets (from sla-definition.md)

| Metric | Target | Measurement Window | Current Baseline |
|--------|--------|-------------------|------------------|
| **Upload success rate** | ≥99% | 30 days | ⏳ TBD (need 1000+ samples) |
| **Upload latency (p50)** | ≤3s | 5 minutes | 2.17s (1 sample, WiFi) |
| **Upload latency (p95)** | ≤5s | 5 minutes | ~5s (estimated) |
| **Upload latency (p99)** | ≤10s | 5 minutes | ~10s (estimated) |
| **Validator uptime** | ≥99.5% | 30 days | ⏳ TBD (new deployment) |
| **Solana confirmation (p95)** | ≤30s | 15 minutes | ⏳ TBD (POI-205 pending) |

**Pre-deployment requirement:** Collect 7 days of staging data to validate latency targets are achievable.

---

### 2.2 Load Testing Results (Required)

**Test plan (POI-204):**
1. Generate 1000 synthetic upload batches (entropy ≥0.75)
2. Submit to staging validator over 24 hours
3. Measure:
   - Success rate (target: ≥99%)
   - Latency distribution (p50/p95/p99)
   - Rejection reasons breakdown
4. Tune Play Integrity timeout if needed

**Acceptance:**
- ✅ Success rate ≥99%
- ✅ p95 latency ≤5s
- ✅ No memory leaks (validator RSS stable over 24h)

**Blocking:** Load testing must pass before production deployment.

---

## 3. Security Requirements

### 3.1 Transport Security

| Requirement | Implementation | Verification |
|-------------|----------------|--------------|
| **TLS 1.3** | Nginx reverse proxy on validator | `nmap --script ssl-enum-ciphers -p 443 validator.poi-lab.pl` |
| **Certificate validity** | Let's Encrypt auto-renewal | Cert expires > 30 days |
| **HSTS header** | `Strict-Transport-Security: max-age=31536000` | `curl -I` response header |
| **Client cert (optional)** | Future: mTLS for device auth | ⏳ Deferred to POI-206 |

---

### 3.2 Secrets Management

| Secret | Storage | Access Control | Rotation Policy |
|--------|---------|----------------|-----------------|
| **Play Integrity OAuth** | Environment variable (`PLAY_INTEGRITY_SERVICE_ACCOUNT_JSON`) | Root only (chmod 600) | 90 days |
| **Solana private key** | CloudHSM (POI-203) | HSM-backed, no export | N/A (hardware-protected) |
| **Database password** | Docker secrets / Vault | Container runtime only | 30 days |

**Enforcement:**
- ❌ Never commit secrets to Git
- ❌ Never log secrets (even DEBUG level)
- ✅ Use environment variables or secure stores only

---

### 3.3 Attack Surface

| Vector | Mitigation | Status |
|--------|------------|--------|
| **DDoS (validator)** | Rate limiting (100 req/min per IP), Cloudflare | ⏳ Pending |
| **Spoofed Play Integrity tokens** | Verify signature via Google API | ✅ Implemented (POI-211) |
| **Nonce replay** | Nonce expires after 5 minutes | ✅ Implemented (POI-213) |
| **Low-entropy sessions** | Reject entropy < 0.75 | ✅ Implemented (POI-101) |
| **Man-in-the-middle** | TLS 1.3 + certificate pinning (Android) | ⚠️ TLS done, pinning pending |

---

## 4. Failure Modes & Fallbacks

### 4.1 Play Integrity API Failure

**Scenario:** Google Play Integrity API returns 5xx or timeout (>30s).

**Behavior:**
- Validator returns `STATUS_REJECTED` with reason `attestation_unavailable`
- Client retries with exponential backoff (max 3 attempts)
- If persistent failure (>1 hour), alert on-call

**Fallback:** None (authenticity is non-negotiable, cannot bypass).

**Monitoring:**
- Alert: `attestation_unavailable_rate > 0.05` (5%) for 10 minutes → Page

---

### 4.2 Solana Network Outage

**Scenario:** Solana devnet/mainnet unavailable (network partition, congestion).

**Behavior:**
- Validator queues batches in-memory (max 60 minutes retention)
- Replays queued batches when Solana recovers
- If queue exceeds 50 batches → Warning alert

**Fallback:** Persist queue to disk if RAM exhausted (max 1000 batches).

**Monitoring:**
- Metric: `poi_validator_queue_length`
- Alert: `queue_length > 50` for 2 minutes → Slack warning

**Recovery SLA:** Batches submitted within 60 minutes of Solana recovery.

---

### 4.3 Validator Crash

**Scenario:** Validator process crash (panic, OOM, segfault).

**Behavior:**
- Kubernetes restarts pod automatically (restart policy: `Always`)
- In-memory queue lost (trade-off: simplicity vs persistence)
- Alerts fire: `poi_validator_up == 0`

**Fallback:** Clients retry uploads (WorkManager ensures eventual delivery).

**Monitoring:**
- Alert: `validator_up == 0` for 1 minute → Page on-call
- Metric: `poi_validator_restarts_total` (counter)

**Mitigation:**
- Set memory limits: `resources.limits.memory: 2Gi`
- Enable core dumps for post-mortem analysis

---

### 4.4 Database Unavailable (TimescaleDB)

**Scenario:** TimescaleDB down (metrics storage).

**Behavior:**
- Prometheus continues scraping (7-day retention)
- Aggregated metrics not written to TimescaleDB
- Dashboards show data gaps

**Fallback:** Backfill from Prometheus after DB recovery.

**Monitoring:**
- Alert: `timescaledb_up == 0` for 5 minutes → Slack warning

**Non-blocking:** Validator continues processing uploads (metrics storage is decoupled).

---

### 4.5 Network Partition (Client → Validator)

**Scenario:** Client cannot reach validator (firewall, DNS failure, network outage).

**Behavior:**
- Android WorkManager retries with exponential backoff (up to 24 hours)
- Upload queued locally on device (SQLite database)
- User sees "Upload pending" notification

**Fallback:** Manual sync button in app (force retry now).

**Monitoring:**
- Metric: `poi_upload_retries_total` (client-side telemetry)

---

## 5. Deployment Checklist

### 5.1 Pre-Deployment (Staging)

- [ ] All unit/integration tests passing
- [ ] Load testing completed (1000+ uploads, success ≥99%)
- [ ] Security scan passed (no critical CVEs)
- [ ] TLS certificate valid (expires > 30 days)
- [ ] Secrets configured in environment (OAuth, HSM keys)
- [ ] NONCE_DEBUG removed from validator code
- [ ] Monitoring stack deployed (Prometheus, Grafana, AlertManager)
- [ ] Alerts tested (trigger test alert, verify PagerDuty page)
- [ ] Dashboards accessible (Grafana login works)
- [ ] External uptime monitor configured (UptimeRobot)
- [ ] Rollback plan documented (see §5.3)

---

### 5.2 Deployment (Production)

**Deployment method:** Blue-green deployment (zero downtime)

**Steps:**
1. Deploy new validator version to "green" environment
2. Smoke test: Submit 10 uploads from test device, verify STATUS_ACCEPTED
3. Route 10% traffic to green (canary deployment)
4. Monitor for 30 minutes:
   - Success rate ≥99%
   - p95 latency ≤5s
   - No error alerts
5. If healthy: Route 100% traffic to green, decomission blue
6. If unhealthy: Rollback to blue (see §5.3)

**Deployment window:** Non-peak hours (UTC 02:00-06:00)

---

### 5.3 Rollback Plan

**Trigger conditions:**
- Success rate drops below 95% for 5 minutes
- p99 latency exceeds 20s for 5 minutes
- Critical alert fires (validator crash loop)

**Rollback steps:**
1. Route 100% traffic back to blue environment (DNS/load balancer flip)
2. Verify blue is healthy (success rate ≥99%)
3. Investigate green failure (check logs, metrics)
4. Fix issue in staging, retest, redeploy

**Rollback SLA:** < 5 minutes from decision to traffic restored

**Automation:** Rollback script (`scripts/rollback.sh`) triggered by on-call

---

### 5.4 Post-Deployment

- [ ] Verify 100% traffic on new version (check validator logs)
- [ ] Monitor dashboards for 24 hours (success rate, latency, errors)
- [ ] Collect 7 days of production data
- [ ] Recalibrate SLA thresholds based on real percentiles (update sla-definition.md)
- [ ] Tune alert rules (target: <2 false positives per week)
- [ ] Document any issues in retrospective (add to PDCA.json "act" section)

---

## 6. Operational Runbooks

### 6.1 Runbook: Upload Success Rate < 99%

**Symptoms:**
- Alert: `PoI_UploadSuccessRateLow` (PagerDuty page)
- Dashboard shows red line on success rate panel

**Diagnosis:**
1. Check rejection reasons: `topk(5, rate(poi_upload_attempts_total{status="rejected"}[5m]))`
2. Common causes:
   - `attestation_invalid` → Play Integrity API issue (Google-side)
   - `nonce_mismatch` → Clock drift or replay attack
   - `low_entropy` → Clients submitting poor-quality data

**Resolution:**
- If `attestation_invalid` spike → Check Google Cloud Status page, wait for recovery
- If `nonce_mismatch` → Check validator system time (`date`), sync NTP if drifted
- If `low_entropy` → Investigate client sensor data quality (device-specific issue?)

**Escalation:** If unresolved after 30 minutes, page Kai (POI owner)

---

### 6.2 Runbook: Validator Down

**Symptoms:**
- Alert: `PoI_ValidatorDown` (PagerDuty page)
- Dashboard shows `validator_up == 0`

**Diagnosis:**
1. SSH to validator host: `ssh root@validator.poi-lab.pl`
2. Check Kubernetes pod status: `kubectl get pods -n poi`
3. Check pod logs: `kubectl logs -n poi poi-validator-xxxxx --tail=100`

**Resolution:**
- If pod crash loop → Check logs for panic/OOM, restart with higher memory limit
- If Kubernetes node down → Failover to backup node (if multi-node cluster)
- If persistent failure → Rollback to previous version (§5.3)

**Escalation:** If unresolved after 10 minutes, page infrastructure team

---

### 6.3 Runbook: Latency Spike (p99 > 10s)

**Symptoms:**
- Alert: `PoI_LatencyP99High` (PagerDuty page)
- Dashboard shows latency spike

**Diagnosis:**
1. Check Play Integrity API latency (dominant bottleneck):
   - If Play Integrity slow → Google-side issue, wait for recovery
2. Check validator CPU/memory: `kubectl top pods -n poi`
   - If CPU > 80% → Scale up replicas or increase CPU limit
3. Check Solana confirmation time (if applicable):
   - If Solana slow → Network congestion, queue batches

**Resolution:**
- If temporary spike → Monitor, latency should recover within 10 minutes
- If sustained spike → Scale validator horizontally (add replicas)

---

## 7. Capacity Planning

### 7.1 Current Capacity (Single Validator Instance)

**Baseline performance:**
- Validator processing: ~2ms per batch
- Max throughput: ~500 batches/second (CPU-bound)
- Expected load: ~10 batches/minute (150 devices × 1 batch per 15min)

**Headroom:** 3000x (500 batch/s capacity vs 0.17 batch/s expected)

**Scaling trigger:** CPU > 60% for 10 minutes → Add replica

---

### 7.2 Growth Projections

| Timeline | Device Count | Batches/min | Validator Replicas | Notes |
|----------|--------------|-------------|-------------------|-------|
| **Launch (Week 1)** | 150 | 10 | 1 | Pilot users |
| **Month 3** | 1,000 | 67 | 1-2 | Public beta |
| **Month 6** | 10,000 | 667 | 5-7 | General availability |
| **Year 1** | 100,000 | 6,667 | 50-70 | Scale-out |

**Bottleneck:** Play Integrity API (1.4s per request) → Cannot parallelize per upload

**Mitigation:** Horizontal scaling (load balancer → N validator replicas)

---

## 8. References

- [sla-definition.md](sla-definition.md) - SLA targets and thresholds
- [monitoring-strategy.md](monitoring-strategy.md) - Metrics, alerts, dashboards
- [test-plan.json](test-plan.json) - QA scenarios (POI-QA-001 to 004)
- [PDCA.json](PDCA.json) - POI-103 planning and retrospective

---

## Sign-Off

**Before deploying to production (POI-209), this checklist must be reviewed and approved by:**

- [ ] **Kai** (QA owner) - All quality gates passed
- [ ] **Nodus** (Infrastructure) - Security requirements met
- [ ] **Orin** (Coordinator) - Business readiness confirmed

**Deployment authorized by:** _______________ (Date: _______)

## QA Tooling Notes

- **Metrics collection**: integrate with Light Ledger telemetry (FAR, FRR, entropy) via gRPC streaming; store in TimescaleDB.
- **Evidence capture**: automated scripts export batch verification logs, Solana transaction hashes, attestation verdicts.
- **Alerting**: thresholds from test-plan.json wired into Grafana + PagerDuty (Kai owner).
- **Next Steps**:
  1. Define protobuf schema for QA telemetry alongside validator API.
  2. Prepare synthetic data generator to simulate low-entropy and spoofed sessions.
  3. Draft Play Integrity verdict mock for offline tests.

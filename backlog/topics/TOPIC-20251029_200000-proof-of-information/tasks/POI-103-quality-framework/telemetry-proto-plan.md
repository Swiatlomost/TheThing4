## Telemetry Schema Plan

- Extend `validator.proto` with streaming endpoint `SubscribeMetrics` (future work) to feed QA dashboards.
- Define protobuf message:
  ```proto
  message BatchTelemetry {
    string batch_id = 1;
    double entropy_avg = 2;
    double trust_avg = 3;
    double far = 4;
    double frr = 5;
    uint64 processed_at = 6;
  }
  ```
- Kai to coordinate with Nodus to publish metrics via gRPC stream + push to TimescaleDB.
- Baseline data collection:
  - Use synthetic generator (qa-generator-plan) to produce 50 batches covering entropy 0.6–0.95.
  - Capture real-device baseline once Light Ledger prototype exports first batches.
- Evidence capture scripts to export telemetry snapshots for inclusion in test-plan scenarios.

## PKCS#11 PoC Notes

### Goals
- Compare signing latency between AWS CloudHSM and GCP CloudKMS fallback.
- Validate Rust integration via `cryptoki` crate and ensure thread-safety.

### Setup
1. Provision staging CloudHSM (single AZ) and install client on validator VM.
2. Configure Rust validator with PKCS#11 slot `0` and label `poi-validator`.
3. Implement benchmark command `validator-cli hsm-bench --iterations 500`.
4. Repeat with CloudKMS using REST API + HSM proxy.

### Metrics
- Average signing latency (ms).
- 95th percentile latency.
- Error rate / reconnect incidents.
- CPU overhead vs. software signing.

### Deliverables
- Benchmark report appended to `validator-plan.md`.
- Decision entry in PDCA (act) summarizing preferred configuration.

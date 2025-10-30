## Synthetic Data Generator Plan

### Goals
- Produce labeled sensor sequences for validating FAR/FRR, entropy thresholds, and spoofing detection.

### Approaches
1. **Replay Capture**: record real sessions (accelerometer, touch) from volunteer devices; anonymise and replay.
2. **GAN-based Spoofing**: use PyTorch time-series GAN to synthesize spoofed movement patterns.
3. **Noise Injection**: apply DP noise (ε ∈ {0.5, 1.0, 2.0}) to real traces to observe degradation.

### Deliverables
- Python toolkit (`qa/generator/`) that exports JSON sessions matching Light Ledger schema.
- CLI flags to set entropy target, spoofing mode, DP noise level.
- Unit tests verifying metadata and hash compatibility.

### Dependencies
- Requires Kotlin Light Ledger schema (`SessionFingerprint`) for schema alignment.
- Needs validator protobuf definitions for metrics compatibility.

### Timeline
- Week 1: capture & anonymise baseline data.
- Week 2: implement GAN/noise injection modules.
- Week 3: integrate with automated QA pipeline.

## PoI Implementation Sequence & Dependencies

1. **Android Foundation**
   - `POI-201-android-scaffold` → scaffolds modules + JNI bridge skeleton.
   - `POI-207-sensor-capture` depends on 201; provides baseline data for QA.

2. **Validator Backend**
   - `POI-202-validator-bootstrap` sets up gRPC server + CI.
   - `POI-203-cloudhsm-provisioning` supplies HSM infrastructure for signing (needed before production).
   - `POI-205-solana-program` requires working validator payload (202) to confirm schema.

3. **Integration & Security**
   - `POI-101-light-ledger-prototype` (in progress) produces device batches.
   - `POI-206-attestation-integration` depends on 101 + validator API (202) + secure aggregation client.

4. **Quality & Telemetry**
   - `POI-103-quality-framework` defines acceptance; `POI-204-qa-toolchain` implements telemetry/monitoring using data from 201/202/207.

5. **Deployment & Alternate Chain**
   - `POI-209-deployment-coordination` orchestrates rollout gates before launch.
   - `POI-208-sui-spike` starts after Solana PoI v1 (205) to evaluate backup chain.

### Known Blockers / Needs
- Source repositories (Android & Rust) not yet created.
- Access to Solana devnet credentials and CloudHSM account.
- CI runners with Android SDK, Rust, Terraform.
- Install `cargo-ndk` + Android NDK locally before attempting native builds; record generated `.so` under `build/generated/jniLibs/`.
- Play Integrity keys + secure aggregation client required prior to POI-206 implementation.

### Recommended Order
1. POI-201 → POI-207 → POI-206  
2. POI-202 → POI-203 → POI-205  
3. POI-103 → POI-204  
4. POI-209 (parallel coordination)  
5. POI-208 after Solana launch

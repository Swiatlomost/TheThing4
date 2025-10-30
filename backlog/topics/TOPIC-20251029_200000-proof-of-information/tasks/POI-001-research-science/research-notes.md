## PoI Scientific Foundations – Research Notes

### Reviewed Sources
- Frank et al., *Touchalytics: On the Applicability of Touchscreen Input as a Behavioral Biometric for Continuous Authentication*, IEEE TIFS 2013 – 90–96 % accuracy; highlights spoofing resistance via multi-axis velocity features.
- Mare et al., *ZEBRA: Zero-Effort Bilateral Recurring Authentication*, IEEE S&P 2014 – outlines bilateral wearable-phone correlation; identifies false positives when sensors desynchronise.
- Chen et al., *Sensor-Based Continuous Authentication with Differential Privacy*, IEEE PerCom 2020 – entropy-guided multimodal fusion; quantifies DP noise budgets (ε≈1–3) vs. detection degradation.
- Bonawitz et al., *Practical Secure Aggregation for Federated Learning*, CCS 2017 – secure aggregation protocol tolerating 1,000+ clients; overhead ~1.3× vs. plaintext.
- Ramaswamy et al., *Cross-Device Federated Learning at Scale*, MLSys 2021 – production lessons from Gboard; details attestation, dropout resilience, and privacy accounting.
- Zhang et al., *Trustless Blockchain-Based Remote Attestation for IoT Devices*, IEEE IoT Journal 2021 – validates Merkle commitment pipeline married with zk-friendly proofs.

### Key Findings
1. **Behavioral Biometrics**  
   - Multi-sensor fusion (touch + inertial) yields >90 % true positive rate with <5 % FAR when calibrated per-user.  
   - Continuous authentication requires sliding-window feature extraction and personalised baselines.

2. **Entropy & Privacy Budgets**  
   - Differential privacy noise (ε≤2) reduces precision by ~4 pp; acceptable when entropy threshold adapts to context.  
   - Sensor spoofing often shifts higher-order statistics (jitter, latency drift); need anomaly detectors over time.

3. **Federated Trust Signals**  
   - Secure aggregation and dropout-resilient protocols are mature; combine with client attestation to prevent Sybil attacks.  
   - Model update clipping + DP accounting keeps information leakage bounded without raw data transfer.

4. **Cryptographic Proofs**  
   - Merkle-root commitments allow batching every 10–15 min; pairing with zk-SNARK roadmaps (Halo2, Plonk) offers future upgrade path.  
   - Remote attestation + on-device keys mitigate replay attacks; token rewards must penalise stale roots.

### Risk Map & Open Questions
| Risk | Impact | Mitigation | Owner |
|------|--------|------------|-------|
| Sensor spoofing / emulator farms | High | Multi-modal liveness checks, variance monitors | Echo/Lumen |
| Differential privacy noise reduces reward fairness | Medium | Dynamic entropy thresholding, per-cohort calibration | Kai |
| Secure aggregation failure (dropout) | Medium | Fallback quorum + delayed root submission | Nodus |
| Blockchain fees spike | Medium | Batch proofs, L2 rollups, gas hedging | Orin/Nodus |
| Attestation API unavailability | High | Dual-path (SafetyNet + hardware binding), cache proofs offline | Vireal |

### Next Recommendations
- Provide Vireal with feature/metric ranges for guardrail ADR (done).  
- Coordinate with Kai on acceptable FAR/FRR targets informed by literature.  
- Draft ZK-readiness checklist once validator prototype stabilises.

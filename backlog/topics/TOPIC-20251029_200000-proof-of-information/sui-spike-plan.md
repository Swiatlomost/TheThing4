## Sui Spike Plan

### Objectives
- Prototype PoI root storage using Sui Move object model.
- Evaluate zkLogin flow for device attestation.
- Measure shared-object transaction latency vs. Solana baseline.

### Scope
1. Implement Move module storing PoI root object with trust/entropy metadata.
2. Build Rust client submitting batches via Sui SDK.
3. Integrate zkLogin mock to validate attestation binding.

### Timeline
- **Week 1**: set up Sui devnet environment, scaffold Move module.
- **Week 2**: implement client + benchmark throughput/finality.
- **Week 3**: document findings, decide on roadmap adoption.

### Owners
- Vireal (architecture oversight)
- Nodus (integration and infra)
- Kai (latency/quality measurements)

### Deliverables
- `sui-poi.move` module + documentation.
- Benchmark report comparing Solana vs Sui latency/failures.
- Decision memo for Orin on whether to adopt Sui as secondary chain.

### Kickoff Session
- **Facilitator**: Orin
- **Participants**: Vireal, Nodus, Kai
- **Trigger**: first successful Solana PoI batch deployment (target milestone: Sprint 2025-11-07).
- **Agenda**:
  1. Review Solana metrics vs. Sui objectives.
  2. Confirm resource allocation for Week 1 tasks.
  3. Define success criteria for adopting Sui.

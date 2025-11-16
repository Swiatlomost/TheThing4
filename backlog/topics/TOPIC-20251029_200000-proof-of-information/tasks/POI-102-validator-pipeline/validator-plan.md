## Validator & Integration Plan

### HSM / Key Management
- **Primary**: AWS CloudHSM cluster (cm-small) – provides dedicated hardware and integrates with Rust via PKCS#11.
- **Fallback**: GCP CloudKMS with EKM connector; slightly higher latency but simpler ops.
- Rotate validator signing key every ≤30 dni; store public key fingerprints in config repo.

### Attestation Flow
1. Device includes Play Integrity verdict + nonce in batch payload.
2. Validator verifies verdict using Google REST API (cached service account credential).
3. If StrongBox unavailable, ensure nonce ties to ledger batch and store audit trail in PostgreSQL.

### gRPC API
```
service PoIValidator {
  rpc SubmitBatch (BatchProofRequest) returns (BatchProofResponse);
  rpc QueryStatus (BatchStatusRequest) returns (BatchStatusResponse);
}
```
- `BatchProofRequest` zawiera teraz: Merkle root (`bytes merkle_root`), list� metryk z hashami (`repeated BatchEntryMetrics { hash bytes }`), podpis DER (`bytes signature`), klucz publiczny X.509 (`bytes public_key`) oraz attestation (`AttestationPayload`).
- Serwis odrzuca brakuj�ce hash'e, b��dny Merkle root, podpis lub attestation zanim doda batch do kolejki.
- `BatchProofResponse` zwraca ID batcha, status (`STATUS_ACCEPTED`, `STATUS_REJECTED`), pow�d oraz przewidywany slot na blockchain.

### Walidacja w serwisie
1. **Hash chain** �?" ka�da metryka musi mie� 32-bajtowy hash; Merkle root liczony jak w `LightLedger` (duplikacja ostatniego li�cia przy nieparzystej warstwie).
2. **Podpis** �?" validator weryfikuje podpis `SHA256withECDSA` nad Merkle rootem u�ywaj�c przes�anego klucza publicznego (DER, p256). B��d -> `signature_invalid_*`.
3. **Attestation** �?" wymagany provider `play_integrity`, token i nonce (walidacja strukturalna; pe�na weryfikacja tokena do wdro�enia po pozyskaniu kluczy Google).
4. **Stan** �?" przy sukcesie status `STATUS_ACCEPTED` i tag `validated`; przy b��dzie `STATUS_REJECTED` z kodem w polu `reason`.

### SubmitBatch Smoke (Xiaomi 13T Pro sample)

- Artefakty źródłowe:  
  - Logcat: `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-101-light-ledger-prototype/evidence/light-ledger-runtime-smoke-2025-10-31.log`  
  - Hash-chain: `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-101-light-ledger-prototype/evidence/hash-chain-snapshot-2025-10-31.txt`
- Przykładowy `BatchProofRequest` payload (proto JSON �?" bytes jako Base64):
```json
{
  "deviceId": "C0S-XIAOMI-13TPRO",
  "batchStart": 1761924794000,
  "batchEnd": 1761924795000,
  "merkleRoot": "JH8RuLauG/T1AqholC1AMkKJKFqVBD5iRGDvGd3i/Zg=",
  "metrics": [
    {
      "ledgerIndex": 0,
      "entropy": 0.82,
      "trustScore": 92,
      "far": 0,
      "frr": 0,
      "hash": "JH8RuLauG/T1AqholC1AMkKJKFqVBD5iRGDvGd3i/Zg=",
      "timestampMs": 1761924794783
    }
  ],
  "signature": "MEUCIDF8XnnxaKOVc+BNokofqz8lGd8FOFOsayISacj5cB1dAiEA2/I0LL4m04FA3juWNLdayYLJGwjEVDwhmTsw/0yLpN4=",
  "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjgSymnlgYBmuG82v3kDo1wj+hMNnyskoGGzwUyI4pMbVfSU1akEMOxyTznOQfI+ZDI54E0i/PB/IF0ctaxl8/w==",
  "attestation": {
    "provider": "play_integrity",
    "nonce": "sample-nonce-20251031",
    "token": "ZHVtbXktYXR0ZXN0YXRpb24tdG9rZW4="
  }
}
```
- Przykład JSON w repo: `evidence/submit-batch-sample.json`.
- Przykład JSON z tokenem attestation: `validator/evidence/submit-batch-sample-attested.json`.
- Komenda smoke (PowerShell):
  ```powershell
  cd D:\TheThing4\TheThing4
  D:\Tools\grpcurl.exe `
    -plaintext `
    -proto proto\validator.proto `
    -import-path proto `
    -d @ `
    localhost:50051 poi.validator.PoIValidator/SubmitBatch `
    < validator\evidence\submit-batch-sample-attested.json
  ```
- Oczekiwany rezultat: `status=accepted`, `reason=validated`, zapis stanu w pamięci i log `Batch validated`. W logach pojawi się Merkle root w Base64.
- Testy jednostkowe pokrywające walidację (`cargo test` w katalogu `validator`) oraz prototypowy kontener `docker compose up validator`.

### Uruchomienie lokalne (Docker Compose)

`
cd validator
docker compose up --build validator
`

- Kontener alidator buduje binarkę Rust (toolchain 1.83) i wystawia gRPC na localhost:50051.
- Kontener softhsm dostarcza lokalny magazyn PKCS#11 (persistowany w wolumenie softhsm-data) – obraz buduje się lokalnie z alidator/softhsm/Dockerfile, więc nie wymaga logowania do zewnętrznego registry.
- Po starcie możesz użyć polecenia grpcurl z sekcji powyżej; logi serwisu dostępne są przez docker compose logs -f validator.

### Solana Devnet Tasks
- Deploy PoI program using Anchor; record `{root, trust_avg, entropy_avg, validator_sig}`.
- Set batching interval 15 min; ensure compute units < 200k per transaction.
- Configure monitoring: Solana RPC health, transaction confirmation time, fee tracker.

### Integration Checklist

- [x] Zaimportować próbkę hash-chain/logcat (Xiaomi 13T Pro) i wykorzystać ją w testowym SubmitBatch.
- [ ] Provision CloudHSM cluster and PKCS#11 client in staging.
- [ ] Implement attestation verifier service + caching (pełna walidacja JWT, cache JWK Google).
- [x] Build Rust validator service skeleton (Tonic) z walidacją Merkle + podpisu + stubem attestation.
- [ ] Write Solana Anchor program + tests on devnet.
- [ ] Set up observability dashboard (Grafana) for batch metrics.
- [ ] Document recovery procedure if Solana outage >30 min (queue & re-submit).









## POI-202 – Validator Bootstrap Notes

### Repository Layout (suggested)
```
validator/
  Cargo.toml
  src/
    main.rs
    generated/          # tonic-build output
    service.rs
proto/
  validator.proto       # symlink / submodule from backlog reference
ci/
  github/
    workflows/validator.yml
```

### Setup Steps
1. **Initialize crate**
   - `cargo new validator --bin`
   - Add dependencies: `tonic`, `prost`, `tokio`, `tracing`, `serde`.

2. **Proto generation**
   - Place `validator.proto` under `proto/`.
   - Add `build.rs` with `tonic_build::compile_protos`.
   - Optional: integrate `buf lint` + `buf generate`.

3. **Skeleton server**
   ```rust
   #[derive(Default)]
   pub struct ValidatorService;

   #[tonic::async_trait]
   impl poi::validator::po_i_validator_server::PoIValidator for ValidatorService {
       async fn submit_batch(...) -> Result<Response<BatchProofResponse>, Status> {
           Ok(Response::new(BatchProofResponse::accepted()))
       }
   }
   ```
   - Include `/healthz` endpoint for readiness.

4. **CI Workflow**
   - Build & run unit tests on push.  
   - Validate `buf lint` via dedicated job.

5. **Local Testing**
   - Provide `scripts/run_dev.sh` to start server with mock CloudKMS signing (placeholder).  
   - gRPC health check script (Python/Go) validating handshake.

### Dependencies / Blocking
- Final PKCS#11 integration tracked in POI-203; use mock signer until HSM ready.
- Solana program not yet available (POI-205) – store batches in PostgreSQL placeholder.
- Need container image definition once service stabilizes.

### References
- `validator-plan.md`
- `validator.proto`
- `grpc-stub-plan.md`

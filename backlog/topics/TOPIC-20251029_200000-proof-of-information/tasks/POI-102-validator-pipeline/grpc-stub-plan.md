## gRPC Stub Generation Plan

- **Rust server/client**
  - Use `tonic-build` in build.rs to generate server/service stubs from `validator.proto`.
  - Output modules `poi::validator` under `src/generated`.
  - Enforce `prost-types` optional fields for metrics arrays.
- **TypeScript client**
  - Generate via `buf generate` with `grpcweb` plugin to support monitoring dashboards.
  - Publish to `packages/poi-validator-client` for QA + dashboards.
- **Automation**
  - Add CI step `buf lint` + `buf generate` check.
  - Ensure proto versioning documented (`proto/README.md`).
- **Next Steps**
  1. Add `validator.proto` to repo root under `proto/`.
  2. Wire tonic build script; provide sample server handshake test.
  3. Publish npm package after integration tests pass.

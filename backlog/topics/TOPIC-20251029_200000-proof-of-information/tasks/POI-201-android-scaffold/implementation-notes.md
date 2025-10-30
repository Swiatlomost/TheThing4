## POI-201 â€“ Android Prototype Bootstrap Notes

### Repo Structure (proposed)
```
android/
  app/                # demo UI showing batch status
  lightledger/        # library module with JNI bridge
  build.gradle.kts
rust/
  light_ledger/       # Cargo crate compiled via cargo-ndk
scripts/
  build_android.sh    # gradlew assembleDebug + cargo-ndk build
```

### Immediate TODOs (after repo exists)
1. **Initialize Gradle project**  
   - `gradle init --type kotlin-application` (or Android Studio).  
   - Apply Kotlin DSL + configure `externalNativeBuild`.

2. **Rust setup**  
   - Add `Cargo.toml` with crate skeleton (`cdylib`).  
   - Include dependencies: `jni`, `sha2`, `serde`.

3. **cargo-ndk configuration**  
   - `cargo ndk -t arm64-v8a -o ../android/lightledger/src/main/jniLibs build`.
   - Add script to run within CI.

4. **JNI bridge skeleton**  
   - Kotlin `object LightLedgerNative { init { System.loadLibrary("light_ledger") } ... }`.  
   - Rust `#[no_mangle] pub extern "system" fn Java_io_thething_LightLedgerNative_buildMerkleRoot(...)`.

5. **Baseline instrumentation test**  
   - Validate JNI call returns placeholder root.  
   - Measure build time and capture output for PDCA `do`.

### Dependencies / Blocking
- Repo Android/Rust not yet present â€“ need scaffolding approval.  
- Access to sensor data harness (POI-207) before real fingerprints.  
- CI runner with Android SDK + Rust toolchain.

### References
- `backlog/topics/.../light-ledger-design.md`  
- `backlog/topics/.../jni-bridge-plan.md`

### Native build
- U¿yj `cargo install cargo-ndk` i uruchom Gradle z `-PenableCargoNdk=true`, aby generowaæ biblioteki (.so) dla arm64-v8a i x86_64.
- Domyœlnie Gradle pomija budowê natywn¹, fallback hash dzia³a w Kotlinie.

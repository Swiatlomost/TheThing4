## POI-201 - Android Prototype Bootstrap Notes

### Repo Structure (proposed)
```
android/
  app/                # demo UI showing batch status
  lightledger/        # library module with JNI bridge
  build.gradle.kts
rust/
  light_ledger/       # Cargo crate compiled via cargo-ndk
scripts/
  build_poi_android.sh    # gradlew assembleDebug + cargo-ndk build
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
- Repo Android/Rust not yet present - need scaffolding approval.  
- Access to sensor data harness (POI-207) before real fingerprints.  
- CI runner with Android SDK + Rust toolchain.

### References
- `backlog/topics/.../light-ledger-design.md`  
- `backlog/topics/.../jni-bridge-plan.md`

### Native build
- Use `cargo install cargo-ndk` and run Gradle with `-PenableCargoNdk=true` to produce `.so` libraries for arm64-v8a and x86_64 (automation in `scripts/build_poi_android.sh`).
- After installation verify with `cargo ndk --version`; on Windows the binary lands in `%USERPROFILE%\.cargo\bin\` so either add it to `PATH` or invoke via the full path.
- By default Gradle skips the native build; the Kotlin fallback hash remains available.

### Release signing (Play Console upload)
- Gradle expects the following properties or environment variables:
  - `poiUploadStoreFile` / `POI_UPLOAD_STORE_FILE` – ścieżka do `upload-keystore.jks` (może być względna względem root).
  - `poiUploadStorePassword` / `POI_UPLOAD_STORE_PASSWORD`
  - `poiUploadKeyAlias` / `POI_UPLOAD_KEY_ALIAS`
  - `poiUploadKeyPassword` / `POI_UPLOAD_KEY_PASSWORD`
- Jeśli któraś wartość nie jest ustawiona, `./gradlew bundleRelease` przerwie build jasnym komunikatem.
- Generowanie klucza upload (`upload-keystore.jks`):
  ```bash
  keytool -genkeypair \
    -alias poi-upload \
    -keyalg RSA \
    -keysize 4096 \
    -validity 10000 \
    -keystore upload-keystore.jks
  ```
- Nie commitujemy keystore do repo; trzymaj plik poza VCS i dodaj do `.gitignore`. W Play Console zarejestruj fingerprint tego klucza jako Upload Key.
